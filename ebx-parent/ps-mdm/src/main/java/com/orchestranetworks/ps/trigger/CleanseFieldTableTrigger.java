package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * Field lvl trigger that does string cleansing.
 * It replace Microsoft character, white spaces with standard space, removes control character and trim the field value.  
 * 
 *
 */
public class CleanseFieldTableTrigger extends TableTrigger
{

	private Path fieldPath;

	@Override
	public void setup(TriggerSetupContext context)
	{
		fieldPath = context.getSchemaNode().getPathInAdaptation();
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException
	{
		super.handleBeforeCreate(aContext);
		ValueContextForUpdate valueContext = aContext.getOccurrenceContextForUpdate();
		if (toPerformForNewRecoeds())
		{
			cleanseField(valueContext);
		}
	}

	protected boolean toPerformForNewRecoeds()
	{
		return true;
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException
	{
		super.handleBeforeModify(aContext);
		ValueContextForUpdate valueContext = aContext.getOccurrenceContextForUpdate();
		if (toPerformForUpdate())
		{
			ValueChanges valueChanges = aContext.getChanges();
			ValueChange valueChange = valueChanges.getChange(fieldPath);
			if (valueChange != null)
			{
				cleanseField(valueContext);
			}
		}
	}

	/**
	 * This methos controls the execution of the 
	 * @return true 
	 */
	protected boolean toPerformForUpdate()
	{
		return true;
	}

	protected void cleanseField(ValueContextForUpdate valueContext)
	{
		String value = (String) valueContext.getValue(fieldPath);
		if (value != null)
		{
			value = performCleanse(value);
			valueContext.setValue(value, fieldPath);
		}
	}

	/**
	 * value of the field to perform action on. the value will never be null. 
	 * @param value
	 * @return the result of the action to perform. e.g. trimming the value, or cleansing the string from undesired characters
	 */
	protected String performCleanse(String value)
	{
		return CleanseStringUtility.CleanseString(value);
	}

	public Path getFieldPath()
	{
		return fieldPath;
	}

	public void setFieldPath(Path fieldPath)
	{
		this.fieldPath = fieldPath;
	}

}
