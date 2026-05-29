/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scheduledtask;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * A scheduled task to launch a workflow
 */
public class LaunchWorkflowScheduledTask extends ScheduledTask
{

	private String workflow;
	private String workflowDescription = null;

	@Override
	public void execute(ScheduledExecutionContext context)
		throws OperationException, ScheduledTaskInterruption
	{
		final Repository repository = context.getRepository();
		final Session session = context.getSession();
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repository, session);
		String workflowToLaunch = getWorkflowToLaunch();
		PublishedProcessKey publishedProcessKey = PublishedProcessKey.forName(workflowToLaunch);
		ProcessLauncher launcher = wfEngine.getProcessLauncher(publishedProcessKey);
		//Assign WorkflowInstanceLabel
		PublishedProcess publishedProcess = wfEngine.getPublishedProcess(publishedProcessKey);
		String workflowInstanceLabel = createWorkflowInstanceLabel(
			publishedProcess,
			session.getLocale());
		launcher.setLabel(UserMessage.createInfo(workflowInstanceLabel));

		if (workflowDescription != null)
		{
			launcher.setDescription(UserMessage.createInfo(workflowDescription));
		}
		// set input parameters if any
		setAdditionalContextVariables(context, launcher);

		launcher.launchProcess();

	}

	// override if needed
	protected void setAdditionalContextVariables(
		ScheduledExecutionContext context,
		ProcessLauncher launcher)
		throws OperationException
	{
		// do nothing
	}

	// Just returns the default label, but this can be overridden to do something else
	protected String createWorkflowInstanceLabel(PublishedProcess publishedProcess, Locale locale)
	{
		return publishedProcess.getLabel().formatMessage(locale);
	}

	/**
	 * By default, will simply return the specified workflow. But can be overridden to have additional logic.
	 */
	protected String getWorkflowToLaunch()
	{
		return workflow;
	}

	public String getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(String workflow)
	{
		this.workflow = workflow;
	}

	public String getWorkflowDescription()
	{
		return workflowDescription;
	}

	public void setWorkflowDescription(String workflowDescription)
	{
		this.workflowDescription = workflowDescription;
	}

}
