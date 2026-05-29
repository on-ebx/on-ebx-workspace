package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Selects the best results from a generator. Searches the generator for the best element, then searches for the ties
 * for best. Returns a generator of the best elements.
 */
public class SelectBestWithTies<T> implements Transformer<T, Generator<T>>
{
	private Comparator<T> cmp;

	/**
	 * The comparator to use to compare elements.
	 */
	public SelectBestWithTies(Comparator<T> cmp)
	{
		this.cmp = cmp;
	}

	/**
	 * Returns a generator of the best elements.
	 * @return Generator of the best elements.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Generator<T> transform(Generator<T> generator)
	{
		Collection<T> elements = generator.toCollection();
		if (elements.isEmpty())
		{
			return Generator.emptyGenerator();
		}
		final T best = Collections.max(elements, cmp);
		// return the best and ties for the best
		return EachElement.from(elements).select(new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T next)
			{
				return cmp.compare(best, next) == 0;
			}
		});
	}
}
