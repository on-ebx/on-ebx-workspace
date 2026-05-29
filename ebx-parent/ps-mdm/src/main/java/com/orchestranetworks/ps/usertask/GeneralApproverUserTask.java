package com.orchestranetworks.ps.usertask;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class GeneralApproverUserTask extends GeneralMaintenanceUserTask
{
	private String selfApproverRoleName = null;

	private boolean allocateCurrentApprover = true;

	public String getSelfApproverRoleName()
	{
		return selfApproverRoleName;
	}

	public void setSelfApproverRoleName(String selfApproverRoleName)
	{
		this.selfApproverRoleName = selfApproverRoleName;
	}

	/**
	 * @return the allocateCurrentApprover
	 */
	public boolean isAllocateCurrentApprover()
	{
		return allocateCurrentApprover;
	}

	/**
	 * @param allocateCurrentApprover the allocateCurrentApprover to set
	 */
	public void setAllocateCurrentApprover(boolean allocateCurrentApprover)
	{
		this.allocateCurrentApprover = allocateCurrentApprover;
	}

	@Override
	protected void addUserAndRole(UserTaskCreationContext context, Role userRole)
		throws OperationException
	{
		// Assign User as Current Approver if there is one already established
		UserReference userReference = null;
		if (allocateCurrentApprover)
		{
			if (isUseRoleNameForCurrentUserDataContextVariable()
					|| (context.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES)))
			{
				super.addUserAndRole(context, userRole);
				return;
			}
			userReference = WorkflowUtilities
				.getCurrentApproverReference(context, context.getRepository());
		}
		addUserReferenceAndRole(context, userRole, userReference);
	}

	@Override
	public void handleCreate(UserTaskCreationContext context) throws OperationException
	{
		if (context.isVariableDefined(PARAM_APPROVAL_STEP))
		{
			context.setVariableString(PARAM_APPROVAL_STEP, Boolean.TRUE.toString());
		}
		String selfApproverRoleName = getSelfApproverRoleName();
		if (context.isVariableDefined(PARAM_SELF_APPROVER_ROLE) && selfApproverRoleName != null
			&& !selfApproverRoleName.isEmpty())
		{
			context.setVariableString(PARAM_SELF_APPROVER_ROLE, selfApproverRoleName);
		}
		super.handleCreate(context);
	}

	@Override
	public void checkBeforeWorkItemCompletion(UserTaskBeforeWorkItemCompletionContext context)
	{
		if (context.isAcceptAction())
		{
			// Exit criteria prevent the currentUserId in the data context
			// from submitting the approval
			// -- allow for self approval if a selfApproverRole is provided and
			// the current user plays that role
			Session session = context.getSession();
			if (session.getUserReference()
				.getUserId()
				.equals(context.getVariableString(WorkflowConstants.PARAM_CURRENT_USER_ID)))
			{
				if (selfApproverRoleName == null
					|| !session.isUserInRole(Role.forSpecificRole(selfApproverRoleName)))
					context.reportMessage(
						UserMessage.createError(
							"You are not allowed to approve your own changes. You must deallocate this work item so that another user can perform the approval task."));
			}
		}
		super.checkBeforeWorkItemCompletion(context);
	}

	@Override
	public void handleWorkItemCompletion(UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		if (context.checkAllWorkItemMatchStrategy())
		{
			if (context.isVariableDefined(PARAM_APPROVAL_STEP))
			{
				context.setVariableString(PARAM_APPROVAL_STEP, Boolean.FALSE.toString());
			}
			if (context.isVariableDefined(PARAM_SELF_APPROVER_ROLE))
			{
				context.setVariableString(PARAM_SELF_APPROVER_ROLE, null);
			}
		}
		super.handleWorkItemCompletion(context);
	}

	@Override
	public void setCurrentUserIdAndLabel(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		// set the currentApproverId variable in the Data Context instead of currentUserId, no need to
		// set a label for the Approver
		if (isUseRoleNameForCurrentUserDataContextVariable())
		{
			super.setCurrentUserIdAndLabel(context);
		}
		else if (context.isVariableDefined(WorkflowConstants.PARAM_CURRENT_APPROVER_ID))
		{
			WorkflowUtilities.setCurrentApprover(context);
		}
	}

}
