package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * Creates a widget that shows an indicator as to whether it's required or not, that can by dynamically shown or hidden.
 * The core logic is in a separate implementation class that gets invoked, so that it can be more easily overridden.
 * @see {@link ConditionallyRequiredUIWidgetFactoryImpl}. The parameters for the factory are in a separate interface so that
 * the logic isn't tied to using this specific factory, but you could implement a different factory that has the same parameters.
 * @see {@link ConditionallyRequiredUIWidgetFactoryParameters}.
 */
public class ConditionallyRequiredUIWidgetFactory<T extends UICustomWidget>
	implements UIWidgetFactory<T>, ConditionallyRequiredUIWidgetFactoryParameters
{
	protected boolean requiredInsideWorkflow;
	protected boolean requiredOnlyWhenNoValue;
	private boolean displayedInTable;

	private boolean listWidget;

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(WidgetFactoryContext context)
	{
		// Create the implementation object
		ConditionallyRequiredUIWidgetFactoryImpl impl = createImpl();
		// Create either the list or simple widget
		if (listWidget)
		{
			return (T) new ConditionallyRequiredUIListCustomWidget(context, impl);
		}
		return (T) new ConditionallyRequiredUISimpleCustomWidget(context, impl);
	}

	@Override
	public void setup(WidgetFactorySetupContext context)
	{
		// It's a list widget if the node is max occurs of more than 1
		listWidget = (context.getSchemaNode().getMaxOccurs() > 1);
	}

	/**
	 * Create the implementation object that will contain the core logic
	 *
	 * @return the implementation object
	 */
	protected ConditionallyRequiredUIWidgetFactoryImpl createImpl()
	{
		// The implementation object takes in a paramaters object,
		// which this factory implements, so we just pass in this
		return new ConditionallyRequiredUIWidgetFactoryImpl(this);
	}

	/**
	 * @see {@link #ConditionallyRequiredUIWidgetFactoryParameters#isRequiredInsideWorkflow()}
	 */
	@Override
	public boolean isRequiredInsideWorkflow()
	{
		return this.requiredInsideWorkflow;
	}

	/**
	 * @see {@link #isRequiredInsideWorkflow()}
	 */
	@Override
	public void setRequiredInsideWorkflow(boolean requiredInsideWorkflow)
	{
		this.requiredInsideWorkflow = requiredInsideWorkflow;
	}

	/**
	 * @see {@link ConditionallyRequiredUIWidgetFactoryParameters#isRequiredOnlyWhenNoValue()}
	 */
	@Override
	public boolean isRequiredOnlyWhenNoValue()
	{
		return this.requiredOnlyWhenNoValue;
	}

	/**
	 * @see {@link #isRequiredOnlyWhenNoValue()}
	 */
	@Override
	public void setRequiredOnlyWhenNoValue(boolean requiredOnlyWhenNoValue)
	{
		this.requiredOnlyWhenNoValue = requiredOnlyWhenNoValue;
	}

	/**
	 * @see {@link ConditionallyRequiredUIWidgetFactoryParameters#isDisplayedInTable()}
	 */
	@Override
	public boolean isDisplayedInTable()
	{
		return this.displayedInTable;
	}

	/**
	 * @see {@link #isDisplayedInTable()}
	 */
	@Override
	public void setDisplayedInTable(boolean displayedInTable)
	{
		this.displayedInTable = displayedInTable;
	}
}
