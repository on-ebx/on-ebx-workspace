package com.orchestranetworks.ps.util.functional;

/**
 * Limits generators to a certain number of iterations. For example: <pre> EachLine.open(file).until(new
 * MaxIterations(1)); </pre> Would only "generate" 1 line from the file. <br>credit: org.apache.commons.functor. copied
 * to extend functionality for ON.
 */

@SuppressWarnings("rawtypes")
public class MaxIterations extends UnaryPredicate
{
	private int maxIters = 0;
	private int currentIter = 0;

	public MaxIterations(int maxIters)
	{
		this.maxIters = maxIters;
	}

	@Override
	public boolean test(Object obj)
	{
		if (++currentIter > maxIters)
		{
			return true;
		}

		return false;
	}
}