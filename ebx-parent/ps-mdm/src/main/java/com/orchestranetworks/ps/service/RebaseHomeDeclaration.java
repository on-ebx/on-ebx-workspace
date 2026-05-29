package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class RebaseHomeDeclaration
	extends
	GenericServiceDeclaration<DataspaceEntitySelection, ActivationContextOnDataspace>
	implements UserServiceDeclaration.OnDataspace

{

	public RebaseHomeDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("RebaseHome")
				: ServiceKey.forModuleServiceName(moduleName, "RebaseHome"),
			null,
			"Rebase Dataspace",
			"Merge the changes made to the parent branch into the child branch.",
			null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new RebaseHomeService<>();
	}

	@Override
	public void defineActivation(ActivationContextOnDataspace definition)
	{
		definition.setPermissionRule(new ChildDataSpaceOnlyServicePermissionRule<>());
	}

}
