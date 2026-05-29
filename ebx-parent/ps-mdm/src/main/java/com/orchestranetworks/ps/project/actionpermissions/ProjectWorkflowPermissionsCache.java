/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.actionpermissions;

import java.lang.ref.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.ProcessExecutionContext.*;
import com.orchestranetworks.workflow.UserTask.*;

// TODO: Refactor the whole project workflow cache to not require a project. For the most part, it's
// relevant to any workflow
// with a record, not specifically a project record. Subclass will handle the project team member
// stuff.
// Will require a path config specific to workflow events. I believe this can all be done without
// affecting the interface
// currently used by workflows, thus being backwards-compatible.
/**
 * Stores users allowed to view/allocate workflows in memory for faster reads during permissions
 * checks.
 * This is a singleton class so only one is used per instance of EBX.
 */
public class ProjectWorkflowPermissionsCache
{
	// Use a soft reference so that EBX can claim the memory used by this cache if needed
	private static SoftReference<ProjectWorkflowPermissionsCache> instanceRef;

	// Key = the key of the workflow instance, Value = a ProjectWorkflowPermissionsInfo object
	// containing the users & roles
	private final Map<ProcessInstanceKey, ProjectWorkflowPermissionsInfo> cacheMap = new HashMap<>();

	/**
	 * Get the singleton instance of this class
	 * 
	 * @return the singleton
	 */
	public static ProjectWorkflowPermissionsCache getInstance()
	{
		// Get the object referenced by the soft reference
		ProjectWorkflowPermissionsCache instance = instanceRef == null ? null : instanceRef.get();
		// If it's null (it's either never been initiated or the garbage collector cleaned it up)
		// then create a new instance of the class and store it in the soft reference
		if (instance == null)
		{
			synchronized (ProjectWorkflowPermissionsCache.class)
			{
				LoggingCategory.getKernel()
					.debug("ProjectWorkflowPermissionsCache: Initializing new instance.");
				instance = new ProjectWorkflowPermissionsCache();
				instanceRef = new SoftReference<>(instance);
			}
		}
		return instance;
	}

	/**
	 * Check whether a user is permitted to access an active or completed workflow
	 * based on the cached information. If the cache is clean then will initialize
	 * it for that workflow.
	 * 
	 * @param processInstanceKey the workflow key
	 * @param session the user's session
	 * @param repo the repository
	 * @param permissionsInfoFactory the factory for the <code>ProjectWorkflowPermissionsInfo</code>
	 * @param permissionType the permission type
	 */
	public final boolean isUserPermitted(
		ProcessInstanceKey processInstanceKey,
		Session session,
		Repository repo,
		ProjectWorkflowPermissionsInfoFactory permissionsInfoFactory,
		WorkflowPermission permissionType)
	{
		ProjectWorkflowPermissionsInfo permissionsInfo = cacheMap.get(processInstanceKey);
		// If the map doesn't contain permissions info for this workflow
		if (permissionsInfo == null)
		{
			WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
			ProcessInstance processInstance = wfEngine.getProcessInstance(processInstanceKey);
			List<WorkItem> workItems = processInstance.getWorkItems();
			// Initialize the cache for this workflow
			permissionsInfo = refreshCache(
				processInstance,
				session,
				repo,
				WorkflowUtilities.getWorkItemUsers(workItems),
				null,
				permissionsInfoFactory);
		}

		// Shouldn't really be null at this point but maybe some other process
		// cleared the cache out. Just in case we'll return false.
		if (permissionsInfo == null)
		{
			return false;
		}
		// If they're permitted based on the info from this workflow, then return true
		if (permissionsInfo.isUserPermitted(session, permissionType))
		{
			return true;
		}

		// If we aren't supposed to recurse subworkflows for this permission, then they're not
		// permitted
		if (!permissionsInfo.shouldRecurseSubworkflows(permissionType))
		{
			return false;
		}

		// Recurse the subworkflows to see if any of them give the user permission
		boolean permitted = false;
		Set<ProcessInstanceKey> subWorkflowKeys = permissionsInfo.getSubWorkflowKeys();
		Iterator<ProcessInstanceKey> iter = subWorkflowKeys.iterator();
		while (!permitted && iter.hasNext())
		{
			ProcessInstanceKey subWorkflowKey = iter.next();
			permitted = isUserPermitted(
				subWorkflowKey,
				session,
				repo,
				permissionsInfoFactory,
				permissionType);
		}
		return permitted;
	}

	/**
	 * Refresh (or initialize) the cache for a workflow
	 * 
	 * @param processInstance the workflow
	 * @param session the session
	 * @param repo the repository
	 * @param users the users being allocated to, or <code>null</code> if not allocating
	 * @param terminatingSubWorkflowKey the key of the subworkflow that's terminating, or <code>null</code> if a subworkflow is not terminating
	 * @param permissionsInfoFactory the factory for the <code>ProjectWorkflowPermissionsInfo</code>
	 * @return the permissions info that was created for this workflow
	 */
	public final ProjectWorkflowPermissionsInfo refreshCache(
		ProcessInstance processInstance,
		Session session,
		Repository repo,
		Set<UserReference> users,
		ProcessInstanceKey terminatingSubWorkflowKey,
		ProjectWorkflowPermissionsInfoFactory permissionsInfoFactory)
	{
		ProjectWorkflowPermissionsInfo permissionsInfo = permissionsInfoFactory
			.createPermissionsInfo(processInstance);
		// Set up all the users & roles
		permissionsInfo.init(users, terminatingSubWorkflowKey);

		synchronized (cacheMap)
		{
			// Store the info in the map
			cacheMap.put(processInstance.getProcessInstanceKey(), permissionsInfo);
		}
		return permissionsInfo;
	}

	/**
	 * Clear all of the cache entries
	 */
	public final void clearAllCache()
	{
		synchronized (cacheMap)
		{
			cacheMap.clear();
		}
	}

	/**
	 * Clear the cache for a workflow
	 * 
	 * @param processInstance the workflow
	 */
	public final void clearCache(ProcessInstance processInstance)
	{
		synchronized (cacheMap)
		{
			cacheMap.remove(processInstance.getProcessInstanceKey());
		}
	}

	private ProjectWorkflowPermissionsCache()
	{
	}
}
