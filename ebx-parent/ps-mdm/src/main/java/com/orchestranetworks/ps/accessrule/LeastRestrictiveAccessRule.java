/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to return the least restrictive permissions from the given access rules.
 * For example, if one rule returns Hidden and a 2nd rule returns ReadOnly, this will return ReadOnly.
 * The {@link AccessRulesManager} applies the most restrictive rule, so in this way you can essentially layer
 * a logical "or" condition.
 */
public class LeastRestrictiveAccessRule implements AccessRule
{
	private AccessRule[] rules;

	public LeastRestrictiveAccessRule()
	{
		this(new AccessRule[0]);
	}

	public LeastRestrictiveAccessRule(AccessRule[] rules)
	{
		this.rules = rules;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		AccessPermission returnValue = AccessPermission.getHidden();
		for (int i = 0; i < rules.length
			&& !AccessPermission.getReadWrite().equals(returnValue); i++)
		{
			AccessRule rule = rules[i];
			AccessPermission permission = rule.getPermission(adaptation, session, node);
			if (!AccessPermission.getHidden().equals(permission))
			{
				if (AccessPermission.getReadWrite().equals(permission)
					|| (AccessPermission.getReadOnly().equals(permission)
						&& AccessPermission.getHidden().equals(returnValue)))
				{
					returnValue = permission;
				}
			}
		}
		return returnValue;
	}

	public AccessRule[] getRules()
	{
		return this.rules;
	}

	public void setRules(AccessRule[] rules)
	{
		this.rules = rules;
	}
}
