/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

// To avoid confusion, this should probably have been named PermissionsUsersTriggerActionValidator,
// because it's based on the permissions users in the tracking info, not the workflow task.
// "Workflow Task" is the term usually used to indicate which string to use when looking up the profile
// to offer/allocate to when using a lookup table.
public class WorkflowTaskTriggerActionValidator implements TriggerActionValidator
{
	private TriggerAction[] actions;
	private String[] workflowTasks;
	private TriggerActionValidator nestedTriggerActionValidator;
	private TrackingInfoHelper trackingInfoHelper;

	public WorkflowTaskTriggerActionValidator(TriggerAction[] actions, String[] workflowTasks)
	{
		this(actions, workflowTasks, null);
	}

	public WorkflowTaskTriggerActionValidator(
		TriggerAction[] actions,
		String[] workflowTasks,
		TriggerActionValidator nestedTriggerActionValidator)
	{
		this(
			actions,
			workflowTasks,
			nestedTriggerActionValidator,
			new FirstSegmentTrackingInfoHelper(
				WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USERS));
	}

	public WorkflowTaskTriggerActionValidator(
		TriggerAction[] actions,
		String[] workflowTasks,
		TriggerActionValidator nestedTriggerActionValidator,
		TrackingInfoHelper trackingInfoHelper)
	{
		this.actions = actions;
		this.workflowTasks = workflowTasks;
		this.nestedTriggerActionValidator = nestedTriggerActionValidator;
		this.trackingInfoHelper = trackingInfoHelper;
	}

	@Override
	public UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action)
		throws OperationException
	{
		if (nestedTriggerActionValidator != null)
		{
			UserMessage msg = nestedTriggerActionValidator.validateTriggerAction(
				session,
				valueContext,
				valueChanges,
				action);
			if (msg != null)
			{
				return msg;
			}
		}
		if (session.getInteraction(true) != null && ArrayUtils.contains(actions, action))
		{
			String trackingInfo = session.getTrackingInfo();
			if (trackingInfo != null)
			{
				String trackingInfoSeg;
				synchronized (trackingInfoHelper)
				{
					trackingInfoHelper.initTrackingInfo(trackingInfo);
					trackingInfoSeg = trackingInfoHelper.getTrackingInfoSegment(
						WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USERS);
				}
				if (ArrayUtils.contains(workflowTasks, trackingInfoSeg))
				{
					AdaptationTable table = valueContext.getAdaptationTable();
					// Only default locale supported
					String tableLabel = table.getTableNode().getLabel(Locale.getDefault());
					return UserMessage.createError(
						"Not allowed to " + action + " record in table "
							+ tableLabel + " from this workflow task.");
				}
			}
		}
		return null;
	}
}
