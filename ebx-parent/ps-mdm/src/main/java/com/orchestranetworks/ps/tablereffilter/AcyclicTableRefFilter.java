/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * This filter can be used to prevent cycles in the data.  Subclasses can override the setup method
 * to provide a getParent function, or this class can be used directly, by configuring it with a 'path expression'
 * which is a list of paths that express how to reach the 'parent' record from the record being validated.
 * 
 * This uses essentially the same logic as {@link com.orchestranetworks.ps.constraint.AcyclicConstraint}. If finding the parent doesn't require
 * queries, it's preferable to use this filter. However, if there are queries involved, that can be a lot
 * of processing so you should consider using the constraint instead. Also, the constraint will give a message showing the full path of the broken
 * ancestry, but the filter won't, since filters don't give context-sensitive messages.
 * 
 * Also, there's currently a limitation that the constraint can only verify this after the record is saved, whereas the filter prevents it from saving,
 * however we have an open request from support for an API call that will let us fulfill that in the constraint as well.
 * 
 * If this is placed on the parent foreign key on a link table, you must specify the path to the child record, via <code>childPath</code>.
 * If this is placed on the child foreign key on a link table, you must NOT specify the <code>childPath</code> and instead specify the
 * relative path from the child path to the parent path by specifying {@link #setChildFilterPathToParentPath(Path)}.
 * (For example, <code>../parentField</code>). This is separate from the <code>pathsToParentString</code> or <code>getParents</code> function.
 * Those should still be relative to the foreign table.
 * 
 * When this is on a child path, it will first check that the parent is not the immediate parent on this link table, and if it is not, it will then
 * utilize the normal mechanism for determining ancestors starting with the foreign record, except that instead of checking if this value is an ancestor
 * of the record being selected, it will check if the record being selected is an ancestor of this value. (i.e. opposite logic since it's on the child).
 * 
 * You should not specify <code>childFilterPathToParentPath</code> if this is not on the child path of a link table.
 */
public class AcyclicTableRefFilter implements TableRefFilter
{
	protected AcyclicConfig config = new AcyclicConfig();

	private Path childFilterPathToParentPath;
	private SchemaNode childFilterPathToParentNode;

	@Override
	public boolean accept(Adaptation adaptation, ValueContext valueContext)
	{
		// Setup can't always guarantee the paths are validated so if they weren't, we do it one time here
		if (config.isPathsValidationIndeterminate())
		{
			String msg = config.revalidatePaths();
			if (msg != null)
			{
				throw new IllegalStateException("Error in setup of filter: " + msg);
			}
		}
		Path childPath = getChildPath();
		PrimaryKey pk = null;
		// Use this record's primary key if we're not on a link table,
		// which we will know if neither a child path nor path to parent from a child path is specified
		if (childPath == null && childFilterPathToParentNode == null)
		{
			pk = valueContext.getAdaptationTable().computePrimaryKey(valueContext);
		}
		// Otherwise determine it based on if we're on the child or parent
		else
		{
			// If this is null, then we're on the parent path so use the child's value when we check the parent hierarchy
			if (childFilterPathToParentNode == null)
			{
				String childValue = (String) valueContext.getValue(childPath);
				if (childValue != null)
				{
					pk = PrimaryKey.parseString(childValue);
				}
			}
			// Else the filter is on the child path
			else
			{
				String parentPK = (String) valueContext.getValue(childFilterPathToParentNode);
				// If there's a value to the parent foreign key
				if (parentPK != null)
				{
					PrimaryKey adaptationPK = adaptation.getOccurrencePrimaryKey();
					// If that value is the same as this child foreign key's value, then it's not allowed
					if (adaptationPK.format().equals(parentPK))
					{
						return false;
					}
					// Get the record for the parent foreign key
					Adaptation parentRecord = adaptation.getContainerTable()
						.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(parentPK));
					if (parentRecord == null)
					{
						return true;
					}
					// It's valid if it's not an ancestor of the parent and invalid otherwise
					return !AdaptationUtil
						.isAncestorOf(adaptationPK, parentRecord, config.getGetParents());
				}
			}
		}
		// If there was no pk then return true.
		// Otherwise it's valid if this value is not an ancestor of it.
		// (This code won't get called when we're on the child field.)
		return pk == null ? true
			: !AdaptationUtil.isAncestorOf(pk, adaptation, config.getGetParents());
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		config.setup(context);
		childFilterPathToParentNode = PathUtils.setupFieldNode(
			context,
			context.getSchemaNode(),
			childFilterPathToParentPath,
			"childFilterPathToParentPath",
			false,
			false);
		context.addFilterErrorMessage(config.createUserDocMessage());
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return config.createUserDocMessage();
	}

	public String getPathsToParentString()
	{
		return config.getPathsToParentString();
	}

	public void setPathsToParentString(String pathsToParentString)
	{
		config.setPathsToParentString(pathsToParentString);
	}

	public BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getGetParents()
	{
		return config.getGetParents();
	}

	public void setGetParents(BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents)
	{
		config.setGetParents(getParents);
	}

	public UnaryFunction<Adaptation, String> getGetRecordLabel()
	{
		return config.getGetRecordLabel();
	}

	public void setGetRecordLabel(UnaryFunction<Adaptation, String> getRecordLabel)
	{
		config.setGetRecordLabel(getRecordLabel);
	}

	public Path getChildPath()
	{
		return config.getChildPath();
	}

	public void setChildPath(Path childPath)
	{
		config.setChildPath(childPath);
	}

	public Path getChildFilterPathToParentPath()
	{
		return childFilterPathToParentPath;
	}

	public void setChildFilterPathToParentPath(Path childFilterPathToParentPath)
	{
		this.childFilterPathToParentPath = childFilterPathToParentPath;
	}
}
