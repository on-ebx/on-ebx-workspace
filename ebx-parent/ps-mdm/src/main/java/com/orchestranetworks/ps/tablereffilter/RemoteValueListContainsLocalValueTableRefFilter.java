/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.tablereffilter;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * If the target table has a list field, and the referencing table has a value field, filter so that the record's
 * list value contains the local value.
 */
public class RemoteValueListContainsLocalValueTableRefFilter implements TableRefFilter
{
	protected Path remoteListPath;
	protected SchemaNode remoteListNode;
	protected Path valuePath;
	protected SchemaNode valueNode;
	private static final String MESSAGE = "{0} must contain the  {1} value.";

	@SuppressWarnings({ "rawtypes" })
	@Override
	public boolean accept(Adaptation adaptation, ValueContext context)
	{
		List remoteList = adaptation.getList(remoteListPath);
		Object value = context.getValue(valuePath);
		return remoteList != null && remoteList.contains(value);
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		SchemaNode node = context.getSchemaNode();
		if (valuePath == null)
		{
			context.addError("valuePath parameter must be specified.");
		}
		else
		{
			valueNode = node.getNode(valuePath);
			if (valueNode == null)
			{
				context.addError("valuePath " + valuePath.format() + " does not exist.");
			}
		}
		SchemaNode remoteTable = AdaptationUtil.getTableNodeForRelated(node);
		if (remoteListPath == null)
		{
			context.addError("remoteListPath parameter must be specified.");
		}
		else
		{
			remoteListNode = remoteTable.getTableOccurrenceRootNode().getNode(remoteListPath);
			if (remoteListNode == null)
			{
				context.addError("remoteListPath " + remoteListPath.format() + " does not exist.");
			}
			else if (remoteListNode.getMaxOccurs() == 1)
			{
				context.addError("remoteListPath " + remoteListPath.format()
					+ " does not represent a list field.");
			}
		}
		if (remoteListNode != null && valueNode != null)
			context.addFilterErrorMessage(createMessage(Locale.getDefault()));
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return createMessage(locale);
	}

	protected String createMessage(Locale locale)
	{
		return MessageFormat.format(
			MESSAGE,
			remoteListNode.getLabel(locale),
			valueNode.getLabel(locale));
	}

	public Path getValuePath()
	{
		return valuePath;
	}

	public void setValuePath(Path valuePath)
	{
		this.valuePath = valuePath;
	}

	public Path getRemoteListPath()
	{
		return remoteListPath;
	}

	public void setRemoteListPath(Path remoteListPath)
	{
		this.remoteListPath = remoteListPath;
	}

}
