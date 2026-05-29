package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

/**
 * This class exists just to type it to {@link TableViewEntitySelection}. Otherwise, get an error casting.
 */
public class CompoundTableViewServicePermissionRule
	extends
	CompoundServicePermissionRule<TableViewEntitySelection>
{
	public CompoundTableViewServicePermissionRule()
	{
		super();
	}

	public CompoundTableViewServicePermissionRule(boolean minPermissions)
	{
		super(minPermissions);
	}
}
