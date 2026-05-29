/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.workflow;

import java.util.*;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class ProjectSubWorkflowsInvocationBean extends BasicSubWorkflowsInvocationBean
{
	@Override
	protected Set<String> getInputParametersToMap(DataContextReadOnly dataContext)
	{
		Set<String> params = super.getInputParametersToMap(dataContext);

		params.add(ProjectWorkflowConstants.PARAM_PROJECT_TYPE);
		params.add(ProjectWorkflowConstants.PARAM_PROJECT_TEAM_MEMBERS);

		return params;
	}

	@Override
	protected Set<String> getOutputParametersToMap(DataContextReadOnly dataContext)
	{
		Set<String> params = super.getOutputParametersToMap(dataContext);

		params.add(ProjectWorkflowConstants.PARAM_IS_CANCELLED);
		params.add(ProjectWorkflowConstants.PARAM_PROJECT_TEAM_MEMBERS);

		return params;
	}
}
