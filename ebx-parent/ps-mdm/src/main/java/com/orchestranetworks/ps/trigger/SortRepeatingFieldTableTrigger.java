package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * Field level trigger that sorts the values in a repeating attribute in alphanumeric order (by default).  
 *
 */
public class SortRepeatingFieldTableTrigger extends CleanseFieldTableTrigger
{

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);
		if (context.getSchemaNode().getMaxOccurs() <= 1
			|| !context.getSchemaNode().isTerminalValue())
		{
			context.addError("Node " + getFieldPath().format() + " must be repeating attribute.");

		}
	}

	@Override
	protected void cleanseField(ValueContextForUpdate valueContext)
	{
		@SuppressWarnings("unchecked")
		List<Object> value = (List<Object>) valueContext.getValue(getFieldPath());
		if (value != null && !value.isEmpty())
		{
			value.sort(getComparator(valueContext));
			valueContext.setValue(value, getFieldPath());
		}
	}

	// Override this method if a custom comparator is desired
	protected Comparator<? super Object> getComparator(ValueContextForUpdate valueContext)
	{
		return null;
	}

}
