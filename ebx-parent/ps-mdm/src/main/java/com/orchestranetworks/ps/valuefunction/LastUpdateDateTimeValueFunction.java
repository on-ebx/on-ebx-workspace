package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class LastUpdateDateTimeValueFunction implements ValueFunction
{
	@Override
	public Object getValue(Adaptation adaptation)
	{
		return adaptation.getTimeOfLastModification();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// do nothing
	}
}
