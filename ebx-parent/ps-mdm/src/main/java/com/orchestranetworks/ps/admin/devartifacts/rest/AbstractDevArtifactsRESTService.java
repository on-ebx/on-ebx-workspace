package com.orchestranetworks.ps.admin.devartifacts.rest;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.rest.annotation.*;
import com.orchestranetworks.rest.inject.*;
import com.orchestranetworks.service.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;

/**
 * An abstract REST service for Dev Artifacts. It contains common logic between both imports and exports.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class AbstractDevArtifactsRESTService
{
	@Context
	private ResourceInfo resourceInfo;

	@Context
	private SessionContext sessionContext;

	@GET
	@Path("/description")
	@Documentation("Gets service description")
	@Consumes({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	@AnonymousAccessEnabled
	public String handleServiceDescription()
	{
		return resourceInfo.getResourceMethod().getAnnotation(Documentation.class).value();
	}

	protected abstract DevArtifactsBase createImpl();

	protected abstract DevArtifactsConfigFactory createConfigFactory();

	/**
	 * Initialize the parameter map that will be used when invoking Dev Artifacts.
	 * This is similar to what would occur in the UI user service before displaying the form.
	 * These are the parameters needed to create the configuration.
	 * 
	 * @param paramMap the parameter map
	 * @param dto the DTO to use when initializing the parameter map
	 */
	protected abstract void initParamMap(Map<String, String[]> paramMap, DevArtifactsDTO dto);

	/**
	 * Update the parameter map that will be used when invoking Dev Artifacts.
	 * This is similar to what would occur in the UI user service after submitting the form.
	 * These are the parameters needed to update the configuration.
	 * 
	 * @param paramMap the parameter map
	 * @param dto the DTO to use when updating the parameter map
	 */
	protected void updateParamMap(Map<String, String[]> paramMap, DevArtifactsDTO dto)
	{
		Boolean environmentCopy = dto.getEnvironmentCopy();
		if (environmentCopy != null)
		{
			paramMap.put(
				DevArtifactsBase.PARAM_ENVIRONMENT_COPY,
				new String[] { environmentCopy.toString() });
		}
	}

	protected void doProcessArtifacts(
		DevArtifactsBase impl,
		DevArtifactsConfig config,
		Repository repo,
		Session session)
		throws OperationException
	{
		if (config.isEnvironmentCopy())
		{
			impl.processArtifacts(repo, session, true);
		}
		// Note that for imports, this will do nothing when env copy is true,
		// but for exports, we want to additionally export the normal artifacts
		impl.processArtifacts(repo, session, false);

		impl.postProcess(repo, session);

		if (impl.getErrorCount() > 0)
		{
			throw OperationException.createError(impl.getDevArtifactsOutcome());
		}
	}

	protected Response processArtifacts(DevArtifactsDTO dto)
	{
		Session session = sessionContext.getSession();
		Repository repo = sessionContext.getRepository();
		DevArtifactsConfigFactory configFactory = createConfigFactory();
		Map<String, String[]> paramMap = new HashMap<>();
		initParamMap(paramMap, dto);

		DevArtifactsConfig config;
		try
		{
			config = configFactory.createConfig(repo, session, paramMap);
		}
		catch (OperationException ex)
		{
			String msg = "Error creating Dev Artifacts configuration.";
			DevArtifactsUtil.getLog().error(msg, ex);
			throw new InternalServerErrorException(msg + " " + ex.getMessage(), ex);
		}

		updateParamMap(paramMap, dto);
		try
		{
			configFactory.updateConfig(config, repo, session, paramMap);
		}
		catch (OperationException ex)
		{
			String msg = "Error updating Dev Artifacts configuration.";
			DevArtifactsUtil.getLog().error(msg, ex);
			throw new InternalServerErrorException(msg + " " + ex.getMessage(), ex);
		}
		DevArtifactsBase impl = createImpl();
		impl.setConfig(config);
		try
		{
			doProcessArtifacts(impl, config, repo, session);
			DevArtifactsUtil.getLog().info("**** Dev Artifacts processing complete.");
		}
		catch (OperationException ex)
		{
			String msg = "Error processing artifacts.";
			DevArtifactsUtil.getLog().error(msg, ex);
			throw new InternalServerErrorException(msg + " " + ex.getMessage(), ex);
		}
		// Return an ok status
		return Response.ok().build();
	}
}