package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.service.*;

/**
 * Hides a node when inside a workflow on creation of a record
 */
public class HideWithinWorkflowAccessRuleForCreate implements AccessRuleForCreate
{
	@Override
	public AccessPermission getPermission(AccessRuleForCreateContext context)
	{
		if (context.getSession().getInteraction(true) != null)
		{
			return AccessPermission.getHidden();
		}
		return AccessPermission.getReadWrite();
	}
}
