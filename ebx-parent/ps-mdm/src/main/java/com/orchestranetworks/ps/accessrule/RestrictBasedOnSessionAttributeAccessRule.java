package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to restrict a field to read only or hidden based on the value of a session attribute.
 * To check for a session attribute being <code>null</code>, pass in <code>null</code> as the value.
 * To check for it not being <code>null</code>, pass in <code>NOT-NULL</code> as the value.
 */
public class RestrictBasedOnSessionAttributeAccessRule implements AccessRule
{
	/**
	 * This value can be used to indicate to check for the value not being null rather than a specific value
	 */
	public static final String NOT_NULL = "<not-null>";

	private String attributeName;
	private Object attributeValue;
	private AccessPermission restrictedPermission;

	/**
	 * Create the rule, restricting the field when the given attribute matches the given value
	 * 
	 * @param attributeName the session attribute name
	 * @param attributeValue the session attribute value to match,
	 *                       or <code>null</code> to match on <code>null</code>,
	 *                       or <code>NOT-NULL</code> to match on anything other than <code>null</code>
	 * @param restrictedPermission the restricted permission to apply when it matches
	 */
	public RestrictBasedOnSessionAttributeAccessRule(
		String attributeName,
		Object attributeValue,
		AccessPermission restrictedPermission)
	{
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.restrictedPermission = restrictedPermission;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		// Get the value from the session attribute
		Object value = session.getAttribute(attributeName);
		// Restrict it if we're looking for NOT-NULL and the value isn't null,
		// or if the value matches (including nulls)
		if ((NOT_NULL.equals(attributeValue) && value != null)
			|| Objects.equals(value, attributeValue))
		{
			return restrictedPermission;
		}
		return AccessPermission.getReadWrite();
	}
}
