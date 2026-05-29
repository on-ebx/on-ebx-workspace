package com.orchestranetworks.ps.util;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
	/**
	 * private constructor -- this class is not for instantiation...
	 */
	@SuppressWarnings("deprecation")
	private DateUtils()
	{
	}

	public static final long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;

	/** Date fields */
	public static enum DateConstant {
		YEAR, HALFYEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE, SECOND
	};

	/** Roll up policy constants */
	public static enum RollUpPolicy {
		ROLL_NONE, ROLL_DOWN, ROLL_UP
	};

	// ///////////////////////////////////////////////////////////
	// expression functions
	// ///////////////////////////////////////////////////////////

	/**
	 * Adds the specified number of periods to the date/time.
	 * 
	 * @param aDate
	 *            The date/time to which to add the specified periods.
	 * @param periodType
	 *            The kind of period to add (Year, Half-Year, Quarter, Month,
	 *            Week, Day, Hour, Minute, or Second).
	 * @param periodsToAdd
	 *            The number of periods to add.
	 * @return The resulting temporal type or null if the input and period type
	 *         are inconsistent.
	 */
	public static Date add(java.util.Date aDate, DateConstant periodType, int periodsToAdd)
	{
		return addUsingRollPolicy(aDate, periodType, periodsToAdd, RollUpPolicy.ROLL_DOWN);
	}

	/**
	 * Adds the specified number of periods to the date/time.<br>
	 * 
	 * @param aDate
	 *            The date/time to which to add the specified periods.
	 * @param periodType
	 *            The kind of period to add (Year, Half-Year, Quarter, Month,
	 *            Week, Day, Hour, Minute, or Second).
	 * @param periodsToAdd
	 *            The number of periods to add.
	 * @param roundingPolicy
	 *            The rounding policy to use (Roll-up or Roll-down).
	 * @return The resulting temporal type or null if the input and period type
	 *         are inconsistent.
	 */
	public static Date addUsingRollPolicy(
		java.util.Date aDate,
		DateConstant periodType,
		int periodsToAdd,
		RollUpPolicy roundingPolicy)
	{
		if (aDate == null)
		{
			return null;
		}
		if (roundingPolicy == null)
		{
			roundingPolicy = RollUpPolicy.ROLL_NONE;
		}
		if (periodType == null)
		{
			periodType = DateConstant.YEAR;
		}
		Calendar cal = getCalendar(aDate);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		int milliSeconds = cal.get(Calendar.MILLISECOND);
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

		int minFieldType = _YEAR;
		if (DateConstant.YEAR.equals(periodType))
		{
			periodsToAdd = periodsToAdd * 12;
		}
		else if (DateConstant.HALFYEAR.equals(periodType))
		{
			periodsToAdd = periodsToAdd * 6;
		}
		else if (DateConstant.QUARTER.equals(periodType))
		{
			periodsToAdd = periodsToAdd * 3;
		}
		else if (DateConstant.MONTH.equals(periodType))
		{
			// nothing to do yet
		}
		else if (DateConstant.WEEK.equals(periodType))
		{
			dayOfYear += periodsToAdd * 7;
			minFieldType = _DAY;
		}
		else if (DateConstant.DAY.equals(periodType))
		{
			dayOfYear += periodsToAdd;
			minFieldType = _DAY;
		}
		else if (DateConstant.HOUR.equals(periodType))
		{
			hour += periodsToAdd;
			minFieldType = _HOUR;
		}
		else if (DateConstant.MINUTE.equals(periodType))
		{
			minute += periodsToAdd;
			minFieldType = _MINUTE;
		}
		else
		{ // if (SECOND.equals(periodType)) {
			second += periodsToAdd;
			minFieldType = _SECOND;
		}
		long millis = 0;
		if (minFieldType < _YEAR)
		{
			int[] fields = new int[] { second, minute, hour, dayOfYear, year };
			makeCanonical(fields, minFieldType);
			// dealing with time and day change -- ignores roll
			millis = getTimeInMillisForDayOfYear(fields, true);
		}
		else
		{
			// normalize month
			year += periodsToAdd / 12;
			month += periodsToAdd % 12;
			if (month > 12)
			{
				month -= 12;
				year++;
			}
			else if (month < 1)
			{
				month += 12;
				year--;
			}
			// roll conditions -- roll for leap year if we are changing year
			if (month == 2 && day == 29 && !isLeapYear(year))
			{
				if (RollUpPolicy.ROLL_UP.equals(roundingPolicy))
				{
					month = 3;
					day = 1;
				}
				else
				{
					day = 28;
				}
			}
			else
			{
				int daysInMonth = daysInMonth(year, month);
				if (!RollUpPolicy.ROLL_NONE.equals(roundingPolicy))
				{
					if (day > daysInMonth)
					{
						if (RollUpPolicy.ROLL_UP.equals(roundingPolicy))
						{
							month++;
							day = 1;
						}
						else
						{ // roundingPolicy == ROLL_DOWN
							day = daysInMonth;
						}
					}
				}
				else
				{
					// normalize day
					if (day > daysInMonth)
					{
						day -= daysInMonth;
						month++;
					}
				}
			}
			millis = getTimeInMillis(year, month, day, hour, minute, second, true);
		}
		// Preserve the original input type if one of our Date or Time types.
		// All other types
		// become DateTime (corresponds to conversion to java.util.Date in
		// releases < 8.5).
		millis += milliSeconds;
		return new Date(millis);
	}

	/**
	 * Return a temporal representing the first day of the month that contains
	 * the given date. The return type corresponds to the input type (i.e. a
	 * {@link Date} or {@link DateTime} or <code>null</code> if the input type
	 * is a {@link Time} - which has no date component).
	 */
	public static Date firstDayOfMonth(java.util.Date aDate)
	{
		if (aDate == null)
		{
			return null;
		}
		Calendar cal = getCalendar(aDate);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * Return the number of days in the given month for the given year.<br>
	 * 
	 * @return 28, 29, 30, or 31
	 */
	public static int daysInMonth(int year, int month)
	{
		if (month == 2)
		{
			return (isLeapYear(year)) ? 29 : 28;
		}
		else if (month == 4 || month == 6 || month == 9 || month == 11)
		{
			return 30;
		}
		return 31;
	}

	/**
	 * Return the number of days until another date.
	 */
	public static Integer daysUntil(java.util.Date aDate, java.util.Date otherDate)
	{
		return periodsBetween(aDate, otherDate, DateConstant.DAY);
		/*
		 * alternate implementation if (aDate == null) return 0; long d1 =
		 * aDate.getTime(); long d2 = otherDate.getTime(); long diff = d2 - d1;
		 * // Account for daylight savings time. if (diff > 0) diff +=
		 * MILLISECONDS_PER_DAY / 2; else diff -= MILLISECONDS_PER_DAY / 2;
		 * return (int)(diff / MILLISECONDS_PER_DAY);
		 */
	}

	/**
	 * Return a temporal representing the last day of the month that contains
	 * the given date. The return type corresponds to the input type (i.e. a
	 * {@link Date} or {@link DateTime} or <code>null</code> if the input type
	 * is a {@link Time} - which has no date component).
	 */
	public static Date lastDayOfMonth(java.util.Date aDate)
	{
		if (aDate == null)
		{
			return null;
		}
		Calendar cal = getCalendar(aDate);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		cal.set(Calendar.DAY_OF_MONTH, daysInMonth(year, month));
		return createDate(cal);
	}

	protected static Date createDate(Calendar cal)
	{
		return cal.getTime();
	}

	/**
	 * Computes the number of periods between this date/time and another (later)
	 * date/time.<br>
	 * Note: This method probably needs a more efficient implementation.
	 * 
	 * @param firstDate
	 *            The first date.
	 * @param lastDate
	 *            The last date.
	 * @param periodType
	 *            The kind of date period to use in making the computation.
	 * @return The number of periods between the two date/time instances or null
	 *         if the inputs and period type are inconsistent.
	 */
	public static Integer periodsBetween(
		java.util.Date firstDate,
		java.util.Date lastDate,
		DateConstant periodType)
	{
		if (firstDate == null || lastDate == null)
			return null;
		int addsBeforeLaterDatePassed = 0;
		java.util.Date interimDate = firstDate;
		while (interimDate != null
			&& (interimDate.equals(lastDate) || interimDate.before(lastDate)))
		{
			interimDate = add(interimDate, periodType, 1);
			addsBeforeLaterDatePassed++;
		}
		return Integer.valueOf(addsBeforeLaterDatePassed - 1);
	}

	/**
	 * Subtracts the specified number of periods from the date/time.<br>
	 * 
	 * @param periodType
	 *            The kind of period to subtract (YEAR, HALFYEAR, QUARTER,
	 *            MONTH, WEEK, or DAY).
	 * @param periodsToSubtract
	 *            The number of periods to subtract.
	 * @return The resulting temporal type or null if the input and period type
	 *         are inconsistent.
	 */
	public static Date subtract(
		java.util.Date aDate,
		DateConstant periodType,
		int periodsToSubtract)
	{
		return subtractUsingRollPolicy(
			aDate,
			periodType,
			periodsToSubtract,
			RollUpPolicy.ROLL_DOWN);
	}

	/**
	 * Subtracts the specified number of periods from the date/time.<br>
	 * 
	 * @param periodType
	 *            The kind of period to subtract (YEAR, HALFYEAR, QUARTER,
	 *            MONTH, WEEK, or DAY).
	 * @param periodsToSubtract
	 *            The number of periods to subtract.
	 * @param roundingPolicy
	 *            The rounding policy to use (ROLL_UP or ROLL_DOWN).
	 * @return The resulting temporal type or null if the input and period type
	 *         are inconsistent.
	 */
	public static Date subtractUsingRollPolicy(
		java.util.Date aDate,
		DateConstant periodType,
		int periodsToSubtract,
		RollUpPolicy roundingPolicy)
	{
		return addUsingRollPolicy(aDate, periodType, -periodsToSubtract, roundingPolicy);
	}

	/**
	 * Statically construct a date from the input-fields.
	 * 
	 * @param date
	 *            the input date
	 * @param time
	 *            the input time
	 * @return new DateTime
	 */
	public static Date createDateTimeFromDateAndTime(Date date, Date time)
	{
		if (date == null || time == null)
		{
			return null;
		}
		Calendar calendar = getCalendar(time);
		Calendar datePart = getCalendar(date);
		calendar.set(Calendar.ERA, datePart.get(Calendar.ERA));
		calendar.set(Calendar.YEAR, datePart.get(Calendar.YEAR));
		calendar.set(Calendar.DAY_OF_YEAR, datePart.get(Calendar.DAY_OF_YEAR));
		return calendar.getTime();
	}

	/**
	 * Return the number of years between the first and second date instances
	 */
	public static Integer yearsUntil(Date aDate, java.util.Date laterDate)
	{
		return periodsBetween(aDate, laterDate, DateConstant.YEAR);
	}

	/**
	 * Return a {@link Date} representing yesterday.
	 */
	public static Date yesterday()
	{
		return subtract(new Date(), DateConstant.DAY, 1);
	}

	/**
	 * Return the number of months between the first and second date
	 */
	public static Integer monthsUntil(java.util.Date aDate, java.util.Date laterDate)
	{
		return periodsBetween(aDate, laterDate, DateConstant.MONTH);
	}

	// ///////////////////////////////////////////////////////
	// private utility methods for dealing with calendars
	// ///////////////////////////////////////////////////////

	/**
	 * Return the indicated Calendar field.
	 * 
	 * @param aDate
	 *            The date to parse.
	 * @param field
	 *            One of the Calendar field values
	 */
	private static Integer getField(java.util.Date aDate, int field)
	{
		if (aDate == null)
		{
			return null; // no date in time or time in date
		}
		Calendar cal = getCalendar(aDate);
		return Integer.valueOf(cal.get(field));
	}

	protected static long getTimeInMillis(
		int year,
		int month,
		int day,
		int hour,
		int minute,
		int second)
	{
		return getTimeInMillis(year, month, day, hour, minute, second, true);
	}

	/**
	 * Return millisecond equivalent to year, month, day, hour, minute, second.
	 * Month is 1-based.
	 */
	protected static long getTimeInMillis(
		int year,
		int month,
		int day,
		int hour,
		int minute,
		int second,
		boolean lenient)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(lenient);
		calendar.set(year, month - 1, day, hour, minute, second);
		long millis = calendar.getTimeInMillis();
		return millis;
	}

	private static final int _YEAR = 4;
	private static final int _DAY = 3;
	private static final int _HOUR = 2;
	private static final int _MINUTE = 1;
	private static final int _SECOND = 0;
	private static final int[] _MAX = new int[] { 60, 60, 24 };

	private static void makeCanonical(int[] fields, int fieldType)
	{
		if (fieldType == _DAY)
		{
			int year = fields[_YEAR];
			int dayOfYear = fields[_DAY];
			// now we know the year, we can normalize dayOfYear
			int daysInYear = isLeapYear(year) ? 366 : 365;
			while (dayOfYear > daysInYear)
			{
				dayOfYear -= daysInYear;
				year++;
				daysInYear = isLeapYear(year) ? 366 : 365;
			}
			while (dayOfYear < 1)
			{
				year--;
				daysInYear = isLeapYear(year) ? 366 : 365;
				dayOfYear += daysInYear;
			}
			fields[_YEAR] = year;
			fields[_DAY] = dayOfYear;
		}
		else
		{
			int small = fields[fieldType];
			int large = fields[fieldType + 1];
			int max = _MAX[fieldType];
			large += small / max;
			small = small % max;
			fields[fieldType] = small;
			fields[fieldType + 1] = large;
			makeCanonical(fields, fieldType + 1);
		}
	}

	/**
	 * Return millisecond equivalent to year, dayOfYear, hour, minute, second.
	 */
	protected static long getTimeInMillisForDayOfYear(int[] fields, boolean lenient)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(lenient);
		calendar.set(Calendar.YEAR, fields[_YEAR]);
		calendar.set(Calendar.DAY_OF_YEAR, fields[_DAY]);
		calendar.set(Calendar.HOUR_OF_DAY, fields[_HOUR]);
		calendar.set(Calendar.MINUTE, fields[_MINUTE]);
		calendar.set(Calendar.SECOND, fields[_SECOND]);
		long millis = calendar.getTimeInMillis();
		return millis;
	}

	/**
	 * Construct a {@link DateTime} from a year, month, day.
	 * 
	 * @return new DateTime
	 */
	protected static Date getDateTime(int year, int month, int day)
	{
		return new Date(getTimeInMillis(year, month, day, 0, 0, 0));
	}

	/**
	 * Construct a {@link DateTime} from a year, month, day, hour, minute,
	 * second.
	 * 
	 * @return new DateTime
	 */
	protected static Date getDateTime(
		int year,
		int month,
		int day,
		int hour,
		int minute,
		int second)
	{
		return new Date(getTimeInMillis(year, month, day, hour, minute, second));
	}

	private static Calendar getCalendar(java.util.Date aDate)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(aDate);
		return cal;
	}

	/**
	 * Return the current time as a Date.
	 */
	public static Date today()
	{
		return new Date(System.currentTimeMillis());
	}

	/**
	 * Return month (plus one since we want 1 = January).<br>
	 * 
	 * @return month base 1
	 */
	public static int getMonth(Date aDate)
	{
		return getField(aDate, Calendar.MONTH) + 1;
	}

	/**
	 * Return this year.<br>
	 * 
	 * @return year
	 */
	public static int getYear(Date aDate)
	{
		return getField(aDate, Calendar.YEAR);
	}

	public static boolean isLeapYear(int year)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
	}
}
