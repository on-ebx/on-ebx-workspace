/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.workflow;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.MailSpec.*;

/**
 */
public class ProjectWorkflowUtilities extends WorkflowUtilities
{
	public static void notifyProjectTeam(
		DataContextReadOnly dataContext,
		ProcessExecutionContext processExecutionContext,
		int notificationTemplate,
		ProjectPathConfig pathConfig)
		throws OperationException
	{
		Adaptation project = getProjectRecord(
			dataContext,
			processExecutionContext.getRepository(),
			pathConfig);
		if (project != null)
		{
			@SuppressWarnings("unchecked")
			List<String> userIds = (List<String>) AdaptationUtil.getLinkedRecordList(
				project,
				pathConfig.getProjectTeamMembersFieldPath(),
				pathConfig.getProjectTeamMemberUserFieldPath());

			MailSpec mailSpec = processExecutionContext.createMailSpec();
			mailSpec.setTemplateMailId(notificationTemplate);
			DirectoryHandler dirHandler = processExecutionContext.getSession().getDirectory();

			for (String userId : userIds)
			{
				if (userId != null)
				{
					UserReference user = UserReference.forUser(userId);
					String email = dirHandler.getUserEmail(user);
					if (email != null)
					{
						mailSpec.notify(NotificationType.TO, email);
					}
				}
			}
			mailSpec.sendMail(processExecutionContext.getSession().getLocale());
		}
	}

