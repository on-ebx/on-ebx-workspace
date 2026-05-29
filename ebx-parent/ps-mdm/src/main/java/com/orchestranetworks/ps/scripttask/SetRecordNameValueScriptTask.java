package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * A script task for a single-record workflow, that takes the label from the workflow's record
 * in the data context and sets it into the data context variable representing the record name
 * (@see WorkflowConstants#PARAM_RECORD_NAME_VALUE).
 * 
 * This is often done via the workflow launcher form, but sometimes you don't know the label until
 * after the first step of the workflow, so you need to do it via a script task.
 */
public class SetRecordNameValueScriptTask extends ScriptTask
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		// Get the record (via standard data context variable names)
		Adaptation record = WorkflowUtilities.getRecord(context, null, context.getRepository());
		// Set the record's label to the context variable
		context.setVariableString(
			WorkflowConstants.PARAM_RECORD_NAME_VALUE,
			record.getLabel(context.getSession().getLocale()));
	}
}
