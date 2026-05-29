package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.service.*;

/**
 * A simple Access Rule to specify a permission on a node while creating a new record.
 * Usually used to set a node (and its descendants) to {@code ReadOnly} or {@code Hidden} while creating a new record.    
 *
 */
public class SetPermissionOnAccessRuleForCreate implements AccessRuleForCreate
{
	AccessPermission permission = null;

	public SetPermissionOnAccessRuleForCreate(AccessPermission permission)
	{
		super();
		this.permission = permission;
	}

	@Override
	public AccessPermission getPermission(AccessRuleForCreateContext aContext)
	{
		return permission;
	}

}
