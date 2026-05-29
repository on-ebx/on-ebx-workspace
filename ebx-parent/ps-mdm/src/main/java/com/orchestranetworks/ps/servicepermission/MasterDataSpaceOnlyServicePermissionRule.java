package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

public class MasterDataSpaceOnlyServicePermissionRule<S extends DataspaceEntitySelection>
	extends
	MasterOrChildDataSpaceOnlyServicePermissionRule<S>
{
	public MasterDataSpaceOnlyServicePermissionRule()
	{
		allowInChild = false;
	}
}