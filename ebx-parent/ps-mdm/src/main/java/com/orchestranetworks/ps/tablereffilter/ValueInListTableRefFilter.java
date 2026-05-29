/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * If there is a local fk list-field and you want to restrict another fk field to be in that list.
 * For example, maybe you have a collection of people and you want to designate one as the head
 * of the family.
 */
public class ValueInListTableRefFilter implements TableRefFilter
{
	protected Path listPath;
	protected SchemaNode listNode;

	@SuppressWarnings("unchecked")
	@Override
	public boolean accept(Adaptation adaptation, ValueContext context)
	{
		String recordPK = adaptation.getOccurrencePrimaryKey().format();
		List<String> list = (List<String>) context.getValue(listPath);
		if (list == null || list.isEmpty())
		{
			return false;
		}
		return list.contains(recordPK);
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		if (listPath == null)
		{
			context.addError("listPath parameter must be specified.");
		}
		else
		{
			listNode = context.getSchemaNode().getNode(listPath);
			if (listNode == null)
			{
				context.addError("listPath " + listPath.format() + " does not exist.");
			}
			else if (listNode.getMaxOccurs() == 1)
			{
				context.addError("listPath " + listPath.format()
					+ " must be a multi-occurring field.");
			}
			else
			{
				// Only handles default locale
				context.addFilterErrorMessage(createMessage(Locale.getDefault()));
			}
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return createMessage(locale);
	}

	protected String createMessage(Locale locale)
	{
		return "Value must be in list specified by " + listNode.getLabel(locale) + ".";
	}

	public String getListPath()
	{
		return this.listPath.format();
	}

	public void setListPath(String listPath)
	{
		this.listPath = Path.parse(listPath);
	}
}
