package com.orchestranetworks.ps.widget;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * A base list widget that can be put on multi-occurring fields that want to potentially use some of
 * the features of the {@link BaseUICustomWidgetFactory}.
 */
public class BaseUIListCustomWidget extends UIListCustomWidget
{
	protected final BaseUICustomWidgetFactory<?> factory;

	/**
	 * Create the widget
	 * 
	 * @param context the context
	 * @param factory the factory that's creating it
	 */
	public BaseUIListCustomWidget(
		WidgetFactoryContext context,
		BaseUICustomWidgetFactory<?> factory)
	{
		super(context);
		this.factory = factory;
	}

	@Override
	public void validate(WidgetListValidationContext validationContext)
	{
		if (factory.shouldValidateWidget())
		{
			super.validate(validationContext);
		}
	}

	/**
	 * Determine if the editor should be read-only
	 * 
	 * @param writer the writer
	 * @param context the context
	 * @return whether the editor should be read-only
	 */
	protected boolean isReadOnly(WidgetWriter writer, WidgetDisplayContext context)
	{
		if (!factory.isReadOnly())
		{
			return false;
		}
		return factory.isNeverEditable() || !context.getPermission().isReadWrite()
			|| !isUserAlwaysReadWrite(writer.getSession());
	}

	/**
	 * Checks if a user is always read/write, based on the specified
	 * <code>editorRoles</code>
	 *
	 * @param session the session
	 * @return whether the user is always read/write
	 */
	protected boolean isUserAlwaysReadWrite(Session session)
	{
		return factory.isUserInEditorRoles(session);
	}

	/**
	 * Adds the editor when it's for display or when it's read-only.
	 * Adds the default editor in disabled mode unless overridden.
	 */
	//
	// NOTE: This used to just have the first parameter. Was modified June 2023 to pass more information
	//       in for use in subclasses. If subclasses had implemented this already, they will not compile
	//       unless they add this second parameter to the method signature as well.
	//
	protected void addForDisplayOrReadOnly(WidgetWriterForList writer, WidgetDisplayContext context)
	{
		UIWidget widget = writer.newBestMatching(Path.SELF);
		widget.setEditorDisabled(true);
		writer.addWidget(widget);
	}

	/**
	 * Adds the editor when it's editable.
	 * Adds the default editor unless overridden.
	 */
	protected void addForEdit(WidgetWriterForList writer, WidgetDisplayContext context)
	{
		writer.addWidget(Path.SELF);
	}

	@Override
	public void write(WidgetWriterForList writer, WidgetDisplayContext context)
	{
		if (context.isDisplayedInTable() || isReadOnly(writer, context))
		{
			addForDisplayOrReadOnly(writer, context);
		}
		else
		{
			addForEdit(writer, context);
		}
	}
}
