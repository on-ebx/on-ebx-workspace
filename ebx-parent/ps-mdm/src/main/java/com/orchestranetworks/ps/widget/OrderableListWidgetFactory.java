package com.orchestranetworks.ps.widget;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.form.widget.*;

public class OrderableListWidgetFactory<T extends UICustomWidget>
	extends
	BaseUICustomWidgetFactory<T>
{
	private Path sortOrderPath;

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(WidgetFactoryContext context)
	{
		if (listWidget)
		{
			return (T) new OrderableListUIListCustomWidget(context);
		}
		return (T) new OrderableListUISimpleCustomWidget(context);
	}

	@Override
	public void setup(WidgetFactorySetupContext context)
	{
		if (sortOrderPath == null)
		{
			context.addError("sortOrderPath must be specified.");
		}
	}

	protected AdaptationFilter getFilter(ValueContext valueContext)
	{
		return null;
	}

	protected void writeWidget(WidgetWriter writer, WidgetDisplayContext context)
	{
		UIComboBox comboBox = writer.newComboBox(Path.SELF);

		ValueContext valueContext = context.getValueContext();
		AdaptationTable foreignTable = context.getNode().getFacetOnTableReference().getTable(
			valueContext);
		AdaptationFilter filter = getFilter(valueContext);
		comboBox.setAjaxPrevalidationEnabled(true);
		comboBox
			.setSpecificNomenclature(createNomenclature(foreignTable, filter, writer.getLocale()));
		writer.addWidget(comboBox);
	}

	private class OrderableListUISimpleCustomWidget extends BaseUISimpleCustomWidget
	{
		public OrderableListUISimpleCustomWidget(WidgetFactoryContext context)
		{
			super(context, OrderableListWidgetFactory.this);
		}

		@Override
		protected void addForEdit(WidgetWriter writer, WidgetDisplayContext context)
		{
			writeWidget(writer, context);
		}
	}

	private class OrderableListUIListCustomWidget extends BaseUIListCustomWidget
	{
		public OrderableListUIListCustomWidget(WidgetFactoryContext context)
		{
			super(context, OrderableListWidgetFactory.this);
		}

		@Override
		protected void addForEdit(WidgetWriterForList writer, WidgetDisplayContext context)
		{
			writeWidget(writer, context);
		}
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

	public String getSortOrderPath()
	{
		return this.sortOrderPath.format();
	}

	public void setSortOrderPath(String sortOrderPath)
	{
		this.sortOrderPath = Path.parse(sortOrderPath);
	}
}
