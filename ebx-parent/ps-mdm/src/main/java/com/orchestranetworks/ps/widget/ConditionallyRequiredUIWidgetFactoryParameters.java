package com.orchestranetworks.ps.widget;

/**
 * This is an interface that defines the parameters used by the {@link ConditionallyRequiredUIWidgetFactory}.
 * These are the parameters that will be available to be configured in the data model. It's defined as an
 * interface so that other factories can be used with the widgets without needing to use that specific factory.
 * They just need to support these parameters.
 */
public interface ConditionallyRequiredUIWidgetFactoryParameters
{
	/**
	 * Returns whether the field is always required inside the workflow.
	 * One common way of using this would be to say the field is required in a workflow, but otherwise not.
	 * If that functionality is not desired, this should be set to <code>false</code>, which is also the default.
	 *
	 * @return whether it's required inside the workflow
	 */
	public boolean isRequiredInsideWorkflow();

	/**
	 * @see {@link #isRequiredInsideWorkflow()}
	 */
	public void setRequiredInsideWorkflow(boolean requiredInsideWorkflow);

	/**
	 * Returns whether the field only shows the required indicator when it has no value. Otherwise it will show it
	 * based on whatever other logic there is.
	 * If that functionality is not desired, this should be set to <code>false</code>, which is also the default.
	 *
	 * @return whether it's only shown when there is no value
	 */
	public boolean isRequiredOnlyWhenNoValue();

	/**
	 * @see {@link #isRequiredOnlyWhenNoValue()}
	 */
	public void setRequiredOnlyWhenNoValue(boolean requiredOnlyWhenNoValue);

	/**
	 * Returns whether to display the indicator when viewed in a table cell.
	 * If <code>false</code>, it will only display in the form.
	 *
	 * @return whether to display in a table
	 */
	public boolean isDisplayedInTable();

	/**
	 * @see {@link #isDisplayedInTable()}
	 * @param displayedInTable
	 */
	public void setDisplayedInTable(boolean displayedInTable);
}
