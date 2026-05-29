package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes two arguments and returns an <code>Object</code> value. Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}.
 * 
 * credit: org.apache.commons.functor. copied to extend functionality for ON.
 */
public abstract class BinaryFunction<L, R, O>
{
	/**
	 * Evaluate this function.
	 * 
	 * @param left the first element of the ordered pair of arguments
	 * @param right the second element of the ordered pair of arguments
	 * @return the result of this function for the given arguments
	 */
	public abstract O evaluate(L left, R right);

	public Function<O> bind(final L left, final R right)
	{
		final BinaryFunction<L, R, O> me = this;
		return new Function<O>()
		{
			@Override
			public O evaluate()
			{
				return me.evaluate(left, right);
			}
		};
	}

	public UnaryFunction<R, O> bindLeft(final L left)
	{
		final BinaryFunction<L, R, O> me = this;
		return new UnaryFunction<R, O>()
		{
			@Override
			public O evaluate(R obj)
			{
				return me.evaluate(left, obj);
			}
		};
	}

	public UnaryFunction<L, O> bindRight(final R right)
	{
		final BinaryFunction<L, R, O> me = this;
		return new UnaryFunction<L, O>()
		{
			@Override
			public O evaluate(L obj)
			{
				return me.evaluate(obj, right);
			}
		};
	}

}
