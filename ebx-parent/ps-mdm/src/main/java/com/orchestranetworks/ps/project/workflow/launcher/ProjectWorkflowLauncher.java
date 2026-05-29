package com.orchestranetworks.ps.project.workflow.launcher;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

import jakarta.servlet.http.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. This should be re-implemented as a User Service.
 */
@Deprecated
@SuppressWarnings("serial")
public abstract class ProjectWorkflowLauncher extends WorkflowLauncher implements ProjectPathCapable
{
	protected boolean resuming;

	protected ProjectWorkflowLauncher()
	{
		this(false);
	}

	protected ProjectWorkflowLauncher(boolean resuming)
	{
		this.resuming = resuming;
	}

	protected abstract String getProjectMasterWorkflowKey();

	@Override
	public void execute(HttpServletRequest request) throws OperationException
	{
		execute(new WorkflowLauncherContext(request));
	}

	public void execute(WorkflowLauncherContext context) throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		super.execute(
			context,
			getProjectMasterWorkflowKey(),
			pathConfig.getProjectTablePath().format(),
			null);
	}

	@Override
	protected void initRedirectionPolicy()
	{
		workItemRedirectPolicy = WorkItemRedirectPolicyEnum.FIRST_SUB_WORKFLOW;
	}

	@Override
	protected String enrichWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		String projectType = workflowLauncherContext.getCurrentAdaptation()
			.getString(getProjectPathConfig().getProjectProjectTypeFieldPath());
		return projectType + " " + getWorkflowModelLabelForInstanceName(workflowName, locale);

	}

	@Override
	protected void setAdditionalContextVariables() throws OperationException
	{
		super.setAdditionalContextVariables();
		if (launcher.isDefinedAsInputParameter(ProjectWorkflowConstants.PARAM_PROJECT_TYPE))
		{
			// get ProjectType from Project
			launcher.setInputParameter(
				ProjectWorkflowConstants.PARAM_PROJECT_TYPE,
				workflowLauncherContext.getCurrentAdaptation()
					.getString(getProjectPathConfig().getProjectProjectTypeFieldPath()));
		}
		if (launcher.isDefinedAsInputParameter(ProjectWorkflowConstants.PARAM_RESUMING))
		{
			launcher.setInputParameter(
				ProjectWorkflowConstants.PARAM_RESUMING,
				String.valueOf(resuming));
		}
	}

	@Override
	protected void assignWorkflowDescription()
	{
		this.workflowDescription = workflowLauncherContext.getCurrentAdaptation()
			.getString(getProjectPathConfig().getProjectProjectTypeFieldPath());
	}

	@Override
	protected void preventMultipleLaunches() throws OperationException
	{
		super.preventMultipleLaunches();
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		String projectType = adaptation
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		List<Adaptation> subjectRecords;
		Path subjectPath = projectPathConfig.getProjectSubjectFieldPath(projectType);
		if (subjectPath == null)
		{
			Path projectSubjectsFieldPath = projectPathConfig
				.getProjectProjectSubjectsFieldPath(projectType);
			if (projectSubjectsFieldPath == null)
			{
				subjectRecords = new ArrayList<>();
			}
			else
			{
				subjectRecords = AdaptationUtil
					.getLinkedRecordList(adaptation, projectSubjectsFieldPath);
			}
		}
		else
		{
			subjectRecords = new ArrayList<>();
			Adaptation subjectRecord = AdaptationUtil.followFK(adaptation, subjectPath);
			if (subjectRecord != null)
			{
				subjectRecords.add(subjectRecord);
			}
		}
		if (subjectRecords.size() > 0)
		{
			SubjectPathConfig subjectPathConfig = projectPathConfig
				.getSubjectPathConfig(projectType);
			Path currentProjectTypePath = subjectPathConfig.getSubjectCurrentProjectTypeFieldPath();
			if (currentProjectTypePath != null)
			{
				Procedure proc = new SetCurrentProjectTypeProcedure(
					subjectRecords,
					currentProjectTypePath,
					projectType);
				AdaptationHome dataSpace = workflowLauncherContext.getCurrentDataSpace();
				if (dataSpace == null)
				{
					dataSpace = workflowLauncherContext.getMasterDataSpace();
				}
				ProcedureExecutor
					.executeProcedure(proc, workflowLauncherContext.getSession(), dataSpace);
			}
		}
	}

	private class SetCurrentProjectTypeProcedure implements Procedure
	{
		private List<Adaptation> subjectRecords;
		private Path currentProjectTypePath;
		private String projectType;

		public SetCurrentProjectTypeProcedure(
			List<Adaptation> subjectRecords,
			Path currentProjectTypePath,
			String projectType)
		{
			this.subjectRecords = subjectRecords;
			this.currentProjectTypePath = currentProjectTypePath;
			this.projectType = projectType;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			ModifyValuesProcedure mvp = new ModifyValuesProcedure();
			mvp.setValue(currentProjectTypePath, projectType);
			mvp.setAllPrivileges(true);
			for (Adaptation subjectRecord : subjectRecords)
			{
				String currentProjectType = subjectRecord.getString(currentProjectTypePath);
				if (currentProjectType != null)
				{
					throw OperationException
						.createError("A workflow is already in process for the selected project.");
				}
				mvp.setAdaptation(subjectRecord);
				mvp.execute(pContext);
			}
		}
	}

	public boolean isResuming()
	{
		return this.resuming;
	}

	public void setResuming(boolean resuming)
	{
		this.resuming = resuming;
	}
}
