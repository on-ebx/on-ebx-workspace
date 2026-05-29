/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import java.util.*;

/**
 * This allows to specify multiple segments in a tracking info and allows the
 * component utilizing it to not need to know where in the tracking info the
 * tokens are.
 */
public class MultiSegmentTrackingInfoHelper implements TrackingInfoHelper
{
	private String separator = DEFAULT_SEPARATOR;
	private String[] segmentNames;
	private Map<String, String> segmentValueMap;

	public MultiSegmentTrackingInfoHelper()
	{
		this(null, new String[0]);
	}

	public MultiSegmentTrackingInfoHelper(String[] segmentNames)
	{
		this(null, segmentNames);
	}

	public MultiSegmentTrackingInfoHelper(String[] segmentNames, String separator)
	{
		this(null, segmentNames);
		this.separator = separator;
	}

	public MultiSegmentTrackingInfoHelper(
		TrackingInfoHelper baseTrackingInfoHelper,
		String[] segmentNames)
	{
		if (baseTrackingInfoHelper == null)
		{
			setSegmentNames(segmentNames);
		}
		else
		{
			this.separator = baseTrackingInfoHelper.getSeparator();
			String[] baseNames = baseTrackingInfoHelper.getSegmentNames();
			String[] arr = new String[baseNames.length + segmentNames.length];
			for (int i = 0; i < baseNames.length; i++)
			{
				arr[i] = baseNames[i];
			}
			for (int i = 0; i < segmentNames.length; i++)
			{
				arr[baseNames.length + i] = segmentNames[i];
			}
			setSegmentNames(arr);
		}
	}

	@Override
	public String getSeparator()
	{
		return this.separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	@Override
	public String[] getSegmentNames()
	{
		return this.segmentNames;
	}

	public void setSegmentNames(String[] segmentNames)
	{
		this.segmentNames = segmentNames;
		this.segmentValueMap = new HashMap<>();
	}

	@Override
	public String createTrackingInfo()
	{
		String[] values = new String[segmentNames.length];
		for (int i = 0; i < segmentNames.length; i++)
		{
			values[i] = segmentValueMap.get(segmentNames[i]);
		}
		return getTrackingInfo(values, separator);
	}

	@Override
	public String[] initTrackingInfo(String trackingInfo)
	{
		segmentValueMap = new HashMap<>();
		String[] arr = getTrackingInfoValues(trackingInfo, separator);
		for (int i = 0; i < arr.length; i++)
		{
			segmentValueMap.put(segmentNames[i], arr[i]);
		}
		return arr;
	}

	@Override
	public String getTrackingInfoSegment(String segmentName)
	{
		if (segmentName == null)
		{
			return null;
		}
		return segmentValueMap.get(segmentName);
	}

	public String getTrackingInfoSegment(int index)
	{
		if (index >= segmentNames.length)
		{
			return null;
		}
		return getTrackingInfoSegment(segmentNames[index]);
	}

	@Override
	public void setTrackingInfoSegment(String segmentName, String segmentValue)
	{
		segmentValueMap.put(segmentName, segmentValue);
	}

	public void setTrackingInfoSegment(int index, String segmentValue)
	{
		if (index >= segmentNames.length)
		{
			return;
		}
		setTrackingInfoSegment(segmentNames[index], segmentValue);
	}

	public static String[] getTrackingInfoValues(String trackingInfo)
	{
		return getTrackingInfoValues(trackingInfo, DEFAULT_SEPARATOR);
	}

	public static String[] getTrackingInfoValues(String trackingInfo, String separator)
	{
		if (trackingInfo == null)
		{
			return null;
		}
		return trackingInfo.split(separator);
	}

	public static String getTrackingInfo(String[] trackingInfoValues, String separator)
	{
		if (trackingInfoValues.length == 0)
		{
			return null;
		}
		StringBuilder bldr = new StringBuilder();
		for (int i = 0; i < trackingInfoValues.length; i++)
		{
			String str = trackingInfoValues[i];
			if (str != null)
			{
				bldr.append(str);
			}
			if (i < trackingInfoValues.length - 1)
			{
				bldr.append(separator);
			}
		}
		return bldr.toString();
	}
}
