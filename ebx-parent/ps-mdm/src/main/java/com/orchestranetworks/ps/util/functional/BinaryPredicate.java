package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * A functor that takes two arguments and returns a <code>boolean</code> value. Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}. credit: org.apache.commons.functor.
 * copied to extend functionality for ON.
 */
public abstract class BinaryPredicate<L, R>
{
	/**
	 * Evaluate this predicate.
	 * 
	 * @param left the first element of the ordered pair of arguments
	 * @param right the second element of the ordered pair of arguments
	 * @return the result of this test for the given arguments
	 */
	public abstract boolean test(L left, R right);

	public UnaryPredicate<R> bindLeft(final L left)
	{
		final BinaryPredicate<L, R> me = this;
		return new UnaryPredicate<R>()
		{
			@Override
			public boolean test(R obj)
			{
				return me.test(left, obj);
			}
		};
	}

	public UnaryPredicate<L> bindRight(final R right)
	{
		final BinaryPredicate<L, R> me = this;
		return new UnaryPredicate<L>()
		{
			@Override
			public boolean test(L obj)
			{
				return me.test(obj, right);
			}
		};
	}

	public static <L, R> BinaryPredicate<L, R> not(final BinaryPredicate<L, R> predicate)
	{
		return new BinaryPredicate<L, R>()
		{
			@Override
			public boolean test(L left, R right)
			{
				return !predicate.test(left, right);
			}
		};
	}

	public BinaryPredicate<L, R> not()
	{
		return not(this);
	}

	public BinaryPredicate<L, R> and(final BinaryPredicate<? super L, ? super R> predicate)
	{
		final BinaryPredicate<L, R> me = this;
		return new BinaryPredicate<L, R>()
		{
			@Override
			public boolean test(L left, R right)
			{
				return me.test(left, right) && predicate.test(left, right);
			}
		};
	}

	public BinaryPredicate<L, R> or(final BinaryPredicate<? super L, ? super R> predicate)
	{
		final BinaryPredicate<L, R> me = this;
		return new BinaryPredicate<L, R>()
		{
			@Override
			public boolean test(L left, R right)
			{
				return me.test(left, right) || predicate.test(left, right);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	public static final BinaryPredicate isEqual = new BinaryPredicate()
	{
		@Override
		public boolean test(Object left, Object right)
		{
			return Objects.equals(left, right);
		}
	};

	@SuppressWarnings("rawtypes")
	public static final BinaryPredicate TRUE = new BinaryPredicate()
	{
		@Override
		public boolean test(Object left, Object right)
		{
			return true;
		}

		@Override
		public BinaryPredicate or(BinaryPredicate predicate)
		{
			return this;
		}

		@Override
		public BinaryPredicate not()
		{
			return FALSE;
		}
	};

	@SuppressWarnings("rawtypes")
	public static final BinaryPredicate FALSE = new BinaryPredicate()
	{
		@Override
		public boolean test(Object left, Object right)
		{
			return false;
		}

		@Override
		public BinaryPredicate and(BinaryPredicate predicate)
		{
			return this;
		}

		@Override
		public BinaryPredicate not()
		{
			return TRUE;
		}
	};
}
