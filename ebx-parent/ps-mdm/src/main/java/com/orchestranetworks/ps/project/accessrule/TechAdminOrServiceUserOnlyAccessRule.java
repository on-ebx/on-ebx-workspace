package com.orchestranetworks.ps.project.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access rule that allows only tech-admins and service users to see a certain field.
 */
public class TechAdminOrServiceUserOnlyAccessRule implements AccessRule
{
	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (session.isUserInRole(CommonConstants.TECH_ADMIN)
			|| session.isUserInRole(CommonConstants.SERVICE_USER))
		{
			return AccessPermission.getReadWrite();
		}
		else
		{
			return AccessPermission.getHidden();
		}
	}
}
