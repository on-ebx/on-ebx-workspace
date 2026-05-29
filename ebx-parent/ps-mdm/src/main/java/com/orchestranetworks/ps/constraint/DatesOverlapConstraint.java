/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.ranges.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Makes sure multiple records in a table don't have begin and end dates that overlap with each other.
 * Note that this works for DateTime fields as well.
 * @see RangesConstraint
 */
public class DatesOverlapConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private Path commonValuePath;
	private Path beginDatePath;
	private Path endDatePath;
	private boolean nullBeginDateIsBeginningOfTime = false;
	private boolean nullEndDateIsEndOfTime = true;
	private boolean excludeBeginDateBoundary = false;
	private boolean excludeEndDateBoundary = true;

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		SchemaNode parentNode = context.getSchemaNode();
		setupNode(context, parentNode, commonValuePath, false, "commonValue");
		setupNode(context, parentNode, beginDatePath, true, "beginDatePath");
		setupNode(context, parentNode, endDatePath, true, "endDatePath");
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		SchemaNode node = context.getNode();
		return createErrorMessage(
			locale,
			node.getNode(getBeginDatePath()),
			node.getNode(getEndDatePath()));
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		if (commonValuePath == null || beginDatePath == null || endDatePath == null)
		{
			return null;
		}
		Object commonValue = recordContext.getValue(commonValuePath);
		Object beginDateValue = recordContext.getValue(beginDatePath);
		Object endDateValue = recordContext.getValue(endDatePath);
		if (commonValue == null || (beginDateValue == null && endDateValue == null))
		{
			return null;
		}
		if (beginDateValue == null && !nullBeginDateIsBeginningOfTime)
		{
			return null;
		}
		if (endDateValue == null && !nullEndDateIsEndOfTime)
		{
			return null;
		}

		AdaptationTable table = recordContext.getAdaptationTable();
		SchemaNode tableOccNode = table.getTableNode().getTableOccurrenceRootNode();
		SchemaNode commonValueNode = tableOccNode.getNode(commonValuePath);
		SchemaNode beginDateNode = tableOccNode.getNode(beginDatePath);
		SchemaNode endDateNode = tableOccNode.getNode(endDatePath);
		boolean useQuotes = AdaptationUtil.isValueQuotedInPredicate(commonValueNode);
		StringBuilder bldr = new StringBuilder();
		bldr.append(commonValuePath.format());
		bldr.append("=");
		if (useQuotes)
		{
			bldr.append("'");
		}
		bldr.append(commonValueNode.formatToXsString(commonValue));
		if (useQuotes)
		{
			bldr.append("'");
		}
		String beginDatePathStr = beginDatePath.format();
		String endDatePathStr = endDatePath.format();
		String beginDateValueStr = "'" + beginDateNode.formatToXsString(beginDateValue) + "'";
		String endDateValueStr = "'" + endDateNode.formatToXsString(endDateValue) + "'";
		if (endDateValue != null)
		{
			bldr.append(" and (date-less-than(");
			bldr.append(beginDatePathStr);
			bldr.append(",");
			bldr.append(endDateValueStr);
			bldr.append(") ");
			if (!excludeBeginDateBoundary)
			{
				bldr.append(" or date-equal(");
				bldr.append(beginDatePathStr);
				bldr.append(",");
				bldr.append(endDateValueStr);
				bldr.append(")");
			}
			if (nullBeginDateIsBeginningOfTime)
			{
				bldr.append(" or (osd:is-null(");
				bldr.append(beginDatePathStr);
				bldr.append(") and osd:is-not-null(");
				bldr.append(endDatePathStr);
				bldr.append("))");
			}
			bldr.append(")");
		}
		if (beginDateValue != null)
		{
			bldr.append(" and (date-greater-than(");
			bldr.append(endDatePathStr);
			bldr.append(",");
			bldr.append(beginDateValueStr);
			bldr.append(")");
			if (!excludeEndDateBoundary)
			{
				bldr.append(" or date-equal(");
				bldr.append(endDatePathStr);
				bldr.append(",");
				bldr.append(beginDateValueStr);
				bldr.append(")");
			}
			if (nullEndDateIsEndOfTime)
			{
				bldr.append(" or (osd:is-null(");
				bldr.append(endDatePathStr);
				bldr.append(") and osd:is-not-null(");
				bldr.append(beginDatePathStr);
				bldr.append("))");
			}
			bldr.append(")");
		}

		boolean showError = false;
		RequestResult reqRes = table.createRequestResult(bldr.toString());
		try
		{
			Adaptation existingRecord = table.lookupAdaptationByPrimaryKey(recordContext);
			// Make sure we don't error if the one we found was the same one we're on
			if (existingRecord == null)
			{
				showError = !reqRes.isEmpty();
			}
			else
			{
				Adaptation adaptation;
				while ((adaptation = reqRes.nextAdaptation()) != null && !showError)
				{
					showError = !adaptation.equals(existingRecord);
				}
			}
		}
		finally
		{
			reqRes.close();
		}
		return showError ? createErrorMessage(getLocale(), beginDateNode, endDateNode) : null;
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return recordContext.getNode(endDatePath);
	}

	// This class only handles the default locale
	protected Locale getLocale()
	{
		return Locale.getDefault();
	}

	private static void setupNode(
		ConstraintContextOnTable context,
		SchemaNode parentNode,
		Path path,
		boolean verifyIsDate,
		String paramName)
	{
		if (path == null)
		{
			context.addError(paramName + " must be specified.");
		}
		else
		{
			SchemaNode node = parentNode.getNode(path);
			if (node == null)
			{
				context.addError("Node " + path.format() + " not found.");
			}
			else
			{
				if (verifyIsDate)
				{
					SchemaTypeName type = node.getXsTypeName();
					if (!(SchemaTypeName.XS_DATE.equals(type)
						|| SchemaTypeName.XS_DATETIME.equals(type)))
					{
						context.addError(
							"Node " + path.format() + " must be of type Date or DateTime.");
					}
				}
				context.addDependencyToInsertDeleteAndModify(node);
			}
		}
	}

	private static String createErrorMessage(
		Locale locale,
		SchemaNode beginDateNode,
		SchemaNode endDateNode)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append(beginDateNode.getLabel(locale));
		bldr.append(" and ");
		bldr.append(endDateNode.getLabel(locale));
		bldr.append(" cannot overlap between records.");
		return bldr.toString();
	}

	public Path getCommonValuePath()
	{
		return this.commonValuePath;
	}

	public void setCommonValuePath(Path commonValuePath)
	{
		this.commonValuePath = commonValuePath;
	}

	public Path getBeginDatePath()
	{
		return this.beginDatePath;
	}

	public void setBeginDatePath(Path beginDatePath)
	{
		this.beginDatePath = beginDatePath;
	}

	public Path getEndDatePath()
	{
		return this.endDatePath;
	}

	public void setEndDatePath(Path endDatePath)
	{
		this.endDatePath = endDatePath;
	}

	public boolean isNullBeginDateIsBeginningOfTime()
	{
		return this.nullBeginDateIsBeginningOfTime;
	}

	public void setNullBeginDateIsBeginningOfTime(boolean nullBeginDateIsBeginningOfTime)
	{
		this.nullBeginDateIsBeginningOfTime = nullBeginDateIsBeginningOfTime;
	}

	public boolean isNullEndDateIsEndOfTime()
	{
		return this.nullEndDateIsEndOfTime;
	}

	public void setNullEndDateIsEndOfTime(boolean nullEndDateIsEndOfTime)
	{
		this.nullEndDateIsEndOfTime = nullEndDateIsEndOfTime;
	}

	public boolean isExcludeBeginDateBoundary()
	{
		return this.excludeBeginDateBoundary;
	}

	public void setExcludeBeginDateBoundary(boolean excludeBeginDateBoundary)
	{
		this.excludeBeginDateBoundary = excludeBeginDateBoundary;
	}

	public boolean isExcludeEndDateBoundary()
	{
		return this.excludeEndDateBoundary;
	}

	public void setExcludeEndDateBoundary(boolean excludeEndDateBoundary)
	{
		this.excludeEndDateBoundary = excludeEndDateBoundary;
	}
}
