package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access rule that allows only users with the Tech Admin role to see a certain field with {@code ReadWrite} permission.
 */
public class TechAdminOnlyAccessRule implements AccessRule
{
	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (session.isUserInRole(CommonConstants.TECH_ADMIN))
		{
			return AccessPermission.getReadWrite();
		}
		else
		{
			return AccessPermission.getHidden();
		}
	}
}
