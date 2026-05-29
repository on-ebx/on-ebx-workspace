package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class InitializeDefaultFieldValuesDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView

{

	public InitializeDefaultFieldValuesDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("InitializeDefaults")
				: ServiceKey.forModuleServiceName(moduleName, "InitializeDefaults"),
			null,
			"Initialize Defaults",
			"When new fields have been added to the data model that have a default value, initialize those.",
			null);
	}

	@Override
	public void defineActivation(ActivationContextOnTableView definition)
	{
		definition.setPermissionRule(
			new CompoundTableViewServicePermissionRule()
				.appendRule(new TechAdminOnlyServicePermissionRules.OnDataSpace<>())
				.appendRule(new WriteAccessRequiredServicePermissionRules.OnDataSpace<>()));
	}

	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new UserServiceNoUI<>(new InitializeDefaultFieldValues());
	}

}
