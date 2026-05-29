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
 * Determine which workflow step to resume into and set that in the data context.
 * Also update the project in whatever way is necessary to begin that step.
 * A "resume step" can be anything that you want to use in your workflow to uniquely identify where to resume.
 * A typical use case would be to return the name of a sub-workflow to resume into but that doesn't have to be the case.
 */
public abstract class DetermineProjectResumeStepScriptTask extends ScriptTask
	implements ProjectPathCapable
{
	protected abstract String getResumeStep(Adaptation projectRecord);

	/**
	 * This does nothing by default but can be implemented to update whatever needs to be changed on the project
	 * prior to resuming into the given step.
	 * For example, you could set the status or clear out some data.
	 * The subclass will be responsible for managing the procedure needed to perform the update.
	 * 
	 * @param session the session
	 * @param projectRecord the project record
	 * @param resumeStep the resume step
	 * @throws OperationException if an error occurs while updating the project
	 */
	protected void updateProjectForResumeStep(
		Session session,
		Adaptation projectRecord,
		String resumeStep) throws OperationException
	{
		// do nothing
	}

	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecord(
			context,
			null,
			context.getRepository(),
			getProjectPathConfig());
		if (projectRecord != null)
		{
			String resumeStep = getResumeStep(projectRecord);
			if (resumeStep != null)
			{
				context.setVariableString(ProjectWorkflowConstants.PARAM_RESUME_STEP, resumeStep);
			}

			updateProjectForResumeStep(context.getSession(), projectRecord, resumeStep);
		}
	}
}
