package com.orchestranetworks.ps.requests;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class MassApproveOrRejectServiceDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView
{
	private final RequestPathConfig config;
	public MassApproveOrRejectServiceDeclaration(RequestPathConfig config)
	{
		super(
			ServiceKey.forName("MassApproveOrReject"),
			null,
			"Batch Accept/Reject Selected Requests",
			"For the selected requests, any that are offered or allocated to you can be batch-advanced or rejected",
			DEFAULT_CONFIRM,
			false);
		this.config = config;
	}

	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new MassApproveOrRejectService(config);
	}

	@Override
	public void defineActivation(ActivationContextOnTableView aContext)
	{
		super.defineActivation(aContext);
		aContext.includeSchemaNodesMatching(config.getRequestTablePath());
	}
}
