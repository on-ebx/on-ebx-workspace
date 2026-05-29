package com.orchestranetworks.ps.scheduledtask;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *
 * Scheduled task to export a CSV file
 *
 * The destination file is fileLocation/filePrefix[date{dateFormat}].fileExtension
 *
 * The Default encoding is UTF-8 and the default separator is ','.
 *
 * A command line can be ran after the export.
 *
 * Parameters are :
 *
 * <ul>
 * <li>dataSpace</li>
 * <li>dataSet</li>
 * <li>tablePath</li>
 * <li>fileLocation</li>
 * <li>filePrefix</li>
 * <li>fileExtension</li>
 * <li>dataFormat</li>
 * <li>commandLine</li>
 * <li>encoding</li>
 * <li>fieldSeparator</li>
 * <li>checkAccessRule</li>
 * <li>includeComputedValues</li>
 * <li>includeTechnicalData</li>
 * <li>xpathFilter</li>
 * <li>view</li>
 * </ul>
 * @author MCH
 */
public class ExportCSVScheduledTask extends ScheduledTask
{

	/** The data space. */
	private String dataSpace;

	/** The data set. */
	private String dataSet;

	/** The path to table. */
	private String tablePath;

	/** The file location. */
	private String fileLocation;

	/** The file prefix. */
	private String filePrefix;

	/** The file extension. */
	private String fileExtension;

	/** The date format. */
	private String dateFormat;

	/** The command line. */
	private String commandLine;

	/** The encoding. */
	private String encoding = "UTF-8";

	/** The field separator. */
	private String fieldSeparator = ",";

	/** If false, access rule won't be evaluated, Default is true. */
	private boolean checkAccessRule = true;

	/** If false computed values are not included. Default is true. */
	private boolean includeComputedValues = true;

	/** If true technical data are included. Default is false. */
	private boolean includeTechnicalData = false;

	/** The xpath filter. */
	private String xpathFilter;

	/** The view to apply before exporting. */
	private String view;

	/**
	 * Builds the file name.
	 *
	 * @return the string
	 */
	private String buildFileName()
	{
		String fileName = this.filePrefix;

		Date date = new Date();
		if (this.dateFormat != null)
		{
			SimpleDateFormat format = new SimpleDateFormat(this.dateFormat);
			fileName += format.format(date);
		}

		fileName += "." + this.fileExtension;
		return fileName;
	}

	/*
	 * @see
	 * com.orchestranetworks.scheduler.ScheduledTask#execute(com.orchestranetworks
	 * .scheduler.ScheduledExecutionContext)
	 */
	/*
	 * @see com.orchestranetworks.scheduler.ScheduledTask#execute(com.orchestranetworks.scheduler.
	 * ScheduledExecutionContext)
	 */
	@Override
	public void execute(final ScheduledExecutionContext pContext)
		throws OperationException, ScheduledTaskInterruption
	{
		String fileName = this.buildFileName();

		if (this.fileLocation.endsWith("/"))
		{
			this.fileLocation = this.fileLocation.substring(0, this.fileLocation.length() - 1);
		}

		File folder = new File(this.fileLocation);
		if (!folder.exists() || !folder.isDirectory())
		{
			pContext.setExecutionInformation("Folder '" + this.fileLocation
				+ "' does not exist or is not a repository.");
			return;
		}

		File file = new File(this.fileLocation + "/" + fileName);

		AdaptationHome home = AdaptationUtil.getDataSpaceOrThrowOperationException(
			pContext.getRepository(),
			this.dataSpace);
		AdaptationTable table = AdaptationUtil.getTable(
			home,
			this.dataSet,
			Path.parse(this.tablePath));
		if (table == null)
		{
			pContext.setExecutionInformation("No table at path '" + this.tablePath
				+ " in data set '" + this.dataSet + "' and  data space '" + this.dataSpace + "'.");
			return;
		}

		ProgrammaticService srv = ProgrammaticService.createForSession(pContext.getSession(), home);
		ExportCSVProcedure proc = this.getConfiguredProcedure(file, table);
		ProcedureResult result = srv.execute(proc);

		if (result.hasFailed())
		{
			pContext.addExecutionInformation(result.getExceptionFullMessage(pContext.getSession()
				.getLocale()));
		}
		else
		{
			pContext.addExecutionInformation("File '" + fileName + "' successfully exported.");
		}

		if (StringUtils.isNotEmpty(this.commandLine))
		{
			SchedulerUtils.executeCommandLine(pContext, this.commandLine);
		}
	}

	/**
	 * Gets the command line.
	 *
	 * @return the command line
	 */
	public String getCommandLine()
	{
		return this.commandLine;
	}

