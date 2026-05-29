/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.workflow;

import com.orchestranetworks.ps.workflow.*;

/**
 */
public interface ProjectWorkflowConstants extends WorkflowConstants
{
	public static final String PARAM_PROJECT_TYPE = "projectType";
	public static final String PARAM_PROJECT_TEAM_MEMBERS = "projectTeamMembers";
	public static final String PARAM_CANCEL_REASON = "cancelReason";
	public static final String PARAM_IS_CANCELLED = "isCancelled";
	public static final String PARAM_RESUME_STEP = "resumeStep";
	public static final String PARAM_RESUMING = "resuming";

	public static final char PROJECT_TEAM_MEMBERS_PARAM_SEPARATOR = ';';

	public static final String SESSION_PARAM_PROJECT_NAME = "projectName";
	public static final String SESSION_PARAM_PROJECT_TYPE = PARAM_PROJECT_TYPE;

	public static final String PERMISSIONS_USER_PROJECT_TYPE_PREFIX = "ProjectType_";
}
