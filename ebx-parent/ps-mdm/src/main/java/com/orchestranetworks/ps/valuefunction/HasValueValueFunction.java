/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.Collection;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 */
public class HasValueValueFunction implements ValueFunction
{
	private Path fieldPath;

	@Override
	public Object getValue(Adaptation adaptation)
	{
		Object fieldValue = adaptation.get(fieldPath);
		boolean isNullOrEmpty = fieldValue == null
				|| (fieldValue instanceof String && fieldValue.toString().trim().length() == 0)
				|| (fieldValue instanceof Collection && ((Collection<?>) fieldValue).isEmpty());
		return Boolean.valueOf(!isNullOrEmpty);
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		if (fieldPath == null)
		{
			context.addError("fieldPath must be specified.");
		}
		else
		{
			SchemaNode fieldNode = context.getSchemaNode()
				.getTableNode()
				.getTableOccurrenceRootNode()
				.getNode(fieldPath);
			if (fieldNode == null)
			{
				context.addError("fieldPath " + fieldPath.format() + " does not exist.");
			}
		}
	}

	public String getFieldPath()
	{
		return this.fieldPath.format();
	}

	public void setFieldPath(String fieldPath)
	{
		this.fieldPath = Path.parse(fieldPath);
	}
}