	/**
	 * Gets the configured procedure.
	 *
	 * @param file the file
	 * @param table the table
	 * @return the configured procedure
	 */
	private ExportCSVProcedure getConfiguredProcedure(final File file, final AdaptationTable table)
	{
		ExportCSVProcedure proc = new ExportCSVProcedure(file, table);
		if (StringUtils.isNotEmpty(this.encoding))
		{
			proc.setEncoding(this.encoding);
		}
		if (StringUtils.isNotEmpty(this.fieldSeparator))
		{
			proc.setFieldSeparator(this.fieldSeparator);
		}
		proc.setCheckAccessRule(this.checkAccessRule);
		proc.setIncludeComputedValues(this.includeComputedValues);
		proc.setIncludeTechnicalData(this.includeTechnicalData);
		if (StringUtils.isNotEmpty(this.xpathFilter))
		{
			proc.setXpathFilter(this.xpathFilter);
		}
		if (StringUtils.isNotEmpty(this.view))
		{
			proc.setView(this.view);
		}
		return proc;
	}

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 */
	public String getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * Gets the data space.
	 *
	 * @return the data space
	 */
	public String getDataSpace()
	{
		return this.dataSpace;
	}

	/**
	 * Gets the date format.
	 *
	 * @return the date format
	 */
	public String getDateFormat()
	{
		return this.dateFormat;
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
	 * Gets the file extension.
	 *
	 * @return the file extension
	 */
	public String getFileExtension()
	{
		return this.fileExtension;
	}

	/**
	 * Gets the file location.
	 *
	 * @return the file location
	 */
	public String getFileLocation()
	{
		return this.fileLocation;
	}

	/**
	 * Gets the file prefix.
	 *
	 * @return the file prefix
	 */
	public String getFilePrefix()
	{
		return this.filePrefix;
	}

	/**
	 * Gets the path to table.
	 *
	 * @return the path to table
	 */
	public String getPathToTable()
	{
		return this.tablePath;
	}

	/**
	 * Gets the view.
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
	 * Checks if is check access rule.
	 *
	 * @return true, if is check access rule
	 */
	public boolean isCheckAccessRule()
	{
		return this.checkAccessRule;
	}

	/**
	 * Checks if is include computed values.
	 *
	 * @return true, if is include computed values
	 */
	public boolean isIncludeComputedValues()
	{
		return this.includeComputedValues;
	}

	/**
	 * Checks if is include technical data.
	 *
	 * @return true, if is include technical data
	 */
	public boolean isIncludeTechnicalData()
	{
		return this.includeTechnicalData;
	}

	/**
	 *If false, access rule won't be evaluated, Default is true.
	 *
	 * @param checkAccessRule the new check access rule
	 */
	public void setCheckAccessRule(final boolean checkAccessRule)
	{
		this.checkAccessRule = checkAccessRule;
	}

	/**
	 * Sets the command line.
	 *
	 * @param commandLine the new command line
	 */
	public void setCommandLine(final String commandLine)
	{
		this.commandLine = commandLine;
	}

	/**
	 * Sets the data set.
	 *
	 * @param dataSet the new data set
	 */
	public void setDataSet(final String dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * Sets the data space.
	 *
	 * @param dataSpace the new data space
	 */
	public void setDataSpace(final String dataSpace)
	{
		this.dataSpace = dataSpace;
	}

	/**
	 * Sets the date format.
	 *
	 * @param dateFormat the new date format
	 */
	public void setDateFormat(final String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	/**
	 * Sets the encoding.
	 *
	 * @param encoding the new encoding
	 */
	public void setEncoding(final String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * Sets the field separator.
	 *
	 * @param fieldSeparator the new field separator
	 */
	public void setFieldSeparator(final String fieldSeparator)
	{
		this.fieldSeparator = fieldSeparator;
	}

	/**
	 * Sets the file extension.
	 *
	 * @param fileExtension the new file extension
	 */
	public void setFileExtension(final String fileExtension)
	{
		this.fileExtension = fileExtension;
	}

	/**
	 * Sets the file location.
	 *
	 * @param fileLocation the new file location
	 */
	public void setFileLocation(final String fileLocation)
	{
		this.fileLocation = fileLocation;
	}

	/**
	 * Sets the file prefix.
	 *
	 * @param filePrefix the new file prefix
	 */
	public void setFilePrefix(final String filePrefix)
	{
		this.filePrefix = filePrefix;
	}

	/**
	 * If false, computed values are not included, Default is true.
	 *
	 * @param includeComputedValues the new include computed values
	 */
	public void setIncludeComputedValues(final boolean includeComputedValues)
	{
		this.includeComputedValues = includeComputedValues;
	}

	/**
	 *If true, technical data are included, Default is false.
	 *
	 * @param includeTechnicalData the new include technical data
	 */
	public void setIncludeTechnicalData(final boolean includeTechnicalData)
	{
		this.includeTechnicalData = includeTechnicalData;
	}

	/**
	 * Sets the path to table.
	 *
	 * @param pathToTable the new path to table
	 */
	public void setPathToTable(final String pathToTable)
	{
		this.tablePath = pathToTable;
	}

	/**
	 * Sets the view.
	 *
	 * @param view the new view
	 */
	public void setView(final String view)
	{
		this.view = view;
	}

	/**
	 * Sets the xpath filter.
	 *
	 * @param xpathFilter the new xpath filter
	 */
	public void setXpathFilter(final String xpathFilter)
	{
		this.xpathFilter = xpathFilter;
	}
}
