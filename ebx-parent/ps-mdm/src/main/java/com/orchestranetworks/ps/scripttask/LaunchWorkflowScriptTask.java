package com.orchestranetworks.ps.scripttask;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

// TODO: Refactor this to use WorkflowLauncher & WorkflowLauncherContext
/**
 * 
 * Launches a Workflow
 * 
 * <pre>{@code 
	<bean className="com.orchestranetworks.ps.workflow.scripttask.LaunchWorkflowScript">
        <documentation xml:lang="en-US">
            <label>Launch a Workflow</label>
            <description>
                Create a process instance of a given publication
            </description>
        </documentation>
        <properties>
            <property name="workflow" input="true">
                <documentation xml:lang="en-US">
                    <label>Workflow publication</label>
                    <description>
                       Workflow publication to launch
                    </description>
                </documentation>
            </property>
        </properties>
    </bean>  
 * }</pre>
 * 
 * @author MCH
 * 
 */
public class LaunchWorkflowScriptTask extends ScriptTaskBean
{
	private String workflow;
	protected String workflowDescription = null;

	@Override
	public void executeScript(ScriptTaskBeanContext aContext) throws OperationException
	{
		final Repository repository = aContext.getRepository();
		final Session session = aContext.getSession();
		final WorkflowEngine engine = WorkflowEngine.getFromRepository(repository, session);
		final PublishedProcessKey publishedProcessKey = PublishedProcessKey
			.forName(getWorkflowToLaunch());
		final ProcessLauncher launcher = engine.getProcessLauncher(publishedProcessKey);

		final ProcessInstance launchingProcess = aContext.getProcessInstance();
		final DataContextReadOnly dataContext = launchingProcess.getDataContext();
		initWorkflowParameters(dataContext, launcher);

		// Assign WorkflowInstanceLabel
		final PublishedProcess publishedProcess = engine.getPublishedProcess(publishedProcessKey);
		final String workflowInstanceLabel = createWorkflowInstanceLabel(
			publishedProcess,
			launchingProcess,
			session.getLocale());
		launcher.setLabel(UserMessage.createInfo(workflowInstanceLabel));
		if (workflowDescription != null)
		{
			launcher.setDescription(UserMessage.createInfo(workflowDescription));
		}

		launcher.launchProcess();
	}

	protected String createWorkflowInstanceLabel(
		PublishedProcess publishedProcess,
		ProcessInstance launchingProcess,
		Locale locale)
	{
		String workflowInstanceLabel = publishedProcess.getLabel().formatMessage(locale);
		final DataContextReadOnly dataContext = launchingProcess.getDataContext();
		if (dataContext.isVariableDefined(WorkflowConstants.PARAM_RECORD_NAME_VALUE))
		{
			String recordNameValue = dataContext
				.getVariableString(WorkflowConstants.PARAM_RECORD_NAME_VALUE);
			if (recordNameValue != null)
			{
				workflowInstanceLabel = recordNameValue + ": " + workflowInstanceLabel;
				workflowInstanceLabel = appendAdditionalData(workflowInstanceLabel, dataContext);
			}
		}
		return workflowInstanceLabel;
	}

	/**
	 * By default, will simply return the specified workflowInstanceLabel. But can be overridden to have additional logic.
	 */
	protected String appendAdditionalData(
		String workflowInstanceLabel,
		DataContextReadOnly dataContext)
	{
		return workflowInstanceLabel;
	}

	/**
	 * By default, will simply return the specified workflow. But can be overridden to have additional logic.
	 */
	protected String getWorkflowToLaunch()
	{
		return workflow;
	}

	protected List<String> getWorkflowParametersToCopy(DataContextReadOnly dataContext)
	{
		ArrayList<String> params = new ArrayList<>();
		final Iterator<String> iterator = dataContext.getVariableNames();
		while (iterator.hasNext())
		{
			params.add(iterator.next());
		}
		return params;
	}

	protected void initWorkflowParameters(DataContextReadOnly dataContext, ProcessLauncher launcher)
	{
		List<String> paramNames = getWorkflowParametersToCopy(dataContext);
		for (String paramName : paramNames)
		{
			launcher.setInputParameter(paramName, dataContext.getVariableString(paramName));
		}
	}

	public final String getWorkflow()
	{
		return this.workflow;
	}

	public final void setWorkflow(String workflow)
	{
		this.workflow = workflow;
	}
}
