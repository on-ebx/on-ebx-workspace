package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Transforms a generator into a collection. If a collection is not passed into the constructor an ArrayList will be
 * returned from the transform method. credit: org.apache.commons.functor. copied to extend functionality for ON.
 */

public class CollectionTransformer<T> implements Transformer<T, Collection<T>>
{
	private Collection<T> toFill = null;

	public CollectionTransformer()
	{
		toFill = new ArrayList<>();
	}

	public CollectionTransformer(Collection<T> toFill)
	{
		this.toFill = toFill;
	}

	@Override
	public Collection<T> transform(Generator<T> generator)
	{
		generator.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				toFill.add(obj);
			}
		});

		return toFill;
	}
}