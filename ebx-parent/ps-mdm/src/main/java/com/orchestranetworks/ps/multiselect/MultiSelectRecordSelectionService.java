/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import java.io.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. This should be re-implemented as a User Service.
 */
@Deprecated
public class MultiSelectRecordSelectionService extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_SERVICE_NAME = "MultiSelectRecordSelection";

	public static final String PARAM_PARENT_TABLE_DATA_SPACE = "ms_parentDataSpace";
	public static final String PARAM_PARENT_TABLE_DATA_SET = "ms_parentDataSet";
	public static final String PARAM_PARENT_TABLE_PATH = "ms_parentTablePath";
	public static final String PARAM_PARENT_RECORD_PK = "ms_parentPK";
	public static final String PARAM_JOIN_TABLE_PARENT_PK_POSITION = "ms_joinParentPKPos";
	public static final String PARAM_JOIN_TABLE_FK_PATH = "ms_joinTableFKPath";

	private static final String FRAME_ID = "select_records_frame";

	private TrackingInfoHelper trackingInfoHelper;

	public MultiSelectRecordSelectionService()
	{
		this(null);
	}

	public MultiSelectRecordSelectionService(TrackingInfoHelper baseTrackingInfoHelper)
	{
		this.trackingInfoHelper = MultiSelectUtil.createTrackingInfoHelper(baseTrackingInfoHelper);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);
		String parentPK = request.getParameter(PARAM_PARENT_RECORD_PK);
		String joinParentPKPos = request.getParameter(PARAM_JOIN_TABLE_PARENT_PK_POSITION);
		Path joinFKPath = Path.SELF.add(Path.parse(request.getParameter(PARAM_JOIN_TABLE_FK_PATH)));
		SchemaNode joinTableNode = sContext.getCurrentNode();
		SchemaNode joinFKNode = joinTableNode.getNode(joinFKPath);

		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		Adaptation joinDataSet = sContext.getCurrentAdaptation();
		AdaptationHome joinDataSpace = joinDataSet.getHome();
		Repository repo = joinDataSpace.getRepository();

		SchemaFacetTableRef selectTableRef = joinFKNode.getFacetOnTableReference();
		Adaptation selectDataSet = joinDataSet;
		AdaptationHome selectDataSpace = joinDataSpace;
		HomeKey tableRefDataSpaceKey = selectTableRef.getContainerHome();
		if (tableRefDataSpaceKey != null)
		{
			selectDataSpace = repo.lookupHome(tableRefDataSpaceKey);
		}
		AdaptationName tableRefDataSetName = selectTableRef.getContainerReference();
		if (tableRefDataSetName != null)
		{
			selectDataSet = selectDataSpace.findAdaptationOrNull(tableRefDataSetName);
		}
		Path selectTablePath = selectTableRef.getTablePath();

		UIHttpManagerComponent uiMgr = sContext.createWebComponentForSubSession();
		uiMgr.selectNode(selectDataSet, selectTablePath);

		String trackingInfo = sContext.getSession().getTrackingInfo();
		if (trackingInfo != null)
		{
			trackingInfoHelper.initTrackingInfo(trackingInfo);
		}
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_SERVICE_ID,
			sContext.getServiceKey().getServiceName());
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_PARENT_TABLE_DATA_SPACE,
			request.getParameter(PARAM_PARENT_TABLE_DATA_SPACE));
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_PARENT_TABLE_DATA_SET,
			request.getParameter(PARAM_PARENT_TABLE_DATA_SET));
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_PARENT_TABLE_PATH,
			request.getParameter(PARAM_PARENT_TABLE_PATH));
		trackingInfoHelper
			.setTrackingInfoSegment(MultiSelectUtil.SEGMENT_PARENT_RECORD_PK, parentPK);
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_JOIN_TABLE_PK_POSITION,
			joinParentPKPos);
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SPACE,
			joinDataSpace.getKey().getName());
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_JOIN_TABLE_DATA_SET,
			joinDataSet.getAdaptationName().getStringName());
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_JOIN_TABLE_PATH,
			joinTableNode.getPathInSchema().format());
		trackingInfoHelper.setTrackingInfoSegment(
			MultiSelectUtil.SEGMENT_JOIN_TABLE_FK_PATH,
			joinFKPath.format());
		uiMgr.setTrackingInfo(trackingInfoHelper.createTrackingInfo());

		writer.add_cr(
			"<iframe id='" + FRAME_ID + "' name='" + FRAME_ID
				+ "' src='' style='border: none; width:100%'></iframe>");
		writer.addJS_addResizeWorkspaceListener("resizeFrame");
		String iFrameSrc = uiMgr.getURIWithParameters();
		// Setting the url directly in the iframe declaration causes 2 subsessions to be created,
		// and some unpredictable behavior. So it should be set via js code after creating the iframe.
		writer.addJS_cr("document.getElementById('" + FRAME_ID + "').src='" + iFrameSrc + "';");

		// This is a hack to remove the header since EBX provides no way to do it.
		writer.addJS_cr("window.onload=removeHeader();");

		writer.addJS_cr("function resizeFrame(size)");
		writer.addJS_cr("{");
		writer.addJS_cr("  var frameElement = document.getElementById('" + FRAME_ID + "');");
		writer.addJS_cr("  frameElement.style.height = size.h + 'px';");
		writer.addJS_cr("}");

		writer.addJS_cr("function removeHeader()");
		writer.addJS_cr("{");
		writer.addJS_cr("  document.getElementById('ebx_WorkspaceHeader').style.display='none';");
		writer.addJS_cr("}");
	}
}
