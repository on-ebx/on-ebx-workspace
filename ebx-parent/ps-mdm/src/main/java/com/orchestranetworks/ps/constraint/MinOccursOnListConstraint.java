/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * This is a constraint that enforces that a list field (minOccurs &gt; 0) must have that number of minimum values in it.
 * This is the same thing that the built-in minOccurs check does, except the built-in check doesn't prevent the save from happening.
 * (The editor for each value won't pre-validate because there's no editor when there's nothing in the list.
 * Also, check for null won't work because a list field is never null, it always returns at least an empty list.)
 * 
 * Also now have the option of specifying a different minOccurs to be checked in this constraint from what is defined on the model
 * -- if omitted, it will use the minOccurs from the model
 * -- this is useful if you want this to be a Warning since the minOccurs on the model is always an Error
 */
public class MinOccursOnListConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private Path fieldPath;
	private SchemaNode fieldNode;
	private Integer minOccursForConstraint;

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		if (fieldPath == null)
		{
			context.addError("fieldPath must be specified.");
		}
		else
		{
			fieldNode = context.getSchemaNode().getNode(fieldPath);
			if (fieldNode == null)
			{
				context.addError("Node " + fieldPath.format() + " not found.");
			}
			else
			{
				if (minOccursForConstraint == null && fieldNode.getMinOccurs() == 0)
				{
					context.addError(
						"Node " + fieldPath.format() + " must have minOccurs greater than 0.");
				}
			}
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return createMessage(locale);
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return fieldNode;
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		List<?> valueList = (List<?>) recordContext.getValue(fieldPath);
		if (valueList.size() < getMinOccursValue())
		{
			return createMessage(Locale.getDefault());
		}
		return null;
	}

	private String createMessage(Locale locale)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append(fieldNode.getLabel(locale));
		bldr.append(" must have at least ");
		int minOccurs = getMinOccursValue();
		bldr.append(minOccurs);
		bldr.append("  value");
		if (minOccurs > 1)
		{
			bldr.append("s");
		}
		bldr.append(".");
		return bldr.toString();
	}

	private int getMinOccursValue()
	{
		if (minOccursForConstraint != null)
		{
			return minOccursForConstraint.intValue();
		}
		else
		{
			return fieldNode.getMinOccurs();
		}
	}

	public Path getFieldPath()
	{
		return fieldPath;
	}

	public void setFieldPath(Path fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	public Integer getMinOccursForConstraint()
	{
		return minOccursForConstraint;
	}

	public void setMinOccursForConstraint(Integer minOccursForConstraint)
	{
		this.minOccursForConstraint = minOccursForConstraint;
	}
}
