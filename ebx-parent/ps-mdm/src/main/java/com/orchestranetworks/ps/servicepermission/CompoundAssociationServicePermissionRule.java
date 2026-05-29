package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

/**
 * This class exists just to type it to {@link AssociationEntitySelection}. Otherwise, get an error casting.
 */
public class CompoundAssociationServicePermissionRule
	extends
	CompoundServicePermissionRule<AssociationEntitySelection>
{
	public CompoundAssociationServicePermissionRule()
	{
		super();
	}

	public CompoundAssociationServicePermissionRule(boolean minPermissions)
	{
		super(minPermissions);
	}
}
