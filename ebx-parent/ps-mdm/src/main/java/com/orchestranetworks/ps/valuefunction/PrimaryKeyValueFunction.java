package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class PrimaryKeyValueFunction implements ValueFunction
{
	@Override
	public Object getValue(Adaptation adaptation)
	{
		return adaptation.getOccurrencePrimaryKey().format();
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// do nothing
	}
}
