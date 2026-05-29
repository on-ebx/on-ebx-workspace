package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.schema.trigger.*;

/**
 * A trigger that prevents creates or deletes for workflow steps based on the permissions users.
 * Multiple users can be specified by separating them with a semicolon (;).
 * 
 * Service permission rules should be used to prevent creates and deletes, but those don't get invoked
 * when importing from a file. That's why this is sometimes needed.
 */
public class PreventActionForPermissionUsersTableTrigger
	extends
	BaseTableTriggerEnforcingPermissions
{
	private String permissionsUsers;
	private boolean preventCreate;
	private boolean preventDelete;

	private String[] permissionsUsersArr;
	private TriggerAction[] triggerActions;

	@Override
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return new WorkflowTaskTriggerActionValidator(triggerActions, permissionsUsersArr);
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);

		if (permissionsUsers == null)
		{
			context.addError("permissionsUsers must be specified.");
		}
		else
		{
			// Create the array of user names by splitting it with a ;
			permissionsUsersArr = permissionsUsers
				.split(WorkflowAccessRule.TRACKING_INFO_PERMISSIONS_USERS_SEPARATOR);
			if (permissionsUsersArr.length == 0)
			{
				context.addError("permissionsUsers must contain at least one user name.");
			}
		}

		if (!(preventCreate || preventDelete))
		{
			context.addError("Either preventCreate or preventDelete must be true.");
		}
		else
		{
			// Create a set of the actions represented by the booleans
			Set<TriggerAction> triggerActionSet = new HashSet<>();
			if (preventCreate)
			{
				triggerActionSet.add(TriggerAction.CREATE);
			}
			if (preventDelete)
			{
				triggerActionSet.add(TriggerAction.DELETE);
			}
			// Convert the set to an array
			triggerActions = triggerActionSet.toArray(new TriggerAction[triggerActionSet.size()]);
		}
	}

	public String getPermissionsUsers()
	{
		return permissionsUsers;
	}

	public void setPermissionsUsers(String permissionsUsers)
	{
		this.permissionsUsers = permissionsUsers;
	}

	public boolean isPreventCreate()
	{
		return preventCreate;
	}

	public void setPreventCreate(boolean preventCreate)
	{
		this.preventCreate = preventCreate;
	}

	public boolean isPreventDelete()
	{
		return preventDelete;
	}

	public void setPreventDelete(boolean preventDelete)
	{
		this.preventDelete = preventDelete;
	}
}
