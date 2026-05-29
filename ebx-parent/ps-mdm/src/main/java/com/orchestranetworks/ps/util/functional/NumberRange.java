package com.orchestranetworks.ps.util.functional;

/**
 * Generator for a numeric range. If min &lt;= max generation will be forward, if min &gt; max generation will be backward.
 */

public abstract class NumberRange<T extends Number> extends Generator<T>
{
	private final T min;
	private final T max;

	/**
	 * Create a range of numbers from min to max.
	 */
	public NumberRange(T min, T max)
	{
		if (min == null || max == null)
		{
			throw new IllegalArgumentException("min and max must not be null");
		}

		this.min = min;
		this.max = max;
	}

	/**
	 * Get min.
	 */
	public T getMin()
	{
		return min;
	}

	/**
	 * Get max.
	 */
	public T getMax()
	{
		return max;
	}

	/**
	 * Check if a number is within range.
	 */
	public abstract boolean isWithinRange(T number);

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof NumberRange))
		{
			return false;
		}
		final NumberRange numberRange = (NumberRange) o;
		if (!max.equals(numberRange.max))
		{
			return false;
		}
		if (!min.equals(numberRange.min))
		{
			return false;
		}
		return true;
	}

	@Override
	public int hashCode()
	{
		return 29 * min.hashCode() + max.hashCode();
	}

	/**
	 * ************************************************ Class methods *************************************************
	 */

	public static NumberRange<Integer> from(Integer min, Integer max)
	{
		return new IntegerRange(min, max);
	}

	public static NumberRange<Integer> from(int min, int max)
	{
		return new IntegerRange(min, max);
	}

	public static NumberRange<Long> from(Long min, Long max)
	{
		return new LongRange(min, max);
	}

	public static NumberRange<Long> from(long min, long max)
	{
		return new LongRange(min, max);
	}

	/**
	 * Integer range.
	 */
	public static class IntegerRange extends NumberRange<Integer>
	{
		public IntegerRange(Integer min, Integer max)
		{
			super(min, max);
		}

		public IntegerRange(int min, int max)
		{
			super(Integer.valueOf(min), Integer.valueOf(max));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run(UnaryProcedure proc)
		{
			checkIfStopped();

			int start = getMin().intValue();
			int end = getMax().intValue();

			if (start <= end)
			{
				for (int i = start; i <= end; i++)
				{
					proc.run(Integer.valueOf(i));
					if (isStopped())
					{
						break;
					}
				}
			}
			else
			{
				for (int i = start; i >= end; i--)
				{
					proc.run(Integer.valueOf(i));
					if (isStopped())
					{
						break;
					}
				}
			}

			stop();
		}

		@Override
		public boolean isWithinRange(Integer number)
		{
			int num = number.intValue();
			int min = getMin().intValue();
			int max = getMax().intValue();

			if (min <= max)
			{
				return num >= min && num <= max;
			}
			else
			{
				return num <= min && num >= max;
			}
		}

		@Override
		public String toString()
		{
			return "IntegerRange(" + getMin() + ", " + getMax() + ")";
		}
	}

	/**
	 * Long range.
	 */
	public static class LongRange extends NumberRange<Long>
	{
		public LongRange(Long min, Long max)
		{
			super(min, max);
		}

		public LongRange(long min, long max)
		{
			super(Long.valueOf(min), Long.valueOf(max));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run(UnaryProcedure proc)
		{
			checkIfStopped();
			long start = getMin().longValue();
			long end = getMax().longValue();

			if (start <= end)
			{
				for (long i = start; i <= end; i++)
				{
					proc.run(Long.valueOf(i));
					if (isStopped())
					{
						break;
					}
				}
			}
			else
			{
				for (long i = start; i >= end; i--)
				{
					proc.run(Long.valueOf(i));
					if (isStopped())
					{
						break;
					}
				}
			}

			stop();
		}

		@Override
		public boolean isWithinRange(Long number)
		{
			long num = number.longValue();
			long min = getMin().longValue();
			long max = getMax().longValue();

			if (min <= max)
			{
				return num >= min && num <= max;
			}
			else
			{
				return num <= min && num >= max;
			}
		}

		@Override
		public String toString()
		{
			return "LongRange(" + getMin() + ", " + getMax() + ")";
		}
	}
}