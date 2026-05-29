package com.orchestranetworks.ps.admin.devartifacts.util;

import org.apache.commons.lang3.builder.*;

import com.onwbp.adaptation.*;

/**
 * Contains a table and a predicate for that table
 */
public class AdaptationTablePredicate
{
	private AdaptationTable table;
	private String predicate;

	public AdaptationTablePredicate(AdaptationTable table)
	{
		this(table, null);
	}

	public AdaptationTablePredicate(AdaptationTable table, String predicate)
	{
		this.table = table;
		this.predicate = predicate;
	}

	public AdaptationTable getTable()
	{
		return table;
	}

	public void setTable(AdaptationTable table)
	{
		this.table = table;
	}

	public String getPredicate()
	{
		return predicate;
	}

	public void setPredicate(String predicate)
	{
		this.predicate = predicate;
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder bldr = new HashCodeBuilder();
		bldr.append(table);
		bldr.append(predicate);
		return bldr.toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof AdaptationTablePredicate)
		{
			AdaptationTablePredicate adaptationTablePredicate = (AdaptationTablePredicate) obj;
			EqualsBuilder bldr = new EqualsBuilder();
			bldr.append(table, adaptationTablePredicate.getTable());
			bldr.append(predicate, adaptationTablePredicate.getPredicate());
			return bldr.isEquals();
		}
		return false;
	}
}