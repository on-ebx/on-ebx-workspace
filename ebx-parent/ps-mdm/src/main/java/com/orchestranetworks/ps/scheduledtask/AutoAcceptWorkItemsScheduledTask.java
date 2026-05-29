package com.orchestranetworks.ps.scheduledtask;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.interactions.SessionInteraction;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.scheduler.ScheduledTask;
import com.orchestranetworks.scheduler.ScheduledTaskInterruption;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.workflow.UserTask.WorkItem;
import com.orchestranetworks.workflow.WorkflowEngine;

public class AutoAcceptWorkItemsScheduledTask extends ScheduledTask
{
	private int numberOfDaysSinceCreate;
	private String matchingWorkflowName;
	private String matchingStepName;

	public int getNumberOfDaysSinceCreate()
	{
		return numberOfDaysSinceCreate;
	}
	public void setNumberOfDaysSinceCreate(int numberOfDaysSinceCreate)
	{
		this.numberOfDaysSinceCreate = numberOfDaysSinceCreate;
	}
	public String getMatchingWorkflowName()
	{
		return matchingWorkflowName;
	}
	public void setMatchingWorkflowName(String matchingWorkflowName)
	{
		this.matchingWorkflowName = matchingWorkflowName;
	}
	public String getMatchingStepName()
	{
		return matchingStepName;
	}
	public void setMatchingStepName(String matchingStepName)
	{
		this.matchingStepName = matchingStepName;
	}
	@Override
	public void execute(ScheduledExecutionContext aContext)
		throws OperationException, ScheduledTaskInterruption
	{
		Repository repo = aContext.getRepository();
		Session session = aContext.getSession();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, this.numberOfDaysSinceCreate * -1);
		Date createTimeThreshhold = cal.getTime();
		WorkflowEngine we = WorkflowEngine.getFromRepository(repo, session);
		List<WorkItem> allItems = we.getAllWorkItems();
		for (WorkItem workItem : allItems)
		{
			if (shouldAutoAccept(workItem, createTimeThreshhold, session.getLocale()))
			{
				SessionInteraction si = we.createOrOpenInteraction(workItem.getWorkItemKey());
				si.accept();
			}
		}
	}

	private boolean shouldAutoAccept(WorkItem workItem, Date createTimeThreshhold, Locale locale)
	{
		Date createDt = workItem.getStartDate(); //is there a create date?
		if (!createDt.before(createTimeThreshhold))
		{
			return false;
		}
		if (matchingStepName != null)
		{
			String stepName = workItem.getLabel().formatMessage(locale);
			if (!stepName.contains(matchingStepName))
				return false;
		}
		if (matchingWorkflowName != null)
		{
			String wfName = workItem.getProcessInstanceKey().format();
			if (!wfName.contains(matchingWorkflowName))
				return false;
		}
		return true;
	}

}
