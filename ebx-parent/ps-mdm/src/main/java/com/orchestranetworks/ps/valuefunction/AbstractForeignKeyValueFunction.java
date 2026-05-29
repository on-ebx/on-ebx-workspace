/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * A value function that looks up a foreign key value via the subclass's function <b>getActualValue(Adaptation)</b> 
 *  will return an attribute from the foreign key value if the attribute path is specified
 *  otherwise returns the Foreign Record's Primary Key.
 *  -- If attribute path is a boolean attribute, then by default, a null record or a null boolean value will return false
 *  --   unless overridden using the convertNullForeignKeyToFalse parameter
 */
public abstract class AbstractForeignKeyValueFunction implements ValueFunction
{
	protected Path foreignKeyPath;
	protected Path attributePath;
	protected boolean convertNullForeignKeyToFalse = true;

	private SchemaTypeName nodeType;

	public abstract Object getActualValue(Adaptation adaptation);

	public abstract boolean validateSetupForeignKeyPath(
		ValueFunctionContext context,
		SchemaNode foreignKeyNode);

	@Override
	public Object getValue(Adaptation adaptation)
	{
		Object value = getActualValue(adaptation);
		return (value == null && convertNullForeignKeyToFalse
			&& SchemaTypeName.XS_BOOLEAN.equals(nodeType)) ? Boolean.FALSE
				: value;
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		nodeType = null;

		SchemaNode foreignKeyNode = PathUtils.setupFieldNode(
			context,
			foreignKeyPath,
			"foreignKeyPath",
			false);
		if (foreignKeyNode != null)
		{
			foreignKeyPath = Path.SELF.add(foreignKeyNode.getPathInAdaptation());
			if (!validateSetupForeignKeyPath(context, foreignKeyNode))
			{
				context.addError(
					"foreignKeyPath " + foreignKeyPath.format()
						+ " is not a valid node.");
			}
			else if (attributePath != null)
			{
				SchemaFacetTableRef schemaFacetTableRef = foreignKeyNode
					.getFacetOnTableReference();
				// TODO: Add logic to check the attributePath value for Associations and Selection Nodes where schemaFacetTableRef is null
				if (schemaFacetTableRef != null)
				{
					SchemaNode foreignTableNode = schemaFacetTableRef.getTableNode();
					if (foreignTableNode != null)
					{
						SchemaNode attributeNode = foreignTableNode.getTableOccurrenceRootNode()
							.getNode(attributePath);
						if (attributeNode == null)
						{
							context.addError(
								"attributePath " + attributePath.format()
									+ " does not exist.");
						}
						else
						{
							nodeType = attributeNode.getXsTypeName();
							SchemaNode thisNode = context.getSchemaNode();
							SchemaTypeName thisNodeType = thisNode.getXsTypeName();
							if (!Objects.equals(nodeType, thisNodeType))
								context.addError(
									"Foreign attribute ("
										+ attributeNode.getLabel(Locale.getDefault())
										+ ") data type does not match the data type of "
										+ thisNode.getLabel(Locale.getDefault()));
						}
					}
				}
			}
		}
	}

	public String getForeignKeyPath()
	{
		return this.foreignKeyPath.format();
	}

	public void setForeignKeyPath(String foreignKeyPath)
	{
		this.foreignKeyPath = Path.parse(foreignKeyPath);
	}

	public String getAttributePath()
	{
		return this.attributePath.format();
	}

	public void setAttributePath(String attributePath)
	{
		this.attributePath = Path.parse(attributePath);
	}

	public boolean isConvertNullForeignKeyToFalse()
	{
		return this.convertNullForeignKeyToFalse;
	}

	public void setConvertNullForeignKeyToFalse(boolean convertNullForeignKeyToFalse)
	{
		this.convertNullForeignKeyToFalse = convertNullForeignKeyToFalse;
	}
}
