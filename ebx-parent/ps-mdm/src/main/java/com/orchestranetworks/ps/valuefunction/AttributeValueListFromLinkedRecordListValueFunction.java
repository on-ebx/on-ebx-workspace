/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A value function that returns an ArrayList from a linked record list.
 * -- if an attributePath is specified, then will return a List of the Attribute Values from the Linked Records 
 * -- otherwise will return the Linked Records as a List of Foreign Key values 
 */
public class AttributeValueListFromLinkedRecordListValueFunction
	extends
	AbstractForeignKeyValueFunction
{
	private Path sortField;
	private boolean ascending = true;
	private boolean distinct = true;
	private boolean excludeNulls = true;

	@Override
	public Object getActualValue(Adaptation adaptation)
	{
		if (attributePath != null)
		{
			return AdaptationUtil.getLinkedRecordList(
				adaptation,
				foreignKeyPath,
				attributePath,
				distinct,
				sortField,
				ascending,
				excludeNulls);
		}
		else
		{
			return AdaptationUtil
				.getLinkedRecordKeyList(adaptation, foreignKeyPath, distinct, sortField, ascending);

		}
	}
	@Override
	public boolean validateSetupForeignKeyPath(
		ValueFunctionContext context,
		SchemaNode foreignKeyNode)
	{
		boolean valid = (foreignKeyNode.isAssociationNode() || foreignKeyNode.isSelectNode());
		if (!valid)
			return false;
		if (sortField != null)
		{
			SchemaNode tableNode = AdaptationUtil.getTableNodeForRelated(foreignKeyNode);
			SchemaNode sortFieldNode = PathUtils.setupFieldNode(
				context,
				tableNode.getTableOccurrenceRootNode(),
				sortField,
				"sortField",
				false,
				false);
			return sortFieldNode != null;
		}
		return true;
	}

	public Path getSortField()
	{
		return sortField;
	}

	public void setSortField(Path sortField)
	{
		this.sortField = sortField;
	}
	public boolean isDistinct()
	{
		return distinct;
	}
	public void setDistinct(boolean distinct)
	{
		this.distinct = distinct;
	}
	public boolean isAscending()
	{
		return ascending;
	}
	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}
	public boolean isExcludeNulls()
	{
		return excludeNulls;
	}
	public void setExcludeNulls(boolean excludeNulls)
	{
		this.excludeNulls = excludeNulls;
	}
}
