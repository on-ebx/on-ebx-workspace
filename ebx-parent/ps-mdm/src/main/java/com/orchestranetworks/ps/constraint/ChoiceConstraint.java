package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * Configured with a set of paths, each representing a field node, this constraint will ensure that:<br/>
 * If required is true, at least one occurrence is required, and<br/>
 * If multipleAllowed is false, that only one has a value
 */
public class ChoiceConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private List<SchemaNode> fields;
	private String pathsAsString;
	private boolean required = true;
	private boolean multipleAllowed = false;

	/**
	 * {@link #setPathsAsString(String)}
	 */
	public String getPathsAsString()
	{
		return pathsAsString;
	}

	/**
	 * Set the paths that are being chosen between, as a semi-colon separated string.
	 * The paths should be specified relative to the root node (i.e. <code>./fieldA</code>).
	 * 
	 * @param pathsAsString the paths
	 */
	public void setPathsAsString(String pathsAsString)
	{
		this.pathsAsString = pathsAsString;
	}

	/**
	 * {@link #setRequired(boolean)}
	 */
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * Set whether a choice is required
	 * 
	 * @param required whether a choice is required
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/**
	 * {@link #setMultipleAllowed(boolean)}
	 */
	public boolean isMultipleAllowed()
	{
		return multipleAllowed;
	}

	/**
	 * Set whether multiple choices are allowed
	 * 
	 * @param multipleAllowed whether multiple choices are allowed
	 */
	public void setMultipleAllowed(boolean multipleAllowed)
	{
		this.multipleAllowed = multipleAllowed;
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		if (fields == null && pathsAsString != null)
		{
			List<Path> pathList = PathUtils.convertStringToPathList(pathsAsString, null);
			fields = new ArrayList<>();
			for (Path path : pathList)
			{
				SchemaNode fieldNode = PathUtils.setupFieldNode(context, path, null, true);
				if (fieldNode != null)
					fields.add(fieldNode);
			}
		}
		if (fields == null || fields.size() < 2)
		{
			context.addError(
				"Choice constraint requires specifying at least two fields for the choice");
		}
		// This won't cause issues, but it's pretty pointless to use this constraint if nothing is
		// required and you can have more than one. So we'll just give a warning.
		if (!required && multipleAllowed)
		{
			context.addWarning(
				"Constraint has no effect when required is false and allowMultiple is true.");
		}
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return createMessage(locale, context, null);
	}

	private String createMessage(
		Locale locale,
		ValueContext context,
		List<SchemaNode> fieldsWithValues)
	{
		StringBuilder sb = new StringBuilder();
		String verb = " can";
		if (required)
		{
			verb = " must";
			if (multipleAllowed)
			{
				sb.append("At least one of ");
			}
			else
			{
				sb.append("Exactly one of ");
			}
		}
		else
		{
			// We won't worry about case where multipleAllowed = true because
			// it's irrelevant - there will never be an error message in that case
			sb.append("At most one of ");
		}
		UnaryFunction<SchemaNode, String> toString = AdaptationUtil.GetNodeLabel.bindRight(locale);
		sb.append(CollectionUtils.joinStrings(fields, toString, ", "));
		sb.append(verb + " have a value");
		if (fieldsWithValues != null)
		{
			if (fieldsWithValues.isEmpty())
			{
				sb.append(" (none has a value)");
			}
			else
			{
				sb.append(" (the following have a value: ");
				sb.append(CollectionUtils.joinStrings(fieldsWithValues, toString, ", "));
				sb.append(")");
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		List<SchemaNode> fieldsWithValues = new ArrayList<>();
		// Collect which fields have a value
		for (SchemaNode schemaNode : fields)
		{
			Object value = recordContext.getValue(schemaNode);
			// If it's a list field and it's an empty list, then it doesn't have a value
			// so keep looping
			if (value instanceof Collection && ((Collection) value).isEmpty())
			{
				continue;
			}
			// It has a value if it's not null
			if (value != null)
			{
				fieldsWithValues.add(schemaNode);
			}
		}
		int size = fieldsWithValues.size();
		// There's an error if a field is required and there are none with values,
		// or if no multiples are allowed and there is more than one with a value
		if ((required && size == 0) || (!multipleAllowed && size > 1))
		{
			return createMessage(Locale.getDefault(), recordContext, fieldsWithValues);
		}

		return null;
	}

}
