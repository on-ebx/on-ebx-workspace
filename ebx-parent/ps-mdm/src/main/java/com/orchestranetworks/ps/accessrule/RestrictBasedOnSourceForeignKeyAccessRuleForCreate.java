package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Restrict a node in a record based on if it's being created from specific
 * source record foreign key drop-downs. The source record foreign key paths passed in should be the full path.
 * The restricted permission will be enhanced with an appropriate message
 * unless it already has a message.
 */
public class RestrictBasedOnSourceForeignKeyAccessRuleForCreate implements AccessRuleForCreate
{
	private Set<Path> sourceForeignKeyPathsToRestrict;
	private AccessPermission restrictedPermission;

	public RestrictBasedOnSourceForeignKeyAccessRuleForCreate(
		Path[] sourceForeignKeyPathsToRestrict,
		AccessPermission restrictedPermission)
	{
		this.sourceForeignKeyPathsToRestrict = new HashSet<>(
			Arrays.asList(sourceForeignKeyPathsToRestrict));
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
		SchemaNode sourceForeignKeyNode = context.getReferencingNode();
		// If null, it's not being created via foreign key drop-down
		if (sourceForeignKeyNode == null)
		{
			return AccessPermission.getReadWrite();
		}

		// Get the path of the source foreign key, and if in the set,
		// return the restricted permission that was specified
		Path sourceForeignKeyPath = sourceForeignKeyNode.getPathInSchema();
		if (sourceForeignKeyPathsToRestrict.contains(sourceForeignKeyNode.getPathInSchema()))
		{
			if (restrictedPermission.getReadOnlyReason() == null)
			{
				UserMessage msg = createErrorMessage(session, sourceForeignKeyPath);
				return createRestrictedPermission(msg);
			}
			return restrictedPermission;
		}
		return AccessPermission.getReadWrite();
	}

	private UserMessage createErrorMessage(Session session, Path sourceForeignKeyPath)
	{
		StringBuilder bldr = new StringBuilder();
		if (restrictedPermission.isHidden())
		{
			bldr.append("Hidden");
		}
		else
		{
			bldr.append("Read-only");
		}
		bldr.append(" when created from ").append(sourceForeignKeyPath.format()).append(
			" foreign key.");
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
