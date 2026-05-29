/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A value function that returns the Min or Max Date from a collection of Dates.
 */
public class MinMaxDateFromLinkedRecordListValueFunction
	extends
	AttributeValueListFromLinkedRecordListValueFunction
{
	protected boolean getMaxValue = false;

	public boolean isGetMaxValue()
	{
		return getMaxValue;
	}

	public void setGetMaxValue(boolean getMaxValue)
	{
		this.getMaxValue = getMaxValue;
	}

	@Override
	public Object getValue(Adaptation adaptation)
	{
		Date desiredDate = null;
		RequestResult reqRes = AdaptationUtil.linkedRecordLookup(adaptation, foreignKeyPath);
		try
		{
			if (reqRes == null)
			{
				return null;
			}
			Adaptation next;
			while ((next = reqRes.nextAdaptation()) != null)
			{
				Date nextDate = next.getDate(attributePath);
				if (desiredDate == null || (nextDate != null
					&& (getMaxValue ? nextDate.after(desiredDate) : nextDate.before(desiredDate))))
				{
					desiredDate = nextDate;
				}
			}
		}
		finally
		{
			if (reqRes != null)
			{
				reqRes.close();
			}
		}

		return desiredDate;
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// TODO: verify that attribute path has been specified and is a date
	}
}
