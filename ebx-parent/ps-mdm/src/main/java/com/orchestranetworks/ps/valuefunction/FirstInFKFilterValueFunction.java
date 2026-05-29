package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

public class FirstInFKFilterValueFunction implements ValueFunction
{
	private SchemaNode fkNode;
	private String filter;
	public String getFilter()
	{
		return filter;
	}

	public void setFilter(String filter)
	{
		this.filter = filter;
	}

	@Override
	public Object getValue(Adaptation adaptation)
	{
		RequestResult rr = AdaptationUtil.performFKFilterQuery(adaptation, fkNode, filter);
		try
		{
			Adaptation next = rr.nextAdaptation();
			if (next != null)
			{
				return next.getOccurrencePrimaryKey().format();
			}
		}
		finally
		{
			rr.close();
		}
		return null;
	}

	@Override
	public void setup(ValueFunctionContext arg0)
	{
		fkNode = arg0.getSchemaNode();
		SchemaFacetTableRef ref = fkNode.getFacetOnTableReference();
		if (ref == null)
			arg0.addError("First in Foreign key value function is only applicable to foreign key fields");
	}

}
