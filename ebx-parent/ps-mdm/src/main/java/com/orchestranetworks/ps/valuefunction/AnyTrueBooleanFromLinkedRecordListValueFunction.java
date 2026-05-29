/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A value function that returns true if any booleans in a collection is true.
 */
public class AnyTrueBooleanFromLinkedRecordListValueFunction
	extends
	AttributeValueListFromLinkedRecordListValueFunction
{
	@Override
	public Object getValue(Adaptation adaptation)
	{
		RequestResult reqRes = AdaptationUtil.linkedRecordLookup(adaptation, foreignKeyPath);
		try
		{
			if (reqRes == null)
			{
				return Boolean.FALSE;
			}

			Adaptation next;
			while ((next = reqRes.nextAdaptation()) != null)
			{
				if (next.get_boolean(attributePath))
				{
					return Boolean.TRUE;
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

		return Boolean.FALSE;
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// TODO: verify that attribute path has been specified and is a boolean
	}
}
