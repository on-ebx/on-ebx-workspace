package com.orchestranetworks.ps.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * Filter that enforces that the selected value of the field is the same as the
 * value of the same field in the parent record, when there is a value for the parent field.
 * 
 * Note that the <code>parentFieldPath</code> should be specified relative to the field
 * that this filter is on. (e.g. <code>"../parent"</code>)
 */
public class MatchesSameFieldFromParentTableRefFilter implements TableRefFilter
{
	private Path parentFieldPath;

	private SchemaNode node;
	private SchemaNode parentFieldNode;
	private String errorMessage;

	@Override
	public boolean accept(Adaptation record, ValueContext valueContext)
	{
		// Get the parent and if there is none, any value is fine
		Adaptation parent = AdaptationUtil.followFK(valueContext, parentFieldPath);
		if (parent == null)
		{
			return true;
		}
		// Get the value of the same field that we're on, but from the parent record
		String parentValue = (String) parent.get(node);
		// If the selected record has the same primary key as the value from the parent, it's good
		return record.getOccurrencePrimaryKey().format().equals(parentValue);
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		// Store the node and the parent node for use later
		node = context.getSchemaNode();
		parentFieldNode = PathUtils
			.setupFieldNode(context, parentFieldPath, "parentFieldPath", true, false);

		errorMessage = createErrorMessage(Locale.getDefault());
		context.addFilterErrorMessage(errorMessage);
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return errorMessage;
	}

	private String createErrorMessage(Locale locale)
	{
		return new StringBuilder(node.getLabel(locale)).append(" must match value from ")
			.append(parentFieldNode.getLabel(locale))
			.append(".")
			.toString();
	}

	public Path getParentFieldPath()
	{
		return parentFieldPath;
	}

	public void setParentFieldPath(Path parentFieldPath)
	{
		this.parentFieldPath = parentFieldPath;
	}
}
