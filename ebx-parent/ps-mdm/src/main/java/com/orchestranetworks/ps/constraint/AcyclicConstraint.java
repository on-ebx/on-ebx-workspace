/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;

/**
 * This table constraint can be used to prevent cycles in the data.  Subclasses can override the setup method
 * to provide a getParents function, or this class can be used directly, by configuring it with a 'path expression'
 * which is a list of paths that express how to reach the 'parent' records from the record being validated.
 * 
 * This uses essentially the same logic as {@link com.orchestranetworks.ps.tablereffilter.AcyclicTableRefFilter}.
 * If finding the parent doesn't require queries, it's preferable to use the filter instead.
 * However, if there are queries involved, that can be a lot of processing so you should consider using this constraint.
 * 
 * Note that there's currently a limitation that the constraint can only verify this after the record is saved,
 * whereas the filter prevents it from saving, however we have an open request from support for an API call that will let
 * us fulfill that in the constraint as well.
 * 
 * There is also a getParent function, but that is strictly here for backwards-compatibility, to not break things if
 * someone was already using that. It's recommended to use either the built-in getParents (plural) method or to
 * pass in your own. By default, getParent won't be used unless you pass one in.
 * 
 * Best Practice is to use this Constraint in conjunction with the Acyclic Table Trigger which prevent cycles from happening.
 * -- if for some reason the Acyclic Table Trigger is NOT being used, 
 *       then it is recommended to turn on the parameter to "Execute during Table Validation",
 *       but be aware that this may add a significant performance impact depending on the table volumes
 * 
 */
public class AcyclicConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private AcyclicConfig config = new AcyclicConfig();
	// This is just a flag used to track if someone specifically specified a getParent
	private boolean useGetParent = false;
	// This will go away eventually
	private BinaryFunction<Adaptation, ValueContext, Adaptation> getParent;
	private boolean executeDuringTableValidation = false;

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);

		config.setup(context);

		final Path[] paths = config.getParentPath().toArray(new Path[0]);
		// Created for backwards-compatibility but default getParent isn't used
		getParent = new BinaryFunction<Adaptation, ValueContext, Adaptation>()
		{
			@Override
			public Adaptation evaluate(Adaptation record, ValueContext valueContext)
			{
				List<Adaptation> results;
				if (valueContext != null)
				{
					results = AdaptationUtil.evaluatePath(valueContext, paths);
				}
				else
				{
					results = AdaptationUtil.evaluatePath(record, paths);
				}
				return results == null || results.isEmpty() ? null : results.get(0);
			}
		};
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return config.createUserDocMessage();
	}

	@Override
	public void checkTable(ValueContextForValidationOnTable context)
	{
		if (executeDuringTableValidation)
		{
			super.checkTable(context);
		}
	}

	// Suppress deprecation warnings because we call into the deprecated checkCycle method.
	// Once that method is removed, we can get rid of this suppression.
	@SuppressWarnings("deprecation")
	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		// Setup can't always guarantee the paths are validated so if they weren't, we do it one time here
		if (config.isPathsValidationIndeterminate())
		{
			String msg = config.revalidatePaths();
			if (msg != null)
			{
				throw new IllegalStateException("Error in setup of constraint: " + msg);
			}
		}

		// TODO: This is assuming if the record doesn't exist yet, nothing can be already pointing to it
		// but that's not true. There could be broken foreign keys that will be un-broken once this is
		// created and then there could be an infinite loop. We need to check based on the pk of
		// the potentially unsaved context but currently there's no way to get associated records
		// from a ValueContext via the API.
		Adaptation record = AdaptationUtil.getRecordForValueContext(recordContext);
		if (record != null)
		{
			String cycle;
			if (useGetParent)
			{
				cycle = AdaptationUtil
					.detectCycle(record, recordContext, getGetParent(), getGetRecordLabel());
			}
			else
			{
				cycle = AdaptationUtil.detectAncestorCycle(
					record,
					recordContext,
					getGetParents(),
					getGetRecordLabel());
			}
			if (cycle != null)
				return MessageFormat.format(
					config.createCheckContextMessage(cycle),
					config.getPathDisplay(),
					cycle);
		}
		return null;
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		List<Path> parentPath = config.getParentPath();
		if (parentPath != null && !parentPath.isEmpty())
		{
			return recordContext.getNode(parentPath.iterator().next());
		}
		return null;
	}

	public String getPathsToParentString()
	{
		return config.getPathsToParentString();
	}

	public void setPathsToParentString(String pathsToParentString)
	{
		config.setPathsToParentString(pathsToParentString);
	}

	/**
	 * @deprecated Use {@link #getGetParents()} instead
	 */
	@Deprecated
	public BinaryFunction<Adaptation, ValueContext, Adaptation> getGetParent()
	{
		return this.getParent;
	}

	/**
	 * @deprecated Use {@link #setGetParents(BinaryFunction)} instead
	 */
	@Deprecated
	public void setGetParent(BinaryFunction<Adaptation, ValueContext, Adaptation> getParent)
	{
		this.getParent = getParent;
		this.useGetParent = (getParent != null);
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

	public boolean isExecuteDuringTableValidation()
	{
		return executeDuringTableValidation;
	}

	public void setExecuteDuringTableValidation(boolean executeDuringTableValidation)
	{
		this.executeDuringTableValidation = executeDuringTableValidation;
	}

}
