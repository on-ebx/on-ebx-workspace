package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Given a string field and a separator, return the list representation of the delimited string.
 *
 * stringPath -  Relative path of the field that contains the delimited string
 *
 * separator -	 A string that will be used as the separator between the instances of the collection.
 * 				 To use a space as a prefix or suffix in the separator use the HTML escape value for space - &nbsp;
*/
public class StringAsListValueFunction implements ValueFunction
{

	private static final String SPACE_TOKEN = "&nbsp;";

	private Path stringPath;
	private String separator = ", ";

	@Override
	public Object getValue(Adaptation adaptation)
	{
		String stringValue = adaptation.getString(stringPath);
		if (stringValue == null)
		{
			return new ArrayList<String>();
		}
		return CollectionUtils.splitString(stringValue, separator.replace(SPACE_TOKEN, " "));
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		if (stringPath == null)
		{
			context.addError("stringPath must be specified.");
		}
		else
		{
			List<Path> pathList = new ArrayList<Path>();
			pathList.add(stringPath);
			List<SchemaNode> nodes = PathUtils.validatePath(context.getSchemaNode(), pathList);
			if (nodes == null || nodes.isEmpty())
			{
				context.addError("stringPath is not a valid path.");
			}
		}
	}

	public String getSeparator()
	{
		return separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public String getStringPath()
	{
		return this.stringPath.format();
	}

	public void setStringPath(String stringPath)
	{
		this.stringPath = Path.parse(stringPath);
	}

}
