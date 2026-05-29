/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.actionpermissions;

import com.orchestranetworks.workflow.*;

/**
 */
public interface ProjectWorkflowPermissionsInfoFactory
{
	ProjectWorkflowPermissionsInfo createPermissionsInfo(ProcessInstance processInstance);
}
