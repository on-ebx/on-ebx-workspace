package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * A rule that only allows a service from within a workflow step for the specified permission profiles.
 * By default, this rule is available to be invoked inside a workflow but not outside a workflow.
 */
public class WorkflowPermissionProfileOnlyServicePermissionRule<S extends DataspaceEntitySelection>
	extends
	MasterOrChildDataSpaceOnlyServicePermissionRule<S>
{
	protected List<String> workflowPermissionProfiles;

	public WorkflowPermissionProfileOnlyServicePermissionRule()
	{
		this(null);
	}

	public WorkflowPermissionProfileOnlyServicePermissionRule(
		List<String> workflowPermissionProfiles)
	{
		this.workflowPermissionProfiles = workflowPermissionProfiles;
		allowInsideWorkflow = true;
		allowOutsideWorkflow = false;
	}

	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
	{
		String trackingInfoUser = WorkflowUtilities
			.getTrackingInfoPermissionsUser(context.getSession());
		if (trackingInfoUser != null && workflowPermissionProfiles != null
			&& workflowPermissionProfiles.contains(trackingInfoUser))
		{
			return super.getPermission(context);
		}
		return UserServicePermission.getDisabled();
	}

	public List<String> getWorkflowPermissionProfiles()
	{
		return workflowPermissionProfiles;
	}

	public void setWorkflowPermissionProfiles(List<String> workflowPermissionProfiles)
	{
		this.workflowPermissionProfiles = workflowPermissionProfiles;
	}
}
