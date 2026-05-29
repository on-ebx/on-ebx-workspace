package com.orchestranetworks.ps.util.functional;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * An {@link UnaryFunction} that simply returns the value of a field.
 * For example, this can be used by the acyclic pattern to display a field from the record
 * in the error message rather than the record's label. The field could be a computed field
 * creating a label just for use in the error message.
 */
public class GetStringFieldValueUnaryFunction extends UnaryFunction<Adaptation, String>
{
	private Path field;

	public GetStringFieldValueUnaryFunction(Path field)
	{
		this.field = field;
	}

	@Override
	public String evaluate(Adaptation record)
	{
		if (record == null)
		{
			return null;
		}
		return record.getString(field);
	}
}
