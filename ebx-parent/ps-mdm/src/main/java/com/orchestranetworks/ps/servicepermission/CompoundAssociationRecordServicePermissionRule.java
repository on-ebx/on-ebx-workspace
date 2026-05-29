package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

/**
 * This class exists just to type it to {@link AssociationRecordEntitySelection}. Otherwise, get an error casting.
 */
public class CompoundAssociationRecordServicePermissionRule
	extends
	CompoundServicePermissionRule<AssociationRecordEntitySelection>
{
	public CompoundAssociationRecordServicePermissionRule()
	{
		super();
	}

	public CompoundAssociationRecordServicePermissionRule(boolean minPermissions)
	{
		super(minPermissions);
	}
}
