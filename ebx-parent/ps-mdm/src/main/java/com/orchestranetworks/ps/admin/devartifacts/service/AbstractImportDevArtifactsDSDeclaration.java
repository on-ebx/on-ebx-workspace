package com.orchestranetworks.ps.admin.devartifacts.service;

import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * An abstract service declaration for Import Dev Artifacts
 */
public abstract class AbstractImportDevArtifactsDSDeclaration
	extends
	AbstractDevArtifactsDSDeclaration
{
	public static final String DEFAULT_SERVICE_KEY = "ImportDevArtifactsDS";
	public static final String DEFAULT_TITLE = "Import Dev Artifacts";

	protected AbstractImportDevArtifactsDSDeclaration(String moduleName)
	{
		this(moduleName, DEFAULT_SERVICE_KEY, DEFAULT_TITLE);
	}

	protected AbstractImportDevArtifactsDSDeclaration(
		String moduleName,
		String serviceKey,
		String title)
	{
		super(moduleName, serviceKey, title);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new ImportDevArtifactsUserService<>(
			(ImportDevArtifactsImpl) createImpl(),
			createConfigFactory(),
			createParamMap());
	}

	@Override
	protected DevArtifactsBase createImpl()
	{
		return new ImportDevArtifactsImpl();
	}
}
