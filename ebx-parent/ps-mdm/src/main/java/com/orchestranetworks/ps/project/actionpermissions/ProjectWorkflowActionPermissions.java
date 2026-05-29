/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.actionpermissions;

import java.util.*;

import com.orchestranetworks.ps.actionpermissions.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.ProcessExecutionContext.*;
import com.orchestranetworks.workflow.UserTask.*;

/**
 * Determines the permissions for a project-related workflow
 */
public class ProjectWorkflowActionPermissions extends DefaultWorkflowActionPermissions
{
	protected ProjectWorkflowPermissionsInfoFactory getPermissionsInfoFactory()
	{
		return DefaultProjectWorkflowPermissionsInfoFactory.getInstance();
	}

	/**
	 * Always returns true, but can be overridden if caching should not be used
	 * under certain circumstances.
	 */
	protected boolean isCachingWorkflow(ActionPermissionsOnWorkflowContext context)
	{
		return true;
	}

	@Override
	protected boolean isNonWorkflowRoleUserPermitted(
		ActionPermissionsOnWorkflowContext context,
		WorkflowPermission permissionType)
	{
		// If caching, then use the cache to see if user is permitted
		// (which will init the cache if it's empty)
		if (isCachingWorkflow(context))
		{
			return ProjectWorkflowPermissionsCache.getInstance().isUserPermitted(
				context.getProcessInstanceKey(),
				context.getSession(),
				context.getRepository(),
				getPermissionsInfoFactory(),
				permissionType);
		}
		// Else don't use cache
		return isUserPermittedWhenNotCaching(context, permissionType);
	}

	/**
	 * By default, attempts the same sort of logic that caching does, except it will happen each time through,
	 * and attempts to do it in the least performance-intensive way. However, this can be overridden for different logic.
	 * 
	 * @param context the context
	 * @param permissionType the permission type
	 * @return whether the user is permitted
	 */
	protected boolean isUserPermittedWhenNotCaching(
		ActionPermissionsOnWorkflowContext context,
		WorkflowPermission permissionType)
	{
		Session session = context.getSession();

		// Create the permissions info and fill it with data context users BEFORE getting the
		// process instance,
		// because that is expensive on performance
		ProjectWorkflowPermissionsInfo permissionsInfo = notCachingPermissionPreProcessInstanceFetch(
			context);
		if (permissionsInfo.isUserPermitted(session, permissionType))
		{
			return true;
		}

		// Now get the process instance and complete the permissions check
		ProcessInstance processInstance = context.getProcessInstance();
		return notCachingPermissionPostProcessInstanceFetch(
			permissionsInfo,
			context,
			session,
			processInstance,
			permissionType);
	}

	// This essentially does the same as isUserPermittedWhenNotCaching, except it can be called from
	// the recursive check
	// through the subworkflows because at that point we have the process instance. We DON'T want to
	// simply call this from
	// the isUserPermittedWhenNotCaching even though that would make for prettier code because we
	// want to separate the
	// pre-process fetch & post-process fetch logic in that case. We can't just use
	// isUserPermittedWhenNotCaching because we don't
	// have a ProcessExecutionInfoContext on the subworkflows.
	private boolean isUserPermittedForSubworkflowWhenNotCaching(
		DataContextReadOnly dataContext,
		Session session,
		ProcessInstance subWorkflowProcessInstance,
		WorkflowPermission permissionType)
	{
		ProjectWorkflowPermissionsInfo permissionsInfo = notCachingPermissionPreProcessInstanceFetch(
			dataContext);
		if (permissionsInfo.isUserPermitted(session, permissionType))
		{
			return true;
		}

		return notCachingPermissionPostProcessInstanceFetch(
			permissionsInfo,
			dataContext,
			session,
			subWorkflowProcessInstance,
			permissionType);
	}

	// Do the logic that can be done prior to fetching the process instance, since that is expensive
	// on performance.
	// This entails creating the permissions info with the data context users.
	private ProjectWorkflowPermissionsInfo notCachingPermissionPreProcessInstanceFetch(
		DataContextReadOnly dataContext)
	{
		ProjectWorkflowPermissionsInfo permissionsInfo = getPermissionsInfoFactory()
			.createPermissionsInfo(null);
		permissionsInfo.addDataContextUsers(dataContext);
		return permissionsInfo;
	}

	// Do the logic that can be done after fetching the process instance. This entails adding work
	// item users & roles and
	// recursing through subworkflows.
	private boolean notCachingPermissionPostProcessInstanceFetch(
		ProjectWorkflowPermissionsInfo permissionsInfo,
		DataContextReadOnly dataContext,
		Session session,
		ProcessInstance processInstance,
		WorkflowPermission permissionType)
	{
		permissionsInfo.setProcessInstance(processInstance);
		if (permissionsInfo.isUserPermitted(session, permissionType))
		{
			return true;
		}

		boolean completed = processInstance.isCompleted();
		permissionsInfo.setCompleted(completed);
		// Now add the users and roles if it's not completed
		// because completed is always in project team by then
		if (!completed)
		{
			// Add this workflow's users and roles and check if that's enough to grant permission
			List<WorkItem> workItems = processInstance.getWorkItems();
			permissionsInfo
				.addWorkItemsUsersAndRoles(WorkflowUtilities.getWorkItemUsers(workItems));
			if (permissionsInfo.isUserPermitted(session, permissionType))
			{
				return true;
			}
			// If we should recurse through subworkflows then check each subworkflow. This
			// recursively will end up calling
			// back into this method with the subworkflow.
			if (permissionsInfo.shouldRecurseSubworkflows(permissionType))
			{
				boolean permitted = false;
				List<ProcessInstance> subWorkflows = processInstance.getCurrentSubWorkflows();
				for (int i = 0; !permitted && i < subWorkflows.size(); i++)
				{
					ProcessInstance subWorkflow = subWorkflows.get(i);
					permitted = isUserPermittedForSubworkflowWhenNotCaching(
						subWorkflow.getDataContext(),
						session,
						subWorkflow,
						permissionType);
				}
				return permitted;
			}
		}
		// Completed can be based solely on the data context above so if that was false
		// it will be false here
		return false;
	}
}
