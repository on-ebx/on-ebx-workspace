/*
 * Copyright Orchestra Networks 2000-2011. All rights reserved.
 */
package com.orchestranetworks.ps.validation.export;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.export.*;
import com.orchestranetworks.ps.validation.bean.*;

public class CSVExporter extends FileExporter
{
	public static final String DEFAULT_EXPORT_FILE_NAME = "ValidationReport";
	public static final String DEFAULT_EXPORT_FILE_EXTENSION = ".csv";

	private List<ValidationErrorElement> errorList;
	private Map<AdaptationTable, String> tableNames;
	private final String separator;
	private final boolean includePK;

	public CSVExporter(
		final String parentDir,
		final String exportDir,
		final String separator,
		final Locale locale)
		throws IOException
	{
		this(
			parentDir,
			exportDir,
			separator,
			locale,
			DEFAULT_EXPORT_FILE_NAME,
			DEFAULT_EXPORT_FILE_EXTENSION,
			false,
			false);
	}

	public CSVExporter(
		final String parentDir,
		final String exportDir,
		final String separator,
		final Locale locale,
		final String exportFileName,
		final String exportFileExtension,
		final boolean appendTimestamp,
		final boolean includePK)
		throws IOException
	{
		super(parentDir, exportDir, locale, exportFileName, exportFileExtension, appendTimestamp);
		this.separator = separator;
		this.includePK = includePK;
	}

	public void setValidationList(List<ValidationErrorElement> errorList)
	{
		this.errorList = errorList;
	}

	@Override
	public String doExport() throws IOException
	{
		List<ValidationErrorElement> list = this.errorList;
		final BufferedWriter writer = new BufferedWriter(new FileWriter(this.exportFilePath));
		this.writeHeader(writer);

		for (ValidationErrorElement validationErrorElement : list)
		{
			final Adaptation record = validationErrorElement.getRecord();
			if (record.isTableOccurrence())
			{
				String tableName = tableNames.get(record.getContainerTable());
				write(writer, tableName);
				writer.write(separator);
				write(writer, record.getLabel(this.locale));
				writer.write(separator);
				if (includePK)
				{
					write(writer, record.getOccurrencePrimaryKey().format());
					writer.write(separator);
				}
				write(writer, validationErrorElement.getMessage());
				writer.write(separator);
				writer.write(validationErrorElement.getSeverity());
				writer.write(separator);
				writer.write(record.getLastUser().getUserId());
				writer.write(separator);
				writer.write(record.getTimeOfLastModification().toString());
				writer.newLine();
				writer.flush();
			}
			else
			{
				writer.write("Data Set");
				writer.write(separator);
				write(writer, record.getLabelOrName(this.locale));
				writer.write(separator);
				write(writer, validationErrorElement.getMessage());
				writer.newLine();
				writer.flush();
			}

		}
		writer.flush();
		writer.newLine();

		writer.close();
		return this.exportFilePath;
	}

	private static void write(final Writer writer, String string) throws IOException
	{
		if (string.contains(",") || string.contains("\n"))
			writer.write("\"" + string + "\"");
		else
			writer.write(string);
	}

	private void writeHeader(final BufferedWriter writer) throws IOException
	{
		writer.write("Table");
		writer.write(separator);
		writer.write("Record");
		writer.write(separator);
		if (includePK)
		{
			writer.write("Key");
			writer.write(separator);
		}
		writer.write("Message");
		writer.write(separator);
		writer.write("Severity");
		writer.write(separator);
		writer.write("Last Modified User");
		writer.write(separator);
		writer.write("Last Modified Time");
		writer.flush();
		writer.newLine();
	}

	public void setTableNames(Map<AdaptationTable, String> tableNames)
	{
		this.tableNames = tableNames;
	}

}
