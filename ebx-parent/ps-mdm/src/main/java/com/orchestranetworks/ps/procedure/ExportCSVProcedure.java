package com.orchestranetworks.ps.procedure;

import java.io.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.ExportImportCSVSpec.*;

/**
 * 
 *         Export a CSV file. Defautl separator is "," and default encoding is
 *         UTF-8.
 * @author MCH
 */
public class ExportCSVProcedure implements Procedure
{

	/** The destination file. */
	private final File file;

	/** The table to export. */
	private final AdaptationTable table;

	/** The encoding. */
	private String encoding = "UTF-8";

	/** The field separator. */
	private String fieldSeparator = ",";

	/** If false access rules won't be evaluated */
	private boolean checkAccessRule = true;

	/** If false, computed value won't be exported. */
	private boolean includeComputedValues = true;

	/** If true, technical data are exported. */
	private boolean includeTechnicalData = false;

	/** Filter applied before the export. */
	private String xpathFilter;

	/** Apply a view to the export. */
	private String view;

	/**
	 * Instantiates a new export csv procedure.
	 * 
	 * @param pFile
	 *            the destination file
	 * @param pTable
	 *            the table to export
	 */
	public ExportCSVProcedure(final File pFile, final AdaptationTable pTable)
	{
		this.file = pFile;
		this.table = pTable;
	}

	/*
	 * @see
	 * com.orchestranetworks.service.Procedure#execute(com.orchestranetworks
	 * .service.ProcedureContext)
	 */
	@Override
	public void execute(final ProcedureContext pContext) throws Exception
	{
		ExportSpec spec = new ExportSpec();
		spec.setDestinationFile(this.file);
		spec.setSelection(this.table.getTableNode());
		spec.setCheckAccessRules(this.checkAccessRule);
		spec.setIncludesComputedValues(this.includeComputedValues);
		spec.setIncludesTechnicalData(this.includeTechnicalData);
		spec.setViewPublication(this.view);
		Request request = this.table.createRequest();
		request.setXPathFilter(this.xpathFilter);
		spec.setRequest(request);
		ExportImportCSVSpec csvSpec = new ExportImportCSVSpec();
		csvSpec.setEncoding(this.encoding);
		csvSpec.setFieldSeparator(this.fieldSeparator.charAt(0));
		csvSpec.setHeader(Header.LABEL);
		spec.setCSVSpec(csvSpec);
		pContext.doExport(spec);
	}

	/**
	 * Gets the encoding.
	 * 
	 * @return the encoding
	 */
	public String getEncoding()
	{
		return this.encoding;
	}

	/**
	 * Gets the field separator.
	 * 
	 * @return the field separator
	 */
	public String getFieldSeparator()
	{
		return this.fieldSeparator;
	}

	/**
	 * Gets the destination file.
	 * 
	 * @return the file
	 */
	public File getFile()
	{
		return this.file;
	}

	/**
	 * Gets the table to export.
	 * 
	 * @return the table
	 */
	public AdaptationTable getTable()
	{
		return this.table;
	}

	/**
	 * Gets the applied view.
	 * 
	 * @return the view
	 */
	public String getView()
	{
		return this.view;
	}

	/**
	 * Gets the xpath filter.
	 * 
	 * @return the xpath filter
	 */
	public String getXpathFilter()
	{
		return this.xpathFilter;
	}

	/**
	 * If false access rules won't be evaluated
	 * 
	 * @return true, if access rule are evaluated
	 */
	public boolean isCheckAccessRule()
	{
		return this.checkAccessRule;
	}

	/**
	 * Checks if computed values are included.
	 * 
	 * @return true, if include computed values are included
	 */
	public boolean isIncludeComputedValues()
	{
		return this.includeComputedValues;
	}

	/**
	 * Checks if technical data are included
	 * 
	 * @return true, if technical dataare included
	 */
	public boolean isIncludeTechnicalData()
	{
		return this.includeTechnicalData;
	}

	/**
	 *	If false access rules won't be evaluated. Default is true.
	 * 
	 * @param checkAccessRule
	 *            the new check access rule
	 */
	public void setCheckAccessRule(final boolean checkAccessRule)
	{
		this.checkAccessRule = checkAccessRule;
	}

	/**
	 * Sets the encoding. Default is UTF-8
	 * 
	 * @param encoding
	 *            the new encoding
	 */
	public void setEncoding(final String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Sets the field separator. Default is ','
	 * 
	 * @param fieldSeparator
	 *            the new field separator
	 */
	public void setFieldSeparator(final String fieldSeparator)
	{
		this.fieldSeparator = fieldSeparator;
	}

	/**
	 * If false, computed value won't be exported. Default is true.
	 * 
	 * @param includeComputedValues
	 */
	public void setIncludeComputedValues(final boolean includeComputedValues)
	{
		this.includeComputedValues = includeComputedValues;
	}

	/**
	 * If true, technical data will be exported. Default is false.
	 * 
	 * @param includeTechnicalData
	 *            the new include technical data
	 */
	public void setIncludeTechnicalData(final boolean includeTechnicalData)
	{
		this.includeTechnicalData = includeTechnicalData;
	}

	/**
	 * Sets the view to apply.
	 * 
	 * @param view
	 *            the new view
	 */
	public void setView(final String view)
	{
		this.view = view;
	}

	/**
	 * Sets the xpath filter.
	 * 
	 * @param xpathFilter
	 *            the new xpath filter
	 */
	public void setXpathFilter(final String xpathFilter)
	{
		this.xpathFilter = xpathFilter;
	}

}
