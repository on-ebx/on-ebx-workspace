package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Declares service to update an existing record in the given table using the the default Workflow Launcher Service
 * 
 * To Register this from your Schema Extension, specify the following:
 * 
 * 		schemaExtensionsContext.registerUserService(
 *			new UpdateRecordWorkflowLauncherServiceDeclaration(serviceName,        // Required   
 *															   serviceLabel,       // Optional, will default to the serviceName
 *                                                             serviceDescription,
 *                                                             confirmBeforeLaunchMessage,  
 *                                                               // specify UpdateRecordWorkflowLauncherServiceDeclaration.DEFAULT_CONFIRM for default confirmation message
 *                                                               // leave null for no confirmation
 *                                                             workflowName,      // Required 
 *															   tablePath)         // Required
 *
 *      By Default, will prevent selection of records that are used in an active instance of this workflow
 *       -- use setter before registering the user service to turn off this feature if desired	
 *
 *      Also by Default, will use the default “Update <table label> Record” workflow instance naming convention (<record label> Update <table label> Record, launched by <userid>)
 *       -- use setter, setWorkflowInstanceName before registering the user service to assign alternate workflow instance name if desired	
 *
 * * 
 */

public class UpdateRecordWorkflowLauncherServiceDeclaration
	extends
	GenericServiceDeclaration<RecordEntitySelection, ActivationContextOnRecord>
	implements UserServiceDeclaration.OnRecord
{

	protected String workflowName;
	protected Path tablePath;
	private String workflowInstanceName = ""; // will trigger default workflow instance update record workflow naming
												// use setter before registering the user service to assign alternate workflow instance name if desired
	private boolean checkIfRecordHasActiveWorkflow = true; // will prevent selection of records that are used in an active instance of this workflow
															// use setter before registering the user service to set false if desired

	public UpdateRecordWorkflowLauncherServiceDeclaration(
		String serviceName,
		String serviceLabel,
		String serviceDescription,
		String confirmBeforeLaunchMessage,
		String workflowName,
		Path tablePath)
	{
		super(
			ServiceKey.forName(serviceName),
			null,
			serviceLabel,
			serviceDescription,
			confirmBeforeLaunchMessage);
		this.workflowName = workflowName;
		this.tablePath = tablePath;

	}

	@Override
	public UserService<RecordEntitySelection> createUserService()
	{
		WorkflowLauncherService<RecordEntitySelection> service = getWorkflowLauncherService();
		service.setXpathToTable(tablePath.format());
		service.setWorkflowName(workflowName);
		service.setWorkflowInstanceName(workflowInstanceName);
		return service;
	}

	@Override
	public void defineActivation(ActivationContextOnRecord context)
	{
		// Declare it on the specified table 
		context.includeSchemaNodesMatching(tablePath);

		// By default it's disabled unless enabled in dataset permissions
		context.setDefaultPermission(UserServicePermission.getDisabled());

		// Enable it only when outside a workflow and optionally, for Records that do not have an Active instance of this Workflow
		context.setPermissionRule(getUpdateRecordWorkflowLauncherServicePermissionRule());
	}

	// override this method if you are extending the WorkflowLauncherService class
	public WorkflowLauncherService<RecordEntitySelection> getWorkflowLauncherService()
	{
		return new WorkflowLauncherService<>();
	}

	// override this method if you are extending the UpdateRecordWorkflowLauncherServicePermissionRule class
	public UpdateRecordWorkflowLauncherServicePermissionRule getUpdateRecordWorkflowLauncherServicePermissionRule()
	{
		return new UpdateRecordWorkflowLauncherServicePermissionRule(this);
	}

	public String getWorkflowInstanceName()
	{
		return workflowInstanceName;
	}

	public void setWorkflowInstanceName(String workflowInstanceName)
	{
		this.workflowInstanceName = workflowInstanceName;
	}

	public String getWorkflowName()
	{
		return workflowName;
	}

	public void setWorkflowName(String workflowName)
	{
		this.workflowName = workflowName;
	}

	public Path getTablePath()
	{
		return tablePath;
	}

	public void setTablePath(Path tablePath)
	{
		this.tablePath = tablePath;
	}

	public boolean isCheckIfRecordHasActiveWorkflow()
	{
		return checkIfRecordHasActiveWorkflow;
	}

	public void setCheckIfRecordHasActiveWorkflow(boolean checkIfRecordHasActiveWorkflow)
	{
		this.checkIfRecordHasActiveWorkflow = checkIfRecordHasActiveWorkflow;
	}

}
