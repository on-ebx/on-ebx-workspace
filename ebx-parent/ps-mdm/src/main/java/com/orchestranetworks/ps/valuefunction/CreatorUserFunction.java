package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * 
 * Gets the id of the user who created the record.
 * @author MCH
 */
public class CreatorUserFunction implements ValueFunction
{

	/*
	 * @see com.orchestranetworks.schema.ValueFunction#getValue(com.onwbp.adaptation.Adaptation)
	 */
	@Override
	public Object getValue(final Adaptation pRecord)
	{

		return pRecord.getCreator();
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
