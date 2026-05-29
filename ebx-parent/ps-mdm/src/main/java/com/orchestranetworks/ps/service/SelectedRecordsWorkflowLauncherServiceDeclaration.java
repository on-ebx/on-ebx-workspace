package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Declares the Selected Records Workflow launcher service
 * -- Service to Launch specified Workflow for Selected Records 
 * 
 * To Register this from your Schema Extension, specify the following:
 * 
 * 		SelectedRecordsWorkflowLauncherServiceDeclaration serviceDeclaration = 
 *			new SelectedRecordsWorkflowLauncherServiceDeclaration(serviceName,        // Required   
 *															   serviceLabel,          // Optional, will default to the serviceName
 *                                                             serviceDescription,    // Optional
 *                                                             confirmBeforeLaunchMessage,   // Optional
 *                                                               // specify SelectedRecordsWorkflowLauncherServiceDeclaration.DEFAULT_CONFIRM for default confirmation message
 *                                                               // leave null for no confirmation
 *                                                             );
 *        serviceDeclaration.setWorkflowName(String);		// Required 
 *        serviceDeclaration.setTablePath(Path);			// Required 
 *        serviceDeclaration.setRecordIdPath(Path);			// Optional
 *                                                               // specify the Record Id Path if your selected record key is composed of a single field.
 *                                                               // otherwise, you can leave this null and instead you will need to override the getSelectedRecordFilter 
 *                                                                             method in your SelectedRecordsWorkflowLauncherService subclass implementation 
 *        serviceDeclaration.setWorkflowInstanceName(String);
 *        													// Optional, default is null
 *                                                          // use <null> to have the workflow model label assigned as the workflow instance name
 *            												// use ""(empty string) to get default Update Record Labels
 *        serviceDeclaration.setCheckIfRecordHasActiveWorkflow(boolean);
 *        													// Optional, default is true 
 *        													// will prevent selection of records that are used in an active instance of this workflow	
 *        serviceDeclaration.setLaunchSeperateWorkflowForEachRecord(boolean);
 *        													// Optional, default is false 
 *        													// will launch a separate workflow instance for each selected record                
 *        serviceDeclaration.setMaxNumberOfSelectedRecords(Integer);
 *        													// Optional, default is null  
 *        													// can be used to limit the number of records that can be selected               
 * 
 * 		schemaExtensionsContext.registerUserService(serviceDeclaration);      
 * 
 * 
 * -- If you have declared "launchSeperateWorkflowForEachRecord=true", then:
 *        -- Workflow Model must have Data Context Variables "record"
 *        -- And be sure to specify "${record}" as the "Dataset node (XPath)" parameter in the Update User Task of your workflow
 * -- Otherwise:
 *        -- Workflow Model must have Data Context Variables "recordIdList" and "recordIdFilter"
 *        -- And be sure to specify "${xpathToTable}${recordIdFilter}" as the "Dataset node (XPath)" parameter in the Update User Task of your workflow
 * 
 */

public class SelectedRecordsWorkflowLauncherServiceDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView
{

	private Path tablePath = null;
	private Path recordIdPath = null;
	private String workflowName = null;
	private String workflowInstanceName = null;

	private boolean checkIfRecordHasActiveWorkflow = true;
	private boolean launchSeperateWorkflowForEachRecord = false;
	private Integer maxNumberOfSelectedRecords = null;

	public SelectedRecordsWorkflowLauncherServiceDeclaration(
		String serviceName,
		String serviceLabel,
		String serviceDescription,
		String confirmBeforeLaunchMessage)
	{
		super(
			ServiceKey.forName(serviceName),
			null,
			serviceLabel,
			serviceDescription,
			confirmBeforeLaunchMessage);
	}

	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		SelectedRecordsWorkflowLauncherService service = getSelectedRecordsLauncherService();
		service.setServiceDeclaration(this);
		service.setXpathToTable(tablePath.format());
		service.setWorkflowName(workflowName);
		service.setWorkflowInstanceName(workflowInstanceName);
		return service;
	}

	@Override
	public void defineActivation(ActivationContextOnTableView context)
	{
		// Declare it on the specified table and require that at least 1 record is selected
		context.includeSchemaNodesMatching(tablePath);
		context.forbidEmptyRecordSelection();

		// By default it's disabled unless enabled in dataset permissions
		context.setDefaultPermission(UserServicePermission.getDisabled());

		// Enable it only when outside a workflow and optionally, for Records that do not have an Active instance of this Workflow
		context.setPermissionRule(getSelectedRecordsWorkflowLauncherServicePermissionRule());
	}

	// override this method if you are extending the SelectedRecordsWorkflowLauncherService class
	public SelectedRecordsWorkflowLauncherService getSelectedRecordsLauncherService()
	{
		return new SelectedRecordsWorkflowLauncherService();
	}

	// override this method if you are extending the SelectedRecordsWorkflowLauncherServicePermissionRule class
	public SelectedRecordsWorkflowLauncherServicePermissionRule getSelectedRecordsWorkflowLauncherServicePermissionRule()
	{
		return new SelectedRecordsWorkflowLauncherServicePermissionRule(this);
	}

	public Path getTablePath()
	{
		return tablePath;
	}

	public void setTablePath(Path tablePath)
	{
		this.tablePath = tablePath;
	}

	public Path getRecordIdPath()
	{
		return recordIdPath;
	}

	public void setRecordIdPath(Path recordIdPath)
	{
		this.recordIdPath = recordIdPath;
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
	public boolean isCheckIfRecordHasActiveWorkflow()
	{
		return checkIfRecordHasActiveWorkflow;
	}

	public void setCheckIfRecordHasActiveWorkflow(boolean checkIfRecordHasActiveWorkflow)
	{
		this.checkIfRecordHasActiveWorkflow = checkIfRecordHasActiveWorkflow;
	}

	public boolean isLaunchSeperateWorkflowForEachRecord()
	{
		return launchSeperateWorkflowForEachRecord;
	}

	public void setLaunchSeperateWorkflowForEachRecord(boolean launchSeperateWorkflowForEachRecord)
	{
		this.launchSeperateWorkflowForEachRecord = launchSeperateWorkflowForEachRecord;
	}

	public Integer getMaxNumberOfSelectedRecords()
	{
		return maxNumberOfSelectedRecords;
	}

	public void setMaxNumberOfSelectedRecords(Integer maxNumberOfSelectedRecords)
	{
		this.maxNumberOfSelectedRecords = maxNumberOfSelectedRecords;
	}

}
