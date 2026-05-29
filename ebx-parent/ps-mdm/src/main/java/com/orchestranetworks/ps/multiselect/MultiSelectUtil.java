/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.orchestranetworks.ps.util.*;

/**
 */
public class MultiSelectUtil
{
	public static final String SEGMENT_SERVICE_ID = "serviceID";
	public static final String SEGMENT_PARENT_TABLE_DATA_SPACE = "parentTableDataSpace";
	public static final String SEGMENT_PARENT_TABLE_DATA_SET = "parentTableDataSet";
	public static final String SEGMENT_PARENT_TABLE_PATH = "parentTablePath";
	public static final String SEGMENT_PARENT_RECORD_PK = "parentRecordPK";
	public static final String SEGMENT_JOIN_TABLE_PK_POSITION = "joinTablePKPosition";
	public static final String SEGMENT_JOIN_TABLE_DATA_SPACE = "joinTableDataSpace";
	public static final String SEGMENT_JOIN_TABLE_DATA_SET = "joinTableDataSet";
	public static final String SEGMENT_JOIN_TABLE_PATH = "joinTablePath";
	public static final String SEGMENT_JOIN_TABLE_FK_PATH = "joinTableFKPath";

	static TrackingInfoHelper createTrackingInfoHelper()
	{
		return createTrackingInfoHelper(null);
	}

	static TrackingInfoHelper createTrackingInfoHelper(TrackingInfoHelper baseTrackingInfoHelper)
	{
		return new MultiSegmentTrackingInfoHelper(baseTrackingInfoHelper, new String[] {
				SEGMENT_SERVICE_ID, SEGMENT_PARENT_TABLE_DATA_SPACE, SEGMENT_PARENT_TABLE_DATA_SET,
				SEGMENT_PARENT_TABLE_PATH, SEGMENT_PARENT_RECORD_PK,
				SEGMENT_JOIN_TABLE_PK_POSITION, SEGMENT_JOIN_TABLE_DATA_SPACE,
				SEGMENT_JOIN_TABLE_DATA_SET, SEGMENT_JOIN_TABLE_PATH, SEGMENT_JOIN_TABLE_FK_PATH });
	}

	private MultiSelectUtil()
	{
		// do nothing
	}
}
