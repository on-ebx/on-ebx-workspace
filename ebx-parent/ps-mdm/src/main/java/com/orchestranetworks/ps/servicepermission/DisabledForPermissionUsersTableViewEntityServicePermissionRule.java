package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ps.util.*;

/**
 * @deprecated Use {@link DisabledForPermissionUsersServicePermissionRules.OnTableView} instead
 */
@Deprecated
public class DisabledForPermissionUsersTableViewEntityServicePermissionRule
	extends
	DisabledForPermissionUsersServicePermissionRules.OnTableView
{
	public DisabledForPermissionUsersTableViewEntityServicePermissionRule(String[] permissionUsers)
	{
		super(permissionUsers);
	}

	public DisabledForPermissionUsersTableViewEntityServicePermissionRule(
		String[] permissionUsers,
		TrackingInfoHelper trackingInfoHelper)
	{
		super(permissionUsers, trackingInfoHelper);
	}
}
