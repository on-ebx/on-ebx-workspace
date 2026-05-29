/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.util.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class FinishProjectScriptTask extends ScriptTask implements ProjectPathCapable
{
	protected abstract String getFinishedProjectStatus(String projectType);

	protected void updateProjectStatus(ProcedureContext pContext, ValueContextForUpdate vc)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		String projectType = (String) vc.getValue(pathConfig.getProjectProjectTypeFieldPath());
		vc.setValue(getFinishedProjectStatus(projectType), pathConfig.getProjectStatusFieldPath());
	}

	// By default, do nothing but can be overridden
	protected void updateSubjectStatus(
		ProcedureContext pContext,
		ValueContextForUpdate vc,
		Adaptation projectRecord,
		String currentStatus)
		throws OperationException
	{
		// do nothing
	}

	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		final Adaptation projectRecord = ProjectWorkflowUtilities
			.getProjectRecord(context, null, context.getRepository(), getProjectPathConfig());
		Procedure proc = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				pContext.setAllPrivileges(true);
				updateProjectAndSubjectStatus(pContext, projectRecord);
			}
		};
		ProcedureExecutor.executeProcedure(proc, context.getSession(), projectRecord.getHome());
	}

	protected void updateProjectAndSubjectStatus(
		ProcedureContext pContext,
		Adaptation projectRecord)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		List<Adaptation> subjectRecords;
		String projectType = projectRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		Path projectSubjectsPath = projectPathConfig
			.getProjectProjectSubjectsFieldPath(projectType);
		if (projectSubjectsPath == null)
		{
			subjectRecords = new ArrayList<>();
			Path subjectFieldPath = projectPathConfig.getProjectSubjectFieldPath(projectType);
			if (subjectFieldPath != null)
			{
				Adaptation subjectRecord = AdaptationUtil.followFK(projectRecord, subjectFieldPath);
				if (subjectRecord != null)
				{
					subjectRecords.add(subjectRecord);
				}
			}
		}
		else
		{
			subjectRecords = AdaptationUtil.getLinkedRecordList(projectRecord, projectSubjectsPath);
		}

		ValueContextForUpdate vc = pContext.getContext(projectRecord.getAdaptationName());
		updateProjectStatus(pContext, vc);

		doModify(pContext, projectRecord, vc);

		if (!subjectRecords.isEmpty())
		{
			SubjectPathConfig subjectPathConfig = projectPathConfig
				.getSubjectPathConfig(projectType);
			Path subjectStatusPath = subjectPathConfig.getSubjectStatusFieldPath();
			if (subjectStatusPath != null)
			{
				for (Adaptation subjectRecord : subjectRecords)
				{
					vc = pContext.getContext(subjectRecord.getAdaptationName());
					if (subjectStatusPath != null)
					{
						String currentSubjectStatus = subjectRecord.getString(subjectStatusPath);
						updateSubjectStatus(pContext, vc, projectRecord, currentSubjectStatus);
					}
					doModify(pContext, subjectRecord, vc);
				}
			}
			clearCurrentProjectType(pContext, subjectRecords, projectType);
		}
	}

	protected void clearCurrentProjectType(
		ProcedureContext pContext,
		List<Adaptation> subjectRecords,
		String projectType)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		SubjectPathConfig subjectPathConfig = projectPathConfig.getSubjectPathConfig(projectType);
		ProjectUtil.setCurrentProjectType(
			null,
			subjectRecords,
			pContext,
			null,
			projectPathConfig,
			subjectPathConfig);
	}

	@SuppressWarnings("deprecation")
	private static Adaptation doModify(
		ProcedureContext pContext,
		Adaptation adaptation,
		ValueContextForUpdate vc)
		throws OperationException
	{
		Adaptation modifiedAdaptation;
		Session session = pContext.getSession();
		TriggerAction[] ignoreActions = (TriggerAction[]) session
			.getAttribute(BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE);
		session.setAttribute(
			BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
			new TriggerAction[] { TriggerAction.MODIFY });
		try
		{
			modifiedAdaptation = pContext.doModifyContent(adaptation, vc);
		}
		finally
		{
			session.setAttribute(
				BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
				ignoreActions);
		}
		return modifiedAdaptation;
	}
}
