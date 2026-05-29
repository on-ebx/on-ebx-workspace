package com.orchestranetworks.ps.admin.devartifacts.impl;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.onwbp.base.text.bean.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.adaptationfilter.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.modifier.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.procedure.Procedures.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.service.extensions.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.PublicationSpec.*;

/**
 * Implementation for importing of the dev artifacts
 */
public class ImportDevArtifactsImpl extends DevArtifactsBase
{
	public static final String PARAM_REPLACE_MODE = "replaceMode";
	public static final String PARAM_SKIP_NONEXISTING_FILES = "skipNonexistingFiles";
	public static final String PARAM_SELECT_ALL_WORKFLOWS = "selectAllWorkflows";
	public static final String PARAM_WORKFLOW_PREFIX = "wf_";
	public static final String PARAM_PUBLISH_WORKFLOW_MODELS = "publishWorkflowModels";

	public ImportDevArtifactsImpl()
	{
		super();
	}

	public ImportDevArtifactsImpl(ImportDevArtifactsConfig config)
	{
		super(config);
	}

	// Get the entire list of data sets to process, which will include those that exist already,
	// which are passed in, and those newly created
	private List<Adaptation> getAllDataSetsToProcess(
		Repository repo,
		Session session,
		List<Adaptation> dataSets)
		throws OperationException
	{
		List<Adaptation> newDataSets = createDataSetsFromDataModels(repo, session);
		List<Adaptation> allDataSets;
		if (newDataSets.isEmpty())
		{
			allDataSets = dataSets;
		}
		else
		{
			allDataSets = new ArrayList<>(dataSets);
			allDataSets.addAll(newDataSets);
		}
		return allDataSets;
	}

	// Get the entire list of tables to process, which will include those that exist already,
	// which are passed in, and those from newly created data sets
	private List<AdaptationTablePredicate> getAllTablesToProcess(
		Repository repo,
		List<AdaptationTablePredicate> tables)
		throws OperationException
	{
		Map<DataSetCreationKey, List<String>> createdDataSetTableSpecs = ((ImportDevArtifactsConfig) config)
			.getCreatedDataSetTableSpecs();
		List<AdaptationTablePredicate> allTables;
		if (createdDataSetTableSpecs.isEmpty())
		{
			allTables = tables;
		}
		else
		{
			allTables = new ArrayList<>(tables);
			for (Map.Entry<DataSetCreationKey, List<String>> entry : createdDataSetTableSpecs
				.entrySet())
			{
				DataSetCreationKey key = entry.getKey();
				List<String> tableSpecs = entry.getValue();
				for (String tableSpec : tableSpecs)
				{
					try
					{
						HomeKey dataSpaceKey = key.getDataSpaceKey();
						AdaptationHome dataSpace = repo.lookupHome(dataSpaceKey);
						if (dataSpace == null)
						{
							throw new DevArtifactsException(
								tableSpec,
								new StringBuilder("Table cannot be processed because Data Space ")
									.append(dataSpaceKey.getName())
									.append(" does not exist.")
									.toString());
						}
						AdaptationName dataSetName = key.getDataSetName();
						Adaptation dataSet = dataSpace.findAdaptationOrNull(dataSetName);
						if (dataSet == null)
						{
							throw new DevArtifactsException(
								tableSpec,
								new StringBuilder("Table cannot be processed because Data Set ")
									.append(dataSetName.getStringName())
									.append(" does not exist.")
									.toString());
						}
						// Replace wildcard with all tables for that data set
						if (tableSpec.endsWith(WILDCARD))
						{
							List<AdaptationTable> allDataSetTables = AdaptationUtil
								.getAllTables(dataSet);
							for (AdaptationTable dataSetTable : allDataSetTables)
							{
								allTables.add(new AdaptationTablePredicate(dataSetTable));
							}
						}
						else
						{
							AdaptationTable table = dataSet.getTable(Path.parse(tableSpec));
							if (table == null)
							{
								throw new DevArtifactsException(
									tableSpec,
									new StringBuilder(
										"Table cannot be processed because it is not defined in Data Set ")
											.append(dataSetName.getStringName())
											.append(".")
											.toString());
							}
							allTables.add(new AdaptationTablePredicate(table));
						}
					}
					catch (DevArtifactsException ex)
					{
						registerException(ArtifactCategory.TABLE_DATA, ex);
					}
				}
			}
		}
		return allTables;
	}

	@Override
	protected void processDataTables(
		Repository repo,
		Session session,
		List<AdaptationTablePredicate> tablePredicates,
		File folder,
		String filePrefix)
		throws OperationException
	{
		List<AdaptationTablePredicate> allTables = getAllTablesToProcess(repo, tablePredicates);
		super.processDataTables(repo, session, allTables, folder, filePrefix);
	}

	@Override
	protected void processTable(
		ProcedureContext pContext,
		final AdaptationTable table,
		final File folder,
		final String filename,
		final String predicate,
		final AdaptationFilter filter,
		final ArtifactFileModifier artifactFileModifier)
		throws DevArtifactsException
	{
		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		boolean replaceMode = ImportSpecMode.REPLACE.equals(importConfig.getImportMode());

		// If replace mode and there's a predicate or filter, then can't really do replace mode
		// because we only want to replace those that match the filter. So we do a "pseudo" replace mode.
		if (replaceMode && !(predicate == null && filter == null))
		{
			try
			{
				importPseudoReplaceMode(
					pContext,
					table,
					folder,
					filename,
					predicate,
					filter,
					artifactFileModifier);
			}
			catch (OperationException ex)
			{
				throw new DevArtifactsException(getFullXMLArtifactFilename(folder, filename), ex);
			}
		}
		else
		{
			importIntoTable(pContext, table, folder, filename, false, false, artifactFileModifier);
		}
	}

	// Import as if it's replace mode, but only replacing those artifacts that match the predicate or filter.
	// Note that we have to fully qualify the ImportResult class because there's also a Data Exchange class
	// called ImportResult.
	private com.orchestranetworks.service.ImportResult importPseudoReplaceMode(
		ProcedureContext pContext,
		AdaptationTable table,
		File folder,
		String filename,
		String predicate,
		AdaptationFilter filter,
		ArtifactFileModifier artifactFileModifier)
		throws DevArtifactsException, OperationException
	{
		File tmpInputFile = null;
		// If a modifier was specified, need to create a temp file like we do in
		// the importIntoTable method, but need to do it before we delete the rows
		// in case the modifier relies on data stored in EBX
		if (artifactFileModifier != null)
		{
			try
			{
				tmpInputFile = File.createTempFile(filename, ".xml");
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					"Error creating temp file.",
					ex);
			}

			copyRealFileIntoTempFile(artifactFileModifier, folder, filename, tmpInputFile);
		}

		Map<String, Adaptation> existingRecordMap = new HashMap<>();

		// Find all records that match the predicate or filter, store them in a map
		// with key = predicate string, value = adaptation, and delete them
		// disabling the trigger. We don't want to trigger it yet because we might be
		// adding it back.
		RequestResult requestResult = queryWithPredicateOrFilter(table, predicate, filter);
		try
		{
			for (Adaptation record; (record = requestResult.nextAdaptation()) != null;)
			{
				existingRecordMap.put(record.toXPathPredicateString(), record);

				boolean allPrivileges = pContext.isAllPrivileges();
				boolean triggerActivation = pContext.isTriggerActivation();
				pContext.setAllPrivileges(true);
				pContext.setTriggerActivation(false);
				try
				{
					deleteWithProcedureContextConfig(pContext, record, false);
				}
				finally
				{
					pContext.setTriggerActivation(triggerActivation);
					pContext.setAllPrivileges(allPrivileges);
				}
			}
		}
		finally
		{
			requestResult.close();
		}

		// Import in update or insert mode. Suppress triggers since they'll all be creates
		// but some are just creates because we deleted them above and they are logically not
		// really creates. We'll deal with triggering it later.
		com.orchestranetworks.service.ImportResult importResult;
		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		importConfig.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
		File folderToImport;
		String filenameToImport;
		if (tmpInputFile == null)
		{
			folderToImport = folder;
			filenameToImport = filename;
		}
		else
		{
			folderToImport = tmpInputFile.getParentFile();
			filenameToImport = tmpInputFile.getName();
			filenameToImport = filenameToImport.substring(0, filenameToImport.length() - 4);
		}
		try
		{
			// We purposely don't pass in the artifact file modifier if it's a temp file
			// because we would have already handled it above in that case
			importResult = importIntoTable(
				pContext,
				table,
				folderToImport,
				filenameToImport,
				true,
				true,
				(tmpInputFile == null) ? artifactFileModifier : null);
		}
		finally
		{
			importConfig.setImportMode(ImportSpecMode.REPLACE);
			if (tmpInputFile != null)
			{
				tmpInputFile.delete();
			}
		}

