package com.orchestranetworks.ps.servicepermission;

import org.apache.commons.lang3.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * These are service permission rules that can be used for services that should be disabled
 * for particular permission users in a workflow.
 * 
 * Each class relates to a particular <code>EntitySelection</code> class.
 * The classes available are:
 * <ul>
 * <li>{@link OnTable} (abstract)</li>
 * <li>{@link OnTableView}</li>
 * <li>{@link OnRecord}</li>
 * </ul>
 * 
 * When referencing these classes, it is recommended that you import the package, and then when referencing the inner class,
 * qualify it with the outer package, like so:
 * <pre>
 * DisabledForPermissionUsersServicePermissionRules.OnRecord rule = new DisabledForPermissionUsersServicePermissionRules.OnRecord();
 * </pre>
 * 
 * There may be some overlap in functionality with {@link WorkflowPermissionProfileOnlyServicePermissionRule},
 * but that class operates on a data space entity selection and enables services rather than disables them.
 * It may be worthwhile at some point to attempt to combine the functionality, but for now they are separate.
 */
public interface DisabledForPermissionUsersServicePermissionRules
{
	public abstract class OnTable<S extends TableEntitySelection>
		implements ServicePermissionRule<S>
	{
		protected String[] permissionUsers;
		protected TrackingInfoHelper trackingInfoHelper;

		/**
		 * Constructs the rule with the given users and default tracking info helper,
		 * which looks at the first segment of the tracking info
		 * 
		 * @param permissionUsers the array of permission user names
		 */
		protected OnTable(String[] permissionUsers)
		{
			this(
				permissionUsers,
				new FirstSegmentTrackingInfoHelper(
					WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USERS));
		}

		/**
		 * Constructs the rule with the given users and tracking info helper
		 * 
		 * @param permissionUsers the array of permission user names
		 * @param trackingInfoHelper the tracking info helper
		 */
		protected OnTable(String[] permissionUsers, TrackingInfoHelper trackingInfoHelper)
		{
			this.permissionUsers = permissionUsers;
			this.trackingInfoHelper = trackingInfoHelper;
		}

		@Override
		public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
		{
			Session session = context.getSession();
			if (session.isInWorkflowInteraction(true))
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
					// TODO: This doesn't handle a tracking info that contains multiple users.
					//       That could be handled by parsing the tracking info segment and
					//       doing an outer loop.

					// If the array of permission users contains the tracking info's user then disable it
					if (ArrayUtils.contains(permissionUsers, trackingInfoSeg))
					{
						return UserServicePermission.getDisabled(
							UserMessage.createError("Service not allowed for this workflow task."));
					}
				}
			}
			return UserServicePermission.getEnabled();
		}
	}

	public class OnTableView extends OnTable<TableViewEntitySelection>
	{
		public OnTableView(String[] permissionUsers)
		{
			super(permissionUsers);
		}

		public OnTableView(String[] permissionUsers, TrackingInfoHelper trackingInfoHelper)
		{
			super(permissionUsers, trackingInfoHelper);
		}
	}

	public class OnRecord extends OnTable<RecordEntitySelection>
	{
		public OnRecord(String[] permissionUsers)
		{
			super(permissionUsers);
		}

		public OnRecord(String[] permissionUsers, TrackingInfoHelper trackingInfoHelper)
		{
			super(permissionUsers, trackingInfoHelper);
		}
	}
}
