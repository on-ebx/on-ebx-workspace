package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class LastUpdateUserIDValueFunction implements ValueFunction
{
	@Override
	public Object getValue(Adaptation adaptation)
	{
		return adaptation.getLastUser().getUserId();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// do nothing
	}
}
