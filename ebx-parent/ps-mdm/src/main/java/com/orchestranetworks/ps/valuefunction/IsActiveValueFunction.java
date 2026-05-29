package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This valueFunction currently assumes inclusive on the end points is Active.
 * If startDate and endDate are null, we assume active.
 * If startDate is not null and today is before startDate, then not active.
 * If endDate is not null and today is after endDate, then not active.
 */
public class IsActiveValueFunction implements ValueFunction
{
	private Path startDatePath;
	private SchemaNode startDateField;
	private Path endDatePath;
	private SchemaNode endDateField;
	private boolean date;
	private boolean nullIsActive = true;

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

	public boolean isNullIsActive()
	{
		return nullIsActive;
	}

	public void setNullIsActive(boolean nullIsActive)
	{
		this.nullIsActive = nullIsActive;
	}

	@Override
	public Object getValue(Adaptation adaptation)
	{
		return Boolean.valueOf(
			DateTimeUtils.isRecordActive(
				adaptation,
				startDateField.getPathInAdaptation(),
				endDateField.getPathInAdaptation(),
				date,
				nullIsActive));
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		startDateField = setup(context, startDatePath, "startDatePath");
		endDateField = setup(context, endDatePath, "endDatePath");
		date = SchemaTypeName.XS_DATE.equals(endDateField.getXsTypeName());
	}

	private SchemaNode setup(ValueFunctionContext context, Path datePath, String datePathName)
	{
		SchemaNode dateNode = PathUtils.setupFieldNode(context, datePath, datePathName, false);
		if (dateNode == null)
			return null; //errors already reported
		SchemaTypeName type = dateNode.getXsTypeName();
		if (!(SchemaTypeName.XS_DATE.equals(type) || SchemaTypeName.XS_DATETIME.equals(type)))
		{
			context.addError("Node " + datePath.format() + " must be of type Date or DateTime.");
		}
		return dateNode;
	}
}
