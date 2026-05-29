package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes no arguments and returns an <code>Object</code> value. Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}. <br>credit: org.apache.commons.functor.
 * copied to extend functionality for ON.
 */
public abstract class Function<O>
{
	/**
	 * Evaluate this function.
	 * 
	 * @return the result of this evaluation
	 */
	public abstract O evaluate();

	/** Constructs a Function that always returns the same value. */
	public static <O> Function<O> constant(final O value)
	{
		return new Function<O>()
		{
			@Override
			public O evaluate()
			{
				return value;
			}
		};
	}

	/** Takes the result of this function and passes it to the UnaryPredicate. */
	public Predicate to(final UnaryPredicate<? super O> predicate)
	{
		return new Predicate()
		{
			@Override
			public boolean test()
			{
				return predicate.test(evaluate());
			}
		};
	}

	/** Takes the result of this function and passes it to the UnaryProcedure. */
	public Procedure to(final UnaryProcedure<? super O> proc)
	{
		return new Procedure()
		{
			@Override
			public void run()
			{
				proc.run(evaluate());
			}
		};
	}

	/** Takes the result of this function and passes it to the UnaryFunction. */
	public <O2> Function<O2> to(final UnaryFunction<? super O, O2> function)
	{
		final Function<O> me = this;
		return new Function<O2>()
		{
			@Override
			public O2 evaluate()
			{
				return function.evaluate(me.evaluate());
			}
		};
	}

	public static final Function<Object> NULL = constant(null);
}
