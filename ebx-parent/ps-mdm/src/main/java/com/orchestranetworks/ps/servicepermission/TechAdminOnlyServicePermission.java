package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class TechAdminOnlyServicePermission implements ServicePermission
{

	@Override
	public ActionPermission getPermission(
		SchemaNode schemaNode,
		Adaptation adaptation,
		Session session)
	{

		if (session.isUserInRole(CommonConstants.TECH_ADMIN))
		{
			return ActionPermission.getEnabled();
		}
		else
		{
			return ActionPermission.getHidden();
		}

	}
}