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
@Path(value = "/export")
@Documentation("Export")
public class ExportDevArtifactsPropertiesFileRESTService
	extends
	AbstractExportDevArtifactsRESTService
{
	@Override
	protected DevArtifactsConfigFactory createConfigFactory()
	{
		return new ExportDevArtifactsPropertiesFileConfigFactory();
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
	@Documentation("Execute the Export")
	public Response handleExecute(DevArtifactsPropertiesFileDTO dto)
	{
		return super.processArtifacts(dto);
	}

	@GET
	@Authorization(TechAdminOnlyAuthorizationRule.class)
	@Path("/executeDefault")
	@Documentation("Execute the Export with default options")
	public Response handleExecuteDefault()
	{
		return super.processArtifacts(new DevArtifactsPropertiesFileDTO());
	}
}