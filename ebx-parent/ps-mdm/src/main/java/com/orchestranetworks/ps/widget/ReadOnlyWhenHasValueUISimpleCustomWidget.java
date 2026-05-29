package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * @see ReadOnlyWhenHasValueUIWidgetFactory
 */
public class ReadOnlyWhenHasValueUISimpleCustomWidget extends BaseUISimpleCustomWidget
{
	public ReadOnlyWhenHasValueUISimpleCustomWidget(
		WidgetFactoryContext context,
		ReadOnlyWhenHasValueUIWidgetFactory<?> factory)
	{
		super(context, factory);
	}

	@Override
	protected boolean isReadOnly(WidgetWriter writer, WidgetDisplayContext context)
	{
		boolean readOnly = super.isReadOnly(writer, context);
		// If the parent implementation says it's read only
		if (readOnly)
		{
			readOnly = ((ReadOnlyWhenHasValueUIWidgetFactory<?>) factory)
				.isReadOnlyWhenHasValue(context);
		}
		return readOnly;
	}
}
