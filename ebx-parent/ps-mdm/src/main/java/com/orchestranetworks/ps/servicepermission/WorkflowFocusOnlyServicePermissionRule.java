package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Only allows a service when it's on the table or record that is the focus of a workflow.
 * 
 * If the rule is on a {@link RecordEntitySelection}, then it will only be enabled when
 * the selection's record is the same as the one specified by the xpath parameter into the
 * user task.
 * 
 * Otherwise, it will only be enabled when the selection's table is the same as the one specified
 * by the xpath parameter into the user task.
 * 
 * If the user task references a data set (i.e. the xpath is not specified), then it will be disabled.
 * 
 * This extends {@link MasterOrChildDataSpaceOnlyServicePermissionRule}, so it can be enabled/disabled
 * based on if the data space is a master or child as well.
 */
public class WorkflowFocusOnlyServicePermissionRule<S extends TableEntitySelection>
	extends
	MasterOrChildDataSpaceOnlyServicePermissionRule<S>
{
	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
	{
		// Super class will determine if it should be disabled based on being a master or child data space
		UserServicePermission permission = super.getPermission(context);
		if (UserServicePermission.getDisabled().equals(permission))
		{
			return permission;
		}
		// If not inside a workflow, should be disabled
		Session session = context.getSession();
		if (!session.isInWorkflowInteraction(true))
		{
			return UserServicePermission.getDisabled(
				UserMessage.createError("Service only allowed when inside of a workflow."));
		}

		String xpath = WorkflowUtilities
			.getSessionInteractionParameter(session, WorkflowConstants.SESSION_PARAM_XPATH);
		if (xpath != null)
		{
			S selection = context.getEntitySelection();
			if (selection instanceof RecordEntitySelection)
			{
				Repository repo = selection.getDataspace().getRepository();
				Adaptation workflowRecord = null;
				try
				{
					workflowRecord = WorkflowUtilities
						.getRecordFromSessionInteraction(session.getInteraction(true), repo);
				}
				catch (OperationException ex)
				{
					// If an error occurred looking up the record, then just print an error and continue,
					// which will lead to it being disabled.
					LoggingCategory.getKernel().error(
						"Error retrieving workflow record in order to evaluate service permissions. xpath = "
							+ xpath,
						ex);
				}
				// Enabled if the selection's record is the same as the workflow's
				if (workflowRecord != null
					&& workflowRecord.equals(((RecordEntitySelection) selection).getRecord()))
				{
					return UserServicePermission.getEnabled();
				}
			}
			else
			{
				// Enabled if the selection's table is the same as the workflow's
				AdaptationTable selectedTable = context.getEntitySelection().getTable();
				if (selectedTable != null && xpath.equals(selectedTable.getTablePath().format()))
				{
					return UserServicePermission.getEnabled();
				}
			}
		}
		return UserServicePermission.getDisabled(
			UserMessage.createError("Service only allowed on table that is focus of workflow."));
	}
}