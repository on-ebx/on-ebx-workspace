/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to hide a field within a create user task.
 * 
 * Note: that this does not restrict during record creation in general,
 * just during a create user task.
 */
public class HideOnCreationAccessRule implements AccessRule
{
	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}
		SessionInteraction sessionInter = session.getInteraction(true);
		if (sessionInter != null && ServiceKey.CREATE.equals(sessionInter.getServiceKey()))
			return AccessPermission.getHidden();
		return AccessPermission.getReadWrite();
	}
}
