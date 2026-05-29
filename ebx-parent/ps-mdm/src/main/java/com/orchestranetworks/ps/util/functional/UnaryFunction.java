package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes one argument and returns an <code>Object</code> value. Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}. <br> credit: org.apache.commons.functor.
 * copied to extend functionality for ON.
 */
public abstract class UnaryFunction<I, O>
{
	/**
	 * Evaluate this function.
	 * 
	 * @param object the object to evaluate
	 * @return the result of this evaluation
	 */
	public abstract O evaluate(I object);

	/** Constructs a UnaryFunction that always returns the same value. */
	public static <I> UnaryFunction<I, I> constant(final I value)
	{
		return new UnaryFunction<I, I>()
		{
			@Override
			public I evaluate(I obj)
			{
				return value;
			}
		};
	}

	/** Just returns the object passed to the function. */
	public static <I> UnaryFunction<I, I> identity()
	{
		return new UnaryFunction<I, I>()
		{
			@Override
			public I evaluate(I obj)
			{
				return obj;
			}
		};
	}

	/** Just returns the object passed to the function. */
	@SuppressWarnings("rawtypes")
	public static final UnaryFunction IDENTITY = identity();

	/** Take the result of this function and pass it to the predicate when run. */
	public UnaryPredicate<I> to(final UnaryPredicate<? super O> predicate)
	{
		final UnaryFunction<I, O> me = this;
		return new UnaryPredicate<I>()
		{
			@Override
			public boolean test(I obj)
			{
				return predicate.test(me.evaluate(obj));
			}

			@Override
			public String toString()
			{
				return me + " to " + predicate;
			}
		};
	}

	/** Take the result of this function and pass it to the proc when run. */
	public UnaryProcedure<I> to(final UnaryProcedure<? super O> proc)
	{
		return new UnaryProcedure<I>()
		{
			@Override
			public void run(I obj)
			{
				proc.run(evaluate(obj));
			}
		};
	}

	/**
	 * Take the result of this function and pass it to the function passed in. Returns the value from the function
	 * passed in.
	 */
	public <O2> UnaryFunction<I, O2> to(final UnaryFunction<? super O, O2> function)
	{
		final UnaryFunction<I, O> me = this;
		return new UnaryFunction<I, O2>()
		{
			@Override
			public O2 evaluate(I obj)
			{
				return function.evaluate(me.evaluate(obj));
			}
		};
	}

	/** Take the result of this function see if it is the same as the object passed in. */
	public UnaryPredicate<I> isSame(Object asThisObject)
	{
		return to(UnaryPredicate.isSame(asThisObject));
	}

	/** Take the result of this function see if it is equal to the object passed in. */
	public UnaryPredicate<I> isEqual(Object toThisObject)
	{
		return to(UnaryPredicate.isEqual(toThisObject));
	}

	/** Take the result of this function see if it is null. */
	public UnaryPredicate<I> isNull()
	{
		return to(UnaryPredicate.isNull);
	}

	/** Take the result of this function see if it is not null. */
	public UnaryPredicate<I> isNotNull()
	{
		return to(UnaryPredicate.isNotNull);
	}

	/** Take the result of this function see if it is an instance of the class passed in. */
	public UnaryPredicate<I> isInstanceOf(Class<?> clazz)
	{
		return to(UnaryPredicate.isInstance(clazz));
	}

	/**
	 * Returns a function that will execute the wrapped function, passing the resulting value into the next execution,
	 * until the predicate matches the return value.
	 */
	@SuppressWarnings("unchecked")
	public UnaryFunction<I, I> recurse(final UnaryPredicate<? super I> until)
	{
		final UnaryFunction<I, I> me = (UnaryFunction<I, I>) this;
		return new UnaryFunction<I, I>()
		{
			@Override
			public I evaluate(I obj)
			{
				I result = me.evaluate(obj);
				if (!until.test(result))
				{
					return evaluate(result);
				}
				return result;
			}
		};
	}

	/** Turns a UnaryFunction into a Function and binds its param to the value. */
	public Function<O> bind(final I value)
	{
		final UnaryFunction<I, O> me = this;
		return new Function<O>()
		{
			@Override
			public O evaluate()
			{
				return me.evaluate(value);
			}
		};
	}

	public static final UnaryFunction<Object, Object> NULL = constant(null);

}
