package com.orchestranetworks.ps.validation.service;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.validation.bean.*;
import com.orchestranetworks.ps.validation.export.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

public class GenerateValidationReport<E extends DatasetEntitySelection>
	extends
	AbstractFileDownloadUserService<E>
{
	/**
	 * Defines the levels at which the report will be generated
	 */
	public enum ValidationReportLevel {
		/**
		 * Always should generate a report for whatever the service is defined on. (i.e. if defined on a table, it's a table validation).
		 * This is the default.
		 */
		DERIVED,

		/** Always generate a data set validation report */
		DATA_SET,

		/** Always generate a table validation report */
		TABLE,

		/** Always generate a record validation report */
		RECORD
	}

	public static final String DEFAULT_EXPORT_DIR_NAME = "validation";

	private static final String CSV_SEPARATOR = ",";
	private static final String DEFAULT_PERMISSIONS_TEMPLATE_DATA_SPACE_NAME = "ValidationDataSpacePermissions";

	private ValidationReportLevel validationReportLevel = ValidationReportLevel.DERIVED;
	protected String exportParentDirName;
	protected String exportDirName;
	protected boolean appendTimestamp;
	protected boolean includePK;
	protected Severity minSeverity;
	protected boolean useChildDataSpace;
	protected String permissionsTemplateDataSpaceName = DEFAULT_PERMISSIONS_TEMPLATE_DATA_SPACE_NAME;
	private File report;

	public GenerateValidationReport()
	{
		this(DEFAULT_EXPORT_DIR_NAME, false, false, Severity.ERROR, false);
	}

	public GenerateValidationReport(
		String exportDirName,
		boolean appendTimestamp,
		boolean includePK,
		Severity minSeverity,
		boolean useChildDataSpace)
	{
		try
		{
			this.exportParentDirName = CommonConstants.getEBXHome();
		}
		catch (IOException ex)
		{
			LoggingCategory.getKernel().error("Error looking up EBX Home.", ex);
		}
		this.exportDirName = exportDirName;
		this.appendTimestamp = appendTimestamp;
		this.includePK = includePK;
		this.minSeverity = minSeverity;
		this.useChildDataSpace = useChildDataSpace;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		final ValidationReport validationReport;
		try
		{
			validationReport = generateValidationReport(session);
		}
		catch (OperationException e)
		{
			LoggingCategory.getKernel().error("Error occurred generating validation report.", e);
			throw OperationException.createError(e);
		}

		try
		{
			processValidationReport(session, validationReport);
		}
		catch (final IOException e)
		{
			LoggingCategory.getKernel().error("Error occurred processing validation report.", e);
			throw OperationException.createError(e);
		}
	}

	/**
	 * Allow this to be overridden so that subclasses can delegate to another data set.
	 * Note that when generating a table-level validation report, this means it will look up
	 * the table in this data set instead of the table that the service is on.
	 * When generating a record-level validation report, this will be ignored.
	 */
	public Adaptation getDataSet()
	{
		return context.getEntitySelection().getDataset();
	}

	/**
	 * Allow this to be overridden so that subclasses can delegate to another table.
	 * When generating a record-level validation report, this will be ignored.
	 */
	public AdaptationTable getTable(Adaptation dataSet)
	{
		AdaptationTable table = ((TableEntitySelection) context.getEntitySelection()).getTable();
		return dataSet.getTable(table.getTablePath());
	}

	protected ValidationReport generateValidationReport(Session session) throws OperationException
	{
		ValidationReport validationReport;
		Adaptation dataSet = getDataSet();
		AdaptationHome childDataSpace = null;
		try
		{
			if (useChildDataSpace)
			{
				childDataSpace = createChildDataSpace(session, dataSet.getHome());
				dataSet = childDataSpace.findAdaptationOrNull(dataSet.getAdaptationName());
			}
			validationReport = getValidationReport(dataSet);
		}
		finally
		{
			if (useChildDataSpace && childDataSpace != null)
			{
				closeChildDataSpace(session, childDataSpace);
			}
		}
		return validationReport;
	}

	protected ValidationReport getValidationReport(Adaptation dataSet)
	{
		E selection = context.getEntitySelection();
		if (validationReportLevel == ValidationReportLevel.RECORD
			|| (validationReportLevel == ValidationReportLevel.DERIVED
				&& selection instanceof RecordEntitySelection))
		{
			Adaptation record = ((RecordEntitySelection) selection).getRecord();
			return record.getValidationReport();
		}
		if (validationReportLevel == ValidationReportLevel.TABLE
			|| (validationReportLevel == ValidationReportLevel.DERIVED
				&& selection instanceof TableViewEntitySelection))
		{
			AdaptationTable table = getTable(dataSet);
			return table.getValidationReport();
		}
		return dataSet.getValidationReport();
	}

	protected AdaptationHome createChildDataSpace(Session session, AdaptationHome dataSpace)
		throws OperationException
	{
		return HomeUtils.createChildDataSpace(
			session,
			dataSpace,
			"Validation Report for ",
			permissionsTemplateDataSpaceName);
	}

	protected void closeChildDataSpace(Session session, AdaptationHome childDataSpace)
		throws OperationException
	{
		HomeUtils.closeDataSpace(session, childDataSpace);
	}

	protected void processValidationReport(Session session, ValidationReport validationReport)
		throws IOException
	{
		if (validationReport.hasItemsOfSeverityOrMore(minSeverity))
		{
			List<ValidationErrorElement> list = getValidationErrorList(validationReport);
			generateUI(list);
		}
		else
		{
			alert("No Validation Messages");
		}
	}

	protected List<ValidationErrorElement> getValidationErrorList(ValidationReport validationReport)
	{
		final ValidationReportItemIterator itemsOfSeverity = validationReport
			.getItemsOfSeverityOrMore(minSeverity);
		Locale locale = context.getWriter().getLocale();

		final ArrayList<ValidationErrorElement> list = new ArrayList<>();
		while (itemsOfSeverity.hasNext())
		{
			final ValidationReportItem nextItem = itemsOfSeverity.nextItem();
			if ((nextItem.getSubject() != null))
			{
				ValidationErrorElement element = new ValidationErrorElement();
				if (nextItem.getSubjectForAdaptation() != null)
				{
					final Adaptation occurence = nextItem.getSubjectForAdaptation().getAdaptation();
					final UserMessage message = nextItem.getMessage();
					final String formatMessage = message.formatMessage(locale);
					element.setMessage(
						formatMessage + " ["
							+ nextItem.getSubjectForAdaptation().getPathInAdaptation().format()
							+ "]");
					element.setSeverity(nextItem.getSeverity().getLabel());
					element.setRecord(occurence);
					list.add(element);
				}
				else if (nextItem.getSubjectForTable() != null)
				{
					RequestResult results = nextItem.getSubjectForTable().getRecords();
					try
					{
						for (Adaptation adaptation; (adaptation = results
							.nextAdaptation()) != null;)
						{
								final UserMessage message = nextItem.getMessage();
								final String formatMessage = message.formatMessage(locale);
								element.setMessage(formatMessage);
								element.setSeverity(nextItem.getSeverity().getLabel());
								element.setRecord(adaptation);
								list.add(element);
								element = new ValidationErrorElement();
						}
					}
					finally
					{
						results.close();
						itemsOfSeverity.close();
					}

				}
			}
		}
		return list;
	}

	protected void generateUI(List<ValidationErrorElement> list) throws IOException
	{
		// If it's null, then don't do the export at all
		if (exportParentDirName != null)
		{
			String filePath = generateFile(list);
			report = new File(filePath);
		}
	}

	public static Map<AdaptationTable, String> getTableNames(
		List<ValidationErrorElement> list,
		Session session)
	{
		Set<AdaptationTable> tables = new LinkedHashSet<>();
		for (ValidationErrorElement element : list)
		{
			Adaptation record = element.getRecord();
			if (record.isTableOccurrence())
				tables.add(record.getContainerTable());
		}
		return AdaptationUtil.deriveTableNames(tables, " - ", session.getLocale());
	}

	protected String generateFile(List<ValidationErrorElement> list) throws IOException
	{
		CSVExporter exporter = new CSVExporter(
			exportParentDirName,
			exportDirName,
			CSV_SEPARATOR,
			context.getWriter().getLocale(),
			CSVExporter.DEFAULT_EXPORT_FILE_NAME,
			CSVExporter.DEFAULT_EXPORT_FILE_EXTENSION,
			appendTimestamp,
			includePK);
		exporter.setValidationList(list);
		exporter.setTableNames(getTableNames(list, context.getSession()));
		return exporter.doExport();
	}

	public String getExportParentDirName()
	{
		return this.exportParentDirName;
	}

	public void setExportParentDirName(String exportParentDirName)
	{
		this.exportParentDirName = exportParentDirName;
	}

	public String getExportDirName()
	{
		return this.exportDirName;
	}

	public void setExportDirName(String exportDirName)
	{
		this.exportDirName = exportDirName;
	}

	public boolean isAppendTimestamp()
	{
		return this.appendTimestamp;
	}

	public void setAppendTimestamp(boolean appendTimestamp)
	{
		this.appendTimestamp = appendTimestamp;
	}

	public boolean isIncludePK()
	{
		return this.includePK;
	}

	public void setIncludePK(boolean includePK)
	{
		this.includePK = includePK;
	}

	public Severity getMinSeverity()
	{
		return this.minSeverity;
	}

	public void setMinSeverity(Severity minSeverity)
	{
		this.minSeverity = minSeverity;
	}

	public boolean isUseChildDataSpace()
	{
		return this.useChildDataSpace;
	}

	public void setUseChildDataSpace(boolean useChildDataSpace)
	{
		this.useChildDataSpace = useChildDataSpace;
	}

	public String getPermissionsTemplateDataSpaceName()
	{
		return this.permissionsTemplateDataSpaceName;
	}

	public void setPermissionsTemplateDataSpace(String permissionsTemplateDataSpaceName)
	{
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}

	@Override
	public List<File> getFiles()
	{
		if (report != null)
			return Collections.singletonList(report);
		return null;
	}

	public File getReport()
	{
		return report;
	}

	public ValidationReportLevel getValidationReportLevel()
	{
		return validationReportLevel;
	}

	public void setValidationReportLevel(ValidationReportLevel validationReportLevel)
	{
		this.validationReportLevel = validationReportLevel;
	}
}
