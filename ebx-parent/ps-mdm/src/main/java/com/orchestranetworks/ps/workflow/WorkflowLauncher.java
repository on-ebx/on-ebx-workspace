package com.orchestranetworks.ps.workflow;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.workflow.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. Should use {@link WorkflowLauncherService} instead.
 */
@Deprecated
public class WorkflowLauncher extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected enum WorkItemRedirectPolicyEnum {
		NONE, MASTER_WORKFLOW, FIRST_SUB_WORKFLOW
	}

	private static final long WAIT_FOR_SUB_PROCESS_MILLIS = 2000;
	private static final int MAX_NUM_OF_SUB_PROCESS_WAITS = 15;
	private static final long WAIT_FOR_WORK_ITEM_MILLIS = 2000;
	private static final int MAX_NUM_OF_WORK_ITEM_WAITS = 15;

	private static final String ALERT_MESSAGE = "Workflow launched.";

	protected WorkflowLauncherContext workflowLauncherContext = null;
	protected WorkflowEngine wfEngine = null;
	protected ProcessLauncher launcher = null;
	protected String workflowName = null;
	protected String workflowDescription = null;
	protected WorkItemRedirectPolicyEnum workItemRedirectPolicy;
	protected boolean takeAndStartWorkItem = true;
	protected boolean showAlert = true;
	protected boolean warnIfNotRedirected = true;
	protected boolean includeUserInLabel = true;
	protected boolean initCurrentUserToLauncher = true;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		try
		{
			execute(request);
		}
		catch (OperationException ex)
		{
			throw new ServletException(ex);
		}
	}

	/**
	 * When executing from a servlet, the workflow name, table xpath, and workflow instance name must be specified by
	 * http request parameters. This method takes those parameters and calls into
	 * {@link #execute(WorkflowLauncherContext,String,String,String)}.
	 * 
	 * @param request the http request
	 * @throws OperationException if an error occurred while executing
	 */
	public void execute(HttpServletRequest request) throws OperationException
	{
		execute(
			new WorkflowLauncherContext(request),
			request.getParameter(WorkflowConstants.PARAM_WORKFLOW_NAME),
			request.getParameter(WorkflowConstants.PARAM_XPATH_TO_TABLE),
			request.getParameter(WorkflowConstants.PARAM_WORKFLOW_NAME));
	}

	/**
	 * This is a convenience method that simply creates a {@link WorkflowLauncherContext} from the given request and calls
	 * {@link #execute(WorkflowLauncherContext,String,String,String)}.
	 * 
	 * @param request the http request
	 * @param inWorkflowName the workflow name
	 * @param xpathToTable the table xpath
	 * @param workflowInstanceName the name of the workflow instance [use ""(empty string) to get default Create/Update Labels]
	 * @throws OperationException if an error occurred while executing
	 */
	public void execute(
		HttpServletRequest request,
		String inWorkflowName,
		String xpathToTable,
		String workflowInstanceName)
		throws OperationException
	{
		execute(
			new WorkflowLauncherContext(request),
			inWorkflowName,
			xpathToTable,
			workflowInstanceName);
	}

	/**
	 * Executes the workflow launch
	 * 
	 * @param context the launcher context, that encapsulates info about the launch such as current data space, current adaptation,
	 * and request parameters
	 * @param inWorkflowName the workflow name
	 * @param xpathToTable the table xpath
	 * @param workflowInstanceName the name of the workflow instance [use ""(empty string) to get default Create/Update Labels]
	 * @throws OperationException if an error occurred while executing
	 */
	public void execute(
		WorkflowLauncherContext context,
		String inWorkflowName,
		String xpathToTable,
		String workflowInstanceName)
		throws OperationException
	{
		this.workflowLauncherContext = context;
		Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		initRedirectionPolicy();
		if (adaptation.isTableOccurrence())
		{
			preventMultipleLaunches();
		}
		workflowName = inWorkflowName;

		wfEngine = context.createWorkflowEngine();
		launcher = wfEngine.getProcessLauncher(PublishedProcessKey.forName(workflowName));

		Session session = context.getSession();
		if (initCurrentUserToLauncher)
		{
			UserReference userReference = session.getUserReference();
			launcher.setInputParameter(
				WorkflowConstants.PARAM_CURRENT_USER_ID,
				userReference.getUserId());
			DirectoryHandler dirHandler = session.getDirectory();
			launcher.setInputParameter(
				WorkflowConstants.PARAM_CURRENT_USER_LABEL,
				dirHandler.displayUser(userReference, session.getLocale()));
		}
		launcher.setInputParameter(WorkflowConstants.PARAM_XPATH_TO_TABLE, xpathToTable);

		AdaptationHome masterDataSpace = context.getMasterDataSpace();
		String masterDataSpaceName = (masterDataSpace == null) ? null
			: masterDataSpace.getKey().getName();
		AdaptationHome currentDataSpace = context.getCurrentDataSpace();
		String currentDataSpaceName = (currentDataSpace == null) ? null
			: currentDataSpace.getKey().getName();
		// If either of the data space names are null, assign it to be equal to the other one
		if (masterDataSpaceName == null)
		{
			masterDataSpaceName = currentDataSpaceName;
		}
		else if (currentDataSpaceName == null)
		{
			currentDataSpaceName = masterDataSpaceName;
		}
		launcher.setInputParameter(WorkflowConstants.PARAM_MASTER_DATA_SPACE, masterDataSpaceName);
		launcher
			.setInputParameter(WorkflowConstants.PARAM_WORKING_DATA_SPACE, currentDataSpaceName);

		assignWorkflowDescription();

		if (workflowDescription != null && !workflowDescription.isEmpty())
		{
			// Verify that the description does not consist of only empty spaces
			if (!(workflowDescription.trim().length() == 0))
			{
				launcher.setDescription(UserMessage.createInfo(workflowDescription));
			}
		}

		workflowInstanceName = enrichWorkflowInstanceName(
			workflowInstanceName,
			session.getLocale());
		if (adaptation.isTableOccurrence())
		{
			launcher.setLabel(
				UserMessage.createInfo(assignLabelForUpdate(workflowInstanceName, session)));
			launcher
				.setInputParameter(WorkflowConstants.PARAM_RECORD, adaptation.toXPathExpression());
			launcher.setInputParameter(
				WorkflowConstants.PARAM_DATA_SET,
				adaptation.getContainer().getAdaptationName().getStringName());
		}
		else if (xpathToTable == null)
		{
			launcher.setLabel(
				UserMessage.createInfo(assignLabelForDataSetWorkflow(workflowInstanceName)));
		}
		else
		{
			launcher.setLabel(
				UserMessage.createInfo(assignLabelForCreate(xpathToTable, workflowInstanceName)));
			launcher.setInputParameter(
				WorkflowConstants.PARAM_DATA_SET,
				adaptation.getAdaptationName().getStringName());
		}
		UserReference user = session.getUserReference();
		launcher.setCreator(user);
		setAdditionalContextVariables();

		ProcessInstanceKey processInstanceKey = launcher.launchProcess();

		ServiceContext sContext = context.getServiceContext();
		if (sContext != null)
		{
			UIServiceComponentWriter writer = sContext.getUIComponentWriter();
			String redirectURL = sContext.getURLForEndingService();
			if (workItemRedirectPolicy == WorkItemRedirectPolicyEnum.MASTER_WORKFLOW
				|| workItemRedirectPolicy == WorkItemRedirectPolicyEnum.FIRST_SUB_WORKFLOW)
			{
				ProcessInstance processInstance = getProcessInstanceForFirstWorkItem(
					processInstanceKey);
				if (processInstance != null)
				{
					UserTask.WorkItem firstWorkItem = null;
					try
					{
						int i;
						for (i = 0; i < MAX_NUM_OF_WORK_ITEM_WAITS
							&& (firstWorkItem = getFirstWorkItem(
								processInstance.getProcessInstanceKey())) == null; i++)
						{
							LoggingCategory.getWorkflow().debug(
								"Sleeping " + WAIT_FOR_WORK_ITEM_MILLIS
									+ " millis for first work item to be created. Attempt #" + i);
							Thread.sleep(WAIT_FOR_WORK_ITEM_MILLIS);
						}
						if (i == MAX_NUM_OF_WORK_ITEM_WAITS)
						{
							LoggingCategory.getWorkflow().error(
								"Max number of retries reached for wait of first user task work item");
						}
					}
					catch (InterruptedException ex)
					{
						LoggingCategory.getWorkflow()
							.error("Waiting for first user task work item interrupted", ex);
					}
					if (firstWorkItem != null)
					{
						if (session.isUserInRole(
							WorkflowUtilities.getWorkItemOfferedToRole(firstWorkItem)))
						{
							redirectURL = sContext.getURLForSelection(
								firstWorkItem.getWorkItemKey(),
								takeAndStartWorkItem);
						}
						else
						{
							if (warnIfNotRedirected)
							{
								LoggingCategory.getWorkflow().warn(
									"User that launched service is not in role for first work item");
							}
							if (showAlert)
							{
								writer.addJS("alert('" + ALERT_MESSAGE + "');");
							}
						}
					}
				}
			}
			else if (showAlert)
			{
				writer.addJS("alert('" + ALERT_MESSAGE + "');");
			}

			writer.addJS("window.location.href=\"" + redirectURL + "\";");
		}
	}
	protected void initRedirectionPolicy()
	{
		workItemRedirectPolicy = WorkItemRedirectPolicyEnum.MASTER_WORKFLOW;
	}

	protected void preventMultipleLaunches() throws OperationException
	{
		// do nothing
	}

	protected ProcessInstance getProcessInstanceForFirstWorkItem(
		ProcessInstanceKey processInstanceKey)
	{
		if (workItemRedirectPolicy == WorkItemRedirectPolicyEnum.FIRST_SUB_WORKFLOW)
		{
			List<ProcessInstance> subProcessInstances = null;
			try
			{
				int i;
				// Need to fetch process instance each time through loop.
				// Can't do once & store in a variable because object needs
				// to be constructed from current state each time
				for (i = 0; i < MAX_NUM_OF_SUB_PROCESS_WAITS
					&& (subProcessInstances = wfEngine.getProcessInstance(processInstanceKey)
						.getCurrentSubWorkflows()).isEmpty(); i++)
				{
					LoggingCategory.getWorkflow().debug(
						"Sleeping " + WAIT_FOR_SUB_PROCESS_MILLIS
							+ " millis for sub-workflow to start. Attempt #" + i);
					Thread.sleep(WAIT_FOR_SUB_PROCESS_MILLIS);
				}
				if (i == MAX_NUM_OF_SUB_PROCESS_WAITS)
				{
					LoggingCategory.getWorkflow()
						.error("Max number of retries reached for wait of first sub-workflow");
				}
			}
			catch (InterruptedException ex)
			{
				LoggingCategory.getWorkflow()
					.error("Waiting for first sub-workflow interrupted", ex);
			}
			return (subProcessInstances == null || subProcessInstances.isEmpty()) ? null
				: subProcessInstances.get(0);
		}
		return wfEngine.getProcessInstance(processInstanceKey);
	}

	protected UserTask.WorkItem getFirstWorkItem(ProcessInstanceKey processInstanceKey)
	{
		// We must look up the process instance again because it needs to be refreshed,
		// otherwise we'd be looking up work items in a stale process instance
		ProcessInstance processInstance = wfEngine.getProcessInstance(processInstanceKey);
		List<UserTask.WorkItem> workItems = processInstance.getWorkItems();
		return workItems.isEmpty() ? null : workItems.get(0);
	}

	protected void assignWorkflowDescription()
	{
		// override in subclass
	}

	protected String enrichWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		// Default behavior is to use the Workflow Instance name if provided, else use the Workflow Model Label
		// If different behavior is desired, then override this method in a subclass
		if (workflowInstanceName != null)
		{
			return workflowInstanceName;
		}
		return getWorkflowModelLabelForInstanceName(workflowName, locale);
	}

	/**
	 * Get the workflow model label as it should appear in the workflow instance name.
	 * By default, this simply returns the workflow model's label but can be sub-classed to massage the label.
	 * 
	 * @param workflowModelName the name of the workflow model
	 * @param locale the locale
	 * @return the label as it should appear in the workflow instance name
	 */
	protected String getWorkflowModelLabelForInstanceName(String workflowModelName, Locale locale)
	{
		return wfEngine.getPublishedProcess(PublishedProcessKey.forName(workflowName))
			.getLabel()
			.formatMessage(locale);
	}

	// override if needed
	protected void setAdditionalContextVariables() throws OperationException
	{
		// do nothing
	}

	protected String assignLabelForCreate(String xpathToTable, String workflowInstanceName)
	{
		//  If the Provided Workflow Instance Name is "" (Empty String), then the default "Create Record" Label will be applied		
		AdaptationTable table = workflowLauncherContext.getCurrentAdaptation()
			.getTable(Path.parse(xpathToTable));
		Session session = workflowLauncherContext.getSession();
		String tableName = table.getTableNode().getLabel(session.getLocale());
		String label = (StringUtils.isNotEmpty(workflowInstanceName)) ? workflowInstanceName
			: "Create " + tableName + " Record";
		return "${" + WorkflowConstants.PARAM_RECORD_NAME_VALUE + "} " + label + ", launched by "
			+ session.getUserReference().getUserId();
	}

	protected String assignLabelForUpdate(String workflowInstanceName, Session session)
	{
		//  If the Provided Workflow Instance Name is "" (Empty String), then the default "Update Record" Label will be applied		
		Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		String tableName = adaptation.getContainerTable().getTableNode().getLabel(
			session.getLocale());
		String recordLabel = getRecordLabel(adaptation, session);
		launcher.setInputParameter(WorkflowConstants.PARAM_RECORD_NAME_VALUE, recordLabel);
		String label = (StringUtils.isNotEmpty(workflowInstanceName)) ? workflowInstanceName
			: "Update " + tableName + " Record";
		StringBuilder bldr = new StringBuilder("${")
			.append(WorkflowConstants.PARAM_RECORD_NAME_VALUE)
			.append("} ")
			.append(label);
		if (includeUserInLabel)
		{
			bldr.append(", launched by ").append(session.getUserReference().getUserId());
		}
		return bldr.toString();
	}

	protected String getRecordLabel(Adaptation record, Session session)
	{
		return record.getLabel(session.getLocale());
	}

	public WorkItemRedirectPolicyEnum getWorkItemRedirectPolicy()
	{
		return this.workItemRedirectPolicy;
	}

	public void setWorkItemRedirectPolicy(WorkItemRedirectPolicyEnum workItemRedirectPolicy)
	{
		this.workItemRedirectPolicy = workItemRedirectPolicy;
	}

	public boolean isTakeAndStartWorkItem()
	{
		return this.takeAndStartWorkItem;
	}

	public void setTakeAndStartWorkItem(boolean takeAndStartWorkItem)
	{
		this.takeAndStartWorkItem = takeAndStartWorkItem;
	}

	public boolean isIncludeUserInLabel()
	{
		return includeUserInLabel;
	}

	public void setIncludeUserInLabel(boolean includeUserInLabel)
	{
		this.includeUserInLabel = includeUserInLabel;
	}

	public boolean isInitCurrentUserToLauncher()
	{
		return initCurrentUserToLauncher;
	}

	public void setInitCurrentUserToLauncher(boolean initCurrentUserToLauncher)
	{
		this.initCurrentUserToLauncher = initCurrentUserToLauncher;
	}

	protected String assignLabelForDataSetWorkflow(String workflowInstanceName)
	{
		if (includeUserInLabel)
		{
			Session session = workflowLauncherContext.getSession();
			return workflowInstanceName + ", launched by " + session.getUserReference().getUserId();
		}
		return workflowInstanceName;
	}
}
