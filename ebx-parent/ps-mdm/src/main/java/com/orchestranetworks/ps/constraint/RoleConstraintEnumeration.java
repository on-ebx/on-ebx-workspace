/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

/**
 * This constraint enumeration can be used to create a selection field for
 * choosing a role
 */
public class RoleConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a role.";
	private boolean relaxed = true;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		if (!relaxed)
		{
			Role role = Role.forSpecificRole(aValue);
			DirectoryHandler directory = DirectoryHandler
				.getInstance(aValidationContext.getHome().getRepository());
			if (!directory.isSpecificRoleDefined(role))
			{
				aValidationContext.addError("Role " + aValue + " does not exist.");
			}
		}
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	@Override
	public String displayOccurrence(String aValue, ValueContext aContext, Locale aLocale)
		throws InvalidSchemaException
	{
		return getRoleLabel(aContext.getHome().getRepository(), aLocale, aValue);
	}

	public static String getRoleLabel(Repository repo, Locale locale, String roleName)
	{
		DirectoryHandler directory = DirectoryHandler.getInstance(repo);
		Role role = Role.forSpecificRole(roleName);
		if (directory.isSpecificRoleDefined(role))
		{
			return directory.displaySpecificRole(role, locale);
		}
		return role.getLabel();
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		Set<String> result = new LinkedHashSet<>();
		String curr = (String) aContext.getValue();
		if (curr != null)
		{
			result.add(curr);
		}
		DirectoryHandler directory = DirectoryHandler
			.getInstance(aContext.getHome().getRepository());

		List<Profile> profiles = directory.getProfiles(ProfileListContextBridge.getForWorkflow());
		for (Profile profile : profiles)
		{
			if (profile.isSpecificRole())
			{
				result.add(((Role) profile).getRoleName());
			}
		}
		return new ArrayList<>(result);
	}

	public boolean isRelaxed()
	{
		return relaxed;
	}

	public void setRelaxed(boolean relaxed)
	{
		this.relaxed = relaxed;
	}
}
