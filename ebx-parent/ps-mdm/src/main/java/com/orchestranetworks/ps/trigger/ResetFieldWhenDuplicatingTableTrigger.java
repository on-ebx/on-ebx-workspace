package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

// This Table Trigger can be attached to any field in a table that should be initialized when using the Duplicate action on a record
public class ResetFieldWhenDuplicatingTableTrigger extends TableTrigger
{
	private Path resetFieldPath;
	private Object defaultValue;

	@Override
	public void setup(TriggerSetupContext context)
	{
		this.resetFieldPath = context.getSchemaNode().getPathInAdaptation();
		this.defaultValue = context.getSchemaNode().getDefaultValue();
	}

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		super.handleNewContext(context);
		ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
		if (context.isDuplication())
		{
			valueContext.setValueEnablingPrivilegeForNode(defaultValue, resetFieldPath);
		}
	}

}
