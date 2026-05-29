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
 * Scheduled task to import a CSV file
 * 
 * The source file is fileLocation/filePrefix[date{dateFormat}].fileExtension
 * 
 * The Default encoding is UTF-8 and the default separator is ','.
 * 
 * A command line can be ran after the import.
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
 * </ul>
 * @author MCH
 */
public class ImportCSVScheduledTask extends ScheduledTask
{

	/** The data space. */
	private String dataSpace;

	/** The data set. */
	private String dataSet;

	/** The path to destination table. */
	private String pathToTable;

	/** The file location. */
	private String fileLocation;

	/** The file prefix. */
	private String filePrefix;

	/** The file extension. */
	private String fileExtension;

	/** The date format. */
	private String dateFormat;

	/** The command line to execute after the import. */
	private String commandLine;

	/** The encoding. */
	private String encoding = "UTF-8";

	/** The field separator. */
	private String fieldSeparator = ",";

	/**
	 * Builds the file name.
	 *
	 * @return the string
	 */
	private String buildFileName()
	{
		String fileName = this.filePrefix;

		if (this.dateFormat != null)
		{
			Date date = new Date();
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

		if (!folder.exists() || folder.isDirectory())
		{
			pContext.setExecutionInformation("Folder '" + this.fileLocation
				+ "' does not exist or is not a repository.");
			return;
		}

		File file = new File(this.fileLocation + "/" + fileName);
		if (!file.exists())
		{
			pContext.setExecutionInformation("File '" + fileName + "' not found at location "
				+ this.fileLocation + ".");
			return;
		}

		AdaptationHome home = AdaptationUtil.getDataSpaceOrThrowOperationException(
			pContext.getRepository(),
			this.dataSpace);
		AdaptationTable table = AdaptationUtil.getTable(
			home,
			this.dataSet,
			Path.parse(this.pathToTable));
		if (table == null)
		{
			pContext.setExecutionInformation("No table at path '" + this.pathToTable
				+ " in data set '" + this.dataSet + "' and  data space '" + this.dataSpace + "'.");
			return;
		}

		ProgrammaticService srv = ProgrammaticService.createForSession(pContext.getSession(), home);
		ImportCSVProcedure proc = this.getConfiguredProcedure(file, table);
		ProcedureResult result = srv.execute(proc);

		if (result.hasFailed())
		{
			pContext.addExecutionInformation(result.getExceptionFullMessage(pContext.getSession()
				.getLocale()));
		}
		else
		{
			pContext.addExecutionInformation("File '" + fileName
				+ "' Found and successfully imported.");
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
	private ImportCSVProcedure getConfiguredProcedure(final File file, final AdaptationTable table)
	{
		ImportCSVProcedure proc = new ImportCSVProcedure(file, table);
		if (StringUtils.isNotEmpty(this.encoding))
		{
			proc.setEncoding(this.encoding);
		}
		if (StringUtils.isNotEmpty(this.fieldSeparator))
		{
			proc.setEncoding(this.fieldSeparator);
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
		return this.pathToTable;
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
	 * Sets the path to table.
	 *
	 * @param pathToTable the new path to table
	 */
	public void setPathToTable(final String pathToTable)
	{
		this.pathToTable = pathToTable;
	}
}
