/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This constraint enumeration can be used to create a selection field for
 * choosing a table within a data set.  The data space and data set can be
 * specified using sibling fields.  If no data set is specified, the data
 * set of the containing table is used.
 */
public class TableConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a table.";
	private Path homePath;
	private String fixedHomeKey;
	private Path dataSetPath;
	private String fixedInstanceName;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		try
		{
			Path tablePath = Path.parse(aValue);
			AdaptationTable table = getTable(aValidationContext, tablePath);
			if (table == null)
				aValidationContext.addError(aValue + " does not exist");
		}
		catch (Exception e)
		{
			aValidationContext.addError(aValue + " does not exist");
		}
	}

	protected AdaptationHome getHome(ValueContext valueContext)
	{
		AdaptationHome home = valueContext.getHome();
		if (homePath != null || fixedHomeKey != null)
		{
			String homeKey = fixedHomeKey;
			if (homePath != null)
				homeKey = (String) valueContext.getValue(Path.PARENT.add(homePath));
			if (homeKey != null)
				return home.getRepository().lookupHome(HomeKey.parse(homeKey));
		}
		return home;
	}

	protected Adaptation getDataSet(ValueContext valueContext)
	{
		Adaptation dataSet = valueContext.getAdaptationInstance();
		if (dataSetPath != null || fixedInstanceName != null)
		{
			AdaptationHome home = getHome(valueContext);
			if (home == null)
				return null;
			String dsKey = fixedInstanceName;
			if (dataSetPath != null)
				dsKey = (String) valueContext.getValue(Path.PARENT.add(dataSetPath));
			if (dsKey != null)
				return home.findAdaptationOrNull(AdaptationName.forName(dsKey));
		}
		return dataSet;
	}

	private AdaptationTable getTable(ValueContext valueContext, Path tablePath)
	{
		Adaptation dataSet = getDataSet(valueContext);
		if (dataSet == null)
			return null;
		return dataSet.getTable(tablePath);
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
		// optional path to other field from which to declare dataspace
		if (homePath != null)
		{
			PathUtils.setupFieldNode(aContext, homePath, "homePath", false, false);
		}
		if (dataSetPath != null)
		{
			PathUtils.setupFieldNode(aContext, dataSetPath, "dataSetPath", false, false);
		}
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	@Override
	public String displayOccurrence(String aValue, ValueContext aContext, Locale aLocale)
		throws InvalidSchemaException
	{
		Path tablePath = Path.parse(aValue);
		AdaptationTable table = getTable(aContext, tablePath);
		if (table == null)
			return tablePath.format();
		return table.getTableNode().getLabel(aLocale);
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		Adaptation dataSet = getDataSet(aContext);
		if (dataSet == null)
			return Collections.emptyList();
		List<String> tableNames = new ArrayList<>();
		SchemaNode dataSetNode = dataSet.getSchemaNode();
		collectTables(tableNames, dataSetNode.getNodeChildren());
		return tableNames;
	}

	public void collectTables(List<String> tableNames, SchemaNode[] nodeChildren)
	{
		for (SchemaNode schemaNode : nodeChildren)
		{
			if (schemaNode.isTableNode())
				tableNames.add(schemaNode.getPathInSchema().format());
			else
				collectTables(tableNames, schemaNode.getNodeChildren());
		}
	}

	public Path getHomePath()
	{
		return homePath;
	}

	public void setHomePath(Path homePath)
	{
		this.homePath = homePath;
	}

	public Path getDataSetPath()
	{
		return dataSetPath;
	}

	public void setDataSetPath(Path dataSetPath)
	{
		this.dataSetPath = dataSetPath;
	}

	public String getFixedHomeKey()
	{
		return fixedHomeKey;
	}

	public void setFixedHomeKey(String fixedHomeKey)
	{
		this.fixedHomeKey = fixedHomeKey;
	}

	public String getFixedInstanceName()
	{
		return fixedInstanceName;
	}

	public void setFixedInstanceName(String fixedInstanceName)
	{
		this.fixedInstanceName = fixedInstanceName;
	}

}
