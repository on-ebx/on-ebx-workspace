/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * In certain financial reporting scenarios, effective dates must always be the first of the month
 * and in the future with respect to some other date field (can be a path).
 */
public class DateIsFirstOfMonthConstraint implements Constraint<Date>
{
	private static final String MESSAGE1 = "{0} must be the first of the month.";
	private static final String MESSAGE2 = "{0} must be the first of the month and at earliest the specified minimum date";
	private static final String MESSAGE3 = "{0} must be the first of the month and at earliest {1}";
	private String pathToMinDate;
	private Path[] minDatePrefixPath;
	private Path minDatePath;

	public String getPathToMinDate()
	{
		return pathToMinDate;
	}

	public void setPathToMinDate(String pathToMinDate)
	{
		this.pathToMinDate = pathToMinDate;
	}

	@Override
	public void checkOccurrence(Date value, ValueContextForValidation valueContext)
		throws InvalidSchemaException
	{
		if (value != null)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(value);
			if (cal.get(Calendar.DAY_OF_MONTH) != 1)
				valueContext.addError(
					MessageFormat
						.format(MESSAGE1, valueContext.getNode().getLabel(Locale.getDefault())));
			if (minDatePath != null)
			{
				Date minDate;
				if (minDatePrefixPath != null)
					minDate = (Date) CollectionUtils.getFirstOrNull(
						AdaptationUtil
							.evaluatePath(valueContext, minDatePrefixPath, minDatePath, true));
				else
					minDate = (Date) valueContext.getValue(
						valueContext.getNode()
							.getTableNode()
							.getTableOccurrenceRootNode()
							.getNode(minDatePath)
							.getPathInAdaptation());
				if (minDate != null && minDate.after(value))
					valueContext.addError(
						MessageFormat.format(
							MESSAGE3,
							valueContext.getNode().getLabel(Locale.getDefault()),
							minDate));
			}
		}
	}

	@Override
	public void setup(ConstraintContext context)
	{
		SchemaNode field = context.getSchemaNode();
		SchemaTypeName type = field.getXsTypeName();
		if (!SchemaTypeName.XS_DATE.equals(type))
		{
			context.addError("Field must be of type Date.");
		}
		if (pathToMinDate != null)
		{
			List<Path> pathList = PathUtils.convertStringToPathList(pathToMinDate, null);
			List<SchemaNode> nodes = PathUtils.validatePath(context.getSchemaNode(), pathList);
			if (nodes.size() < pathList.size())
			{
				context.addError("Path " + pathList.get(nodes.size()).format() + " not found.");
			}
			else
			{
				minDatePath = pathList.remove(pathList.size() - 1);
				if (!pathList.isEmpty())
				{
					this.minDatePrefixPath = pathList.toArray(new Path[0]);
				}
				//ensure the last node is a date
				SchemaNode lastNode = nodes.get(nodes.size() - 1);
				type = lastNode.getXsTypeName();
				if (!SchemaTypeName.XS_DATE.equals(type))
					context.addError("Min date field must be of type Date");
			}
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		if (pathToMinDate == null)
			return MessageFormat.format(MESSAGE1, valueContext.getNode().getLabel(locale));
		return MessageFormat.format(MESSAGE2, valueContext.getNode().getLabel(locale));
	}
}
