/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * This constraint enumeration can be used to create a selection field for
 * choosing a field (terminal node) within a table.  Concrete subclasses of
 * this class should implement the getTable(ValueContext) method.
 */
public abstract class TableFieldConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a field.";
	private boolean includeHidden = false;

	public boolean isIncludeHidden() {
		return includeHidden;
	}

	public void setIncludeHidden(boolean includeHidden) {
		this.includeHidden = includeHidden;
	}

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		if (aValue != null)
		{
			try
			{
				Path fieldPath = Path.SELF.add(Path.parse(aValue));
				AdaptationTable table = getTable(aValidationContext);
				if (table == null || table.getTableOccurrenceRootNode().getNode(fieldPath) == null)
					aValidationContext.addError(aValue + " does not exist");
			}
			catch (Exception e)
			{
				aValidationContext.addError(aValue + " does not exist");
			}
		}
	}

	protected abstract AdaptationTable getTable(ValueContext valueContext);

	@Override
	public void setup(ConstraintContext aContext)
	{
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	@Override
	public String displayOccurrence(String aValue, ValueContext aContext, Locale aLocale)
		throws InvalidSchemaException
	{
		if (aValue != null)
		{
			Path fieldPath = Path.SELF.add(Path.parse(aValue));
			AdaptationTable table = getTable(aContext);
			if (table == null)
				return fieldPath.format();
			SchemaNode field = table.getTableOccurrenceRootNode().getNode(fieldPath);
			return field == null ? aValue : getLabel(field, aLocale);
		}
		return null;
	}
	
	private static String getLabel(SchemaNode field, Locale locale) {
		String label = field.getLabel(locale);
		SchemaNode parent = field.getParent();
		if (parent == null || parent.getPathInAdaptation().equals(Path.ROOT))
			return label;
		return getLabel(parent, locale) + "/" + label;
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		AdaptationTable table = getTable(aContext);
		if (table == null)
			return Collections.emptyList();
		List<String> fieldNames = new ArrayList<>();
		SchemaNode rootNode = table.getTableOccurrenceRootNode();
		collectFields(fieldNames, rootNode.getNodeChildren());
		return fieldNames;
	}

	private void collectFields(List<String> fieldNames, SchemaNode[] nodeChildren)
	{
		for (SchemaNode schemaNode : nodeChildren)
		{
			SchemaNodeDefaultView viewProps = schemaNode.getDefaultViewProperties();
			if (!includeHidden && viewProps != null && viewProps.isHidden())
				continue;
			if (schemaNode.isTerminalValue() && !schemaNode.isAssociationNode()
				&& !schemaNode.isSelectNode())
				fieldNames.add(schemaNode.getPathInAdaptation().format());
			else
				collectFields(fieldNames, schemaNode.getNodeChildren());
		}
	}

}
