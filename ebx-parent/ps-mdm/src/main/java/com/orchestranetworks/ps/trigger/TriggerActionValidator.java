/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * This provides a convenient way of throwing an exception from a trigger. When an exception is thrown, the current operation is canceled
 * and the message is shown to the user. This is referred to as a "functional guard" in the EBX documentation.
 * 
 * Note that functional guards should only be used when one of the standard EBX components can't handle this functionality.
 * For data validation, Constraints should be used. To programmatically prevent a create or delete, most often a ServicePermissionRule can
 * be set on the built-in services.
 * 
 * This is mostly useful when you need to prevent an operation based on some session context, and when the built-in service isn't being invoked.
 * For example, when importing records, the Create service isn't invoked, so its ServicePermissionRule won't be executed, and Constraints can't
 * access the session so that might not be an option for your logic.
 * 
 * @see BaseTableTrigger
 */
public interface TriggerActionValidator
{
	enum TriggerAction {
		CREATE("create"), MODIFY("modify"), DELETE("delete");

		private final String label;

		private TriggerAction(String label)
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	/**
	 * Validate the given action by a trigger and throw an exception if it should not be allowed
	 * 
	 * @param session the session
	 * @param valueContext the value context of the record
	 * @param valueChanges the changes occurring, if this is a modify. Otherwise this is <code>null</code>.
	 * @param action the action: either <code>CREATE</code>, <code>MODIFY</code>, or <code>DELETE</code>
	 * @return the error message, or <code>null</code> if there is no error
	 * @throws OperationException
	 */
	UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action) throws OperationException;
}
