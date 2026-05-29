package com.orchestranetworks.ps.project.workflow.launcher;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;

import jakarta.servlet.http.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. This should be re-implemented as a User Service.
 */
@Deprecated
@SuppressWarnings("serial")
public abstract class SubjectWorkflowLauncher extends WorkflowLauncher
	implements ProjectPathCapable, SubjectPathCapable
{
	public static final String PARAM_PROJECT_TYPE = "projectType";
	public static final String PARAM_PROJECT_NAME = "projectName";

	protected abstract String getProjectMasterWorkflowKey(WorkflowLauncherContext context);

	@Override
	public void execute(HttpServletRequest request) throws OperationException
	{
		execute(new WorkflowLauncherContext(request));
	}

	public void execute(WorkflowLauncherContext context) throws OperationException
	{
		super.execute(
			context,
			getProjectMasterWorkflowKey(context),
			getProjectPathConfig().getProjectTablePath().format(),
			null);
	}

	public void execute(HttpServletRequest request, String projectType) throws OperationException
	{
		WorkflowLauncherContext context = new WorkflowLauncherContext(request);
		context.setParameter(SubjectWorkflowLauncher.PARAM_PROJECT_TYPE, projectType);
		execute(context);
	}

	@Override
	protected void initRedirectionPolicy()
	{
		workItemRedirectPolicy = WorkItemRedirectPolicyEnum.FIRST_SUB_WORKFLOW;
	}

	@Override
	protected String enrichWorkflowInstanceName(String workflowInstanceName, Locale locale)
	{
		return workflowLauncherContext.getParameter(PARAM_PROJECT_TYPE) + " "
			+ getWorkflowModelLabelForInstanceName(workflowName, locale);
	}

	@Override
	protected void assignWorkflowDescription()
	{
		this.workflowDescription = workflowLauncherContext.getParameter(PARAM_PROJECT_TYPE);
	}

	@Override
	protected void setAdditionalContextVariables() throws OperationException
	{
		super.setAdditionalContextVariables();
		launcher.setInputParameter(
			ProjectWorkflowConstants.PARAM_PROJECT_TYPE,
			workflowLauncherContext.getParameter(PARAM_PROJECT_TYPE));
		String projectName = workflowLauncherContext.getParameter(PARAM_PROJECT_NAME);
		if (projectName != null)
		{
			launcher
				.setInputParameter(ProjectWorkflowConstants.PARAM_RECORD_NAME_VALUE, projectName);
		}
	}

	@Override
	protected void preventMultipleLaunches() throws OperationException
	{
		super.preventMultipleLaunches();
		final Adaptation adaptation = workflowLauncherContext.getCurrentAdaptation();
		SubjectPathConfig subjectPathConfig = getSubjectPathConfig();
		if (subjectPathConfig != null)
		{
			String newSubjectProjectType = subjectPathConfig.getNewSubjectProjectType();
			if (newSubjectProjectType == null
				|| !workflowLauncherContext.getParameter(PARAM_PROJECT_TYPE)
					.equals(newSubjectProjectType))
			{
				if (subjectPathConfig.getSubjectCurrentProjectTypeFieldPath() != null)
				{
					// This is done in a procedure to ensure thread safety
					Procedure proc = new Procedure()
					{
						@Override
						public void execute(ProcedureContext pContext) throws Exception
						{
							UserMessage msg = checkCurrentProjectType(adaptation);
							if (msg != null)
							{
								throw OperationException.createError(msg);
							}
							setCurrentProjectType(pContext, adaptation);
						}
					};
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
	}

	protected UserMessage checkCurrentProjectType(Adaptation subjectRecord)
	{
		String currentProjectType = subjectRecord
			.getString(getSubjectPathConfig().getSubjectCurrentProjectTypeFieldPath());
		return currentProjectType == null ? null
			: UserMessage.createError(
				"A " + currentProjectType
					+ " workflow is already in process for the selected record.");
	}

	protected void setCurrentProjectType(ProcedureContext pContext, Adaptation subjectRecord)
		throws OperationException
	{
		ModifyValuesProcedure mvp = new ModifyValuesProcedure(subjectRecord);
		mvp.setValue(
			getSubjectPathConfig().getSubjectCurrentProjectTypeFieldPath(),
			workflowLauncherContext.getParameter(PARAM_PROJECT_TYPE));
		mvp.setAllPrivileges(true);
		mvp.execute(pContext);
	}
}
