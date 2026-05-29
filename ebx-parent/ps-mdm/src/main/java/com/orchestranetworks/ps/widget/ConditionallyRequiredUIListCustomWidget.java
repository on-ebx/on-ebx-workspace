package com.orchestranetworks.ps.widget;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * A widget for a multi-occurring field that displays an indicator that it's a required field.
 * Whether it's required can be dynamic. This class is essentially the same as {@link ConditionallyRequiredUISimpleCustomWidget},
 * except for multi-occurring fields. The EBX API doesn't have a common parent class between them, so it requires two
 * implementations.
 */
public class ConditionallyRequiredUIListCustomWidget extends UIListCustomWidget
{
	protected ConditionallyRequiredUIWidgetFactoryImpl impl;
	protected UIListCustomWidget wrappedWidget;

	/**
	 * Create the widget by wrapping the standard out-of-the-box widget. This is equivalent to calling
	 * {@link #ConditionallyRequiredUIListCustomWidget(WidgetFactoryContext, ConditionallyRequiredUIWidgetFactoryImpl, UIListCustomWidget))}
	 * with <code>null</code> for the last argument.
	 *
	 * @param context the context
	 * @param impl an implementation class that contains the behavior
	 */
	public ConditionallyRequiredUIListCustomWidget(
		WidgetFactoryContext context,
		ConditionallyRequiredUIWidgetFactoryImpl impl)
	{
		this(context, impl, createDefaultWrappedWidget(context));
	}

	/**
	 * Create the widget
	 *
	 * @param context the context
	 * @param impl an implementation class that contains the behavior
	 * @param wrappedWidget the widget to add to the screen, along with the custom indicator
	 */
	public ConditionallyRequiredUIListCustomWidget(
		WidgetFactoryContext context,
		ConditionallyRequiredUIWidgetFactoryImpl impl,
		UIListCustomWidget wrappedWidget)
	{
		super(context);
		this.impl = impl;
		this.wrappedWidget = wrappedWidget;
	}

	@Override
	public void write(WidgetWriterForList writer, WidgetDisplayContext context)
	{
		// Write the custom indicator, using an implementation class so that it can be more easily customized
		impl.handleRequired(writer, context);
		// Write the normal widget
		wrappedWidget.write(writer, context);
	}

	// Create a standard out-of-the-box widget for use when no custom wrapped widget was specified.
	// This simply creates a widget that adds the default for the node.
	private static UIListCustomWidget createDefaultWrappedWidget(WidgetFactoryContext context)
	{
		return new UIListCustomWidget(context)
		{
			@Override
			public void write(WidgetWriterForList writer, WidgetDisplayContext displayContext)
			{
				writer.addWidget(Path.SELF);
			}
		};
	}
}