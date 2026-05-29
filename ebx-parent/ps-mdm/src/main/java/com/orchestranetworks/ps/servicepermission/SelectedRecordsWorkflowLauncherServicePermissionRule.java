package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;
import com.orchestranetworks.workflow.*;

/**
 * Selected Records Workflow Launcher Service Permission Rule
 * -- Will disable the service under the following conditions: 
 *        -- Disable it when already inside a workflow
 *        -- Disable if maxNumberOfSelectedRecords is declared for the service and is exceeded by the user
 *        -- Disable it if checkIfRecordHasActiveWorkflow is declared for the service and any Records already have an Active instance of this Workflow
 * 
 */
public class SelectedRecordsWorkflowLauncherServicePermissionRule
	implements ServicePermissionRule<TableViewEntitySelection>
{
	private SelectedRecordsWorkflowLauncherServiceDeclaration serviceDeclaration;

	public SelectedRecordsWorkflowLauncherServicePermissionRule(
		SelectedRecordsWorkflowLauncherServiceDeclaration serviceDeclaration)
	{
		super();
		this.serviceDeclaration = serviceDeclaration;
	}

	@Override
	public UserServicePermission getPermission(
		ServicePermissionRuleContext<TableViewEntitySelection> context)
	{
		// Disable it when already inside a workflow
		if (context.getSession().isInWorkflowInteraction(true))
		{
			return UserServicePermission.getDisabled(
				UserMessage
					.createError("This service can't be invoked from inside of a workflow."));
		}

		//  Disable if maxNumberOfSelectedRecords is exceeded
		Integer maxNumberOfSelectedRecords = serviceDeclaration.getMaxNumberOfSelectedRecords();
		if (maxNumberOfSelectedRecords != null)
		{
			try
			{
				int selectedRecordListSize = SelectedRecordsWorkflowLauncherService
					.getSelectedRecordList(context.getEntitySelection())
					.size();
				if (maxNumberOfSelectedRecords < selectedRecordListSize)
				{
					return UserServicePermission.getDisabled(
						UserMessage.createError(
							"The number of selected records (" + selectedRecordListSize
								+ ") exceeeds the maximum allowed ("
								+ maxNumberOfSelectedRecords.toString() + ")."));
				}
			}
			catch (OperationException e)
			{
				LoggingCategory.getKernel().error("Error getting selected record list.", e);
				return UserServicePermission.getDisabled();
			}

		}

		// Check for active workflows if service was declared to check for active workflows
		if (serviceDeclaration.isCheckIfRecordHasActiveWorkflow())
		{
			return disableIfActiveWorkflowExists(context);
		}

		return UserServicePermission.getEnabled();

	}

	// Disable it if any Records already have an Active instance of this Workflow
	public UserServicePermission disableIfActiveWorkflowExists(
		ServicePermissionRuleContext<TableViewEntitySelection> context)
	{
		// get selected record id list
		List<String> selectedRecordIdList = null;
		try
		{
			selectedRecordIdList = SelectedRecordsWorkflowLauncherService
				.getSelectedRecordIdList(context.getEntitySelection());
		}
		catch (OperationException e)
		{
			LoggingCategory.getKernel().error("Error getting selected record id list.", e);
			return UserServicePermission.getDisabled();
		}

		// get list of process instance keys for the given workflow
		Repository repo = context.getEntitySelection().getDataspace().getRepository();
		WorkflowEngine workflowEngine = WorkflowEngine
			.getFromRepository(repo, context.getSession());
		List<ProcessInstanceKey> processInstanceKeyList = workflowEngine
			.getProcessInstanceKeys(serviceDeclaration.getWorkflowName());

		// build the list of record ids that have an active workflow (using TreeSet to avoid duplicates)
		SortedSet<String> haveActiveWorkflowsList = new TreeSet<String>();
		for (ProcessInstanceKey processInstanceKey : processInstanceKeyList)
		{
			ProcessInstance processInstance = workflowEngine.getProcessInstance(processInstanceKey);
			if (!workflowEngine.getProcessInstance(processInstanceKey).isCompleted())
			{
				if (serviceDeclaration.isLaunchSeperateWorkflowForEachRecord())
					try
					{
						// get the single record from the active workflow and if it is in the selected records list,
						//   then add it to the "have active workflows" list
						Adaptation record = WorkflowUtilities
							.getRecord(processInstance.getDataContext(), null, repo);
						if (record != null && selectedRecordIdList
							.contains(record.getOccurrencePrimaryKey().format()))
						{
							haveActiveWorkflowsList.add(record.getOccurrencePrimaryKey().format());
						}
					}
					catch (OperationException e)
					{
						LoggingCategory.getKernel()
							.error("Error getting record from existing process instance.", e);
						return UserServicePermission.getDisabled();
					}
				else if (processInstance.getDataContext()
					.isVariableDefined(SelectedRecordsWorkflowLauncherService.PARAM_RECORD_ID_LIST))
				{
					// get the record list from the active workflow and if any of them are in the selected records list,
					//   then add them to the "have active workflows" list
					String recordIdListValue = processInstance.getDataContext().getVariableString(
						SelectedRecordsWorkflowLauncherService.PARAM_RECORD_ID_LIST);
					List<String> recordIdList = CollectionUtils.splitString(
						recordIdListValue,
						SelectedRecordsWorkflowLauncherService.RECORD_ID_LIST_DELIMITER);
					haveActiveWorkflowsList
						.addAll(CollectionUtils.intersection(selectedRecordIdList, recordIdList));
				}
			}
		}

		// if any records have active workflows, then return Disabled
		if (!haveActiveWorkflowsList.isEmpty())
		{
			return UserServicePermission.getDisabled(
				UserMessage.createError(
					"Cannot select record(s) " + String.join(", ", haveActiveWorkflowsList)
						+ " because they are currently being used in an active workflow for this service."));
		}

		return UserServicePermission.getEnabled();
	}

	public SelectedRecordsWorkflowLauncherServiceDeclaration getServiceDeclaration()
	{
		return serviceDeclaration;
	}

	public void setServiceDeclaration(
		SelectedRecordsWorkflowLauncherServiceDeclaration serviceDeclaration)
	{
		this.serviceDeclaration = serviceDeclaration;
	}

}
