/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.service.*;

/**
 * Access Rule to hide a field depending on it matching (or not matching) an XPath Predicate using attributes of the record. 
 * Also supports matching within a create context by specifying a Session Attribute Value to match on instead of a record attribute.
 */
public class HideWhenRecordMatchesPredicateAccessRule
	extends
	RestrictWhenRecordMatchesPredicateAccessRule
{
	public HideWhenRecordMatchesPredicateAccessRule(String predicate)
	{
		this(predicate, null, null, true);
	}

	public HideWhenRecordMatchesPredicateAccessRule(String predicate, boolean match)
	{
		this(predicate, null, null, match);
	}

	public HideWhenRecordMatchesPredicateAccessRule(
		String predicate,
		String sessionKey,
		String sessionValue,
		boolean match)
	{
		super(predicate, sessionKey, sessionValue, match, AccessPermission.getHidden());
	}

}
