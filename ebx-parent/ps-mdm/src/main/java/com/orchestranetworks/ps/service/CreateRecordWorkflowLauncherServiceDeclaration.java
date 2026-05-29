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
 * Declares service to create a new record in the given table using the the default Workflow Launcher Service
 * 
 * To Register this from your Schema Extension, specify the following:
 * 
 * 		schemaExtensionsContext.registerUserService(
 *			new CreateRecordWorkflowLauncherServiceDeclaration(serviceName,        // Required   
 *															   serviceLabel,       // Optional, will default to the serviceName
 *                                                             serviceDescription,
 *                                                             confirmBeforeLaunchMessage,  
 *                                                               // specify CreateRecordWorkflowLauncherServiceDeclaration.DEFAULT_CONFIRM for default confirmation message
 *                                                               // leave null for no confirmation
 *                                                             workflowName,      // Required 
 *															   tablePath)         // Required
 *
 *      By Default, will use the default “Create New <table label> Record” workflow instance naming convention (<record label once created> Create New <table label> Record, launched by <userid>)
 *       -- use setter, setWorkflowInstanceName before registering the user service to assign alternate workflow instance name if desired	
 *
* * 
 */

public class CreateRecordWorkflowLauncherServiceDeclaration
	extends
	GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
	implements UserServiceDeclaration.OnTableView
{

	protected String workflowName;
	protected Path tablePath;
	private String workflowInstanceName = ""; // will trigger default workflow instance create record workflow naming
												// use setter before registering the user service to assign alternate workflow instance name

	public CreateRecordWorkflowLauncherServiceDeclaration(
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
	public UserService<TableViewEntitySelection> createUserService()
	{
		WorkflowLauncherService<TableViewEntitySelection> service = getWorkflowLauncherService();
		service.setXpathToTable(tablePath.format());
		service.setWorkflowName(workflowName);
		service.setWorkflowInstanceName(workflowInstanceName);
		return service;
	}

	@Override
	public void defineActivation(ActivationContextOnTableView context)
	{
		// Declare it on the specified table 
		context.includeSchemaNodesMatching(tablePath);

		// By default it's disabled unless enabled in dataset permissions
		context.setDefaultPermission(UserServicePermission.getDisabled());

		// Disable it when already inside a Workflow
		MasterOrChildDataSpaceOnlyServicePermissionRule<TableViewEntitySelection> servicePermissionRule = new MasterOrChildDataSpaceOnlyServicePermissionRule<>();
		servicePermissionRule.setAllowInsideWorkflow(false);
		context.setPermissionRule(servicePermissionRule);
	}

	// override this method if you are extending the WorkflowLauncherService class
	public WorkflowLauncherService<TableViewEntitySelection> getWorkflowLauncherService()
	{
		return new WorkflowLauncherService<>();
	}

	public String getWorkflowInstanceName()
	{
		return workflowInstanceName;
	}

	public void setWorkflowInstanceName(String workflowInstanceName)
	{
		this.workflowInstanceName = workflowInstanceName;
	}

}
