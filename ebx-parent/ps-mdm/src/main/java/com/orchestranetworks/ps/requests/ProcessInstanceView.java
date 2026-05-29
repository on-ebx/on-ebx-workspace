package com.orchestranetworks.ps.requests;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.workflow.*;

@Deprecated
public abstract class ProcessInstanceView extends UIBeanEditor
{
	public abstract RequestPathConfig getRequestPathConfig();

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		UIHttpManagerComponent managerComponent = context.createWebComponentForSubSession();
		ValueContext vc = context.getValueContext();
		Adaptation request = AdaptationUtil.getRecordForValueContext(vc);
		ProcessInstance pi = getProcessInstance(request, context.getSession());
		WorkflowMode mode = WorkflowMode.WORKFLOW_HISTORY;
		if (pi != null)
		{
			if (!pi.isCompleted())
				mode = WorkflowMode.MONITORED_WORKFLOW;
			managerComponent.setPredicate("./id = " + pi.getProcessInstanceKey().getId());
			//				managerComponent.setPredicate(
			//						"starts-with(./dwfLabel, '" + pi.getLabel().formatMessage(Locale.getDefault()) + "')");
		}
		WorkflowView view = WorkflowViewHelper.getWorkflowView(mode);
		managerComponent.selectWorkflowView(view);
		UIButtonSpecJSAction buildButtonPreview = context.buildButtonPreview(managerComponent);
		context.addButtonJavaScript(buildButtonPreview);
	}

	@Override
	public void addForEdit(UIResponseContext arg0)
	{
		addForDisplay(arg0);
	}

	/** Return the workflow (if it is active) corresponding to thise request */
	public ProcessInstance getProcessInstance(Adaptation request, Session session)
	{
		String key = request.getString(getRequestPathConfig().getRequestProcessInstanceKeyPath());
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

}
