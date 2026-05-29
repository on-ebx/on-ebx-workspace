package com.orchestranetworks.ps.admin.devartifacts.rest;

import com.orchestranetworks.ps.admin.devartifacts.impl.*;

public abstract class AbstractExportDevArtifactsRESTService extends AbstractDevArtifactsRESTService
{
	@Override
	protected DevArtifactsBase createImpl()
	{
		return new ExportDevArtifactsImpl();
	}
}