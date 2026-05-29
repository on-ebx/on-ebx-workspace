/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * A trigger that will convert a value to uppercase. It should be set on a field, not at the table level.
 * It expects that the field will be a String field.
 */
public class ConvertToUppercaseTableTrigger extends TableTrigger
{
	private Path fieldPath;

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);
		convertToUppercase(context.getOccurrenceContextForUpdate(), context.getSession());
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		super.handleBeforeModify(context);
		convertToUppercase(context.getOccurrenceContextForUpdate(), context.getSession());
	}

	/**
	 * Whether to convert to uppercase. By default, returns <code>true</code> but can be overridden for different behavior.
	 *
	 * @param valueContext the value context of the record
	 * @return whether to convert to uppercase
	 */
	protected boolean shouldConvertToUppercase(ValueContext valueContext)
	{
		return true;
	}

	private void convertToUppercase(ValueContextForUpdate valueContext, Session session)
	{
		String value = (String) valueContext.getValue(fieldPath);
		if (value != null && shouldConvertToUppercase(valueContext))
		{
			valueContext.setValue(value.toUpperCase(session.getLocale()), fieldPath);
		}
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		fieldPath = context.getSchemaNode().getPathInAdaptation();
	}
}
