/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * An access rule for use with Multi Select that eliminates records already chosen / created
 */
public class MultiSelectAccessRule implements AccessRule
{
	private String serviceID;
	private TrackingInfoHelper trackingInfoHelper;
	private TableRefFilter filter;

	public MultiSelectAccessRule(String serviceID)
	{
		this(serviceID, null, null);
	}

	public MultiSelectAccessRule(String serviceID, TrackingInfoHelper baseTrackingInfoHelper)
	{
		this(serviceID, baseTrackingInfoHelper, null);
	}

	public MultiSelectAccessRule(
		String serviceID,
		TrackingInfoHelper baseTrackingInfoHelper,
		TableRefFilter filter)
	{
		this.serviceID = serviceID;
		this.trackingInfoHelper = MultiSelectUtil.createTrackingInfoHelper(baseTrackingInfoHelper);
		this.filter = filter;
	}

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isSchemaInstance() || adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}

		String trackingInfo = session.getTrackingInfo();
		if (trackingInfo == null)
		{
			return AccessPermission.getReadWrite();
		}

		trackingInfoHelper.initTrackingInfo(trackingInfo);
		if (!serviceID
			.equals(trackingInfoHelper.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_SERVICE_ID)))
		{
			return AccessPermission.getReadWrite();
		}

		Repository repo = adaptation.getHome().getRepository();
		String parentRecordPK = trackingInfoHelper
			.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_PARENT_RECORD_PK);
		if (parentRecordPK == null)
		{
			return AccessPermission.getReadWrite();
		}
		int joinTableParentPKPos = Integer
			.valueOf(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_PK_POSITION))
			.intValue();
		AdaptationHome joinDataSpace = repo.lookupHome(
			HomeKey.forBranchName(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SPACE)));
		Adaptation joinDataSet = joinDataSpace.findAdaptationOrNull(
			AdaptationName.forName(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SET)));
		Path joinTablePath = Path.parse(
			trackingInfoHelper.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_PATH));
		Path joinTableFKPath = Path.parse(
			trackingInfoHelper.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_FK_PATH));

		AdaptationTable joinTable = joinDataSet.getTable(joinTablePath);
		Path[] pkPaths = joinTable.getPrimaryKeySpec();
		RequestResult reqRes = joinTable.createRequestResult(
			Path.SELF.add(pkPaths[joinTableParentPKPos]).format() + "='" + parentRecordPK + "' and "
				+ Path.SELF.add(joinTableFKPath).format() + "='"
				+ adaptation.getOccurrencePrimaryKey().format() + "'");
		try
		{
			if (reqRes.isEmpty())
			{
				if (filter == null)
				{
					return AccessPermission.getReadWrite();
				}
				AdaptationHome parentTableDataSpace = repo.lookupHome(
					HomeKey.forBranchName(
						trackingInfoHelper.getTrackingInfoSegment(
							MultiSelectUtil.SEGMENT_PARENT_TABLE_DATA_SPACE)));
				Adaptation parentTableDataSet = parentTableDataSpace.findAdaptationOrNull(
					AdaptationName.forName(
						trackingInfoHelper.getTrackingInfoSegment(
							MultiSelectUtil.SEGMENT_PARENT_TABLE_DATA_SET)));
				AdaptationTable parentTable = parentTableDataSet.getTable(
					Path.parse(
						trackingInfoHelper
							.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_PARENT_TABLE_PATH)));
				Adaptation parentRecord = parentTable
					.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(parentRecordPK));
				return filter.accept(adaptation, parentRecord.createValueContext())
					? AccessPermission.getReadWrite()
					: AccessPermission.getHidden();
			}
		}
		finally
		{
			reqRes.close();
		}
		return AccessPermission.getHidden();
	}

	public TrackingInfoHelper getTrackingInfoHelper()
	{
		return trackingInfoHelper;
	}
}
