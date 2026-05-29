/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.actionpermissions;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.ProcessExecutionContext.*;
import com.orchestranetworks.workflow.UserTask.*;

/**
 * Determines the permissions for a workflow. By default is limited to users in workflow manager
 * or monitor roles but can be extended for different behavior.
 */
public class DefaultWorkflowActionPermissions extends ActionPermissionsOnWorkflow
{
	// TODO: Might make sense for these to be true by default, but for backwards compatibility they're false
	private boolean viewAllowedByCurrentRoles;
	private boolean allocateAllowedByCurrentRoles;

	/**
	 * Determine if a user has a particular workflow-related role.
	 * Should be extended if the default Workflow Manager or Workflow Monitor aren't sufficient
	 * 
	 * @param repo the repository
	 * @param session the session
	 * @param context the data context
	 * @param workflowRole a string representing the role
	 * @return whether the user has the role
	 */
	protected boolean isWorkflowRole(
		Repository repo,
		Session session,
		DataContextReadOnly context,
		String workflowRole)
	{
		return session.isUserInRole(Role.forSpecificRole(workflowRole));
	}

	/**
	 * Determine if the user who is not in one of the special workflow roles
	 * is permitted to access the workflow. By default, they are not.
	 * 
	 * @param context the permissions context
	 * @param permissionType the permission type. Only <code>VIEW</code> and <code>MANAGE_ALLOCATION</code> supported.
	 * @return whether the user is permitted
	 */
	protected boolean isNonWorkflowRoleUserPermitted(
		ActionPermissionsOnWorkflowContext context,
		WorkflowPermission permissionType)
	{
		return false;
	}

	/**
	 * Determine if the user can administer workflows
	 * @param context the action permissions context
	 * @return whether the user can administer workflows
	 */
	protected boolean canUserAdminister(ActionPermissionsOnWorkflowContext context)
	{
		// No one can administer (other than Tech Admin, which is handled elsewhere)
		return false;
	}

	/**
	 * Determine if the user can allocate workflows
	 * @param context the action permissions context
	 * @return whether the user can allocate workflows
	 */
	protected boolean canUserAllocate(ActionPermissionsOnWorkflowContext context)
	{
		return isNonWorkflowRoleUserPermitted(context, WorkflowPermission.MANAGE_ALLOCATION)
			|| isWorkflowRole(
				context.getRepository(),
				context.getSession(),
				context,
				WorkflowConstants.ROLE_WORKFLOW_MANAGER)
			|| canUserAdminister(context)
			|| (isAllocateAllowedByCurrentRoles() && isUserInCurrentRoles(context));
	}

	/**
	 * Determine if the user can view workflows
	 * @param context the action permissions context
	 * @return whether the user can view workflows
	 */
	protected boolean canUserView(ActionPermissionsOnWorkflowContext context)
	{
		return isNonWorkflowRoleUserPermitted(context, WorkflowPermission.VIEW)
			|| canUserAllocate(context)
			|| isWorkflowRole(
				context.getRepository(),
				context.getSession(),
				context,
				WorkflowConstants.ROLE_WORKFLOW_MONITOR)
			|| ((isAllocateAllowedByCurrentRoles() || isViewAllowedByCurrentRoles())
				&& isUserInCurrentRoles(context));
	}

	/**
	 * Determine if the user can launch workflows. By default, no one can launch but can be overridden.
	 * @param context the action permissions context
	 * @return whether the user can launch workflows
	 */
	protected boolean canUserLaunch(ActionPermissionsOnWorkflowContext context)
	{
		return false;
	}

	private static boolean isUserInCurrentRoles(ActionPermissionsOnWorkflowContext context)
	{
		Session session = context.getSession();
		List<WorkItem> workItems = context.getProcessInstance().getWorkItems();
		Iterator<WorkItem> workItemIter = workItems.iterator();
		boolean returnVal = false;
		while (!returnVal && workItemIter.hasNext())
		{
			WorkItem workItem = workItemIter.next();
			List<Profile> profiles = workItem.getOfferedToProfiles();
			Iterator<Profile> profileIter = profiles.iterator();
			while (!returnVal && profileIter.hasNext())
			{
				Profile profile = profileIter.next();
				// In theory could offer to "everyone" or "administrator" or
				// specific user. We're not going to do that, but I'll put this
				// check in just in case.
				if (profile.isSpecificRole())
				{
					returnVal = session.isUserInRole(profile);
				}
			}
		}
		return returnVal;
	}

	@Override
	public ActionPermission getActionPermission(
		WorkflowPermission workflowPermission,
		ActionPermissionsOnWorkflowContext context)
	{
		if (isUserAlwaysEnabled(context.getSession()))
		{
			return ActionPermission.getEnabled();
		}
		boolean enabled = false;
		// Check if the user can do these 3 actions
		if (context.isOnProcessInstance())
		{
			if (WorkflowPermission.ADMINISTRATE.equals(workflowPermission))
			{
				enabled = canUserAdminister(context);
			}
			else if (WorkflowPermission.MANAGE_ALLOCATION.equals(workflowPermission))
			{
				enabled = canUserAllocate(context);
			}
			else if (WorkflowPermission.VIEW.equals(workflowPermission))
			{
				enabled = canUserView(context);
			}
			// User not allowed to do anything other than those actions
		}
		else
		{
			if (WorkflowPermission.CREATE_PROCESS.equals(workflowPermission))
			{
				enabled = canUserLaunch(context);
			}
		}

		return enabled ? ActionPermission.getEnabled()
			: ActionPermission.getHidden(
				UserMessage
					.createError("You don't have permission to execute the specified action."));
	}

	protected boolean isUserAlwaysEnabled(Session session)
	{
		return session.isUserInRole(CommonConstants.TECH_ADMIN);
	}

	/**
	 * @see #setViewAllowedByCurrentRoles(boolean)
	 */
	public boolean isViewAllowedByCurrentRoles()
	{
		return viewAllowedByCurrentRoles;
	}

	/**
	 * Set whether users in the roles that are currently offered to are allowed to view the workflow.
	 * If {@link #isAllocateAllowedByCurrentRoles()} is <code>true</code>, then this will be ignored
	 * since allocate permission always gives you view permission.
	 * 
	 * @param viewAllowedByCurrentRole whether they can view
	 */
	public void setViewAllowedByCurrentRoles(boolean viewAllowedByCurrentRoles)
	{
		this.viewAllowedByCurrentRoles = viewAllowedByCurrentRoles;
	}

	/**
	 * @see #setAllocateAllowedByCurrentRoles(boolean)
	 */
	public boolean isAllocateAllowedByCurrentRoles()
	{
		return allocateAllowedByCurrentRoles;
	}

	/**
	 * Set whether users in the roles that are currently offered to are allowed to allocate the workflow
	 * 
	 * @param allocateAllowedByCurrentRole whether they can allocate
	 */
	public void setAllocateAllowedByCurrentRoles(boolean allocateAllowedByCurrentRoles)
	{
		this.allocateAllowedByCurrentRoles = allocateAllowedByCurrentRoles;
	}
}
