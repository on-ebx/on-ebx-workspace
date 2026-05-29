package com.orchestranetworks.ps.constraint;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.adaptation.RequestResult;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.instance.ValueContextForValidationOnTable;
import com.orchestranetworks.ps.util.AdaptationUtil;
import com.orchestranetworks.ps.util.PathUtils;
import com.orchestranetworks.schema.ConstraintContextOnTable;
import com.orchestranetworks.schema.InvalidSchemaException;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;

/**
 * Often, there will be a boolean field in an association table to indicate the default or primary record in the group.
 * More generally, one may want to constrain the number of times a given value for a field, within a group of records,
 * may occur.
 */
public class LimitFieldValueInRecordGroup extends BaseConstraintOnTableWithRecordLevelCheck
{
	private Path field;
	private SchemaNode fieldNode;
	private int minOccurs = 0;
	private Integer maxOccurs = Integer.valueOf(1);
	private Object value = Boolean.TRUE;
	private String valueString = "true";
	private String commonValuePathsString;
	private List<Path> commonValuePaths;
	private static String NOTNULL = "<not-null>";
	private static String NULL = "<null>";
	private Map<String, List<Adaptation>> buckets = new HashMap<>();

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		SchemaNode parentNode = context.getSchemaNode();
		fieldNode = PathUtils.setupFieldNode(context, field, "field", true, true);
		if (fieldNode.getMaxOccurs() != 1 || !fieldNode.getXsTypeName().isXSDBuiltInDatatype())
			context.addError("Field should be a single valued simple type.");
		if (valueString != null)
		{
			if (NULL.equals(valueString))
				value = null;
			else if (!NOTNULL.equals(valueString))
				value = fieldNode.parseXsString(valueString);
		}
		if (commonValuePathsString != null)
		{
			commonValuePaths = PathUtils.convertStringToPathList(commonValuePathsString, null);
			for (Path path : commonValuePaths)
			{
				PathUtils.setupFieldNode(context, parentNode, path, "commonValue", true, false);
			}
		}
		super.setup(context);
	}

	@Override
	public String toUserDocumentation(Locale arg0, ValueContext arg1) throws InvalidSchemaException
	{
		boolean required = minOccurs > 0;
		boolean bounded = maxOccurs != null;
		StringBuilder result = new StringBuilder("The field {0} should have value of {1} ");
		if (required)
		{
			result.append("at least {2} times");
		}
		if (bounded)
		{
			if (required)
				result.append("and ");
			result.append("at most {3} times");
		}
		if (commonValuePaths.isEmpty())
			result.append(".");
		else
		{
			result.append(" within the context of each group.");
		}
		return MessageFormat.format(
			result.toString(),
			fieldNode.getLabel(arg0),
			valueString,
			Integer.valueOf(minOccurs),
			maxOccurs);
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		List<Adaptation> groupMembers = buckets.get(getKey(recordContext, null));
		if (groupMembers == null)
		{
			try
			{
				groupMembers = AdaptationUtil
					.fetchRecordsMatching(recordContext, commonValuePaths, null);
				if (groupMembers == null)
					return null;
			}
			catch (NullPointerException e)
			{
				// TODO: During shutdown, Module.getLocaleDefault is raising NPE so this is a workaround
				//       but ideally should fix the NPE in the first place since any number of things
				//       could throw an NPE.
				return null;
			}
		}
		int count = 0;
		Object thisValue = recordContext.getValue(field);
		if (matches(thisValue))
			count++;
		Adaptation thisRecord = AdaptationUtil.getRecordForValueContext(recordContext);
		for (Adaptation adaptation : groupMembers)
		{
			if (thisRecord != null && adaptation.equalsToAdaptation(thisRecord))
				continue;
			if (matches(adaptation.get(field)))
				count++;
		}
		if (minOccurs > 0 && count == 0)
			return fieldNode.getLabel(Locale.getDefault()) + " never has the value of "
				+ valueString + " in the group";
		if (maxOccurs != null && count > maxOccurs.intValue())
			return fieldNode.getLabel(Locale.getDefault()) + " has the value of " + valueString
				+ " " + count + " times when it should appear at most " + maxOccurs + " times.";
		return null;
	}

	private boolean matches(Object thisValue)
	{
		if (NOTNULL.equals(valueString) && thisValue != null)
			return true;
		if (value == null && thisValue == null)
			return true;
		return value != null && value.equals(thisValue);
	}

	@Override
	public void checkTable(ValueContextForValidationOnTable context)
	{
		// to optimize validating the table, first sort the records into
		// 'common' buckets
		synchronized (buckets)
		{
			buckets.clear();
			try
			{
				sortBuckets(context.getTable());
				super.checkTable(context);
			}
			finally
			{
				buckets.clear();
			}
		}
	}

	private void sortBuckets(AdaptationTable table)
	{
		RequestResult rr = table.createRequest().execute();
		try
		{
			Adaptation next;
			while ((next = rr.nextAdaptation()) != null)
			{
				String key = getKey(null, next);
				List<Adaptation> list = buckets.get(key);
				if (list == null)
				{
					list = new ArrayList<>();
					buckets.put(key, list);
				}
				list.add(next);
			}
		}
		finally
		{
			rr.close();
		}
	}

	private String getKey(ValueContext recordContext, Adaptation record)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Path path : commonValuePaths)
		{
			if (!first)
				sb.append("|");
			first = false;
			Object val = recordContext != null ? recordContext.getValue(path) : record.get(path);
			sb.append(val);
		}
		return sb.toString();
	}

	public Path getField()
	{
		return field;
	}

	public void setField(Path field)
	{
		this.field = field;
	}

	public int getMinOccurs()
	{
		return minOccurs;
	}

	public void setMinOccurs(int minOccurs)
	{
		this.minOccurs = minOccurs;
	}

	public Integer getMaxOccurs()
	{
		return maxOccurs;
	}

	public void setMaxOccurs(Integer maxOccurs)
	{
		this.maxOccurs = maxOccurs;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getValueString()
	{
		return valueString;
	}

	public void setValueString(String valueString)
	{
		this.valueString = valueString;
	}

	public String getCommonValuePathsString()
	{
		return commonValuePathsString;
	}

	public void setCommonValuePathsString(String commonValuePathsString)
	{
		this.commonValuePathsString = commonValuePathsString;
	}

}
