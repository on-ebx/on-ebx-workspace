package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;
import com.orchestranetworks.workflow.*;

/**
 * Update Record Workflow Launcher Service Permission Rule -- Will disable the
 * service under the following conditions: -- Disable it when already inside a
 * workflow -- Disable it if checkIfRecordHasActiveWorkflow is declared for the
 * service and the Record already has an Active instance of this Workflow
 * 
 */
public class UpdateRecordWorkflowLauncherServicePermissionRule
	extends
	MasterOrChildDataSpaceOnlyServicePermissionRule<RecordEntitySelection>
{
	private UpdateRecordWorkflowLauncherServiceDeclaration serviceDeclaration;

	public UpdateRecordWorkflowLauncherServicePermissionRule(
		UpdateRecordWorkflowLauncherServiceDeclaration serviceDeclaration)
	{
		super();
		this.serviceDeclaration = serviceDeclaration;
	}

	@Override
	public UserServicePermission getPermission(
		ServicePermissionRuleContext<RecordEntitySelection> context)
	{
		// Disable it when already inside a workflow
		if (context.getSession().isInWorkflowInteraction(true))
		{
			return UserServicePermission.getDisabled(
				UserMessage
					.createError("This service can't be invoked from inside of a workflow."));
		}

		// Check for active workflows if service was declared to check for active
		// workflows
		if (serviceDeclaration.isCheckIfRecordHasActiveWorkflow())
		{
			return disableIfActiveWorkflowExists(context);
		}

		return UserServicePermission.getEnabled();

	}

	// Disable it if the record already has an Active instance of this Workflow
	public UserServicePermission disableIfActiveWorkflowExists(
		ServicePermissionRuleContext<RecordEntitySelection> context)
	{
		// return enabled if no record has been selected yet
		if (context.getEntitySelection().getRecord() == null)
		{
			return UserServicePermission.getEnabled();
		}

		// get list of process instance keys for the given workflow
		Repository repo = context.getEntitySelection().getDataspace().getRepository();
		WorkflowEngine workflowEngine = WorkflowEngine
			.getFromRepository(repo, context.getSession());
		List<ProcessInstanceKey> processInstanceKeyList = workflowEngine
			.getProcessInstanceKeys(serviceDeclaration.getWorkflowName());

		// check if there active workflow for this record
		for (ProcessInstanceKey processInstanceKey : processInstanceKeyList)
		{
			ProcessInstance processInstance = workflowEngine.getProcessInstance(processInstanceKey);
			if (!workflowEngine.getProcessInstance(processInstanceKey).isCompleted())
			{
				Adaptation record = null;
				try
				{
					// get the record from the active workflow and if it is the selected record,
					// then return Disabled
					record = WorkflowUtilities
						.getRecord(processInstance.getDataContext(), null, repo);
				}
				catch (OperationException e)
				{
					// if there is a problem getting the record from one of the other workflows, just log it and continue
					LoggingCategory.getKernel()
						.error("Error getting record from existing process instance.", e);
				}
				if (record != null && record.getOccurrencePrimaryKey().format().equals(
					context.getEntitySelection().getRecord().getOccurrencePrimaryKey().format()))
				{
					return UserServicePermission.getDisabled(
						UserMessage.createError(
							"Cannot select record because it is currently being used in an active workflow for this service."));

				}
			}
		}

		return UserServicePermission.getEnabled();
	}

	public UpdateRecordWorkflowLauncherServiceDeclaration getServiceDeclaration()
	{
		return serviceDeclaration;
	}

	public void setServiceDeclaration(
		UpdateRecordWorkflowLauncherServiceDeclaration serviceDeclaration)
	{
		this.serviceDeclaration = serviceDeclaration;
	}

}
