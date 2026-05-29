/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Simple Access Rule that can be constructed with a restricted permission (default is hidden)
 * and will return that permission when the session has an interaction (meaning it is in the context of a workflow).
 */
public class RestrictInWorkflowAccessRule implements AccessRule
{
	private AccessPermission restrictionType;

	public RestrictInWorkflowAccessRule()
	{
		this(AccessPermission.getHidden());
	}

	public RestrictInWorkflowAccessRule(AccessPermission restrictionType)
	{
		this.restrictionType = restrictionType;
	}

	// Always restrict in a workflow, but this allows you to override for more customized behavior
	protected boolean restrictInWorkflow(Adaptation adaptation, Session session)
	{
		return true;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}
		if (session.getInteraction(true) != null && restrictInWorkflow(adaptation, session))
		{
			return restrictionType;
		}
		return AccessPermission.getReadWrite();
	}

	public AccessPermission getRestrictionType()
	{
		return this.restrictionType;
	}

	public void setRestrictionType(AccessPermission restrictionType)
	{
		this.restrictionType = restrictionType;
	}
}
