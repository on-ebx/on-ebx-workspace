package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class EncryptionServiceDeclaration extends TechAdminOnlyServiceDeclaration
{
	public static final String SERVICE_KEY = "EncryptionService";

	public EncryptionServiceDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName(SERVICE_KEY)
				: ServiceKey.forModuleServiceName(moduleName, SERVICE_KEY),
			null,
			"Encryption Service",
			"Gets a string and generates its encrypted form",
			null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new EncryptionService<>();
	}
}