package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ui.selection.*;

/**
 * @deprecated Use {@link DisabledForPermissionUsersServicePermissionRules.OnTable} instead
 */
@Deprecated
public abstract class AbstractDisabledForPermissionUsersServicePermissionRule
	extends
	DisabledForPermissionUsersServicePermissionRules.OnTable<TableEntitySelection>
{
	protected AbstractDisabledForPermissionUsersServicePermissionRule(String[] permissionUsers)
	{
		super(permissionUsers);
	}

	protected AbstractDisabledForPermissionUsersServicePermissionRule(
		String[] permissionUsers,
		TrackingInfoHelper trackingInfoHelper)
	{
		super(permissionUsers, trackingInfoHelper);
	}
}
