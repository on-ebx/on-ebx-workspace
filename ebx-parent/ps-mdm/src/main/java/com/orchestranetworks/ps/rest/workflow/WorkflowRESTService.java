package com.orchestranetworks.ps.rest.workflow;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.rest.annotation.*;
import com.orchestranetworks.rest.annotation.Documentation;
import com.orchestranetworks.rest.inject.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;

/**
 * Provides Workflow related REST Services developed using the APIs offered by the REST Toolkit.
 * 
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path(value = "/workflow")
@Documentation("Workflow REST Service")
public class WorkflowRESTService
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

		return this.resourceInfo.getResourceMethod().getAnnotation(Documentation.class).value();
	}

	/**
	 * Launches an instance of the workflow with the specified publication name and the data context variables provided in the json payload {@link WorkflowDTO}.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the workflow launcher service (this is a GET call): 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/workflow/workflow/launchWorkflow/<publicationName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/workflow/workflow/launchWorkflow/EP_MaintainMasterData
	 * }
	 * </pre>
	 * @param workflowName the publication name of the workflow to be launched.
	 * @return the status and the {@link WorkflowResponseDTO} with the processInstanceId and the processInstanceKey.
	 */
	@GET
	@Path("/launchWorkflow/{workflowName}")
	@Documentation("Workflow launcher service")
	public Response handleLaunchWorkflow(@PathParam("workflowName") String workflowName)
	{
		return handleLaunchWorkflow(workflowName, null);
	}

	/**
	 * Launches an instance of the workflow with the specified publication name. </br>
	 * This service allows to post a json payload {@link WorkflowDTO} to set the values for the data context variables. 
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the workflow launcher service (this is a POST call): 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/workflow/workflow/launchWorkflow/<publicationName>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/workflow/workflow/launchWorkflow/EP_MaintainMasterData
	 * 
	 * 		Sample json payload with the values for the data context variables:
	 * 		{
	 * 			"workflowInstanceLabel": "Update Master data",
	 *			"workflowDescription": "This workflow is launched using the REST API"
	 *		}
	 * }
	 * </pre>
	 * @param workflowName the publication name of the workflow to be launched.
	 * @param workflowDTO the DTO with getters and setters for the data context variables from the json payload.
	 * @return the status and the {@link WorkflowResponseDTO} with the processInstanceId and the processInstanceKey.
	 */
	@POST
	@Path("/launchWorkflow/{workflowName}")
	@Documentation("Workflow launcher service with parameters.")
	public Response handleLaunchWorkflow(
		@PathParam("workflowName") String workflowName,
		WorkflowDTO workflowDTO)
	{

		if (workflowName == null)
		{
			throw new NotFoundException("Must supply a valid Workflow Name.");
		}
		final Repository repository = sessionContext.getRepository();
		final Session session = sessionContext.getSession();

		WorkflowResponseDTO response = null;
		WorkflowLauncherContext workflowLauncherContext = new WorkflowLauncherContext(
			session,
			repository,
			null,
			null);
		WorkflowEngine wfEngine = workflowLauncherContext.createWorkflowEngine();
		PublishedProcessKey publishedProcessKey = PublishedProcessKey.forName(workflowName);
		ProcessLauncher launcher = wfEngine.getProcessLauncher(publishedProcessKey);

		// Assign WorkflowInstanceLabel
		PublishedProcess publishedProcess = wfEngine.getPublishedProcess(publishedProcessKey);
		String workflowInstanceLabel = assignWorkflowInstanceLabel(
			publishedProcess,
			session.getLocale(),
			workflowDTO);
		launcher.setLabel(UserMessage.createInfo(workflowInstanceLabel));
		try
		{
			if (workflowDTO != null)
			{
				setAdditionalContextVariables(launcher, workflowDTO);
			}

			ProcessInstanceKey piKey = launcher.launchProcess();
			if (piKey != null)
			{
				response = new WorkflowResponseDTO();
				response.setProcessInstanceKey(piKey.format());
				response.setProcessInstanceId(piKey.getId());
			}
		}
		catch (OperationException ex)
		{
			String msg = "Error launching the workflow.";
			LoggingCategory.getKernel().error(ex.getMessage(), ex);
			throw new InternalServerErrorException(msg, ex);
		}
		return Response.ok().entity(response).build();
	}

	/**
	 * Gets the status of the given Process Instance.
	 * <pre>
	 * {@code 
	 * 		Sample REST URL to access the service: 
	 * 		http[s]://<host>:<port>/<moduleName>/rest/v1/ps/workflow/workflow/getWorkflowStatus/<processInstanceKey>
	 * 		http://localhost:8494/sce-mdm/rest/v1/ps/workflow/workflow/getWorkflowStatus/37|V2020-02-28T15:53:14.567|EP_MaintainMasterData
	 * }
	 * </pre>
	 * @param processInstanceKey the publication name of the workflow to be launched.
	 * @return the {@link WorkflowStatusDTO} with the processInstance attributes.
	 */
	@GET
	@Path("/getWorkflowStatus/{processInstanceKey}")
	@Documentation("Gets the status of the Process Instance.")
	public Response handleGetWorkflowStatus(
		@PathParam("processInstanceKey") String processInstanceKey)
	{
		if (processInstanceKey == null)
		{
			throw new NotFoundException("Must supply a valid ProcessInstanceKey.");
		}
		final Repository repository = sessionContext.getRepository();
		final Session session = sessionContext.getSession();
		WorkflowStatusDTO response = null;

		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repository, session);
		ProcessInstance processInstance = wfEngine
			.getProcessInstance(ProcessInstanceKey.parse(processInstanceKey));
		if (processInstance != null)
		{
			response = new WorkflowStatusDTO();
			response.setProcessInstanceLabel(
				processInstance.getLabel().formatMessage(session.getLocale()));
			if (processInstance.isInError())
			{
				response.setStatus(WorkflowStatus.ERROR);
			}
			else if (processInstance.isCompleted())
			{
				response.setStatus(WorkflowStatus.COMPLETED);
			}
			else
			{
				response.setStatus(WorkflowStatus.ACTIVE);
			}
			response.setCreatedDate(processInstance.getCreationDate().toString());
			response.setModifiedDate(processInstance.getLastModificationDate().toString());
		}
		else
		{
			throw new NotFoundException(
				"ProcessInstance does not exist for the given processInstanceKey.");
		}
		return Response.ok().entity(response).build();
	}

	/**
	 * Assign the workflow instance label as received from the json request, if not, default the workflow model label as the workflow instance label. </br> 
	 * This behavior can be overridden as needed.
	 * @param publishedProcess the workflow publication that is being launched
	 * @param locale the locale.
	 * @param workflowDTO the DTO with getters and setters for the data context variables from the json payload.
	 * @return the workflow instance label to be assigned for the workflow that is being launched.
	 */
	protected String assignWorkflowInstanceLabel(
		PublishedProcess publishedProcess,
		Locale locale,
		WorkflowDTO workflowDTO)
	{
		if (workflowDTO != null && workflowDTO.getWorkflowInstanceLabel() != null)
		{
			return workflowDTO.getWorkflowInstanceLabel();
		}
		return createWorkflowInstanceLabel(publishedProcess, locale);
	}

	/**
	 * Returns the default label, but this can be overridden to do something else.
	 * @param publishedProcess the workflow publication that is being launched.
	 * @param locale the locale.
	 * @return the workflow model label as the default workflow instance label.
	 */
	protected String createWorkflowInstanceLabel(PublishedProcess publishedProcess, Locale locale)
	{
		return publishedProcess.getLabel().formatMessage(locale);
	}

	/**
	 * Sets the values to the data context variables as received from the json payload {@link WorkflowDTO}.
	 * @param launcher the process launcher.
	 * @param workflowDTO the DTO with getters and setters for the data context variables from the json payload.
	 * @throws OperationException
	 */
	protected void setAdditionalContextVariables(ProcessLauncher launcher, WorkflowDTO workflowDTO)
		throws OperationException
	{
		if (workflowDTO.getWorkflowDescription() != null)
		{
			launcher.setDescription(UserMessage.createInfo(workflowDTO.getWorkflowDescription()));
		}
		if (workflowDTO.getDataSet() != null)
		{
			launcher.setInputParameter(WorkflowConstants.PARAM_DATA_SET, workflowDTO.getDataSet());
		}
		if (workflowDTO.getMasterDataSpace() != null)
		{
			launcher.setInputParameter(
				WorkflowConstants.PARAM_MASTER_DATA_SPACE,
				workflowDTO.getMasterDataSpace());
		}
		if (workflowDTO.getRecord() != null)
		{
			launcher.setInputParameter(WorkflowConstants.PARAM_RECORD, workflowDTO.getRecord());
		}
		if (workflowDTO.getWorkingDataSpace() != null)
		{
			launcher.setInputParameter(
				WorkflowConstants.PARAM_WORKING_DATA_SPACE,
				workflowDTO.getWorkingDataSpace());
		}
		if (workflowDTO.getXpathToTable() != null)
		{
			launcher.setInputParameter(
				WorkflowConstants.PARAM_XPATH_TO_TABLE,
				workflowDTO.getXpathToTable());
		}
		if (workflowDTO.getAdditionalDataContextVariables() != null)
		{
			Set<String> keys = workflowDTO.getAdditionalDataContextVariables().keySet();
			for (String key : keys)
			{
				launcher.setInputParameter(
					key,
					workflowDTO.getAdditionalDataContextVariables().get(key));
			}
		}

	}
}