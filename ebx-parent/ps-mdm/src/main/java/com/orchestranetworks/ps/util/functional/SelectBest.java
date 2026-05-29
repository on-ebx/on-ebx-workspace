package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Selects the best results from a generator. Searches the generator for the best element, then searches for the ties
 * for best. Returns a generator of the best elements.
 */
public class SelectBest<T> implements Transformer<T, T>
{
	private Comparator<T> cmp;

	/**
	 * The comparator to use to compare elements.
	 */
	public SelectBest(Comparator<T> cmp)
	{
		this.cmp = cmp;
	}

	/**
	 * Returns a generator of the best elements.
	 * @return Generator of the best elements.
	 */
	@Override
	public T transform(Generator<T> generator)
	{
		Collection<T> elements = generator.toCollection();
		if (elements.isEmpty())
		{
			return null;
		}
		return Collections.max(elements, cmp);
	}
}
