/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * This is an editor so that list-fields can be displayed in a table as a comma-separated string
 * (or some separator other than comma).
 * When using some other seperator, specify "&nbsp." wherever a blank space is needed
 */
public class ListUIWidgetFactory extends BaseUICustomWidgetFactory<ListUICustomWidget>
{
	private static final String DEFAULT_LIST_VALUES_SEPARATOR = ", ";
	private static final String NON_BREAKING_SPACE = "&nbsp;";

	private String separator = DEFAULT_LIST_VALUES_SEPARATOR;

	@Override
	public ListUICustomWidget newInstance(WidgetFactoryContext aContext)
	{
		return new ListUICustomWidget(aContext, this);
	}

	@Override
	public void setup(WidgetFactorySetupContext aContext)
	{
		if (separator == null)
		{
			aContext.addError("separator parameter must be specified.");
		}
		if (separator.contains(NON_BREAKING_SPACE))
		{
			separator = separator.replace(NON_BREAKING_SPACE, " ");
		}
		if (aContext.getSchemaNode().getMaxOccurs() == 1)
		{
			aContext.addError("This widget must be used with a multi-occurring field.");
		}
		super.setup(aContext);
	}

	/**
	 * See {@link #setSeparator()}
	 */
	public String getSeparator()
	{
		return separator;
	}

	/**
	 * Set the separator for the list of values
	 * 
	 * @param separator the separator
	 */
	public void setSeparator(String separator)
	{
		this.separator = separator;
	}
}
