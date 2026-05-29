package com.orchestranetworks.ps.constraint;

import java.util.*;

import org.apache.commons.validator.routines.checkdigit.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * 
 *         IBAN validity control following ISO ISO 7064
 * 
 * @author MCH
 */
public class IBANConstraint implements Constraint<String>
{

	@Override
	public void checkOccurrence(final String pValue, final ValueContextForValidation pContext)
		throws InvalidSchemaException
	{

		String iban = pValue.replaceAll(" ", "").replaceAll("-", "");

		if (iban.startsWith("IBAN"))
		{
			iban = iban.substring(4);
		}

		IBANCheckDigit checker = new IBANCheckDigit();

		if (checker.isValid(iban))
		{
			pContext.addError("This IBAN code does not appear to be valid.");
		}
	}

	@Override
	public void setup(final ConstraintContext pContext)
	{
		pContext.setDependencyToLocalNode();
	}

	@Override
	public String toUserDocumentation(final Locale local, final ValueContext valueContext)
		throws InvalidSchemaException
	{
		return null;
	}

}
