/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * Sets the status of a project to Cancelled and puts the CancelReason and sets isCancelled in the data context
 */
public abstract class CancelProjectScriptTask extends FinishProjectScriptTask
{
	@Override
	protected String getFinishedProjectStatus(String projectType)
	{
		return getProjectPathConfig().getCancelledProjectStatus(projectType);
	}

	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		context.setVariableString(ProjectWorkflowConstants.PARAM_IS_CANCELLED, "true");
		Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecord(
			context,
			null,
			context.getRepository(),
			pathConfig);
		// Check if it was cancelled without ever submitting
		if (projectRecord == null)
		{
			return;
		}
		context.setVariableString(
			ProjectWorkflowConstants.PARAM_CANCEL_REASON,
			projectRecord.getString(pathConfig.getProjectCancelReasonFieldPath()));

		super.executeScript(context);
	}
}
