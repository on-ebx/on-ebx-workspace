/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 */
public class FirstNonNullValueValueFunction implements ValueFunction
{
	protected static final String SEPARATOR = ";";

	private String fieldList;
	private List<SchemaNode> fieldNodes;

	@Override
	public Object getValue(Adaptation adaptation)
	{
		Iterator<SchemaNode> iter = fieldNodes.iterator();
		Object value = null;
		while (value == null && iter.hasNext())
		{
			value = getFieldValue(adaptation, iter.next());
		}
		return value;
	}

	protected Object getFieldValue(Adaptation adaptation, SchemaNode fieldNode)
	{
		return adaptation.get(fieldNode);
	}

	@Override
	public void setup(ValueFunctionContext context)
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
			String[] pathStrArr = fieldList.split(SEPARATOR);
			for (String pathStr : pathStrArr)
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

	public String getFieldList()
	{
		return this.fieldList;
	}

	public void setFieldList(String fieldList)
	{
		this.fieldList = fieldList;
	}
}
