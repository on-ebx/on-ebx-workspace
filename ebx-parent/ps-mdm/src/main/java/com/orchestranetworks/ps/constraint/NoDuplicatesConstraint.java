/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Configure this table constraint with a fieldPath that represents a list field (can be any field with maxOccurs != 1)
 * and the constraint will ensure that the list contains no duplicates.
 * For example, if ./group/field is a path to a field on the table, setUp will ensure that the path exists, represents
 * a list field, and the checkOccurence will ensure there are no duplicates.
 * 
 * Optional Parameter @caseInsensitive to perform Case Insensitive check (applicable for String fields only), defaults to false
 */
public class NoDuplicatesConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private Path fieldPath;
	private SchemaNode field;
	private boolean caseInsensitive = false;

	public Path getFieldPath()
	{
		return fieldPath;
	}

	public void setFieldPath(Path fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	public boolean isCaseInsensitive()
	{
		return caseInsensitive;
	}

	public void setCaseInsensitive(boolean caseInsensitive)
	{
		this.caseInsensitive = caseInsensitive;
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return field.getLabel(locale) + " can not contain duplicates.";
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		field = PathUtils.setupFieldNode(context, fieldPath, "fieldPath", true);
		if (field.getMaxOccurs() == 1)
			context
				.addError("fieldPath " + fieldPath.format() + " does not represent a list field.");
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return field;
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		if (AdaptationUtil.hasDuplicates(recordContext, field, caseInsensitive))
			return field.getLabel(Locale.getDefault()) + " contains duplicates.";
		return null;
	}

}
