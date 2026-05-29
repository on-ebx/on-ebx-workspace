package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * Field level table trigger that simulates EBX Field Inheritance.
 * -- This field trigger will simulate EBX Field Inheritance and should be used in places where field inheritance is not supported (i.e MAME, and record labels)
 * -- It should be attached to the target field in the data model, specifying the source field path as a parameter (i.e. <./sourceFieldName>)
 * 
 */
public class SimulatedFieldInheritanceTableTrigger extends TableTrigger
{

	private Path targetFieldPath;
	private Path sourceFieldPath;

	@Override
	public void setup(TriggerSetupContext context)
	{
		targetFieldPath = context.getSchemaNode().getPathInAdaptation();

		if (sourceFieldPath == null)
		{
			context.addError("sourceFieldPath must be specified.");
		}
		else if (context.getSchemaNode().getNode(Path.PARENT.add(sourceFieldPath)) == null)
		{
			context.addError("sourceFieldPath " + sourceFieldPath.format() + " does not exist.");
		}
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
		simulateFieldInheritance(sourceFieldPath, targetFieldPath, valueContext, null);
		super.handleBeforeCreate(context);
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
		ValueChanges valueChanges = context.getChanges();
		simulateFieldInheritance(sourceFieldPath, targetFieldPath, valueContext, valueChanges);

		super.handleBeforeModify(context);
	}

	@SuppressWarnings("rawtypes")
	static public void simulateFieldInheritance(
		Path sourceFieldPath,
		Path targetFieldPath,
		ValueContextForUpdate valueContext,
		ValueChanges valueChanges)
	{
		Object targetFieldValue = valueContext.getValue(targetFieldPath);
		Object sourceFieldValue = valueContext.getValue(sourceFieldPath);

		// for Creates (valueChanges will be null), set the target field if it is left null (or empty for Collection attributes)
		if (valueChanges == null)
		{
			if (targetFieldValue == null || (targetFieldValue instanceof Collection
				&& ((Collection) targetFieldValue).isEmpty()))
			{
				valueContext.setValueEnablingPrivilegeForNode(sourceFieldValue, targetFieldPath);
			}
		}
		else
		{
			// For Updates. if source field has changed and the original value of the source field is equal to the original value of the target field, and the target field has not changed,
			//  then set the target field value to the new source field value
			ValueChange sourceValueChange = valueChanges.getChange(sourceFieldPath);
			ValueChange targetValueChange = valueChanges.getChange(targetFieldPath);
			if (sourceValueChange != null && targetValueChange == null
				&& Objects.equals(sourceValueChange.getValueBefore(), targetFieldValue))
			{
				valueContext.setValueEnablingPrivilegeForNode(sourceFieldValue, targetFieldPath);
			}

		}

	}

	public Path getSourceFieldPath()
	{
		return sourceFieldPath;
	}

	public void setSourceFieldPath(Path sourceFieldPath)
	{
		this.sourceFieldPath = sourceFieldPath;
	}

}
