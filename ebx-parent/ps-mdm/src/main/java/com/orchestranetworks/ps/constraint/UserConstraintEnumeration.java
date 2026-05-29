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
 * choosing a user
 */
public class UserConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a user.";
	private boolean relaxed = true;
	private boolean alwaysReadOnly = false;
	private boolean simpleLabel = false;
	private String roleName;
	private boolean defaultDir = false;
	private Path rolePath;
	private SchemaNode roleNode;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		if (!relaxed)
		{
			UserReference user = UserReference.forUser(aValue);
			DirectoryHandler directory = DirectoryHandler
				.getInstance(aValidationContext.getHome().getRepository());
			if (!directory.isUserDefined(user))
			{
				aValidationContext.addError("User " + aValue + " does not exist.");
			}

			String roleNameToCheck = getRoleNameToCheck(aValidationContext);
			if (roleNameToCheck != null
				&& !directory.isUserInRole(user, Role.forSpecificRole(roleNameToCheck)))
			{
				aValidationContext
					.addError("User " + aValue + " is not in role " + roleNameToCheck + ".");
			}
		}
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
		if (roleName != null && rolePath != null)
		{
			aContext.addError("Parameters roleName and rolePath cannot both be specified.");
		}
		if (rolePath != null)
		{
			roleNode = aContext.getSchemaNode().getNode(rolePath);
			if (roleNode == null)
			{
				aContext.addError("rolePath " + rolePath.format() + " does not exist.");
			}
			else if (!relaxed)
			{
				aContext.addDependencyToInsertDeleteAndModify(roleNode);
			}
		}
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
		if (simpleLabel)
			return aValue;
		return getUserLabel(aContext.getHome().getRepository(), aLocale, aValue);
	}

	public static String getUserLabel(Repository repo, Locale locale, String userId)
	{
		if (userId == null)
			return null;
		DirectoryHandler directory = DirectoryHandler.getInstance(repo);
		UserReference user = UserReference.forUser(userId);
		if (directory.isUserDefined(user))
		{
			return directory.displayUser(user, locale);
		}
		return user.getLabel();
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

		// if this user field is always read only or is a computed attribute, there is no need to get the full list of user ids
		if (alwaysReadOnly || aContext.getNode().isTerminalValueComputed())
		{
			return new ArrayList<>(result);
		}

		Role role = Role.EVERYONE;
		String roleNameToCheck = getRoleNameToCheck(aContext);
		if (roleNameToCheck != null)
		{
			role = Role.forSpecificRole(roleNameToCheck);
		}
		List<UserReference> users;
		if (defaultDir)
		{
			try
			{
				users = new DirectoryDefaultFactory().createDirectory(aContext.getHome())
					.getUsersInRole(role);
			}
			catch (Exception e)
			{
				users = Collections.emptyList();
			}
		}
		else
		{
			users = DirectoryHandler.getInstance(aContext.getHome().getRepository())
				.getUsersInRole(role);
		}
		for (UserReference user : users)
		{
			result.add(user.getUserId());
		}
		return new ArrayList<>(result);
	}

	protected String getRoleNameToCheck(ValueContext aContext)
	{
		return roleNode == null ? roleName : (String) aContext.getValue(roleNode);
	}

	public boolean isRelaxed()
	{
		return relaxed;
	}

	public void setRelaxed(boolean relaxed)
	{
		this.relaxed = relaxed;
	}

	/**
	 * @see {@link #setAlwaysReadOnly(boolean)}
	 */
	public boolean isAlwaysReadOnly()
	{
		return alwaysReadOnly;
	}

	/**
	 * Set whether this is always used in a read only context.
	 * If so, we can be more efficient by not reading all users into the possible values list.
	 * This doesn't actually enforce that it's read only: That's done via permissions.
	 * 
	 * @param alwaysReadOnly whether this is always used in a read only context
	 */
	public void setAlwaysReadOnly(boolean alwaysReadOnly)
	{
		this.alwaysReadOnly = alwaysReadOnly;
	}

	public boolean isSimpleLabel()
	{
		return simpleLabel;
	}

	public void setSimpleLabel(boolean simpleLabel)
	{
		this.simpleLabel = simpleLabel;
	}

	public String getRoleName()
	{
		return roleName;
	}

	public void setRoleName(String roleName)
	{
		this.roleName = roleName;
	}

	public boolean isDefaultDir()
	{
		return defaultDir;
	}

	public void setDefaultDir(boolean defaultDir)
	{
		this.defaultDir = defaultDir;
	}

	public Path getRolePath()
	{
		return rolePath;
	}

	public void setRolePath(Path rolePath)
	{
		this.rolePath = rolePath;
	}
}
