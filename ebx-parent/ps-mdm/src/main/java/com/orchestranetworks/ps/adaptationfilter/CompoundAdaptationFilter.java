package com.orchestranetworks.ps.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;

/**
 * Invokes multiple filters. If <code>mustAcceptAll<code> is <code>true</code>,
 * then it only returns true if the value matches all of the filters (i.e. an "and").
 * Otherwise, it returns true if the value matches at least one of the filters (i.e. an "or").
 */
public class CompoundAdaptationFilter implements AdaptationFilter
{
	private List<AdaptationFilter> filters;
	private boolean mustAcceptAll;

	public CompoundAdaptationFilter(List<AdaptationFilter> filters)
	{
		this(filters, false);
	}

	public CompoundAdaptationFilter(List<AdaptationFilter> filters, boolean mustAcceptAll)
	{
		this.filters = filters;
		this.mustAcceptAll = mustAcceptAll;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		Iterator<AdaptationFilter> iter = filters.iterator();
		boolean accepted = false;
		boolean done = false;
		while (!done && iter.hasNext())
		{
			AdaptationFilter filter = iter.next();
			accepted = filter.accept(adaptation);
			done = (mustAcceptAll != accepted);
		}
		return accepted;
	}
}
