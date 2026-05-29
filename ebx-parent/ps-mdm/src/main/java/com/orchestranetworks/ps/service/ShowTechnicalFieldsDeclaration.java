package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.ServiceKey;
import com.orchestranetworks.ui.selection.RecordEntitySelection;
import com.orchestranetworks.userservice.declaration.ActivationContextOnRecord;
import com.orchestranetworks.userservice.declaration.UserServiceDeclaration;

public class ShowTechnicalFieldsDeclaration extends
GenericServiceDeclaration<RecordEntitySelection, ActivationContextOnRecord>
implements UserServiceDeclaration.OnRecord

{

	public ShowTechnicalFieldsDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("ShowTechnicalFields")
				: ServiceKey.forModuleServiceName(moduleName, "ShowTechnicalFields"),
			ShowTechnicalFields.class,
			"Show Technical Fields",
			null,
			null);
	}

}