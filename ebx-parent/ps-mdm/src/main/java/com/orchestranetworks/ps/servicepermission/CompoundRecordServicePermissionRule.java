package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

/**
 * This class exists just to type it to {@link RecordEntitySelection}. Otherwise, get an error casting.
 */
public class CompoundRecordServicePermissionRule
	extends
	CompoundServicePermissionRule<RecordEntitySelection>
{
	public CompoundRecordServicePermissionRule()
	{
		super();
	}

	public CompoundRecordServicePermissionRule(boolean minPermissions)
	{
		super(minPermissions);
	}
}
