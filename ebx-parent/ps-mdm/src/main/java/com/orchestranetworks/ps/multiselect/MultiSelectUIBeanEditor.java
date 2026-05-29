/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 */
@Deprecated
public class MultiSelectUIBeanEditor extends UIBeanEditor
{
	private int joinTableParentPKPosition = 0;
	private Path joinTableFKPath;
	private String selectLabel = "Select";
	private String selectionServiceName = MultiSelectRecordSelectionService.DEFAULT_SERVICE_NAME;
	private boolean allowOutsideWorkflow = false;
	private boolean allowWithoutCreate = false;

	public boolean isAllowWithoutCreate()
	{
		return allowWithoutCreate;
	}

	public void setAllowWithoutCreate(boolean allowWithoutCreate)
	{
		this.allowWithoutCreate = allowWithoutCreate;
	}

	public boolean isAllowOutsideWorkflow()
	{
		return allowOutsideWorkflow;
	}

	public void setAllowOutsideWorkflow(boolean allowOutsideWorkflow)
	{
		this.allowOutsideWorkflow = allowOutsideWorkflow;
	}

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		UIWidget widget = context.newBestMatching(Path.SELF);
		widget.setEditorDisabled(true);
		context.addWidget(widget);
	}

	@Override
	public void addForNull(UIResponseContext context)
	{
		addForEdit(context);
	}

	@Override
	public void addForEdit(UIResponseContext context)
	{
		if (!allowWithoutCreate)
		{
			context.addWidget(Path.SELF);
		}

		Session session = context.getSession();
		if (session.getInteraction(true) == null && !allowOutsideWorkflow)
		{
			return;
		}
		ValueContext vc = context.getValueContext();
		AdaptationTable table = vc.getAdaptationTable();
		PrimaryKey pk = table.computePrimaryKey(vc);
		Adaptation record = table.lookupAdaptationByPrimaryKey(pk);
		// It shouldn't be null or else the editor wouldn't be displayed
		// but just in case
		if (record == null)
		{
			return;
		}

		// TODO: We're getting the request result just to get the link table.
		//       Probably is a better way to do it that doesn't execute the query.
		AdaptationTable joinTable;
		RequestResult reqRes = AdaptationUtil
			.linkedRecordLookup(record, Path.SELF.add(context.getNode().getPathInAdaptation()));
		try
		{
			joinTable = reqRes.getTable();
		}
		finally
		{
			reqRes.close();
		}

		if (allowWithoutCreate
			|| PermissionsUtil.isUserPermittedToCreateRecord(context.getSession(), joinTable))
		{
			if (!allowWithoutCreate)
			{
				context.add(
					"<label style='margin-left:30px; font-style:normal; color:black'>" + selectLabel
						+ "</label>");
			}
			UIHttpManagerComponent uiMgr = context.createWebComponentForSubSession();
			uiMgr.selectNode(joinTable.getContainerAdaptation(), joinTable.getTablePath());
			uiMgr.setTrackingInfo(session.getTrackingInfo());
			uiMgr.setService(ServiceKey.forName(selectionServiceName));
			String url = createURL(uiMgr.getURIWithParameters(), table, pk);
			UIButtonSpecJSAction buttonSpec = context.buildButtonPreview(url);
			buttonSpec.setButtonLayout(UIButtonLayout.ICON_ONLY);
			buttonSpec.setRelief(UIButtonRelief.FLAT);
			context.addButtonJavaScript(buttonSpec);
		}
	}

	private String createURL(String serviceURL, AdaptationTable table, PrimaryKey pk)
	{
		Adaptation dataSet = table.getContainerAdaptation();

		StringBuilder bldr = new StringBuilder(serviceURL);
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_PARENT_TABLE_DATA_SPACE);
		bldr.append("=");
		bldr.append(dataSet.getHome().getKey().getName());
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_PARENT_TABLE_DATA_SET);
		bldr.append("=");
		bldr.append(dataSet.getAdaptationName().getStringName());
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_PARENT_TABLE_PATH);
		bldr.append("=");
		bldr.append(table.getTablePath().format());
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_PARENT_RECORD_PK);
		bldr.append("=");
		bldr.append(pk.format());
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_JOIN_TABLE_PARENT_PK_POSITION);
		bldr.append("=");
		bldr.append(joinTableParentPKPosition);
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_JOIN_TABLE_FK_PATH);
		bldr.append("=");
		bldr.append(joinTableFKPath.format());
		return bldr.toString();
	}

	public int getJoinTableParentPKPosition()
	{
		return this.joinTableParentPKPosition;
	}

	public void setJoinTableParentPKPosition(int joinTableParentPKPosition)
	{
		this.joinTableParentPKPosition = joinTableParentPKPosition;
	}

	public Path getJoinTableFKPath()
	{
		return this.joinTableFKPath;
	}

	public void setJoinTableFKPath(Path joinTableFKPath)
	{
		this.joinTableFKPath = joinTableFKPath;
	}

	public String getSelectLabel()
	{
		return this.selectLabel;
	}

	public void setSelectLabel(String selectLabel)
	{
		this.selectLabel = selectLabel;
	}

	public String getSelectionServiceName()
	{
		return this.selectionServiceName;
	}

	public void setSelectionServiceName(String selectionServiceName)
	{
		this.selectionServiceName = selectionServiceName;
	}
}
