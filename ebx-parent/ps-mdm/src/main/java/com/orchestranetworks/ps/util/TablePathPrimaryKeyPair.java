/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 */
public class TablePathPrimaryKeyPair
{
	private Path tablePath;
	private PrimaryKey primaryKey;

	public TablePathPrimaryKeyPair()
	{
	}

	public TablePathPrimaryKeyPair(Path tablePath, PrimaryKey primaryKey)
	{
		this.tablePath = tablePath;
		this.primaryKey = primaryKey;
	}

	@Override
	public int hashCode()
	{
		int hashCode = 1;
		hashCode = 31 * hashCode + ((tablePath == null) ? 0 : tablePath.hashCode());
		hashCode = 31 * hashCode + ((primaryKey == null) ? 0 : primaryKey.hashCode());
		return hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		TablePathPrimaryKeyPair other = (TablePathPrimaryKeyPair) obj;
		return Objects.equals(this.tablePath, other.tablePath)
			&& Objects.equals(this.primaryKey, other.primaryKey);
	}

	@Override
	public String toString()
	{
		return "TablePathPrimaryKeyPair [tablePath=" + tablePath + ", primaryKey=" + primaryKey
			+ "]";
	}

	public Path getTablePath()
	{
		return this.tablePath;
	}

	public void setTablePath(Path tablePath)
	{
		this.tablePath = tablePath;
	}

	public PrimaryKey getPrimaryKey()
	{
		return this.primaryKey;
	}

	public void setPrimaryKey(PrimaryKey primaryKey)
	{
		this.primaryKey = primaryKey;
	}
}
