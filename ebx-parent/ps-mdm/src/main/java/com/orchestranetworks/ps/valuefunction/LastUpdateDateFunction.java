package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * 
 * Gets the time of last modification of a record.
 * @author MCH
 */
public class LastUpdateDateFunction implements ValueFunction
{

	/*
	 * @see com.orchestranetworks.schema.ValueFunction#getValue(com.onwbp.adaptation.Adaptation)
	 */
	@Override
	public Object getValue(final Adaptation pRecord)
	{

		return pRecord.getTimeOfLastModification();
	}

	/*
	 * @see com.orchestranetworks.schema.ValueFunction#setup(com.orchestranetworks.schema.
	 * ValueFunctionContext)
	 */
	@Override
	public void setup(final ValueFunctionContext arg0)
	{
	}

}
