package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes one argument and returns no value. Implementors are encouraged but not required to make
 * their functors {@link java.io.Serializable Serializable}. <br> credit: org.apache.commons.functor. copied to extend
 * functionality for ON.
 */
public abstract class UnaryProcedure<T>
{
	/**
	 * Execute this procedure.
	 * 
	 * @param object a parameter to this execution
	 */
	public abstract void run(T object);

	public UnaryProcedure<T> onlyIf(final UnaryPredicate<T> predicate)
	{
		return predicate.ifTrue(this);
	}

	public static final UnaryProcedure<Object> NoOp = new UnaryProcedure<Object>()
	{
		@Override
		public void run(Object obj)
		{
		}
	};

	public UnaryProcedure<T> then(final UnaryProcedure<? super T> doThis)
	{
		final UnaryProcedure<T> me = this;
		return new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				me.run(obj);
				doThis.run(obj);
			}
		};
	}

	public UnaryProcedure<T> then(final Procedure doThis)
	{
		final UnaryProcedure<T> me = this;
		return new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				me.run(obj);
				doThis.run();
			}
		};
	}

	/**
	 * When run executes the Procedure passed with no arguments.
	 */
	public static UnaryProcedure<Object> dropAndExecute(final Procedure andExecute)
	{
		return new UnaryProcedure<Object>()
		{
			@Override
			public void run(Object obj)
			{
				andExecute.run();
			}
		};
	}

	/** Turns a UnaryProcedure into a Procedure and binds its param to the value. */
	public Procedure bind(final T value)
	{
		return new BoundProcedure<>(this, value);
	}

	public static class BoundProcedure<T> extends Procedure
	{
		private final UnaryProcedure<T> innerProcedure;
		private final T value;

		public BoundProcedure(UnaryProcedure<T> innerProcedure, T value)
		{
			this.innerProcedure = innerProcedure;
			this.value = value;
		}

		@Override
		public void run()
		{
			innerProcedure.run(value);
		}

		@Override
		public String toString()
		{
			return "BoundProcedure{" + "innerProcedure=" + innerProcedure + ", value=" + value
				+ "}";
		}
	}
}
