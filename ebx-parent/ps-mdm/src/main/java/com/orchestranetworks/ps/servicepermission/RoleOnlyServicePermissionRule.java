package com.orchestranetworks.ps.servicepermission;

import com.onwbp.base.text.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * A rule that enables a user service only for the specified role
 */
public class RoleOnlyServicePermissionRule<S extends DataspaceEntitySelection>
	implements ServicePermissionRule<S>
{
	private Role role;

	public RoleOnlyServicePermissionRule(Role role)
	{
		this.role = role;
	}

	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
	{
		// Note that the first time you set up the repository, you need to create the role
		if (context.getSession().isUserInRole(role))
		{
			return UserServicePermission.getEnabled();
		}
		return UserServicePermission.getDisabled(
			UserMessage.createError("User must be in role " + role.getRoleName() + "."));
	}
}