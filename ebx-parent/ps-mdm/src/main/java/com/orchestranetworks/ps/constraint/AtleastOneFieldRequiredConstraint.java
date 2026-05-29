package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * Configured with a set of paths, each representing a field node, 
 * this constraint will ensure that at least one of the configured fields have a value.
 */
public class AtleastOneFieldRequiredConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private List<SchemaNode> fields;
	private String pathsAsString;

	public String getPathsAsString()
	{
		return pathsAsString;
	}

	public void setPathsAsString(String pathsAsString)
	{
		this.pathsAsString = pathsAsString;
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
				SchemaNode fieldNode = PathUtils.setupFieldNode(context, path, null, false);
				if (fieldNode != null)
				{
					fields.add(fieldNode);
					context.addDependencyToInsertDeleteAndModify(fieldNode);
				}
			}
		}
		if (fields == null || fields.size() < 1)
			context.addError("At least one field must be specified for this constraint.");
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return createMessage(locale, context);
	}

	private String createMessage(Locale locale, ValueContext context)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("At least one of ");
		UnaryFunction<SchemaNode, String> toString = AdaptationUtil.GetNodeLabel.bindRight(locale);
		sb.append(CollectionUtils.joinStrings(fields, toString, ", "));
		sb.append(" must have a value.");
		return sb.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		List<SchemaNode> fieldsWithValues = new ArrayList<>();
		for (SchemaNode schemaNode : fields)
		{
			Object value = recordContext.getValue(schemaNode);
			if (value instanceof Collection && ((Collection) value).isEmpty())
			{
				continue;
			}
			if (value != null)
			{
				fieldsWithValues.add(schemaNode);
			}
		}
		int size = fieldsWithValues.size();
		if (size == 0)
		{
			return createMessage(Locale.getDefault(), recordContext);
		}

		return null;
	}

}
