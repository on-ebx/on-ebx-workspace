package com.orchestranetworks.ps.ranges;

import java.math.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * For a range, which is an object with a pair of like-typed, comparable values, this class provides some utilities
 * for comparing two ranges, seeing if they overlap or leave a gap.
 */
public class RangeUtils
{
	public static BigDecimal ONE_DAY_IN_MILLIS = BigDecimal.valueOf(60 * 60 * 24 * 1000);
	public static BigDecimal ONE_SECOND_IN_MILLIS = BigDecimal.valueOf(1000);
	public static BigDecimal ONE = BigDecimal.valueOf(1);
	public static BigDecimal ONE_HUNDREDTH = BigDecimal.valueOf(0.01);

	public static enum State {
		Overlap, Gap
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RangeInfo<ValueContext> checkRanges(
		List<Adaptation> ranges,
		Path minPath,
		Path maxPath,
		final Adaptation existing,
		final ValueContext valueContext,
		RangeConfig config)
	{
		List<ValueContext> rangeVCs = Algorithms
			.apply(ranges, new UnaryFunction<Adaptation, ValueContext>()
			{
				@Override
				public ValueContext evaluate(Adaptation object)
				{
					if (existing != null && existing.equalsToAdaptation(object))
						return valueContext;
					return object.createValueContext();
				}
			});
		if (existing == null)
			rangeVCs.add(valueContext);
		UnaryFunction<ValueContext, Comparable> getMin = createGetValue(minPath);
		UnaryFunction<ValueContext, Comparable> getMax = createGetValue(maxPath);
		return checkRanges(rangeVCs, getMin, getMax, valueContext, config);
	}

