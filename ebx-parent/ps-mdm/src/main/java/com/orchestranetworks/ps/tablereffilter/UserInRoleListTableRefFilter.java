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
public class UserInRoleListTableRefFilter implements TableRefFilter
{
	private static final String SEPARATOR = ";";

	private String requiredRoleNames;
	private String excludedRoleNames;

	@Override
	public boolean accept(Adaptation adaptation, ValueContext valueContext)
	{
		String userId = adaptation.getOccurrencePrimaryKey().format();
		DirectoryHandler dirHandler = DirectoryHandler.getInstance(adaptation.getHome()
			.getRepository());
		UserReference user = UserReference.forUser(userId);
		if (requiredRoleNames != null)
		{
			String[] roleNameArr = requiredRoleNames.split(SEPARATOR);
			for (String roleName : roleNameArr)
			{
				if (!dirHandler.isUserInRole(user, Role.forSpecificRole(roleName)))
				{
					return false;
				}
			}
		}
		if (excludedRoleNames != null)
		{
			String[] roleNameArr = excludedRoleNames.split(SEPARATOR);
			for (String roleName : roleNameArr)
			{
				if (dirHandler.isUserInRole(user, Role.forSpecificRole(roleName)))
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		if (requiredRoleNames == null && excludedRoleNames == null)
		{
			context.addError("Either requiredRoleNames or excludedRoleNames must be specified.");
		}
		else
		{
			context.addFilterErrorMessage(createMessage());
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return createMessage();
	}

	private String createMessage()
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("User ");

		if (requiredRoleNames != null)
		{
			bldr.append("must be in one of the roles [");
			bldr.append(requiredRoleNames.replaceAll(SEPARATOR, ", "));
			bldr.append("]");
			if (excludedRoleNames != null)
			{
				bldr.append(" and ");
			}
		}
		if (excludedRoleNames != null)
		{
			bldr.append("must not be in any of the roles [");
			bldr.append(excludedRoleNames.replaceAll(SEPARATOR, ", "));
			bldr.append("]");
		}
		return bldr.toString();
	}

	public String getRequiredRoleNames()
	{
		return this.requiredRoleNames;
	}

	public void setRequiredRoleNames(String requiredRoleNames)
	{
		this.requiredRoleNames = requiredRoleNames;
	}

	public String getExcludedRoleNames()
	{
		return this.excludedRoleNames;
	}

	public void setExcludedRoleNames(String excludedRoleNames)
	{
		this.excludedRoleNames = excludedRoleNames;
	}
}
