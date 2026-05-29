/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

/**
 */
public class UserInRoleSpecifiedByFieldTableRefFilter implements TableRefFilter
{
	private static final String MESSAGE = "User must be in the selected role.";

	protected Path roleFieldPath;

	@Override
	public boolean accept(Adaptation adaptation, ValueContext valueContext)
	{
		String roleName = (String) valueContext.getValue(roleFieldPath);
		if (roleName == null)
		{
			return true;
		}
		String userId = adaptation.getOccurrencePrimaryKey().format();
		return isUserInRole(valueContext, userId, roleName);
	}

	protected boolean isUserInRole(ValueContext valueContext, String userId, String roleName)
	{
		DirectoryHandler dirHandler = DirectoryHandler.getInstance(valueContext.getHome()
			.getRepository());
		return dirHandler.isUserInRole(
			UserReference.forUser(userId),
			Role.forSpecificRole(roleName));
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		if (roleFieldPath == null)
		{
			context.addError("roleFieldPath must be specified.");
		}
		else
		{
			SchemaNode roleNode = context.getSchemaNode().getNode(roleFieldPath);
			if (roleNode == null)
			{
				context.addError("roleFieldPath " + roleFieldPath.format() + " does not exist.");
			}
			else
			{
				context.addFilterErrorMessage(MESSAGE);
			}
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	public String getRoleFieldPath()
	{
		return this.roleFieldPath.format();
	}

	public void setRoleFieldPath(String roleFieldPath)
	{
		this.roleFieldPath = Path.parse(roleFieldPath);
	}
}
