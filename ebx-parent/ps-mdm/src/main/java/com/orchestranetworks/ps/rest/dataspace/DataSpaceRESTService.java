package com.orchestranetworks.ps.rest.dataspace;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.rest.annotation.*;
import com.orchestranetworks.rest.inject.*;
import com.orchestranetworks.service.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;

/**
 * Provides DataSpace related REST Services developed using the APIs offered by the REST Toolkit.
 * 
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path(value = "/dataspace")
@Documentation("Dataspace REST Service")
public class DataSpaceRESTService
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
	// TODO: Should this be an anonymous service?
	public String handleServiceDescription()
	{
		// TODO: Not sure why we're doing this
		return this.resourceInfo.getResourceMethod().getAnnotation(Documentation.class).value();
	}

	/**
	 * Creates a child dataspace for the given parent dataspace.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/dataspace/dataspace/createChildDataSpace/<parentDataSpaceName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/dataspace/dataspace/createChildDataSpace/CommonReferenceMasterDataSpace
	 * }
	 * </pre>
	 * @param parentDataSpaceName the name of the parent dataspace.
	 * @return the {@link DataSpaceDTO} with the dataSpaceId and dataSpaceLabel.
	 */
	@GET
	@Path("/createChildDataSpace/{parentDataSpaceName}")
	@Documentation("Creates a child dataspace.")
	public DataSpaceDTO handleCreateChildDataSpace(
		@PathParam("parentDataSpaceName") String parentDataSpaceName)
	{
		return handleCreateChildDataSpace(parentDataSpaceName, null, null, null);
	}

	/**
	 * Creates a child dataspace for the given parent dataspace with the given permissions template.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/dataspace/dataspace/createChildDataSpace/<parentDataSpaceName>/<permissionsTemplateDataSpaceName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/dataspace/dataspace/createChildDataSpace/CommonReferenceMasterDataSpace/COMM_WorkflowPermissions
	 * }
	 * </pre>
	 * @param parentDataSpaceName the name of the parent dataspace.
	 * @param permissionsTemplateDataSpaceName the name of the permissions template dataspace
	 * @return the {@link DataSpaceDTO} with the dataSpaceId and dataSpaceLabel.
	 */
	@GET
	@Path("/createChildDataSpace/{parentDataSpaceName}/{permissionsTemplateDataSpaceName}")
	@Documentation("Creates a child dataspace.")
	public DataSpaceDTO handleCreateChildDataSpace(
		@PathParam("parentDataSpaceName") String parentDataSpaceName,
		@PathParam("permissionsTemplateDataSpaceName") String permissionsTemplateDataSpaceName)
	{
		return handleCreateChildDataSpace(
			parentDataSpaceName,
			null,
			null,
			permissionsTemplateDataSpaceName);
	}

	/**
	 * Creates a child dataspace for the given parent dataspace with the given name prefix, label prefix and the permissions template.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/dataspace/dataspace/createChildDataSpace/<parentDataSpaceName>/<childNamePrefix>/<childLabelPrefix>/<permissionsTemplateDataSpaceName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/dataspace/dataspace/createChildDataSpace/CommonReferenceMasterDataSpace/TEST/Test/COMM_WorkflowPermissions
	 * }
	 * </pre>
	 * @param parentDataSpaceName the name of the parent dataspace.
	 * @param childNamePrefix the prefix for the child dataspace name.
	 * @param childLabelPrefix the prefix for the child dataspace label.
	 * @param permissionsTemplateDataSpaceName the name of the permissions template dataspace.
	 * @return the {@link DataSpaceDTO} with the dataSpaceId and dataSpaceLabel.
	 */
	@GET
	@Path("/createChildDataSpace/{parentDataSpaceName}/{childNamePrefix}/{childLabelPrefix}/{permissionsTemplateDataSpaceName}")
	@Documentation("Creates a child dataspace.")
	public DataSpaceDTO handleCreateChildDataSpace(
		@PathParam("parentDataSpaceName") String parentDataSpaceName,
		@PathParam("childNamePrefix") String childNamePrefix,
		@PathParam("childLabelPrefix") String childLabelPrefix,
		@PathParam("permissionsTemplateDataSpaceName") String permissionsTemplateDataSpaceName)
	{
		AdaptationHome parentDataSpace = null;
		if (parentDataSpaceName != null)
		{
			parentDataSpace = sessionContext.getRepository()
				.lookupHome(HomeKey.forBranchName(parentDataSpaceName));
			if (parentDataSpace == null)
			{
				LoggingCategory.getKernel()
					.error("Parent data space " + parentDataSpaceName + " not found.");
			}
		}
		childLabelPrefix = childLabelPrefix == null ? "" : childLabelPrefix;
		AdaptationHome childDataSpace = null;
		try
		{
			childDataSpace = HomeUtils.createChildDataSpace(
				sessionContext.getSession(),
				parentDataSpace,
				childNamePrefix,
				childLabelPrefix,
				permissionsTemplateDataSpaceName);
		}
		catch (OperationException ex)
		{
			String msg = "Error creating child dataspace.";
			LoggingCategory.getKernel().error(ex.getMessage(), ex);
			throw new InternalServerErrorException(msg, ex);
		}
		return createDTO(childDataSpace);
	}

	/**
	 * Closes a dataspace.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/dataspace/dataspace/closeDataSpace/<dataSpaceName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/dataspace/dataspace/closeDataSpace/TEST2020-07-22T13:58:45.567
	 * }
	 * </pre>
	 * @param dataSpaceName the name of the dataspace to be closed.
	 */
	@GET
	@Path("/closeDataSpace/{dataSpaceName}")
	@Documentation("Closes a dataspace.")
	public void handleCloseDataSpace(@PathParam("dataSpaceName") String dataSpaceName)
	{
		AdaptationHome dataSpace = null;
		if (dataSpaceName != null)
		{
			dataSpace = sessionContext.getRepository()
				.lookupHome(HomeKey.forBranchName(dataSpaceName));
			if (dataSpace == null)
			{
				LoggingCategory.getKernel().error("DataSpace " + dataSpaceName + " not found.");
			}
		}
		try
		{
			HomeUtils.closeDataSpace(sessionContext.getSession(), dataSpace);
		}
		catch (OperationException ex)
		{
			String msg = "Error closing dataspace " + dataSpaceName + ".";
			LoggingCategory.getKernel().error(ex.getMessage(), ex);
			throw new InternalServerErrorException(msg, ex);
		}
	}

	/**
	 * Create the {@link DataSpaceDTO} objects.
	 * @param childDataSpace the created child dataspace (AdaptationHome)
	 * @return the {@link DataSpaceDTO} with the dataSpaceId and dataSpaceLabel.
	 */
	private static DataSpaceDTO createDTO(AdaptationHome childDataSpace)
	{

		DataSpaceDTO dto = new DataSpaceDTO();
		dto.setDataSpaceId(childDataSpace.getKey().format());
		dto.setDataSpaceLabel(childDataSpace.getLabel(Locale.getDefault()));
		return dto;
	}
}