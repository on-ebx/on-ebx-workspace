/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.widget;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * This is an editor so that list-fields can be displayed in a table.
 */
public class ListUICustomWidget extends BaseUIListCustomWidget
{
	public ListUICustomWidget(WidgetFactoryContext aContext, ListUIWidgetFactory factory)
	{
		super(aContext, factory);
	}

	private void addForDisplayInTable(WidgetWriterForList aWriter, WidgetDisplayContext aContext)
	{
		SchemaNode node = aContext.getNode();
		Path path = node.getPathInAdaptation();
		if (path.isIndexed())
		{
			addForDisplayOrReadOnly(aWriter, aContext);
			return;
		}
		ValueContext vc = aContext.getValueContext();
		List<?> values = (List<?>) vc.getValue();
		StringBuilder sb = new StringBuilder();
		Locale locale = aWriter.getLocale();
		String separator = ((ListUIWidgetFactory) factory).getSeparator();

		boolean first = true;
		for (Object value : values)
		{
			value = node.displayOccurrence(value, true, vc, locale);
			if (!first)
				sb.append(separator);
			else
				first = false;
			sb.append(value);
		}
		aWriter.add(sb.toString());
	}

	@Override
	public void write(WidgetWriterForList aWriter, WidgetDisplayContext aContext)
	{
		if (aContext.isDisplayedInTable())
		{
			addForDisplayInTable(aWriter, aContext);
		}
		else
		{
			super.write(aWriter, aContext);
		}
	}

}
