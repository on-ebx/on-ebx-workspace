package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * 
 * Service declaration for the User Service to clear the PermissionsUser cache {@link ClearPermissionsUserCacheUserService}.
 *
 */
public class ClearPermissionsUserCacheServiceDeclaration extends TechAdminOnlyServiceDeclaration
{

	public ClearPermissionsUserCacheServiceDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("ClearPermissionsUserCache")
				: ServiceKey.forModuleServiceName(moduleName, "ClearPermissionsUserCache"),
			null,
			"Clear Permissions User Cache",
			null,
			"");
	}

	/**
	 * Creates a new user service implementation.
	 * This method is invoked every time the declared user service must be launched, once all activation controls have been performed.
	 */
	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new UserServiceNoUI<>(new ClearPermissionsUserCacheUserService());
	}

}
