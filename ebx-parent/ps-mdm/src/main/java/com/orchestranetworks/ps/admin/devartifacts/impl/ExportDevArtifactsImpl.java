package com.orchestranetworks.ps.admin.devartifacts.impl;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.org.apache.commons.io.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.adaptationfilter.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.modifier.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Implementation for exporting of the dev artifacts
 */
public class ExportDevArtifactsImpl extends DevArtifactsBase
{
	public static final String PARAM_DOWNLOAD_TO_LOCAL = "downloadToLocal";

	public ExportDevArtifactsImpl()
	{
		super();
	}

	public ExportDevArtifactsImpl(ExportDevArtifactsConfig config)
	{
		super(config);
	}

	@Override
	public void processArtifacts(Repository repo, Session session, boolean environmentCopy)
		throws OperationException
	{
		initArtifactsFolders(environmentCopy);
		super.processArtifacts(repo, session, environmentCopy);
	}

	@Override
	protected void processUsersRolesTable(
		ProcedureContext pContext,
		AdaptationTable usersRolesTable,
		Set<String> restrictedRoleNames,
		File folder,
		String filePrefix)
		throws DevArtifactsException
	{
		Set<String> usersInRolesPredicateSet = new HashSet<>();
		// If restricting to particular roles
		if (restrictedRoleNames != null && !restrictedRoleNames.isEmpty())
		{
			// Filter Users Roles table to only those with a role in the given set
			Request usersRolesMatchingRoleInSetRequest = usersRolesTable.createRequest();
			usersRolesMatchingRoleInSetRequest.setSpecificFilter(
				new FieldValueInCollectionAdaptationFilter(
					AdminUtil.getDirectoryUsersRolesRolePath(),
					restrictedRoleNames));
			RequestResult requestResult = usersRolesMatchingRoleInSetRequest.execute();
			try
			{
				// Loop through the results and add the user from each row to the usersInRolesPredicateSet
				for (Adaptation userRoleMatchingRoleInSet; (userRoleMatchingRoleInSet = requestResult
					.nextAdaptation()) != null;)
				{
					usersInRolesPredicateSet.add(
						userRoleMatchingRoleInSet
							.getString(AdminUtil.getDirectoryUsersRolesUserPath()));
				}
			}
			finally
			{
				requestResult.close();
			}
		}

		AdaptationTable usersTable = AdminUtil
			.getDirectoryUsersTable(usersRolesTable.getContainerAdaptation());
		String usersRolesPredicate = config.getUsersRolesPredicate();
		String usersTableFilename = filePrefix + getTableFilename(usersTable);
		String usersRolesTableFilename = filePrefix + getTableFilename(usersRolesTable);

		AdaptationFilter usersRolesFilter = null;
		AdaptationFilter usersFilter = null;

		// It's unlikely that all users would want to be processed, but if for some reason they wish to,
		// the filter will be null so they'll all be processed
		String tenantRolesPredicate = config.getTenantRolesPredicate();
		if (!(tenantRolesPredicate == null && usersRolesPredicate == null))
		{
			// Use a set to avoid duplicates
			HashSet<String> users = new HashSet<>();

			// If no usersRolesPredicate was specified, then this will just return all users
			// (but not likely)
			RequestResult requestResult = usersRolesTable.createRequestResult(usersRolesPredicate);
			try
			{
				// Put users from every row that matches the predicate and has a role in the roles predicate
				// (if specified) into the set
				Adaptation usersRolesRecord;
				while ((usersRolesRecord = requestResult.nextAdaptation()) != null)
				{
					String user = usersRolesRecord
						.getString(AdminUtil.getDirectoryUsersRolesUserPath());
					if (tenantRolesPredicate == null || usersInRolesPredicateSet.contains(user))
					{
						users.add(user);
					}
				}
			}
			finally
			{
				requestResult.close();
			}

			// Create filters that only process the users we got from the query.
			// (Not same as simply processing the query because we want all other rows for those
			// same users)
			usersRolesFilter = getUsersFilter(AdminUtil.getDirectoryUsersRolesUserPath(), users);
			usersFilter = getUsersFilter(AdminUtil.getDirectoryUsersLoginPath(), users);
		}

		// usersRoles table
		processTable(
			pContext,
			usersRolesTable,
			folder,
			usersRolesTableFilename,
			null,
			usersRolesFilter,
			null);

		// users table
		processTable(pContext, usersTable, folder, usersTableFilename, null, usersFilter, null);
	}

