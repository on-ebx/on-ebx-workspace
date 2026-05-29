package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ui.form.widget.*;

/**
 * This widget will expand the cell height for a text field (or long string) so that user can scroll
 * to see the entire value inline.
 * -- The cell height will default to a cell height of 4 which provides 3 completely visible lines plus a bit extra.
 *       but can be set to different value by specifying the cellHeight parameter in the EBX Data Model UI if desired
 * -- The cell width will default to 100% of the EBX generated width, based on the size of the column heading label
 *       but can be set to different value by specifying the cellWidthInPixels parameter in the EBX Data Model UI if desired
 * 
 */
public class TableCellTextScrollWidget extends BaseUISimpleCustomWidget
{

	public TableCellTextScrollWidget(WidgetFactoryContext context, Factory factory)
	{
		super(context, factory);
	}

	@Override
	public void write(WidgetWriter writer, WidgetDisplayContext context)
	{
		String value = (String) context.getValueContext().getValue();
		if (context.isDisplayedInTable() && value != null)
		{
			int cellHeight = ((Factory) this.factory).getCellHeight();
			if (cellHeight < 1)
			{
				cellHeight = 4;
			}
			String cellWidthString = "100%";
			int cellWidthInPixels = ((Factory) this.factory).getCellWidthInPixels();
			if (cellWidthInPixels > 0)
			{
				cellWidthString = cellWidthInPixels + "px";
			}
			writer.add("<p");
			writer.addSafeAttribute(
				"style",
				"height: " + cellHeight + "em; width: " + cellWidthString
					+ "; overflow: auto; white-space: normal;");
			writer.add(">");
			writer.addSafeInnerHTML(value);
			writer.add("</p>");
		}
		else
		{
			super.write(writer, context);
		}
	}

	public static class Factory extends BaseUICustomWidgetFactory<TableCellTextScrollWidget>
	{
		private int cellHeight = 4;
		private Integer cellWidthInPixels = 0;

		@Override
		public TableCellTextScrollWidget newInstance(WidgetFactoryContext context)
		{
			return new TableCellTextScrollWidget(context, this);
		}
		public int getCellHeight()
		{
			return cellHeight;
		}
		public void setCellHeight(int cellHeight)
		{
			this.cellHeight = cellHeight;
		}
		public int getCellWidthInPixels()
		{
			return cellWidthInPixels;
		}
		public void setCellWidthInPixels(int cellWidthInPixels)
		{
			this.cellWidthInPixels = cellWidthInPixels;
		}
	}
}
