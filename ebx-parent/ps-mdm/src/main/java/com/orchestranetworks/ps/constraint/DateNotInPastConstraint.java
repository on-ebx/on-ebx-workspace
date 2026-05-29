/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Constraint that can be used on a date or dateTime field to ensure that the value of the date
 * is not in the past at the time it is entered.
 */
public class DateNotInPastConstraint implements Constraint<Date>
{
	private static final String MESSAGE = "Date must not be in the past.";

	@Override
	public void checkOccurrence(Date value, ValueContextForValidation valueContext)
		throws InvalidSchemaException
	{
		if (value != null)
		{
			Adaptation record = AdaptationUtil.getRecordForValueContext(valueContext);
			if (record != null
				&& value.equals(record.getDate(valueContext.getNode().getPathInAdaptation())))
			{
				return;
			}

			Calendar cal = Calendar.getInstance();
			SchemaTypeName type = valueContext.getNode().getXsTypeName();
			if (SchemaTypeName.XS_DATE.equals(type))
			{
				cal = DateUtils.truncate(cal, Calendar.DATE);
			}
			Date now = cal.getTime();
			if (now.after(value))
			{
				valueContext.addError(MESSAGE);
			}
		}
	}
	@Override
	public void setup(ConstraintContext context)
	{
		SchemaTypeName type = context.getSchemaNode().getXsTypeName();
		if (!SchemaTypeName.XS_DATE.equals(type) || SchemaTypeName.XS_DATETIME.equals(type))
		{
			context.addError("Node must be of type Date or DateTime.");
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}
}
