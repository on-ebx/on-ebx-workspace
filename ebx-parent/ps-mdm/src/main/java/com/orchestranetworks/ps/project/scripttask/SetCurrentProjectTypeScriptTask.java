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
public abstract class SetCurrentProjectTypeScriptTask extends ScriptTask
	implements ProjectPathCapable
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		// Note: The record in the context should be a project record
		ProjectWorkflowUtilities.setCurrentProjectType(
			context,
			context.getSession(),
			context.getRepository(),
			getProjectPathConfig());
	}
}
