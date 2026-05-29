/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.uibeaneditor;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * This is an editor with a drop-down that orders itself according to some specified
 * field in the foreign table. It is intended to be used for small sets of data
 * because it utilizes a query against the table, sorting the results, and constructing
 * a Nomenclature from that, which would be expensive with a lot of data.
 * 
 * By default, pulls all values from the foreign table. There's currently no way in API to get the foreign key's predicate
 * so if you want to filter the values, you must implement a subclass that specifies an <code>AdaptationFilter</code>.
 */
@Deprecated
public class OrderableListUIBeanEditor extends UIBeanEditor
{
	private Path sortOrderPath;

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		doAddForDisplay(context);
	}

	@Override
	public void addForDisplayInTable(UIResponseContext context)
	{
		doAddForDisplay(context);
	}

	@Override
	public void addForEdit(UIResponseContext context)
	{
		UIComboBox comboBox = context.newComboBox(Path.SELF);
		ValueContext valueContext = context.getValueContext();
		AdaptationTable foreignTable = context.getNode().getFacetOnTableReference().getTable(
			valueContext);
		AdaptationFilter filter = getFilter(valueContext);
		comboBox.setAjaxPrevalidationEnabled(true);
		comboBox
			.setSpecificNomenclature(createNomenclature(foreignTable, filter, context.getLocale()));
		context.addWidget(comboBox);
	}

	protected AdaptationFilter getFilter(ValueContext valueContext)
	{
		return null;
	}

	private Nomenclature<String> createNomenclature(
		AdaptationTable foreignTable,
		AdaptationFilter filter,
		Locale locale)
	{
		Nomenclature<String> nomenclature = new Nomenclature<>();
		Request request = foreignTable.createRequest();
		if (filter != null)
		{
			request.setSpecificFilter(filter);
		}
		request.setSortCriteria((new RequestSortCriteria()).add(sortOrderPath));
		RequestResult reqRes = request.execute();
		try
		{
			Adaptation record;
			while ((record = reqRes.nextAdaptation()) != null)
			{
				nomenclature.addItemValue(
					record.getOccurrencePrimaryKey().format(),
					record.getLabel(locale));
			}
		}
		finally
		{
			reqRes.close();
		}
		return nomenclature;
	}

	private static void doAddForDisplay(UIResponseContext context)
	{
		UIWidget widget = context.newBestMatching(Path.SELF);
		widget.setEditorDisabled(true);
		context.addWidget(widget);
	}

	public String getSortOrderPath()
	{
		return this.sortOrderPath.format();
	}

	public void setSortOrderPath(String sortOrderPath)
	{
		this.sortOrderPath = Path.parse(sortOrderPath);
	}
}
