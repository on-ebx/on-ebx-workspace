/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public class ProjectTypeAccessRule implements AccessRule
{
	protected Path projectTypePath;
	protected PermissionsUserManager permissionsUserManager;

	public ProjectTypeAccessRule()
	{
		this(null);
	}

	public ProjectTypeAccessRule(Path projectTypePath)
	{
		this(projectTypePath, DefaultPermissionsUserManager.getInstance());
	}

	public ProjectTypeAccessRule(Path projectTypePath, PermissionsUserManager permissionsUserManager)
	{
		this.projectTypePath = projectTypePath;
		this.permissionsUserManager = permissionsUserManager;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory() || isUserAlwaysReadWrite(session))
		{
			return AccessPermission.getReadWrite();
		}

		AccessPermission accessPermission;
		String projectType = null;
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction == null)
		{
			if (projectTypePath == null || adaptation.isSchemaInstance())
			{
				return AccessPermission.getReadWrite();
			}
			projectType = adaptation.getString(projectTypePath);
		}
		else
		{
			projectType = interaction.getInputParameters().getVariableString(
				ProjectWorkflowConstants.SESSION_PARAM_PROJECT_TYPE);
			if (projectType == null && projectTypePath != null && !adaptation.isSchemaInstance())
			{
				projectType = adaptation.getString(projectTypePath);
			}
		}

		if (projectType == null)
		{
			accessPermission = AccessPermission.getReadWrite();
		}
		else
		{
			UserReference permissionsUser = UserReference.forUser(getPermissionsUserId(projectType));
			if (session.getDirectory().isUserDefined(permissionsUser))
			{
				accessPermission = getProjectTypePermissionsUserPermission(
					projectType,
					session,
					adaptation,
					node);
			}
			else
			{
				accessPermission = AccessPermission.getReadWrite();
			}
		}
		return accessPermission;
	}

	protected boolean isUserAlwaysReadWrite(Session session)
	{
		// This method will be called back into for the permissions user being applied
		// when used in conjunction with a WorkflowAccessRule, so for more efficiency, return R/W for those.
		return session.isUserInRole(CommonConstants.TECH_ADMIN)
			|| WorkflowUtilities.isPermissionsUser(session);
	}

	protected AccessPermission getProjectTypePermissionsUserPermission(
		String projectType,
		Session session,
		Adaptation adaptation,
		SchemaNode node)
	{
		UserReference permissionsUser = UserReference.forUser(getPermissionsUserId(projectType));
		// TODO: Do we need to check if it's defined? You shouldn't be using this unless you define all the
		//       project type users. Anything we can do to speed this up will be helpful.
		if (session.getDirectory().isUserDefined(permissionsUser))
		{
			Repository repo = adaptation.getHome().getRepository();
			SessionPermissions permissions;
			if (permissionsUserManager == null)
			{
				permissions = repo.createSessionPermissionsForUser(permissionsUser);
			}
			else
			{
				permissions = permissionsUserManager.getSessionPermissions(adaptation.getHome()
					.getRepository(), permissionsUser);
			}
			return permissions.getNodeAccessPermission(node, adaptation);
		}
		return AccessPermission.getReadWrite();
	}

	/**
	 * Return the id used for the permissions user
	 *
	 * @param projectType the project type
	 * @return the permissions user id
	 */
	protected String getPermissionsUserId(String projectType)
	{
		return ProjectWorkflowConstants.PERMISSIONS_USER_PROJECT_TYPE_PREFIX
			+ convertProjectTypeForUserId(projectType);
	}

	/**
	 * Convert the given project type into a string that can be used in a user ID,
	 * because certain characters aren't allowed.
	 * The default behavior is simply to replace spaces with underscores but this could be expanded upon
	 * in the future to do more, or overridden in a subclass.
	 *
	 * @param projectType the project type
	 * @return the converted string
	 */
	protected String convertProjectTypeForUserId(String projectType)
	{
		return projectType == null ? "" : projectType.replace(' ', '_');
	}
}
