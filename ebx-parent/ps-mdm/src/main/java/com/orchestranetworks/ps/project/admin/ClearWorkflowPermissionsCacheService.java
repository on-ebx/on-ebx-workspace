/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.admin;

import com.orchestranetworks.ps.project.actionpermissions.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * A service that clears the cache of workflow permissions.
 */
public class ClearWorkflowPermissionsCacheService
	extends
	AbstractUserService<DataspaceEntitySelection>
{
	@Override
	public void execute(Session session) throws OperationException
	{
		// Make sure only admins can execute
		if (!session.isUserInRole(Role.ADMINISTRATOR))
		{
			throw OperationException
				.createError("User doesn't have permission to execute service.");
		}

		ProjectWorkflowPermissionsCache.getInstance().clearAllCache();
	}
}
