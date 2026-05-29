package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

public class TableInDataSetConstraintEnumeration implements ConstraintEnumeration<String>
{
	public static final String MESSAGE = "Table must be the name of a table in the data set.";
	private String dataSetName;

	public String getDataSetName()
	{
		return dataSetName;
	}

	public void setDataSetName(String dataSetName)
	{
		this.dataSetName = dataSetName;
	}

	public static final UnaryPredicate<AdaptationTable> TRUE = new UnaryPredicate<AdaptationTable>()
	{
		@Override
		public boolean test(AdaptationTable object)
		{
			return true;
		}
	};

	/**
	 * Given a value context, find the data set in which to look for the table.  Override this method
	 * if you need to look in another data set than that of the context itself.
	 * @param context
	 * @return Adaptation representing the data set
	 */
	public Adaptation getDataSet(ValueContext context)
	{
		Adaptation dataSet = context.getAdaptationTable().getContainerAdaptation();
		if (dataSetName != null)
			dataSet = dataSet.getHome().findAdaptationOrNull(AdaptationName.forName(dataSetName));
		return dataSet;
	}

	/**
	 * Override this method if you want to filter the tables to include.
	 */
	public UnaryPredicate<AdaptationTable> getTableFilter()
	{
		return TRUE;
	}

	@Override
	public void checkOccurrence(String value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		AdaptationTable table = getTable(getDataSet(context), value);
		if (table == null)
		{
			context.addError(value + " is not a the name of a table.");
		}
	}

	public static AdaptationTable getTable(Adaptation dataSet, String tableName)
	{
		try
		{
			return dataSet.getTable(Path.parse(tableName));
		}
		catch (PathAccessException e)
		{
			return null;
		}
	}

	@Override
	public void setup(ConstraintContext constraintContext)
	{
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	@Override
	public String displayOccurrence(String value, ValueContext context, Locale locale)
		throws InvalidSchemaException
	{
		try
		{
			AdaptationTable table = getTable(getDataSet(context), value);
			if (table == null)
			{
				return value;
			}
			SchemaNode tableNode = table.getTableNode();
			return tableNode.getLabel(locale);
		}
		catch (Exception e)
		{
			return value;
		}
	}

	@Override
	public List<String> getValues(ValueContext context) throws InvalidSchemaException
	{
		Adaptation dataSet = getDataSet(context);
		List<String> tableNames = findAllTables(dataSet);
		return tableNames;
	}

	private static List<String> findAllTables(Adaptation dataSet)
	{
		List<AdaptationTable> tables = AdaptationUtil.getAllTables(dataSet);
		List<String> result = new ArrayList<>();
		for (AdaptationTable table : tables)
		{
			SchemaNode tableNode = table.getTableNode();
			result.add(tableNode.getPathInSchema().format());
		}
		return result;
	}

}
