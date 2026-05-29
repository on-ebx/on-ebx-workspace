package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

// TODO write an article on this

/**
 * Looks up user access when in the context of a workflow.
 * <p>
 * Tech-admin users always have read-write access. If not in a workflow, other
 * users will have read-only access. When in the context of a workflow,
 * permission is determined by getting tracking information from the session.
 * Many users can be set separated by a ';'.
 * <p>
 * Getting a list of users from this tracking and returning the most restrictive
 * access rule.
 *
 * @author MCH
 */
public class WorkflowAccessRule implements AccessRule
{
	public static final String SEGMENT_WORKFLOW_PERMISSIONS_USERS = "workflowPermissionsUsers";
	public static final String TRACKING_INFO_PERMISSIONS_USERS_SEPARATOR = ";";

	/** The role giving permission despite the rule */
	private Role permissiveRole = CommonConstants.TECH_ADMIN;
	protected Set<String> nonWorkflowWritableRoles;

	private TrackingInfoHelper trackingInfoHelper;
	private PermissionsUserManager permissionsUserManager;

	public WorkflowAccessRule()
	{
		this(new HashSet<String>());
	}

	@Deprecated
	public WorkflowAccessRule(TrackingInfoHelper trackingInfoHelper)
	{
		this(trackingInfoHelper, new HashSet<String>());
	}

	@Deprecated
	public WorkflowAccessRule(Set<String> nonWorkflowWritableRoles)
	{
		this(
			new FirstSegmentTrackingInfoHelper(SEGMENT_WORKFLOW_PERMISSIONS_USERS),
			nonWorkflowWritableRoles);
	}

	@Deprecated
	public WorkflowAccessRule(
		TrackingInfoHelper trackingInfoHelper,
		Set<String> nonWorkflowWritableRoles)
	{
		this(
			trackingInfoHelper,
			nonWorkflowWritableRoles,
			DefaultPermissionsUserManager.getInstance());
	}

	@Deprecated
	public WorkflowAccessRule(
		TrackingInfoHelper trackingInfoHelper,
		Set<String> nonWorkflowWritableRoles,
		PermissionsUserManager permissionsUserManager)
	{
		this.trackingInfoHelper = trackingInfoHelper;
		this.nonWorkflowWritableRoles = nonWorkflowWritableRoles;
		this.permissionsUserManager = permissionsUserManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.orchestranetworks.service.AccessRule#getPermission(com.onwbp.adaptation
	 * .Adaptation, com.orchestranetworks.service.Session,
	 * com.orchestranetworks.schema.SchemaNode)
	 */
	@Override
	public AccessPermission getPermission(
		final Adaptation pAdaptation,
		final Session pSession,
		final SchemaNode pNode)
	{
		/*
		 * When we call for the permissions for the Permissions User later in this
		 * method, it will end up calling back into this rule. So for all Permissions
		 * Users, skip all this.
		 */
		if (pAdaptation.isHistory() || isPermissionsUser(pSession))
		{
			return AccessPermission.getReadWrite();
		}

		if ((this.permissiveRole != null && pSession.isUserInRole(this.permissiveRole)))
		{
			return AccessPermission.getReadWrite();
		}

		if (!WorkflowUtilities.isInWorkflow(pSession))
		{
			return getNonWorkflowPermission(pAdaptation, pSession, pNode);
		}

		final String trackingInfo = convertToUserId(pSession.getTrackingInfo());
		if (trackingInfo == null)
		{
			return AccessPermission.getReadWrite();
		}

		String trackingInfoSeg;
		TrackingInfoHelper tiHelper = getTrackingInfoHelper();
		synchronized (trackingInfoHelper)
		{
			tiHelper.initTrackingInfo(trackingInfo);
			trackingInfoSeg = tiHelper.getTrackingInfoSegment(SEGMENT_WORKFLOW_PERMISSIONS_USERS);
		}

		Repository repository = pAdaptation.getHome().getRepository();
		SessionPermissions permissions = null;
		AccessPermission userAccessPermission = null;
		AccessPermission accessPermission = AccessPermission.getReadWrite();
		PermissionsUserManager puManager = getPermissionsUserManager();
		final String[] users = trackingInfoSeg.split(TRACKING_INFO_PERMISSIONS_USERS_SEPARATOR);
		for (String user : users)
		{
			if (!user.isEmpty())
			{
				UserReference userReference = UserReference.forUser(user);
				if (puManager == null)
				{
					if (WorkflowUtilities.isPermissionsUser(repository, userReference))
					{
						permissions = repository.createSessionPermissionsForUser(userReference);
					}
					else
					{
						permissions = null;
					}
				}
				else
				{
					permissions = puManager.getSessionPermissions(repository, userReference);
				}
				if (permissions == null)
				{
					continue;
				}
				userAccessPermission = permissions.getNodeAccessPermission(pNode, pAdaptation);
				accessPermission = joinPermissions(accessPermission, userAccessPermission);
			}
		}

		return accessPermission;
	}

