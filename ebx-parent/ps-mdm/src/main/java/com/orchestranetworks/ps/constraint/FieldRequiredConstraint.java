package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This constraint on null will check if this field (on which the constraint is declared) has a value.
 *
 *   Since setting a field as minOccurs=1 in the Data Modeling UI is will always result as an Error,
 *    this constraint is necessary when you want a required field to give a Warning or Info message
 * 		<severity>F=Fatal, E=Error (default), W=Warning, I=Information</severity>
 */
public class FieldRequiredConstraint<T> implements Constraint<T>, ConstraintOnNull
{
	private String severity = Severity.ERROR.toParsableString();

	private static String EBX_MESSAGE = "Field {0} is required.";
	private String message;

	@Override
	public void checkNull(ValueContextForValidation context) throws InvalidSchemaException
	{
		context.addMessage(AdaptationUtil.createUserMessage(message, Severity.parseFlag(severity)));
	}

	@Override
	public void checkOccurrence(T value, ValueContextForValidation context)
		throws InvalidSchemaException
	{
		return;
	}

	@Override
	public void setup(ConstraintContext context)
	{
		message = MessageFormat
			.format(EBX_MESSAGE, "'" + context.getSchemaNode().getLabel(Locale.getDefault()) + "'");
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return message;
	}

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

}
