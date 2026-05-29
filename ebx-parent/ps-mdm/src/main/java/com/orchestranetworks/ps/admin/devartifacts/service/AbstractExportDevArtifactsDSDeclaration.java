package com.orchestranetworks.ps.admin.devartifacts.service;

import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * An abstract service declaration for Export Dev Artifacts
 */
public abstract class AbstractExportDevArtifactsDSDeclaration
	extends
	AbstractDevArtifactsDSDeclaration
{
	public static final String DEFAULT_SERVICE_KEY = "ExportDevArtifactsDS";
	public static final String DEFAULT_TITLE = "Export Dev Artifacts";

	protected AbstractExportDevArtifactsDSDeclaration(String moduleName)
	{
		this(moduleName, DEFAULT_SERVICE_KEY, DEFAULT_TITLE);
	}

	protected AbstractExportDevArtifactsDSDeclaration(
		String moduleName,
		String serviceKey,
		String title)
	{
		super(moduleName, serviceKey, title);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new ExportDevArtifactsUserService<>(
			(ExportDevArtifactsImpl) createImpl(),
			createConfigFactory(),
			createParamMap());
	}

	@Override
	protected DevArtifactsBase createImpl()
	{
		return new ExportDevArtifactsImpl();
	}
}