	@Override
	protected void processTable(
		ProcedureContext pContext,
		AdaptationTable table,
		File folder,
		String filename,
		String predicate,
		AdaptationFilter filter,
		ArtifactFileModifier artifactFileModifier)
		throws DevArtifactsException
	{
		File tmpFile;
		ExportSpec exportSpec;
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			exportSpec = DevArtifactsUtil.createExportSpec(folder, filename);
		}
		// If modifying artifact on export, need to create a temp file
		else
		{
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
			exportSpec = DevArtifactsUtil.createTempFileExportSpec(tmpFile);
		}

		try
		{
			DevArtifactsUtil.exportTable(
				pContext,
				table,
				exportSpec,
				config.getLineSeparator(),
				predicate,
				filter);
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(getFullXMLArtifactFilename(folder, filename), ex);
		}

		// If modifying artifact on export, need to read in the temp file, modifying it and
		// writing to the real file, then delete the temp file
		if (artifactFileModifier != null)
		{
			try
			{
				copyTempFileIntoRealFile(artifactFileModifier, tmpFile, folder, filename);
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					new StringBuilder("Error reading from temp file ")
						.append(tmpFile.getAbsolutePath())
						.append(" and writing into file.")
						.toString(),
					ex);
			}
			finally
			{
				tmpFile.delete();
			}
		}
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
		ExportSpec exportSpec;
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			exportSpec = DevArtifactsUtil.createExportSpec(folder, filename);
		}
		// If modifying artifact on export, need to create a temp file
		else
		{
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
			exportSpec = DevArtifactsUtil.createTempFileExportSpec(tmpFile);
		}
		exportSpec.setSourceAdaptation(dataSet);
		exportSpec.setSelection(groupNode);

		try
		{
			DevArtifactsUtil.doExport(pContext, exportSpec, config.getLineSeparator());
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(getFullXMLArtifactFilename(folder, filename), ex);
		}

		// If modifying artifact on export, need to read in the temp file, modifying it and
		// writing to the real file, then delete the temp file
		if (artifactFileModifier != null)
		{
			try
			{
				copyTempFileIntoRealFile(artifactFileModifier, tmpFile, folder, filename);
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					new StringBuilder("Error reading from temp file ")
						.append(tmpFile.getAbsolutePath())
						.append(" and writing into file.")
						.toString(),
					ex);
			}
			finally
			{
				tmpFile.delete();
			}
		}
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
		ExportSpec spec;
		if (artifactFileModifier == null)
		{
			tmpFile = null;
			spec = DevArtifactsUtil.createExportSpec(folder, filename);
		}
		// If modifying artifact on export, need to create a temp file
		else
		{
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
			spec = DevArtifactsUtil.createTempFileExportSpec(tmpFile);
		}

		spec.setSourceAdaptation(dataSet);
		try
		{
			DevArtifactsUtil.doExport(pContext, spec, config.getLineSeparator());
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(getFullXMLArtifactFilename(folder, filename), ex);
		}

		// If modifying artifact on export, need to read in the temp file, modifying it and
		// writing to the real file, then delete the temp file
		if (artifactFileModifier != null)
		{
			try
			{
				copyTempFileIntoRealFile(artifactFileModifier, tmpFile, folder, filename);
			}
			catch (IOException ex)
			{
				throw new DevArtifactsException(
					getFullXMLArtifactFilename(folder, filename),
					new StringBuilder("Error reading from temp file ")
						.append(tmpFile.getAbsolutePath())
						.append(" and writing into file.")
						.toString(),
					ex);
			}
			finally
			{
				tmpFile.delete();
			}
		}
	}

	private static void copyTempFileIntoRealFile(
		ArtifactFileModifier artifactFileModifier,
		File tempFile,
		File folder,
		String filename)
		throws IOException
	{
		File file = new File(folder, filename + ".xml");
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		try
		{
			modifyArtifactFile(tempFile, writer, artifactFileModifier, true);
		}
		finally
		{
			writer.close();
		}
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
		Adaptation dataSet = dataSpace.findAdaptationOrNull(dataSetName);
		String label = dataSet.getLabel(locale);
		if (label == null)
		{
			label = "";
		}
		String description = dataSet.getDescription(locale);
		if (description == null)
		{
			description = "";
		}
		Profile owner = dataSet.getOwner();

		Properties props = new Properties();
		props.put(DATA_SET_PROPERTY_LABEL, label);
		props.put(DATA_SET_PROPERTY_DESCRIPTION, description);
		props.put(DATA_SET_PROPERTY_OWNER, owner.format());

		Adaptation parentDataSet = dataSet.getParent();
		String parentDataSetName = parentDataSet == null ? ""
			: parentDataSet.getAdaptationName().getStringName();
		props.put(DATA_SET_PROPERTY_PARENT_DATA_SET, parentDataSetName);

		Iterator<Adaptation> childDataSetIter = dataSpace.findAllChildren(dataSet).iterator();
		StringBuilder bldr = new StringBuilder();
		while (childDataSetIter.hasNext())
		{
			Adaptation childDataSet = childDataSetIter.next();
			bldr.append(childDataSet.getAdaptationName().getStringName());
			if (childDataSetIter.hasNext())
			{
				bldr.append(DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR);
			}
		}
		props.put(DATA_SET_PROPERTY_CHILD_DATA_SETS, bldr.toString());
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
		StringBuilder bldr = new StringBuilder(filePrefix);
		if (qualifyDataSet && config.isQualifyDataSetAndTableFileNames())
		{
			bldr.append(dataSet.getHome().getKey().getName()).append(FILE_NAME_SEPARATOR);
		}
		bldr.append(dataSet.getAdaptationName().getStringName()).append(DATA_SET_PROPERTIES_SUFFIX);
		String filename = bldr.toString();
		File propertiesFile = new File(folder, filename);

		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));

			// We can't use Properties methods to write it out because we need to specify the line
			// separator
			try
			{
				@SuppressWarnings("unchecked")
				Enumeration<String> keys = (Enumeration<String>) props.propertyNames();
				while (keys.hasMoreElements())
				{
					String key = keys.nextElement();
					writer.append(key);
					writer.append("=");
					writer.append(props.getProperty(key));
					writer.append(config.getLineSeparator());
				}
				writer.flush();
			}
			finally
			{
				writer.close();
			}
		}
		catch (IOException ex)
		{
			throw new DevArtifactsException(getFullXMLArtifactFilename(folder, filename), ex);
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
		AdaptationName wfModelName = AdaptationName.forName(wfModel);
		AdaptationHome wfDataSpace = pContext.getAdaptationHome();
		Adaptation wfDataSet = wfDataSpace.findAdaptationOrNull(wfModelName);
		processDataSetDataXML(pContext, wfDataSet, folder, filePrefix, null, false);

		Properties props = processDataSetDataPropertiesFile(
			pContext.getSession().getLocale(),
			null,
			false,
			wfDataSpace,
			wfModelName,
			folder,
			filePrefix);
		processDataSetDataProperties(pContext, props, false, wfDataSet, folder, filePrefix);
	}

	@Override
	protected void doProcessPerspectivesDataSet(
		ProcedureContext pContext,
		File folder,
		String dataSetName,
		boolean root)
		throws DevArtifactsException
	{
		AdaptationHome dataSpace = pContext.getAdaptationHome();
		Adaptation dataSet = dataSpace.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		if (shouldProcessPerspectiveDataSet(dataSetName, root))
		{
			doProcessPerspectivesDataGroups(pContext, folder, dataSet);

			Properties props = doProcessPerspectivePropertiesFile(
				pContext,
				dataSet.getAdaptationName(),
				folder);
			processDataSetDataProperties(
				pContext,
				props,
				false,
				dataSet,
				folder,
				PERSPECTIVE_PREFIX);

			// Also output its parent data set, if the parent isn't part of this tenant.
			// Note that this can mean the same parent is processed multiple times if there
			// are multiple direct children that are part of this tenant, but while being
			// less efficient, this shouldn't really matter
			Adaptation parentDataSet = dataSet.getParent();
			AdaptationName parentDataSetName = (parentDataSet == null) ? null
				: parentDataSet.getAdaptationName();
			if (parentDataSetName != null
				&& !shouldProcessPerspectiveDataSet(parentDataSetName.getStringName(), false))
			{
				Properties parentProps = doProcessPerspectivePropertiesFile(
					pContext,
					parentDataSetName,
					folder);
				processDataSetDataProperties(
					pContext,
					parentProps,
					false,
					parentDataSet,
					folder,
					PERSPECTIVE_PREFIX);
			}
		}
		// Regardless of if this perspective should be processed with this tenant,
		// process its children. Even though this perspective isn't part of the tenant
		// doesn't mean it doesn't have a child perspective that's part of the tenant.
		processPerspectivesChildDataSets(pContext, folder, dataSet);
	}

	private Properties doProcessPerspectivePropertiesFile(
		ProcedureContext pContext,
		AdaptationName dataSetName,
		File folder)
		throws DevArtifactsException
	{
		Properties props = processDataSetDataPropertiesFile(
			pContext.getSession().getLocale(),
			null,
			false,
			pContext.getAdaptationHome(),
			dataSetName,
			folder,
			PERSPECTIVE_PREFIX);

		// If not single tenant, need to eliminate the child perspectives from the list
		// within the properties object that aren't associated with this tenant.
		// Simplest way is to just get the value that was already put into the properties file,
		// which will contain all of them, parse it for the right ones, and overwrite it with just those.
		if (!DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
		{
			String childDataSetsStr = getPropertyValueOrNull(
				props,
				DATA_SET_PROPERTY_CHILD_DATA_SETS);
			if (childDataSetsStr != null)
			{
				String[] childPerspectives = childDataSetsStr
					.split(DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR_ESCAPED);
				List<String> tenantChildPerspectives = new ArrayList<>();
				for (String childPerspective : childPerspectives)
				{
					if (shouldProcessPerspectiveDataSet(childPerspective, false))
					{
						tenantChildPerspectives.add(childPerspective);
					}
				}
				Iterator<String> iter = tenantChildPerspectives.iterator();
				StringBuilder bldr = new StringBuilder();
				while (iter.hasNext())
				{
					String tenantChildPerspective = iter.next();
					bldr.append(tenantChildPerspective);
					if (iter.hasNext())
					{
						bldr.append(DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR);
					}
				}
				props.put(DATA_SET_PROPERTY_CHILD_DATA_SETS, bldr.toString());
			}
		}
		return props;
	}

	private void processPerspectivesChildDataSets(
		ProcedureContext pContext,
		File folder,
		Adaptation parentDataSet)
		throws DevArtifactsException
	{
		AdaptationHome perspectivesDataSpace = parentDataSet.getHome();
		List<Adaptation> childPerspectivesDataSets = perspectivesDataSpace
			.findAllChildren(parentDataSet);
		for (Adaptation childPerspectivesDataSet : childPerspectivesDataSets)
		{
			doProcessPerspectivesDataSet(
				pContext,
				folder,
				childPerspectivesDataSet.getAdaptationName().getStringName(),
				false);
		}
	}

	@Override
	protected void doProcessDMA(ProcedureContext pContext, File folder) throws DevArtifactsException
	{
		AdaptationHome dmaDataSpace = pContext.getAdaptationHome();
		Map<AdaptationName, boolean[]> dmaDataSetMap = getDMADataSetMap(
			dmaDataSpace,
			config.getTenantPolicy(),
			config.getModules());
		if (!dmaDataSetMap.isEmpty())
		{
			File destFile = new File(
				config.getCopyEnvironmentDMAFolder(),
				dmaDataSpace.getKey().getName() + ".ebx");
			exportArchive(pContext, dmaDataSetMap, destFile);
		}
	}

	private void exportArchive(
		ProcedureContext pContext,
		Map<AdaptationName, boolean[]> dataSetMap,
		File destFile)
		throws DevArtifactsException
	{
		final ArchiveExportSpec spec = new ArchiveExportSpec();
		Archive archive = Archive.forFile(destFile);
		spec.setArchive(archive);
		if (!dataSetMap.isEmpty())
		{
			for (Map.Entry<AdaptationName, boolean[]> entry : dataSetMap.entrySet())
			{
				AdaptationName dataSetName = entry.getKey();
				InstanceContentSpec instanceContentSpec = new InstanceContentSpec();
				boolean[] booleanArr = entry.getValue();
				instanceContentSpec.setIncludeValues(booleanArr[0]);
				instanceContentSpec.setIncludeAllDescendants(booleanArr[1]);
				spec.addInstance(dataSetName, instanceContentSpec);
			}
		}

		boolean allPrivileges = pContext.isAllPrivileges();
		pContext.setAllPrivileges(true);
		try
		{
			pContext.doExportArchive(spec);
		}
		catch (OperationException ex)
		{
			throw new DevArtifactsException(destFile.getAbsolutePath(), ex);
		}
		finally
		{
			pContext.setAllPrivileges(allPrivileges);
		}
	}

	private static Map<AdaptationName, boolean[]> getDMADataSetMap(
		AdaptationHome dmaDataSpace,
		String tenantPolicy,
		List<String> modules)
	{
		boolean[] booleanArr = { true, false };

		List<Adaptation> dmaDataSets = dmaDataSpace.findAllRoots();
		Map<AdaptationName, boolean[]> dmaDataSetMap = new LinkedHashMap<>();
		for (Adaptation dmaDataSet : dmaDataSets)
		{
			// Always include the model if it's single tenant. Otherwise, only if it has the right module.
			boolean include = DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(tenantPolicy);
			if (!include)
			{
				SchemaLocation schemaLocation = dmaDataSet.getSchemaLocation();
				if (schemaLocation != null)
				{
					// Check the module name of the data set itself.
					// The DMA extensions are in different data sets, but the export will know
					// to include them as long as we include the main data set.
					String dmaDataSetModuleName = schemaLocation.getModuleName();
					if (AdminUtil.getDMAModuleName().equals(dmaDataSetModuleName))
					{
						String module = dmaDataSet
							.getString(AdminUtil.getDMASchemaModuleNamePath());
						include = (module != null && modules.contains(module));
					}
				}
			}
			if (include)
			{
				dmaDataSetMap.put(dmaDataSet.getAdaptationName(), booleanArr);
			}
		}
		return dmaDataSetMap;
	}

	private void initArtifactsFolders(boolean environmentCopy) throws OperationException
	{
		List<File> folders = new ArrayList<>();
		if (environmentCopy)
		{
			File copyEnvFolder = config.getCopyEnvironmentFolder();
			if (copyEnvFolder.exists())
			{
				// Clear the contents of copyEnv if it exists
				try
				{
					clearFolder(copyEnvFolder);
				}
				catch (IOException ex)
				{
					throw OperationException.createError(
						"Error clearing contents of copy environment folder "
							+ copyEnvFolder.getAbsolutePath() + ".",
						ex);
				}
			}
			else
			{
				folders.add(copyEnvFolder);
			}
			folders.add(config.getCopyEnvironmentDataFolder());
			folders.add(config.getCopyEnvironmentPermissionsFolder());
			folders.add(config.getCopyEnvironmentWorkflowsFolder());
			folders.add(config.getCopyEnvironmentAdminFolder());
			folders.add(config.getCopyEnvironmentPerspectivesFolder());
			if (config.isProcessAddonAdixData())
			{
				folders.add(config.getCopyEnvironmentAddonAdixFolder());
			}
			if (config.isProcessAddonDamaData())
			{
				folders.add(config.getCopyEnvironmentAddonDamaFolder());
			}
			if (config.isProcessAddonDintData())
			{
				folders.add(config.getCopyEnvironmentAddonDintFolder());
			}
			if (config.isProcessAddonDpraData())
			{
				folders.add(config.getCopyEnvironmentAddonDpraFolder());
			}
			if (config.isProcessAddonDqidData())
			{
				folders.add(config.getCopyEnvironmentAddonDqidFolder());
			}
			if (config.isProcessAddonMameData())
			{
				folders.add(config.getCopyEnvironmentAddonMameFolder());
			}
			folders.add(config.getCopyEnvironmentDMAFolder());
		}
		else
		{
			folders.add(config.getDataFolder());
			folders.add(config.getPermissionsFolder());
			folders.add(config.getWorkflowsFolder());
			folders.add(config.getAdminFolder());
			folders.add(config.getPerspectivesFolder());
			if (config.isProcessAddonAdixData())
			{
				folders.add(config.getAddonAdixFolder());
			}
			if (config.isProcessAddonDamaData())
			{
				folders.add(config.getAddonDamaFolder());
			}
			if (config.isProcessAddonDintData())
			{
				folders.add(config.getAddonDintFolder());
			}
			if (config.isProcessAddonDpraData())
			{
				folders.add(config.getAddonDpraFolder());
			}
			if (config.isProcessAddonDqidData())
			{
				folders.add(config.getAddonDqidFolder());
			}
			if (config.isProcessAddonMameData())
			{
				folders.add(config.getAddonMameFolder());
			}
		}
		boolean success;
		try
		{
			// For backwards-compatibility, check if it's not null. Eventually, it will be an error if it's null.
			success = (config.getArtifactsFolder() == null
				|| createFolder(config.getArtifactsFolder()));

			Iterator<File> iter = folders.iterator();
			while (success && iter.hasNext())
			{
				File folder = iter.next();
				success = createFolder(folder);
			}
		}
		catch (IOException ex)
		{
			throw OperationException.createError("Error creating artifacts folders.", ex);
		}

		if (!success)
		{
			throw OperationException.createError("Error creating artifacts folders.");
		}
	}

	private static boolean createFolder(File folder) throws IOException
	{
		// Return true if it already exists, otherwise return result
		// of creating the folder
		return folder.exists() || folder.mkdirs();
	}

	private void clearFolder(File folder) throws IOException
	{
		File[] files = folder.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					FileUtils.deleteDirectory(file);
				}
				else
				{
					file.delete();
				}
			}
		}
	}
}