		if (importResult != null)
		{
			// Loop through each record that was created from the import, which gets returned
			// from the result as a list of record predicates
			Iterator<String> iter = importResult.getCreatedRecordsPredicatesStrings();
			if (iter != null)
			{
				final List<Adaptation> createdRecords = new ArrayList<>();
				while (iter.hasNext())
				{
					String recordPredicate = iter.next();
					// If this is a record that existed before we deleted and recreated it,
					// then simply remove it from the map of existing records because we don't need
					// to do anything further with it
					if (existingRecordMap.containsKey(recordPredicate))
					{
						existingRecordMap.remove(recordPredicate);
					}
					// Otherwise, it's truly a new one that didn't exist previously, so we want to call
					// the create trigger, which we had suppressed on import. To do this, we have to
					// delete & recreate it. For now, put it into a list.
					else
					{
						Adaptation record = table
							.lookupFirstRecordMatchingPredicate(recordPredicate);
						createdRecords.add(record);
					}
				}

				// If we actually created records (not just replaced)
				if (!createdRecords.isEmpty())
				{
					// Create a temp file for the created records to be exported to,
					// because we'll be deleting them and then re-importing so that the
					// trigger gets invoked
					File tmpFileForCreates;
					try
					{
						tmpFileForCreates = File.createTempFile("created_" + filename, ".xml");
					}
					catch (IOException ex)
					{
						throw OperationException
							.createError("Error creating temp file created_" + filename + ".", ex);
					}
					File tmpFolderForCreates = tmpFileForCreates.getParentFile();
					String tmpFilenameForCreates = tmpFileForCreates.getName();
					// We'll want to pass the filename without the extension
					tmpFilenameForCreates = tmpFilenameForCreates
						.substring(0, tmpFilenameForCreates.length() - 4);
					// Create a filter that simply allows a record if it's in the list
					AdaptationFilter createdRecordsFilter = new AdaptationFilter()
					{
						@Override
						public boolean accept(Adaptation adaptation)
						{
							return createdRecords.contains(adaptation);
						}
					};
					// Export just those records to the temp file
					ExportSpec exportSpec = DevArtifactsUtil
						.createExportSpec(tmpFolderForCreates, tmpFilenameForCreates);
					DevArtifactsUtil.exportTable(
						pContext,
						table,
						exportSpec,
						config.getLineSeparator(),
						null,
						createdRecordsFilter);

					// Disable history so that it doesn't record that a delete happened.
					// Most tables that capture artifacts don't have history anyway, but some could so
					// might as well do this to make sure.
					// Also disable triggers since we'll be recreating it next.
					boolean allPrivileges = pContext.isAllPrivileges();
					boolean historyActivation = pContext.isHistoryActivation();
					boolean triggerActivation = pContext.isTriggerActivation();
					pContext.setAllPrivileges(true);
					pContext.setHistoryActivation(false);
					pContext.setTriggerActivation(false);
					try
					{
						for (Adaptation createdRecord : createdRecords)
						{
							deleteWithProcedureContextConfig(pContext, createdRecord, false);
						}
					}
					finally
					{
						pContext.setTriggerActivation(triggerActivation);
						pContext.setHistoryActivation(historyActivation);
						pContext.setAllPrivileges(allPrivileges);
					}

					// Now import the records again, but this time the trigger will be called
					importConfig.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
					try
					{
						importIntoTable(
							pContext,
							table,
							tmpFolderForCreates,
							tmpFilenameForCreates,
							false,
							false,
							null);
					}
					finally
					{
						importConfig.setImportMode(ImportSpecMode.REPLACE);
						// Delete the temp file that we used, since it's no longer needed
						tmpFileForCreates.delete();
					}
				}
			}

			// Loop through the records that still remain in the map.
			// These are the ones that were deleted but not recreated,
			// i.e. the ones truly being deleted by the import.
			// The ones recreated were removed from the map so that's why we can just
			// look at all those still in the map.
			// We skipped calling the delete trigger earlier so now we need to
			// recreate it and delete it so that the delete trigger gets called.
			for (String recordPredicate : existingRecordMap.keySet())
			{
				Adaptation record = existingRecordMap.get(recordPredicate);
				// Create a copy value context of the record
				ValueContext valueContext = pContext.getContextForNewOccurrence(record, table);
				boolean allPrivileges = pContext.isAllPrivileges();
				boolean triggerActivation = pContext.isTriggerActivation();
				boolean historyActivation = pContext.isHistoryActivation();
				pContext.setAllPrivileges(true);
				// Recreate the record. We don't want to activate triggers or history when recreating the record,
				// since the point is to just recreate it so that we can delete it again and activate the delete trigger.
				pContext.setTriggerActivation(false);
				pContext.setHistoryActivation(false);
				try
				{
					record = pContext.doCreateOccurrence(valueContext, table);
				}
				finally
				{
					pContext.setHistoryActivation(historyActivation);
					pContext.setTriggerActivation(triggerActivation);
					pContext.setAllPrivileges(allPrivileges);
				}

				// Delete the record we just created, with the trigger being invoked this time
				// (if it was originally specified as being invoked).
				// Don't activate history since there's already history of the delete from before
				// (if there's history on the table at all, which for most artifacts tables there's not).
				pContext.setAllPrivileges(true);
				pContext.setHistoryActivation(false);
				try
				{
					deleteWithProcedureContextConfig(pContext, record, false);
				}
				finally
				{
					pContext.setHistoryActivation(historyActivation);
					pContext.setAllPrivileges(allPrivileges);
				}
			}
		}
		return importResult;
	}

	// If no predicate is specified, query with the given filter.
	// Otherwise, use the predicate.
	private static RequestResult queryWithPredicateOrFilter(
		AdaptationTable table,
		String predicate,
		AdaptationFilter filter)
	{
		RequestResult requestResult;
		if (predicate == null)
		{
			Request request = table.createRequest();
			if (filter != null)
			{
				request.setSpecificFilter(filter);
			}
			requestResult = request.execute();
		}
		else
		{
			requestResult = table.createRequestResult(predicate);
		}
		return requestResult;
	}

	private com.orchestranetworks.service.ImportResult importIntoTable(
		ProcedureContext pContext,
		AdaptationTable table,
		File folder,
		String filename,
		boolean includeDetailedResult,
		boolean suppressTriggers,
		ArtifactFileModifier artifactFileModifier)
		throws DevArtifactsException
	{
		File tmpFile;
		ImportSpec spec;
		// If no modifier was specified, just do a normal import from the file
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			spec = createImportSpec(folder, filename, includeDetailedResult);
			if (spec == null)
			{
				return null;
			}
		}
		// If a modifier was specified, need to utilize a temp file
		else
		{
			// Create the temp file, which will have the same name but include a unique string
			// and will be in the system's temp folder
			try
			{
				tmpFile = File.createTempFile(filename, ".xml");
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					"Error creating temp file.",
					ex);
			}

			// Need to read in the real file, modifying it and writing to the temp file
			copyRealFileIntoTempFile(artifactFileModifier, folder, filename, tmpFile);

			spec = createTempFileImportSpec(tmpFile, includeDetailedResult);
		}

		spec.setTargetAdaptationTable(table);

		com.orchestranetworks.service.ImportResult importResult;
		try
		{
			boolean triggerActivation = pContext.isTriggerActivation();
			if (suppressTriggers)
			{
				pContext.setTriggerActivation(false);
			}
			try
			{
				importResult = doImport(pContext, spec);
			}
			finally
			{
				pContext.setTriggerActivation(triggerActivation);
			}
		}
		finally
		{
			// If we used a modifier, delete the temp file
			if (artifactFileModifier != null)
			{
				tmpFile.delete();
			}
		}
		return importResult;
	}

	private static com.orchestranetworks.service.ImportResult doImport(
		ProcedureContext pContext,
		ImportSpec spec)
		throws DevArtifactsException
	{
		boolean allPrivileges = pContext.isAllPrivileges();
		pContext.setAllPrivileges(true);
		com.orchestranetworks.service.ImportResult importResult;
		try
		{
			importResult = pContext.doImport(spec);
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(spec.getSourceFile().getAbsolutePath(), ex);
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
		return importResult;
	}

	// This will do a delete but not overriding the procedure context's configuration.
	// This is not how DeleteRecordProcedure operates. Any value not explicitly specified
	// is assumed the default, not assumed to be what is already set on the procedure context.
	// Long-term, we may want to address this in DeleteRecordProcedure, but for
	// backwards-compatibility, it isn't easy to do, so we're just handling it here.
	private static void deleteWithProcedureContextConfig(
		ProcedureContext pContext,
		Adaptation record,
		boolean deletingChildren)
		throws OperationException
	{
		DeleteRecordProcedure procedure = new DeleteRecordProcedure(record);
		procedure.setAllPrivileges(pContext.isAllPrivileges());
		procedure.setBlockingConstraintDisabled(pContext.isBlockingConstraintsDisabled());
		procedure.setDatabaseHistoryActivation(pContext.isDatabaseHistoryActivation());
		procedure.setDeletingChildren(deletingChildren);
		procedure.setHistoryActivation(pContext.isHistoryActivation());
		procedure.setTriggerActivation(pContext.isTriggerActivation());
		procedure.execute(pContext);
	}

	@Override
	protected void processGroup(
		ProcedureContext pContext,
		Adaptation dataSet,
		SchemaNode groupNode,
		File folder,
		String filename,
		ArtifactFileModifier artifactFileModifier)
		throws DevArtifactsException
	{
		File tmpFile;
		ImportSpec importSpec;
		// If no modifier was specified, just do a normal import from the file
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			importSpec = createImportSpec(folder, filename, false);
			if (importSpec == null)
			{
				return;
			}
		}
		// If a modifier was specified, need to utilize a temp file
		else
		{
			// Create the temp file, which will have the same name but include a unique string
			// and will be in the system's temp folder
			try
			{
				tmpFile = File.createTempFile(filename, ".xml");
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					"Error creating temp file.",
					ex);
			}

			// Need to read in the real file, modifying it and writing to the temp file
			copyRealFileIntoTempFile(artifactFileModifier, folder, filename, tmpFile);

			importSpec = createTempFileImportSpec(tmpFile, false);
		}

		importSpec.setTargetAdaptation(dataSet);

		try
		{
			doImport(pContext, importSpec);
		}
		finally
		{
			// If we used a modifier, delete the temp file
			if (artifactFileModifier != null)
			{
				tmpFile.delete();
			}
		}
	}

	private ImportSpec createImportSpec(File folder, String filename, boolean includeDetailedResult)
		throws DevArtifactsException
	{
		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		File srcFile = new File(folder, filename + ".xml");
		// Only throw an error if they don't want to skip non-existent files
		if (!srcFile.exists())
		{
			if (importConfig.isSkipNonExistingFiles())
			{
				return null;
			}
			throw new DevArtifactsException(
				srcFile.getAbsolutePath(),
				"Expected import file not found.");
		}

		ImportSpec spec = new ImportSpec();
		spec.setImportMode(importConfig.getImportMode());
		spec.setSourceFile(srcFile);
		spec.setDetailedResult(includeDetailedResult);
		return spec;
	}

	@Override
	protected void processDataSetDataXML(
		ProcedureContext pContext,
		Adaptation dataSet,
		File folder,
		String filePrefix,
		ArtifactFileModifier artifactFileModifier,
		boolean suppressTriggers)
		throws DevArtifactsException
	{
		StringBuilder bldr = new StringBuilder();
		if (filePrefix != null)
		{
			bldr.append(filePrefix);
		}
		String filename = bldr.append(dataSet.getAdaptationName().getStringName()).toString();
		File tmpFile;
		ImportSpec spec;
		// If no modifier was specified, just do a normal import from the file
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			spec = createImportSpec(folder, filename, false);
			if (spec == null)
			{
				return;
			}
		}
		// If a modifier was specified, need to utilize a temp file
		else
		{
			// Create the temp file, which will have the same name but include a unique string
			// and will be in the system's temp folder
			try
			{
				tmpFile = File.createTempFile(filename, ".xml");
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					"Error creating temp file.",
					ex);
			}

			// Need to read in the real file, modifying it and writing to the temp file
			copyRealFileIntoTempFile(artifactFileModifier, folder, filename, tmpFile);

			spec = createTempFileImportSpec(tmpFile, false);
		}

		spec.setTargetAdaptation(dataSet);

		boolean triggerActivation = pContext.isTriggerActivation();
		if (suppressTriggers)
		{
			pContext.setTriggerActivation(false);
		}
		try
		{
			doImport(pContext, spec);
		}
		finally
		{
			pContext.setTriggerActivation(triggerActivation);
			// If we used a modifier, delete the temp file
			if (artifactFileModifier != null)
			{
				tmpFile.delete();
			}
		}
	}

	private static void copyRealFileIntoTempFile(
		ArtifactFileModifier artifactFileModifier,
		File folder,
		String filename,
		File tempFile)
		throws DevArtifactsException
	{
		File file = new File(folder, filename + ".xml");
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
			try
			{
				modifyArtifactFile(file, writer, artifactFileModifier, false);
			}
			finally
			{
				writer.close();
			}
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error reading from file and writing into temp file " + tempFile.getAbsolutePath()
					+ ".",
				ex);
		}
	}

	private ImportSpec createTempFileImportSpec(File tempFile, boolean includeDetailedResult)
		throws DevArtifactsException
	{
		// Create the spec from the temp file
		File tmpFolder = tempFile.getParentFile();
		String tmpFilename = tempFile.getName();
		// We'll want to pass the filename without the extension
		tmpFilename = tmpFilename.substring(0, tmpFilename.length() - 4);
		return createImportSpec(tmpFolder, tmpFilename.toString(), includeDetailedResult);
	}

	@Override
	protected Properties processDataSetDataPropertiesFile(
		Locale locale,
		String masterDataSpaceName,
		boolean qualifyDataSet,
		AdaptationHome dataSpace,
		AdaptationName dataSetName,
		File folder,
		String filePrefix)
		throws DevArtifactsException
	{
		Properties props = new Properties();
		StringBuilder bldr = new StringBuilder();
		if (filePrefix != null)
		{
			bldr.append(filePrefix);
		}
		if (qualifyDataSet && config.isQualifyDataSetAndTableFileNames())
		{
			bldr.append(masterDataSpaceName).append(FILE_NAME_SEPARATOR);
		}
		bldr.append(dataSetName.getStringName()).append(DATA_SET_PROPERTIES_SUFFIX);
		File propertiesFile = new File(folder, bldr.toString());
		try
		{
			if (propertiesFile.exists())
			{
				BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));

				try
				{
					props.load(reader);
				}
				finally
				{
					reader.close();
				}
			}
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(propertiesFile.getAbsolutePath(), ex);
		}
		return props;
	}

	@Override
	protected void processDataSetDataProperties(
		ProcedureContext pContext,
		Properties props,
		boolean qualifyDataSet,
		Adaptation dataSet,
		File folder,
		String filePrefix)
		throws DevArtifactsException
	{
		String label = getPropertyValueOrNull(props, DATA_SET_PROPERTY_LABEL);
		String description = getPropertyValueOrNull(props, DATA_SET_PROPERTY_DESCRIPTION);
		String owner = getPropertyValueOrNull(props, DATA_SET_PROPERTY_OWNER);
		Locale locale = pContext.getSession().getLocale();

		if (!Objects.equals(label, dataSet.getLabel(locale))
			|| (!Objects.equals(description, dataSet.getDescription(locale)))
			|| (owner != null && !owner.equals(dataSet.getOwner().format())))
		{
			try
			{
				setDataSetProperties(pContext, dataSet, label, description, owner);
			}
			catch (OperationException ex)
			{
				throw new DevArtifactsException(dataSet.getAdaptationName().getStringName(), ex);
			}
		}
	}

	private static void setDataSetProperties(
		ProcedureContext pContext,
		Adaptation dataSet,
		String label,
		String description,
		String owner)
		throws OperationException
	{
		boolean allPrivileges = pContext.isAllPrivileges();
		try
		{
			pContext.setAllPrivileges(true);

			Locale locale = pContext.getSession().getLocale();
			if (!Objects.equals(label, dataSet.getLabel(locale)))
			{
				pContext.setInstanceLabel(dataSet, UserMessage.createInfo(label));
			}
			if (!Objects.equals(description, dataSet.getDescription(locale)))
			{
				pContext.setInstanceDescription(dataSet, UserMessage.createInfo(description));
			}
			// Unlike label & description, owner can't be set to null so just ignore if it's null
			if (owner != null && !owner.equals(dataSet.getOwner().format()))
			{
				Profile ownerProfile;
				try
				{
					ownerProfile = Profile.parse(owner);
				}
				// Profile.parse declares that it throws an IllegalArgumentException when it can't parse the string
				catch (IllegalArgumentException ex)
				{
					throw OperationException.createError(
						"Error parsing owner for data set. Value " + owner + " is not valid.",
						ex);
				}
				pContext.setInstanceOwner(dataSet, ownerProfile);
			}
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
	}

	@Override
	protected void processDataSetPermissions(
		ProcedureContext pContext,
		String masterDataSpaceName,
		final boolean qualifyDataSet,
		Adaptation dataSet,
		final File folder,
		boolean neverProcessChildren,
		boolean child)
		throws DevArtifactsException
	{
		// This gets recursively called and we want to pass the master data space's name
		// in to each invocation
		String dataSpaceName = (masterDataSpaceName == null) ? dataSet.getHome().getKey().getName()
			: masterDataSpaceName;
		AdaptationTable permissionsTable = AdminPermissionsUtil.getDataSetPermissionsTable(dataSet);
		String filename = getPermissionsFilename(
			qualifyDataSet,
			config.isQualifyDataSetAndTableFileNames(),
			dataSpaceName,
			dataSet.getAdaptationName().getStringName());

		boolean continueProcessing = true;

		// On import, need to also potentially process child data spaces, if specified.
		// We purposely process the child data spaces before their parent, so that if
		// there's an issue, next time it's run, it will still process the top parent
		// (since it hasn't been changed yet).
		if (!neverProcessChildren)
		{
			if (!child)
			{
				continueProcessing = didDataSetPermissionsChange(
					pContext,
					permissionsTable,
					folder,
					filename);
			}
			if (continueProcessing)
			{
				AdaptationHome dataSpace = dataSet.getHome();
				Session session = pContext.getSession();
				List<AdaptationHome> snapshots = dataSpace.getVersionChildren();
				for (AdaptationHome snapshot : snapshots)
				{
					List<AdaptationHome> childDataSpaces = snapshot.getBranchChildren();
					for (AdaptationHome childDataSpace : childDataSpaces)
					{
						if (childDataSpace.isOpen())
						{
							final Adaptation childDataSet = childDataSpace
								.findAdaptationOrNull(dataSet.getAdaptationName());
							if (childDataSet != null)
							{
								DevArtifactsProcedure proc = new DevArtifactsProcedure()
								{
									@Override
									protected void doExecute(ProcedureContext pContext)
										throws DevArtifactsException, OperationException
									{
										processDataSetPermissions(
											pContext,
											dataSpaceName,
											qualifyDataSet,
											childDataSet,
											folder,
											false,
											true);
									}
								};
								try
								{
									DevArtifactsUtil
										.executeProcedure(proc, session, childDataSpace);
								}
								catch (OperationException ex)
								{
									throw new DevArtifactsException(
										getFullXMLArtifactFilename(folder, filename),
										ex);
								}
							}
						}
					}
				}
			}
		}
		if (continueProcessing)
		{
			super.processDataSetPermissions(
				pContext,
				dataSpaceName,
				qualifyDataSet,
				dataSet,
				folder,
				neverProcessChildren,
				child);
		}
	}

	private boolean didDataSetPermissionsChange(
		ProcedureContext pContext,
		AdaptationTable permissionsTable,
		File folder,
		String filename)
		throws DevArtifactsException
	{
		File tmpFile;
		try
		{
			tmpFile = File.createTempFile(filename, ".xml");
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error creating temp file for data set permissions comparison.",
				ex);
		}
		ExportSpec exportSpec = DevArtifactsUtil.createTempFileExportSpec(tmpFile);

		try
		{
			// Don't change the line separator because we'll just be reading it line by line anyway,
			// and we won't care which line separator it uses. It's just more efficient this way.
			DevArtifactsUtil.exportTable(pContext, permissionsTable, exportSpec, null, null, null);
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error exporting to temp table for data set permissions compare.",
				ex);
		}

		File srcFile = new File(folder, filename + ".xml");

		BufferedReader tmpFileReader;
		try
		{
			tmpFileReader = new BufferedReader(new FileReader(tmpFile));
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error opening reader for temp file for data set permissions compare.",
				ex);
		}
		BufferedReader srcFileReader;
		try
		{
			srcFileReader = new BufferedReader(new FileReader(srcFile));
		}
		catch (IOException ex)
		{
			try
			{
				tmpFileReader.close();
			}
			catch (IOException ex2)
			{
				LoggingCategory.getKernel()
					.error(
						"Error closing reader for temp file for data set permissions compare.",
						ex2);
			}
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error opening reader for source file for data set permissions compare.",
				ex);
		}

		boolean diffFound = false;
		try
		{
			for (String tmpFileLine; !diffFound
				&& (tmpFileLine = tmpFileReader.readLine()) != null;)
			{
				String srcFileLine = srcFileReader.readLine();
				if (srcFileLine == null)
				{
					diffFound = true;
				}
				else
				{
					diffFound = !tmpFileLine.equals(srcFileLine);
				}
			}

			if (!diffFound)
			{
				diffFound = (srcFileReader.readLine() != null);
			}
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(
				getFullXMLArtifactFilename(folder, filename),
				"Error reading files for data set permissions compare.",
				ex);
		}
		finally
		{
			try
			{
				tmpFileReader.close();
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					"Error closing reader for temp file for data set permissions compare.",
					ex);
			}
			finally
			{
				tmpFile.delete();

				try
				{
					srcFileReader.close();
				}
				catch (IOException ex)
				{
					throw new DevArtifactsException(
						getFullXMLArtifactFilename(folder, filename),
						"Error closing reader for source file for data set permissions compare.",
						ex);
				}
			}
		}
		return diffFound;
	}

	@Override
	protected void doProcessTasksData(ProcedureContext pContext, File folder)
		throws DevArtifactsException
	{
		// Can't use replace mode since there could be other tasks that were created locally in each environment 
		// that are not part of the Data_task.xml. Using replace mode will try to delete the tasks (which we do not want to happen).
		// Moreover, deleting tasks will fail if the tasks have a "Scheduled Task" - in which case the Import Dev Artifacts will fail
		// with an error "Task currently scheduled: it cannot be deleted."
		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		ImportSpecMode importMode = importConfig.getImportMode();
		importConfig.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
		try
		{
			super.doProcessTasksData(pContext, folder);
		}
		finally
		{
			importConfig.setImportMode(importMode);
		}
	}

	@Override
	protected void doProcessDMA(ProcedureContext pContext, File folder) throws DevArtifactsException
	{
		AdaptationHome dmaDataSpace = pContext.getAdaptationHome();
		File srcFile = new File(
			config.getCopyEnvironmentDMAFolder(),
			dmaDataSpace.getKey().getName() + ".ebx");
		importArchive(pContext, srcFile);
	}

	@Override
	public void processArtifacts(Repository repo, Session session, boolean environmentCopy)
		throws OperationException
	{
		// For import, only process if environment copy wasn't specified,
		// or if it was and this is the invocation for environment copy.
		// In other words, processArtifacts gets called twice for environment copy but
		// we only want to process once.
		if (!config.isEnvironmentCopy() || environmentCopy)
		{
			if (config.isRefreshSchemas())
			{
				// refresh schemas that have been modified or that include schemas that have been modified
				repo.refreshSchemas(false);
			}
			super.processArtifacts(repo, session, environmentCopy);
		}
	}

	@Override
	protected void processDataSetPermissions(
		Repository repo,
		Session session,
		List<Adaptation> dataSets,
		File folder)
		throws OperationException
	{
		List<Adaptation> allDataSets = getAllDataSetsToProcess(repo, session, dataSets);
		super.processDataSetPermissions(repo, session, allDataSets, folder);

		// If we created any data sets, refresh schemas after processing
		if (!((ImportDevArtifactsConfig) config).getDataSetsToCreate().isEmpty())
		{
			repo.refreshSchemas(false);
		}
	}

	private List<Adaptation> createDataSetsFromDataModels(Repository repo, Session session)
		throws OperationException
	{
		List<DataSetCreationInfo> dataSetsToCreate = ((ImportDevArtifactsConfig) config)
			.getDataSetsToCreate();
		final List<Adaptation> dataSets = new ArrayList<>();
		for (DataSetCreationInfo dataSetCreationInfo : dataSetsToCreate)
		{
			DataSetCreationKey key = dataSetCreationInfo.getDataSetCreationKey();
			final String dataModelXSD = dataSetCreationInfo.getDataModelXSD();
			final HomeKey dataSpaceKey = key.getDataSpaceKey();
			final AdaptationName dataSetName = key.getDataSetName();

			final File folder = config.getPermissionsFolder();
			AdaptationHome dataSpace = repo.lookupHome(dataSpaceKey);
			if (dataSpace == null)
			{
				String filename = getPermissionsFilename(
					true,
					config.isQualifyDataSetAndTableFileNames(),
					dataSpaceKey.getName(),
					dataSetName.getStringName());
				registerException(
					ArtifactCategory.DATA_SET,
					new DevArtifactsException(
						getFullPropertiesArtifactFilename(folder, filename),
						new StringBuilder("Data Set can't be created because Data Space ")
							.append(dataSpaceKey.getName())
							.append(" does not exist.")
							.toString()));
				continue;
			}
			final Properties props;
			try
			{
				props = processDataSetDataPropertiesFile(
					session.getLocale(),
					null,
					true,
					dataSpace,
					dataSetName,
					folder,
					PERMISSIONS_DATA_SET_PREFIX);
			}
			catch (DevArtifactsException ex)
			{
				registerException(ArtifactCategory.DATA_SET, ex);
				continue;
			}
			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					try
					{
						Adaptation dataSet = getOrCreateDataSetFromProperties(
							pContext,
							dataSetName.getStringName(),
							props,
							SchemaLocation.parse(DATA_MODEL_SCHEMA_LOCATION_PREFIX + dataModelXSD));
						if (dataSet != null)
						{
							dataSets.add(dataSet);
						}
					}
					catch (OperationException ex)
					{
						String filename = getPermissionsFilename(
							true,
							config.isQualifyDataSetAndTableFileNames(),
							dataSpaceKey.getName(),
							dataSetName.getStringName());
						registerException(
							ArtifactCategory.DATA_SET,
							new DevArtifactsException(
								getFullPropertiesArtifactFilename(folder, filename),
								ex));
					}
				}
			};
			ProcedureExecutor.executeProcedure(proc, session, dataSpace);
		}
		return dataSets;
	}

	@Override
	protected void processDataSpacePermissions(
		ProcedureContext pContext,
		AdaptationHome dataSpace,
		File folder,
		AdaptationTable permissionsTable)
		throws DevArtifactsException
	{
		// Data space permissions can't use replace mode so manually delete all rows for
		// this data space that aren't owner, then change mode to update/insert and import
		final ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		ImportSpecMode importMode = importConfig.getImportMode();
		if (ImportSpecMode.REPLACE.equals(importMode))
		{
			final RequestResult requestResult = permissionsTable.createRequestResult(
				AdminPermissionsUtil.DATA_SPACE_PERMISSIONS_DATA_SPACE_KEY_PATH.format() + "='"
					+ dataSpace.getKey().format() + "' and not("
					+ AdminPermissionsUtil.DATA_SPACE_PERMISSIONS_PROFILE_PATH.format() + "='"
					+ Profile.OWNER.format() + "')");
			try
			{
				// Delete all but the owner row for this data space
				Adaptation record;
				while ((record = requestResult.nextAdaptation()) != null)
				{
					try
					{
						Delete.execute(pContext, record);
					}
					catch (OperationException ex)
					{
						throw new DevArtifactsException(
							getFullXMLArtifactFilename(
								folder,
								PERMISSIONS_DATA_SPACE_PREFIX + dataSpace.getKey().getName()),
							ex);
					}
				}
				try
				{
					importConfig.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
					super.processDataSpacePermissions(
						pContext,
						dataSpace,
						folder,
						permissionsTable);
				}
				finally
				{
					importConfig.setImportMode(importMode);
				}
			}
			finally
			{
				requestResult.close();
			}
		}
		else
		{
			super.processDataSpacePermissions(pContext, dataSpace, folder, permissionsTable);
		}
	}

	// This handles importing usersRoles & users which requires extra logic to take
	// the usersRolesPredicate and determine which users that means. Also, the artifacts can
	// include more users than indicated by the predicate so we need to make sure we ignore those.
	@Override
	protected void processUsersRolesTable(
		ProcedureContext pContext,
		final AdaptationTable usersRolesTable,
		Set<String> restrictedRoleNames,
		File folder,
		String filePrefix)
		throws DevArtifactsException
	{
		Set<String> usersInRestrictedRoles;
		if (restrictedRoleNames == null)
		{
			usersInRestrictedRoles = null;
		}
		else
		{
			// Find all usersRoles rows whose roles are in the given set,
			// and collect their users into a set
			usersInRestrictedRoles = new HashSet<>();
			Request request = usersRolesTable.createRequest();
			request.setSpecificFilter(
				new FieldValueInCollectionAdaptationFilter(
					AdminUtil.getDirectoryUsersRolesRolePath(),
					restrictedRoleNames));
			RequestResult requestResult = request.execute();
			try
			{
				for (Adaptation userRoleRecord; (userRoleRecord = requestResult
					.nextAdaptation()) != null;)
				{
					String username = userRoleRecord
						.getString(AdminUtil.getDirectoryUsersRolesUserPath());
					usersInRestrictedRoles.add(username);
				}
			}
			finally
			{
				requestResult.close();
			}
		}

		String usersRolesPredicate = config.getUsersRolesPredicate();
		Set<String> usersInUsersRolesPredicateBeforeImport;
		if (usersRolesPredicate == null)
		{
			usersInUsersRolesPredicateBeforeImport = null;
		}
		else
		{
			// Query the usersRoles for the predicate and collect the users in a set.
			// This is independent from the roles that we're restricting to.
			usersInUsersRolesPredicateBeforeImport = new HashSet<>();
			RequestResult requestResult = usersRolesTable.createRequestResult(usersRolesPredicate);
			try
			{
				for (Adaptation userRoleRecord; (userRoleRecord = requestResult
					.nextAdaptation()) != null;)
				{
					String username = userRoleRecord
						.getString(AdminUtil.getDirectoryUsersRolesUserPath());
					usersInUsersRolesPredicateBeforeImport.add(username);
				}
			}
			finally
			{
				requestResult.close();
			}
		}

		AdaptationTable usersTable = AdminUtil
			.getDirectoryUsersTable(usersRolesTable.getContainerAdaptation());
		StringBuilder bldr = new StringBuilder();
		if (filePrefix != null)
		{
			bldr.append(filePrefix);
		}
		bldr.append(getTableFilename(usersRolesTable));
		String usersRolesFilename = bldr.toString();

		bldr = new StringBuilder();
		if (filePrefix != null)
		{
			bldr.append(filePrefix);
		}
		bldr.append(getTableFilename(usersTable));
		String usersFilename = bldr.toString();

		// If we're not restricting by roles or predicate, then do a straight process of usersRoles & users
		if (usersInRestrictedRoles == null && usersInUsersRolesPredicateBeforeImport == null)
		{
			processTable(pContext, usersRolesTable, folder, usersRolesFilename, null, null, null);
			processTable(pContext, usersTable, folder, usersFilename, null, null, null);
		}
		// Otherwise we have to do a lot of special processing
		else
		{
			ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
			com.orchestranetworks.service.ImportResult importResult;
			// If it's replace mode, we need to insert in pseudo replace mode only those usersRoles
			// whose user is in both of the sets (if both roles and predicate were specified -
			// if just one then just that set)
			if (ImportSpecMode.REPLACE.equals(importConfig.getImportMode()))
			{
				List<Set<String>> usersInRolesSets = new ArrayList<>();
				if (usersInRestrictedRoles != null)
				{
					usersInRolesSets.add(usersInRestrictedRoles);
				}
				if (usersInUsersRolesPredicateBeforeImport != null)
				{
					usersInRolesSets.add(usersInUsersRolesPredicateBeforeImport);
				}
				AdaptationFilter usersRolesFilter = new UsersInSetsAdaptationFilter(
					AdminUtil.getDirectoryUsersRolesUserPath(),
					usersInRolesSets);
				try
				{
					importResult = importPseudoReplaceMode(
						pContext,
						usersRolesTable,
						folder,
						usersRolesFilename,
						null,
						usersRolesFilter,
						null);
				}
				catch (OperationException ex)
				{
					throw new DevArtifactsException(
						getFullXMLArtifactFilename(folder, usersRolesFilename),
						ex);
				}
			}
			// If not replace mode, then a filter isn't needed, we're just importing in insert/update mode
			else
			{
				importResult = importIntoTable(
					pContext,
					usersRolesTable,
					folder,
					usersRolesFilename,
					true,
					false,
					null);
			}
			// Note that at this point, regardless of if it was replace or not, we can have more usersRoles imported
			// than are specified by the predicate because the import would have imported all rows from the file.
			// Next we need to delete the ones that shouldn't have been imported based on the predicate.

			Set<String> usersInUsersRolesPredicateAfterImport;
			if (usersInUsersRolesPredicateBeforeImport == null)
			{
				usersInUsersRolesPredicateAfterImport = null;
			}
			else
			{
				// Query for all usersRoles that match the predicate now that the import has occurred,
				// and save their users in a separate set
				usersInUsersRolesPredicateAfterImport = new HashSet<>();
				RequestResult requestResult = usersRolesTable
					.createRequestResult(usersRolesPredicate);
				try
				{
					for (Adaptation userRoleRecord; (userRoleRecord = requestResult
						.nextAdaptation()) != null;)
					{
						String username = userRoleRecord
							.getString(AdminUtil.getDirectoryUsersRolesUserPath());
						usersInUsersRolesPredicateAfterImport.add(username);
					}
				}
				finally
				{
					requestResult.close();
				}

				// Loop through all the usersRoles records that were created in the import
				// and for each one, if its user isn't in the set of users that match the
				// predicate, then delete it. Note that this will handle the rare situation
				// where a user already existed prior to import but wasn't in the import file.
				// Since it wouldn't have been created from the import, then it will be left alone.
				// It also should handle the situation where a user existed prior to the import
				// and was in the import file, but doesn't match the predicate. In that case,
				// it wouldn't have been deleted & recreated, it would have just been updated by
				// the import and not included in the list of created records and therefore ignored.
				if (importResult != null)
				{
					Iterator<String> iter = importResult.getCreatedRecordsPredicatesStrings();
					if (iter != null)
					{
						while (iter.hasNext())
						{
							String createdUserRolePredicate = iter.next();
							Adaptation userRoleRecord = usersRolesTable
								.lookupFirstRecordMatchingPredicate(createdUserRolePredicate);
							String username = userRoleRecord
								.getString(AdminUtil.getDirectoryUsersRolesUserPath());
							if (!usersInUsersRolesPredicateAfterImport.contains(username))
							{
								try
								{
									Delete.execute(pContext, userRoleRecord);
								}
								catch (OperationException ex)
								{
									throw new DevArtifactsException(usersRolesFilename, ex);
								}
							}
						}
					}
				}
			}

			// Now we need to handle the users. We've already collected the users that
			// need to be processed from when we handled the usersRoles, so we do
			// an import in either pseudo replace mode or update/insert mode based on
			// a filter that uses those sets.

			// Use a modifier to handle removing the administrator role, if specified by config
			ArtifactFileModifier usersModifier = (importConfig.isRemoveAdministratorRole())
				? new DirectoryUserArtifactFileModifier()
				: null;

			if (ImportSpecMode.REPLACE.equals(importConfig.getImportMode()))
			{
				List<Set<String>> usersInRolesSets = new ArrayList<>();
				if (usersInRestrictedRoles != null)
				{
					usersInRolesSets.add(usersInRestrictedRoles);
				}
				if (usersInUsersRolesPredicateBeforeImport != null)
				{
					usersInRolesSets.add(usersInUsersRolesPredicateBeforeImport);
				}
				AdaptationFilter usersFilter = new UsersInSetsAdaptationFilter(
					AdminUtil.getDirectoryUsersLoginPath(),
					usersInRolesSets);
				try
				{
					importResult = importPseudoReplaceMode(
						pContext,
						usersTable,
						folder,
						usersFilename,
						null,
						usersFilter,
						usersModifier);
				}
				catch (OperationException ex)
				{
					throw new DevArtifactsException(usersFilename, ex);
				}
			}
			else
			{
				importResult = importIntoTable(
					pContext,
					usersTable,
					folder,
					usersFilename,
					true,
					false,
					usersModifier);
			}

			// Note that, as with usersRoles, we can have users now that were in the input file
			// but don't match the predicate so we will need to remove them

			// First, we re-import in insert/update mode to fix the password update field.
			// This must be done before deleting the extra records or else they'd be re-inserted
			// as creates. If done at this point, it will just update the records that were already
			// created and no creates should be happening here.
			fixUserPasswordUpdateField(pContext, usersTable, folder, usersFilename, usersModifier);

			// Now we delete the extra users that didn't match the predicate, similar to what we did for usersRoles
			if (usersInUsersRolesPredicateAfterImport != null && importResult != null)
			{
				Iterator<String> iter = importResult.getCreatedRecordsPredicatesStrings();
				if (iter != null)
				{
					while (iter.hasNext())
					{
						String createdUserPredicate = iter.next();
						Adaptation userRecord = usersTable
							.lookupFirstRecordMatchingPredicate(createdUserPredicate);
						String username = userRecord.getOccurrencePrimaryKey().format();
						if (!usersInUsersRolesPredicateAfterImport.contains(username))
						{
							try
							{
								Delete.execute(pContext, userRecord);
							}
							catch (OperationException ex)
							{
								throw new DevArtifactsException(usersFilename, ex);
							}
						}
					}
				}
			}
		}
	}

	private Adaptation processDataSetFromFile(
		ProcedureContext pContext,
		String dataSetName,
		Properties dataSetProps,
		File folder,
		String filePrefix,
		SchemaLocation schemaLocation)
		throws DevArtifactsException
	{
		Adaptation dataSet;
		try
		{
			dataSet = getOrCreateDataSetFromProperties(
				pContext,
				dataSetName,
				dataSetProps,
				schemaLocation);
		}
		catch (OperationException ex)
		{
			StringBuilder bldr = new StringBuilder();
			if (filePrefix != null)
			{
				bldr.append(filePrefix);
			}
			bldr.append(dataSetName);
			throw new DevArtifactsException(
				getFullPropertiesArtifactFilename(folder, bldr.toString()),
				ex);
		}

		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		ImportSpecMode importMode = importConfig.getImportMode();
		importConfig.setImportMode(ImportSpecMode.REPLACE);

		processDataSetDataXML(pContext, dataSet, folder, filePrefix, null, false);

		importConfig.setImportMode(importMode);

		processDataSetDataProperties(pContext, dataSetProps, true, dataSet, folder, filePrefix);
		return dataSet;
	}

	private Adaptation getOrCreateDataSetFromProperties(
		ProcedureContext pContext,
		String dataSetName,
		Properties dataSetProps,
		SchemaLocation schemaLocation)
		throws OperationException
	{
		AdaptationReference adaptationRef = AdaptationReference.forPersistentName(dataSetName);
		Adaptation dataSet = pContext.getAdaptationHome().findAdaptationOrNull(adaptationRef);
		if (dataSet == null)
		{
			String parentDataSetName = getPropertyValueOrNull(
				dataSetProps,
				DATA_SET_PROPERTY_PARENT_DATA_SET);
			if (parentDataSetName == null)
			{
				dataSet = createRootDataSet(pContext, adaptationRef, schemaLocation);
			}
			else
			{
				dataSet = createChildDataSet(
					pContext,
					adaptationRef,
					AdaptationName.forName(parentDataSetName));
			}
		}
		return dataSet;
	}

	@Override
	protected void processWorkflowModels(
		Repository repo,
		Session session,
		List<String> wfModels,
		File folder,
		String filePrefix)
		throws OperationException
	{
		super.processWorkflowModels(repo, session, wfModels, folder, filePrefix);

		if (((ImportDevArtifactsConfig) config).isPublishWorkflowModels())
		{
			publishWorkflowModels(repo, session, wfModels);
		}
	}

	@Override
	protected void processWorkflowModel(
		ProcedureContext pContext,
		String wfModel,
		File folder,
		String filePrefix)
		throws DevArtifactsException
	{
		AdaptationHome wfDataSpace = pContext.getAdaptationHome();
		Properties props = processDataSetDataPropertiesFile(
			pContext.getSession().getLocale(),
			null,
			false,
			wfDataSpace,
			AdaptationName.forName(wfModel),
			folder,
			filePrefix);
		processDataSetFromFile(
			pContext,
			wfModel,
			props,
			folder,
			filePrefix,
			AdminUtil.getWorkflowModelsSchemaLocation());
	}

	// Publication of workflow models uses some internal classes, like WorkflowPublisher,
	// that are not available as part of the public API. They were made available for the PS team and
	// are being used with permission from engineering (ON-9907)
	private void publishWorkflowModels(Repository repo, Session session, List<String> wfModelNames)
	{
		if (!wfModelNames.isEmpty())
		{
			ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
			Map<String, Set<String>> masterWorkflowModels = importConfig.getMasterWorkflowModels();
			List<String> wfModelsToNotPublish = importConfig.getWorkflowModelsToNotPublish();
			// The map is keyed by master workflow, but for our processing, we also need to lookup by sub-wf
			// so create another map that's the reverse
			Map<String, Set<String>> reverseMap = getReverseMasterWorkflowModelMap(
				masterWorkflowModels);
			Map<String, Set<String>> publications = new LinkedHashMap<>();
			Adaptation workflowModelsConfigurationDataSet = AdminUtil
				.getWorkflowModelsConfigurationDataSet(repo);
			String workflowModelsConfigurationDataSetName = workflowModelsConfigurationDataSet
				.getAdaptationName()
				.getStringName();

			for (String wfModelName : wfModelNames)
			{
				// Never publish the configuration data set or any of the wf models that were specified to never publish
				if (!(workflowModelsConfigurationDataSetName.equals(wfModelName)
					|| wfModelsToNotPublish.contains(wfModelName)))
				{
					// Get the master workflows that this workflow is used by
					Set<String> masterWFModelNames = reverseMap.get(wfModelName);
					// If no master workflows then it needs to be published itself
					if (masterWFModelNames == null || masterWFModelNames.isEmpty())
					{
						addToWorkflowPublications(publications, wfModelName);
					}
					// It belongs to a master, or else is listed as having itself as its master
					// (see comment in getReverseMasterWorkflowModelMap)
					else
					{
						// Loop over the master wf models that it's used by
						Iterator<String> masterWFModelIter = masterWFModelNames.iterator();
						while (masterWFModelIter.hasNext())
						{
							// Add both the master wf model and this wf model to the set of wf models to publish for the master.
							// It's a set so we don't need to check if it's already there - it will prevent duplicates
							String masterWFModelName = masterWFModelIter.next();
							Set<String> publicationWFModels = addToWorkflowPublications(
								publications,
								masterWFModelName);
							publicationWFModels.add(wfModelName);
						}
					}
				}
			}

			AdaptationHome workflowModelsDataSpace = AdminUtil.getWorkflowModelsDataSpace(repo);

			// Dev artifacts is not internationalized at this point so just uses default locale
			Locale locale = Locale.getDefault();
			WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
			// Master workflow publications need to be treated differently than others.
			// As with publishing via the UI, you need to publish masters one at a time. Others you can publish at once.
			List<PublicationSpec> masterPublicationSpecs = new ArrayList<>();
			List<PublicationSpec> nonMasterPublicationSpecs = new ArrayList<>();

			// There is no method for looking up if a published process key exists, so we need to get all and keep in a map for easy lookup
			Map<String, PublishedProcessKey> publishedProcessKeys = getPublishedProcessKeyMap(
				wfEngine);
			// Loop over all the publications we need to publish
			for (String publicationName : publications.keySet())
			{
				// Get the data sets (i.e. wf models) to include in the publication
				Set<String> dataSetNames = publications.get(publicationName);
				if (dataSetNames != null)
				{
					PublicationMode publicationMode = null;
					PublishedProcessKey publishedProcessKey = publishedProcessKeys
						.get(publicationName);
					// If its published process key isn't found, then it's a new publication and we always want to publish it
					if (publishedProcessKey == null)
					{
						publicationMode = PublicationMode.NEW;
					}
					// Otherwise it's an update and we only want to publish it if there were changes to any of the data sets
					else
					{
						PublishedProcess publishedProcess = wfEngine
							.getPublishedProcess(publishedProcessKey);
						// Get the snapshot that the publication uses
						AdaptationHome publicationSnapshot = repo
							.lookupHome(publishedProcess.getVersionKey());
						Iterator<String> dataSetNamesIter = dataSetNames.iterator();
						// Loop over the data sets for the publication until we have determined we need to publish
						while (publicationMode == null && dataSetNamesIter.hasNext())
						{
							// If the workflow model has changed then we need to publish in update mode (and end the loop)
							if (hasWorkflowModelChanged(
								dataSetNamesIter.next(),
								workflowModelsDataSpace,
								publicationSnapshot,
								locale))
							{
								publicationMode = PublicationMode.UPDATE;
							}
						}
					}
					// If it's not null, it means we need to publish (either new or update)
					if (publicationMode != null)
					{
						// Create the spec and add it to the appropriate list based on whether it's a master or not
						PublicationSpec spec = new PublicationSpec(
							AdaptationName.forName(publicationName),
							publicationName,
							publicationMode);
						if (masterWorkflowModels.containsKey(publicationName))
						{
							masterPublicationSpecs.add(spec);
						}
						else
						{
							nonMasterPublicationSpecs.add(spec);
						}
					}
				}
			}

			// Loop over all master specs and publish each one separately
			for (PublicationSpec spec : masterPublicationSpecs)
			{
				try
				{
					WorkflowPublisher publisher = WorkflowEngineBridge
						.getWorkflowPublisher(wfEngine);
					publisher.addPublicationSpec(spec);
					publisher.publish();
				}
				catch (OperationException ex)
				{
					registerException(
						ArtifactCategory.WORKFLOW_MODEL_PUBLICATIONS,
						new DevArtifactsException(
							spec.getPublicationName(),
							"Error publishing master workflow model. Must be published manually.",
							ex));
				}
			}
			// Loop over all non master specs and publish them all at once
			if (!nonMasterPublicationSpecs.isEmpty())
			{
				try
				{
					WorkflowPublisher publisher = WorkflowEngineBridge
						.getWorkflowPublisher(wfEngine);
					for (PublicationSpec spec : nonMasterPublicationSpecs)
					{
						publisher.addPublicationSpec(spec);
					}
					publisher.publish();
				}
				catch (OperationException ex)
				{
					StringBuilder bldr = new StringBuilder(
						"Error publishing non-master workflow models. Must be published manually. Workflow models that must be published are: ");
					Iterator<PublicationSpec> iter = nonMasterPublicationSpecs.iterator();
					while (iter.hasNext())
					{
						PublicationSpec spec = iter.next();
						bldr.append(spec.getPublicationName());
						if (iter.hasNext())
						{
							bldr.append(", ");
						}
					}
					bldr.append(".");
					registerException(
						ArtifactCategory.WORKFLOW_MODEL_PUBLICATIONS,
						new DevArtifactsException(null, bldr.toString(), ex));
				}
			}
		}
	}

	private static Set<String> addToWorkflowPublications(
		Map<String, Set<String>> publications,
		String workflowModelName)
	{
		// Get the wf models already specified for this workflow (or create if none specified yet)
		Set<String> publicationWFModels = publications.get(workflowModelName);
		if (publicationWFModels == null)
		{
			publicationWFModels = new HashSet<>();
			publications.put(workflowModelName, publicationWFModels);
		}
		// Add this workflow to the set of wf models to include for this publication and return the list
		publicationWFModels.add(workflowModelName);
		return publicationWFModels;
	}

	// Helper method that takes in a map with key of master wf model, value of set of sub-wf models and reverses it
	// to return a map with key of sub-wf model and value of set of master wf models.
	// If a master workflow is listed with no sub-workflows, then it will be put in the reverseMap as the master workflow
	// to itself. This isn't accurate, but helps with the processing so that we know that we have to publish it on its own,
	// possibly in addition to being part of another master
	private static Map<String, Set<String>> getReverseMasterWorkflowModelMap(
		Map<String, Set<String>> masterWFModels)
	{
		Map<String, Set<String>> reverseMap = new LinkedHashMap<>();
		Iterator<String> masterIter = masterWFModels.keySet().iterator();
		while (masterIter.hasNext())
		{
			String masterWFModelName = masterIter.next();
			Set<String> subWFModels = masterWFModels.get(masterWFModelName);
			if (subWFModels.isEmpty())
			{
				Set<String> reverseSet = reverseMap.get(masterWFModelName);
				if (reverseSet == null)
				{
					reverseSet = new HashSet<>();
					reverseMap.put(masterWFModelName, reverseSet);
				}
				reverseSet.add(masterWFModelName);
			}
			else
			{
				Iterator<String> subIter = subWFModels.iterator();
				while (subIter.hasNext())
				{
					String subWFModelName = subIter.next();
					Set<String> reverseSet = reverseMap.get(subWFModelName);
					if (reverseSet == null)
					{
						reverseSet = new HashSet<>();
						reverseMap.put(subWFModelName, reverseSet);
					}
					reverseSet.add(masterWFModelName);
				}
			}
		}
		return reverseMap;
	}

	// This just puts all the enabled published process keys into a map.
	// We do this so that we can check if it exists since simply calling getPublishedProcess and passing in a non-existent key throws an exception.
	private static Map<String, PublishedProcessKey> getPublishedProcessKeyMap(
		WorkflowEngine workflowEngine)
	{
		Map<String, PublishedProcessKey> map = new LinkedHashMap<>();

		List<PublishedProcessKey> publishedProcessKeys = workflowEngine.getPublishedKeys();
		for (PublishedProcessKey key : publishedProcessKeys)
		{
			map.put(key.getName(), key);
		}
		return map;
	}

	private static boolean hasWorkflowModelChanged(
		String dataSetName,
		AdaptationHome workflowModelsDataSpace,
		AdaptationHome publicationSnapshot,
		Locale locale)
	{
		AdaptationReference adaptationRef = AdaptationReference.forPersistentName(dataSetName);
		Adaptation publicationDataSet = publicationSnapshot.findAdaptationOrNull(adaptationRef);
		if (publicationDataSet == null)
		{
			return true;
		}

		Adaptation dataSet = workflowModelsDataSpace.findAdaptationOrNull(adaptationRef);
		// Compare label, description, and owner because these aren't included in data set comparison
		if (!Objects.equals(publicationDataSet.getLabel(locale), dataSet.getLabel(locale))
			|| !Objects
				.equals(publicationDataSet.getDescription(locale), dataSet.getDescription(locale))
			|| !Objects.equals(publicationDataSet.getOwner(), dataSet.getOwner()))
		{
			return true;
		}

		// Compare the data sets and it's changed if there are any differences
		DifferenceBetweenInstances wfDiffs = DifferenceHelper
			.compareInstances(publicationDataSet, dataSet, false);
		return !wfDiffs.isEmpty();
	}

	@Override
	protected void processPerspectivesData(Repository repo, Session session, final File folder)
		throws OperationException
	{
		super.processPerspectivesData(repo, session, folder);

		if (config.isProcessPerspectivesData())
		{
			Adaptation perspectivesDataSet = AdminUtil.getPerspectivesDataSet(repo);

			// After importing, need to optimize the perspectives tree so that values that are identical
			// to what's in the parent perspective get set to inherited
			AdaptationTreeOptimizerSpec_RemoveDuplicates optimizer = new AdaptationTreeOptimizerSpec_RemoveDuplicates(
				perspectivesDataSet,
				true);
			AdaptationTreeOptimizerIterator iter = optimizer.createOptimizerIterator(session);
			try
			{
				iter.executeAll();
			}
			catch (OperationException ex)
			{
				registerException(
					ArtifactCategory.PERSPECTIVES,
					new DevArtifactsException(null, "Error optimizing perspectives.", ex));
				return;
			}

			if (!DevArtifactsConstants.TENANT_POLICY_MULTI.equals(config.getTenantPolicy()))
			{
				String windowName = config.getPerspectiveWindowName();
				if (windowName != null)
				{
					//different environments may want to have custom window name to disambiguate
					Path welcomePath = AdminUtil.getPerspectivesErgonomicsWelcomeMessagePath();
					Path winNamePath = AdminUtil.getPerspectivesErgonomicsWindowNamePath();
					//using non-public API as I can see no other way to update the label...
					Label welcome = (Label) perspectivesDataSet.get(welcomePath);
					Label window = (Label) perspectivesDataSet.get(winNamePath);
					Locale locale = session.getLocale();
					LabelForLocale lflWelcome = welcome == null ? null
						: welcome.getLocalizedDocumentation(locale);
					LabelForLocale lflWindow = window == null ? null
						: window.getLocalizedDocumentation(locale);
					String defWelcome = lflWelcome == null ? null : lflWelcome.getLabel();
					String defWindow = lflWindow == null ? null : lflWindow.getLabel();
					ModifyValuesProcedure mvp = new ModifyValuesProcedure();
					mvp.setAllPrivileges(true);
					mvp.setAdaptation(perspectivesDataSet);
					//if the existing welcome is null or the existing window name is null
					//or they are the same, assume we should update both
					if (defWelcome == null || defWindow == null || defWindow.equals(defWelcome))
					{
						if (welcome == null)
						{
							welcome = new Label();
						}
						welcome.setLocalizedDocumentations(locale, windowName);
						mvp.setValue(welcomePath, welcome);
					}
					if (window == null)
					{
						window = new Label();
					}
					window.setLocalizedDocumentations(locale, windowName);
					mvp.setValue(winNamePath, window);
					try
					{
						mvp.execute(session);
					}
					catch (OperationException ex)
					{
						registerException(
							ArtifactCategory.PERSPECTIVES,
							new DevArtifactsException(
								null,
								"Error processing window name: " + windowName,
								ex));
					}
				}
			}
		}
	}

	@Override
	protected void doProcessPerspectivesDataSet(
		ProcedureContext pContext,
		File folder,
		String dataSetName,
		boolean root)
		throws DevArtifactsException
	{
		AdaptationName perspectiveName = AdaptationName.forName(dataSetName);
		AdaptationHome dataSpace = pContext.getAdaptationHome();
		Properties props = processDataSetDataPropertiesFile(
			pContext.getSession().getLocale(),
			null,
			false,
			dataSpace,
			perspectiveName,
			folder,
			PERSPECTIVE_PREFIX);

		Adaptation dataSet;

		// If this is a perspective for this tenant, then either get it from EBX
		// or create it from the properties file, and process the properties
		if (shouldProcessPerspectiveDataSet(dataSetName, root))
		{
			try
			{
				dataSet = getOrCreateDataSetFromProperties(pContext, dataSetName, props, null);
			}
			catch (OperationException ex)
			{
				throw new DevArtifactsException(
					getFullPropertiesArtifactFilename(folder, PERSPECTIVE_PREFIX + dataSetName),
					ex);
			}

			doProcessPerspectivesDataGroups(pContext, folder, dataSet);

			processDataSetDataProperties(
				pContext,
				props,
				false,
				dataSet,
				folder,
				PERSPECTIVE_PREFIX);
		}
		// If this isn't a perspective for this tenant, then we still need to potentially process its children
		// so get the data set from EBX
		else
		{
			// Either it is a parent perspective, in which case this tenant depends on it,
			// or it was found as a child in EBX when navigating the hierarchy.
			// Either way, it should exist in EBX.
			AdaptationReference adaptationRef = AdaptationReference.forPersistentName(dataSetName);
			dataSet = dataSpace.findAdaptationOrNull(adaptationRef);
			if (dataSet == null)
			{
				throw new DevArtifactsException(
					getFullPropertiesArtifactFilename(folder, PERSPECTIVE_PREFIX + dataSetName),
					"Perspective not found in EBX.");
			}
		}

		processPerspectivesChildDataSets(pContext, folder, dataSetName, dataSet, props);
	}

	private void processPerspectivesChildDataSets(
		ProcedureContext pContext,
		File folder,
		String parentDataSetName,
		Adaptation parentDataSet,
		Properties parentDataSetProperties)
		throws DevArtifactsException
	{
		Set<String> childPerspectiveNames = new HashSet<>();

		// If there's a properties file, need to collect all child perspectives that
		// are specified there
		if (parentDataSetProperties != null && !parentDataSetProperties.isEmpty())
		{
			String childDataSetsStr = getPropertyValueOrNull(
				parentDataSetProperties,
				DATA_SET_PROPERTY_CHILD_DATA_SETS);
			if (childDataSetsStr != null)
			{
				String[] childDataSetNames = childDataSetsStr
					.split(DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR);
				if (childDataSetNames.length > 0)
				{
					// If children were specified in the properties file, but the parent data set wasn't found
					// in EBX, then that's an issue, because it should exist by now.
					if (parentDataSet == null)
					{
						throw new DevArtifactsException(
							getFullPropertiesArtifactFilename(
								folder,
								PERSPECTIVE_PREFIX + parentDataSetName),
							"Can't find perspective in EBX in order to process its child perspectives.");
					}
					// Add all the children to the set of names
					childPerspectiveNames.addAll(Arrays.asList(childDataSetNames));
				}
			}
		}

		// In addition to those specified in the properties file, need to also collect all child perspectives
		// that are in EBX. This is because there could be perspectives belonging to other tenants that have
		// children (or grandchildren, etc) belonging to this tenant. So here we are collecting all child
		// data set names from EBX and adding them to the set. Since it's a set, there won't be duplicates if
		// it was already found in the properties file.
		AdaptationHome perspectivesDataSpace = parentDataSet.getHome();
		List<Adaptation> childDataSets = perspectivesDataSpace.findAllChildren(parentDataSet);
		Iterator<Adaptation> iter = childDataSets.iterator();
		while (iter.hasNext())
		{
			Adaptation childDataSet = iter.next();
			childPerspectiveNames.add(childDataSet.getAdaptationName().getStringName());
		}

		// Now process each child perspective
		for (String childPerspectiveName : childPerspectiveNames)
		{
			doProcessPerspectivesDataSet(pContext, folder, childPerspectiveName, false);
		}
	}

	//	@Override
	//	protected void processAddonAdixDataExchangeDataSpaceData(ProcedureContext pContext, File folder)
	//		throws DevArtifactsException
	//	{
	//		boolean triggerActivation = pContext.isTriggerActivation();
	//		pContext.setTriggerActivation(false);
	//
	//		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
	//		ImportSpecMode importMode = importConfig.getImportMode();
	//		importConfig.setImportMode(ImportSpecMode.UPDATE);
	//
	//		try
	//		{
	//			super.processAddonAdixDataExchangeDataSpaceData(pContext, folder);
	//		}
	//		finally
	//		{
	//			importConfig.setImportMode(importMode);
	//			pContext.setTriggerActivation(triggerActivation);
	//		}
	//	}

	//	@Override
	//	protected void processAdixDataExchangeTenantTables(
	//		ProcedureContext pContext,
	//		Adaptation adixDataExchangeDataSet,
	//		File folder,
	//		AddonAdixDevArtifactsCache cache)
	//		throws DevArtifactsException
	//	{
	//		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
	//		ImportSpecMode importMode = importConfig.getImportMode();
	//		if (ImportSpecMode.REPLACE.equals(importMode))
	//		{
	//			Set<String> applicationPKs = cache.getApplicationPrimaryKeys();
	//			for (String applicationPK : applicationPKs)
	//			{
	//
	//			}
	//		}
	//	}

	private void importArchive(ProcedureContext pContext, File srcFile) throws DevArtifactsException
	{
		if (!srcFile.exists())
		{
			throw new DevArtifactsException(
				srcFile.getAbsolutePath(),
				"Archive file does not exist.");
		}

		final ArchiveImportSpec spec = new ArchiveImportSpec();
		Archive archive = Archive.forFile(srcFile);
		spec.setArchive(archive);

		boolean allPrivileges = pContext.isAllPrivileges();
		pContext.setAllPrivileges(true);
		try
		{
			pContext.doImportArchive(spec);
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(srcFile.getAbsolutePath(), ex);
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
	}

	// Import the users again in update mode.
	// This is because the password last update date gets changed when importing a new record
	// but doing the import again will update it to the original value from the file.
	private void fixUserPasswordUpdateField(
		ProcedureContext pContext,
		AdaptationTable usersTable,
		File folder,
		String usersFilename,
		ArtifactFileModifier modifier)
		throws DevArtifactsException
	{
		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		ImportSpecMode importMode = importConfig.getImportMode();
		importConfig.setImportMode(ImportSpecMode.UPDATE);
		try
		{
			importIntoTable(pContext, usersTable, folder, usersFilename, false, false, modifier);
		}
		finally
		{
			importConfig.setImportMode(importMode);
		}
	}

	private static Adaptation createRootDataSet(
		ProcedureContext pContext,
		AdaptationReference dataSetName,
		SchemaLocation schemaLocation)
		throws OperationException
	{
		Adaptation dataSet;
		boolean allPrivileges = pContext.isAllPrivileges();
		try
		{
			pContext.setAllPrivileges(true);
			dataSet = pContext.doCreateRoot(
				schemaLocation,
				dataSetName,
				pContext.getSession().getUserReference());
		}
		// doCreateRoot declares that it throws an IllegalArgumentException when the input doesn't conform to
		// the right format
		catch (IllegalArgumentException ex)
		{
			throw OperationException
				.createError("Error creating root data set " + dataSetName + ".", ex);
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
		return dataSet;
	}

	private static Adaptation createChildDataSet(
		ProcedureContext pContext,
		AdaptationReference dataSetName,
		AdaptationName parentDataSetName)
		throws OperationException
	{
		Adaptation dataSet;
		boolean allPrivileges = pContext.isAllPrivileges();
		try
		{
			pContext.setAllPrivileges(true);
			dataSet = pContext.doCreateChild(
				parentDataSetName,
				dataSetName,
				pContext.getSession().getUserReference());
		}
		// doCreateChild declares that it throws an IllegalArgumentException when the input doesn't conform to
		// the right format
		catch (IllegalArgumentException ex)
		{
			throw OperationException.createError(
				"Error creating child data set " + dataSetName + " of parent " + parentDataSetName
					+ ".",
				ex);
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
		return dataSet;
	}

	// A filter that only accepts users that are in all of the given sets
	private class UsersInSetsAdaptationFilter implements AdaptationFilter
	{
		private Path userPath;
		private List<Set<String>> usersSets;

		public UsersInSetsAdaptationFilter(Path userPath, List<Set<String>> usersSets)
		{
			this.userPath = userPath;
			this.usersSets = usersSets;
		}

		@Override
		public boolean accept(Adaptation record)
		{
			String username = record.getString(userPath);
			boolean found = true;
			Iterator<Set<String>> iter = usersSets.iterator();
			// Keep looping until it's not found in one of the sets
			while (found && iter.hasNext())
			{
				Set<String> userSet = iter.next();
				found = userSet.contains(username);
			}
			return found;
		}
	}
}