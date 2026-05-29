package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;

/**
 * Restrict a node in a record based on if it's being created from specific
 * associations. The association paths passed in should be the full path.
 * The restricted permission will be enhanced with an appropriate message
 * unless it already has a message.
 */
public class RestrictBasedOnSourceAssociationAccessRuleForCreate implements AccessRuleForCreate
{
	private Set<Path> sourceAssociationPathsToRestrict;
	private AccessPermission restrictedPermission;

	public RestrictBasedOnSourceAssociationAccessRuleForCreate(
		Path[] sourceAssociationPathsToRestrict,
		AccessPermission restrictedPermission)
	{
		this.sourceAssociationPathsToRestrict = new HashSet<>(
			Arrays.asList(sourceAssociationPathsToRestrict));
		this.restrictedPermission = restrictedPermission;
	}

	@Override
	public AccessPermission getPermission(AccessRuleForCreateContext context)
	{
		Session session = context.getSession();
		if (session.isUserInRole(CommonConstants.TECH_ADMIN)
			|| WorkflowUtilities.isPermissionsUser(session))
		{
			return AccessPermission.getReadWrite();
		}
		AssociationLink associationLink = context.getAssociationLink();
		// If null, it's not being created via an association
		if (associationLink == null)
		{
			return AccessPermission.getReadWrite();
		}
		// Get the path of the source association, and if in the set,
		// return the restricted permission that was specified
		SchemaNode associationNode = associationLink.getOwnerNode();
		Path associationPath = associationNode.getPathInSchema();
		if (sourceAssociationPathsToRestrict.contains(associationPath))
		{
			if (restrictedPermission.getReadOnlyReason() == null)
			{
				UserMessage msg = createErrorMessage(session, associationNode);
				return createRestrictedPermission(msg);
			}
			return restrictedPermission;
		}
		return AccessPermission.getReadWrite();
	}

	private UserMessage createErrorMessage(Session session, SchemaNode associationNode)
	{
		Locale locale = session.getLocale();
		StringBuilder bldr = new StringBuilder();
		if (restrictedPermission.isHidden())
		{
			bldr.append("Hidden");
		}
		else
		{
			bldr.append("Read-only");
		}
		bldr.append(" when created from ").append(associationNode.getLabel(locale)).append(
			" association.");
		return UserMessage.createError(bldr.toString());
	}

	private AccessPermission createRestrictedPermission(UserMessage message)
	{
		if (restrictedPermission.isHidden())
		{
			return AccessPermission.getHidden(message);
		}
		return AccessPermission.getReadOnly(message);
	}
}
