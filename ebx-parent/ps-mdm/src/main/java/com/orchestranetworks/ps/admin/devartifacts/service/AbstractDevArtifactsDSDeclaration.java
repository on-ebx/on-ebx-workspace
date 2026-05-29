package com.orchestranetworks.ps.admin.devartifacts.service;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.userservice.declaration.*;

public abstract class AbstractDevArtifactsDSDeclaration extends TechAdminOnlyServiceDeclaration
{
	private Role authorizedRole = CommonConstants.TECH_ADMIN;

	protected AbstractDevArtifactsDSDeclaration(String moduleName, String serviceKey, String title)
	{
		super(
			moduleName == null ? ServiceKey.forName(serviceKey)
				: ServiceKey.forModuleServiceName(moduleName, serviceKey),
			null,
			title,
			null,
			null);
	}

	// Even though class extends TechAdminOnlyServiceDeclaration, we are actually allowing anyone in the authorized role,
	// which defaults to Tech Admin but can be overridden.
	// Still extending TechAdminOnlyServiceDeclaration to get its other standard behavior like adding to the group.
	@Override
	public void defineActivation(ActivationContextOnDataspace aContext)
	{
		aContext.setPermissionRule(new RoleOnlyServicePermissionRule<>(authorizedRole));
	}

	protected abstract DevArtifactsBase createImpl();

	protected abstract DevArtifactsConfigFactory createConfigFactory();

	protected abstract Map<String, String[]> createParamMap();

	public Role getAuthorizedRole()
	{
		return authorizedRole;
	}

	public void setAuthorizedRole(Role authorizedRole)
	{
		this.authorizedRole = authorizedRole;
	}
}
