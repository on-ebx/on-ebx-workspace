package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Configured with sibling field paths and a sibling field values and compare
 * operators, this constraint on null will check, when the sibling fields
 * compare to the specified values, that this field (on which the constraint is
 * declared) requires a value. If the mere existence of a sibling value makes this
 * field required, set the otherFieldValue to "&lt;not-null&gt;".
 */
public class FieldConditionallyRequiredMultiConstraint<T> implements Constraint<T>, ConstraintOnNull
{
	private static String NOTNULL = "<not-null>";

	private static enum Op {
		Equals("is equal to"), NotEquals("is not equal to"), StartsWith("starts with",
			true), EndsWith("ends with", true), Contains("contains", true), GreaterThan(
				"is greater than"), GreaterThanEq("is greater than or equal to"), LessThan(
					"is less than"), LessThanEq("is less than or equal to");
		private final String text;
		private final boolean stringoper;

		private Op(String text)
		{
			this(text, false);
		}

		private Op(String text, boolean stringoper)
		{
			this.text = text;
			this.stringoper = stringoper;
		}

	};

	private String pathsAsString;
	private String valuesAsString;
	private String opsAsString;
	private String message;

	private SchemaNode[] otherFieldNodes;
	@SuppressWarnings("rawtypes")
	private Comparable[] otherFieldValues;
	private Op[] operators;

	@SuppressWarnings("rawtypes")
	@Override
	public void checkNull(ValueContextForValidation context) throws InvalidSchemaException
	{
		// 'and' together the component conditions so we can fast break if a
		// non-match
		for (int i = 0; i < otherFieldNodes.length; i++)
		{
			Object value = context.getValue(otherFieldNodes[i]);
			Comparable otherFieldValue = otherFieldValues[i];
			Op op = operators[i];
			if (!matches(value, otherFieldValue, op))
				return;
		}
		context.addError(message);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static boolean matches(Object value, Comparable otherFieldValue, Op op)
	{
		if (value == null)
			return false;
		if (NOTNULL.equals(otherFieldValue) && Op.Equals.equals(op) && value != null)
			return true;
		if (op.stringoper)
		{
			String stringValue = String.valueOf(value);
			switch (op)
			{
			case StartsWith:
				return stringValue.startsWith((String) otherFieldValue);
			case EndsWith:
				return stringValue.endsWith((String) otherFieldValue);
			case Contains:
				return stringValue.contains((String) otherFieldValue);
			default:
				break;
			}
		}
		else
		{
			switch (op)
			{
			case Equals:
				return otherFieldValue.equals(value);
			case NotEquals:
				return !otherFieldValue.equals(value);
			case GreaterThan:
				return otherFieldValue.compareTo(value) < 0;
			case GreaterThanEq:
				return otherFieldValue.compareTo(value) <= 0;
			case LessThan:
				return otherFieldValue.compareTo(value) > 0;
			case LessThanEq:
				return otherFieldValue.compareTo(value) >= 0;
			default:
				break;
			}
		}
		return false;
	}

	@Override
	public void checkOccurrence(T value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		// nothing to do
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setup(ConstraintContext context)
	{
		if (pathsAsString == null)
			context.addError(
				"Conditionally required field constraint requires paths to other fields in the record");
		List<Path> paths = PathUtils.convertStringToPathList(pathsAsString, null);
		int size = paths.size();
		otherFieldNodes = new SchemaNode[size];
		operators = new Op[size];
		otherFieldValues = new Comparable[size];
		String[] otherFieldStrings = valuesAsString.split(";");
		if (size != otherFieldStrings.length)
		{
			context.addError("Should have exactly one value for each path specified");
			return;
		}
		String[] opStrings = null;
		if (opsAsString != null)
		{
			opStrings = opsAsString.split(";");
			if (size != opStrings.length)
			{
				context.addError("Should have exactly one operator for each path specified");
				return;
			}
		}
		SchemaNode rootNode = context.getSchemaNode().getTableNode();
		for (int i = 0; i < size; i++)
		{
			Path path = paths.get(i);
			SchemaNode node = PathUtils
				.setupFieldNode(context, rootNode, path, "otherField", true, true);
			if (node == null)
				return;
			otherFieldNodes[i] = node;
			String otherFieldString = otherFieldStrings[i];
			if (NOTNULL.equals(otherFieldString))
				otherFieldValues[i] = otherFieldString;
			else
				otherFieldValues[i] = (Comparable) node.parseXsString(otherFieldString);
			if (opStrings == null)
				operators[i] = Op.Equals;
			else
				operators[i] = Op.valueOf(opStrings[i]);
		}
		Locale locale = Locale.getDefault();
		String prefix = context.getSchemaNode().getLabel(locale) + " is required ";
		if (message == null)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(prefix);
			sb.append(" when ");
			for (int i = 0; i < otherFieldNodes.length; i++)
			{
				SchemaNode schemaNode = otherFieldNodes[i];
				if (i > 0)
					sb.append(" and ");
				sb.append(schemaNode.getLabel(locale));
				String otherValue = otherFieldStrings[i];
				if (NOTNULL.equals(otherValue))
					sb.append(" is not null");
				else
					sb.append(" ").append(operators[i].text).append(" ").append(otherValue);
			}
			message = sb.toString();
		}
		else if (!message.startsWith(prefix))
			message = prefix + message;
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return message;
	}

	public String getPathsAsString()
	{
		return pathsAsString;
	}

	public void setPathsAsString(String pathsAsString)
	{
		this.pathsAsString = pathsAsString;
	}

	public String getValuesAsString()
	{
		return valuesAsString;
	}

	public void setValuesAsString(String valuesAsString)
	{
		this.valuesAsString = valuesAsString;
	}

	public String getOpsAsString()
	{
		return opsAsString;
	}

	public void setOpsAsString(String opsAsString)
	{
		this.opsAsString = opsAsString;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

}
