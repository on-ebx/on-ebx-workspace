/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

/**
 */
public abstract class CompleteProjectScriptTask extends FinishProjectScriptTask
{
	@Override
	protected String getFinishedProjectStatus(String projectType)
	{
		return getProjectPathConfig().getCompletedProjectStatus(projectType);
	}
}
