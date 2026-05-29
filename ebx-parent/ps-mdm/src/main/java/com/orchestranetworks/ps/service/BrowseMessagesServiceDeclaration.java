package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 * @author David Dahan
 * 
 * UI service that allow a user to select rows in any table and publish the selected rows as a json messages in proprietary format.
 * extend this class and override the method "defineActivation" to control the access to this service.
 *
 */
public class BrowseMessagesServiceDeclaration
	extends
	GenericServiceDeclaration<DataspaceEntitySelection, ActivationContextOnDataspace>
	implements UserServiceDeclaration.OnDataspace
{
	private static final String PUBLISH_SELECTED_RECORDS_SERVICE = "Browse Messages";
	private static final String PUBLISH_SELECTED_RECORDS_SERVICE_DESCRIPTION = "Browse messages from the current queue configuration.";
	private static final String SERVICE_KEY = "BrowseMessagesService";

	public BrowseMessagesServiceDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName(SERVICE_KEY)
				: ServiceKey.forModuleServiceName(moduleName, SERVICE_KEY),
			null,
			PUBLISH_SELECTED_RECORDS_SERVICE,
			PUBLISH_SELECTED_RECORDS_SERVICE_DESCRIPTION,
			null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new BrowseMessagesService();
	}

	@Override
	public void defineActivation(ActivationContextOnDataspace aContext)
	{
		// TODO Auto-generated method stub
		super.defineActivation(aContext);
	}

}
