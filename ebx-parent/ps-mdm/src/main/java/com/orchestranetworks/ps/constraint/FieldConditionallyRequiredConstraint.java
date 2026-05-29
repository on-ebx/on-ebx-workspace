package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * Configured with a sibling field path and a sibling field value,
 * this constraint on null will check, when the sibling field has the specified value,
 * that this field (on which the constraint is declared) requires a value.
 * If the existence of the sibling value makes this field required, set the
 * otherFieldValue to "&lt;not-null&gt;".
 * If the non existence of the sibling value makes this field required, set the
 * otherFieldValue to "&lt;null&gt;" or just leave it empty.
 * <p>
 * This can be considered as "not allowed" constraint when <code>mode</code> is set to <code>NOT_ALLOWED</code>.
 * This can be considered as "Required else not allowed" constraint when <code>mode</code> is set to <code>REQUIRED_ELSE_NOT_ALLOWED</code>.
 * <p>
 * If the otherFieldPath specified is a foreign key field and otherFieldForeignFieldPath is specified, then the value
 * will be pulled from that field in the linked record instead.
 * If the otherFieldPath is a Repeating Attribute, then it will check if the List contains the otherFieldValue
 * <p>
 * This constraint can be ignored when the the passOver Path and Value are configured
 * within the UI.
 *
 * 		<severity>F=Fatal, E=Error (default), W=Warning, I=Information</severity>
 * 		<allowFormSubmissionWithError>will allow a record to be saved even if this constraint reports an error</allowFormSubmissionWithError>
*/
public class FieldConditionallyRequiredConstraint<T> implements Constraint<T>, ConstraintOnNull
{
	private String severity = Severity.ERROR.toParsableString();
	private boolean allowFormSubmissionWithError = false;

	private static final String MODE_REQUIRED = "required";
	private static final String MODE_REQUIRED_ELSE_NOT_ALLOWED = "required else not allowed";
	private static final String MODE_NOT_ALLOWED = "not allowed";

	private static String MESSAGE = "When {2} is {3}, {0} is {1} .";
	private static String EBX_MESSAGE = "Field {0} is {1}.";
	private static String IGNORE_CONSTRAINT_MESSAGE = " This constraint will be ignored when {0} contains the value {1}.";
	private static String NOTNULL = "<not-null>";
	private static String NULL = "<null>";

	private Path otherFieldPath;
	private Path otherFieldForeignFieldPath;
	private SchemaNode otherFieldNode;
	private String otherFieldValue;
	private String mode = MODE_REQUIRED;

	// Note: Prefixed these data members with "passOver" so that they would be displayed
	// under the otherField parameters in UI. Would have preferred the prefix "ignore".
	private Path passOverConstraintFieldPath;
	private SchemaNode passOverConstraintFieldNode;
	private String passOverConstratintFieldValue;

	private boolean useEBXmessage;
	private String customMessage;

	private String message;
	private String helpMessage;

	/**
	 * @return the passOverConstraintFieldPath
	 */
	public Path getPassOverConstraintFieldPath()
	{
		return passOverConstraintFieldPath;
	}

	/**
	 * @param passOverConstraintFieldPath the passOverConstraintFieldPath to set
	 */
	public void setPassOverConstraintFieldPath(Path passOverConstraintFieldPath)
	{
		this.passOverConstraintFieldPath = passOverConstraintFieldPath;
	}

	/**
	 * @return the passOverConstratintFieldValue
	 */
	public String getPassOverConstratintFieldValue()
	{
		return passOverConstratintFieldValue;
	}

	/**
	 * @param passOverConstratintFieldValue the passOverConstratintFieldValue to set
	 */
	public void setPassOverConstratintFieldValue(String passOverConstratintFieldValue)
	{
		this.passOverConstratintFieldValue = passOverConstratintFieldValue;
	}

	public Path getOtherFieldPath()
	{
		return otherFieldPath;
	}

	public void setOtherFieldPath(Path otherFieldPath)
	{
		this.otherFieldPath = otherFieldPath;
	}

	public Path getOtherFieldForeignFieldPath()
	{
		return otherFieldForeignFieldPath;
	}

