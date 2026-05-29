package com.orchestranetworks.ps.service;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.UIHttpManagerComponent.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.workflow.*;

public class ViewWorkflow extends AbstractUserService<RecordEntitySelection>
{
	private String url;
	private final Path processKeyPath;

	public ViewWorkflow(Path processKeyPath)
	{
		super();
		this.processKeyPath = processKeyPath;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		// this service is available on Request records only.
		Adaptation request = context.getEntitySelection().getRecord();
		UIHttpManagerComponent managerComponent = context.getWriter()
			.createWebComponentForSubSession();
		ProcessInstance pi = getProcessInstance(request, session);
		if (pi == null)
			return;
		WorkflowMode mode = WorkflowMode.WORKFLOW_HISTORY;
		if (!pi.isCompleted())
			mode = WorkflowMode.MONITORED_WORKFLOW;
		managerComponent.setPredicate(
			"./id = " + pi.getProcessInstanceKey().getId() + " and contains(./publication,'"
				+ getWorkflowModelName(request.getHome().getRepository(), session, pi)
				+ "') and osd:is-not-null(./creator)");
		WorkflowView view = WorkflowViewHelper.getWorkflowView(mode);
		managerComponent.selectWorkflowView(view);
		managerComponent.setScope(Scope.NODE);
		url = managerComponent.getURIWithParameters();
	}

	public static String getWorkflowModelName(Repository repo, Session session, ProcessInstance pi)
	{
		PublishedProcessKey ppk = pi.getPublishProcessKey();
		if (ppk.isDisabled())
		{
			WorkflowEngine wfe = WorkflowEngine.getFromRepository(repo, session);
			return wfe.getPublishedProcess(ppk).getAdaptationName().getStringName();
		}
		return ppk.getName();
	}

	/** Return the workflow (if it is active) corresponding to the record */
	public ProcessInstance getProcessInstance(Adaptation request, Session session)
	{
		String key = request.getString(processKeyPath);
		Repository repo = request.getHome().getRepository();
		WorkflowEngine we = WorkflowEngine.getFromRepository(repo, session);
		try
		{
			return we.getProcessInstance(ProcessInstanceKey.parse(key));
		}
		catch (Exception e)
		{
			// ignore
		}
		return null;
	}

	@Override
	public void landService()
	{
		if (url == null)
		{
			super.landService();
			return;
		}

		displayInFrameWithCloseButton("view-worflow-frame", url);
	}

}
