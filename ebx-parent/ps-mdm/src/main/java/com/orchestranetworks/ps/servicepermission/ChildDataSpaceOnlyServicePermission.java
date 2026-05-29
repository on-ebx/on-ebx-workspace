package com.orchestranetworks.ps.servicepermission;

public class ChildDataSpaceOnlyServicePermission
	extends
	MasterOrChildDataSpaceOnlyServicePermission
{
	public ChildDataSpaceOnlyServicePermission()
	{
		allowInMaster = false;
	}
}