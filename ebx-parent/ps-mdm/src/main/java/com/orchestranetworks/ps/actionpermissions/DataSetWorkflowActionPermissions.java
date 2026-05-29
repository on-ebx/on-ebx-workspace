/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.actionpermissions;

import com.orchestranetworks.workflow.*;

public abstract class DataSetWorkflowActionPermissions extends DefaultWorkflowActionPermissions
{
	protected abstract boolean isEntryRoleUser(ActionPermissionsOnWorkflowContext context);

	protected abstract boolean isApproverRoleUser(ActionPermissionsOnWorkflowContext context);

	@Override
	protected boolean canUserAllocate(ActionPermissionsOnWorkflowContext context)
	{
		return super.canUserAllocate(context) || isEntryRoleUser(context)
			|| isApproverRoleUser(context);
	}

	protected boolean canUserView(ActionPermissionsOnWorkflowContext context)
	{
		return super.canUserView(context) || isEntryRoleUser(context)
			|| isApproverRoleUser(context);
	}
}
