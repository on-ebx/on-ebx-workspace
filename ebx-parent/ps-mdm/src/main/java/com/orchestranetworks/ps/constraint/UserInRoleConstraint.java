/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

/**
 * This constraint enumeration can be used to create a selection field for
 * choosing a user
 */
public class UserInRoleConstraint implements Constraint<String>
{
	private static final String MESSAGE = "Specify a user in role {0}.";
	private String roleName;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		UserReference user = UserReference.forUser(aValue);
		DirectoryHandler directory = DirectoryHandler
			.getInstance(aValidationContext.getHome().getRepository());
		if (!directory.isUserDefined(user))
		{
			aValidationContext.addError("User " + aValue + " does not exist.");
		}
		if (roleName != null && !directory.isUserInRole(user, Role.forSpecificRole(roleName)))
		{
			aValidationContext.addError("User " + aValue + " is not in role " + roleName + ".");
		}
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
		if (roleName == null)
			aContext.addError("Please specify a role");
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return new MessageFormat(MESSAGE).format(roleName);
	}

	public String getRoleName()
	{
		return roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

}
