package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes two arguments and has no return value. Implementors are encouraged but not required to make
 * their functors {@link java.io.Serializable Serializable}. <br>credit: org.apache.commons.functor. copied to extend
 * functionality for ON.
 */
public abstract class BinaryProcedure<L, R>
{
	/**
	 * Execute this procedure.
	 * 
	 * @param left the first element of the ordered pair of arguments
	 * @param right the second element of the ordered pair of arguments
	 */
	public abstract void run(L left, R right);

	public static final BinaryProcedure<Object, Object> NoOp = new BinaryProcedure<Object, Object>()
	{
		@Override
		public void run(Object left, Object right)
		{
		}
	};

	public Procedure bind(final L left, final R right)
	{
		final BinaryProcedure<L, R> me = this;
		return new Procedure()
		{
			@Override
			public void run()
			{
				me.run(left, right);
			}
		};
	}

	public UnaryProcedure<R> bindLeft(final L left)
	{
		final BinaryProcedure<L, R> me = this;
		return new UnaryProcedure<R>()
		{
			@Override
			public void run(R obj)
			{
				me.run(left, obj);
			}
		};
	}

	public UnaryProcedure<L> bindRight(final R right)
	{
		final BinaryProcedure<L, R> me = this;
		return new UnaryProcedure<L>()
		{
			@Override
			public void run(L obj)
			{
				me.run(obj, right);
			}
		};
	}

	/**
	 * When run executes the UnaryProcedure passed in with the right hand argument to the binary function.
	 */
	public static <R> BinaryProcedure<?, R> dropLeftAndExecute(
		final UnaryProcedure<? super R> andExecute)
	{
		return new BinaryProcedure<Object, R>()
		{
			@Override
			public void run(Object left, R right)
			{
				andExecute.run(right);
			}
		};
	}

	/**
	 * When run executes the UnaryProcedure passed in with the left hand argument to the binary function.
	 */
	public static <L> BinaryProcedure<L, ?> dropRightAndExecute(
		final UnaryProcedure<? super L> andExecute)
	{
		return new BinaryProcedure<L, Object>()
		{
			@Override
			public void run(L left, Object right)
			{
				andExecute.run(left);
			}
		};
	}

	/**
	 * When run executes the Procedure passed with no arguments.
	 */
	public static BinaryProcedure<Object, Object> dropAndExecute(final Procedure andExecute)
	{
		return new BinaryProcedure<Object, Object>()
		{
			@Override
			public void run(Object left, Object right)
			{
				andExecute.run();
			}
		};
	}

	public BinaryProcedure<L, R> then(final BinaryProcedure<L, R> doThis)
	{
		final BinaryProcedure<L, R> me = this;
		return new BinaryProcedure<L, R>()
		{
			@Override
			public void run(L left, R right)
			{
				me.run(left, right);
				doThis.run(left, right);
			}
		};
	}
}
