package com.orchestranetworks.ps.admin.devartifacts;

import com.orchestranetworks.ps.admin.devartifacts.service.*;

/**
 * @deprecated Use {@link ImportDevArtifactsPropertiesFileDSDeclaration} instead
 */
@Deprecated
public class ImportDevArtifactsDSDeclaration extends ImportDevArtifactsPropertiesFileDSDeclaration
{
	/**
	 * @deprecated Use {@link AbstractImportDevArtifactsDSDeclaration#DEFAULT_SERVICE_KEY} instead
	 */
	@Deprecated
	public static final String SERVICE_KEY = AbstractImportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY;

	public ImportDevArtifactsDSDeclaration(String moduleName)
	{
		super(moduleName);
	}
}
