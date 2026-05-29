package com.orchestranetworks.ps.codegen;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * This service implementation is meant to act on a data set.  For each table in the data set,
 * it will generate a java bean class for that table.
 * @see JavaBeanExporter
 */
public class GenerateBeansForTables extends AbstractUserService<DatasetEntitySelection>
{
	public static final String DEFAULT_EXPORT_DIR_NAME = "beanExport";
	private final String exportParentDir;
	private final String exportDir;
	private final boolean appendTimestamp;

	public GenerateBeansForTables(String parentDir, String exportDir, boolean appendTimestamp)
	{
		this.exportParentDir = parentDir;
		this.exportDir = exportDir;
		this.appendTimestamp = appendTimestamp;
	}

	public void execute(Session session) throws OperationException
	{
		final Adaptation container = context.getEntitySelection().getDataset();
		List<AdaptationTable> tables = AdaptationUtil.getAllTables(container);
		String lastFileName = null;
		for (AdaptationTable table : tables)
		{
			SchemaNode tableNode = table.getTableNode();
			JavaBeanExporter exporter;
			try
			{
				exporter = new JavaBeanExporter(
					exportParentDir,
					exportDir,
					session.getLocale(),
					appendTimestamp,
					tableNode);
				lastFileName = exporter.doExport();
			}
			catch (final IOException e)
			{
				LoggingCategory.getKernel().error("Error occurred generating bean.", e);
				throw OperationException.createError(e);
			}
		}
		StringBuilder message = new StringBuilder();
		if (lastFileName == null)
			message.append("No tables were generated as java beans");
		else
		{
			File file = new File(lastFileName);
			message.append("Java beans generated to directory ").append(file.getParent());
		}
		alert(message.toString());
	}

}
