/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */


/**
 */
public class CopyUserAndRoleTableTrigger extends BaseTableTriggerEnforcingPermissions
{
	private Path roleFKPath;
	private Path roleNamePath;
	private Path userFKPath;
	private Path userIdPath;

	@Override
	public void setup(TriggerSetupContext context)
	{
		if (roleFKPath == null && userFKPath == null)
		{
			context.addError("Either roleFKPath or userFKPath must be specified.");
		}
		else
		{
			if (roleFKPath != null)
			{
				if (context.getSchemaNode().getNode(roleFKPath) == null)
				{
					context.addError("roleFKPath " + roleFKPath.format() + " does not exist.");
				}

				if (roleNamePath == null)
				{
					context.addError("roleNamePath must be specified if roleFKPath is specified.");
				}
				else if (context.getSchemaNode().getNode(roleNamePath) == null)
				{
					context.addError("roleNamePath " + roleNamePath.format() + " does not exist.");
				}
			}
			if (userFKPath != null)
			{
				if (context.getSchemaNode().getNode(userFKPath) == null)
				{
					context.addError("userFKPath " + userFKPath.format() + " does not exist.");
				}

				if (userIdPath == null)
				{
					context.addError("userIdPath must be specified if userFKPath is specified.");
				}
				else if (context.getSchemaNode().getNode(userIdPath) == null)
				{
					context.addError("userIdPath " + userIdPath.format() + " does not exist.");
				}
			}
		}
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);

		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();
		if (roleFKPath != null)
		{
			handleRoleName(vc);
		}
		if (userFKPath != null)
		{
			handleUserId(vc);
		}
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		super.handleBeforeModify(context);

		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();
		if (roleFKPath != null && context.getChanges().getChange(roleFKPath) != null)
		{
			handleRoleName(vc);
		}
		if (userFKPath != null && context.getChanges().getChange(userFKPath) != null)
		{
			handleUserId(vc);
		}
	}

	private void handleRoleName(ValueContextForUpdate vc) throws OperationException
	{
		Object roleName = vc.getValue(roleFKPath);
		vc.setValueEnablingPrivilegeForNode(roleName, roleNamePath);
	}

	private void handleUserId(ValueContextForUpdate vc) throws OperationException
	{
		Object userId = vc.getValue(userFKPath);
		vc.setValueEnablingPrivilegeForNode(userId, userIdPath);
	}

	public String getRoleFKPath()
	{
		return this.roleFKPath.format();
	}

	public void setRoleFKPath(String roleFKPath)
	{
		this.roleFKPath = Path.parse(roleFKPath);
	}

	public String getRoleNamePath()
	{
		return this.roleNamePath.format();
	}

	public void setRoleNamePath(String roleNamePath)
	{
		this.roleNamePath = Path.parse(roleNamePath);
	}

	public String getUserFKPath()
	{
		return this.userFKPath.format();
	}

	public void setUserFKPath(String userFKPath)
	{
		this.userFKPath = Path.parse(userFKPath);
	}

	public String getUserIdPath()
	{
		return this.userIdPath.format();
	}

	public void setUserIdPath(String userIdPath)
	{
		this.userIdPath = Path.parse(userIdPath);
	}
}
