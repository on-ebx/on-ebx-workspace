package com.orchestranetworks.ps.module;

import com.onwbp.base.text.*;
import com.orchestranetworks.module.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.service.*;
import com.orchestranetworks.service.*;

/**
 * A registration listener that defines the Dev Artifacts services for a non-admin multi-tenant module.
 * The admin tenant should extend {@link PSModuleRegistrationListener} and pass an instance of
 * {@link PSMultiTenantAdminModuleRegistration} into the constructor.
 * The non-admin tenants can't extend {@link PSModuleRegistrationListener} because that also defines other
 * services besides the Dev Artifacts.
 * 
 * See {@link PSMultiTenantAdminModuleRegistration} for a description of the parameters, which are the same
 * as this class.
 */
public abstract class PSMultiTenantModuleRegistrationListener extends ModuleRegistrationListener
{
	private String moduleName;
	private String serviceKeyPrefix;
	private String serviceLabelPrefix;
	private String systemPropertyPrefix;

	protected PSMultiTenantModuleRegistrationListener(
		String moduleName,
		String serviceKeyPrefix,
		String serviceLabelPrefix,
		String systemPropertyPrefix)
	{
		this.moduleName = moduleName;
		this.serviceKeyPrefix = serviceKeyPrefix;
		this.serviceLabelPrefix = serviceLabelPrefix;
		this.systemPropertyPrefix = systemPropertyPrefix;
	}

	@Override
	public void handleServiceRegistration(ModuleServiceRegistrationContext context)
	{
		super.handleServiceRegistration(context);

		// Define the admin group
		String moduleAdminGroup = serviceKeyPrefix + "_" + PSModuleRegistration.ADMIN_GROUP;
		String moduleLabelPrefix = "[" + serviceLabelPrefix + "] ";
		String systemProperty = systemPropertyPrefix + "."
			+ DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY;

		context.registerServiceGroup(
			ServiceGroupKey.forServiceGroupInModule(moduleName, moduleAdminGroup),
			UserMessage.createInfo(moduleLabelPrefix + PSModuleRegistration.ADMIN_GROUP),
			UserMessage
				.createInfo(moduleLabelPrefix + PSModuleRegistration.ADMIN_GROUP_DESCRIPTION));

		// Define the export and import services
		ExportDevArtifactsPropertiesFileDSDeclaration exportDevArtifactsServiceDeclaration = new ExportDevArtifactsPropertiesFileDSDeclaration(
			moduleName,
			serviceKeyPrefix + "_" + AbstractExportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY,
			moduleLabelPrefix + AbstractExportDevArtifactsDSDeclaration.DEFAULT_TITLE);
		exportDevArtifactsServiceDeclaration.setServiceGroupName(moduleAdminGroup);
		exportDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(systemProperty);
		context.registerUserService(exportDevArtifactsServiceDeclaration);

		ImportDevArtifactsPropertiesFileDSDeclaration importDevArtifactsServiceDeclaration = new ImportDevArtifactsPropertiesFileDSDeclaration(
			moduleName,
			serviceKeyPrefix + "_" + AbstractImportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY,
			moduleLabelPrefix + AbstractImportDevArtifactsDSDeclaration.DEFAULT_TITLE);
		importDevArtifactsServiceDeclaration.setServiceGroupName(moduleAdminGroup);
		importDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(systemProperty);
		context.registerUserService(importDevArtifactsServiceDeclaration);
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