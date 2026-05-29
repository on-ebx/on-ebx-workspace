package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule that ensures that some fields are only
 * read-write until assigned a value and are otherwise read-only.
 * Note: the Tech Admin role will always have read-write access.
 */
public class ReadWriteUntilValueAssignedAccessRule implements AccessRule
{

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		// Will be data set when called for column permissions
		// or a record not yet saved
		if (adaptation.isSchemaInstance() || adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}
		if (isUserAlwaysReadWrite(session))
		{
			return AccessPermission.getReadWrite();
		}
		else if (adaptation.get(node.getPathInAdaptation()) == null)
		{
			return AccessPermission.getReadWrite();
		}
		else
		{
			return AccessPermission.getReadOnly();
		}
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
