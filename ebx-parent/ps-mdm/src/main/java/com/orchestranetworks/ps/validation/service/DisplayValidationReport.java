package com.orchestranetworks.ps.validation.service;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.validation.bean.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.selection.DatasetEntitySelection;

public class DisplayValidationReport extends GenerateValidationReport<DatasetEntitySelection>
{
	private static final String TABLE_STYLE = "border:1px solid lightgrey;border-collapse:collapse;";

	private static final String DIV_STYLE = "padding-top:20px; padding-left:20px;height:90%;";

	private static final String OCCURRENCE_STYLE = "padding:5px;" + "height:20px;"
		+ "border:1px solid lightgrey;"
		+ "text-align:center;" + "vertical-align:center;" + "white-space:nowrap;"
		+ "cursor:pointer;";

	public DisplayValidationReport()
	{
		super();
	}

	public DisplayValidationReport(
		String exportDirName,
		boolean appendTimestamp,
		boolean includePK,
		Severity minSeverity)
	{
		super(exportDirName, appendTimestamp, includePK, minSeverity, false);
	}

	@Override
	protected void generateUI(List<ValidationErrorElement> list) throws IOException
	{
		final UIComponentWriter uiComponentWriter = context.getWriter();
		uiComponentWriter.add_cr("<style type=\"text/css\">   ");
		uiComponentWriter.add_cr("    .pg-normal { ");
		uiComponentWriter.add_cr("        color: black;");
		uiComponentWriter.add_cr("         font-weight: normal;");
		uiComponentWriter.add_cr("        text-decoration: none;");
		uiComponentWriter.add_cr("        cursor: pointer;");
		uiComponentWriter.add_cr("    }");
		uiComponentWriter.add_cr("    .pg-selected {");
		uiComponentWriter.add_cr("        color: black;");
		uiComponentWriter.add_cr("        font-weight: bold;");
		uiComponentWriter.add_cr("        text-decoration: underline;");
		uiComponentWriter.add_cr("        cursor: pointer;");
		uiComponentWriter.add_cr("    }");
		uiComponentWriter.add_cr("</style>");

		uiComponentWriter.add_cr("<div style=\"" + DIV_STYLE + "\">");

		uiComponentWriter.add_cr("<script type=\"text/javascript\">");
		uiComponentWriter.add_cr("function Pager(tableName, itemsPerPage) {");
		uiComponentWriter.add_cr("    this.tableName = tableName;");
		uiComponentWriter.add_cr("    this.itemsPerPage = itemsPerPage;");
		uiComponentWriter.add_cr("    this.currentPage = 1;");
		uiComponentWriter.add_cr("    this.pages = 0;");
		uiComponentWriter.add_cr("    this.inited = false;");

		uiComponentWriter.add_cr("    this.showRecords = function(from, to) {");
		uiComponentWriter.add_cr("        var rows = document.getElementById(tableName).rows;");
		// i starts from 1 to skip table header row
		uiComponentWriter.add_cr("        for (var i = 1; i < rows.length; i++) {");
		uiComponentWriter.add_cr("            if (i < from || i > to)");
		uiComponentWriter.add_cr("                rows[i].style.display = 'none';");
		uiComponentWriter.add_cr("            else");
		uiComponentWriter.add_cr("                rows[i].style.display = '';");
		uiComponentWriter.add_cr("        }");
		uiComponentWriter.add_cr("    }");

		uiComponentWriter.add_cr("    this.showPage = function(pageNumber) {");
		uiComponentWriter.add_cr("     if (! this.inited) {");
		uiComponentWriter.add_cr("      alert(\"not inited\");");
		uiComponentWriter.add_cr("      return;");
		uiComponentWriter.add_cr("     }");

		uiComponentWriter
			.add_cr("        var oldPageAnchor = document.getElementById('pg'+this.currentPage);");
		uiComponentWriter.add_cr("        oldPageAnchor.className = 'pg-normal';");

		uiComponentWriter.add_cr("        this.currentPage = pageNumber;");
		uiComponentWriter
			.add_cr("        var newPageAnchor = document.getElementById('pg'+this.currentPage);");
		uiComponentWriter.add_cr("        newPageAnchor.className = 'pg-selected';");

		uiComponentWriter.add_cr("        var from = (pageNumber - 1) * itemsPerPage + 1;");
		uiComponentWriter.add_cr("        var to = from + itemsPerPage - 1;");
		uiComponentWriter.add_cr("        this.showRecords(from, to);");
		uiComponentWriter.add_cr("    }");

		uiComponentWriter.add_cr("    this.prev = function() {");
		uiComponentWriter.add_cr("        if (this.currentPage > 1)");
		uiComponentWriter.add_cr("            this.showPage(this.currentPage - 1);");
		uiComponentWriter.add_cr("    }");

		uiComponentWriter.add_cr("    this.next = function() {");
		uiComponentWriter.add_cr("        if (this.currentPage < this.pages) {");
		uiComponentWriter.add_cr("            this.showPage(this.currentPage + 1);");
		uiComponentWriter.add_cr("        }");
		uiComponentWriter.add_cr("    }");

		uiComponentWriter.add_cr("this.init = function() {");
		uiComponentWriter.add_cr("        var rows = document.getElementById(tableName).rows;");
		uiComponentWriter.add_cr("        var records = (rows.length - 1);");
		uiComponentWriter.add_cr("        this.pages = Math.ceil(records / itemsPerPage);");
		uiComponentWriter.add_cr("        this.inited = true;");
		uiComponentWriter.add_cr("    }");

		uiComponentWriter.add_cr("   this.showPageNav = function(pagerName, positionId) {");
		uiComponentWriter.add_cr("     if (! this.inited) {");
		uiComponentWriter.add_cr("      alert(\"not inited\");");
		uiComponentWriter.add_cr("      return;");
		uiComponentWriter.add_cr("     }");
		uiComponentWriter.add_cr("     var element = document.getElementById(positionId);");

		uiComponentWriter.add_cr(
			"    var pagerHtml = '<span onclick=\"' + pagerName + '.prev();\" class=\"pg-normal\"> &#171 Prev </span> | ';");
		uiComponentWriter.add_cr("        for (var page = 1; page <= this.pages; page++)");
		uiComponentWriter.add_cr(
			"            pagerHtml += '<span id=\"pg' + page + '\" class=\"pg-normal\" onclick=\"' + pagerName + '.showPage(' + page + ');\">' + page + '</span> | ';");
		uiComponentWriter.add_cr(
			"        pagerHtml += '<span onclick=\"'+pagerName+'.next();\" class=\"pg-normal\"> Next &#187;</span>'; ");

		uiComponentWriter.add_cr("        element.innerHTML = pagerHtml;");
		uiComponentWriter.add_cr("    }");
		uiComponentWriter.add_cr("}");
		uiComponentWriter.add_cr("</script>");

		uiComponentWriter
			.add_cr("<table id=\"results\" class=\"text\" style=\"" + TABLE_STYLE + "\">");

		uiComponentWriter.add_cr("<tr style=\"background-color:#FFFBE6;\">");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Table</td>");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Record</td>");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Message</td>");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Last Modified User</td>");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Last Mofified Time</td>");
		uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">Link</td>");
		uiComponentWriter.add_cr("</tr>");

		Map<AdaptationTable, String> tableNames = getTableNames(list, context.getSession());
		for (int i = 0; i < list.size(); i++)
		{
			uiComponentWriter.add_cr(
				"<tr style=\"padding:5px;height:20px;border:1px solid lightgrey;text-align:center;vertical-align:center;white-space:nowrap;cursor:pointer;\">");

			final ValidationErrorElement validationErrorElement = list.get(i);

			final UIHttpManagerComponent managerComponent = uiComponentWriter
				.createWebComponentForSubSession();
			managerComponent.selectInstanceOrOccurrence(validationErrorElement.getRecord());
			final String url = managerComponent.getURIWithParameters();

			final UIButtonSpecJSAction buttonCreateOrganisation = uiComponentWriter
				.buildButtonPreview(
					url,
					UserMessage.createInfo("Link"));
			buttonCreateOrganisation.setButtonLayout(UIButtonLayout.TEXT_ONLY);

			if (validationErrorElement.getRecord().isTableOccurrence())
			{
				uiComponentWriter
					.add_cr(
						"<td style=\"" + OCCURRENCE_STYLE + "\">"
							+ tableNames.get(
								validationErrorElement.getRecord()
									.getContainerTable()));
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord().getLabel(Locale.US));
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">" + validationErrorElement.getMessage()
						+ "</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord().getLastUser().getUserId() + "</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord().getTimeOfLastModification().toString()
						+ "</td>");
				uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">");
				uiComponentWriter.addButtonJavaScript(buttonCreateOrganisation);
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr("</tr>");
			}
			else
			{
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord()
							.getLabelOrName(uiComponentWriter.getLocale()));
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\"> Data Set");
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">" + validationErrorElement.getMessage()
						+ "</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord().getLastUser().getUserId() + "</td>");
				uiComponentWriter.add_cr(
					"<td style=\"" + OCCURRENCE_STYLE + "\">"
						+ validationErrorElement.getRecord().getTimeOfLastModification().toString()
						+ "</td>");
				uiComponentWriter.add_cr("<td style=\"" + OCCURRENCE_STYLE + "\">");
				uiComponentWriter.addButtonJavaScript(buttonCreateOrganisation);
				uiComponentWriter.add_cr("</td>");
				uiComponentWriter.add_cr("</tr>");
			}

		}
		uiComponentWriter.add_cr("</table>");

		uiComponentWriter.add_cr("<br>");
		uiComponentWriter.add_cr("<br>");
		uiComponentWriter.add_cr("<br>");

		uiComponentWriter.add_cr("<div id=\"pageNavPosition\"></div>");

		uiComponentWriter.add_cr("				<script type=\"text/javascript\">");
		uiComponentWriter.add_cr("       var pager = new Pager('results', 10);");
		uiComponentWriter.add_cr("    pager.init();");
		uiComponentWriter.add_cr("       pager.showPageNav('pager', 'pageNavPosition');");
		uiComponentWriter.add_cr("        pager.showPage(1);");
		uiComponentWriter.add_cr("    </script>");

		uiComponentWriter.add_cr("</div>");

		super.generateUI(list);
	}
}
