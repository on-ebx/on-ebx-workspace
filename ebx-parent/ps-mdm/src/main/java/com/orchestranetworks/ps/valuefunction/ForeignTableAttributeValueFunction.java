/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A value function that looks up a field's value via a foreign key.
 * This is often done via an inherited field, but this can be used when you want a value function.
 * 
 * Now also supports repeating Foreign keys, in which case, it will return a collection of values
 * 
 */
public class ForeignTableAttributeValueFunction extends AbstractForeignKeyValueFunction
{
	private boolean isRepeatingForeignKey = false;

	@Override
	public Object getActualValue(Adaptation adaptation)
	{
		if (isRepeatingForeignKey)
		{
			return AdaptationUtil.followFKList(adaptation, foreignKeyPath, attributePath, false);
		}
		else
		{
			return AdaptationUtil.followFK(adaptation, foreignKeyPath, attributePath);
		}
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		if (attributePath == null)
		{
			context.addError("attributePath must be specified.");
		}
		super.setup(context);
	}

	@Override
	public boolean validateSetupForeignKeyPath(
		ValueFunctionContext context,
		SchemaNode foreignKeyNode)
	{
		isRepeatingForeignKey = (foreignKeyNode.getMaxOccurs() > 1);
		return foreignKeyNode.getFacetOnTableReference() != null;
	}
}
