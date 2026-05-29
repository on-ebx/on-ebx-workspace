package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ps.util.*;

/**
 * @deprecated Use {@link DisabledForPermissionUsersServicePermissionRules.OnRecord} instead
 */
@Deprecated
public class DisabledForPermissionUsersRecordEntityServicePermissionRule
	extends
	DisabledForPermissionUsersServicePermissionRules.OnRecord
{
	public DisabledForPermissionUsersRecordEntityServicePermissionRule(String[] permissionUsers)
	{
		super(permissionUsers);
	}

	public DisabledForPermissionUsersRecordEntityServicePermissionRule(
		String[] permissionUsers,
		TrackingInfoHelper trackingInfoHelper)
	{
		super(permissionUsers, trackingInfoHelper);
	}
}
