package com.orchestranetworks.ps.util;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.ranges.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class DateTimeUtils
{
	public static final String DATE_MATCH_OLDEST = "oldest";
	public static final String DATE_MATCH_NEWEST = "newest";

	/**
	 * Compares a Date's time to that of the String passed in.
	 * <p>The String passed in should be in the format:<p>
	 *<ul><li><code>HH:mm:ss</code></li></ul>
	 *
	 * @param date	Instance of Date whose time value will be compared against the string passed in.
	 * @param time	Instance of String representing the time to use for the comparison.
	 * @return true if the time portion matches the string provided
	 */
	public static boolean isDateTimeEqualToTime(Date date, String time)
	{
		boolean isEqual = false;

		SimpleDateFormat parser = new SimpleDateFormat(CommonConstants.EBX_TIME_FORMAT);

		String timeToCompare = parser.format(date);

		isEqual = timeToCompare.equalsIgnoreCase(time);

		return isEqual;
	}

	/**
	 * Compares only the time setting (HH:mm:ss) of the two Dates passed in.
	 * <p>
	 * <strong>NOTE:</strong> This method only compares the time between the two Dates. Not the actual calendar day.
	 * <p>
	 * @param firstDateTime Instance of Date
	 * @param secondDateTime Instance of Date
	 *
	 * @return True if the Time of the firstDateTime is equal to the secondDateTime.
	 */
	public static boolean isTimeEqual(Date firstDateTime, Date secondDateTime)
	{

		boolean isEqual = false;

		SimpleDateFormat parser = new SimpleDateFormat(CommonConstants.EBX_TIME_FORMAT);

		if (firstDateTime != null && secondDateTime != null)
		{
			String time1 = parser.format(firstDateTime);
			String time2 = parser.format(secondDateTime);

			isEqual = time1.equalsIgnoreCase(time2);
		}

		return isEqual;
	}

	/**
	 * Return whether the first date is before or the same as the second date
	 * @param date1 date to check is before other date
	 * @param date2 date to check is after other date
	 * @return whether date1 &lt;= date2 or true if either date is null
	 */
	public static boolean beforeInclusive(Date date1, Date date2)
	{
		if (Objects.equals(date1, date2))
			return true;
		return beforeExclusive(date1, date2);
	}

	/**
	 * Return whether the first date is before the second date
	 * @param date1 date to check is before other date
	 * @param date2 date to check is after other date
	 * @return whether date1 &lt; date2 or true if either date, but not both, are null
	 */
	public static boolean beforeExclusive(Date date1, Date date2)
	{
		if (date2 == null)
			return date1 != null;
		return date1 == null || date1.before(date2);
	}

	/**
	 * Return whether the first date is after or the same as the second date
	 * @param date1 date to check is after other date
	 * @param date2 date to check is before other date
	 * @return whether date1 &gt;= date2 or true if either date is null
	 */
	public static boolean afterInclusive(Date date1, Date date2)
	{
		if (Objects.equals(date1, date2))
			return true;
		return afterExclusive(date1, date2);
	}

	/**
	 * Return whether the first date is after the second date
	 * @param date1 date to check is after other date
	 * @param date2 date to check is before other date
	 * @return whether date1 &gt; date2 or true if either date, but not both, are null
	 */
	public static boolean afterExclusive(Date date1, Date date2)
	{
		if (date2 == null)
			return date1 != null;
		;
		return date1 == null || date1.after(date2);
	}

	/**
	 * Return whether the between date is between the Start and End Date (inclusive)
	 * @param betweenDate date to check between other two dates
	 * @param startDate early date
	 * @param endDate later date
	 * @return whether startDate &lt;= betweenDate &lt;= endDate or true if either start or end date is null
	 */
	public static boolean betweenInclusive(Date betweenDate, Date startDate, Date endDate)
	{
		if (betweenDate == null)
			return false;

		return (DateTimeUtils.beforeInclusive(startDate, betweenDate)
			&& DateTimeUtils.beforeInclusive(betweenDate, endDate));
	}

	/**
	 * Return whether the between date is between the Start and End Date (exclusive)
	 * @param betweenDate date to check between other two dates
	 * @param startDate early date
	 * @param endDate later date
	 * @return whether startDate &lt; betweenDate &lt; endDate or true if either start or end date is null
	 */
	public static boolean betweenExclusive(Date betweenDate, Date startDate, Date endDate)
	{
		if (betweenDate == null)
			return false;

		return (DateTimeUtils.beforeExclusive(startDate, betweenDate)
			&& DateTimeUtils.beforeExclusive(betweenDate, endDate));
	}

	/**
	 * Return the Current System Date (DatePortion only)
	 * @return current date
	 */
	public static Date currentDate()
	{
		return DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
	}

	/**
	 * Determine if the given record is active, meaning the current date is between its start & end dates.
	 * This is the same as {@link isRecordActive(ValueContext,Path,Path,boolean)} except it takes in an <code>Adaptation</code>.
	 *
	 * @param record the record
	 * @param startDatePath the path to the start date
	 * @param endDatePath the path to the end date
	 * @param ignoreTime if the time component should be ignored when comparing dates
	 * @param nullIsActive if a <code>null</code> for start & end dates indicates that it's active
	 * @return whether it is active
	 */
	public static boolean isRecordActive(
		Adaptation record,
		Path startDatePath,
		Path endDatePath,
		boolean ignoreTime,
		boolean nullIsActive)
	{
		return isActive(
			record.getDate(startDatePath),
			record.getDate(endDatePath),
			ignoreTime,
			nullIsActive);
	}

	/**
	 * Determine if the given record is active, meaning the current date is between its start & end dates.
	 * This is the same as {@link isRecordActive(Adaptation,Path,Path,boolean)} except it takes in a <code>ValueContext</code>.
	 *
	 * @param recordContext the record value context
	 * @param startDatePath the path to the start date
	 * @param endDatePath the path to the end date
	 * @param ignoreTime if the time component should be ignored when comparing dates
	 * @param nullIsActive if a <code>null</code> for start & end dates indicates that it's active
	 * @return whether it is active
	 */
	public static boolean isRecordActive(
		ValueContext recordContext,
		Path startDatePath,
		Path endDatePath,
		boolean ignoreTime,
		boolean nullIsActive)
	{
		return isActive(
			(Date) recordContext.getValue(startDatePath),
			(Date) recordContext.getValue(endDatePath),
			ignoreTime,
			nullIsActive);
	}

	private static boolean isActive(
		Date startDate,
		Date endDate,
		boolean ignoreTime,
		boolean nullIsActive)
	{
		Date currentDate = new Date();
		if (startDate == null && endDate == null)
		{
			return nullIsActive;
		}
		if (ignoreTime && endDate != null)
		{
			endDate = (Date) RangeUtils.padMax(endDate, RangeUtils.ONE_DAY_IN_MILLIS);
		}

		if (startDate != null && currentDate.before(startDate))
		{
			return false;
		}
		if (endDate != null && currentDate.after(endDate))
		{
			return false;
		}
		return true;
	}

	/**
	 * Parse the given string into a Date object, using the default EBX date format.
	 * Converts any <code>ParseException</code> into an <code>OperationException</code>.
	 * This is the same as calling {@link parseDateOrDateTime(String,String)} with a format of <code>CommonConstants.EBX_DATE_FORMAT</code>.
	 *
	 * @param str the date string, i.e. "2017-12-31"
	 * @return the parsed Date
	 * @throws OperationException if a <code>ParseException</code> occurs while parsing.
	 */
	public static Date parseDate(String str) throws OperationException
	{
		return parseDateOrDateTimeOrTime(str, CommonConstants.EBX_DATE_FORMAT);
	}

	/**
	 * Parse the given string into a Date object, using the default EBX date/time format.
	 * Converts any <code>ParseException</code> into an <code>OperationException</code>.
	 * This is the same as calling {@link parseDateOrDateTime(String,String)} with a format of <code>CommonConstants.EBX_DATE_TIME_FORMAT</code>.
	 *
	 * @param str the date/time string, i.e. "2017-12-31T22:00:00"
	 * @return the parsed Date
	 * @throws OperationException if a <code>ParseException</code> occurs while parsing.
	 */
	public static Date parseDateTime(String str) throws OperationException
	{
		return parseDateOrDateTimeOrTime(str, CommonConstants.EBX_DATE_TIME_FORMAT);
	}

	/**
	 * Parse the given string into a Date object, using the default EBX time format.
	 * Converts any <code>ParseException</code> into an <code>OperationException</code>.
	 * This is the same as calling {@link parseDateOrDateTimeOrTime(String,String)} with a format of <code>CommonConstants.EBX_TIME_FORMAT</code>.
	 *
	 * @param str the time string, i.e. "22:00:00"
	 * @return the parsed Date
	 * @throws OperationException if a <code>ParseException</code> occurs while parsing.
	 */
	public static Date parseTime(String str) throws OperationException
	{
		return parseDateOrDateTimeOrTime(str, CommonConstants.EBX_TIME_FORMAT);
	}

	/**
	 * Parse the given string into a Date object, using the supplied format.
	 * Converts any <code>ParseException</code> into an <code>OperationException</code>.
	 *
	 * @param str the date or date/time or time string, i.e. "2017-12-31" or "2017-12-31T22:00:00" or "22:00:00"
	 * @param format the format to apply to the string
	 * @return the parsed Date
	 * @throws OperationException if a <code>ParseException</code> occurs while parsing.
	 */
	public static Date parseDateOrDateTimeOrTime(String str, String format)
		throws OperationException
	{
		SimpleDateFormat parser = new SimpleDateFormat(format);
		try
		{
			return parser.parse(str);
		}
		catch (ParseException ex)
		{
			throw OperationException.createError(
				"Error parsing " + str + " into Date using format " + format + ".",
				ex);
		}
	}

	/**
	 * @deprecated Use {@link parseDateOrDateTimeOrTime(String,String)} instead
	 */
	@Deprecated
	public static Date parseDateOrDateTime(String str, String format) throws OperationException
	{
		return parseDateOrDateTimeOrTime(str, format);
	}

	/**
	 * Get the date from the record's list of fields that is either the newest or the oldest.
	 * This is equivalent of {@link #getOldestOrNewestOfDates(ValueContext, Path[], String, String)} except uses an <code>Adaptation</code>.
	 *
	 * @param record the record
	 * @param dateFieldPaths a list of paths for the date fields
	 * @param oldestOrNewest either {@link #DATE_MATCH_OLDEST} or {@link #DATE_MATCH_NEWEST} representing whether you're looking for the oldest or newest match
	 * @param nullConfig indicates how null values should be treated:
	 *        {@link #DATE_MATCH_OLDEST} means null is the oldest possible date,
	 *        {@link #DATE_MATCH_NEWEST} means null is the newest possible date,
	 *        null means null values should be ignored when determining oldest or newest date
	 * @return the oldest or newest date
	 */
	public static Date getOldestOrNewestOfDates(
		Adaptation record,
		Path[] dateFieldPaths,
		String oldestOrNewest,
		String nullConfig)
	{
		return getOldestOrNewestOfDates(record, null, dateFieldPaths, oldestOrNewest, nullConfig);
	}

	/**
	 * Get the date from the record's list of fields that is either the newest or the oldest.
	 * This is equivalent of {@link #getOldestOrNewestOfDates(Adaptation, Path[], String, String)} except uses a <code>ValueContext</code>.
	 *
	 * @param recordContext the <code>ValueContext</code> for the record
	 * @param dateFieldPaths a list of paths for the date fields
	 * @param oldestOrNewest either {@link #DATE_MATCH_OLDEST} or {@link #DATE_MATCH_NEWEST} representing whether you're looking for the oldest or newest match
	 * @param nullConfig indicates how null values should be treated:
	 *        {@link #DATE_MATCH_OLDEST} means null is the oldest possible date,
	 *        {@link #DATE_MATCH_NEWEST} means null is the newest possible date,
	 *        null means null values should be ignored when determining oldest or newest date
	 * @return the oldest or newest date
	 */
	public static Date getOldestOrNewestOfDates(
		ValueContext recordContext,
		Path[] dateFieldPaths,
		String oldestOrNewest,
		String nullConfig)
	{
		return getOldestOrNewestOfDates(
			null,
			recordContext,
			dateFieldPaths,
			oldestOrNewest,
			nullConfig);
	}

	// A private method that takes in either an adaptation or a value context. It would be cleaner code to simply always use value context and
	// call createValueContext() when we have an adaptation, but this is used by value functions so trying to make it as efficient as possible.
	private static Date getOldestOrNewestOfDates(
		Adaptation record,
		ValueContext recordContext,
		Path[] dateFieldPaths,
		String oldestOrNewest,
		String nullConfig)
	{
		Path fieldPath = dateFieldPaths[0];
		Date returnVal = recordContext == null ? record.getDate(fieldPath)
			: (Date) recordContext.getValue(fieldPath);
		// Matching on null if the nulls indicate the type of date we're looking for
		boolean matchOnNull = oldestOrNewest.equals(nullConfig);
		// If the first date is null and we're matching on nulls, then no need to look further
		if (returnVal == null && matchOnNull)
		{
			return returnVal;
		}
		// Look through the rest of the dates
		for (int i = 1; i < dateFieldPaths.length; i++)
		{
			fieldPath = dateFieldPaths[i];
			Date value = recordContext == null ? record.getDate(fieldPath)
				: (Date) recordContext.getValue(fieldPath);
			// If the new value is a better match (i.e. older or newer depending on what we're looking for)
			if (newDateMatchFound(returnVal, value, oldestOrNewest, nullConfig))
			{
				// If the new value is null and we're matching on null then just return it since no need to look further
				// (null is the oldest or newest you can get)
				if (value == null && matchOnNull)
				{
					return value;
				}
				// Otherwise, this is the new value to beat
				returnVal = value;
			}
		}
		return returnVal;
	}

	// Private method utilized by the getOldestOrNewestOfDates methods
	private static boolean newDateMatchFound(
		Date date1,
		Date date2,
		String oldestOrNewest,
		String nullConfig)
	{
		if (date2 == null)
		{
			// If they're both null or we're ignoring nulls then no new match was found
			if (date1 == null || nullConfig == null)
			{
				return false;
			}
			// Otherwise, a new match was found only if what we're looking for is what null represents
			return oldestOrNewest.equalsIgnoreCase(nullConfig);
		}
		// If there's a date2 but date1 is null, then it's a new match only if nulls don't represent what we're looking for
		// (which also will handle if nulls are ignored)
		if (date1 == null)
		{
			return !oldestOrNewest.equalsIgnoreCase(nullConfig);
		}
		// Both aren't null so if we're looking for oldest then we found a new match if date2 is before date1
		if (DATE_MATCH_OLDEST.equalsIgnoreCase(oldestOrNewest))
		{
			return date2.before(date1);
		}
		// Otherwise we found a new match if date2 is after date1
		return date2.after(date1);
	}
}
