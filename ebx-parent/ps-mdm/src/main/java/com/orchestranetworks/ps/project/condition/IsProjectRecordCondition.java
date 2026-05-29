/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.condition;

import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class IsProjectRecordCondition extends Condition implements ProjectPathCapable
{
	@Override
	public boolean evaluateCondition(ConditionContext context) throws OperationException
	{
		// Check if the data context variable "record" is for a Project
		return (ProjectWorkflowUtilities.getProjectRecord(
			context,
			null,
			context.getRepository(),
			getProjectPathConfig()) != null);
	}

}
