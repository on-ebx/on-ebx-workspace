/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

/**
 */
public interface TrackingInfoHelper
{
	public static final String DEFAULT_SEPARATOR = "::";

	String getSeparator();

	String[] getSegmentNames();

	String createTrackingInfo();

	String[] initTrackingInfo(String trackingInfo);

	String getTrackingInfoSegment(String segmentName);

	void setTrackingInfoSegment(String segmentName, String segmentValue);
}
