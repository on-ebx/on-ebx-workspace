package com.orchestranetworks.ps.module;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.service.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 * Extends the standard registration in order to replace the default dev artifacts services
 * with a multi-tenant one (for the admin tenant). This should only be used by an admin tenant.
 * A subclass of {@link PSModuleRegistrationListener} should be created, and an instance of this
 * class should be passed into its constructor.
 * 
 * Non-admin tenants should extend {@link PSMultiTenantModuleRegistrationListener} instead.
 * 
 * <code>serviceKeyPrefix</code> will be used to prefix the name of the Dev Artifacts services
 * for this tenant, followed by an underscore. For example, "product", would create the prefix
 * "product_".
 * 
 * <code>serviceLabelPrefix</code> will be used to prefix the label of the Dev Artifacts services,
 * as well as the Admin group, surrounded by braces and followed by a space. For example,
 * "Product" would create the prefix "[Product] ".
 * 
 * <code>systemPropertyPrefix</code> will be used to prefix the system property used to specify
 * the Dev Artifacts properties file, followed by a period. For example, "prod" would create
 * the prefix "prod.".
 */
public class PSMultiTenantAdminModuleRegistration extends PSModuleRegistration
{
	private String serviceKeyPrefix;
	private String serviceLabelPrefix;
	private String systemPropertyPrefix;

	public PSMultiTenantAdminModuleRegistration(
		String moduleName,
		String serviceKeyPrefix,
		String serviceLabelPrefix,
		String systemPropertyPrefix)
	{
		super(moduleName);
		this.serviceKeyPrefix = serviceKeyPrefix;
		this.serviceLabelPrefix = serviceLabelPrefix;
		this.systemPropertyPrefix = systemPropertyPrefix;

		String adminGroupLabelPrefix = "[" + serviceLabelPrefix + "] ";
		setAdminGroupName(serviceKeyPrefix + getAdminGroupName());
		setAdminGroupLabel(adminGroupLabelPrefix + getAdminGroupLabel());
		setAdminGroupDescription(adminGroupLabelPrefix + getAdminGroupDescription());
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void initServiceDeclarations(Map<String, UserServiceDeclaration> serviceDeclarations)
	{
		super.initServiceDeclarations(serviceDeclarations);

		// Remove the default services
		removeServiceDeclaration(
			serviceDeclarations,
			ExportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY);
		removeServiceDeclaration(
			serviceDeclarations,
			ImportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY);

		// Add the replacement services, that have been prefixed for this tenant
		String moduleLabelPrefix = "[" + serviceLabelPrefix + "] ";
		String systemProperty = systemPropertyPrefix + "."
			+ DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY;

		ExportDevArtifactsPropertiesFileDSDeclaration exportDevArtifactsServiceDeclaration = new ExportDevArtifactsPropertiesFileDSDeclaration(
			getModuleName(),
			serviceKeyPrefix + "_"
				+ ExportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY,
			moduleLabelPrefix + ExportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_TITLE);
		exportDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(systemProperty);
		addServiceDeclaration(serviceDeclarations, exportDevArtifactsServiceDeclaration);

		ImportDevArtifactsPropertiesFileDSDeclaration importDevArtifactsServiceDeclaration = new ImportDevArtifactsPropertiesFileDSDeclaration(
			getModuleName(),
			serviceKeyPrefix + "_"
				+ ImportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY,
			moduleLabelPrefix + ImportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_TITLE);
		importDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(systemProperty);
		addServiceDeclaration(serviceDeclarations, importDevArtifactsServiceDeclaration);
	}

	public String getServiceKeyPrefix()
	{
		return serviceKeyPrefix;
	}

	public String getServiceLabelPrefix()
	{
		return serviceLabelPrefix;
	}

	public String getSystemPropertyPrefix()
	{
		return systemPropertyPrefix;
	}
}
