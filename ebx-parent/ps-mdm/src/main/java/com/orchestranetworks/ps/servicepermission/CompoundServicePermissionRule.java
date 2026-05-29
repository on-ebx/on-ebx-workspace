package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

public class CompoundServicePermissionRule<S extends DataspaceEntitySelection>
	implements ServicePermissionRule<S>
{
	private List<ServicePermissionRule<S>> componentRules = new ArrayList<>();
	protected boolean minPermissions = true;
	private String messageSeparator = " and ";

	public CompoundServicePermissionRule()
	{
	}

	public CompoundServicePermissionRule(boolean minPermissions)
	{
		this.minPermissions = minPermissions;
	}

	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> arg0)
	{
		UserServicePermission result = UserServicePermission.getEnabled();
		if (!minPermissions)
		{
			result = UserServicePermission.getDisabled();
		}
		for (ServicePermissionRule<S> servicePermissionRule : componentRules)
		{
			UserServicePermission subResult = servicePermissionRule.getPermission(arg0);
			result = combine(result, subResult, minPermissions);
		}
		return result;
	}

	private UserServicePermission combine(
		UserServicePermission result,
		UserServicePermission newSub,
		boolean minPermissions)
	{
		if (!minPermissions)
		{
			if (newSub.isEnabled())
			{
				return newSub;
			}
		}
		if (newSub.isDisabled())
		{
			if (result.isDisabled())
				return UserServicePermission.getDisabled(
					appendMessage(result.getDisabledReason(), newSub.getDisabledReason()));
			else if (minPermissions)
				return newSub;
		}
		return result;
	}

	private UserMessage appendMessage(UserMessage left, UserMessage right)
	{
		if (left == null)
			return right;
		if (right == null)
			return left;
		StringBuilder msg = new StringBuilder();
		msg.append(left.formatMessage(Locale.getDefault())).append(getMessageSeparator()).append(
			right.formatMessage(Locale.getDefault()));
		return UserMessage.createError(msg.toString());
	}

	public CompoundServicePermissionRule<S> appendRule(ServicePermissionRule<S> rule)
	{
		componentRules.add(rule);
		return this;
	}

	public String getMessageSeparator()
	{
		return messageSeparator;
	}

	public void setMessageSeparator(String messageSeparator)
	{
		this.messageSeparator = messageSeparator;
	}
}
