package com.orchestranetworks.ps.scripttask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * Terminates all active workflows of the given publication name whose data context parameter matches the given data space name.
 * If <code>clean</code> is specified as <code>true</code>, will also clean it. Otherwise it will terminate it but it will still
 * be in the completed workflows list.
 * 
 * For example, let's say the workflow you are in has a data space parameter called "workingDataSpace" and there's another
 * workflow publication called "UpdateCustomerRecord" that has a data context parameter called "customerDataSpace". You want to terminate
 * all of those workflows whose customerDataSpace is equal to the value of this workflow's workingDataSpace. You want them terminated
 * but still show up as completed. You'd configure it with:
 * <dl>
 *   <dt>workflowPublication</dt><dd>UpdateCustomerRecord</dd>
 *   <dt>dataspaceParameter</dt><dd>customerDataSpace</dd>
 *   <dt>dataspace</dt><dd>${workingDataSpace}</dd>
 *   <dt>clean</dt><dd>false</dd>
 * </dl>
 * 
 * This is a bean and must be defined in the <code>module.xml</code>.
 */
public class TerminateWorkflowsForDataSpaceScriptTask extends ScriptTaskBean
{
	private String workflowPublication;
	private String dataspaceParameter;
	private String dataspace;
	private boolean clean;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		// Need to do this in a procedure so we can enable all privileges before
		// creating the WorkflowEngine. Otherwise, it won't allow the terminate.
		Procedure proc = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				pContext.setAllPrivileges(true);
				WorkflowEngine wfEngine = WorkflowEngine.getFromProcedureContext(pContext);
				terminateWorkflows(pContext, wfEngine);
			}
		};
		AdaptationHome dataSpaceRef = context.getRepository()
			.lookupHome(HomeKey.forBranchName(dataspace));
		ProcedureExecutor.executeProcedure(proc, context.getSession(), dataSpaceRef);
	}

	private void terminateWorkflows(ProcedureContext pContext, WorkflowEngine workflowEngine)
	{
		// Find all workflows where the data context param matches the data space
		List<ProcessInstance> processInstances = WorkflowUtilities
			.getActiveProcessInstancesForDataContextVariable(
				workflowEngine,
				workflowPublication,
				dataspaceParameter,
				dataspace,
				false);
		for (ProcessInstance processInstance : processInstances)
		{
			terminateWorkflow(workflowEngine, processInstance.getProcessInstanceKey());
		}
	}

	private void terminateWorkflow(WorkflowEngine wfEngine, ProcessInstanceKey processInstanceKey)
	{
		if (clean)
		{
			wfEngine.terminateProcessInstance(processInstanceKey);
		}
		else
		{
			wfEngine.forceTerminationWithoutCleaning(processInstanceKey);
		}
	}

	public String getWorkflowPublication()
	{
		return workflowPublication;
	}

	public void setWorkflowPublication(String workflowPublication)
	{
		this.workflowPublication = workflowPublication;
	}

	public String getDataspaceParameter()
	{
		return dataspaceParameter;
	}

	public void setDataspaceParameter(String dataspaceParameter)
	{
		this.dataspaceParameter = dataspaceParameter;
	}

	public String getDataspace()
	{
		return dataspace;
	}

	public void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	public boolean isClean()
	{
		return clean;
	}

	public void setClean(boolean clean)
	{
		this.clean = clean;
	}
}