	protected AccessPermission joinPermissions(
		AccessPermission permission1,
		AccessPermission permission2)
	{
		return permission1.min(permission2);
	}

	public String getPermissiveRole()
	{
		return this.permissiveRole.getRoleName();
	}

	public void setPermissiveRole(final String permissiveRole)
	{
		this.permissiveRole = "administrator".equals(permissiveRole) ? Role.ADMINISTRATOR
			: Role.forSpecificRole(permissiveRole);
	}

	/**
	 * Return whether the current user is a special "permissions user", so that the
	 * permissions check can be ignored.
	 */
	protected boolean isPermissionsUser(Session session)
	{
		return WorkflowUtilities.isPermissionsUser(session);
	}

	protected AccessPermission getRestrictedPermission()
	{
		return AccessPermission.getReadOnly();
	}

	/**
	 * Determines whether the given data space is writable outside of the workflow,
	 * assuming the user has permission to write outside of workflow. By default, it
	 * returns <code>true</code>, but can be overridden to say, for example, only
	 * master data space is writable.
	 *
	 * @param dataSpace     the data space
	 * @param userReference the user
	 * @return whether data space is writable
	 */
	protected boolean isDataSpaceWritableOutsideWorkflow(
		AdaptationHome dataSpace,
		UserReference userReference)
	{
		return true;
	}

	/**
	 * Get the permission to use when not in a workflow
	 *
	 * @param adaptation the adaptation
	 * @param session    the session
	 * @param node       the schema node
	 * @return the permission
	 */
	protected AccessPermission getNonWorkflowPermission(
		Adaptation adaptation,
		Session session,
		SchemaNode node)
	{

		// If not in a permissive role, only can update if data space is writable
		// outside workflow
		if (isDataSpaceWritableOutsideWorkflow(adaptation.getHome(), session.getUserReference()))
		{
			// Only those in the roles specified can update
			for (String roleName : getNonWorkflowWritableRoles())
			{
				Role role = Role.forSpecificRole(roleName);

				if (session.getDirectory().isSpecificRoleDefined(role)
					&& session.isUserInRole(role))
				{
					return AccessPermission.getReadWrite();
				}
			}
		}
		return getRestrictedPermission();
	}

	protected String convertToUserId(String trackingInfo)
	{
		return trackingInfo == null ? null : trackingInfo.replaceAll(" ", "_");
	}

	public Set<String> getNonWorkflowWritableRoles()
	{
		return nonWorkflowWritableRoles;
	}

	public void setNonWorkflowWritableRoles(Set<String> nonWorkflowWritableRoles)
	{
		this.nonWorkflowWritableRoles = nonWorkflowWritableRoles;
	}

	public TrackingInfoHelper getTrackingInfoHelper()
	{
		return trackingInfoHelper;
	}

	public void setTrackingInfoHelper(TrackingInfoHelper trackingInfoHelper)
	{
		this.trackingInfoHelper = trackingInfoHelper;
	}

	public PermissionsUserManager getPermissionsUserManager()
	{
		return permissionsUserManager;
	}

	public void setPermissionsUserManager(PermissionsUserManager permissionsUserManager)
	{
		this.permissionsUserManager = permissionsUserManager;
	}
}
