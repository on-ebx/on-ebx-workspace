/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.procedure.Procedures.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.util.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */

/**
 */
public abstract class ProjectTableTrigger extends BaseTableTriggerEnforcingPermissions
	implements ProjectPathCapable
{
	protected Map<Path, Object> getNewSubjectPathValueMap(
		AfterCreateOccurrenceContext context,
		String projectType)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);
		HashMap<Path, Object> pathValueMap = new HashMap<>();
		Path subjectNameFieldPath = subjectPathConfig.getSubjectNameFieldPath();
		if (subjectNameFieldPath != null)
		{
			Adaptation projectRecord = context.getAdaptationOccurrence();
			Path projectNamePath = projectPathConfig.getProjectNameFieldPath();
			String projectName = projectRecord.getString(projectNamePath);
			if (projectName != null)
			{
				AdaptationTable subjectTable = projectRecord.getContainer()
					.getTable(subjectPathConfig.getSubjectTablePath());
				SchemaNode subjectNameFieldNode = subjectTable.getTableOccurrenceRootNode()
					.getNode(subjectNameFieldPath);
				SchemaFacetMaxLength maxLenFacet = subjectNameFieldNode.getFacetMaxLength();
				if (maxLenFacet != null)
				{
					int maxLen = maxLenFacet.getValue().intValue();
					if (projectName.length() > maxLen)
					{
						projectName = projectName.substring(0, maxLen);
					}
				}
				pathValueMap.put(subjectNameFieldPath, projectName);
			}
		}
		Path projectSubjectFieldPath = projectPathConfig.getProjectSubjectFieldPath(projectType);
		if (projectSubjectFieldPath != null)
		{
			initNewSubjectCurrentProjectType(pathValueMap, projectType);
		}

		return pathValueMap;
	}

	/**
	 * Determine if a subject record should be created after the project record is created.
	 * By default, if it's a "new subject" project type then it should be created.
	 *
	 * @param projectType the project type
	 * @return whether a subject record should be created
	 */
	protected boolean shouldCreateSubject(String projectType)
	{
		return getProjectPathConfig().isNewSubjectProjectType(projectType);
	}

	/**
	 * Create the subject record
	 *
	 * @param context the trigger context
	 * @param projectType the project type
	 * @return the subject record
	 */
	protected Adaptation createSubject(AfterCreateOccurrenceContext context, String projectType)
		throws OperationException
	{
		AdaptationTable table = context.getAdaptationOccurrence()
			.getContainer()
			.getTable(
				getProjectPathConfig().getSubjectPathConfig(projectType).getSubjectTablePath());
		Map<Path, Object> pathValueMap = getNewSubjectPathValueMap(context, projectType);
		CreateRecordProcedure crp = new CreateRecordProcedure(table, pathValueMap);
		crp.setAllPrivileges(true);
		crp.execute(context.getProcedureContext());
		return crp.getCreatedRecord();
	}

	/**
	 * Determine whether the subject record should be deleted when the project is deleted
	 *
	 * @param projectType the project type
	 * @param projectStatus the project status
	 * @param subjectRecord the subject record
	 * @return whether the subject record should be deleted
	 */
	protected boolean shouldDeleteSubject(
		String projectType,
		String projectStatus,
		Adaptation subjectRecord)
	{
		if (subjectRecord == null)
		{
			return false;
		}
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		// If it's a New Subject project that's in-process
		if (projectPathConfig.isNewSubjectProjectType(projectType)
			&& projectPathConfig.isInProcessProjectStatus(projectStatus))
		{
			// Only delete it if there is no project still pointing to it.
			// At this point, the current project has already been deleted, so it won't be returned
			// by the selection node.
			SubjectPathConfig subjectPathConfig = projectPathConfig
				.getSubjectPathConfig(projectType);
			Path subjectProjectsFieldPath = subjectPathConfig.getSubjectProjectsFieldPath();
			if (subjectProjectsFieldPath == null)
			{
				return AdaptationUtil
					.getLinkedRecordList(
						subjectRecord,
						subjectPathConfig.getSubjectProjectSubjectsFieldPath())
					.isEmpty();
			}
			return AdaptationUtil.getLinkedRecordList(subjectRecord, subjectProjectsFieldPath)
				.isEmpty();
		}
		return false;
	}

	// We have to override this because there's no way to get the link table from an
	// AssociationLink,
	// so we can't do that in a generic fashion in BaseTableTrigger. That is scheduled to be
	// available in EBX 5.5.1. FOr now, we specify in the trigger config to ignore those
	// associations
	// and we handle them here. IF we didn't have to do that, we could probably rely on implementing
	// a special
	// cascade delete on ProjectSubject only.
	//
	@Override
	protected void cascadeDelete(Adaptation deletedRecord, AfterDeleteOccurrenceContext context)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		// We use the project type from the session param
		String projectType = deletedRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);
		if (subjectPathConfig != null)
		{
			String projectStatus = deletedRecord
				.getString(projectPathConfig.getProjectStatusFieldPath());
			ProcedureContext pContext = context.getProcedureContext();
			Path projectSubjectsFieldPath = projectPathConfig
				.getProjectProjectSubjectsFieldPath(projectType);
			// If there is no link table
			DeleteRecordProcedure drp = new DeleteRecordProcedure();
			drp.setAllPrivileges(true);
			if (projectSubjectsFieldPath == null)
			{
				Adaptation subjectRecord = AdaptationUtil.followFK(
					deletedRecord,
					projectPathConfig.getProjectSubjectFieldPath(projectType));
				if (!CascadeDeleteService.CASCADE_DELETE_SERVICE_TRACKING_INFO
					.equals(context.getSession().getTrackingInfo())
					&& shouldDeleteSubject(projectType, projectStatus, subjectRecord))
				{
					// Delete the subject record, which will in turn do a cascade delete
					drp.setAdaptation(subjectRecord);
					drp.execute(pContext);
				}
			}
			else
			{
				// Find all project subjects for this project.
				AdaptationTable projectSubjectTable = deletedRecord.getContainer()
					.getTable(subjectPathConfig.getProjectSubjectTablePath());
				RequestResult reqRes = projectSubjectTable.createRequestResult(
					subjectPathConfig.getProjectSubjectProjectFieldPath().format() + "='"
						+ deletedRecord.getOccurrencePrimaryKey().format() + "'");
				try
				{
					Adaptation projectSubjectRecord;
					while ((projectSubjectRecord = reqRes.nextAdaptation()) != null)
					{
						// Delete the project subject (must happen first for shouldDeleteSubject to
						// work properly).
						drp.setAdaptation(projectSubjectRecord);
						drp.execute(pContext);

						// Delete the subject
						Adaptation subjectRecord = AdaptationUtil.followFK(
							projectSubjectRecord,
							subjectPathConfig.getProjectSubjectSubjectFieldPath());
						if (!CascadeDeleteService.CASCADE_DELETE_SERVICE_TRACKING_INFO
							.equals(context.getSession().getTrackingInfo())
							&& shouldDeleteSubject(projectType, projectStatus, subjectRecord))
						{
							drp.setAdaptation(subjectRecord);
							drp.execute(pContext);
						}
					}
				}
				finally
				{
					reqRes.close();
				}
			}
		}
		super.cascadeDelete(deletedRecord, context);
	}

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		Session session = context.getSession();
		if (session.getInteraction(true) != null)
		{
			ProjectPathConfig pathConfig = getProjectPathConfig();
			// Assign Project Type from Workflow Session Interaction
			ValueContextForUpdate updateContext = context.getOccurrenceContextForUpdate();
			String projectType = WorkflowUtilities.getSessionInteractionParameter(
				session,
				ProjectWorkflowConstants.SESSION_PARAM_PROJECT_TYPE);
			updateContext.setValueEnablingPrivilegeForNode(
				projectType,
				pathConfig.getProjectProjectTypeFieldPath());

			String projectName = WorkflowUtilities.getSessionInteractionParameter(
				session,
				ProjectWorkflowConstants.SESSION_PARAM_PROJECT_NAME);
			if (projectName != null)
			{
				updateContext.setValueEnablingPrivilegeForNode(
					projectName,
					pathConfig.getProjectNameFieldPath());
			}

			Path subjectPath = pathConfig.getProjectSubjectFieldPath(projectType);

			if (!shouldCreateSubject(projectType) && subjectPath != null)
			{
				try
				{
					updateContext.setValueEnablingPrivilegeForNode(
						getSelectedRecordPK(context),
						subjectPath);
				}
				catch (OperationException ex)
				{
					LoggingCategory.getKernel()
						.error("Error setting subject record on project.", ex);
				}
			}
		}
		super.handleNewContext(context);
	}

	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		Session session = context.getSession();
		Adaptation projectRecord = context.getAdaptationOccurrence();

		if (session.getInteraction(true) == null)
		{
			String projectStatus = projectRecord
				.getString(projectPathConfig.getProjectStatusFieldPath());
			String projectType = projectRecord
				.getString(projectPathConfig.getProjectProjectTypeFieldPath());
			if (!(projectPathConfig.getCancelledProjectStatus(projectType).equals(projectStatus)
				|| projectPathConfig.getCompletedProjectStatus(projectType).equals(projectStatus)))
			{
				createProjectTeamMembers(context);
			}
		}
		else
		{
			createProjectTeamMembers(context);

			String projectType = projectRecord
				.getString(projectPathConfig.getProjectProjectTypeFieldPath());
			SubjectPathConfig subjectPathConfig = projectPathConfig
				.getSubjectPathConfig(projectType);
			if (subjectPathConfig != null)
			{
				Path projectSubjectsFieldPath = projectPathConfig
					.getProjectProjectSubjectsFieldPath(projectType);
				if (shouldCreateSubject(projectType))
				{
					Adaptation subjectRecord = createSubject(context, projectType);
					ProcedureContext pContext = context.getProcedureContext();
					if (projectSubjectsFieldPath == null)
					{
						setSubjectOnProject(
							pContext,
							projectPathConfig,
							projectRecord,
							subjectRecord.getOccurrencePrimaryKey().format());
					}
					else
					{
						createProjectSubjectRecord(
							pContext,
							projectRecord,
							subjectRecord.getOccurrencePrimaryKey().format(),
							subjectPathConfig);
					}
				}
				else if (projectSubjectsFieldPath != null)
				{
					linkSubjectToProject(context);
				}
			}
		}
		super.handleAfterCreate(context);
	}

	@Override
	public void handleAfterModify(AfterModifyOccurrenceContext context) throws OperationException
	{
		if (context.getSession().getInteraction(true) != null)
		{
			ProjectPathConfig projectPathConfig = getProjectPathConfig();
			Adaptation projectRecord = context.getAdaptationOccurrence();
			AdaptationHome parentDataSpace = projectRecord.getHome().getParentBranch();
			// If the parent branch is the master data space
			if (parentDataSpace != null && projectPathConfig.getMasterDataSpaceName()
				.equals(parentDataSpace.getKey().getName()))
			{
				String projectType = projectRecord
					.getString(projectPathConfig.getProjectProjectTypeFieldPath());
				Path projectSubjectFieldPath = projectPathConfig
					.getProjectSubjectFieldPath(projectType);
				// If no link table is used
				if (projectSubjectFieldPath != null)
				{
					// If the subject was changed then clear the old subject's current project type
					// and set the new subject's
					ValueChange subjectChange = context.getChanges()
						.getChange(projectSubjectFieldPath);
					if (subjectChange != null)
					{
						SubjectPathConfig subjectPathConfig = projectPathConfig
							.getSubjectPathConfig(projectType);
						ProcedureContext pContext = context.getProcedureContext();
						Session session = context.getSession();
						String valueBefore = (String) subjectChange.getValueBefore();
						if (valueBefore != null)
						{
							AdaptationTable subjectTable = projectRecord.getContainer()
								.getTable(subjectPathConfig.getSubjectTablePath());
							Adaptation oldSubjectRecord = subjectTable
								.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(valueBefore));
							if (oldSubjectRecord != null)
							{
								setCurrentProjectType(
									null,
									pContext,
									session,
									oldSubjectRecord,
									projectType);
							}
						}

						Adaptation newSubjectRecord = AdaptationUtil
							.followFK(projectRecord, projectSubjectFieldPath);
						if (newSubjectRecord != null)
						{
							setCurrentProjectType(
								projectType,
								pContext,
								session,
								newSubjectRecord,
								projectType);
						}
					}
				}
			}
		}
		super.handleAfterModify(context);
	}

	/**
	 * Link the record to the project by creating the record linking the subject to the project
	 * in the case of a link table. If not using a link table, should not call this method.
	 *
	 * @param context the trigger context
	 * @param projectType the project type
	 * @return the project subject record
	 */
	protected Adaptation linkSubjectToProject(AfterCreateOccurrenceContext context)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		Adaptation projectRecord = context.getAdaptationOccurrence();
		String projectType = projectRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);

		return createProjectSubjectRecord(
			context.getProcedureContext(),
			projectRecord,
			getSelectedRecordPK(context),
			subjectPathConfig);
	}

	protected void initNewSubjectCurrentProjectType(
		Map<Path, Object> pathValueMap,
		String projectType)
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);

		Path subjectCurrentProjectTypeFieldPath = subjectPathConfig
			.getSubjectCurrentProjectTypeFieldPath();
		if (subjectCurrentProjectTypeFieldPath != null)
		{
			pathValueMap.put(
				subjectCurrentProjectTypeFieldPath,
				subjectPathConfig.getNewSubjectProjectType());
		}
	}

	protected void setCurrentProjectType(
		String currentProjectType,
		ProcedureContext pContext,
		Session session,
		Adaptation subjectRecord,
		String projectType)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);
		ProjectUtil.setCurrentProjectType(
			null,
			subjectRecord,
			pContext,
			session,
			projectPathConfig,
			subjectPathConfig);
	}

	protected static Adaptation createProjectSubjectRecord(
		ProcedureContext pContext,
		Adaptation projectRecord,
		String subjectPK,
		SubjectPathConfig subjectPathConfig)
		throws OperationException
	{
		// Load Attribute Values to be assigned
		Map<Path, Object> pathValueMap = new HashMap<>();
		pathValueMap.put(
			subjectPathConfig.getProjectSubjectProjectFieldPath(),
			projectRecord.getOccurrencePrimaryKey().format());
		pathValueMap.put(subjectPathConfig.getProjectSubjectSubjectFieldPath(), subjectPK);

		AdaptationTable projectSubjectTable = projectRecord.getContainer()
			.getTable(subjectPathConfig.getProjectSubjectTablePath());
		CreateRecordProcedure crp = new CreateRecordProcedure(projectSubjectTable, pathValueMap);
		crp.setAllPrivileges(true);
		crp.execute(pContext);
		return crp.getCreatedRecord();
	}

	protected static void setSubjectOnProject(
		ProcedureContext pContext,
		ProjectPathConfig projectPathConfig,
		Adaptation projectRecord,
		String subjectPK)
		throws OperationException
	{
		String projectType = projectRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		ModifyValuesProcedure mvp = new ModifyValuesProcedure(projectRecord);
		mvp.setValue(projectPathConfig.getProjectSubjectFieldPath(projectType), subjectPK);
		mvp.setAllPrivileges(true);
		mvp.execute(pContext);
	}

	private void createProjectTeamMembers(AfterCreateOccurrenceContext context)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		if (pathConfig.getProjectRoleTablePath() == null)
		{
			return;
		}
		Adaptation projectRole = ProjectUtil.lookupProjectRole(
			context.getAdaptationOccurrence(),
			pathConfig.getProjectRoleTablePath(),
			pathConfig.getProjectPathsForProjectRolePK(),
			pathConfig.getAdminDataSet(context.getAdaptationHome().getRepository()));
		List<String> roles = projectRole.getList(pathConfig.getProjectRoleRolesFieldPath());

		for (String role : roles)
		{
			createProjectTeamMember(context, role);
		}
	}

	private void createProjectTeamMember(AfterCreateOccurrenceContext context, String projectRole)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Adaptation project = context.getAdaptationOccurrence();
		String projectPK = project.getOccurrencePrimaryKey().format();

		AdaptationTable table = project.getContainer()
			.getTable(pathConfig.getProjectTeamMemberTablePath());
		Adaptation existingProjectTeamMember = table.lookupAdaptationByPrimaryKey(
			table.computePrimaryKey(new Object[] { projectPK, projectRole }));
		// If there's not already a project team member created then create it.
		// (In normal situations this wouldn't happen, but a Tech Admin may have deleted project but
		// not team members for example.)
		if (existingProjectTeamMember == null)
		{
			ProcedureContext pContext = context.getProcedureContext();

			// create Project Team Member
			// Load Attribute Values to be assigned
			Map<Path, Object> pathValueMap = new HashMap<>();
			pathValueMap.put(pathConfig.getProjectTeamMemberProjectFieldPath(), projectPK);
			pathValueMap.put(pathConfig.getProjectTeamMemberProjectRoleFieldPath(), projectRole);
			String defaultProjectTeamMemberUser = getDefaultProjectTeamMemberUser(
				context.getSession(),
				project,
				projectRole);
			if (defaultProjectTeamMemberUser != null)
			{
				pathValueMap.put(
					pathConfig.getProjectTeamMemberUserFieldPath(),
					defaultProjectTeamMemberUser);
			}

			Create.execute(pContext, table, pathValueMap);
		}
	}

	// By default, does not specify a default user, but can be overridden to have special logic if needed
	protected String getDefaultProjectTeamMemberUser(
		Session session,
		Adaptation projectRecord,
		String projectRole)
		throws OperationException
	{
		return null;
	}

	protected String getSelectedRecordPK(TableTriggerExecutionContext context)
		throws OperationException
	{
		Session session = context.getSession();
		// When creating projects for existing subjects, the "record" parameter contains its XPath
		String recordXPath = WorkflowUtilities
			.getSessionInteractionParameter(session, WorkflowConstants.PARAM_RECORD);
		if (recordXPath == null)
		{
			return null;
		}
		Path tablePath = XPathExpressionHelper.getTablePathForXPath(recordXPath);
		// If it's not a project record
		if (!tablePath.equals(getProjectPathConfig().getProjectTablePath()))
		{
			Adaptation record = XPathExpressionHelper.lookupFirstRecordMatchingXPath(
				true,
				getSelectedRecordDataSet(context, recordXPath),
				recordXPath);
			return record == null ? null : record.getOccurrencePrimaryKey().format();
		}
		return null;
	}

	// Most cases, the data set is the same for the selected record that needs to become the subject
	// of the project, but in cases where it's different, this can be overridden
	protected Adaptation getSelectedRecordDataSet(
		TableTriggerExecutionContext context,
		String recordXPath)
	{
		return context.getOccurrenceContext().getAdaptationInstance();
	}
}
