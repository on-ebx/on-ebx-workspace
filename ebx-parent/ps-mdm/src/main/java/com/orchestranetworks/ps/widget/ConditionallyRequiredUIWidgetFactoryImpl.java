package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * An implementation class for the {@link ConditionallyRequiredUIWidgetFactory} that contains the core logic.
 * It is separated into a separate class so that it can be overridden more easily by subclasses and combined
 * with other factories without requiring specifically that factory.
 */
public class ConditionallyRequiredUIWidgetFactoryImpl
{
	// Creates a bold asterisk and a space after it by default
	private static final String REQUIRED_INDICATOR = "<b>*</b>&nbsp;";

	protected ConditionallyRequiredUIWidgetFactoryParameters params;

	/**
	 * Create the implementation with the parameters that can be configured through the model.
	 * The factory is one implementation of the parameters, but you could also have a different factory
	 * implement the parameters instead.
	 *
	 * @param params the parameters
	 */
	public ConditionallyRequiredUIWidgetFactoryImpl(
		ConditionallyRequiredUIWidgetFactoryParameters params)
	{
		this.params = params;
	}

	/**
	 * Handle the required indicator
	 *
	 * @param writer the writer
	 * @param context the context
	 */
	public void handleRequired(WidgetWriter writer, WidgetDisplayContext context)
	{
		if (isIndicatorRequired(writer, context))
		{
			writeRequiredIndicator(writer);
		}
	}

	/**
	 * Determine if the indicator is required to be written
	 *
	 * @param writer the writer
	 * @param context the context
	 * @return whether it should be written
	 */
	protected boolean isIndicatorRequired(WidgetWriter writer, WidgetDisplayContext context)
	{
		// If this is viewing a table and we're not ever displaying it in a table, then return false
		if (context.isDisplayedInTable() && !params.isDisplayedInTable())
		{
			return false;
		}

		boolean required = false;

		// If it should only be written when there's no value, then see if there's a value and if not,
		// return false, otherwise assume it should be written and continue the logic.
		if (params.isRequiredOnlyWhenNoValue())
		{
			if (context.getValueContext().getValue() != null)
			{
				return false;
			}
			required = true;
		}

		// If it had a value when it should only be shown when it's empty, then it would have returned
		// already above. So here either it's not required only when there's no value, or it is and there was a value.
		// Check if it's required when inside a workflow, and if it is, return true if we're in a workflow, or return
		// whatever the previously determined value is if we're not.
		return params.isRequiredInsideWorkflow() ? writer.getSession().isInWorkflowInteraction(true)
			: required;
	}

	/**
	 * Write the indicator
	 *
	 * @param writer the writer
	 */
	protected void writeRequiredIndicator(WidgetWriter writer)
	{
		writer.add(REQUIRED_INDICATOR);
	}
}
