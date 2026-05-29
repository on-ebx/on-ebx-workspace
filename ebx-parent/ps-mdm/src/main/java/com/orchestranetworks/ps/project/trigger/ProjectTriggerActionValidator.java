/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * This is a trigger action validator that, in addition to the functionality of <code>ProjectRecordMatchesTriggerActionValidator</code>,
 * also ensures that you can't delete the project itself that is associated with the workflow.
 * It is expected that if this is used, it will be put on the project table itself.
 */
public abstract class ProjectTriggerActionValidator
	extends
	ProjectRecordMatchesTriggerActionValidator
{
	@Override
	public UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action) throws OperationException
	{
		UserMessage msg = super.validateTriggerAction(session, valueContext, valueChanges, action);
		if (msg == null && action == TriggerAction.DELETE)
		{
			SessionInteraction interaction = session.getInteraction(true);
			if (interaction != null)
			{
				Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecordFromSessionInteraction(
					interaction,
					valueContext.getAdaptationInstance(),
					getProjectPathConfig());
				if (projectRecord != null)
				{
					msg = UserMessage.createError("Can't delete the project associated with the workflow.");
				}
			}
		}
		return msg;
	}
}
