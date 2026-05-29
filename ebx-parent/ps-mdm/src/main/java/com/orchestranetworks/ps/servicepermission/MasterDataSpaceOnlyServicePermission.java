package com.orchestranetworks.ps.servicepermission;

public class MasterDataSpaceOnlyServicePermission
	extends
	MasterOrChildDataSpaceOnlyServicePermission
{
	public MasterDataSpaceOnlyServicePermission()
	{
		allowInChild = false;
	}
}