	public static <T, C extends Comparable<C>> RangeInfo<T> checkRanges(
		List<T> ranges,
		UnaryFunction<T, C> getMin,
		UnaryFunction<T, C> getMax,
		T current,
		RangeConfig config)
	{
		sortRanges(ranges, getMin, getMax, config.isSortOnMax());
		boolean checkOverlap = config.isCheckOverlaps();
		boolean checkGap = config.isCheckGaps();
		BigDecimal paddingNum = config.getPaddingNum();
		if (current != null)
		{
			int index = ranges.indexOf(current);
			if (index == -1)
				return null;
			if (index > 0)
			{
				T pred = ranges.get(index - 1);
				RangeInfo<T> state = getState(
					pred,
					current,
					getMin,
					getMax,
					checkOverlap,
					checkGap,
					paddingNum);
				if (state != null)
					return state;
			}
			if (index < ranges.size() - 1)
			{
				T succ = ranges.get(index + 1);
				RangeInfo<T> state = getState(
					current,
					succ,
					getMin,
					getMax,
					checkOverlap,
					checkGap,
					paddingNum);
				if (state != null)
					return state;
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static final BinaryFunction<ValueContext, Path, Comparable> getValue = new BinaryFunction<ValueContext, Path, Comparable>()
	{
		@Override
		public Comparable evaluate(ValueContext left, Path right)
		{
			return (Comparable) left.getValue(right);
		}

	};

	@SuppressWarnings("rawtypes")
	public static UnaryFunction<ValueContext, Comparable> createGetValue(final Path valuePath)
	{
		return getValue.bindRight(valuePath);
	}

	@SuppressWarnings("rawtypes")
	public static UnaryFunction<Adaptation, Comparable> createGetValue(
		final Adaptation existing,
		final Comparable newValue,
		final Path valuePath)
	{
		return new UnaryFunction<Adaptation, Comparable>()
		{
			@Override
			public Comparable evaluate(Adaptation record)
			{
				if (record == null)
					return null;
				if (record.equalsToAdaptation(existing))
					return newValue;
				return (Comparable) record.get(valuePath);
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static <T, C extends Comparable<C>> RangeInfo<T> getState(
		T range1,
		T range2,
		UnaryFunction<T, C> getMin,
		UnaryFunction<T, C> getMax,
		boolean checkOverlap,
		boolean checkGap,
		BigDecimal paddingNum)
	{
		C min1 = getMin.evaluate(range1);
		C min2 = getMin.evaluate(range2);
		if ((min1 != null && min2 != null && min1.compareTo(min2) > 0)
			|| (min1 != null && min2 == null))
		{ //swap
			T temp = range1;
			range1 = range2;
			range2 = temp;
			min1 = min2;
			min2 = getMin.evaluate(range2);
		}
		min2 = (C) padMin(min2, paddingNum);
		C max1 = getMax.evaluate(range1);
		C max2 = getMax.evaluate(range2);
		if (checkOverlap && (min1 == null || max2 == null || min1.compareTo(max2) < 0)
			&& (max1 == null || min2 == null || max1.compareTo(min2) > 0))
			return new RangeInfo<>(State.Overlap, range1, range2);
		if (checkGap && min2 != null && max1 != null && min2.compareTo(max1) > 0)
			return new RangeInfo<>(State.Gap, range1, range2);
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static Comparable padMin(Comparable minVal, BigDecimal paddingNum)
	{
		if (paddingNum != null && paddingNum.doubleValue() > 0)
		{
			if (minVal instanceof Date)
			{
				if (ONE_DAY_IN_MILLIS.equals(paddingNum))
				{
					//fix issue with DST
					Calendar cal = GregorianCalendar.getInstance();
					cal.setTime((Date) minVal);
					cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1);
					return cal.getTime();
				}
				long dateInMillis = ((Date) minVal).getTime();
				return new Date(dateInMillis - paddingNum.longValue());
			}
			else if (minVal instanceof Integer)
			{
				return Integer.valueOf(((Integer) minVal).intValue() - paddingNum.intValue());
			}
			else if (minVal instanceof BigDecimal)
			{
				return ((BigDecimal) minVal).subtract(paddingNum);
			}
		}
		return minVal;
	}

	@SuppressWarnings("rawtypes")
	public static Comparable padMax(Comparable maxVal, BigDecimal paddingNum)
	{
		if (paddingNum != null && paddingNum.doubleValue() > 0)
		{
			if (maxVal instanceof Date)
			{
				if (ONE_DAY_IN_MILLIS.equals(paddingNum))
				{
					//fix issue with DST
					Calendar cal = GregorianCalendar.getInstance();
					cal.setTime((Date) maxVal);
					cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
					return cal.getTime();
				}
				long dateInMillis = ((Date) maxVal).getTime();
				return new Date(dateInMillis + paddingNum.longValue());
			}
			else if (maxVal instanceof Integer)
			{
				return Integer.valueOf(((Integer) maxVal).intValue() + paddingNum.intValue());
			}
			else if (maxVal instanceof BigDecimal)
			{
				return ((BigDecimal) maxVal).add(paddingNum);
			}
		}
		return maxVal;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T, C extends Comparable<C>> void sortRanges(List<T> ranges, RangeConfig config)
	{
		if (config.isSortOnMax())
			Collections.sort(
				ranges,
				new RangeComparator<>(
					null,
					(UnaryFunction) createGetValue(config.getMaxValuePath())));
		else
			Collections.sort(
				ranges,
				new RangeComparator<>(
					(UnaryFunction) createGetValue(config.getMinValuePath()),
					null));
	}

	public static <T, C extends Comparable<C>> void sortRanges(
		List<T> ranges,
		UnaryFunction<T, C> getMin)
	{
		Collections.sort(ranges, new RangeComparator<>(getMin, null));
	}

	public static <T, C extends Comparable<C>> void sortRanges(
		List<T> ranges,
		UnaryFunction<T, C> getMin,
		UnaryFunction<T, C> getMax,
		boolean sortOnMax)
	{
		if (sortOnMax)
			Collections.sort(ranges, new RangeComparator<>(null, getMax));
		else
			Collections.sort(ranges, new RangeComparator<>(getMin, null));
	}

	public static class RangeComparator<T, C extends Comparable<C>> implements Comparator<T>
	{
		private final UnaryFunction<T, C> getMin;
		private final UnaryFunction<T, C> getMax;

		public RangeComparator(UnaryFunction<T, C> getMin, UnaryFunction<T, C> getMax)
		{
			this.getMin = getMin;
			this.getMax = getMax;
		}

		@Override
		public int compare(T o1, T o2)
		{
			if (getMin != null)
			{
				C min1 = getMin.evaluate(o1);
				C min2 = getMin.evaluate(o2);
				if (min1 == null)
				{
					if (min2 == null)
						return 0;
					return -1;
				}
				else if (min2 == null)
				{
					return 1;
				}
				return min1.compareTo(min2);
			}
			else
			{
				C max1 = getMax.evaluate(o1);
				C max2 = getMax.evaluate(o2);
				if (max1 == null)
				{
					if (max2 == null)
						return 0;
					return 1;
				}
				else if (max2 == null)
				{
					return -1;
				}
				return max1.compareTo(max2);

			}
		}

	}

	public static class RangeInfo<T>
	{
		private final State state;
		private final T object;
		private final T otherObject;
		public RangeInfo(State state, T object, T otherObject)
		{
			super();
			this.state = state;
			this.object = object;
			this.otherObject = otherObject;
		}
		public State getState()
		{
			return state;
		}
		public T getObject()
		{
			return object;
		}
		public T getOtherObject()
		{
			return otherObject;
		}
	}
}
