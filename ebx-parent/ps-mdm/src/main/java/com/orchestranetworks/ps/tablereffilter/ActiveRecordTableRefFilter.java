package com.orchestranetworks.ps.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A filter that only allows active records to be chosen, unless it's already the value of the foreign key
 * on the saved record. In other words, if the record was made inactive after having been chosen and saved,
 * then still allow it, but don't allow it to be chosen going forward.
 * 
 * The field on the foreign record that indicates if it's active or not is specified by
 * <code>indicatorFieldPath</code>. It must be a <code>boolean</code> field. Default value is "./isInactive".
 * If using another type, such as an enumeration, you can override the {@link #isActive(Adaptation)} method
 * to perform the necessary logic.
 * 
 * By default, this assumes that the indicator represents inactive when <code>true</code>, otherwise active.
 * If the opposite is desired, with <code>true</code> indicating active, then set
 * <code>indicatorRepresentsActive</code> to <code>true</code>.
 */
public class ActiveRecordTableRefFilter implements TableRefFilter
{
	private Path indicatorFieldPath = Path.parse("./isInactive");
	private boolean indicatorRepresentsActive;

	private SchemaNode node;
	private SchemaNode indicatorFieldNode;

	@Override
	public boolean accept(Adaptation record, ValueContext valueContext)
	{
		if (isActive(record))
		{
			return true;
		}
		Adaptation recordForVC = AdaptationUtil.getRecordForValueContext(valueContext);
		// If it hasn't been saved yet, don't allow it
		if (recordForVC == null)
		{
			return false;
		}
		// It's already been saved, allow it only if it's the same value that's already set.
		// If it's a multi-occurring node, this means checking that it's in the list.
		// Otherwise, it means just comparing the value.
		Object currentValue = recordForVC.get(node);
		if (currentValue == null)
		{
			return false;
		}
		String recordPK = record.getOccurrencePrimaryKey().format();
		if (node.getMaxOccurs() > 1)
		{
			return ((Collection<?>) currentValue).contains(recordPK);
		}
		return recordPK.equals(currentValue);
	}

	protected boolean isActive(Adaptation record)
	{
		Boolean value = (Boolean) record.get(indicatorFieldNode);
		boolean returnVal = Boolean.TRUE.equals(value);
		// If indicator doesn't represent active, then need to negate the value
		// (i.e. false means active, true means inactive)
		if (!indicatorRepresentsActive)
		{
			returnVal = !returnVal;
		}
		return returnVal;
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		// Store the node that this filter is on. We will use it to get the value on the saved record.
		node = context.getSchemaNode();
		// Verify the indicator field is a field on the table referenced by this foreign key,
		// and store the node so we can utilize it later
		SchemaNode foreignTableNode = node.getFacetOnTableReference().getTableNode();
		indicatorFieldNode = PathUtils.setupFieldNode(
			context,
			foreignTableNode,
			indicatorFieldPath,
			"indicatorFieldPath",
			true,
			false);

		context.addFilterErrorMessage(createMessage(Locale.getDefault()));
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return createMessage(locale);
	}

	/**
	 * Create the message to use as an error message and in the documentation
	 * 
	 * @param locale the locale
	 * @return the message
	 */
	protected String createMessage(Locale locale)
	{
		StringBuilder bldr = new StringBuilder(indicatorFieldNode.getTableNode().getLabel(locale))
			.append(" must ");
		if (indicatorRepresentsActive)
		{
			bldr.append("be active.");
		}
		else
		{
			// When the field indicates inactive rather than active, this wording seems less confusing
			bldr.append("not be inactive.");
		}
		return bldr.toString();
	}

	public Path getIndicatorFieldPath()
	{
		return indicatorFieldPath;
	}

	public void setIndicatorFieldPath(Path indicatorFieldPath)
	{
		this.indicatorFieldPath = indicatorFieldPath;
	}

	public boolean isIndicatorRepresentsActive()
	{
		return indicatorRepresentsActive;
	}

	public void setIndicatorRepresentsActive(boolean indicatorRepresentsActive)
	{
		this.indicatorRepresentsActive = indicatorRepresentsActive;
	}
}
