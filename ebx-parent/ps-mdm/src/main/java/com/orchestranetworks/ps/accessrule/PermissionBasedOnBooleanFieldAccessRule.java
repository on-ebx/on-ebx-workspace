/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *  Access Rule to return a specific permission on a node based on a boolean field value. 
 *  The constructors allow to specify the path to the boolean field, the boolean value to match and the specific permission to return when the value matches.
 *  The default boolean value to match is {@code true} and the default restricted permission returned is {@code ReadOnly}.
 */
public class PermissionBasedOnBooleanFieldAccessRule implements AccessRule
{
	private static final boolean DEFAULT_VALUE_TO_MATCH = true;
	private static final AccessPermission DEFAULT_RESTRICTED_PERMISSION = AccessPermission
		.getReadOnly();

	private Path booleanFieldPath;
	private boolean valueToMatch;
	private AccessPermission restrictedPermission;

	public PermissionBasedOnBooleanFieldAccessRule(Path booleanFieldPath)
	{
		this(booleanFieldPath, DEFAULT_VALUE_TO_MATCH);
	}

	public PermissionBasedOnBooleanFieldAccessRule(Path booleanFieldPath, boolean valueToMatch)
	{
		this(booleanFieldPath, valueToMatch, DEFAULT_RESTRICTED_PERMISSION);
	}

	public PermissionBasedOnBooleanFieldAccessRule(
		Path booleanFieldPath,
		boolean valueToMatch,
		AccessPermission restrictedPermission)
	{
		this.booleanFieldPath = booleanFieldPath;
		this.valueToMatch = valueToMatch;
		this.restrictedPermission = restrictedPermission;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isSchemaInstance() || adaptation.isHistory()
			|| isUserAlwaysReadWrite(session)
			|| adaptation.get_boolean(booleanFieldPath) != valueToMatch)
		{
			return AccessPermission.getReadWrite();
		}
		return restrictedPermission;
	}

	/**
	 * Defines the logic to determine if a user should always have read/write permission based on the session. 
	 * In this case, the users with the Tech Admin role will always have read/write permission. 
	 * 
	 * This behavior can be overridden if needed. 
	 *
	 * @param session the user session
	 * @return whether the session user is a Tech Admin
	 */
	protected boolean isUserAlwaysReadWrite(Session session)
	{
		return session.isUserInRole(CommonConstants.TECH_ADMIN);
	}
}