	@Deprecated
	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		Repository repo,
		ProjectPathConfig pathConfig)
		throws OperationException
	{
		return getProjectRecord(
			dataContext,
			null,
			repo,
			pathConfig,
			WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		ProjectPathConfig pathConfig)
		throws OperationException
	{
		return getProjectRecord(
			dataContext,
			interaction,
			repo,
			pathConfig,
			WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	@Deprecated
	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		Repository repo,
		ProjectPathConfig pathConfig,
		String dataSpaceParam)
		throws OperationException
	{
		return getProjectRecord(dataContext, null, repo, pathConfig, dataSpaceParam, true);
	}

	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		ProjectPathConfig pathConfig,
		String dataSpaceParam)
		throws OperationException
	{
		return getProjectRecord(dataContext, interaction, repo, pathConfig, dataSpaceParam, true);
	}

	@Deprecated
	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		Repository repo,
		ProjectPathConfig pathConfig,
		String dataSpaceParam,
		boolean errorIfNotFound)
		throws OperationException
	{
		return getProjectRecord(
			dataContext,
			null,
			repo,
			pathConfig,
			dataSpaceParam,
			errorIfNotFound);
	}

	public static Adaptation getProjectRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		ProjectPathConfig pathConfig,
		String dataSpaceParam,
		boolean errorIfNotFound)
		throws OperationException
	{
		Adaptation projectRecord = WorkflowUtilities.getRecord(
			dataContext,
			interaction,
			repo,
			WorkflowConstants.PARAM_RECORD,
			dataSpaceParam,
			errorIfNotFound);
		// check that record is a Project
		// -- when first Initiating Maintenance Projects,
		// it is the selected record on which maintenance is being performed
		if (!isProjectRecord(projectRecord, pathConfig))
		{
			projectRecord = null;
		}
		return projectRecord;
	}

	public static Adaptation getProjectRecordFromSessionInteraction(
		SessionInteraction interaction,
		Repository repo,
		ProjectPathConfig pathConfig)
		throws OperationException
	{
		Adaptation projectRecord = WorkflowUtilities
			.getRecordFromSessionInteraction(interaction, repo);
		if (!isProjectRecord(projectRecord, pathConfig))
		{
			projectRecord = null;
		}
		return projectRecord;
	}

	public static Adaptation getProjectRecordFromSessionInteraction(
		SessionInteraction interaction,
		Adaptation dataSet,
		ProjectPathConfig pathConfig)
		throws OperationException
	{
		Adaptation projectRecord = WorkflowUtilities
			.getRecordFromSessionInteraction(interaction, dataSet);
		if (!isProjectRecord(projectRecord, pathConfig))
		{
			projectRecord = null;
		}
		return projectRecord;
	}

	private static boolean isProjectRecord(Adaptation record, ProjectPathConfig pathConfig)
	{
		return record != null
			&& record.getContainerTable().getTablePath().equals(pathConfig.getProjectTablePath());
	}

	public static void clearCurrentProjectType(
		DataContextReadOnly dataContext,
		Session session,
		Repository repo,
		ProjectPathConfig projectPathConfig,
		SubjectPathConfig subjectPathConfig)
		throws OperationException
	{
		List<Adaptation> subjectRecords = getSubjectRecords(
			dataContext,
			session.getInteraction(true),
			repo,
			projectPathConfig);
		if (!subjectRecords.isEmpty())
		{
			setCurrentProjectType(
				subjectRecords,
				dataContext,
				session,
				repo,
				subjectPathConfig,
				null);
		}
	}

	public static void setCurrentProjectType(
		DataContextReadOnly dataContext,
		Session session,
		Repository repo,
		ProjectPathConfig projectPathConfig)
		throws OperationException
	{
		List<Adaptation> subjectRecords = getSubjectRecords(
			dataContext,
			session.getInteraction(true),
			repo,
			projectPathConfig);
		if (!subjectRecords.isEmpty())
		{
			Adaptation projectRecord = getProjectRecord(dataContext, repo, projectPathConfig);
			String projectType = projectRecord
				.getString(projectPathConfig.getProjectProjectTypeFieldPath());
			setCurrentProjectType(
				subjectRecords,
				dataContext,
				session,
				repo,
				projectPathConfig.getSubjectPathConfig(projectType),
				projectType);
		}
	}

	private static List<Adaptation> getSubjectRecords(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		ProjectPathConfig projectPathConfig)
		throws OperationException
	{
		List<Adaptation> subjectRecords = new ArrayList<>();
		String recordParam = dataContext.getVariableString(WorkflowConstants.PARAM_RECORD);
		if (recordParam != null)
		{
			Adaptation record = WorkflowUtilities.getRecord(
				dataContext,
				interaction,
				repo,
				WorkflowConstants.PARAM_RECORD,
				WorkflowConstants.PARAM_WORKING_DATA_SPACE,
				false);
			if (record != null)
			{
				Path recordTablePath = record.getContainerTable().getTablePath();
				// If it's a project record, get its subjects
				if (projectPathConfig.getProjectTablePath().equals(recordTablePath))
				{
					String projectType = record
						.getString(projectPathConfig.getProjectProjectTypeFieldPath());
					Path projectSubjectFieldPath = projectPathConfig
						.getProjectSubjectFieldPath(projectType);
					if (projectSubjectFieldPath == null)
					{
						Path projectSubjectsFieldPath = projectPathConfig
							.getProjectProjectSubjectsFieldPath(projectType);
						if (projectSubjectsFieldPath != null)
						{
							subjectRecords = AdaptationUtil
								.getLinkedRecordList(record, projectSubjectsFieldPath);
						}
					}
					else
					{
						Adaptation subjectRecord = AdaptationUtil
							.followFK(record, projectSubjectFieldPath);
						if (subjectRecord != null)
						{
							subjectRecords.add(subjectRecord);
						}
					}
				}
				// Otherwise it's already a subject record
				else
				{
					subjectRecords.add(record);
				}
			}
		}
		return subjectRecords;
	}

	// This does something similar to ProjectUtil.setCurrentProjectType but it only needs to set it
	// in the master,
	// and it does it for multiple records at once in same procedure, so it's different enough that
	// it's worth having separate code.
	private static void setCurrentProjectType(
		List<Adaptation> subjectRecords,
		DataContextReadOnly context,
		Session session,
		Repository repo,
		final SubjectPathConfig subjectPathConfig,
		final String value)
		throws OperationException
	{
		final Path currentProjectTypePath = subjectPathConfig
			.getSubjectCurrentProjectTypeFieldPath();
		if (currentProjectTypePath == null || subjectRecords.isEmpty())
		{
			return;
		}
		AdaptationHome masterDataSpace = WorkflowUtilities
			.getDataSpace(context, repo, WorkflowConstants.PARAM_MASTER_DATA_SPACE);
		Adaptation firstSubjectRecord = subjectRecords.get(0);
		Adaptation masterDataSet = masterDataSpace
			.findAdaptationOrNull(firstSubjectRecord.getContainer().getAdaptationName());
		AdaptationTable masterTable = masterDataSet
			.getTable(firstSubjectRecord.getContainerTable().getTablePath());
		final List<Adaptation> masterSubjectRecords = new ArrayList<>();
		for (Adaptation subjectRecord : subjectRecords)
		{
			Adaptation masterSubjectRecord = masterTable
				.lookupAdaptationByPrimaryKey(subjectRecord.getOccurrencePrimaryKey());
			if (masterSubjectRecord != null)
			{
				masterSubjectRecords.add(masterSubjectRecord);
			}
		}
		Procedure proc = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				ModifyValuesProcedure mvp = new ModifyValuesProcedure();
				mvp.setAllPrivileges(true);
				for (Adaptation subjectRecord : masterSubjectRecords)
				{
					String currentProjectType = subjectRecord.getString(currentProjectTypePath);
					if (!Objects.equals(currentProjectType, value))
					{
						mvp.setAdaptation(subjectRecord);
						mvp.setValue(currentProjectTypePath, value);
						mvp.execute(pContext);
					}
				}
			}
		};
		ProcedureExecutor.executeProcedure(proc, session, masterDataSpace);
	}
}
