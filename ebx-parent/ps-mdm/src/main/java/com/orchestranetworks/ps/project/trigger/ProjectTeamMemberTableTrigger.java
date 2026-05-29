/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
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
public abstract class ProjectTeamMemberTableTrigger extends BaseTableTriggerEnforcingPermissions
	implements ProjectPathCapable
{
	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);

		ProjectPathConfig pathConfig = getProjectPathConfig();
		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();

		Path calculatedUserIdPath = pathConfig.getProjectTeamMemberCalculatedUserIdFieldPath();
		if (calculatedUserIdPath != null)
		{
			copyUserId(vc);
		}

		Path calculatedProjectRoleNamePath = pathConfig
			.getProjectTeamMemberCalculatedProjectRoleNameFieldPath();
		if (calculatedProjectRoleNamePath != null)
		{
			Object roleName = vc.getValue(pathConfig.getProjectTeamMemberProjectRoleFieldPath());
			vc.setValueEnablingPrivilegeForNode(roleName, calculatedProjectRoleNamePath);
		}
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		super.handleBeforeModify(context);

		ProjectPathConfig pathConfig = getProjectPathConfig();
		if (pathConfig.getProjectTeamMemberCalculatedUserIdFieldPath() != null
			&& context.getChanges()
				.getChange(pathConfig.getProjectTeamMemberUserFieldPath()) != null)
		{
			copyUserId(context.getOccurrenceContextForUpdate());
		}
	}

	private void copyUserId(ValueContextForUpdate valueContext)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Object userId = valueContext.getValue(pathConfig.getProjectTeamMemberUserFieldPath());
		valueContext.setValueEnablingPrivilegeForNode(
			userId,
			pathConfig.getProjectTeamMemberCalculatedUserIdFieldPath());
	}

	@Override
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return new ProjectTeamMemberTriggerActionValidator(getProjectPathConfig());
	}

	protected class ProjectTeamMemberTriggerActionValidator
		extends
		ProjectRecordMatchesTriggerActionValidator
	{
		private ProjectPathConfig projectPathConfig;

		public ProjectTeamMemberTriggerActionValidator(ProjectPathConfig projectPathConfig)
		{
			super(projectPathConfig.getProjectTeamMemberProjectFieldPath());
			this.projectPathConfig = projectPathConfig;
		}

		@Override
		public ProjectPathConfig getProjectPathConfig()
		{
			return projectPathConfig;
		}

		@Override
		public UserMessage validateTriggerAction(
			Session session,
			ValueContext valueContext,
			ValueChanges valueChanges,
			TriggerAction action)
			throws OperationException
		{
			UserMessage msg = super.validateTriggerAction(
				session,
				valueContext,
				valueChanges,
				action);
			if (msg == null && action == TriggerAction.DELETE
				&& !projectPathConfig.isProjectTeamMemberDeletionAllowed()
				&& session.getInteraction(true) != null)
			{
				msg = UserMessage.createError("Can't delete a project team member record.");
			}
			return msg;
		}
	}
}
