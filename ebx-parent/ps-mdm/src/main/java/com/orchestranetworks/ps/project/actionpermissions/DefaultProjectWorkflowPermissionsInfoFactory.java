/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.actionpermissions;

import com.orchestranetworks.workflow.*;

/**
 */
public class DefaultProjectWorkflowPermissionsInfoFactory
	implements ProjectWorkflowPermissionsInfoFactory
{
	private static DefaultProjectWorkflowPermissionsInfoFactory instance;

	public static DefaultProjectWorkflowPermissionsInfoFactory getInstance()
	{
		if (instance == null)
		{
			instance = new DefaultProjectWorkflowPermissionsInfoFactory();
		}
		return instance;
	}

	@Override
	public ProjectWorkflowPermissionsInfo createPermissionsInfo(ProcessInstance processInstance)
	{
		return new ProjectWorkflowPermissionsInfo(processInstance);
	}

	private DefaultProjectWorkflowPermissionsInfoFactory()
	{
	}
}
