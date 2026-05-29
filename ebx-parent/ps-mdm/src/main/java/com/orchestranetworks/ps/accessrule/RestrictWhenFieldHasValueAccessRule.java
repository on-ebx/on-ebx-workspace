package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Restrict a node when there is a value for a given field.
 * This is configured with the path to the field to check, relative to the root,
 * and the permission to set when it's restricted (i.e. hidden or read-only).
 * The restricted permission will be enhanced with an appropriate message
 * unless it already has a message.
 */
public class RestrictWhenFieldHasValueAccessRule implements AccessRule
{
	private Path fieldPath;
	private AccessPermission restrictedPermission;

	public RestrictWhenFieldHasValueAccessRule(
		Path fieldPath,
		AccessPermission restrictedPermission)
	{
		this.fieldPath = fieldPath;
		this.restrictedPermission = restrictedPermission;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isSchemaInstance() || adaptation.isHistory()
			|| session.isUserInRole(CommonConstants.TECH_ADMIN)
			|| WorkflowUtilities.isPermissionsUser(session))
		{
			return AccessPermission.getReadWrite();
		}
		Object value = adaptation.get(fieldPath);
		if (value == null)
		{
			return AccessPermission.getReadWrite();
		}
		if (restrictedPermission.getReadOnlyReason() == null)
		{
			UserMessage msg = createErrorMessage(session, node);
			return createRestrictedPermission(msg);
		}
		return restrictedPermission;
	}

	private UserMessage createErrorMessage(Session session, SchemaNode node)
	{
		Locale locale = session.getLocale();
		SchemaNode fieldNode = node.getTableNode().getTableOccurrenceRootNode().getNode(fieldPath);
		StringBuilder bldr = new StringBuilder(node.getLabel(locale)).append(" is ");
		if (restrictedPermission.isHidden())
		{
			bldr.append("hidden");
		}
		else
		{
			bldr.append("read-only");
		}
		bldr.append(" when there is a value for ").append(fieldNode.getLabel(locale)).append(".");
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
