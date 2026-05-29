/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.workflow;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class BasicSubWorkflowsInvocationBean extends SubWorkflowsInvocationBean
{
	protected abstract List<String> determineSubWorkflowModels(DataContextReadOnly dataContext);

	protected abstract String getSubWorkflowLabel(
		ProcessLauncher launcher,
		DataContextReadOnly dataContext);

	protected abstract String getSubWorkflowDescription(
		ProcessLauncher launcher,
		DataContextReadOnly dataContext);

	@Override
	public void handleCreateSubWorkflows(SubWorkflowsCreationContext context)
		throws OperationException
	{
		List<String> subWorkflowModels = determineSubWorkflowModels(context);
		for (int i = 0; i < subWorkflowModels.size(); i++)
		{
			String subWorkflowModel = subWorkflowModels.get(i);
			ProcessLauncher launcher = context
				.registerSubWorkflow(AdaptationName.forName(subWorkflowModel), "subworkflow" + i);
			mapSubWorkflowInput(launcher, context);

			String label = getSubWorkflowLabel(launcher, context);
			if (label != null)
			{
				launcher.setLabel(UserMessage.createInfo(label));
			}
			String description = getSubWorkflowDescription(launcher, context);
			if (description != null)
			{
				launcher.setDescription(UserMessage.createInfo(description));
			}
		}
		context.launchSubWorkflows();
	}

	@Override
	public void handleCompleteAllSubWorkflows(SubWorkflowsCompletionContext context)
		throws OperationException
	{
		List<ProcessInstance> subWorkflows = context.getCompletedSubWorkflows();
		for (ProcessInstance subWorkflow : subWorkflows)
		{
			mapSubWorkflowOutput(subWorkflow, context);
		}
	}

	protected void mapSubWorkflowInput(ProcessLauncher launcher, DataContextReadOnly dataContext)
	{
		Set<String> paramsToMap = getInputParametersToMap(dataContext);
		for (String param : paramsToMap)
		{
			if (dataContext.isVariableDefined(param) && launcher.isDefinedAsInputParameter(param))
			{
				launcher.setInputParameter(param, dataContext.getVariableString(param));
			}
		}
	}

	protected void mapSubWorkflowOutput(ProcessInstance processInstance, DataContext dataContext)
	{
		DataContextReadOnly subWorkflowDataContext = processInstance.getDataContext();
		Set<String> paramsToMap = getOutputParametersToMap(dataContext);
		for (String param : paramsToMap)
		{
			if (dataContext.isVariableDefined(param)
				&& subWorkflowDataContext.isVariableDefined(param))
			{
				if (param.equals(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES))
				{
					// need special logic to update CurrentUsersForRoles in the Main Workflow Data Context with changes from the Subworkflow Data Context 
					updateCurrentUsersForRoles(dataContext, subWorkflowDataContext);
				}
				else
				{
					dataContext
						.setVariableString(param, subWorkflowDataContext.getVariableString(param));
				}
			}
		}
	}

	protected Set<String> getInputParametersToMap(DataContextReadOnly dataContext)
	{
		HashSet<String> params = new HashSet<>();

		params.add(WorkflowConstants.PARAM_MASTER_DATA_SPACE);
		params.add(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
		params.add(WorkflowConstants.PARAM_DATA_SET);
		params.add(WorkflowConstants.PARAM_XPATH_TO_TABLE);
		params.add(WorkflowConstants.PARAM_RECORD);
		params.add(WorkflowConstants.PARAM_RECORD_NAME_VALUE);
		params.add(WorkflowConstants.PARAM_CURRENT_USER_ID);
		params.add(WorkflowConstants.PARAM_CURRENT_USER_LABEL);
		params.add(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES);
		params.add(WorkflowConstants.PARAM_USER_TASK_CREATE_DATE_TIME);
		return params;
	}

	protected Set<String> getOutputParametersToMap(DataContextReadOnly dataContext)
	{
		HashSet<String> params = new HashSet<>();

		params.add(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
		params.add(WorkflowConstants.PARAM_RECORD);
		params.add(WorkflowConstants.PARAM_RECORD_NAME_VALUE);
		params.add(WorkflowConstants.PARAM_CURRENT_USER_ID);
		params.add(WorkflowConstants.PARAM_CURRENT_USER_LABEL);
		params.add(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES);
		params.add(WorkflowConstants.PARAM_USER_TASK_CREATE_DATE_TIME);
		return params;
	}

	// update the CurrentUsersForRoles in the Main Workflow Data Context with changes from the Subworkflow Data Context
	private void updateCurrentUsersForRoles(
		DataContext dataContext,
		DataContextReadOnly subWorkflowDataContext)
	{
		String currentUsersForRoles = dataContext
			.getVariableString(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES);
		List<String> currentUserForRoleList = CollectionUtils.splitString(
			currentUsersForRoles,
			WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_DELIMITER);

		String subworkflowUsersForRoles = subWorkflowDataContext
			.getVariableString(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES);
		List<String> subworkflowUserForRoleList = CollectionUtils.splitString(
			subworkflowUsersForRoles,
			WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_DELIMITER);

		for (String subworkflowUserForRole : subworkflowUserForRoleList)
		{
			List<String> subworkflowRoleAndUser = CollectionUtils.splitString(
				subworkflowUserForRole,
				WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER);
			String subworkflowRole = subworkflowRoleAndUser.get(0);
			String subworkflowUserId = subworkflowRoleAndUser.get(1);

			// if the subworkflow user is already assigned as the current user for this role, then continue to next subworkflow role
			if (!subworkflowUserId
				.equals(getUserIdForRole(subworkflowRole, currentUserForRoleList)))
			{
				// Otherwise, we are ready to update the Current Users for Roles Data Context Variable 
				// -- First, find and remove the Role/User entry from the current list of Users for Roles
				for (String currentUserForRole : currentUserForRoleList)
				{
					List<String> roleAndUser = CollectionUtils.splitString(
						currentUserForRole,
						WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER);
					if (subworkflowRole.equals(roleAndUser.get(0)))
					{
						currentUserForRoleList.remove(currentUserForRole);
						break;
					}
				}
				// -- Then, add the subworkflow Role/User entry to the current list of Users for Roles
				currentUserForRoleList.add(
					subworkflowRole + WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER
						+ subworkflowUserId);
				dataContext.setVariableString(
					WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES,
					StringUtils.join(
						currentUserForRoleList,
						WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_DELIMITER));
			}
		}
	}

	protected String getUserIdForRole(String roleName, List<String> currentUserForRoleList)
	{
		for (String currentUserForRole : currentUserForRoleList)
		{
			List<String> roleAndUser = CollectionUtils.splitString(
				currentUserForRole,
				WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER);
			if (roleName.equals(roleAndUser.get(0)))
			{
				return roleAndUser.get(1);
			}
		}
		return null;
	}

}
