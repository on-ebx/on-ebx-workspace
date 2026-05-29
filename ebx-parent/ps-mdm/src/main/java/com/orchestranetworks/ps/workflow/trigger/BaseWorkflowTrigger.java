package com.orchestranetworks.ps.workflow.trigger;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.workflow.*;

/**
 * Base trigger for workflows. 
 * First feature:
 * When used in conjunction with GeneralApproverUserTask, this trigger will prevent allocation
 * of a user task to an invalid self-approver.  Requires the use of a data context variable indicating
 * the approval step.  Extend this class if additional trigger behaviors are needed.
 */
public class BaseWorkflowTrigger extends WorkflowTriggerBean implements WorkflowConstants
{
	@Override
	public void handleAfterProcessInstanceStart(
		WorkflowTriggerAfterProcessInstanceStartContext aContext)
		throws OperationException
	{
	}

	@Override
	public void handleAfterWorkItemCreation(WorkflowTriggerAfterWorkItemCreationContext aContext)
		throws OperationException
	{
	}

	@Override
	public void handleBeforeProcessInstanceTermination(
		WorkflowTriggerBeforeProcessInstanceTerminationContext aContext)
		throws OperationException
	{
	}

	@Override
	public void handleBeforeWorkItemAllocation(
		WorkflowTriggerBeforeWorkItemAllocationContext aContext)
		throws OperationException
	{
		preventSelfApproval(aContext, aContext.getUserReference());
	}

	@Override
	public void handleBeforeWorkItemDeallocation(
		WorkflowTriggerBeforeWorkItemDeallocationContext aContext)
		throws OperationException
	{
	}

	@Override
	public void handleBeforeWorkItemReallocation(
		WorkflowTriggerBeforeWorkItemReallocationContext aContext)
		throws OperationException
	{
		preventSelfApproval(aContext, aContext.getUserReference());
	}

	@Override
	public void handleBeforeWorkItemStart(WorkflowTriggerBeforeWorkItemStartContext aContext)
		throws OperationException
	{
		preventSelfApproval(aContext, aContext.getWorkItem().getUserReference());
	}

	@Override
	public void handleBeforeWorkItemTermination(
		WorkflowTriggerBeforeWorkItemTerminationContext aContext)
		throws OperationException
	{
	}

	protected void preventSelfApproval(
		WorkflowTriggerWorkItemContext aContext,
		UserReference forAllocation)
		throws OperationException
	{
		if (aContext.isVariableDefined(PARAM_APPROVAL_STEP))
		{
			String approvalStep = aContext.getVariableString(PARAM_APPROVAL_STEP);
			if (Boolean.TRUE.toString().equals(approvalStep))
			{
				String selfApproverRoleName = null;
				if (aContext.isVariableDefined(PARAM_SELF_APPROVER_ROLE))
				{
					selfApproverRoleName = aContext.getVariableString(PARAM_SELF_APPROVER_ROLE);
				}
				UserReference creator = getCreator(aContext);
				Session session = aContext.getSession();
				if (forAllocation == null)
					forAllocation = session.getUserReference();
				DirectoryHandler dir = DirectoryHandler.getInstance(aContext.getRepository());
				if (creator.equals(forAllocation) && (selfApproverRoleName == null || !dir
					.isUserInRole(forAllocation, Role.forSpecificRole(selfApproverRoleName))))
				{
					throw OperationException
						.createError("You are not authorized to approve your own requests.");
				}
			}
		}
	}

	private UserReference getCreator(WorkflowTriggerWorkItemContext aContext)
	{
		//use the processInstance creator or the currentUser?
		if (aContext.isVariableDefined(PARAM_CURRENT_USER_ID))
		{
			String userId = aContext.getVariableString(PARAM_CURRENT_USER_ID);
			if (userId != null && !userId.isEmpty())
			{
				return UserReference.forUser(userId);
			}
		}

		return aContext.getProcessInstanceCreator();
	}

}
