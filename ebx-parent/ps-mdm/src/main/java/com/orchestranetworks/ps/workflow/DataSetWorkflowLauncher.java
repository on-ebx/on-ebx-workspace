package com.orchestranetworks.ps.workflow;

import java.util.*;

import com.orchestranetworks.service.*;

import jakarta.servlet.http.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. Should implement with the User Service framework.
 */
@Deprecated
public class DataSetWorkflowLauncher extends WorkflowLauncher
{
	private static final long serialVersionUID = 1L;

	public static final String PARAM_WORKFLOW_INSTANCE_NAME = "workflowInstanceName";
	public static final String PARAM_WORKFLOW_INSTANCE_DESCRIPTION = "workflowInstanceDescription";
	private HttpServletRequest request = null;
	private WorkflowLauncherContext context = null;

	@Override
	public void execute(HttpServletRequest request) throws OperationException
	{
		this.request = request;
		execute(
			new WorkflowLauncherContext(request),
			getWorkflowKey(),
			request.getParameter(WorkflowConstants.PARAM_XPATH_TO_TABLE),
			request.getParameter(PARAM_WORKFLOW_INSTANCE_NAME));
	}

	public void execute(WorkflowLauncherContext context) throws OperationException
	{
		this.context = context;
		super.execute(
			context,
			getWorkflowKey(),
			context.getParameter(WorkflowConstants.PARAM_XPATH_TO_TABLE),
			context.getParameter(PARAM_WORKFLOW_INSTANCE_NAME));
	}

	protected String getWorkflowKey()
	{
		if (request != null)
		{
			return request.getParameter(WorkflowConstants.PARAM_WORKFLOW_NAME);
		}
		else
		{
			return context.getParameter(WorkflowConstants.PARAM_WORKFLOW_NAME);
		}
	}

	@Override
	protected String enrichWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		return workflowLauncherContext.getParameter(PARAM_WORKFLOW_INSTANCE_NAME) + " "
			+ super.enrichWorkflowInstanceName(workflowInstanceName, locale);
	}

	@Override
	protected void assignWorkflowDescription()
	{
		String description = workflowLauncherContext
			.getParameter(PARAM_WORKFLOW_INSTANCE_DESCRIPTION);

		workflowDescription = description;
	}
}