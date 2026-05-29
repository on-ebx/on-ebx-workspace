package com.orchestranetworks.ps.admin.devartifacts;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;

/**
 * A servlet for importing of the dev artifacts.
 * This is no longer used by the User Service but it still needed for the command line interface.
 * 
 * @deprecated Use the REST service or user service instead. See {@link com.orchestranetworks.ps.admin.devartifacts.rest.ImportDevArtifactsPropertiesFileRESTService}
 *             or {@link com.orchestranetworks.ps.admin.devartifacts.service.ImportDevArtifactsUserService}.
 */
@Deprecated
public class ImportDevArtifactsService extends DevArtifactsService
{
	private static final long serialVersionUID = 1L;

	@Override
	protected DevArtifactsBase createImpl()
	{
		return new ImportDevArtifactsImpl();
	}

	@Override
	protected DevArtifactsConfigFactory createConfigFactory()
	{
		return new ImportDevArtifactsPropertiesFileConfigFactory();
	}
}
