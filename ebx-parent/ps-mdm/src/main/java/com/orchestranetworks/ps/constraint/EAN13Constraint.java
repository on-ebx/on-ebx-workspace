package com.orchestranetworks.ps.constraint;

import java.util.*;

import org.apache.commons.validator.routines.checkdigit.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * 
 *         EAN validity control
 * 
 * @author MCH
 */
public class EAN13Constraint implements Constraint<String>
{

	@Override
	public void checkOccurrence(final String pValue, final ValueContextForValidation pContext)
		throws InvalidSchemaException
	{

		EAN13CheckDigit checker = new EAN13CheckDigit();

		if (checker.isValid(pValue))
		{
			pContext.addError("This EAN code does not appear to be valid.");
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
