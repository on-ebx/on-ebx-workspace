package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class CreateDateTimeValueFunction implements ValueFunction
{
	@Override
	public Object getValue(Adaptation adaptation)
	{

		return adaptation.getTimeOfCreation();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// do nothing
	}
}
