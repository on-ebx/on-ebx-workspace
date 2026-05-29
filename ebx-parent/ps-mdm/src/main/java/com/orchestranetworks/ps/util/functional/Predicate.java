package com.orchestranetworks.ps.util.functional;

/**
 * A functor that takes no arguments and returns a <code>boolean</code> value. <p/> Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}. </p> credit: org.apache.commons.functor.
 * copied to extend functionality for ON.
 */
public abstract class Predicate
{
	/**
	 * Evaluate this predicate.
	 * 
	 * @return the result of this test
	 */
	public abstract boolean test();

	public Predicate and(final Predicate predicate)
	{
		final Predicate me = this;
		return new Predicate()
		{
			@Override
			public boolean test()
			{
				return me.test() && predicate.test();
			}
		};
	}

	public Predicate or(final Predicate predicate)
	{
		final Predicate me = this;
		return new Predicate()
		{
			@Override
			public boolean test()
			{
				return me.test() || predicate.test();
			}
		};
	}

	public static Predicate not(final Predicate predicate)
	{
		return new Predicate()
		{
			@Override
			public boolean test()
			{
				return !predicate.test();
			}
		};
	}

	public Predicate not()
	{
		return not(this);
	}

	public Procedure ifTrue(final Procedure proc)
	{
		return new Procedure()
		{
			@Override
			public void run()
			{
				if (test())
				{
					proc.run();
				}
			}
		};
	}

	public static final Predicate TRUE = new Predicate()
	{
		@Override
		public boolean test()
		{
			return true;
		}

		@Override
		public Predicate or(Predicate predicate)
		{
			return this;
		}

		@Override
		public Predicate not()
		{
			return FALSE;
		}
	};

	public static final Predicate FALSE = new Predicate()
	{
		@Override
		public boolean test()
		{
			return false;
		}

		@Override
		public Predicate and(Predicate predicate)
		{
			return this;
		}

		@Override
		public Predicate not()
		{
			return TRUE;
		}
	};
}
