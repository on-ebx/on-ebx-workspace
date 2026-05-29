/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;

/**
 * A trigger that converts postal codes to uppercase, when the country allows letters.
 * The only country supported currently is Canada.
 * Should be used in conjunction with {@link com.orchestranetworks.ps.constraint.PostalCodeFormatConstraint}.
 */
public class PostalCodeConvertToUppercaseTableTrigger extends ConvertToUppercaseTableTrigger
{
	protected Path countryPath;
	protected String canadaCountryId;

	private SchemaNode countryNode;

	/**
	 * Overridden to only convert to uppercase when country is Canada
	 */
	@Override
	protected boolean shouldConvertToUppercase(ValueContext valueContext)
	{
		boolean returnVal = super.shouldConvertToUppercase(valueContext);
		if (returnVal)
		{
			Object country = valueContext.getValue(countryNode);
			if (country == null)
			{
				returnVal = false;
			}
			else
			{
				returnVal = Objects.equals(country.toString(), canadaCountryId);
			}
		}
		return returnVal;
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);

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

		if (canadaCountryId == null)
		{
			context.addError("canadaCountryId must be specified.");
		}
	}

	public Path getCountryPath()
	{
		return this.countryPath;
	}

	public void setCountryPath(Path countryPath)
	{
		this.countryPath = countryPath;
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
