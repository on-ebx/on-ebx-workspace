/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import java.lang.ref.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;

/**
 * Default implementation of the PermissionsUserManager. 
 * This uses SoftReference to cache the user session permissions so that EBX can claim the memory used by this cache if needed.
 *
 */
public class DefaultPermissionsUserManager implements PermissionsUserManager
{
	// Use a soft reference so that EBX can claim the memory used by this cache if needed
	private static SoftReference<DefaultPermissionsUserManager> instanceRef;

	private final Map<UserReference, SessionPermissions> userSessionPermissions = new HashMap<>();

	public static DefaultPermissionsUserManager getInstance()
	{
		// Get the object referenced by the soft reference
		DefaultPermissionsUserManager instance = instanceRef == null ? null : instanceRef.get();
		// If it's null (it's either never been initiated or the garbage collector cleaned it up)
		// then create a new instance of the class and store it in the soft reference
		if (instance == null)
		{
			synchronized (DefaultPermissionsUserManager.class)
			{
				instance = new DefaultPermissionsUserManager();
				instanceRef = new SoftReference<>(instance);
			}
		}
		return instance;
	}

	/**
	 * Returns the SessionPermissions of the given user. If the permissions for the user is not available in the cache, refreshes from the repository. 
	 * 
	 * @param repo the repository
	 * @param user the user
	 * @return the SessionPermissions
	 */
	public final SessionPermissions getSessionPermissions(Repository repo, UserReference user)
	{
		SessionPermissions permissions;
		// If the map contains an entry for this user already, then get its permissions
		// object (which may be null if it's not actually a permissions user)
		if (userSessionPermissions.containsKey(user))
		{
			permissions = userSessionPermissions.get(user);
		}
		// Otherwise refresh the cache and return the permissions it finds for this user
		// (or null if it's not actually a permissions user)
		else
		{
			permissions = refreshCache(repo, user);
		}
		return permissions;
	}

	/**
	 * Clears the SessionPermissions cache.
	 */
	public final void clearCache()
	{
		synchronized (userSessionPermissions)
		{
			userSessionPermissions.clear();
		}
	}

	private SessionPermissions refreshCache(Repository repo, UserReference user)
	{
		SessionPermissions permissions;
		// If the user is actually a permissions user, then create a permissions object
		// for it
		if (WorkflowUtilities.isPermissionsUser(repo, user))
		{
			permissions = repo.createSessionPermissionsForUser(user);
		}
		// Otherwise, the permissions object will be null, but we will still put that in
		// the map so that we know we've processed this (non-existent) user already
		else
		{
			permissions = null;
		}
		synchronized (userSessionPermissions)
		{
			userSessionPermissions.put(user, permissions);
		}
		return permissions;
	}

	private DefaultPermissionsUserManager()
	{
	}
}
