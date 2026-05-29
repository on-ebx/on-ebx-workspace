/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

import jakarta.mail.internet.*;

/**
 * Basic constraint to check that an email address value is well-formed.  Note: this won't actually
 * ping the email address or anything requiring internet access.
 * 		<severity>F=Fatal, E=Error (default), W=Warning, I=Information</severity>
 */
public class EmailAddressConstraint implements Constraint<String>
{
	private String severity = Severity.ERROR.toParsableString();

	private static final String MESSAGE = "Must be a valid email address.";

	@Override
	public void checkOccurrence(String value, ValueContextForValidation valueContext)
		throws InvalidSchemaException
	{
		if (value != null)
		{
			if (value.length() > 0)
			{
				try
				{
					InternetAddress internetAddress = new InternetAddress(value);
					internetAddress.validate();
				}
				catch (AddressException ex)
				{
					valueContext.addMessage(
						AdaptationUtil.createUserMessage(
							MESSAGE + " " + ex.getMessage(),
							Severity.parseFlag(severity)));
				}
			}
		}
	}
	@Override
	public void setup(ConstraintContext context)
	{
		// do nothing
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

}
