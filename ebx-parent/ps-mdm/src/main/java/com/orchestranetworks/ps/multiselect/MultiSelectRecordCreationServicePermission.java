/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public class MultiSelectRecordCreationServicePermission implements ServicePermission
{
	private static final String EMPTY_SEGMENT_NAME = "EmptySegment";

	private int serviceTrackingInfoPosition = 0;
	@SuppressWarnings("deprecation")
	private String selectionServiceName = MultiSelectRecordSelectionService.DEFAULT_SERVICE_NAME;
	private String separator = MultiSegmentTrackingInfoHelper.DEFAULT_SEPARATOR;

	@Override
	public ActionPermission getPermission(SchemaNode node, Adaptation adaptation, Session session)
	{
		if (session.getInteraction(true) == null)
		{
			return ActionPermission.getHidden();
		}
		String trackingInfo = session.getTrackingInfo();
		if (trackingInfo == null)
		{
			return ActionPermission.getHidden();
		}
		TrackingInfoHelper trackingInfoHelper = MultiSelectUtil
			.createTrackingInfoHelper(createEmptyTrackingInfoHelper());
		trackingInfoHelper.initTrackingInfo(trackingInfo);
		String serviceID = trackingInfoHelper
			.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_SERVICE_ID);
		return (serviceID != null && selectionServiceName.equals(serviceID))
			? ActionPermission.getEnabled()
			: ActionPermission.getHidden();
	}

	// This creates a dummy tracking info helper just so we can pad it out to where the
	// multi select part starts. We don't care what's in this.
	private TrackingInfoHelper createEmptyTrackingInfoHelper()
	{
		if (serviceTrackingInfoPosition < 1)
		{
			return null;
		}
		String[] segmentNames = new String[serviceTrackingInfoPosition];
		for (int i = 0; i < segmentNames.length; i++)
		{
			segmentNames[i] = EMPTY_SEGMENT_NAME + i;
		}
		return new MultiSegmentTrackingInfoHelper(segmentNames, separator);
	}

	public int getServiceTrackingInfoPosition()
	{
		return this.serviceTrackingInfoPosition;
	}

	public void setServiceTrackingInfoPosition(int serviceTrackingInfoPosition)
	{
		this.serviceTrackingInfoPosition = serviceTrackingInfoPosition;
	}

	public String getSelectionServiceName()
	{
		return this.selectionServiceName;
	}

	public void setSelectionServiceName(String selectionServiceName)
	{
		this.selectionServiceName = selectionServiceName;
	}

	public String getSeparator()
	{
		return this.separator;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}
}
