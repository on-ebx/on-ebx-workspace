package com.orchestranetworks.ps.admin.devartifacts.rest;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.ps.rest.authorizationrule.*;
import com.orchestranetworks.rest.annotation.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path(value = "/import")
@Documentation("Import")
public class ImportDevArtifactsPropertiesFileRESTService
	extends
	AbstractImportDevArtifactsRESTService
{
	@Override
	protected DevArtifactsConfigFactory createConfigFactory()
	{
		return new ImportDevArtifactsPropertiesFileConfigFactory();
	}

	@Override
	protected void initParamMap(Map<String, String[]> paramMap, DevArtifactsDTO dto)
	{
		String propertiesFileSystemProperty = ((DevArtifactsPropertiesFileDTO) dto)
			.getPropertiesFileSystemProperty();
		DevArtifactsPropertyFileHelper
			.updateParametersWithPropertiesFile(paramMap, propertiesFileSystemProperty);
	}

	@POST
	@Authorization(TechAdminOnlyAuthorizationRule.class)
	@Path("/execute")
	@Documentation("Execute the Import")
	public Response handleExecute(DevArtifactsPropertiesFileDTO dto)
	{
		return super.processArtifacts(dto);
	}

	@GET
	@Authorization(TechAdminOnlyAuthorizationRule.class)
	@Path("/executeDefault")
	@Documentation("Execute the Import with default options")
	public Response handleExecuteDefault()
	{
		return super.processArtifacts(new DevArtifactsPropertiesFileDTO());
	}
}