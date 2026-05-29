/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Hide or show records based on if a field in the record matches the field in a user lookup table.
 * For example, UserCountry matches a user id to a country, and it can restrict any record that contains
 * a country field. This must be applied on an occurrence in the schema extensions.
 */
public abstract class UserTableLookupAccessRule implements AccessRule
{
	/**
	 * Get the path to the value in the record you're doing the lookup for.
	 * For example, if looking up if a user is associated with a country, this would be the adaptation's country path.
	 * 
	 * @return the path to the value in the record
	 */
	protected abstract Path getRecordValuePath();

	/**
	 * Get the table to look up the user in
	 * 
	 * @param repo the repository
	 * @return the table
	 */
	protected abstract AdaptationTable getUserLookupTable(Repository repo);

	/**
	 * Get the permission to use when the user isn't found. By default, it is hidden.
	 * 
	 * @return the permission
	 */
	protected AccessPermission getRestrictedPermission()
	{
		return AccessPermission.getHidden();
	}

	/**
	 * Get whether we should ignore looking up the user in the table altogether.
	 * By default, we ignore any Permissions Users and Tech Admins.
	 * 
	 * @param session
	 * @return whether we should ignore looking up the user
	 */
	protected boolean isLookupTableIgnoredForUser(Session session)
	{
		return WorkflowUtilities.isPermissionsUser(session)
			|| session.isUserInRole(CommonConstants.TECH_ADMIN);
	}

	/**
	 * Create the primary key values used to lookup the user.
	 * By default, assumes the primary key consists of just the user ID and the record's value, in that order.
	 * For example, if looking up if a user is associated with a country, this would be [user ID, country].
	 * This can be overridden if there are additional values or if that's not the order used.
	 * 
	 * @param user the user
	 * @param recordValue the value from the record
	 * @param adaptation the record to compare against
	 * @return the primary key values
	 */
	protected Object[] createLookupPrimaryKeyValues(
		UserReference user,
		Object recordValue,
		Adaptation adaptation)
	{
		return new Object[] { user.getUserId(), recordValue };
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isSchemaInstance() || isLookupTableIgnoredForUser(session))
		{
			return AccessPermission.getReadWrite();
		}

		Repository repo = adaptation.getHome().getRepository();
		AdaptationTable userLookupTable = getUserLookupTable(repo);
		UserReference user = session.getUserReference();

		Set<PrimaryKey> pks = createLookupPrimaryKeys(userLookupTable, user, adaptation);
		Iterator<PrimaryKey> iter = pks.iterator();

		boolean matchFound = false;
		while (!matchFound && iter.hasNext())
		{
			PrimaryKey pk = iter.next();
			matchFound = (userLookupTable.lookupAdaptationByPrimaryKey(pk) != null);
		}

		// If he has a record then he can see it, otherwise he can't
		return matchFound ? AccessPermission.getReadWrite() : getRestrictedPermission();
	}

	// Create the primary keys to look up the user with. It can return multiple primary key values
	// if the record can result in multiple lookups.
	// For example, if looking up by country and the record's country field is multi-occurring, it
	// will result in multiple primary keys to look for.
	// If there is only one then this will return a set with one element.
	private Set<PrimaryKey> createLookupPrimaryKeys(
		AdaptationTable userLookupTable,
		UserReference user,
		Adaptation adaptation)
	{
		Set<PrimaryKey> pks = new HashSet<>();

		// Get the value to compare against
		Path recordValuePath = getRecordValuePath();
		SchemaNode recordValueNode = adaptation.getSchemaNode().getNode(recordValuePath);
		// Protect against NPE in case this node does not exist in History records (could be CA).
		if (recordValueNode == null)
		{
			return pks;
		}
		// If multi-occurring
		if (recordValueNode.getMaxOccurs() > 1)
		{
			List<Object> recordValueList = adaptation.getList(recordValuePath);
			// Add the primary key for each of the values
			for (Object recordValue : recordValueList)
			{
				PrimaryKey pk = userLookupTable
					.computePrimaryKey(createLookupPrimaryKeyValues(user, recordValue, adaptation));
				if (pk != null)
				{
					pks.add(pk);
				}
			}
		}
		else
		{
			// Add the primary key for the value
			Object recordValue = adaptation.get(recordValuePath);
			if (recordValue != null)
			{
				PrimaryKey pk = userLookupTable
					.computePrimaryKey(createLookupPrimaryKeyValues(user, recordValue, adaptation));
				if (pk != null)
				{
					pks.add(pk);
				}
			}
		}
		return pks;
	}
}