	public void setOtherFieldForeignFieldPath(Path otherFieldForeignFieldPath)
	{
		this.otherFieldForeignFieldPath = otherFieldForeignFieldPath;
	}

	public String getOtherFieldValue()
	{
		return otherFieldValue;
	}

	public void setOtherFieldValue(String otherFieldValue)
	{
		this.otherFieldValue = otherFieldValue;
	}

	public String getMode()
	{
		return mode;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	@Override
	public void checkNull(ValueContextForValidation context) throws InvalidSchemaException
	{
		if (MODE_REQUIRED.equals(mode) || MODE_REQUIRED_ELSE_NOT_ALLOWED.equals(mode))
		{
			checkOtherValue(context, true);
		}
	}

	@Override
	public void checkOccurrence(T value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		if (MODE_NOT_ALLOWED.equals(mode))
		{
			checkOtherValue(context, true);
		}
		else if (MODE_REQUIRED_ELSE_NOT_ALLOWED.equals(mode))
		{
			checkOtherValue(context, false);
		}
	}

	private void checkOtherValue(ValueContextForValidation context, boolean errorIfConditionIsTrue)
	{
		if (passOverConstraintFieldNode != null)
		{
			Object passOverConstraintValue = context.getValue(passOverConstraintFieldNode);
			if (passOverConstratintFieldValue.equals(passOverConstraintValue))
			{
				return; // Pass Over value matches criteria so ignore constraint.
			}
		}

		boolean conditionIsTrue = conditionIsTrue(context);
		if ((conditionIsTrue && errorIfConditionIsTrue)
			|| (!conditionIsTrue && !errorIfConditionIsTrue))
		{
			context.addMessage(
				AdaptationUtil.createUserMessage(message, Severity.parseFlag(severity)));
		}
	}

	@SuppressWarnings("rawtypes")
	protected boolean conditionIsTrue(ValueContextForValidation context)
	{
		Object otherValue = getValueOfOtherFieldNode(context);
		boolean otherValueIsList = otherValue instanceof List;
		if (otherValue == null || (otherValueIsList && ((List) otherValue).isEmpty()))
		{
			if (otherFieldValue == null || NULL.equals(otherFieldValue))
			{
				return true;
			}
		}
		else
		{
			if (NOTNULL.equals(otherFieldValue)
				|| (String.valueOf(otherValue).equals(otherFieldValue))
				|| (otherValueIsList && listContainsValue((List) otherValue, otherFieldValue)))
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	protected boolean listContainsValue(List otherValue, String otherFieldVal)
	{

		for (Object listEntry : otherValue)
		{
			if (otherFieldVal.equals(String.valueOf(listEntry)))
			{
				return true;
			}
		}
		return false;
	}

	// This gets the value of the other field from the given context.
	// When otherFieldForeignFieldPath is specified, it follows the otherFieldPath foreign key and
	// pulls the value from the foreign record.
	// Otherwise, it simply returns the value of the otherFieldPath.
	protected Object getValueOfOtherFieldNode(ValueContext context)
	{
		if (otherFieldForeignFieldPath == null)
		{
			return context.getValue(otherFieldNode);
		}
		Adaptation linkedRecord = otherFieldNode.getFacetOnTableReference()
			.getLinkedRecord(context);
		return linkedRecord == null ? null : linkedRecord.get(otherFieldForeignFieldPath);
	}

	@Override
	public void setup(ConstraintContext context)
	{
		if (otherFieldPath == null)
		{
			context.addError(
				"Conditionally required field constraint requires a path to another field in the record");
		}
		else
		{
			otherFieldNode = otherFieldPath.startsWith(Path.PARENT)
				? context.getSchemaNode().getNode(otherFieldPath)
				: context.getSchemaNode().getTableNode().getTableOccurrenceRootNode().getNode(
					otherFieldPath);
			if (otherFieldNode == null)
			{
				context.addError(otherFieldPath.format() + " not found");
			}
			else
			{
				if (otherFieldForeignFieldPath != null)
				{
					SchemaFacetTableRef tableRef = otherFieldNode.getFacetOnTableReference();
					if (tableRef == null)
					{
						context.addError(
							"otherFieldForeignFieldPath can only be specified when otherFieldPath is a foreign key.");
					}
					else
					{
						SchemaNode otherFieldForeignFieldNode = tableRef.getTableNode()
							.getNode(otherFieldForeignFieldPath);
						if (otherFieldForeignFieldNode == null)
						{
							context.addError(otherFieldForeignFieldPath.format() + " not found");
						}
					}
				}

				String modeText = (mode == MODE_REQUIRED_ELSE_NOT_ALLOWED
					? "required, else not allowed"
					: mode);

				String otherFieldValueText = otherFieldValue;
				if (otherFieldValueText == null || otherFieldValue.equals(NULL))
				{
					otherFieldValueText = "not specified";
				}
				else if (otherFieldValue.equals(NOTNULL))
				{
					otherFieldValueText = "specified";
				}

				helpMessage = MessageFormat.format(
					MESSAGE,
					context.getSchemaNode().getLabel(Locale.getDefault()),
					modeText,
					otherFieldNode.getLabel(Locale.getDefault()),
					otherFieldValueText);
				if (customMessage != null)
				{
					message = customMessage;
				}
				else if (useEBXmessage)
				{
					message = MessageFormat.format(
						EBX_MESSAGE,
						"'" + context.getSchemaNode().getLabel(Locale.getDefault()) + "'",
						modeText);
				}
				else
				{
					message = helpMessage;
				}

				if (null == context.getSchemaNode().getInheritanceProperties())
				{

					context.addDependencyToInsertDeleteAndModify(otherFieldNode);
				}

			}
		}

		if (mode == null)
		{
			context.addError("mode is required");
		}
		else if (!(MODE_REQUIRED.equals(mode) || MODE_NOT_ALLOWED.equals(mode)
			|| MODE_REQUIRED_ELSE_NOT_ALLOWED.equals(mode)))
		{
			context.addError(
				"mode " + mode + " is not valid. Must be '" + MODE_REQUIRED + "' or '"
					+ MODE_NOT_ALLOWED + "' or '" + MODE_REQUIRED_ELSE_NOT_ALLOWED + "'");
		}

		setupPassOverConstraintWhen(context);

		if (allowFormSubmissionWithError)
		{
			context.setBlocksCommitToNever();
		}

	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return helpMessage;
	}

	/**
	 * Configure this constraint to be ignored if the passOver data members are populated.
	 * If the passOver members are configured then the associated message is also updated.
	 *
	 * @param context
	 */
	private void setupPassOverConstraintWhen(ConstraintContext context)
	{
		if (passOverConstraintFieldPath == null)
		{
			return; // Nothing to setup
		}
		passOverConstraintFieldNode = passOverConstraintFieldPath.startsWith(Path.PARENT)
			? context.getSchemaNode().getNode(passOverConstraintFieldPath)
			: context.getSchemaNode().getTableNode().getTableOccurrenceRootNode().getNode(
				passOverConstraintFieldPath);
		if (passOverConstraintFieldNode == null)
			context.addError(passOverConstraintFieldPath.format() + " not found");
		if (passOverConstratintFieldValue == null)
			passOverConstratintFieldValue = String.valueOf(null);

		String passOverMessage = MessageFormat.format(
			IGNORE_CONSTRAINT_MESSAGE,
			passOverConstraintFieldNode.getLabel(Locale.getDefault()),
			passOverConstratintFieldValue);

		helpMessage += passOverMessage;
	}

	public boolean isUseEBXmessage()
	{
		return useEBXmessage;
	}

	public void setUseEBXmessage(boolean useEBXmessage)
	{
		this.useEBXmessage = useEBXmessage;
	}

	public String getCustomMessage()
	{
		return customMessage;
	}

	public void setCustomMessage(String customMessage)
	{
		this.customMessage = customMessage;
	}

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

	public boolean isAllowFormSubmissionWithError()
	{
		return allowFormSubmissionWithError;
	}

	public void setAllowFormSubmissionWithError(boolean allowFormSubmissionWithError)
	{
		this.allowFormSubmissionWithError = allowFormSubmissionWithError;
	}

}
