package com.ebx.mdm.common.module;

import java.util.Map;

import com.orchestranetworks.ps.admin.devartifacts.config.DevArtifactsPropertyFileHelper;
import com.orchestranetworks.ps.admin.devartifacts.service.AbstractExportDevArtifactsDSDeclaration;
import com.orchestranetworks.ps.admin.devartifacts.service.AbstractImportDevArtifactsDSDeclaration;
import com.orchestranetworks.ps.admin.devartifacts.service.ExportDevArtifactsPropertiesFileDSDeclaration;
import com.orchestranetworks.ps.admin.devartifacts.service.ImportDevArtifactsPropertiesFileDSDeclaration;
import com.orchestranetworks.ps.module.PSModuleRegistration;
import com.orchestranetworks.userservice.declaration.UserServiceDeclaration;

public class EBXModuleRegistration extends PSModuleRegistration
{
	private static final String MODULE_KEY_PREFIX = "ebx_";
	private static final String MODULE_LABEL_PREFIX = "[" + EBXModuleRegistrationListener.MODULE_NAME + "] ";
	protected static final String MODULE_DEV_ARTIFACTS_SYSTEM_PROPERTY = "ebx_" + DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY;

	protected String moduleName = "onebx-mdm";
	
	public EBXModuleRegistration()
	{
		super(EBXModuleRegistrationListener.MODULE_NAME);
		setAdminGroupName(MODULE_KEY_PREFIX + PSModuleRegistration.ADMIN_GROUP);
		setAdminGroupLabel(MODULE_LABEL_PREFIX + PSModuleRegistration.ADMIN_GROUP);
		setAdminGroupDescription(MODULE_LABEL_PREFIX + PSModuleRegistration.ADMIN_GROUP_DESCRIPTION);
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void initServiceDeclarations(Map<String, UserServiceDeclaration> serviceDeclarations)
	{
		System.out.println("EBXModuleRegistration initServiceDeclarations begins... ");
		
		super.initServiceDeclarations(serviceDeclarations);

		removeServiceDeclaration(serviceDeclarations, ExportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY);
		removeServiceDeclaration(serviceDeclarations, ImportDevArtifactsPropertiesFileDSDeclaration.DEFAULT_SERVICE_KEY);

		ExportDevArtifactsPropertiesFileDSDeclaration exportDevArtifactsServiceDeclaration = new ExportDevArtifactsPropertiesFileDSDeclaration(
			EBXModuleRegistrationListener.MODULE_NAME,
			MODULE_KEY_PREFIX + AbstractExportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY,
			MODULE_LABEL_PREFIX + AbstractExportDevArtifactsDSDeclaration.DEFAULT_TITLE);
		exportDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(MODULE_DEV_ARTIFACTS_SYSTEM_PROPERTY);
		addServiceDeclaration(serviceDeclarations, exportDevArtifactsServiceDeclaration);

		ImportDevArtifactsPropertiesFileDSDeclaration importDevArtifactsServiceDeclaration = new ImportDevArtifactsPropertiesFileDSDeclaration(
			EBXModuleRegistrationListener.MODULE_NAME,
			MODULE_KEY_PREFIX + AbstractImportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY,
			MODULE_LABEL_PREFIX + AbstractImportDevArtifactsDSDeclaration.DEFAULT_TITLE);
		importDevArtifactsServiceDeclaration.setPropertiesFileSystemProperty(MODULE_DEV_ARTIFACTS_SYSTEM_PROPERTY);
		addServiceDeclaration(serviceDeclarations, importDevArtifactsServiceDeclaration);

		System.out.println("EBXModuleRegistration initServiceDeclarations ends... ");
	}

}
