/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.admin;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 */
public class ClearWorkflowPermissionsCacheDeclaration extends TechAdminOnlyServiceDeclaration
implements UserServiceDeclaration.OnDataspace
{
	public static final String SERVICE_NAME = "ClearWorkflowPermissionsCache";

	public ClearWorkflowPermissionsCacheDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName(SERVICE_NAME)
				: ServiceKey.forModuleServiceName(moduleName, SERVICE_NAME),
				null,
				"Clear Workflow Permissions Cache",
				null,
				null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new ClearWorkflowPermissionsCacheService();
	}
}
