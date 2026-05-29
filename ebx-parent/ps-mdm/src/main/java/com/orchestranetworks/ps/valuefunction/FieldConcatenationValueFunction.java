package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * Concatenates field values with separators between them. If a field has no value, won't show the separator.
 * If no separators are specified, concats without a separator.
 * If one separator is specified, uses it between each field value.
 * If more than one separator is specified, you must specify the same number of separators as fields, minus 1.
 * Use tokens {@link #SEPARATOR_TOKEN_SPACE} for a space, {@link #SEPARATOR_TOKEN_TAB} for a tab,
 * {@link #SEPARATOR_TOKEN_NEWLINE} for a newline, or {@link #SEPARATOR_TOKEN_LIST_SEPARATOR_EQUIVALENT} if the
 * separator needs to contain the same string being used to separate values by {@link #LIST_SEPARATOR}.
 * 
 * For example, if you have four fields with values A, B, C, and D, and separator is specified as ",<space>;--;<space>"
 * then the result would be "A, B--C D". If there was no value for the 3rd field, then it would be "A, B D".
 * (The separator prior to the existing field is used rather than the separator prior to the missing field.)
 * 
 * In the same example of four fields, if the separator is specified as simply "<space>", then it will be used throughout,
 * resulting in "A B C D".
 */
public class FieldConcatenationValueFunction implements ValueFunction
{
	protected static final String LIST_SEPARATOR = ";";

	protected static final String SEPARATOR_TOKEN_SPACE = "<space>";
	protected static final String SEPARATOR_TOKEN_TAB = "<tab>";
	protected static final String SEPARATOR_TOKEN_NEWLINE = "<newline>";
	protected static final String SEPARATOR_TOKEN_LIST_SEPARATOR_EQUIVALENT = "<semicolon>";

	private String fieldList;
	private String separatorList;

	private List<SchemaNode> fieldNodes;
	private List<String> separators;

	@Override
	public Object getValue(Adaptation adaptation)
	{
		StringBuilder bldr = new StringBuilder();
		// Used to indicate that the builder has had a value written to it.
		// More efficient than checking length of string each time through and comparing to 0.
		boolean bldrInitialized = false;

		int numOfSeps = separators.size();
		// This will contain a value only if one separator is specified
		String sep = (numOfSeps == 1) ? separators.get(0) : null;
		for (int i = 0; i < fieldNodes.size(); i++)
		{
			SchemaNode fieldNode = fieldNodes.get(i);
			String fieldValue = getFieldValue(adaptation, fieldNode);
			if (fieldValue != null && !fieldValue.isEmpty())
			{
				// If the builder already has a value written to it, then we want to append the separator first
				if (bldrInitialized && numOfSeps != 0)
				{
					// If there's not one separator for the whole list, then append the separator for this field
					if (sep == null)
					{
						bldr.append(separators.get(i - 1));
					}
					// Otherwise append the one separator we're using
					else
					{
						bldr.append(sep);
					}
				}
				bldr.append(fieldValue);
				bldrInitialized = true;
			}
		}

		if (!bldrInitialized)
		{
			return null;
		}
		else
		{
			return bldr.toString();
		}
	}

	protected String getFieldValue(Adaptation adaptation, SchemaNode fieldNode)
	{
		// We could have more complicated logic in here for determining how to format different
		// kinds of nodes, whether to apply label or not, etc - but most often we just want to
		// concatenate, and we want to be as efficient as we can in a value function.
		// This can be subclassed to handle specific fields differently.
		if (fieldNode == null)
		{
			return null;
		}
		Object value = adaptation.get(fieldNode);
		return value == null ? null : value.toString();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		setupFieldList(context);
		setupSeparatorList(context);
	}

	private void setupFieldList(ValueFunctionContext context)
	{
		if (fieldList == null)
		{
			context.addError("fieldList must be specified.");
		}
		else
		{
			fieldNodes = new ArrayList<>();
			SchemaNode tableOccNode = context.getSchemaNode()
				.getTableNode()
				.getTableOccurrenceRootNode();
			String[] pathStrArr = fieldList.split(LIST_SEPARATOR);
			for (String pathStr : pathStrArr)
			{
				if (pathStr.length() == 0)
				{
					fieldNodes.add(null);
				}
				else
				{
					Path path = Path.parse(pathStr);
					SchemaNode node = tableOccNode.getNode(path);
					if (node == null)
					{
						context.addError("fieldList path " + path.format() + " does not exist.");
					}
					else
					{
						fieldNodes.add(node);
					}
				}
			}
		}
	}

	private void setupSeparatorList(ValueFunctionContext context)
	{
		separators = new ArrayList<>();
		if (separatorList != null)
		{
			String[] separatorArr = separatorList.split(LIST_SEPARATOR);
			for (String separator : separatorArr)
			{
				if (separator.length() == 0)
				{
					separators.add(null);
				}
				else
				{
					separators.add(replaceTokens(separator));
				}
			}
			int numOfSeparators = separators.size();
			if (numOfSeparators != 1 && numOfSeparators != fieldNodes.size() - 1)
			{
				context.addError(
					"There must be one separator, or a number of separators equal to one less than the number of fields.");
			}
		}
	}

	private static String replaceTokens(String str)
	{
		// This might not be the most efficient, but it happens only in setup, not each time the value function is called
		return str.replace(SEPARATOR_TOKEN_SPACE, " ")
			.replace(SEPARATOR_TOKEN_TAB, "\t")
			.replace(SEPARATOR_TOKEN_NEWLINE, System.lineSeparator())
			.replace(SEPARATOR_TOKEN_LIST_SEPARATOR_EQUIVALENT, LIST_SEPARATOR);
	}

	public String getFieldList()
	{
		return this.fieldList;
	}

	public void setFieldList(String fieldList)
	{
		this.fieldList = fieldList;
	}

	public String getSeparatorList()
	{
		return separatorList;
	}

	public void setSeparatorList(String separatorList)
	{
		this.separatorList = separatorList;
	}
}
