/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 * A simple adaptation filter that checks if a field in the record is one of the values in the specified collection
 */
public class FieldValueInCollectionAdaptationFilter implements AdaptationFilter
{
	private Path fieldPath;
	private Collection<?> values;

	public FieldValueInCollectionAdaptationFilter(Path fieldPath, Collection<?> values)
	{
		this.fieldPath = fieldPath;
		this.values = values;
	}

	@Override
	public boolean accept(Adaptation record)
	{
		return values.contains(record.get(fieldPath));
	}

	public Path getFieldPath()
	{
		return this.fieldPath;
	}

	public void setFieldPath(Path fieldPath)
	{
		this.fieldPath = fieldPath;
	}

	public Collection<?> getValues()
	{
		return this.values;
	}

	public void setValues(Collection<Object> values)
	{
		this.values = values;
	}
}
