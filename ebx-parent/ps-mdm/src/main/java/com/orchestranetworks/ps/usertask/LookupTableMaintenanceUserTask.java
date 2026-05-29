package com.orchestranetworks.ps.usertask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public abstract class LookupTableMaintenanceUserTask extends GeneralMaintenanceUserTask
{
	protected static final String MULTI_WORKFLOW_TASK_SEPARATOR = "&";
	protected static final String MULTI_PERMISSIONS_USER_SEPARATOR = "&";

	protected String workflowTask;
	protected String permissionsUser;

	protected abstract AdaptationTable getLookupTable(DataContext context, Repository repo);

	protected abstract PrimaryKey[] getLookupTablePrimaryKeys(
		AdaptationTable lookupTable,
		DataContext context);

	protected abstract Path getLookupTableRoleFieldPath(AdaptationTable lookupTable);

	@Override
	protected List<Role> getRolesForUserTask(DataContext context, Repository repo)
		throws OperationException
	{
		AdaptationTable lookupTable = getLookupTable(context, repo);

		// Allow for possibility that a Subclass many not be using a lookup Table (i.e a subclass of
		// Project)
		if (lookupTable == null)
		{
			return super.getRolesForUserTask(context, repo);
		}
		List<Role> roles = getRolesFromLookupTable(
			lookupTable,
			getLookupTablePrimaryKeys(lookupTable, context),
			getLookupTableRoleFieldPath(lookupTable));
		if (roles.isEmpty())
		{
			roles = super.getRolesForUserTask(context, repo);
		}
		return roles;
	}

	protected List<Role> getRolesFromLookupTable(
		AdaptationTable lookupTable,
		PrimaryKey[] primaryKeys,
		Path rolePath)
		throws OperationException
	{
		List<Role> roles = new ArrayList<>();
		for (PrimaryKey primaryKey : primaryKeys)
		{
			Adaptation record = lookupTable.lookupAdaptationByPrimaryKey(primaryKey);
			if (record == null)
			{
				throw OperationException.createError(
					"No role found for workflow role with key '" + primaryKey.format() + "'");
			}
			roles.add(createRoleFromLookupTableRecord(record, rolePath));
		}
		return roles;
	}

	protected Role createRoleFromLookupTableRecord(Adaptation record, Path rolePath)
	{
		return Role.forSpecificRole(record.getString(rolePath));
	}

	@Override
	protected void addUsersAndRoles(final UserTaskCreationContext context, final List<Role> roles)
		throws OperationException
	{
		String[] users = getUsersForMultiPermissionsUser();
		boolean multipleUsers = users.length > 1;
		if (multipleUsers && users.length != roles.size())
		{
			throw OperationException
				.createError("Number of permissions users not equal to number of roles specified.");
		}
		boolean wfSupportsMultipleUsers = context
			.isVariableDefined(WorkflowConstants.PARAM_CURRENT_PERMISSIONS_USER);
		for (int i = 0; i < roles.size(); i++)
		{
			Role role = roles.get(i);
			if (wfSupportsMultipleUsers)
			{
				if (multipleUsers)
				{
					context.setVariableString(
						WorkflowConstants.PARAM_CURRENT_PERMISSIONS_USER,
						users[i]);
				}
				else if (permissionsUser != null)
				{
					context.setVariableString(
						WorkflowConstants.PARAM_CURRENT_PERMISSIONS_USER,
						users[0]);
				}
			}
			addUserAndRole(context, role);
		}
		if (wfSupportsMultipleUsers)
		{
			context.setVariableString(WorkflowConstants.PARAM_CURRENT_PERMISSIONS_USER, null);
		}
	}

	protected String[] getTasksForMultiWorkflowTask()
	{
		return workflowTask == null ? new String[0]
			: workflowTask.split(MULTI_WORKFLOW_TASK_SEPARATOR);
	}

	protected String[] getUsersForMultiPermissionsUser()
	{
		return permissionsUser == null ? new String[0]
			: permissionsUser.split(MULTI_PERMISSIONS_USER_SEPARATOR);
	}

	public String getWorkflowTask()
	{
		return workflowTask;
	}

	public void setWorkflowTask(String workflowTask)
	{
		this.workflowTask = workflowTask;
	}

	public String getPermissionsUser()
	{
		return this.permissionsUser;
	}

	public void setPermissionsUser(String permissionsUser)
	{
		this.permissionsUser = permissionsUser;
	}
}
