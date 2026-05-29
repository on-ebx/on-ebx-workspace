/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

/**
 * This assumes the only segment you're concerned about is the first one and therefore
 * can be a bit more efficient than parsing the whole thing
 */
public class FirstSegmentTrackingInfoHelper implements TrackingInfoHelper
{
	public static final String DEFAULT_SEGMENT_NAME = "firstSegment";

	private String segName;
	private String separator;
	private String fullTrackingInfo;
	private String[] segmentArr;

	public FirstSegmentTrackingInfoHelper()
	{
		this(DEFAULT_SEGMENT_NAME, DEFAULT_SEPARATOR);
	}

	public FirstSegmentTrackingInfoHelper(String segmentName)
	{
		this(segmentName, DEFAULT_SEPARATOR);
	}

	public FirstSegmentTrackingInfoHelper(String segmentName, String separator)
	{
		this.segName = segmentName;
		this.separator = separator;
	}

	@Override
	public String[] getSegmentNames()
	{
		return new String[] { segName };
	}

	@Override
	public String createTrackingInfo()
	{
		if (fullTrackingInfo == null)
		{
			return null;
		}
		int ind = fullTrackingInfo.indexOf(separator);
		if (ind == -1)
		{
			return segmentArr[0];
		}
		int nextStartInd = ind + separator.length();
		if (fullTrackingInfo.length() == nextStartInd)
		{
			return segmentArr[0] + separator;
		}
		return segmentArr[0] + separator + fullTrackingInfo.substring(nextStartInd);
	}

	@Override
	public String[] initTrackingInfo(String trackingInfo)
	{
		this.fullTrackingInfo = trackingInfo;
		segmentArr = new String[1];
		if (trackingInfo == null)
		{
			return segmentArr;
		}
		int ind = trackingInfo.indexOf(separator);
		if (ind == -1)
		{
			segmentArr[0] = trackingInfo;
		}
		else
		{
			segmentArr[0] = trackingInfo.substring(0, ind);
		}
		return segmentArr;
	}

	@Override
	public String getTrackingInfoSegment(String segmentName)
	{
		return segmentArr[0];
	}

	@Override
	public void setTrackingInfoSegment(String segmentName, String segmentValue)
	{
		segmentArr[0] = segmentValue;
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
}
