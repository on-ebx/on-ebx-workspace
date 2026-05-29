package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.module.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class ConfigurationOnlyServiceDeclaration
	extends
	GenericServiceDeclaration<DataspaceEntitySelection, ActivationContextOnDataspace>
	implements UserServiceDeclaration.OnDataspace
{
	public ConfigurationOnlyServiceDeclaration(
		ServiceKey aServiceKey,
		Class<? extends UserService<DataspaceEntitySelection>> implementation,
		String aTitle,
		String aDescription,
		String anInstruction)
	{
		super(aServiceKey, implementation, aTitle, aDescription, anInstruction);
	}

	@Override
	public void defineActivation(ActivationContextOnDataspace aContext)
	{
		aContext.setPermissionRule(new TechAdminOnlyServicePermissionRules.OnDataSpace<>());
	}

	@Override
	public void defineProperties(UserServicePropertiesDefinitionContext aDefinition)
	{
		super.defineProperties(aDefinition);
		aDefinition.setGroup(
			ServiceGroupKey.forServiceGroupInModule(
				getServiceKey().getModuleName(),
				PSModuleRegistration.CONFIGURATION_GROUP));
	}

}
