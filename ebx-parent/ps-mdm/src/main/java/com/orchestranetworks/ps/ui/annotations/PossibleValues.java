package com.orchestranetworks.ps.ui.annotations;

import java.util.*;

public abstract class PossibleValues
{
	private List<EnumerationValue> values = new ArrayList<>();

	protected PossibleValues()
	{
		initValues();
	}

	public abstract void initValues();

	public List<EnumerationValue> getValues()
	{
		return values;
	}

	public boolean add(EnumerationValue value)
	{
		return values.add(value);
	}

}
