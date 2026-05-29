package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.schema.*;

/**
 * An adaptation filter that applies a different adaptation filter to a foreign key field
 * and only allows the record if that filter accepts it.
 * 
 * In other words, if a filter is already written on the table that a foreign key points to,
 * this allows you to simply invoke that code on the foreign record.
 * 
 * To follow multiple foreign keys (from record A to record B to record C, etc), then you can do
 * so by specifying a list of paths, in order, all relative to the root of their respective records.
 * For example, <code>{./fkToB, ./fkToC}</code>. The adapatation filter supplied would be on table C
 * in that example.
 */
public class ApplyAdaptationFilterToForeignKeyAdaptationFilter implements AdaptationFilter
{
	private Path[] foreignKeyPaths;
	private AdaptationFilter adaptationFilter;

	public ApplyAdaptationFilterToForeignKeyAdaptationFilter(
		Path[] foreignKeyPaths,
		AdaptationFilter adaptationFilter)
	{
		this.foreignKeyPaths = foreignKeyPaths;
		this.adaptationFilter = adaptationFilter;
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
		return adaptationFilter.accept(recordToUse);
	}
}
