package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;

/**
 * Field lvl trigger that initializes the Created in UI indicator to True when record is created in UI (as opposed to Import)
 * -- This is because the HandleNewContext method is not executed during Imports
 * -- by exposing this in a Data Service, the data service can decide if it wants to behave like an Import or like a UI Created record
 * 
 *
 */
public class CreatedInUiTableTrigger extends TableTrigger
{

	private Path fieldPath;

	@Override
	public void setup(TriggerSetupContext context)
	{
		fieldPath = context.getSchemaNode().getPathInAdaptation();
	}

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		context.getValueContext().setValue(Boolean.TRUE, fieldPath);
		super.handleNewContext(context);
	}

}
