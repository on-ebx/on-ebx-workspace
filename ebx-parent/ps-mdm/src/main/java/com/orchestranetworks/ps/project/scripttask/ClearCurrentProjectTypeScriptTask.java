/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class ClearCurrentProjectTypeScriptTask extends ScriptTask
	implements ProjectPathCapable
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		String projectType = context.getVariableString(ProjectWorkflowConstants.PARAM_PROJECT_TYPE);
		ProjectWorkflowUtilities.clearCurrentProjectType(
			context,
			context.getSession(),
			context.getRepository(),
			projectPathConfig,
			projectPathConfig.getSubjectPathConfig(projectType));
	}
}
