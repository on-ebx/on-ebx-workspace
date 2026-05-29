package com.orchestranetworks.ps.admin.devartifacts.config;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

/**
 * A class to help read Dev Artifacts values from the properties file
 */
public class DevArtifactsPropertyFileHelper extends PropertyFileHelper
	implements DevArtifactsConstants
{
	public static final String PARAM_PROPERTIES_FILE = "propFile";
	public static final String PARAM_PROPERTIES_FILE_SYSTEM_PROPERTY = "propFileSysProp";

	public static final String DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY = "dev.artifacts.properties";
	public static final String DEFAULT_PROPERTIES_FILE = initDefaultPropertiesFile();

	protected static final int PROPERTY_TOKEN_INDEX_PARENT_DATA_SPACE_NAME = 1;
	protected static final int PROPERTY_TOKEN_INDEX_DATA_SPACE_LABEL = 2;
	protected static final int PROPERTY_TOKEN_INDEX_DATA_SPACE_OWNER = 3;
	/** Relational mode no longer exists in EBX6 */
	@Deprecated
	protected static final int PROPERTY_TOKEN_INDEX_RELATIONAL = 4;
	protected static final int PROPERTY_TOKEN_INDEX_TABLE_PREDICATE = 3;

	private static final boolean DEFAULT_VALUE_CREATE_DATA_SPACES = true;
	private static final boolean DEFAULT_VALUE_CREATE_DATA_SETS = true;

	private static final String MATCHES_NONE_ROLES_PREDICATE = "osd:is-null("
		+ DirectoryPaths._Directory_Roles._Name.format() + ")";

	private static final String MESSAGE_TEMPLATE_RANGES_SEPARATOR = ",";
	private static final String MESSAGE_TEMPLATE_START_AND_END_SEPARATOR = "-";

	public DevArtifactsPropertyFileHelper(Map<String, String[]> paramMap) throws IOException
	{
		super(getPropertiesFile(paramMap));
	}

	public DevArtifactsPropertyFileHelper(String propertiesFile) throws IOException
	{
		super(propertiesFile);
	}

	public static void updateParametersWithPropertiesFile(
		Map<String, String[]> paramMap,
		String propertiesFileSystemProperty)
	{
		// Get properties file from system property
		String systemProperty = (propertiesFileSystemProperty == null)
			? DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY
			: propertiesFileSystemProperty;
		String propertiesFile = System.getProperty(systemProperty);
		paramMap.put(
			DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE,
			new String[] { propertiesFile });
	}

	private static String getPropertiesFile(Map<String, String[]> paramMap)
	{
		String[] propertiesFileValues = paramMap.get(PARAM_PROPERTIES_FILE);
		if (propertiesFileValues == null || propertiesFileValues.length == 0
			|| propertiesFileValues[0] == null || "".equals(propertiesFileValues[0]))
		{
			propertiesFileValues = new String[] {
					DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE };
		}
		return propertiesFileValues[0];
	}

	/**
	 * Initialize the configuration from the loaded properties
	 *
	 * @param config the configuration
	 * @param repo the repository
	 */
	public void initConfig(DevArtifactsConfig config, Repository repo, Session session)
		throws IOException, OperationException
	{
		initFolders(config);
		initLineSeparator(config);

		String authorizedRoleStr = getPropertyValueOrNull(PROPERTY_AUTHORIZED_ROLE);
		Role authorizedRole;
		if (authorizedRoleStr == null)
		{
			authorizedRole = null;
		}
		else if (Role.ADMINISTRATOR.getRoleName().equals(authorizedRoleStr))
		{
			authorizedRole = Role.ADMINISTRATOR;
		}
		else
		{
			authorizedRole = Role.forSpecificRole(authorizedRoleStr);
		}
		config.setAuthorizedRole(authorizedRole);

		initDefaults(config);
		initTenantInfo(config);
		initDataSpacesForPermissions(config, repo, session);

		config.setRefreshSchemas(getBooleanProperty(PROPERTY_REFRESH_SCHEMAS, false));
		config.setProcessDataSetPermissionsInChildDataSpaces(
			getBooleanProperty(PROPERTY_DATA_SET_PERMISSIONS_IN_CHILD_DATA_SPACES, true));
		initDataSetsForPermissions(config, repo, session);

		initTablesForData(config, repo);
		initAdminFlags(config, repo);

		String perspectiveWindowName = getPropertyValueOrNull(PROPERTY_PERSPECTIVE_WINDOW_NAME);
		if (DevArtifactsConstants.TENANT_POLICY_MULTI.equals(config.getTenantPolicy())
			&& perspectiveWindowName != null)
		{
			DevArtifactsUtil.getLog()
				.warn(
					"perspectiveWindowName is not applicable for "
						+ DevArtifactsConstants.TENANT_POLICY_MULTI
						+ " tenant policy. Ignoring perspectiveWindowName.");
			perspectiveWindowName = null;
		}
		config.setPerspectiveWindowName(perspectiveWindowName);

		config
			.setIgnorePerspectiveLogo(getBooleanProperty(PROPERTY_IGNORE_PERSPECTIVE_LOGO, false));

		String[] tenantViewPublicationValues = getPropertyAsArray(
			PROPERTY_TENANT_VIEW_PUBLICATIONS);
		List<String> tenantViewPublications = Arrays.asList(tenantViewPublicationValues);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy())
			&& !tenantViewPublications.isEmpty())
		{
			DevArtifactsUtil.getLog()
				.warn(
					"tenantViewPublications is not applicable for single tenant policy. Ignoring tenantViewPublications.");
			tenantViewPublications = new ArrayList<>();
		}
		config.setTenantViewPublications(tenantViewPublications);

		String tenantViewPublicationsPrefix = getPropertyValueOrNull(
			PROPERTY_TENANT_VIEW_PUBLICATIONS_PREFIX);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy())
			&& tenantViewPublicationsPrefix != null)
		{
			DevArtifactsUtil.getLog()
				.warn(
					"tenantViewPublicationsPrefix is not applicable for single tenant policy. Ignoring tenantViewPublicationsPrefix.");
			tenantViewPublicationsPrefix = null;
		}
		config.setTenantViewPublicationsPrefix(tenantViewPublicationsPrefix);

		String[] tenantPerspectiveValues = getPropertyAsArray(PROPERTY_TENANT_PERSPECTIVES);
		List<String> tenantPerspectives = Arrays.asList(tenantPerspectiveValues);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy())
			&& !tenantPerspectives.isEmpty())
		{
			DevArtifactsUtil.getLog()
				.warn(
					"tenantPerspectives is not applicable for single tenant policy. Ignoring tenantPerspectives.");
			tenantPerspectives = new ArrayList<>();
		}
		config.setTenantPerspectives(tenantPerspectives);

		String tenantPerspectivesPrefix = getPropertyValueOrNull(
			PROPERTY_TENANT_PERSPECTIVES_PREFIX);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy())
			&& tenantPerspectivesPrefix != null)
		{
			DevArtifactsUtil.getLog()
				.warn(
					"tenantPerspectivesPrefix is not applicable for single tenant policy. Ignoring tenantPerspectivesPrefix.");
			tenantPerspectivesPrefix = null;
		}
		config.setTenantPerspectivesPrefix(tenantPerspectivesPrefix);

		String tenantSharedRolesPredicate = getPropertyValueOrNull(
			PROPERTY_TENANT_SHARED_ROLES_PREDICATE);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
		{
			if (tenantSharedRolesPredicate != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"tenantSharedRolesPredicate is not applicable for single tenant policy. Ignoring tenantSharedRolesPredicate.");
				tenantSharedRolesPredicate = null;
			}
		}
		else if (tenantSharedRolesPredicate == null)
		{
			tenantSharedRolesPredicate = MATCHES_NONE_ROLES_PREDICATE;
		}
		config.setTenantSharedRolesPredicate(tenantSharedRolesPredicate);

		String tenantRolesPredicate = getPropertyValueOrNull(PROPERTY_TENANT_ROLES_PREDICATE);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
		{
			if (tenantRolesPredicate != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"tenantRolesPredicate is not applicable for single tenant policy. Ignoring tenantRolesPredicate.");
				tenantRolesPredicate = null;
			}
		}
		else if (tenantRolesPredicate == null)
		{
			tenantRolesPredicate = MATCHES_NONE_ROLES_PREDICATE;
		}
		config.setTenantRolesPredicate(tenantRolesPredicate);
		config.setUsersRolesPredicate(getPropertyValueOrNull(PROPERTY_USERS_ROLES_PREDICATE));

		config
			.setProcessAddonRegistrations(getBooleanProperty(PROPERTY_ADDON_REGISTRATIONS, false));

		initMessageTemplates(config);

		initAddonAdix(config);
		initAddonDama(config);
		initAddonDint(config);
		initAddonDpra(config);
		initAddonDqid(config);
		initAddonMame(config);

		if (config instanceof ImportDevArtifactsConfig)
		{
			ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
			initMasterWorkflowModels(importConfig, repo);
			initWorkflowModelsToNotPublish(importConfig, repo);
			importConfig.setRemoveAdministratorRole(
				getBooleanProperty(PROPERTY_REMOVE_ADMINISTRATOR_ROLE, false));
		}
	}

	@SuppressWarnings("deprecation")
	private void initFolders(DevArtifactsConfig config) throws IOException
	{
		String artifactsFolder = props.getProperty(PROPERTY_ARTIFACTS_FOLDER);
		// Backwards compatibility - eventually will get rid of these options
		if (artifactsFolder == null || "".equals(artifactsFolder))
		{
			config.setDataFolder(new File(getRequiredProperty(PROPERTY_DATA_FOLDER)));
			config.setPermissionsFolder(new File(getRequiredProperty(PROPERTY_PERMISSIONS_FOLDER)));
			config.setWorkflowsFolder(new File(getRequiredProperty(PROPERTY_WORKFLOWS_FOLDER)));
			File adminFolder = new File(getRequiredProperty(PROPERTY_ADMIN_FOLDER));
			config.setAdminFolder(adminFolder);
			config.setPerspectivesFolder(adminFolder);
		}
		else
		{
			config.setArtifactsFolder(new File(artifactsFolder));
		}

		config.setCopyEnvironmentFolder(
			new File(getRequiredProperty(PROPERTY_COPY_ENVIRONMENT_FOLDER)));
	}

	private void initLineSeparator(DevArtifactsConfig config) throws IOException
	{
		String lineSepType = getRequiredProperty(PROPERTY_LINE_SEPARATOR);
		String lineSep = LINE_SEP_TYPE_WINDOWS.equals(lineSepType) ? "\r\n" : "\n";
		config.setLineSeparator(lineSep);
	}

	private void initDefaults(DevArtifactsConfig config) throws IOException
	{
		// Support the deprecated way of defining the default environment copy
		// by falling back to this value if the import/export specific version
		// isn't defined below
		@SuppressWarnings("deprecation")
		boolean defaultEnvCopy = getBooleanProperty(PROPERTY_DEFAULT_ENVIRONMENT_COPY, false);

		if (config instanceof ImportDevArtifactsConfig)
		{
			ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;

			String defaultEnvCopyImportStr = props
				.getProperty(PROPERTY_DEFAULT_ENVIRONMENT_COPY_IMPORT);
			if (defaultEnvCopyImportStr == null)
			{
				importConfig.setEnvironmentCopy(defaultEnvCopy);
			}
			else
			{
				importConfig.setEnvironmentCopy(
					Boolean.valueOf(defaultEnvCopyImportStr.trim()).booleanValue());
			}

			importConfig.setPublishWorkflowModels(
				getBooleanProperty(PROPERTY_DEFAULT_PUBLISH_WORKFLOW_MODELS, true));

			boolean defaultReplaceMode = getBooleanProperty(PROPERTY_DEFAULT_REPLACE_MODE, true);
			ImportSpecMode importSpecMode = defaultReplaceMode ? ImportSpecMode.REPLACE
				: ImportSpecMode.UPDATE_OR_INSERT;
			importConfig.setImportMode(importSpecMode);

			importConfig.setSkipNonExistingFiles(
				getBooleanProperty(PROPERTY_DEFAULT_SKIP_NON_EXISTING_FILES, false));
		}
		else
		{
			ExportDevArtifactsConfig exportConfig = (ExportDevArtifactsConfig) config;

			String defaultEnvCopyExportStr = props
				.getProperty(PROPERTY_DEFAULT_ENVIRONMENT_COPY_EXPORT);
			if (defaultEnvCopyExportStr == null)
			{
				exportConfig.setEnvironmentCopy(defaultEnvCopy);
			}
			else
			{
				exportConfig.setEnvironmentCopy(
					Boolean.valueOf(defaultEnvCopyExportStr.trim()).booleanValue());
			}

			boolean enableDownloadToLocal = getBooleanProperty(
				PROPERTY_ENABLE_DOWNLOAD_TO_LOCAL,
				false);
			exportConfig.setEnableDownloadToLocal(enableDownloadToLocal);
			if (enableDownloadToLocal)
			{
				exportConfig.setDownloadToLocal(
					getBooleanProperty(PROPERTY_DEFAULT_DOWNLOAD_TO_LOCAL, false));
			}
			else
			{
				exportConfig.setDownloadToLocal(false);
			}
		}
	}

	private void initTenantInfo(DevArtifactsConfig config)
	{
		String tenantPolicy = getPropertyValueOrNull(DevArtifactsConstants.PROPERTY_TENANT_POLICY);
		if (tenantPolicy == null)
		{
			config.setTenantPolicy(DevArtifactsConstants.TENANT_POLICY_SINGLE);
		}
		else
		{
			config.setTenantPolicy(tenantPolicy);
		}

		if (!DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
		{
			String[] moduleValues = getPropertyAsArray(PROPERTY_MODULES);
			List<String> modules = Arrays.asList(moduleValues);
			config.setModules(modules);
		}
		else
		{
			config.setModules(new ArrayList<>());
		}
	}

	private void initTablesForData(DevArtifactsConfig config, Repository repo)
	{
		ImportDevArtifactsConfig importConfig = (config instanceof ImportDevArtifactsConfig)
			? (ImportDevArtifactsConfig) config
			: null;
		String[] tableValues = getPropertyAsArray(PROPERTY_TABLES_FOR_DATA);
		List<AdaptationTablePredicate> tablesForData = new ArrayList<>();
		Map<DataSetCreationKey, List<String>> createdDataSetTableSpecs = new LinkedHashMap<>();
		for (String tableValue : tableValues)
		{
			String[] tokens = getPropertyValueTokens(tableValue);
			String tableNameStr = tokens[PROPERTY_TOKEN_INDEX_TABLE_NAME];
			String tablePredicate = (tokens.length > PROPERTY_TOKEN_INDEX_TABLE_PREDICATE)
				? tokens[PROPERTY_TOKEN_INDEX_TABLE_PREDICATE]
				: null;

			// Replace wildcard with all tables for that data set
			if (tableNameStr.endsWith(WILDCARD))
			{
				Adaptation dataSet = getDataSetFromProperty(tableValue, repo);
				if (dataSet == null)
				{
					addTableSpecToMap(tableValue, createdDataSetTableSpecs);
				}
				else
				{
					List<AdaptationTable> allTables = AdaptationUtil.getAllTables(dataSet);
					for (AdaptationTable table : allTables)
					{
						tablesForData.add(new AdaptationTablePredicate(table, tablePredicate));
					}
				}
			}
			else
			{
				AdaptationTable table = getTableFromProperty(tableValue, repo);
				if (table == null)
				{
					addTableSpecToMap(tableValue, createdDataSetTableSpecs);
				}
				else
				{
					tablesForData.add(new AdaptationTablePredicate(table, tablePredicate));
				}
			}
		}
		config.setTablesForData(tablesForData);
		if (importConfig != null)
		{
			importConfig.setCreatedDataSetTableSpecs(createdDataSetTableSpecs);
		}
	}

	private void addTableSpecToMap(
		String tableValue,
		Map<DataSetCreationKey, List<String>> createdDataSetTableSpecs)
	{
		String[] tokens = getPropertyValueTokens(tableValue);
		String dataSpaceName = tokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME];
		String dataSetName = tokens[PROPERTY_TOKEN_INDEX_DATA_SET_NAME];
		String tableName = tokens[PROPERTY_TOKEN_INDEX_TABLE_NAME];

		DataSetCreationKey dataSetCreationKey = new DataSetCreationKey(dataSpaceName, dataSetName);
		List<String> tableSpecs = createdDataSetTableSpecs.get(dataSetCreationKey);
		if (tableSpecs == null)
		{
			tableSpecs = new ArrayList<>();
			createdDataSetTableSpecs.put(dataSetCreationKey, tableSpecs);
		}
		tableSpecs.add(tableName);
	}

	private void initDataSetsForPermissions(
		DevArtifactsConfig config,
		Repository repo,
		Session session)
		throws OperationException
	{
		ImportDevArtifactsConfig importConfig = (config instanceof ImportDevArtifactsConfig)
			? (ImportDevArtifactsConfig) config
			: null;
		String[] dataSetValues = getPropertyAsArray(PROPERTY_DATA_SETS_FOR_PERMISSIONS);
		boolean createDataSets = getBooleanProperty(
			PROPERTY_CREATE_DATA_SETS,
			DEFAULT_VALUE_CREATE_DATA_SETS);
		List<Adaptation> dataSets = new ArrayList<>();
		List<DataSetCreationInfo> dataSetsToCreate = new ArrayList<>();
		// Loop through all data set values specified in properties
		for (String dataSetValue : dataSetValues)
		{
			Adaptation dataSet = getDataSetFromProperty(dataSetValue, repo);
			// If data set doesn't exist, it's an error, unless it's an import
			// and createDataSets was specified. In that case, add its info to
			// the list of data sets to create
			if (dataSet == null)
			{
				boolean throwError = true;
				if (importConfig != null && createDataSets)
				{
					String[] tokens = getPropertyValueTokens(dataSetValue);
					if (tokens.length > PROPERTY_TOKEN_INDEX_DATA_MODEL_XSD)
					{
						String dataModelXSD = tokens[PROPERTY_TOKEN_INDEX_DATA_MODEL_XSD];
						if (dataModelXSD.length() > 0)
						{
							String dataSpaceName = tokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME];
							String dataSetName = tokens[PROPERTY_TOKEN_INDEX_DATA_SET_NAME];

							dataSetsToCreate.add(
								new DataSetCreationInfo(
									new DataSetCreationKey(dataSpaceName, dataSetName),
									dataModelXSD));
							throwError = false;
						}
					}
				}
				if (throwError)
				{
					throw OperationException.createError(
						"Data set " + dataSetValue + " not found during Dev Artifacts processing.");
				}
			}
			// Otherwise, it exists so just add it to the list
			else
			{
				dataSets.add(dataSet);
			}
		}
		config.setDataSetsForPermissions(dataSets);
		if (importConfig != null)
		{
			importConfig.setDataSetsToCreate(dataSetsToCreate);
		}
	}

	private void initDataSpacesForPermissions(
		DevArtifactsConfig config,
		Repository repo,
		Session session)
		throws OperationException
	{
		ImportDevArtifactsConfig importConfig = (config instanceof ImportDevArtifactsConfig)
			? (ImportDevArtifactsConfig) config
			: null;
		String[] dataSpaceValues = getPropertyAsArray(PROPERTY_DATA_SPACES_FOR_PERMISSIONS);
		List<AdaptationHome> dataSpaces = new ArrayList<>();
		// Loop through all data space values specified in properties
		for (String dataSpaceValue : dataSpaceValues)
		{
			AdaptationHome dataSpace = getDataSpaceFromProperty(dataSpaceValue, repo);
			// If it's an import, check if the data space exists.
			// If not, create it if createDataSpaces is specified.
			// If so, update its info.
			if (importConfig != null)
			{
				if (dataSpace == null)
				{
					boolean createDataSpaces = getBooleanProperty(
						PROPERTY_CREATE_DATA_SPACES,
						DEFAULT_VALUE_CREATE_DATA_SPACES);
					if (createDataSpaces)
					{
						dataSpace = createDataSpace(
							getPropertyValueTokens(dataSpaceValue),
							repo,
							session);
					}
				}
				else
				{
					try
					{
						updateDataSpaceInfo(
							dataSpace,
							getPropertyValueTokens(dataSpaceValue),
							repo,
							session);
					}
					catch (OperationException e)
					{
						DevArtifactsUtil.getLog()
							.debug(
								"Failed to update data space properties for "
									+ dataSpace.getKey().getName());
					}
				}
			}
			// If the data space doesn't exist at this point (either because it is an export,
			// or it's an import and createDataSpaces is false) then it's an error
			if (dataSpace == null)
			{
				throw OperationException.createError(
					"Data space " + dataSpaceValue + " not found during Dev Artifacts processing.");
			}
			dataSpaces.add(dataSpace);
		}
		config.setDataSpacesForPermissions(dataSpaces);
	}

	private static AdaptationHome createDataSpace(
		String[] dataSpaceTokens,
		Repository repo,
		Session session)
		throws OperationException
	{
		HomeCreationSpec spec = new HomeCreationSpec();

		String parentDataSpaceName = dataSpaceTokens[PROPERTY_TOKEN_INDEX_PARENT_DATA_SPACE_NAME];
		if (parentDataSpaceName == null || parentDataSpaceName.trim().length() == 0)
		{
			// Assume Reference if no parent data space was specified
			parentDataSpaceName = CommonConstants.REFERENCE_DATA_SPACE_NAME;
		}
		AdaptationHome parentDataSpace = repo
			.lookupHome(HomeKey.forBranchName(parentDataSpaceName));
		if (parentDataSpace == null)
		{
			throw OperationException
				.createError("Could not find data space " + parentDataSpaceName + ".");
		}
		spec.setParent(parentDataSpace);

		String dataSpaceName = dataSpaceTokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME];
		spec.setKey(HomeKey.forBranchName(dataSpaceName));
		spec.setLabel(
			UserMessage.createInfo(dataSpaceTokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_LABEL]));
		spec.setOwner(Profile.parse(dataSpaceTokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_OWNER]));

		if (dataSpaceTokens.length > PROPERTY_TOKEN_INDEX_RELATIONAL)
		{
			String relationalStr = dataSpaceTokens[PROPERTY_TOKEN_INDEX_RELATIONAL];
			if (relationalStr != null && relationalStr.trim().length() > 0)
			{
				LoggingCategory.getKernel()
					.warn(
						"Relational mode is no longer supported. The relational specification for data space "
							+ dataSpaceName + " will be ignored.");
			}
		}
		return repo.createHome(spec, session);
	}

	private static void updateDataSpaceInfo(
		AdaptationHome dataSpace,
		String[] dataSpaceTokens,
		Repository repo,
		Session session)
		throws OperationException
	{
		String label = dataSpaceTokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_LABEL];
		repo.setDocumentationLabel(dataSpace, label, session.getLocale(), session);
		// TODO: No way in API to set owner, so on update we have to just ignore that
	}

	@SuppressWarnings("deprecation")
	private void initAdminFlags(DevArtifactsConfig config, Repository repo)
	{
		config.setProcessAdminDataSetPermissions(
			getBooleanProperty(PROPERTY_ADMIN_DATA_SET_PERMISSIONS, false));
		config.setProcessDirectoryData(getBooleanProperty(PROPERTY_DIRECTORY, true));
		config
			.setProcessGlobalPermissionsData(getBooleanProperty(PROPERTY_GLOBAL_PERMISSIONS, true));
		config.setProcessViewsData(getBooleanProperty(PROPERTY_VIEWS, true));
		config.setProcessTasksData(getBooleanProperty(PROPERTY_TASKS, true));
		config.setProcessPerspectivesData(getBooleanProperty(PROPERTY_PERSPECTIVES, true));
		config.setQualifyDataSetAndTableFileNames(
			getBooleanProperty(PROPERTY_QUALIFY_DATASET_AND_TABLE_NAMES, false));
		config.setProcessHistorizationProfiles(
			getBooleanProperty(PROPERTY_HISTORIZATION_PROFILES, false));
		config.setProcessAddonAdixData(getBooleanProperty(PROPERTY_ADDON_ADIX, false));
		config.setProcessAddonDamaData(getBooleanProperty(PROPERTY_ADDON_DAMA, false));
		config.setProcessAddonDintData(getBooleanProperty(PROPERTY_ADDON_DINT, false));
		config.setProcessAddonDpraData(getBooleanProperty(PROPERTY_ADDON_DPRA, false));
		config.setProcessAddonDqidData(getBooleanProperty(PROPERTY_ADDON_DQID, false));
		config.setProcessAddonDmdvData(getBooleanProperty(PROPERTY_ADDON_DMDV, false));
		config.setProcessAddonMameData(getBooleanProperty(PROPERTY_ADDON_MAME, false));

		if (getBooleanProperty(PROPERTY_ADDON_DAQA, false))
		{
			LoggingCategory.getKernel()
				.warn(
					"DAQA add-on is no longer supported. Property " + PROPERTY_ADDON_DAQA
						+ " will be ignored.");
		}
		if (getBooleanProperty(PROPERTY_ADDON_HMFH, false))
		{
			LoggingCategory.getKernel()
				.warn(
					"HMFH add-on is no longer supported. Property " + PROPERTY_ADDON_HMFH
						+ " will be ignored.");
		}
		if (getBooleanProperty(PROPERTY_ADDON_RPFL, false))
		{
			LoggingCategory.getKernel()
				.warn(
					"RPFL add-on is no longer supported. Property " + PROPERTY_ADDON_RPFL
						+ " will be ignored.");
		}
		if (getBooleanProperty(PROPERTY_ADDON_TESE, false))
		{
			LoggingCategory.getKernel()
				.warn(
					"TESE add-on is no longer supported. Property " + PROPERTY_ADDON_TESE
						+ " will be ignored.");
		}
	}

	private void initAddonAdix(DevArtifactsConfig config)
	{
		String addonAdixPreferencePrefix = getPropertyValueOrNull(
			PROPERTY_ADDON_ADIX_PREFERENCE_PREFIX);
		String addonAdixTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_ADIX_TENANT_PREFIX);
		if (config.isProcessAddonAdixData())
		{
			config.setAddonAdixPreferencePrefix(addonAdixPreferencePrefix);

			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonAdixTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonAdixTenantPrefix is not applicable for single tenant policy. Ignoring addonAdixTenantPrefix.");
					config.setAddonAdixTenantPrefix(null);
				}
			}
			else
			{
				if (addonAdixTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonAdixTenantPrefix was specified. No ADIX artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonAdixTenantPrefix(addonAdixTenantPrefix);
				}
			}
		}
		else
		{
			if (addonAdixPreferencePrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonAdixPreferencePrefix is not applicable when addonAdix is false. Ignoring addonAdixPreferencePrefix.");
				config.setAddonAdixPreferencePrefix(null);
			}
			if (addonAdixTenantPrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonAdixTenantPrefix is not applicable when addonAdix is false. Ignoring addonAdixTenantPrefix.");
				config.setAddonAdixTenantPrefix(null);
			}
		}
	}

	private void initAddonDama(DevArtifactsConfig config)
	{
		String addonDamaDrivePathPrefix = getPropertyValueOrNull(
			PROPERTY_ADDON_DAMA_DRIVE_PATH_PREFIX);
		String addonDamaTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_DAMA_TENANT_PREFIX);
		if (config.isProcessAddonDamaData())
		{
			config.setAddonDamaDrivePathPrefix(addonDamaDrivePathPrefix);

			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonDamaTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonDamaTenantPrefix is not applicable for single tenant policy. Ignoring addonDamaTenantPrefix.");
					config.setAddonDamaTenantPrefix(null);
				}
			}
			else
			{
				if (addonDamaTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonDamaTenantPrefix was specified. No DAMA artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonDamaTenantPrefix(addonDamaTenantPrefix);
				}
			}
		}
		else
		{
			if (addonDamaDrivePathPrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonDamaDrivePathPrefix is not applicable when addonDama is false. Ignoring addonDamaDrivePathPrefix.");
				config.setAddonDamaDrivePathPrefix(null);
			}

			if (addonDamaTenantPrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonDamaTenantPrefix is not applicable when addonDama is false. Ignoring addonDamaTenantPrefix.");
				config.setAddonDamaTenantPrefix(null);
			}
		}
	}

	private void initAddonDint(DevArtifactsConfig config)
	{
		String addonDintUserTemplatePrefix = getPropertyValueOrNull(
			PROPERTY_ADDON_DINT_USER_TEMPLATE_PREFIX);
		String addonDintTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_DINT_TENANT_PREFIX);
		if (config.isProcessAddonDintData())
		{
			config.setAddonDintUserTemplatePrefix(addonDintUserTemplatePrefix);

			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonDintTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonDintTenantPrefix is not applicable for single tenant policy. Ignoring addonDintTenantPrefix.");
					config.setAddonDintTenantPrefix(null);
				}
			}
			else
			{
				if (addonDintTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonDintTenantPrefix was specified. No DINT artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonDintTenantPrefix(addonDintTenantPrefix);
				}
			}
		}
		else
		{
			if (addonDintUserTemplatePrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonDintUserTemplatePrefix is not applicable when addonDint is false. Ignoring addonDintUserTemplatePrefix.");
				config.setAddonDintUserTemplatePrefix(null);
			}
			if (addonDintTenantPrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonDintTenantPrefix is not applicable when addonDint is false. Ignoring addonDintTenantPrefix.");
				config.setAddonDintTenantPrefix(null);
			}
		}
	}

	private void initAddonDpra(DevArtifactsConfig config)
	{
		String addonDpraTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_DPRA_TENANT_PREFIX);
		if (config.isProcessAddonDpraData())
		{
			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonDpraTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonDpraTenantPrefix is not applicable for single tenant policy. Ignoring addonDpraTenantPrefix.");
					config.setAddonDpraTenantPrefix(null);
				}
			}
			else
			{
				if (addonDpraTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonDpraTenantPrefix was specified. No DPRA artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonDpraTenantPrefix(addonDpraTenantPrefix);
				}
			}
		}
		else if (addonDpraTenantPrefix != null)
		{
			DevArtifactsUtil.getLog()
				.warn(
					"addonDpraTenantPrefix is not applicable when addonDpra is false. Ignoring addonDpraTenantPrefix.");
			config.setAddonDpraTenantPrefix(null);
		}
	}

	private void initAddonDqid(DevArtifactsConfig config)
	{
		String addonDqidTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_DQID_TENANT_PREFIX);
		if (config.isProcessAddonDqidData())
		{
			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonDqidTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonDqidTenantPrefix is not applicable for single tenant policy. Ignoring addonDqidTenantPrefix.");
					config.setAddonDqidTenantPrefix(null);
				}
			}
			else
			{
				if (addonDqidTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonDqidTenantPrefix was specified. No DQID artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonDqidTenantPrefix(addonDqidTenantPrefix);
				}
			}
		}
		else if (addonDqidTenantPrefix != null)
		{
			DevArtifactsUtil.getLog()
				.warn(
					"addonDqidTenantPrefix is not applicable when addonDqid is false. Ignoring addonDqidTenantPrefix.");
			config.setAddonDqidTenantPrefix(null);
		}
	}

	private void initAddonMame(DevArtifactsConfig config)
	{
		String addonMameTenantPrefix = getPropertyValueOrNull(PROPERTY_ADDON_MAME_TENANT_PREFIX);
		if (config.isProcessAddonMameData())
		{
			if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
			{
				if (addonMameTenantPrefix != null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"addonMameTenantPrefix is not applicable for single tenant policy. Ignoring addonMameTenantPrefix.");
					config.setAddonMameTenantPrefix(null);
				}
			}
			else
			{
				if (addonMameTenantPrefix == null)
				{
					DevArtifactsUtil.getLog()
						.warn(
							"No value for addonMameTenantPrefix was specified. No MAME artifacts will be processed for this tenant.");
				}
				else
				{
					config.setAddonMameTenantPrefix(addonMameTenantPrefix);
				}
			}
		}
		else
		{
			if (addonMameTenantPrefix != null)
			{
				DevArtifactsUtil.getLog()
					.warn(
						"addonmameTenantPrefix is not applicable when addonMame is false. Ignoring addonMameTenantPrefix.");
				config.setAddonMameTenantPrefix(null);
			}
		}
	}

	private void initMessageTemplates(DevArtifactsConfig config) throws OperationException
	{
		boolean processMessageTemplates = getBooleanProperty(
			DevArtifactsConstants.PROPERTY_MESSAGE_TEMPLATES,
			true);
		config.setProcessMessageTemplates(processMessageTemplates);

		// List of array of Integers representing the start & end of ranges (nulls if not specified)
		List<Integer[]> messageTemplateRange = new ArrayList<>();
		if (processMessageTemplates)
		{
			String range = getPropertyValueOrNull(
				DevArtifactsConstants.PROPERTY_TENANT_MESSAGE_TEMPLATE_RANGE);
			if (range != null)
			{
				if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(config.getTenantPolicy()))
				{
					DevArtifactsUtil.getLog()
						.warn(
							"tenantMessageTemplateRange is not applicable for single tenant policy. Ignoring tenantMessageTemplateRange.");
				}
				else
				{
					// If multiple ranges were specified, then each token will be a start & end number
					String[] tokens = range.split(MESSAGE_TEMPLATE_RANGES_SEPARATOR);
					for (String token : tokens)
					{
						String subRangeStr = token.trim();
						if (!subRangeStr.isEmpty())
						{
							String startIdStr;
							String endIdStr;
							int sepInd = subRangeStr
								.indexOf(MESSAGE_TEMPLATE_START_AND_END_SEPARATOR);
							// If there's no separator, then there's just one number being specified, so the end
							// should be the same as the start
							if (sepInd == -1)
							{
								startIdStr = subRangeStr;
								endIdStr = subRangeStr;
							}
							// Otherwise, if there's something after the separator, parse out the end.
							// If there's nothing after the separator, it means no end is specified.
							else
							{
								startIdStr = subRangeStr.substring(0, sepInd).trim();
								if (subRangeStr.length() > sepInd + 1)
								{
									endIdStr = subRangeStr.substring(sepInd + 1).trim();
								}
								else
								{
									endIdStr = null;
								}
							}

							Integer startId;
							try
							{
								startId = Integer.valueOf(startIdStr);
							}
							catch (NumberFormatException ex)
							{
								throw OperationException.createError(
									"Message template range start ID " + startIdStr
										+ " is not valid.",
									ex);
							}

							Integer endId;
							try
							{
								// End is optional
								endId = (endIdStr == null) ? null : Integer.valueOf(endIdStr);
							}
							catch (NumberFormatException ex)
							{
								throw OperationException.createError(
									"Message template range end ID " + endIdStr + " is not valid.",
									ex);
							}
							// Add the start & end numbers to an array and add that array to the list
							Integer[] subRange = new Integer[2];
							subRange[0] = startId;
							subRange[1] = endId;
							messageTemplateRange.add(subRange);
						}
					}
				}
			}
		}
		config.setTenantMessageTemplateRange(messageTemplateRange);
	}

	private void initMasterWorkflowModels(ImportDevArtifactsConfig importConfig, Repository repo)
		throws OperationException
	{
		String[] masterWFModelsValues = getPropertyAsArray(PROPERTY_MASTER_WORKFLOW_MODELS);
		Map<String, Set<String>> masterWFModels = new LinkedHashMap<>();
		for (String value : masterWFModelsValues)
		{
			String[] tokens = getPropertyValueTokens(value);
			String masterWFName = tokens[0];
			Set<String> subWFModels = new HashSet<>();
			for (int i = 1; i < tokens.length; i++)
			{
				subWFModels.add(tokens[i]);
			}
			masterWFModels.put(masterWFName, subWFModels);
		}
		importConfig.setMasterWorkflowModels(masterWFModels);
	}

	private void initWorkflowModelsToNotPublish(
		ImportDevArtifactsConfig importConfig,
		Repository repo)
	{
		String[] wfModelValues = getPropertyAsArray(PROPERTY_WORKFLOW_MODELS_TO_NOT_PUBLISH);
		List<String> wfModels = Arrays.asList(wfModelValues);
		importConfig.setWorkflowModelsToNotPublish(wfModels);
	}

	private String getPropertyValueOrNull(String key)
	{
		String value = props.getProperty(key);
		if (value != null)
		{
			value = value.trim();
			if ("".equals(value))
			{
				value = null;
			}
		}
		return value;
	}

	private static String initDefaultPropertiesFile()
	{
		String folder;
		try
		{
			folder = CommonConstants.getEBXHome();
		}
		catch (IOException ex)
		{
			DevArtifactsUtil.getLog().error("Error looking up EBX Home.", ex);
			return null;
		}
		return folder + "/dev-artifacts.properties";
	}
}