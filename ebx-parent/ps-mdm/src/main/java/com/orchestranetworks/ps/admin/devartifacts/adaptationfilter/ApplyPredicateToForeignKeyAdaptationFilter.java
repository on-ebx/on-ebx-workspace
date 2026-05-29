package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.schema.*;

/**
 * An adaptation filter that applies a predicate to a foreign key field
 * and only allows the record if it matches.
 * 
 * To follow multiple foreign keys (from record A to record B to record C, etc), then you can do
 * so by specifying a list of paths, in order, all relative to the root of their respective records.
 * For example, <code>{./fkToB, ./fkToC}</code>. The predicate supplied would be on table C
 * in that example.
 */
public class ApplyPredicateToForeignKeyAdaptationFilter implements AdaptationFilter
{
	private Path[] foreignKeyPaths;
	private String predicate;

	public ApplyPredicateToForeignKeyAdaptationFilter(Path[] foreignKeyPaths, String predicate)
	{
		this.foreignKeyPaths = foreignKeyPaths;
		this.predicate = predicate;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		Adaptation recordToUse = DevArtifactsUtil
			.followForeignKeyChain(foreignKeyPaths, adaptation);
		if (recordToUse == null)
		{
			return false;
		}
		return recordToUse.matches(predicate);
	}
}
