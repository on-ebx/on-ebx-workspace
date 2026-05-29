package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * @deprecated This is no longer used by {@link com.orchestranetworks.ps.widget.ReadOnlyUIWidgetFactory}.
 *             It uses a {@link BaseUISimpleCustomWidget} instead.
 */
@Deprecated
public class ReadOnlyUICustomWidget extends BaseUISimpleCustomWidget
{
	@SuppressWarnings("rawtypes")
	public ReadOnlyUICustomWidget(WidgetFactoryContext aContext, ReadOnlyUIWidgetFactory factory)
	{
		super(aContext, factory);
	}
}
