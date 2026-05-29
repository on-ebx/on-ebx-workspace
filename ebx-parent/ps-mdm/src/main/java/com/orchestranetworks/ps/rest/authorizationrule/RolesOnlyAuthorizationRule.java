package com.orchestranetworks.ps.rest.authorizationrule;

import java.util.*;

import com.orchestranetworks.rest.security.*;
import com.orchestranetworks.service.*;

/**
 * A rule for REST services that requires user to be in one of the specified role in order to execute.
 * Authorization rules need to have no-arg constructors when configuring a REST service,
 * so this is abstract, but you would define a sub-class that specifies the list of roles.
 * To use it for a REST service function, you'd specify an "Authorization" tag, passing in the class.
 * See the EBX documentation for REST Toolkit.
 */
public abstract class RolesOnlyAuthorizationRule implements AuthorizationRule
{
	protected abstract List<Role> getRoles();

	@Override
	public AuthorizationOutcome check(AuthorizationContext context)
	{
		// Loop until we find a role that the user is in
		boolean found = false;
		Iterator<Role> iter = getRoles().iterator();
		while (!found && iter.hasNext())
		{
			Role role = iter.next();
			found = context.getSession().isUserInRole(role);
		}
		// If we found one, the user is authorized
		if (found)
		{
			return AuthorizationOutcome.getAuthorized();
		}
		// Otherwise, the user is not authorized so construct an apprpriate message
		return AuthorizationOutcome.createForbidden(createErrorMessage());
	}

	private String createErrorMessage()
	{
		StringBuilder bldr = new StringBuilder("User must be in ");
		List<Role> roles = getRoles();
		Iterator<Role> iter = roles.iterator();
		if (!iter.hasNext())
		{
			throw new IllegalStateException("getRoles must contain at least one role.");
		}
		if (roles.size() == 1)
		{
			bldr.append("role ").append(iter.next().getRoleName());
		}
		else
		{
			bldr.append("one of the roles [");
			while (iter.hasNext())
			{
				Role role = iter.next();
				bldr.append(role.getRoleName());
				if (iter.hasNext())
				{
					bldr.append(", ");
				}
			}
			bldr.append("]");
		}
		bldr.append(".");
		return bldr.toString();
	}
}
