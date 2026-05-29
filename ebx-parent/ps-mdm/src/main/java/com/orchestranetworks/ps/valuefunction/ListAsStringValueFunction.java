/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * Given a list field and a separator, return the string representation of that list.
 * If the list items are Adaptations, use the label of the adaptation.
 * If you don't need to display the list on the form and only want it in the table, you can use
 * {@link com.orchestranetworks.ps.uibeaneditor.ListAsStringInTableUIBeanEditor} on the list field instead.
 *
 * separator -	 A string that will be used as the separator between the instances of the collection.
 * 				 To use a space as a prefix or suffix in the separator use the HTML escape value for space - &nbsp;
 * prefix -		 The result string will be prefixed with the value of the prefix. Same rule for space applies.
 * formatValue - If <code>false</code> (default) the value will be converted to a string using the function returned by
 *               {@link #getToStringFunction()}. If <code>true</code>, the value will be converted according to the
 *               formatting policy used by the node, in the {@link #convertToString(Object, ValueContext)} method.
 * omitEmptyValues -- defaults to <code>false</code>.  if <code>true</code>, then Nulls and Empty Strings will be omitted             
 */
public class ListAsStringValueFunction implements ValueFunction
{
	public static final String STRING_VALUE_OPTION_TO_STRING = "toString";
	public static final String STRING_VALUE_OPTION_FORMATTED = "formatted";
	public static final String STRING_VALUE_OPTION_LABEL = "label";

	private static final String SPACE_TOKEN = "&nbsp;";
	private static final List<String> NULL_LIST = new ArrayList<String>();
	{
		NULL_LIST.add("");
		NULL_LIST.add(null);
	}

	private String pathsString;
	private Path[] pathOfPaths;
	private Path lastPath;
	private String separator = ", ";
	private String prefix;
	private boolean formatValue;
	private SchemaNode contextNode;
	private boolean omitEmptyValues = false;

	public static final UnaryFunction<Object, String> toString = new UnaryFunction<Object, String>()
	{
		@Override
		public String evaluate(Object object)
		{
			if (object instanceof Adaptation)
			{
				return ((Adaptation) object).getLabel(Locale.getDefault());
			}

			return String.valueOf(object);
		}
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getValue(Adaptation adaptation)
	{
		List list;
		if (pathOfPaths != null)
		{
			list = lastPath != null
				? AdaptationUtil.evaluatePath(adaptation, pathOfPaths, lastPath, true)
				: AdaptationUtil.evaluatePath(adaptation, pathOfPaths);
		}
		else
		{
			list = adaptation.getList(lastPath);
		}
		if (list == null || list.isEmpty())
		{
			return null;
		}
		List<String> stringList;
		if (formatValue || getToStringFunction() == null)
		{
			stringList = new ArrayList<>();
			for (Object obj : list)
			{
				stringList.add(convertToString(obj, adaptation.createValueContext()));
			}
		}
		else
		{
			stringList = Algorithms.apply(list, getToStringFunction());
		}
		if (stringList == null || stringList.isEmpty())
		{
			return null;
		}
		StringBuilder bldr = new StringBuilder();
		if (!StringUtils.isEmpty(prefix))
		{
			bldr.append(prefix.replace(SPACE_TOKEN, " "));
		}
		String sep = separator == null ? null : separator.replace(SPACE_TOKEN, " ");
		if (omitEmptyValues)
		{
			stringList.removeAll(NULL_LIST);
		}
		bldr.append(StringUtils.join(stringList, sep));
		return bldr.toString();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		if (pathsString == null)
		{
			context.addError("pathsString must be specified.");
		}
		else
		{
			List<Path> paths = PathUtils.convertStringToPathList(pathsString, null);
			if (paths.isEmpty())
				context.addError("pathsString must specify at least one path.");
			List<SchemaNode> nodes = PathUtils.validatePath(context.getSchemaNode(), paths);
			boolean hasCollection = false;
			int nodesSize = nodes.size();
			int pathsSize = paths.size();
			if (nodesSize < pathsSize)
			{
				Path unresolvedPath = paths.get(nodesSize);
				context.addError("Path element " + unresolvedPath.format() + " does not exist.");
				return;
			}
			for (int i = 0; i < paths.size(); i++)
			{
				SchemaNode node = nodes.get(i);
				if (node.getMaxOccurs() != 1)
				{
					hasCollection = true;
				}
			}
			if (!hasCollection)
				context.addError("Paths should resolve to a collection.");
			this.contextNode = nodes.get(nodes.size() - 1);
			boolean lastIsRelationship = AdaptationUtil.isRelationshipNode(contextNode);
			if (!lastIsRelationship)
				lastPath = paths.remove(paths.size() - 1);
			if (!paths.isEmpty())
				pathOfPaths = paths.toArray(new Path[0]);
		}
	}

	protected UnaryFunction<Object, String> getToStringFunction()
	{
		return toString;
	}

	// For normal non-formatted use, this behaves the same as the default toString method,
	// but for backwards-compatibility, keeping that function.
	protected String convertToString(Object object, ValueContext valueContext)
	{
		if (object instanceof Adaptation)
		{
			return ((Adaptation) object).getLabel(Locale.getDefault());
		}
		if (formatValue)
		{
			return contextNode.displayOccurrence(object, false, valueContext, Locale.getDefault());
		}
		return object.toString();
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public String getPathsString()
	{
		return pathsString;
	}

	public String getPrefix()
	{
		return this.prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public void setPathsString(String pathsString)
	{
		this.pathsString = pathsString;
	}

	public boolean isFormatValue()
	{
		return this.formatValue;
	}

	public void setFormatValue(boolean formatValue)
	{
		this.formatValue = formatValue;
	}

	public boolean isOmitEmptyValues()
	{
		return omitEmptyValues;
	}

	public void setOmitEmptyValues(boolean omitEmptyValues)
	{
		this.omitEmptyValues = omitEmptyValues;
	}
}
