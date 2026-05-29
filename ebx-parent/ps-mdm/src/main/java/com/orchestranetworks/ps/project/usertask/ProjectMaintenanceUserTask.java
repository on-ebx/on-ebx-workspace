/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.usertask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.constants.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.util.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.usertask.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class ProjectMaintenanceUserTask extends LookupTableMaintenanceUserTask
	implements ProjectPathCapable
{

	protected boolean cancelable = false;
	protected boolean acceptIsCancel = false;
	protected boolean initialCreationTask = false;
	protected boolean cancelReasonRequired = true;

	protected String getProjectTeamRole()
	{
		return CommonProjectConstants.ROLE_PROJECT_TEAM;
	}

	// By default it always should if it's not the intial creation task
	// but this can be overridden with different logic (based on project type for example)
	protected boolean shouldSendToCancellationProcess(Adaptation projectRecord)
	{
		return projectRecord != null && !initialCreationTask;
	}

	@Override
	protected void addUserAndRole(UserTaskCreationContext context, Role userRole)
		throws OperationException
	{
		if (getProjectTeamRole().equals(userRole.getRoleName()))
		{
			addAllProjectTeamUsers(context);
		}
		else
		{
			super.addUserAndRole(context, userRole);
		}
	}

	@Override
	protected boolean isUserValidForAutoAllocate(
		UserTaskCreationContext context,
		Role role,
		UserReference user)
		throws OperationException
	{
		// If this is the initial creation task, then the user from the workflow is the one that
		// launched it and should therefore be valid for auto-allocate. (Already checked if he's
		// in the role before calling this.)
		if (initialCreationTask)
		{
			return true;
		}
		UserReference projectTeamMemberUser = getDefaultUserForRole(context, role);
		// The user is valid if there is no project team member user or there is and he is that user
		return projectTeamMemberUser == null || projectTeamMemberUser.equals(user);
	}

	// Assign the Default User from the Project Team
	@Override
	protected UserReference getDefaultUserForRole(UserTaskCreationContext context, Role role)
		throws OperationException
	{
		Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecord(
			context,
			context.getSession().getInteraction(true),
			context.getRepository(),
			getProjectPathConfig());
		if (projectRecord == null)
		{
			return null;
		}
		return getProjectTeamUserForRole(projectRecord, context, role);
	}

	// Assign the User from the Project Team
	protected UserReference getProjectTeamUserForRole(
		Adaptation projectRecord,
		ProcessExecutionContext context,
		Role userRole)
		throws OperationException
	{
		return ProjectUtil.getProjectTeamUserForRole(
			projectRecord,
			userRole,
			context.getSession(),
			context.getRepository(),
			getProjectPathConfig());
	}

	protected Adaptation getProjectTeamMemberRecord(Adaptation projectRecord, Role userRole)
	{
		return ProjectUtil
			.getProjectTeamMemberRecord(projectRecord, userRole, getProjectPathConfig());
	}

	@Override
	public void checkBeforeWorkItemCompletion(UserTaskBeforeWorkItemCompletionContext context)
	{
		super.checkBeforeWorkItemCompletion(context);
		if (cancelable && cancelReasonRequired && ((!acceptIsCancel && !context.isAcceptAction())
			|| (acceptIsCancel && context.isAcceptAction())))
		{
			UserMessage msg = checkCancelReasonEntered(
				context,
				context.getRepository(),
				context.getSession());
			if (msg != null)
			{
				context.reportMessage(msg);
			}
		}
	}

	protected UserMessage checkCancelReasonEntered(
		DataContextReadOnly context,
		Repository repository,
		Session session)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		SessionInteraction interaction = session.getInteraction(true);
		Adaptation projectRecord;
		try
		{
			projectRecord = ProjectWorkflowUtilities
				.getProjectRecord(context, interaction, repository, pathConfig);
		}
		catch (OperationException ex)
		{
			return UserMessage.createError("Error looking up project record.", ex);
		}

		// If we're sending to a cancellation process, require a comment
		if (shouldSendToCancellationProcess(projectRecord))
		{
			String cancelReason = projectRecord
				.getString(pathConfig.getProjectCancelReasonFieldPath());
			if (cancelReason == null)
			{
				String wfComment = interaction.getComment();
				if (wfComment == null)
				{
					return UserMessage
						.createError("A cancel reason must be entered when cancelling a project.");
				}
			}
		}
		return null;
	}

	@Override
	public void handleWorkItemCompletion(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		final Adaptation projectRecord = ProjectWorkflowUtilities
			.getProjectRecord(context, null, context.getRepository(), getProjectPathConfig());
		if (cancelable)
		{
			handleCancelableProject(
				context.getCompletedWorkItem(),
				projectRecord,
				context,
				context.getSession(),
				context.getRepository());
		}

		if (projectRecord != null)
		{
			final Role role = WorkflowUtilities
				.getWorkItemOfferedToRole(context.getCompletedWorkItem());
			final UserReference user = context.getCompletedWorkItem().getUserReference();
			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					updateProjectTeamUserForRole(projectRecord, role, user, pContext, false);
				}
			};
			ProcedureExecutor.executeProcedure(proc, context.getSession(), projectRecord.getHome());
		}

		updateTeamMembersInContext(projectRecord, context);

		super.handleWorkItemCompletion(context);
	}

	protected void handleCancelableProject(
		WorkItem workItem,
		Adaptation projectRecord,
		UserTaskWorkItemCompletionContext context,
		Session session,
		Repository repo)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		if ((!acceptIsCancel && workItem.isRejected()) || (acceptIsCancel && workItem.isAccepted()))
		{
			// If this is going to a cancellation process, we want to update the cancel reason
			if (shouldSendToCancellationProcess(projectRecord))
			{
				if (cancelReasonRequired)
				{
					Path cancelReasonPath = projectPathConfig.getProjectCancelReasonFieldPath();
					String cancelReason = projectRecord.getString(cancelReasonPath);
					if (cancelReason == null)
					{
						String comment = workItem.getComment();
						if (comment != null)
						{
							SchemaNode cancelReasonNode = projectRecord.getSchemaNode()
								.getNode(cancelReasonPath);
							SchemaFacetMaxLength maxLenFacet = cancelReasonNode.getFacetMaxLength();
							if (maxLenFacet != null)
							{
								int maxLen = maxLenFacet.getValue().intValue();
								if (comment.length() > maxLen)
								{
									comment = comment.substring(0, maxLen);
								}
							}

							ModifyValuesProcedure mvp = new ModifyValuesProcedure(projectRecord);
							mvp.setAllPrivileges(true);
							mvp.setValue(cancelReasonPath, comment);
							mvp.execute(session);
						}
					}
				}
			}
			// Otherwise delete the project record
			else
			{
				String projectType = context
					.getVariableString(ProjectWorkflowConstants.PARAM_PROJECT_TYPE);
				// For non-new subject projects, clear the current project type from the subject
				// records.
				// This should work whether the context contains a project record or a subject
				// record.
				if (shouldClearCurrentProjectTypeOnCancel(projectType))
				{
					ProjectWorkflowUtilities.clearCurrentProjectType(
						context,
						session,
						repo,
						projectPathConfig,
						projectPathConfig.getSubjectPathConfig(projectType));
				}

				// If there is a project record (could have been cancelled before project was
				// saved) then delete it. Trigger will do appropriate action based
				// on project type.
				if (projectRecord != null)
				{
					CascadeDeleter.invokeCascadeDelete(projectRecord, session);
				}
				// Clear out the record to indicate it was cancelled from first step
				context.setVariableString(WorkflowConstants.PARAM_RECORD, null);
			}
		}
		// Only set isCancelled in the data context if one or more work items were rejected or
		// logically cancelled
		boolean cancelled = (!acceptIsCancel && context.countRejectedWorkItems() > 0)
			|| (acceptIsCancel && context.countAcceptedWorkItems() > 0);
		context.setVariableString(
			ProjectWorkflowConstants.PARAM_IS_CANCELLED,
			String.valueOf(cancelled));
		// If not canceling and workflow will be moving on, then clear the cancel reason field
		if (!cancelled && context.checkAllWorkItemMatchStrategy())
		{
			ModifyValuesProcedure mvp = new ModifyValuesProcedure();
			mvp.setAdaptation(projectRecord);
			mvp.setAllPrivileges(true);
			mvp.setValue(projectPathConfig.getProjectCancelReasonFieldPath(), null);
			mvp.execute(session);
		}
	}

	protected boolean shouldClearCurrentProjectTypeOnCancel(String projectType)
	{
		return !getProjectPathConfig().isNewSubjectProjectType(projectType);
	}

	protected void updateProjectTeamUserForRole(
		Adaptation projectRecord,
		Role role,
		UserReference user,
		ProcedureContext pContext,
		boolean overwrite)
		throws OperationException
	{
		Adaptation teamMember = getProjectTeamMemberRecord(projectRecord, role);
		if (teamMember != null)
		{
			Path userIdPath = getProjectPathConfig().getProjectTeamMemberUserFieldPath();
			// If there's no user already assigned to this role for this project, assign the user
			// that just completed this task
			String teamMemberUserId = teamMember.getString(userIdPath);
			if (teamMemberUserId == null || overwrite)
			{
				ModifyValuesProcedure mvp = new ModifyValuesProcedure(teamMember);
				mvp.setAllPrivileges(true);
				mvp.setValue(userIdPath, user.getUserId());
				mvp.execute(pContext);
			}
		}
	}

	private void updateTeamMembersInContext(Adaptation projectRecord, DataContext dataContext)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		updateTeamMembersInContext(projectRecord, dataContext, pathConfig);
	}

	public static void updateTeamMembersInContext(
		Adaptation projectRecord,
		DataContext dataContext,
		ProjectPathConfig pathConfig)
	{
		StringBuilder bldr = new StringBuilder();
		List<Adaptation> teamMembers = AdaptationUtil
			.getLinkedRecordList(projectRecord, pathConfig.getProjectTeamMembersFieldPath());
		for (Adaptation teamMember : teamMembers)
		{
			String userId = teamMember.getString(pathConfig.getProjectTeamMemberUserFieldPath());
			if (userId != null)
			{
				bldr.append(userId);
				bldr.append(ProjectWorkflowConstants.PROJECT_TEAM_MEMBERS_PARAM_SEPARATOR);
			}
		}
		if (bldr.length() > 0)
		{
			bldr.deleteCharAt(bldr.length() - 1);
		}
		dataContext.setVariableString(
			ProjectWorkflowConstants.PARAM_PROJECT_TEAM_MEMBERS,
			bldr.toString());
	}

	private void addAllProjectTeamUsers(UserTaskCreationContext context) throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Adaptation projectRecord = ProjectWorkflowUtilities
			.getProjectRecord(context, null, context.getRepository(), pathConfig);
		List<Adaptation> teamMembers = AdaptationUtil
			.getLinkedRecordList(projectRecord, pathConfig.getProjectTeamMembersFieldPath());
		for (Adaptation teamMember : teamMembers)
		{
			String roleName = teamMember
				.getString(pathConfig.getProjectTeamMemberProjectRoleFieldPath());
			addUserAndRole(context, Role.forSpecificRole(roleName));
		}
	}

	protected Adaptation getProjectRecordFromUserTaskBeforeWorkItemCompletionContext(
		UserTaskBeforeWorkItemCompletionContext context)
	{
		try
		{
			return getProjectRecordFromDataContext(
				context,
				context.getSession(),
				context.getRepository());
		}
		catch (OperationException ex)
		{
			context.reportMessage(UserMessage.createError("Error looking up project record.", ex));
		}
		return null;
	}

	protected Adaptation getProjectRecordFromUserTaskWorkItemCompletionContext(
		UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		return getProjectRecordFromDataContext(
			context,
			context.getSession(),
			context.getRepository());
	}

	protected Adaptation getProjectRecordFromDataContext(
		DataContextReadOnly dataContext,
		Session session,
		Repository repo)
		throws OperationException
	{
		// Get the project record, from the session params (either created or xpath)
		return ProjectWorkflowUtilities.getProjectRecordFromSessionInteraction(
			session.getInteraction(true),
			WorkflowUtilities
				.getDataSet(dataContext, repo, WorkflowConstants.PARAM_WORKING_DATA_SPACE),
			getProjectPathConfig());
	}

	public boolean isCancelable()
	{
		return this.cancelable;
	}

	public void setCancelable(boolean cancelable)
	{
		this.cancelable = cancelable;
	}

	public boolean isAcceptIsCancel()
	{
		return this.acceptIsCancel;
	}

	public void setAcceptIsCancel(boolean acceptIsCancel)
	{
		this.acceptIsCancel = acceptIsCancel;
	}

	public boolean isInitialCreationTask()
	{
		return this.initialCreationTask;
	}

	public void setInitialCreationTask(boolean initialCreationTask)
	{
		this.initialCreationTask = initialCreationTask;
	}

	public boolean isCancelReasonRequired()
	{
		return this.cancelReasonRequired;
	}

	public void setCancelReasonRequired(boolean cancelReasonRequired)
	{
		this.cancelReasonRequired = cancelReasonRequired;
	}
}
