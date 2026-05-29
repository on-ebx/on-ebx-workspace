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
public class PublishServiceDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView
{
	private static final String PUBLISH_SELECTED_RECORDS_SERVICE = "Publish selected records";
	private static final String PUBLISH_SELECTED_RECORDS_SERVICE_DESCRIPTION = "Publish selected record to a destination based on messaging configuration.";
	private static final String SERVICE_KEY = "PublishService";

	public PublishServiceDeclaration(String moduleName)
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
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new PublishSelectedRecordsService();
	}

}
