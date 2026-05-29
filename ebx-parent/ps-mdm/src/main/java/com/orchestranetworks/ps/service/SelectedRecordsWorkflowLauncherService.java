package com.orchestranetworks.ps.service;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * Selected Records Workflow Launcher Service
 * -- Service to Launch specified Workflow for Selected Records 
 * -- Unless you have declared "launchSeperateWorkflowForEachRecord", then:
 *        -- Workflow Model must have Data Context Variables "recordIdList" and "recordIdFilter"
 *        -- And be sure to specify "${xpathToTable}${recordIdFilter}" as the "Dataset node (XPath)" parameter in the Update User Task of your workflow
 * 
 */
public class SelectedRecordsWorkflowLauncherService
	extends
	WorkflowLauncherService<TableViewEntitySelection>
{
	public static final String RECORD_ID_LIST_DELIMITER = ", ";
	public static final String PARAM_RECORD_ID_LIST = "recordIdList";
	public static final String PARAM_RECORD_ID_FILTER = "recordIdFilter";

	private SelectedRecordsWorkflowLauncherServiceDeclaration serviceDeclaration;

	@Override
	protected void setAdditionalContextVariables() throws OperationException
	{
		if (!serviceDeclaration.isLaunchSeperateWorkflowForEachRecord())
		{
			// set data context variables for the selected record list and filter
			List<String> selectedRecordIdList = getSelectedRecordIdList(
				context.getEntitySelection());
			if (selectedRecordIdList.isEmpty())
			{
				return;
			}
			String selectedRecordFilter = getSelectedRecordFilter(selectedRecordIdList);
			launcher.setInputParameter(PARAM_RECORD_ID_FILTER, selectedRecordFilter);
			launcher.setInputParameter(
				PARAM_RECORD_ID_LIST,
				String.join(RECORD_ID_LIST_DELIMITER, selectedRecordIdList));
		}

		super.setAdditionalContextVariables();
	}

	// Resulting Filter should be formatted as: [./recordIdFieldName=4120 or ./recordIdFieldName=4129 or ./recordIdFieldName=12394]
	// -- assumes the recordId is an Integer  
	// --  for Strings, override this methods to wrap the values in ""
	// --  for Concatenated Keys, override this method to form the appropriate predicate
	public String getSelectedRecordFilter(List<String> selectedRecordIdList)
	{
		return "[" + serviceDeclaration.getRecordIdPath().format() + "="
			+ String.join(
				" or " + serviceDeclaration.getRecordIdPath().format() + "=",
				selectedRecordIdList)
			+ "]";

	}

	// build the selected record id list from the entity selection object
	public static List<String> getSelectedRecordIdList(TableViewEntitySelection entitySelection)
		throws OperationException
	{
		List<String> selectedRecordIdList = new ArrayList<>();
		for (Adaptation selectedRecord : getSelectedRecordList(entitySelection))
		{
			selectedRecordIdList.add(selectedRecord.getOccurrencePrimaryKey().format());
		}
		return selectedRecordIdList;
	}

	@Override
	// This method is overridden to allow launching of a separate workflow for each selected record (if specified)
	public void execute(Session session) throws OperationException
	{
		if (!serviceDeclaration.isLaunchSeperateWorkflowForEachRecord())
		{
			super.execute(session);
			return;
		}

		// launch workflow for each selected record
		UserServiceRequest paneContext = context.getRequest();
		TableViewEntitySelection entitySelection = context.getEntitySelection();
		for (Adaptation selectedRecord : getSelectedRecordList(entitySelection))
		{
			xpathToTable = selectedRecord.getContainerTable().getTablePath().format();
			WorkflowLauncherContext wc = new WorkflowLauncherContext(
				paneContext.getSession(),
				paneContext.getRepository(),
				entitySelection.getDataspace(),
				selectedRecord);
			wc.setWriter(context.getWriter());
			wc.setLocator(context.getLocator());
			execute(wc, workflowName, xpathToTable, workflowInstanceName);
		}
	}

	// build the selected record list from the entity selection object
	public static List<Adaptation> getSelectedRecordList(TableViewEntitySelection entitySelection)
		throws OperationException
	{
		Request selectedRecordsRequest = entitySelection.getSelectedRecords();
		RequestResult result = selectedRecordsRequest.execute();
		try
		{
			return AdaptationUtil.getRecords(result);
		}
		finally
		{
			result.close();
		}
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
