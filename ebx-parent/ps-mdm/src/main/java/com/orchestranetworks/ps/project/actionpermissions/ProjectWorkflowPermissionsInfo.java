/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.actionpermissions;

import java.util.*;

import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.ProcessExecutionContext.*;
import com.orchestranetworks.workflow.UserTask.*;

/**
 * A class that encapsulates the information needed to determine workflow permissions for a
 * project-related workflow.
 */
public class ProjectWorkflowPermissionsInfo
{
	protected ProcessInstance processInstance;
	protected boolean completed;

	// Use sets for roles & users so if a user id or role name is added twice, it will only be in
	// there once.
	// Would like to be able to determine permissions per work item, but that's not possible with
	// the API.
	// If a user has permission for any of the work items, he has permission for all of them.
	protected Set<Role> roles = new HashSet<>();
	protected Set<String> userIds = new HashSet<>();

	protected Set<ProcessInstanceKey> subWorkflowKeys = new HashSet<>();

	public ProjectWorkflowPermissionsInfo()
	{
		this(null);
	}

	public ProjectWorkflowPermissionsInfo(ProcessInstance processInstance)
	{
		this.processInstance = processInstance;
		this.completed = processInstance != null && processInstance.isCompleted();
	}

	public boolean isUserPermitted(Session session, WorkflowPermission permissionType)
	{
		return userIds.contains(session.getUserReference().getUserId()) || isUserInRoles(session);
	}

	protected boolean isUserInRoles(Session session)
	{
		boolean found = false;
		Iterator<Role> iter = roles.iterator();
		while (!found && iter.hasNext())
		{
			Role role = iter.next();
			found = session.isUserInRole(role);
		}
		return found;
	}

	/**
	 * Initialize the permissions info
	 * 
	 * @param users the users being allocated to, or <code>null</code> if not allocating
	 * @param terminatingSubWorkflowKey the key of the subworkflow that's terminating, or <code>null</code> if a subworkflow is not terminating
	 */
	public void init(Set<UserReference> users, ProcessInstanceKey terminatingSubWorkflowKey)
	{
		addDataContextUsers(processInstance.getDataContext());
		if (!completed)
		{
			addWorkItemsUsersAndRoles(users);
			addSubworkflowKeys(processInstance.getCurrentSubWorkflows(), terminatingSubWorkflowKey);
		}
	}

	protected boolean shouldAddRoleWhenAllWorkItemsAllocated(List<WorkItem> workItems)
	{
		return true;
	}

	public void addWorkItemsUsersAndRoles(Set<UserReference> users)
	{
		List<WorkItem> workItems = processInstance.getWorkItems();
		// If role should be added when all work items are allocated or there's no new user (which
		// means it's
		// offered or about to be offered)
		if (!workItems.isEmpty() && (users == null || users.isEmpty()
			|| shouldAddRoleWhenAllWorkItemsAllocated(workItems)))
		{
			addWorkItemsOfferedRoles(workItems);
		}
		if (users != null)
		{
			for (UserReference user : users)
			{
				userIds.add(user.getUserId());
			}
		}
	}

	public void addWorkItemsOfferedRoles(List<WorkItem> workItems)
	{
		for (WorkItem workItem : workItems)
		{
			roles.add(WorkflowUtilities.getWorkItemOfferedToRole(workItem));
		}
	}

	public void addDataContextUsers(DataContextReadOnly dataContext)
	{
		// Get the currentUserId variable from the data context and store it.
		String currentUserId = dataContext
			.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USER_ID)
				? dataContext.getVariableString(WorkflowConstants.PARAM_CURRENT_USER_ID)
				: null;
		if (currentUserId != null)
		{
			userIds.add(currentUserId);
		}

		// Add the project team members
		if (dataContext.isVariableDefined(ProjectWorkflowConstants.PARAM_PROJECT_TEAM_MEMBERS))
		{
			String projectTeamMembers = dataContext
				.getVariableString(ProjectWorkflowConstants.PARAM_PROJECT_TEAM_MEMBERS);
			if (projectTeamMembers != null)
			{
				String[] arr = projectTeamMembers.split(
					String.valueOf(ProjectWorkflowConstants.PROJECT_TEAM_MEMBERS_PARAM_SEPARATOR));
				for (String str : arr)
				{
					userIds.add(str);
				}
			}
		}
	}

	public void addSubworkflowKeys(
		List<ProcessInstance> subWorkflows,
		ProcessInstanceKey terminatingSubWorkflowKey)
	{
		for (ProcessInstance subWorkflow : subWorkflows)
		{
			ProcessInstanceKey subWorkflowKey = subWorkflow.getProcessInstanceKey();
			if (terminatingSubWorkflowKey == null
				|| !subWorkflowKey.equals(terminatingSubWorkflowKey))
			{
				subWorkflowKeys.add(subWorkflowKey);
			}
		}
	}

	public boolean shouldRecurseSubworkflows(WorkflowPermission permissionType)
	{
		// Don't need to recurse for these work item related permissions because it would always be
		// based on
		// the work items in this workflow, not its subworkflows
		return !(WorkflowPermission.ALLOCATE.equals(permissionType)
			|| WorkflowPermission.DEALLOCATE.equals(permissionType)
			|| WorkflowPermission.REALLOCATE.equals(permissionType)
			|| WorkflowPermission.MANAGE_ALLOCATION.equals(permissionType));
	}

	public ProcessInstance getProcessInstance()
	{
		return this.processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance)
	{
		this.processInstance = processInstance;
	}

	public boolean isCompleted()
	{
		return this.completed;
	}

	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}

	public Set<Role> getRoles()
	{
		return this.roles;
	}

	public void setRoles(Set<Role> roles)
	{
		this.roles = roles;
	}

	public Set<String> getUserIds()
	{
		return this.userIds;
	}

	public void setUserIds(Set<String> userIds)
	{
		this.userIds = userIds;
	}

	public Set<ProcessInstanceKey> getSubWorkflowKeys()
	{
		return this.subWorkflowKeys;
	}

	public void setSubWorkflowKeys(Set<ProcessInstanceKey> subWorkflowKeys)
	{
		this.subWorkflowKeys = subWorkflowKeys;
	}
}
