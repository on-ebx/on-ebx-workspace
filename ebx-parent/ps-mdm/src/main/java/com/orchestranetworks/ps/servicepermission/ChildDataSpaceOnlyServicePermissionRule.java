package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

public class ChildDataSpaceOnlyServicePermissionRule<S extends DataspaceEntitySelection>
	extends
	MasterOrChildDataSpaceOnlyServicePermissionRule<S>
{
	public ChildDataSpaceOnlyServicePermissionRule()
	{
		allowInMaster = false;
	}
}