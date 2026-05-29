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
public class MultiSelectCustomWidget extends UIListCustomWidget
{
	private final MultiSelectWidgetFactory factory;
	public MultiSelectCustomWidget(MultiSelectWidgetFactory factory, WidgetFactoryContext aContext)
	{
		super(aContext);
		this.factory = factory;
	}

	@SuppressWarnings("deprecation")
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
		bldr.append(factory.getJoinTableParentPKPosition());
		bldr.append("&");
		bldr.append(MultiSelectRecordSelectionService.PARAM_JOIN_TABLE_FK_PATH);
		bldr.append("=");
		bldr.append(factory.getJoinTableFKPath().format());
		return bldr.toString();
	}

	@Override
	public void write(WidgetWriterForList aWriter, WidgetDisplayContext aContext)
	{
		if (aContext.isDisplayedInTable())
		{
			UIWidget widget = aWriter.newBestMatching(Path.SELF);
			widget.setEditorDisabled(true);
			aWriter.addWidget(widget);
		}
		else
		{
			boolean allowWithoutCreate = factory.isAllowWithoutCreate();
			if (!allowWithoutCreate)
			{
				aWriter.addWidget(Path.SELF);
			}

			Session session = aWriter.getSession();
			boolean allowOutsideWorkflow = factory.isAllowOutsideWorkflow();
			if (session.getInteraction(true) == null && !allowOutsideWorkflow)
			{
				return;
			}
			ValueContext vc = aContext.getValueContext();
			AdaptationTable table = vc.getAdaptationTable();
			PrimaryKey pk = table.computePrimaryKey(vc);
			if (aContext.isCreatingRecord())
			{
				return;
			}
			Adaptation record = AdaptationUtil.getRecordForValueContext(vc);

			// TODO: We're getting the request result just to get the link table.
			//       Probably is a better way to do it that doesn't execute the query.
			AdaptationTable joinTable;
			RequestResult reqRes = AdaptationUtil.linkedRecordLookup(
				record,
				Path.SELF.add(aContext.getNode().getPathInAdaptation()));
			try
			{
				joinTable = reqRes.getTable();
			}
			finally
			{
				reqRes.close();
			}

			if (allowWithoutCreate
				|| PermissionsUtil.isUserPermittedToCreateRecord(aWriter.getSession(), joinTable))
			{
				if (!allowWithoutCreate)
				{
					aWriter.add(
						"<label style='margin-left:30px; font-style:normal; color:black'>"
							+ factory.getSelectLabel() + "</label>");
				}
				UIHttpManagerComponent uiMgr = aWriter.createWebComponentForSubSession();
				uiMgr.selectNode(joinTable.getContainerAdaptation(), joinTable.getTablePath());
				uiMgr.setTrackingInfo(session.getTrackingInfo());
				uiMgr.setService(ServiceKey.forName(factory.getSelectionServiceName()));
				String url = createURL(uiMgr.getURIWithParameters(), table, pk);
				UIButtonSpecJSAction buttonSpec = aWriter.buildButtonPreview(url);
				buttonSpec.setButtonLayout(UIButtonLayout.ICON_ONLY);
				buttonSpec.setRelief(UIButtonRelief.FLAT);
				aWriter.addButtonJavaScript(buttonSpec);
			}
		}
	}
}
