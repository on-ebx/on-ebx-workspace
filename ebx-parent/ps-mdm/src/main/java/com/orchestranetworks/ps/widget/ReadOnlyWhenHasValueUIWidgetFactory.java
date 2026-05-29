package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * Creates a widget that is read only when the field has a value only, and is normally considered read only by the parent class's
 * functionality, i.e. takes into account <code>editorRoles</code>, etc.
 * 
 * This can be useful when you have a field that is sometimes given a default upon initial creation and you don't want
 * the user to change it, but when there is no default, you want them to be able to enter a value.
 * 
 * You may also want them to be able to change the value when they're duplicating a record because in that case, it's not
 * really the default, it just has a value due to having duplicated a record. So there is a parameter on the factory called
 * <code>readWriteWhileDuplicating<code> that can be used for that.
 * 
 * In the context of this class, <code>neverEditable</code> of <code>true</code> can be thought of as
 * "never editable when it has a value, otherwise editable" and <code>false</code> can be thought of as
 * "never editable when it has a value except for these roles, otherwise editable".
 * <code>readOnly</code> of <code>false</code> isn't valid since there's no point in using this widget if it's always editable.
 */
public class ReadOnlyWhenHasValueUIWidgetFactory<T extends ReadOnlyWhenHasValueUISimpleCustomWidget>
	extends
	BaseUICustomWidgetFactory<T>
{
	private boolean readWriteWhenDuplicating;

	public ReadOnlyWhenHasValueUIWidgetFactory()
	{
		setReadOnly(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(WidgetFactoryContext context)
	{
		return (T) new ReadOnlyWhenHasValueUISimpleCustomWidget(context, this);
	}

	@Override
	public void setup(WidgetFactorySetupContext context)
	{
		// These parameters exist in the parent class so we can't prevent someone from specifying values for them,
		// but they don't make sense for this widget factory. (If it's always editable then you should just be using
		// BaseUICustomUIWidgetFactory, and you should not be specifying to never validate when this is read/write sometimes.)
		if (!isReadOnly())
		{
			context.addError("readOnly must be true for this widget.");
		}
		if (isNeverValidate())
		{
			context.addError("neverValidate parameter is not supported.");
		}
		super.setup(context);
	}

	/**
	 * This has most of the special logic for determining if it's really read only when the parent widget says it is.
	 * This is in the factory so that it can be used by both the List and Simple widgets,
	 * which have to extend from two different base classes.
	 * 
	 * @param context the context
	 * @return whether it's read only
	 */
	protected boolean isReadOnlyWhenHasValue(WidgetDisplayContext context)
	{
		// If it doesn't have a value, it's actually going to be read/write
		if (context.getValueContext().getValue() == null)
		{
			return false;
		}
		// Otherwise it has a value so it's only going to be read only if
		// we're not duplicating when we've said it should be read/write while duplicating.
		return !(context.isDuplicatingRecord() && isReadWriteWhenDuplicating());
	}

	public boolean isReadWriteWhenDuplicating()
	{
		return readWriteWhenDuplicating;
	}

	public void setReadWriteWhenDuplicating(boolean readWriteWhenDuplicating)
	{
		this.readWriteWhenDuplicating = readWriteWhenDuplicating;
	}
}
