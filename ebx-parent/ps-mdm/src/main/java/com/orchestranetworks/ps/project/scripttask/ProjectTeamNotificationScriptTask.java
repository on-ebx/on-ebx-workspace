/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class ProjectTeamNotificationScriptTask extends ScriptTask
	implements ProjectPathCapable
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		updateAdditionalNotificationInfo(context);

		ProjectWorkflowUtilities.notifyProjectTeam(
			context,
			context,
			getNotificationTemplate(context),
			getProjectPathConfig());
	}

	private void updateAdditionalNotificationInfo(ScriptTaskContext context)
		throws OperationException
	{
		if (context.isVariableDefined(WorkflowConstants.PARAM_ADDITIONAL_NOTIFICATION_INFO))
		{
			String additionalNotificationInfo = createAdditionalNotificationInfo(
				context,
				context.getRepository());
			if (additionalNotificationInfo != null)
			{
				context.setVariableString(
					WorkflowConstants.PARAM_ADDITIONAL_NOTIFICATION_INFO,
					additionalNotificationInfo);
			}
		}
	}

	protected String createAdditionalNotificationInfo(
		DataContextReadOnly dataContext,
		Repository repo) throws OperationException
	{
		// Default implementation doesn't use additional notification info
		return null;
	}

	protected abstract int getNotificationTemplate(DataContextReadOnly dataContext);
}
