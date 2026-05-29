package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.declaration.*;

public class GenerateDataDictionaryDeclaration
	extends
	GenericServiceDeclaration<DatasetEntitySelection, ActivationContextOnDataset>
	implements UserServiceDeclaration.OnDataset

{

	public GenerateDataDictionaryDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("GenerateDataDictionary")
				: ServiceKey.forModuleServiceName(moduleName, "GenerateDataDictionary"),
			GenerateDataDictionary.class,
			"Generate Data Dictionary",
			"Download an excel workbook describing the associated datamodel",
			null);
	}

	@Override
	public void defineActivation(ActivationContextOnDataset definition)
	{
		definition.setPermissionRule(new TechAdminOnlyServicePermissionRules.OnDataSpace<>());
	}

}
