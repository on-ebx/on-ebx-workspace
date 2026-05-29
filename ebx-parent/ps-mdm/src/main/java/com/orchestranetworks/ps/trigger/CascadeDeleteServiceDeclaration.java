package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Declares the {@link CascadeDeleteService}.
 * The service should be put on tables that have <code>enableCascadeDelete</code> set to <code>true</code>
 * on their triggers (see {@link BaseTableTriggerEnforcingPermissions}). You pass the table paths to put this on into the constructor.
 * This table would be registered on the schema in the schema extensions class.
 */
public class CascadeDeleteServiceDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView
{
	private final Path[] tablePaths;

	/**
	 * Create the service declaration
	 * 
	 * @param tablePaths the table paths to enable the cascade delete service for
	 */
	public CascadeDeleteServiceDeclaration(Path... tablePaths)
	{
		super(ServiceKey.forName("CascadeDelete"), null, "Cascade Delete", null, null);
		this.tablePaths = tablePaths;
	}

	@Override
	public void defineActivation(ActivationContextOnTableView aContext)
	{
		super.defineActivation(aContext);
		aContext.setDefaultPermission(UserServicePermission.getDisabled());
		aContext.includeSchemaNodesMatching(tablePaths);
		aContext.forbidEmptyRecordSelection();
	}

	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new CascadeDeleteService<>();
	}
}
