package com.orchestranetworks.ps.requests;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.UserTask.*;

/**
 * Service to batch accept or reject a selection of request records that are tied to 
 * process instances.  User selects whether the selection should be accepted or rejected and
 * supplies a batch comment for the same.
 */

public class MassApproveOrRejectService extends AbstractUserService<TableViewEntitySelection>
{
	private final static ObjectKey _InputKey = ObjectKey.forName("input");
	private final static Path rejectPath = Path.parse("./reject");
	private final static Path commentPath = Path.parse("./comment");
	private boolean approve;
	private String comment;
	private final RequestPathConfig config;

	public MassApproveOrRejectService(RequestPathConfig config)
	{
		super();
		this.config = config;
	}

	public void execute(Session session) throws OperationException
	{
		// this service is available on Request records only (as specified in the request path
		// config).
		TableViewEntitySelection selection = context.getEntitySelection();
		List<Adaptation> rows = AdaptationUtil.getRecords(selection.getSelectedRecords().execute());
		WorkflowEngine we = WorkflowEngine
			.getFromRepository(selection.getDataspace().getRepository(), session);
		UserReference user = session.getUserReference();
		List<WorkItem> workItemsOffered = we.getWorkItemsOfferedToUser(user);
		Map<ProcessInstanceKey, WorkItem> offered = createWorkItemMap(workItemsOffered);
		List<WorkItem> workItemsTaken = we.getWorkItemsAllocatedToUser(user);
		Map<ProcessInstanceKey, WorkItem> taken = createWorkItemMap(workItemsTaken);
		for (Adaptation row : rows)
		{
			ProcessInstanceKey pik = ProcessInstanceKey
				.parse(row.getString(config.getRequestProcessInstanceKeyPath()));
			WorkItem workItem = offered.get(pik);
			if (workItem == null)
				workItem = taken.get(pik);
			if (workItem != null)
			{
				WorkItemKey wik = workItem.getWorkItemKey();
				if (!user.equals(workItem.getUserReference()))
				{
					we.allocateWorkItemToMyself(wik);
				}
				SessionInteraction si = we.createOrOpenInteraction(wik);
				String comment = this.comment;
				if (comment == null)
					comment = (approve ? "Batch-advanced by " : "Batch-rejected by ")
						+ user.getLabel();
				si.setComment(comment);
				if (approve)
				{
					si.accept();
				}
				else if (workItem.isRejectEnabled())
				{
					si.reject();
				}
			}
		}
	}

	public static Map<ProcessInstanceKey, WorkItem> createWorkItemMap(List<WorkItem> workItems)
	{
		Map<ProcessInstanceKey, WorkItem> result = new HashMap<>();
		for (WorkItem workItem : workItems)
		{
			ProcessInstanceKey pik = workItem.getProcessInstanceKey();
			if (!result.containsKey(pik))
				result.put(pik, workItem);
		}
		return result;
	}

	public boolean isApprove()
	{
		return approve;
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		approve = !Boolean.TRUE
			.equals(fromContext.getValueContext(_InputKey, rejectPath).getValue());
		comment = (String) fromContext.getValueContext(_InputKey, commentPath).getValue();
		return super.readValues(fromContext);
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<TableViewEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		BeanDefinition def = context.defineObject(aBuilder, _InputKey);
		defineElement(
			def,
			rejectPath,
			"Reject selected requests?",
			SchemaTypeName.XS_BOOLEAN,
			false);
		BeanElement comment = defineElement(
			def,
			commentPath,
			"Enter comment for batch-" + (approve ? "accept" : "reject"),
			SchemaTypeName.OSD_TEXT,
			null);
		comment.setDescription("If left blank, a default message will be used");
		super.setupObjectContext(aContext, aBuilder);
	}
}
