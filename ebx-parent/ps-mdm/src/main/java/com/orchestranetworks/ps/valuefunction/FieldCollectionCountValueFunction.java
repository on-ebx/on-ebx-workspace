/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A value function evaluates the paths as 'path expression' to produce a related collection,
 * and returns the size of that collection.
 */
public class FieldCollectionCountValueFunction implements ValueFunction
{
	private String pathsAsString;
	private Path[] adaptationPaths;
	private Path attributePath;
	private boolean ignoreNull = true;

	public String getPathsAsString()
	{
		return pathsAsString;
	}

	public void setPathsAsString(String pathsAsString)
	{
		this.pathsAsString = pathsAsString;
	}

	public boolean isIgnoreNull()
	{
		return ignoreNull;
	}

	public void setIgnoreNull(boolean ignoreNull)
	{
		this.ignoreNull = ignoreNull;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getValue(Adaptation adaptation)
	{
		List list;
		if (adaptationPaths != null)
		{
			list = attributePath != null ? AdaptationUtil.evaluatePath(
				adaptation,
				adaptationPaths,
				attributePath,
				true) : AdaptationUtil.evaluatePath(adaptation, adaptationPaths);
		}
		else
		{
			list = adaptation.getList(attributePath);
		}
		return Integer.valueOf(CollectionUtils.size(list));
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		List<Path> paths = PathUtils.convertStringToPathList(pathsAsString, null);
		if (paths.isEmpty())
			context.addError("Must specify at least on path representing a collection");
		SchemaNode caFieldNode = context.getSchemaNode();
		SchemaNode tableNode = caFieldNode.getTableNode().getTableOccurrenceRootNode();
		List<SchemaNode> nodes = PathUtils.validatePath(tableNode, paths);
		boolean hasCollection = false;
		int nodesSize = nodes.size();
		int pathsSize = paths.size();
		if (nodesSize < pathsSize)
		{
			Path unresolvedPath = paths.get(nodesSize);
			context.addError("Path element " + unresolvedPath.format() + " does not exist.");
			return;
		}
		for (int i = 0; i < paths.size(); i++)
		{
			SchemaNode node = nodes.get(i);
			if (node.getMaxOccurs() != 1)
			{
				hasCollection = true;
			}
		}
		if (!hasCollection)
			context.addError("Paths should resolve to a collection.");

		SchemaNode lastNode = nodes.get(nodes.size() - 1);
		boolean listIsLinkedRecordList = lastNode.isAssociationNode() || lastNode.isSelectNode();

		if (!listIsLinkedRecordList)
			attributePath = paths.remove(paths.size() - 1);

		if (!paths.isEmpty())
			adaptationPaths = paths.toArray(new Path[0]);
	}
}
