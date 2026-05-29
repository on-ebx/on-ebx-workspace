/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;

/**
 */
public class DataSetComparator implements Comparator<Adaptation>
{
	private Locale locale;

	public DataSetComparator(Locale locale)
	{
		this.locale = locale;
	}

	@Override
	public int compare(Adaptation dataSet1, Adaptation dataSet2)
	{
		if (dataSet1 == null)
		{
			return -1;
		}
		if (dataSet2 == null)
		{
			return 1;
		}
		String label1 = dataSet1.getLabelOrName(locale);
		String label2 = dataSet2.getLabelOrName(locale);
		return label1.compareTo(label2);
	}
}
