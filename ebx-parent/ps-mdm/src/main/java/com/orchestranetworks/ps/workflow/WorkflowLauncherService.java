package com.orchestranetworks.ps.workflow;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.workflow.*;

public class WorkflowLauncherService<S extends DatasetEntitySelection>
	extends
	AbstractUserService<S>
{
	protected enum WorkItemRedirectPolicyEnum {
		NONE, MASTER_WORKFLOW, FIRST_SUB_WORKFLOW
	}

	public static final String PARAM_WORKFLOW_NAME = "workflowName";
	public static final String PARAM_XPATH_TO_TABLE = "xpathToTable";

	protected static final ObjectKey INPUT_OBJECT_KEY = ObjectKey.forName("input");
	protected static final Path NAME_PATH = Path.parse("./name");
	protected static final Path DESCRIPTION_PATH = Path.parse("./description");

	private static final long WAIT_FOR_SUB_PROCESS_MILLIS = 2000;
	private static final int MAX_NUM_OF_SUB_PROCESS_WAITS = 15;
	private static final long WAIT_FOR_WORK_ITEM_MILLIS = 2000;
	private static final int MAX_NUM_OF_WORK_ITEM_WAITS = 15;

	private static final String ALERT_MESSAGE = "Workflow launched.";

	protected boolean showForm;
	protected String nameInputValue;
	protected String descriptionInputValue;

	protected WorkflowLauncherContext workflowLauncherContext = null;
	protected WorkflowEngine wfEngine = null;
	protected ProcessLauncher launcher = null;
	protected String workflowName = null;
	protected String workflowInstanceName = null;
	protected String xpathToTable = null;
	protected String workflowDescription = null;
	protected WorkItemRedirectPolicyEnum workItemRedirectPolicy;
	protected boolean takeAndStartWorkItem = true;
	protected boolean showAlert = true;
	protected boolean warnIfNotRedirected = true;

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<S> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (showForm)
		{
			initContext(aContext);
			if (aContext.isInitialDisplay())
			{
				BeanDefinition def = context.defineObject(aBuilder, INPUT_OBJECT_KEY);
				BeanElement nameElement = defineElement(
					def,
					NAME_PATH,
					"Workflow Name",
					SchemaTypeName.XS_STRING,
					null);
				// Require name
				nameElement.setMinOccurs(1);
				defineElement(
					def,
					DESCRIPTION_PATH,
					"Workflow Description",
					SchemaTypeName.OSD_TEXT,
					null);
				aBuilder.registerBean(INPUT_OBJECT_KEY, def);
			}
		}
		super.setupObjectContext(aContext, aBuilder);
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		if (showForm)
		{
			nameInputValue = StringUtils.trimToNull(
				(String) fromContext.getValueContext(INPUT_OBJECT_KEY, NAME_PATH).getValue());
			descriptionInputValue = StringUtils.trimToNull(
				(String) fromContext.getValueContext(INPUT_OBJECT_KEY, DESCRIPTION_PATH)
					.getValue());
		}
		return super.readValues(fromContext);
	}

	/**
	 * Executes the workflow launch
	 *
	 * @param context
	 *            the launcher context, that encapsulates info about the launch
	 *            such as current data space, current adaptation, and request
	 *            parameters
	 * @param inWorkflowName
	 *            the workflow name
	 * @param xpathToTable
	 *            the table xpath
	 * @param workflowInstanceName
	 *            the name of the workflow instance [use ""(empty string) to get default Create/Update Labels]
	 *                                              [use <null> to have the the workflow model label assigned as the workflow instance name]
	 * @throws OperationException
	 *             if an error occurred while executing
	 */
	public void execute(
		WorkflowLauncherContext context,
		String inWorkflowName,
		String xpathToTable,
		String workflowInstanceName)
		throws OperationException
	{
		this.workflowLauncherContext = context;
		UserServiceWriter writer = context.getWriter();
		Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		initRedirectionPolicy();
		if (adaptation.isTableOccurrence())
		{
			preventMultipleLaunches();
		}
		if (inWorkflowName != null)
			workflowName = inWorkflowName;

		wfEngine = context.createWorkflowEngine();
		PublishedProcessKey ppk = PublishedProcessKey.forName(workflowName);
		try
		{
			launcher = wfEngine.getProcessLauncher(ppk);
		}
		catch (IllegalArgumentException e)
		{
			if (writer != null)
			{
				writer.addJS("alert('" + workflowName + " is currently disabled');");
			}
			else
			{
				LoggingCategory.getWorkflow()
					.warn("Attempted to launch disabled workflow " + workflowName);
			}
			return;
		}

		Session session = context.getSession();
		UserReference userReference = session.getUserReference();
		launcher
			.setInputParameter(WorkflowConstants.PARAM_CURRENT_USER_ID, userReference.getUserId());
		DirectoryHandler dirHandler = session.getDirectory();
		launcher.setInputParameter(
			WorkflowConstants.PARAM_CURRENT_USER_LABEL,
			dirHandler.displayUser(userReference, session.getLocale()));
		if (xpathToTable != null)
		{
			launcher.setInputParameter(WorkflowConstants.PARAM_XPATH_TO_TABLE, xpathToTable);
		}

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
		if (workflowDescription != null)
		{
			launcher.setDescription(UserMessage.createInfo(workflowDescription));
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
		else
		{
			if (xpathToTable == null)
			{
				launcher.setLabel(
					UserMessage.createInfo(assignLabelForDataSetWorkflow(workflowInstanceName)));
			}
			else
			{
				launcher.setLabel(
					UserMessage
						.createInfo(assignLabelForCreate(xpathToTable, workflowInstanceName)));
			}
			launcher.setInputParameter(
				WorkflowConstants.PARAM_DATA_SET,
				adaptation.getAdaptationName().getStringName());
		}
		UserReference user = session.getUserReference();
		launcher.setCreator(user);
		setAdditionalContextVariables();

		ProcessInstanceKey processInstanceKey = launcher.launchProcess();

		if (writer != null)
		{
			UserServiceResourceLocator locator = context.getLocator();
			String redirectURL = locator.getURLForEndingService();
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
						Role role = WorkflowUtilities.getWorkItemOfferedToRole(firstWorkItem);
						if (user.equals(firstWorkItem.getUserReference()) || (dirHandler != null
							&& role != null && dirHandler.isUserInRole(user, role)))
						{
							redirectURL = locator.getURLForSelection(
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
		// We must look up the process instance again because it needs to be
		// refreshed,
		// otherwise we'd be looking up work items in a stale process instance
		ProcessInstance processInstance = wfEngine.getProcessInstance(processInstanceKey);
		List<UserTask.WorkItem> workItems = processInstance.getWorkItems();
		return workItems.isEmpty() ? null : workItems.get(0);
	}

	protected void assignWorkflowDescription()
	{
		if (showForm && descriptionInputValue != null)
		{
			workflowDescription = descriptionInputValue;
		}
	}

	protected String enrichWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		String baseName = getBaseWorkflowInstanceName(workflowInstanceName, locale);

		if (showForm && nameInputValue != null)
		{
			return enrichWorkflowInstanceNameFromFormInput(baseName);
		}
		return baseName;
	}

	/**
	 * Get the base workflow instance name to use when enriching the workflow instance name in
	 * {@link #enrichWorkflowInstanceName(String, Locale)}. If the value passed in is not <code>null</code>,
	 * this simply returns it. Otherwise, it returns the result of {@link #getWorkflowModelLabelForInstanceName(String, Locale)}.
	 * This represents the workflow name used in the label before any input from the (optional) form is applied.
	 * 
	 * @param workflowInstanceName the workflow instance name
	 * @param locale the locale
	 * @return the base workflow instance name
	 */
	protected String getBaseWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		if (workflowInstanceName == null)
		{
			return getWorkflowModelLabelForInstanceName(workflowName, locale);
		}
		return workflowInstanceName;
	}

	/**
	 * Further enrich the workflow instance name with information from the form.
	 * This only gets called when <code>showForm</code> is <code>true</code> and
	 * a value was input for the workflow name.
	 * 
	 * @param baseWorkflowInstanceName the base name that is going to be enriched
	 *        (determined in {@link #getBaseWorkflowInstanceName(String, Locale)})
	 * @return the full enriched workflow instance name
	 */
	protected String enrichWorkflowInstanceNameFromFormInput(String baseWorkflowInstanceName)
	{
		return nameInputValue + ", " + baseWorkflowInstanceName;
	}

	/**
	 * Get the workflow model label as it should appear in the workflow instance
	 * name. By default, this simply returns the workflow model's label but can
	 * be sub-classed to massage the label.
	 *
	 * @param workflowModelName
	 *            the name of the workflow model
	 * @param locale
	 *            the locale
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
		Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		String tableName = adaptation.getContainerTable().getTableNode().getLabel(
			session.getLocale());
		String recordLabel = getRecordLabel(adaptation, session);
		launcher.setInputParameter(WorkflowConstants.PARAM_RECORD_NAME_VALUE, recordLabel);
		String label = (StringUtils.isNotEmpty(workflowInstanceName)) ? workflowInstanceName
			: "Update " + tableName + " Record";
		return "${" + WorkflowConstants.PARAM_RECORD_NAME_VALUE + "} " + label + ", launched by "
			+ session.getUserReference().getUserId();
	}

	protected String assignLabelForDataSetWorkflow(String workflowInstanceName)
	{
		Session session = workflowLauncherContext.getSession();
		return workflowInstanceName + ", launched by " + session.getUserReference().getUserId();
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

	@Override
	public void execute(Session session) throws OperationException
	{
		UserServiceRequest paneContext = context.getRequest();
		S selection = context.getEntitySelection();
		Adaptation adaptation;
		if (selection instanceof RecordEntitySelection)
		{
			adaptation = ((RecordEntitySelection) selection).getRecord();
			xpathToTable = adaptation.getContainerTable().getTablePath().format();
		}
		else
		{
			adaptation = selection.getDataset();
		}
		WorkflowLauncherContext wc = new WorkflowLauncherContext(
			paneContext.getSession(),
			paneContext.getRepository(),
			selection.getDataspace(),
			adaptation);
		wc.setWriter(context.getWriter());
		wc.setLocator(context.getLocator());
		execute(wc, workflowName, xpathToTable, workflowInstanceName);
	}

	public String getWorkflowName()
	{
		return workflowName;
	}

	public void setWorkflowName(String workflowName)
	{
		this.workflowName = workflowName;
	}

	public String getWorkflowInstanceName()
	{
		return workflowInstanceName;
	}

	public void setWorkflowInstanceName(String workflowInstanceName)
	{
		this.workflowInstanceName = workflowInstanceName;
	}

	public String getXpathToTable()
	{
		return xpathToTable;
	}

	public void setXpathToTable(String xpathToTable)
	{
		this.xpathToTable = xpathToTable;
	}

	public String getWorkflowDescription()
	{
		return workflowDescription;
	}

	public void setWorkflowDescription(String workflowDescription)
	{
		this.workflowDescription = workflowDescription;
	}

	public boolean isShowForm()
	{
		return showForm;
	}

	public void setShowForm(boolean showForm)
	{
		this.showForm = showForm;
	}

	@Override
	public void landService()
	{
		// do nothing -- execute handles it
	}

}
