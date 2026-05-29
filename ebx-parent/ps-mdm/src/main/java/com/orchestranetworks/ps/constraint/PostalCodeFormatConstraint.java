/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 */
public class PostalCodeFormatConstraint implements Constraint<String>
{
	private static final String USER_DOC_MESSAGE = "Postal Code must match the proper format for the country.";

	private static final String REGEX_USA = "^\\d{5}(?:-\\d{4})?$";
	private static final String REGEX_CANADA = "^[ABCEGHJKLMNPRSTVWXYZabceghjklmnprstvwxyz]\\d[ABCEGHJKLMNPRSTVWXYZabceghjklmnprstvwxyz]\\s\\d[ABCEGHJKLMNPRSTVWXYZabceghjklmnprstvwxyz]\\d$";

	private static final String MESSAGE_USA = "Value must be in the format 99999 or 99999-9999.";
	private static final String MESSAGE_CANADA = "Value must be in the format A9A 9A9, where A is a letter and 9 is a digit. Can't contain letters D, F, I, O, Q, or U.";

	protected Path countryPath;
	protected String usaCountryId;
	protected String canadaCountryId;

	private SchemaNode countryNode;

	@Override
	public void checkOccurrence(String value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		Object country = context.getValue(countryNode);
		if (country == null)
		{
			return;
		}

		String countryStr = country.toString();
		String regex;
		String message;
		if (Objects.equals(countryStr, usaCountryId))
		{
			regex = REGEX_USA;
			message = MESSAGE_USA;
		}
		else if (Objects.equals(countryStr, canadaCountryId))
		{
			regex = REGEX_CANADA;
			message = MESSAGE_CANADA;
		}
		else
		{
			// Not handling any other countries at this point so all other countries are valid
			return;
		}

		if (!value.matches(regex))
		{
			context.addError(message);
		}
	}

	@Override
	public void setup(ConstraintContext context)
	{
		// Not using PathUtils.setupFieldNode because that assumes the path is relative to the root of the record,
		// and this is relative to the field this constraint is on.
		if (countryPath == null)
		{
			context.addError("countryPath must be specified.");
		}
		else
		{
			countryNode = context.getSchemaNode().getNode(countryPath);
			if (countryNode == null)
			{
				context.addError("countryPath " + countryPath.format() + " does not exist.");
			}
		}

		if (usaCountryId == null && canadaCountryId == null)
		{
			context.addError("Either usaCountryId or canadaCountryId must be specified.");
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return USER_DOC_MESSAGE;
	}

	public Path getCountryPath()
	{
		return this.countryPath;
	}

	public void setCountryPath(Path countryPath)
	{
		this.countryPath = countryPath;
	}

	public String getUsaCountryId()
	{
		return this.usaCountryId;
	}

	public void setUsaCountryId(String usaCountryId)
	{
		this.usaCountryId = usaCountryId;
	}

	public String getCanadaCountryId()
	{
		return this.canadaCountryId;
	}

	public void setCanadaCountryId(String canadaCountryId)
	{
		this.canadaCountryId = canadaCountryId;
	}
}
