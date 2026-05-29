package com.orchestranetworks.ps.scripttask;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class SetWorkflowCurrentUserToCreatorScriptTask extends ScriptTask
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		WorkflowUtilities.setWorkflowCurrentUser(context, context);
	}

}
