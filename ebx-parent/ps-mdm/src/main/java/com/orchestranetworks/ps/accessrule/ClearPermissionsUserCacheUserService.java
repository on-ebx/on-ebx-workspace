/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * User Service to clear the PermissionsUser cache. This replaces the {@link ClearPermissionsUserCacheService}. 
 *
 */
public class ClearPermissionsUserCacheUserService
	extends
	AbstractUserService<DataspaceEntitySelection>
{

	/**
	 * Executes the user service.
	 */
	@Override
	public void execute(Session session) throws OperationException
	{
		DefaultPermissionsUserManager.getInstance().clearCache();
	}
}
