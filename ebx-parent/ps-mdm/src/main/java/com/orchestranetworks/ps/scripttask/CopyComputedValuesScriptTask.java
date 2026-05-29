/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class CopyComputedValuesScriptTask extends ScriptTask
{
	protected abstract CopyComputedValuesProcedure createProcedure(String dataSetName);

	protected boolean useChildDataSpace()
	{
		return true;
	}

	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		String dataSetName = context.getVariableString(WorkflowConstants.PARAM_DATA_SET);
		AdaptationHome dataSpace = WorkflowUtilities.getDataSpace(
			context,
			context.getRepository(),
			WorkflowConstants.PARAM_WORKING_DATA_SPACE);
		CopyComputedValuesProcedure proc = createProcedure(dataSetName);

		if (useChildDataSpace())
		{
			ProcedureExecutor.executeProcedureInChild(
				proc,
				context.getSession(),
				dataSpace,
				proc.getChildDataSpaceLabelPrefix(),
				proc.getPermissionsTemplateDataSpaceName());
		}
		else
		{
			ProcedureExecutor.executeProcedure(proc, context.getSession(), dataSpace);
		}
	}
}
