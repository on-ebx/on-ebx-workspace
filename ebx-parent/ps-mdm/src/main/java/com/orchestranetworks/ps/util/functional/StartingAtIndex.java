package com.orchestranetworks.ps.util.functional;

/**
 * This predicate allows you to start the iteration of a function at a specific index position in a collection.
 */
@SuppressWarnings("rawtypes")
public class StartingAtIndex extends UnaryPredicate
{
	private int startingAt = 0;
	private int currentIndex = 0;

	public StartingAtIndex(int startingAt)
	{
		this.startingAt = startingAt;
	}

	@Override
	public boolean test(Object object)
	{
		return currentIndex++ >= startingAt;
	}
}
