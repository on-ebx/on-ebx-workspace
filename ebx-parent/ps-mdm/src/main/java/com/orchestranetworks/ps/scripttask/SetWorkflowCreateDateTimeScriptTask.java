package com.orchestranetworks.ps.scripttask;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class SetWorkflowCreateDateTimeScriptTask extends ScriptTask
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		WorkflowUtilities.setWorkflowInstanceCreateDateTime(context, context);
	}

}
