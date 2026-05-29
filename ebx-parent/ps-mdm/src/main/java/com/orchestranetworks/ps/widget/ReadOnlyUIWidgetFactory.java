package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * This provides the same functionality as its parent class, {@link BaseUICustomWidgetFactory},
 * except that it turns on read only mode by default.
 */
public class ReadOnlyUIWidgetFactory<T extends UICustomWidget> extends BaseUICustomWidgetFactory<T>
{
	public ReadOnlyUIWidgetFactory()
	{
		setReadOnly(true);
	}

	@Override
	public void setup(WidgetFactorySetupContext context)
	{
		// This parameter exists in the parent class so we can't prevent someone from specifying a value for it,
		// but it doesn't make sense for this widget factory. (If it's always editable then you should just be using
		// BaseUICustomUIWidgetFactory.)
		if (!isReadOnly())
		{
			context.addError("readOnly must be true for this widget.");
		}
		super.setup(context);
	}
}
