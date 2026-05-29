package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Disable Delete Unless Created In Child Data Space Permission Rule
 * -- Will always be enabled for Tech Admin 
 * 
 */
public class DisableDeleteUnlessCreatedInChild
	extends
	ChildDataSpaceOnlyServicePermissionRule<RecordEntitySelection>
{
	public DisableDeleteUnlessCreatedInChild()
	{
		super();
	}

	@Override
	public UserServicePermission getPermission(
		ServicePermissionRuleContext<RecordEntitySelection> context)
	{
		// Tech Admin Role can always Delete
		if (context.getSession().isUserInRole(CommonConstants.TECH_ADMIN))
		{
			return UserServicePermission.getEnabled();
		}

		// This will Disable Delete if in the Master Data Space
		if (super.getPermission(context).isDisabled())
		{
			return UserServicePermission.getDisabled(
				UserMessage.createError(
					"You are not allowed to Delete records from the Master Data Space."));
		}

		// Checks if record existed in the initial snapshot 
		// -- if not, then that means it was created in this child data space and should be enabled
		Adaptation record = context.getEntitySelection().getRecord();
		if (AdaptationUtil.getRecordFromInitialSnapshot(record) == null)
		{
			return UserServicePermission.getEnabled();
		}

		return UserServicePermission.getDisabled(
			UserMessage.createError(
				"Record " + record.getLabel(context.getSession().getLocale())
					+ " cannot be deleted because it has already been merged into the master data space."));

	}

}
