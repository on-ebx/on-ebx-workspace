/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.uibeaneditor;

import org.apache.commons.lang3.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * This is a concrete subclass that lets you specify a semicolon-separated list of values to remove from the list.
 */
@Deprecated
public class DefaultRestrictedEnumerationUIBeanEditor extends RestrictedEnumerationUIBeanEditor
{
	private static final String VALUE_SEPARATOR = ";";

	protected String eliminatedValues;

	@Override
	protected boolean allowValue(Object value, ValueContext context, Session session)
	{
		if (eliminatedValues == null)
		{
			return true;
		}
		String[] arr = eliminatedValues.trim().split(VALUE_SEPARATOR);
		return !ArrayUtils.contains(arr, value);
	}

	public String getEliminatedValues()
	{
		return this.eliminatedValues;
	}

	public void setEliminatedValues(String eliminatedValues)
	{
		this.eliminatedValues = eliminatedValues;
	}
}
