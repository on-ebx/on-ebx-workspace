package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * A filter that allows records that are in a given collection of primary keys.
 * If a <code>foreignKeyPath</code> is specified, it will look at that field for the value.
 * Otherwise, it will look at the record's primary key.
 * This extends {@link NonBuiltInAdaptationFilter} to also allow filtering of built-in records,
 * since they're both features used by many addons.
 */
public class PrimaryKeySetContainsAdaptationFilter extends NonBuiltInAdaptationFilter
{
	private Collection<String> primaryKeys;
	private Path foreignKeyPath;

	public PrimaryKeySetContainsAdaptationFilter(
		Collection<String> primaryKeys,
		Path foreignKeyPath,
		Path builtInFieldPath)
	{
		super(builtInFieldPath);
		this.primaryKeys = primaryKeys;
		this.foreignKeyPath = foreignKeyPath;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		if (!super.accept(adaptation))
		{
			return false;
		}
		// Null indicates to not use a cache at all and only check the built-in field above
		if (primaryKeys == null)
		{
			return true;
		}
		if (primaryKeys.isEmpty())
		{
			return false;
		}
		String pk;
		if (foreignKeyPath == null)
		{
			pk = adaptation.getOccurrencePrimaryKey().format();
		}
		else
		{
			pk = adaptation.getString(foreignKeyPath);
			if (pk == null)
			{
				return false;
			}
		}
		return primaryKeys.contains(pk);
	}
}
