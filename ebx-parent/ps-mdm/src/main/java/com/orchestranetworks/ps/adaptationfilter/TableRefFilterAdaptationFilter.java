/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.adaptationfilter;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * This class provides an {@link AdaptationFilter} interface to a {@link TableRefFilter}.
 * This can be useful if you have code that requires an <code>AdaptationFilter</code> due to the API,
 * but you want it to behave the same way as code you already have in a <code>TableRefFilter</code>.
 * This takes in the <code>ValueContext</code> of the current record you're on, similar to what a <code>TableRefFilter</code> expects.
 */
public class TableRefFilterAdaptationFilter implements AdaptationFilter
{
	private TableRefFilter tableRefFilter;
	private ValueContext valueContext;

	public TableRefFilterAdaptationFilter(TableRefFilter tableRefFilter, ValueContext valueContext)
	{
		this.tableRefFilter = tableRefFilter;
		this.valueContext = valueContext;
	}

	@Override
	public boolean accept(Adaptation record)
	{
		return tableRefFilter.accept(record, valueContext);
	}

}
