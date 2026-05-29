package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class GenerateDataSpaceCompareReportDeclaration
	extends
	GenericServiceDeclaration<DataspaceEntitySelection, ActivationContextOnDataspace>
	implements UserServiceDeclaration.OnDataspace

{

	public GenerateDataSpaceCompareReportDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("GenerateDataSpaceCompareReport")
				: ServiceKey.forModuleServiceName(moduleName, "GenerateDataSpaceCompareReport"),
			null,
			"Generate Dataspace Compare Report",
			"Download an excel workbook detailing the differences between this data space and its initial snapshot",
			null);
	}

	@Override
	public void defineActivation(ActivationContextOnDataspace definition)
	{
		definition.setPermissionRule(new ChildDataSpaceOnlyServicePermissionRule<>());
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new GenerateCompareReport<>();
	}

}
