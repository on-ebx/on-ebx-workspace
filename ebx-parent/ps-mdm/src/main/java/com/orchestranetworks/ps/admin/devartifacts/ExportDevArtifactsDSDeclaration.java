package com.orchestranetworks.ps.admin.devartifacts;

import com.orchestranetworks.ps.admin.devartifacts.service.*;

/**
 * @deprecated Use {@link ExportDevArtifactsPropertiesFileDSDeclaration} instead
 */
@Deprecated
public class ExportDevArtifactsDSDeclaration extends ExportDevArtifactsPropertiesFileDSDeclaration
{
	/**
	 * @deprecated Use {@link AbstractExportDevArtifactsDSDeclaration#DEFAULT_SERVICE_KEY} instead
	 */
	@Deprecated
	public static final String SERVICE_KEY = AbstractExportDevArtifactsDSDeclaration.DEFAULT_SERVICE_KEY;

	public ExportDevArtifactsDSDeclaration(String moduleName)
	{
		super(moduleName);
	}
}
