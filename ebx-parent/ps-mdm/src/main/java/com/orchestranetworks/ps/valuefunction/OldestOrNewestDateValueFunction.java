/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A <code>ValueFunction</code> that returns the oldest or newest value from a list of date fields, depending on how it's configured.
 * See {@link DateTimeUtils#getOldestOrNewestOfDates(Adaptation, Path[], String, String)} for more information.
 */
public class OldestOrNewestDateValueFunction implements ValueFunction
{
	private static final String SEPARATOR = ";";

	private String dateFields;
	private String oldestOrNewest;
	// Specifies how to treat nulls: If null, ignore them. If OLDEST, a null is the oldest possible date. If NEWEST, a null is the newest possible date.
	private String nullConfig;

	private Path[] dateFieldPaths;

	@Override
	public Object getValue(Adaptation adaptation)
	{
		return DateTimeUtils.getOldestOrNewestOfDates(
			adaptation,
			dateFieldPaths,
			oldestOrNewest,
			nullConfig);
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		if (dateFields == null || dateFields.trim().length() == 0)
		{
			context.addError("dateFields must be specified.");
		}
		else
		{
			SchemaNode rootNode = context.getSchemaNode()
				.getTableNode()
				.getTableOccurrenceRootNode();
			String[] dateFieldPathStrs = dateFields.split(SEPARATOR);
			if (dateFieldPathStrs.length < 2)
			{
				context.addError("At least two paths must be specified for dateFields.");
			}
			else
			{
				dateFieldPaths = new Path[dateFieldPathStrs.length];
				for (int i = 0; i < dateFieldPathStrs.length; i++)
				{
					Path dateFieldPath = Path.parse(dateFieldPathStrs[i]);
					if (dateFieldPath == null)
					{
						context.addError("Invalid format for dateFieldPaths.");
					}
					else
					{
						SchemaNode dateFieldNode = rootNode.getNode(dateFieldPath);
						if (dateFieldNode == null)
						{
							context.addError("Field " + dateFieldPath.format() + " does not exist.");
						}
						else
						{
							SchemaTypeName nodeType = dateFieldNode.getXsTypeName();
							if (!(SchemaTypeName.XS_DATE.equals(nodeType) || SchemaTypeName.XS_DATETIME.equals(nodeType)))
							{
								context.addError("Field " + dateFieldPath.format()
									+ " is not a date or dateTime field.");
							}
							else
							{
								dateFieldPaths[i] = dateFieldPath;
							}
						}
					}
				}
			}
		}
		if (oldestOrNewest == null)
		{
			context.addError("oldestOrNewest must be specified. Valid values are \""
				+ DateTimeUtils.DATE_MATCH_OLDEST + "\" or \"" + DateTimeUtils.DATE_MATCH_NEWEST
				+ "\".");
		}
		else
		{
			if (!(DateTimeUtils.DATE_MATCH_OLDEST.equalsIgnoreCase(oldestOrNewest) || DateTimeUtils.DATE_MATCH_NEWEST.equalsIgnoreCase(oldestOrNewest)))
			{
				context.addError("Invalid value for oldestOrNewest. Valid values are \""
					+ DateTimeUtils.DATE_MATCH_OLDEST + "\" or \""
					+ DateTimeUtils.DATE_MATCH_NEWEST + "\".");
			}
		}
		if (nullConfig != null
			&& !(DateTimeUtils.DATE_MATCH_OLDEST.equalsIgnoreCase(nullConfig) || DateTimeUtils.DATE_MATCH_NEWEST.equalsIgnoreCase(nullConfig)))
		{
			context.addError("Invalid value for nullConfig. If specified, valid values are \""
				+ DateTimeUtils.DATE_MATCH_OLDEST + "\" or \"" + DateTimeUtils.DATE_MATCH_NEWEST
				+ "\".");
		}
	}

	public String getDateFields()
	{
		return this.dateFields;
	}

	public void setDateFields(String dateFields)
	{
		this.dateFields = dateFields;
	}

	public String getOldestOrNewest()
	{
		return this.oldestOrNewest;
	}

	public void setOldestOrNewest(String oldestOrNewest)
	{
		this.oldestOrNewest = oldestOrNewest;
	}

	public String getNullConfig()
	{
		return this.nullConfig;
	}

	public void setNullConfig(String nullConfig)
	{
		this.nullConfig = nullConfig;
	}
}
