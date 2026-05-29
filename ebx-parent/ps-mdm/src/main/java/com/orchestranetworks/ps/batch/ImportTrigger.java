package com.orchestranetworks.ps.batch;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.schema.definition.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.batch.enums.*;
import com.orchestranetworks.ps.batch.path.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.ExportImportCSVSpec.*;

public class ImportTrigger extends TableTrigger
{

	@Override
	public void setup(final TriggerSetupContext arg0)
	{

	}

	@Override
	public void handleAfterCreate(final AfterCreateOccurrenceContext context)
		throws OperationException
	{

		final Repository repository = context.getAdaptationHome().getRepository();
		final Adaptation adaptation = context.getAdaptationOccurrence();

		// Get all the input values
		final String parentDataSpace = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._DataSpace);
		final String dataSet = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._DataSet);
		final String tablePath = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._TablePath);
		final String filePath = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._FilePath);
		final String headerFormat = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._HeaderFormat);
		final String fieldSeparator = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._FieldSeparator);
		final String fileType = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._FileType);
		final String fileEncoding = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._FileEncoding);
		final String importMode = adaptation
			.getString(BatchImportExportPaths._Root_BatchImport._ImportMode);
		final int commitThreshold = adaptation
			.get_int(BatchImportExportPaths._Root_BatchImport._CommitThreshold);

		// Set the status, start and end date
		HashMap<Path, Object> pathValueMap = new HashMap<>();
		pathValueMap.put(BatchImportExportPaths._Root_BatchImport._StartDate, new Date());

		final AdaptationHome parentAdaptationHome = repository
			.lookupHome(HomeKey.forBranchName(parentDataSpace));

		// Create a child data space - The import of the file will happen in
		// that dataspace
		final HomeCreationSpec homeCreationSpec = new HomeCreationSpec();
		homeCreationSpec.setKey(
			HomeKey.forBranchName(
				adaptation.getOccurrencePrimaryKey().format() + "-"
					+ XsFormats.SINGLETON.formatDateTime(new Date())));
		homeCreationSpec.setOwner(context.getSession().getUserReference());
		homeCreationSpec.setParent(parentAdaptationHome);
		final AdaptationHome workingHome = repository
			.createHome(homeCreationSpec, context.getSession());
		try
		{

			if (workingHome == null)
			{
				throw OperationException.createError("Temporary DataSpace not found");
			}

			final Adaptation dataSetContainer = workingHome
				.findAdaptationOrNull(AdaptationName.forName(dataSet));
			if (dataSetContainer == null)
			{
				throw OperationException.createError("Dataset not found");
			}

			final AdaptationTable targetTable = dataSetContainer.getTable(Path.parse(tablePath));
			final File file = new File(filePath);
			if (!file.exists() || !file.canRead() || !file.isFile())
			{
				throw OperationException
					.createError("Path " + filePath + " is not a readable file");
			}

			// Set the header based on the Header Format field in the WF table
			Header header;

			if (headerFormat.toUpperCase().equals("LABEL"))
			{
				header = Header.LABEL;
			}
			else if (headerFormat.toUpperCase().equals("XPATH"))
			{
				header = Header.PATH_IN_TABLE;
			}
			else
			{
				header = Header.NONE;
			}

			// File import procedure
			final ImportSpec importSpecForCSV = getImportSpecForCSV(
				file,
				targetTable,
				header,
				fieldSeparator,
				fileType,
				fileEncoding,
				importMode);
			final Procedure proc = new Procedure()
			{
				@Override
				public void execute(final ProcedureContext pContext) throws Exception
				{
					pContext.setAllPrivileges(true);
					pContext.setCommitThreshold(commitThreshold);
					pContext.setHistoryActivation(false);

					pContext.doImport(importSpecForCSV);
					pContext.setAllPrivileges(false);
				}
			};

			// File Import execuiton
			final ProgrammaticService srv = ProgrammaticService
				.createForSession(context.getSession(), workingHome);
			ProcedureResult result = srv.execute(proc);
			OperationException resultException = result.getException();
			if (resultException != null)
			{
				throw resultException;
			}
			if (result.hasFailed())
			{
				throw OperationException.createError("File Import execution failed.");
			}

			// Merge of child data space procedure
			final Procedure merge = new Procedure()
			{
				@Override
				public void execute(final ProcedureContext pContext) throws Exception
				{
					pContext.setAllPrivileges(true);
					pContext.doMergeToParent(workingHome);
					pContext.setAllPrivileges(false);
				}
			};

			// Merge of child data space execution
			final ProgrammaticService mergeService = ProgrammaticService
				.createForSession(context.getSession(), parentAdaptationHome);
			result = mergeService.execute(merge);
			resultException = result.getException();
			if (resultException != null)
			{
				throw resultException;
			}
			if (result.hasFailed())
			{
				throw OperationException.createError("Dataspace Merge execution failed.");
			}

			// Execute Optional Post Import Procedure
			executePostImportExtension(context);

			// Set the status to Succeeded if nothing went wrong during import
			pathValueMap.put(
				BatchImportExportPaths._Root_BatchImport._Status,
				BatchImportStatuses.SUCCEEDED);
		}
		catch (final OperationException e)
		{
			// Set the status to Failed if something went wrong during import
			pathValueMap
				.put(BatchImportExportPaths._Root_BatchImport._Status, BatchImportStatuses.FAILED);
			pathValueMap.put(BatchImportExportPaths._Root_BatchImport._Exception, e.getMessage());
			// Closing the working data space
			repository.closeHome(workingHome, context.getSession());
		}

		// Set the child dataspace id
		if (workingHome != null
			&& adaptation.getContainerTable().getTableOccurrenceRootNode().getNode(
				Path.parse("workingDataSpace")) != null)
		{
			pathValueMap.put(
				BatchImportExportPaths._Root_BatchImport._WorkingDataSpace,
				workingHome.getKey().getName());
		}

		// Finalize by setting the end date
		pathValueMap.put(BatchImportExportPaths._Root_BatchImport._EndDate, new Date());
		ModifyValuesProcedure mvp = new ModifyValuesProcedure(adaptation, pathValueMap);
		mvp.setAllPrivileges(true);
		mvp.execute(context.getProcedureContext());
		super.handleAfterCreate(context);
	}

	protected void executePostImportExtension(AfterCreateOccurrenceContext context)
		throws OperationException
	{
		// Override this method if any post import processing is required

	}

	private ImportSpec getImportSpecForCSV(
		final File csvFile,
		final AdaptationTable table,
		final Header headerType,
		final String fieldSeparator,
		final String fileType,
		final String fileEncoding,
		final String importMode)
	{
		final ImportSpec importSpec = new ImportSpec();
		if (fileType == null || !fileType.equals("XML"))
		{
			final ExportImportCSVSpec csvSpec = new ExportImportCSVSpec();
			csvSpec.setHeader(headerType);
			if (fileEncoding != null && !"".equals(fileEncoding))
			{
				csvSpec.setEncoding(fileEncoding);
			}
			else
			{
				// csvSpec.setEncoding("ISO-8859-15");
			}
			if (fieldSeparator != null && !"".equals(String.valueOf(fieldSeparator.charAt(0))))
			{
				csvSpec.setFieldSeparator(fieldSeparator.charAt(0));
			}
			importSpec.setCSVSpec(csvSpec);
		}
		if (importMode != null && !"".equals(importMode))
		{
			// Must be "O" or "U" or "I" or "R"
			importSpec.setImportMode(ImportSpecMode.parse(importMode));
		}
		else
		{
			importSpec.setImportMode(ImportSpecMode.REPLACE);
		}
		importSpec.setSourceFile(csvFile);
		importSpec.setTargetAdaptationTable(table);

		return importSpec;
	}

}
