/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * An access rule that applies the node permissions of another user (the "permissions user").
 * It can be thought of as restricting the actual user to the template specified in the data set permissions of this
 * permissions user.
 */
public abstract class PermissionsUserNodeAccessRule implements AccessRule
{
	private String permissionsUserId;
	private PermissionsUserManager permissionsUserManager;

	protected PermissionsUserNodeAccessRule(String permissionsUserId)
	{
		this(permissionsUserId, DefaultPermissionsUserManager.getInstance());
	}

	protected PermissionsUserNodeAccessRule(
		String permissionsUserId,
		PermissionsUserManager permissionsUserManager)
	{
		this.permissionsUserId = permissionsUserId;
		this.permissionsUserManager = permissionsUserManager;
	}

	protected abstract boolean isPermissionsUserApplied(
		Adaptation adaptation,
		Session session,
		SchemaNode node);

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory() || isUserAlwaysReadWrite(session)
			|| WorkflowUtilities.isPermissionsUser(session)
			|| !isPermissionsUserApplied(adaptation, session, node))
		{
			return AccessPermission.getReadWrite();
		}

		UserReference permissionsUser = UserReference.forUser(permissionsUserId);
		Repository repo = adaptation.getHome().getRepository();
		SessionPermissions permissions;
		if (permissionsUserManager == null)
		{
			permissions = repo.createSessionPermissionsForUser(permissionsUser);
		}
		else
		{
			permissions = permissionsUserManager.getSessionPermissions(repo, permissionsUser);
		}
		return permissions.getNodeAccessPermission(node, adaptation);
	}

	/**
	 * Defines the logic to determine if a user should always have read/write permission based on the session. 
	 * In this case, the users with the Tech Admin role will always have read/write permission. 
	 * 
	 * This behavior can be overridden if needed. 
	 *
	 * @param session the user session
	 * @return whether the session user is a Tech Admin
	 */
	protected boolean isUserAlwaysReadWrite(Session session)
	{
		return session.isUserInRole(CommonConstants.TECH_ADMIN);
	}
}
