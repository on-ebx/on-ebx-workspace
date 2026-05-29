package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * This simply wraps multiple {@link TriggerActionValidator} objects so that a trigger
 * can invoke multiple of them. The first message found will be returned,
 * or <code>null</code> will be returning if none returned a message.
 */
public class MultiTriggerActionValidator implements TriggerActionValidator
{
	private List<TriggerActionValidator> validators;

	public MultiTriggerActionValidator()
	{
		this(new ArrayList<>());
	}

	public MultiTriggerActionValidator(List<TriggerActionValidator> validators)
	{
		this.validators = validators;
	}

	@Override
	public UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action)
		throws OperationException
	{
		UserMessage msg = null;
		Iterator<TriggerActionValidator> iter = validators.iterator();
		// Loop over each validator, calling each until one of them returns a message
		while (msg == null && iter.hasNext())
		{
			TriggerActionValidator validator = iter.next();
			msg = validator.validateTriggerAction(session, valueContext, valueChanges, action);
		}
		// Return the message found, or null if none returned a message
		return msg;
	}

	public List<TriggerActionValidator> getValidators()
	{
		return validators;
	}

	public void setValidators(List<TriggerActionValidator> validators)
	{
		this.validators = validators;
	}
}
