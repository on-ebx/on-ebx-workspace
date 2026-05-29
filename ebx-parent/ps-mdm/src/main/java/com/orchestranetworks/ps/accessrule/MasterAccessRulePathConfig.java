/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.schema.*;

/**
 * Specifies the paths for the MasterAccessRule
 */
public abstract class MasterAccessRulePathConfig
{
	protected abstract Path getAccessPermissionPath();
	protected abstract Path getAccessPermissionDataSpacePath();
	protected abstract Path getAccessPermissionDataSetPath();
	protected abstract Path getAccessPermissionTablePath();
	protected abstract Path getAccessPermissionConditionPath();
	protected abstract Path getAccessPermissionRolePath();
	protected abstract Path getAccessPermissionPermissionPath();
}
