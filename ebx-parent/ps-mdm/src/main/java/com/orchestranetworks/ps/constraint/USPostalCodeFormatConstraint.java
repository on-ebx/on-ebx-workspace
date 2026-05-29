/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * @deprecated Use {@link PostalCodeFormatConstraint instead}
 */
@Deprecated
public class USPostalCodeFormatConstraint implements Constraint<String>
{
	private static final String MESSAGE = "US Postal Code must be in one of the following formats: 99999 or 99999-9999.";
	private Path countryPath;
	private String usaCountryPrimaryKey;

	public Path getCountryPath()
	{
		return this.countryPath;
	}

	public void setCountryPath(Path countryPath)
	{
		this.countryPath = countryPath;
	}

	public String getUsaCountryPrimaryKey()
	{
		return usaCountryPrimaryKey;
	}

	public void setUsaCountryPrimaryKey(String usaCountryPrimaryKey)
	{
		this.usaCountryPrimaryKey = usaCountryPrimaryKey;
	}

	@Override
	public void checkOccurrence(String value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		// If no Country Path is Specified or if no Country is specified, then assume US Postal Format is needed
		//  (i.e. only skip US Postal Code Validation if we know it is definitely not US)
		if (getCountryPath() != null)
		{
			String country = (String) context.getValue(Path.PARENT.add(getCountryPath()));
			if (country != null && !country.equals(usaCountryPrimaryKey))
			{
				return;
			}
		}

		String regex = "^[0-9]{5}(?:-[0-9]{4})?$";

		if (!value.matches(regex))
		{
			context.addError(MESSAGE);
		}

	}
	@Override
	public void setup(ConstraintContext context)
	{
		// Do Nothing
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

}
