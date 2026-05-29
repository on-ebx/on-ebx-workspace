package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class GenerateTableCompareReportDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView

{

	public GenerateTableCompareReportDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("GenerateTableCompareReport")
				: ServiceKey.forModuleServiceName(moduleName, "GenerateTableCompareReport"),
			null,
			"Generate Table Compare Report",
			"Download an excel workbook detailing the differences between this table and the one from the data space's initial snapshot",
			null);
	}

	@Override
	public void defineActivation(ActivationContextOnTableView definition)
	{
		definition.setPermissionRule(
			new CompoundServicePermissionRule<>()
				.appendRule(new TechAdminOnlyServicePermissionRules.OnDataSpace<>())
				.appendRule(new ChildDataSpaceOnlyServicePermissionRule<>()));
	}

	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new GenerateCompareReport<>();
	}

}
