/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.selection.*;

/**
 */
public class MultiSelectRecordCreationService extends AbstractUserService<TableViewEntitySelection>
{
	private TrackingInfoHelper trackingInfoHelper;

	public MultiSelectRecordCreationService()
	{
		this(null);
	}

	public MultiSelectRecordCreationService(TrackingInfoHelper baseTrackingInfoHelper)
	{
		this.trackingInfoHelper = MultiSelectUtil.createTrackingInfoHelper(baseTrackingInfoHelper);
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		String trackingInfo = session.getTrackingInfo();
		trackingInfoHelper.initTrackingInfo(trackingInfo);
		final String parentPK = trackingInfoHelper
			.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_PARENT_RECORD_PK);
		final int joinParentPKPos = Integer
			.valueOf(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_PK_POSITION))
			.intValue();
		AdaptationHome currentHome = context.getEntitySelection().getDataspace();
		AdaptationHome joinDataSpace = currentHome.getRepository().lookupHome(
			HomeKey.forBranchName(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SPACE)));
		Adaptation joinDataSet = joinDataSpace.findAdaptationOrNull(
			AdaptationName.forName(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SET)));
		final AdaptationTable joinTable = joinDataSet.getTable(
			Path.parse(
				trackingInfoHelper
					.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_PATH)));
		final Path joinFKPath = Path.parse(
			trackingInfoHelper.getTrackingInfoSegment(MultiSelectUtil.SEGMENT_JOIN_TABLE_FK_PATH));

		final List<Adaptation> selectedRecords = AdaptationUtil
			.getRecords(context.getEntitySelection().getSelectedRecords().execute());

		Procedure createRecordsProc = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				CreateRecordProcedure crp = new CreateRecordProcedure(joinTable);
				crp.setAllPrivileges(true);
				for (Adaptation selectedRecord : selectedRecords)
				{
					PrimaryKey selectedRecordPK = selectedRecord.getOccurrencePrimaryKey();
					Path[] pkPaths = joinTable.getPrimaryKeySpec();
					crp.setValue(pkPaths[joinParentPKPos], parentPK);
					crp.setValue(joinFKPath, selectedRecordPK.format());
					crp.execute(pContext);
				}
			}
		};
		ProcedureExecutor.executeProcedure(createRecordsProc, session, joinDataSpace);

		String joinTableLabel = joinTable.getTableNode().getLabel(session.getLocale());
		int size = selectedRecords.size();
		StringBuilder bldr = new StringBuilder();
		bldr.append(size);
		bldr.append(" ");
		if (joinTableLabel != null)
		{
			bldr.append(joinTableLabel);
			bldr.append(" ");
		}
		if (size == 1)
		{
			bldr.append("record was");
		}
		else
		{
			bldr.append("records were");
		}
		bldr.append(" created.");
		alert(bldr.toString());
	}

	@Override
	public void landService()
	{
		UIHttpManagerComponent uiMgr = context.getWriter().createWebComponentForSubSession();
		AdaptationTable table = context.getEntitySelection().getTable();
		uiMgr.selectNode(table.getContainerAdaptation(), table.getTablePath());
		uiMgr.setTrackingInfo(context.getSession().getTrackingInfo());
		context.getWriter()
		.addJS_cr("window.location.href='" + uiMgr.getURIWithParameters() + "';");
	}
}
