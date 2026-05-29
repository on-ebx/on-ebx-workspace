/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.uibeaneditor;

import java.util.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * This can be put on a multi-occurring field. It won't do anything different to display it on the form,
 * but in a table view, it will display it as a single line comma-separated string.
 * This is similar to {@link com.orchestranetworks.ps.valuefunction.ListAsStringValueFunction} but instead of having
 * a separate field that is a value function, it's put on the list field itself, so it's only useful when you only want
 * to display it like that in a table.
 * separator -	A string that will be used as the separator between the instances of the collection.
 * 				To use a space as a prefix or suffix in the separator use the HTML escape value for space - &nbsp;
 * prefix -		The result string will be prefixed with the value of the prefix. Same rule for space applies.
 * maxValues -  The maximum number of values to show. If not specified (or zero), will assume show all values. This can be used
 *              to truncate the list to a certain number of values if it's possible it could contain a large number.
 */
@Deprecated
public class ListAsStringInTableUIBeanEditor extends UIBeanEditor
{
	private static final String SPACE_TOKEN = "&nbsp;";

	private String separator = ", ";
	private String prefix;
	private int maxValues;

	@Override
	public void addForDisplayInTable(UIResponseContext context)
	{
		SchemaNode node = context.getNode();
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) context.getValue();
		if (!list.isEmpty())
		{
			StringBuilder bldr = new StringBuilder();
			if (!StringUtils.isEmpty(prefix))
			{
				bldr.append(prefix.replace(SPACE_TOKEN, " "));
			}
			String sep = separator == null ? null : separator.replace(SPACE_TOKEN, " ");
			ValueContext vc = context.getValueContext();
			Locale locale = context.getLocale();
			int end = (maxValues == 0 || list.size() < maxValues) ? list.size() : maxValues;
			for (int i = 0; i < end; i++)
			{
				Object value = list.get(i);
				String str = node.displayOccurrence(value, true, vc, locale);
				bldr.append(str);
				if (i < end - 1)
				{
					bldr.append(sep);
				}
			}
			if (maxValues != 0 && end == maxValues)
			{
				bldr.append(sep);
				bldr.append("...");
			}
			context.add(bldr.toString());
		}
	}

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		UIWidget widget = context.newBestMatching(Path.SELF);
		widget.setEditorDisabled(true);
		context.addWidget(widget);
	}

	@Override
	public void addForEdit(UIResponseContext context)
	{
		UIWidget widget = context.newBestMatching(Path.SELF);
		context.addWidget(widget);
	}

	public String getSeparator()
	{
		return this.separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	public String getPrefix()
	{
		return this.prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public int getMaxValues()
	{
		return this.maxValues;
	}

	public void setMaxValues(int maxValues)
	{
		this.maxValues = maxValues;
	}
}
