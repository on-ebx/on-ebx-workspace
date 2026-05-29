/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A constraint that doesn't allow specified fields to have values simultaneously.
 * It can be used as-is by specifying 2 fields, but if more fields are desired, it can be extended.
 */
public class FieldCombinationNotAllowedConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private Path field1Path;
	private Path field2Path;

	private String message;

	/**
	 * Get the paths that can't be specified at same time. By default, these are <code>field1Path</code> and <code>field2Path</code>
	 * but this can be extended if more than 2 fields are desired.
	 * 
	 * @return the paths
	 */
	protected Path[] getFieldPaths()
	{
		return new Path[] { field1Path, field2Path };
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		Path[] fieldPaths = getFieldPaths();
		if (fieldPaths == null || fieldPaths.length < 2)
		{
			context.addError("At least 2 field paths must be specified.");
		}
		else
		{
			SchemaNode[] fieldNodes = new SchemaNode[fieldPaths.length];
			for (int i = 0; i < fieldPaths.length; i++)
			{
				Path fieldPath = fieldPaths[i];
				if (fieldPath == null)
				{
					context.addError("null field path specified.");
				}
				else
				{
					SchemaNode fieldNode = context.getSchemaNode().getNode(fieldPath);
					if (fieldNode == null)
					{
						context.addError("Node " + fieldPath.format() + " not found.");
					}
					else
					{
						fieldNodes[i] = fieldNode;
					}
				}
			}
			message = createMessage(fieldNodes, Locale.getDefault());
		}
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext context)
		throws InvalidSchemaException
	{
		return message;
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		Path[] fieldPaths = getFieldPaths();
		Object firstValueFound = null;
		for (Path fieldPath : fieldPaths)
		{
			Object value = recordContext.getValue(fieldPath);
			// TODO: Could enhance by allowing customization of what "value found" means (i.e. is > 0 "found"?)
			if (value != null)
			{
				if (firstValueFound == null)
				{
					firstValueFound = value;
				}
				else
				{
					return message;
				}
			}
		}
		return null;
	}

	private static String createMessage(SchemaNode[] fieldNodes, Locale locale)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("Only one of the fields ");
		for (int i = 0; i < fieldNodes.length; i++)
		{
			bldr.append(fieldNodes[i].getLabel(locale));
			if (i < fieldNodes.length - 1)
			{
				bldr.append(" and ");
			}
		}
		bldr.append(" can be specified.");
		return bldr.toString();
	}

	public Path getField1Path()
	{
		return this.field1Path;
	}

	public void setField1Path(Path field1Path)
	{
		this.field1Path = field1Path;
	}

	public Path getField2Path()
	{
		return this.field2Path;
	}

	public void setField2Path(Path field2Path)
	{
		this.field2Path = field2Path;
	}
}
