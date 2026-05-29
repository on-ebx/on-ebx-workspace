/*
 * Copyright Orchestra Networks 2000-2011. All rights reserved.
 */
package com.orchestranetworks.ps.export;

import java.io.*;
import java.text.*;
import java.util.*;

public abstract class FileExporter
{
	private static final String FILE_NAME_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

	protected final String exportFilePath;
	protected final Locale locale;

	public FileExporter(
		final String parentDir,
		final String exportDir,
		final Locale locale,
		final String exportFileName,
		final String exportFileExtension,
		final boolean appendTimestamp) throws IOException
	{
		/*
		 * Directory to export the Report.
		 */
		final String exportDirPath = this.createExportDir(parentDir, exportDir);

		String filenameWithExt = buildFilenameWithExt(
			exportFileName,
			exportFileExtension,
			appendTimestamp);
		final File exportFile = new File(exportDirPath, filenameWithExt);
		exportFile.createNewFile();
		this.exportFilePath = exportFile.getAbsolutePath();
		this.locale = locale;
	}

	public abstract String doExport() throws IOException;

	private static String buildFilenameWithExt(
		final String exportFileName,
		final String exportFileExtension,
		final boolean appendTimestamp)
	{
		StringBuilder filenameBldr = new StringBuilder(exportFileName);
		if (appendTimestamp)
		{
			final SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_TIMESTAMP_FORMAT);
			filenameBldr.append('_');
			filenameBldr.append(dateFormat.format(new Date()));
		}
		filenameBldr.append(exportFileExtension);
		return filenameBldr.toString();
	}

	private String createExportDir(final String parentDir, final String exportDir)
		throws IOException
	{
		StringBuilder dirPathBldr = new StringBuilder(parentDir);
		if (exportDir != null)
		{
			if (!parentDir.endsWith("/"))
			{
				dirPathBldr.append("/");
			}
			dirPathBldr.append(exportDir);
		}
		final File dir = new File(dirPathBldr.toString());
		if (!dir.exists())
		{
			if (!dir.mkdirs())
			{
				throw new IOException("Error creating directories.");
			}
		}
		return dir.getAbsolutePath();
	}
}
