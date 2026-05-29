package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.ranges.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * If startDate and endDate are not null, and the endDate is before the startDate, then it is considered Canceled, so not Future 
 * If startDate is not null and today is before startDate, then Future.
 */
public class IsFutureValueFunction implements ValueFunction
{
	private Path startDatePath;
	private Path endDatePath;
	private boolean date;

	public Path getStartDatePath()
	{
		return startDatePath;
	}

	public void setStartDatePath(Path startDatePath)
	{
		this.startDatePath = startDatePath;
	}

	public Path getEndDatePath()
	{
		return endDatePath;
	}

	public void setEndDatePath(Path endDatePath)
	{
		this.endDatePath = endDatePath;
	}

	@Override
	public Object getValue(Adaptation record)
	{
		return isFuture(record, startDatePath, endDatePath, date);
	}

	public static Boolean isFuture(
		Adaptation record,
		Path startDatePath,
		Path endDatePath,
		boolean date)
	{
		Date currentDate = new Date();
		Date startDate = (Date) record.get(startDatePath);
		Date endDate = (Date) record.get(endDatePath);

		if (!DateTimeUtils.beforeInclusive(startDate, endDate))
		{
			return Boolean.FALSE;
		}

		if (date)
		{
			endDate = (Date) RangeUtils.padMax(endDate, RangeUtils.ONE_DAY_IN_MILLIS);
		}

		if (startDate != null && currentDate.before(startDate))
		{
			return Boolean.TRUE;
		}
		if (endDate != null && currentDate.after(endDate))
		{
			return Boolean.FALSE;
		}

		return Boolean.FALSE;
	}
	@Override
	public void setup(ValueFunctionContext context)
	{
		setup(context, startDatePath, "startDatePath");
		SchemaTypeName type = setup(context, endDatePath, "endDatePath");
		date = SchemaTypeName.XS_DATE.equals(type);
	}

	private SchemaTypeName setup(ValueFunctionContext context, Path datePath, String datePathName)
	{
		SchemaNode dateNode = PathUtils.setupFieldNode(context, datePath, datePathName, false);
		if (dateNode == null)
			return null; //errors already reported
		SchemaTypeName type = dateNode.getXsTypeName();
		if (!(SchemaTypeName.XS_DATE.equals(type) || SchemaTypeName.XS_DATETIME.equals(type)))
		{
			context.addError("Node " + datePath.format() + " must be of type Date or DateTime.");
		}
		return type;
	}
}
