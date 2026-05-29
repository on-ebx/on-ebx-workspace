package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.misc.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * This is a service that is only allowed from within Workflow Steps that use one of the specified Permission Profiles
 */
public class WorkflowPermissionProfileOnlyServicePermission
	extends
	MasterOrChildDataSpaceOnlyServicePermission
{

	// can take a String with a collection of space separated Permission Profiles
	protected String workflowPermissionProfile = null;
	protected List<String> workflowPermissionProfileList = null;

	public WorkflowPermissionProfileOnlyServicePermission()
	{
		super();
		allowInsideWorkflow = true;
		allowOutsideWorkflow = false;
	}

	@Override
	public ActionPermission getPermission(
		SchemaNode schemaNode,
		Adaptation adaptation,
		Session session)
	{
		if (workflowPermissionProfileList != null && workflowPermissionProfileList
			.contains(WorkflowUtilities.getTrackingInfoPermissionsUser(session)))
		{
			return super.getPermission(schemaNode, adaptation, session);
		}
		return ActionPermission.getDisabled();

	}

	public String getWorkflowPermissionProfile()
	{
		return workflowPermissionProfile;
	}

	public void setWorkflowPermissionProfile(String workflowPermissionProfile)
	{
		this.workflowPermissionProfile = workflowPermissionProfile;
		String[] profileArray = StringUtils.splitByWhitespace(workflowPermissionProfile);
		this.workflowPermissionProfileList = Arrays.asList(profileArray);
	}

}
