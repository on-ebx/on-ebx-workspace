package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class AlwaysNullValueFunction implements ValueFunction
{

	@Override
	public Object getValue(Adaptation adaptation)
	{

		return null;
	}

	@Override
	public void setup(ValueFunctionContext arg0)
	{

	}

}
