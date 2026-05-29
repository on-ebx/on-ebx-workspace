/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to restrict access to a field depending on it matching (or not matching) an XPath Predicate using attributes of the record. 
 * This rule also supports matching within a create context by specifying a Session Attribute Value to match on instead of a record attribute.
 */
public class RestrictWhenRecordMatchesPredicateAccessRule implements AccessRule
{
	private final String predicate;
	private final String sessionKey;
	private final String sessionValue;
	private final boolean match;
	private final AccessPermission restrictedPermission;
	private Set<Role> permissiveRoles;

	public Set<Role> getPermissiveRoles()
	{
		return permissiveRoles;
	}

	public void setPermissiveRoles(Set<Role> permissiveRoles)
	{
		this.permissiveRoles = permissiveRoles;
	}

	public RestrictWhenRecordMatchesPredicateAccessRule(
		String predicate,
		AccessPermission restrictedPermission)
	{
		this(predicate, null, null, true, restrictedPermission);
	}

	public RestrictWhenRecordMatchesPredicateAccessRule(
		String predicate,
		boolean match,
		AccessPermission restrictedPermission)
	{
		this(predicate, null, null, match, restrictedPermission);
	}

	public RestrictWhenRecordMatchesPredicateAccessRule(
		String predicate,
		String sessionKey,
		String sessionValue,
		boolean match,
		AccessPermission restrictedPermission)
	{
		super();
		this.predicate = predicate;
		this.sessionKey = sessionKey;
		this.sessionValue = sessionValue;
		this.match = match;
		this.restrictedPermission = restrictedPermission;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (permissiveRoles != null)
		{
			for (Role role : permissiveRoles)
			{
				if (session.isUserInRole(role))
				{
					return AccessPermission.getReadWrite();
				}
			}
		}
		if (adaptation.isTableOccurrence() && match == adaptation.matches(predicate))
		{
			return restrictedPermission;
		}
		else if (adaptation.isSchemaInstance() && sessionKey != null)
		{
			Object val = session.getAttribute(sessionKey);
			if (val != null && match == val.equals(sessionValue))
			{
				return restrictedPermission;
			}
		}
		return AccessPermission.getReadWrite();
	}
}
