package com.orchestranetworks.ps.admin.devartifacts.constants;

/**
 * Constants for Dev Artifacts
 */
public interface DevArtifactsConstants
{
	static final String FOLDER_NAME_ADDON_ADIX = "addonAdix";
	static final String FOLDER_NAME_ADDON_DAMA = "addonDama";
	static final String FOLDER_NAME_ADDON_DINT = "addonDint";
	static final String FOLDER_NAME_ADDON_DPRA = "addonDpra";
	static final String FOLDER_NAME_ADDON_DQID = "addonDqid";
	static final String FOLDER_NAME_ADDON_MAME = "addonMame";
	static final String FOLDER_NAME_ADMIN = "admin";
	static final String FOLDER_NAME_DATA = "data";
	static final String FOLDER_NAME_PERMISSIONS = "permissions";
	static final String FOLDER_NAME_PERSPECTIVES = "perspectives";
	static final String FOLDER_NAME_WORKFLOWS = "workflows";
	static final String FOLDER_NAME_DMA = "dma";

	static final String DATA_PREFIX = "Data_";
	static final String DATA_SET_ARCHIVE_PREFIX = "DSet_";
	static final String PERMISSIONS_DATA_SET_PREFIX = "DSet_Perm_";
	static final String PERMISSIONS_DATA_SPACE_PREFIX = "DSpc_Perm_";
	static final String WORKFLOW_PREFIX = "WF_";
	static final String PERSPECTIVE_PREFIX = "Persp_";
	static final String DATA_SET_PROPERTIES_SUFFIX = ".properties";
	static final String DATA_SET_PROPERTY_LABEL = "label";
	static final String DATA_SET_PROPERTY_DESCRIPTION = "description";
	static final String DATA_SET_PROPERTY_OWNER = "owner";
	static final String DATA_SET_PROPERTY_PARENT_DATA_SET = "parentDataSet";
	static final String DATA_SET_PROPERTY_CHILD_DATA_SETS = "childDataSets";
	static final String DMA_ARCHIVE_PREFIX = "EnvCopy_";
	static final String SHARED_ROLES_PREFIX = "Shared_";
	static final String ADDON_ADIX_PREFIX = "Addon_Adix_";
	static final String ADDON_ADIX_DATA_EXCHANGE_PREFIX = ADDON_ADIX_PREFIX + "Dex_";
	static final String ADDON_ADIX_DATA_MODELING_PREFIX = ADDON_ADIX_PREFIX + "Dm_";
	static final String ADDON_DAMA_PREFIX = "Addon_Dama_";
	static final String ADDON_DPRA_PREFIX = "Addon_Dpra_";
	static final String ADDON_DQID_PREFIX = "Addon_Dqid_";
	static final String ADDON_DMDV_PREFIX = "Addon_Dmdv_";
	static final String ADDON_MAME_PREFIX = "Addon_Mame_";

	static final String LOG_PREFIX = "[Dev Artifacts]";
	static final String SERVICE_COMPLETE_MSG = "Service complete.";

	// When writing the child data sets out, need to escape the pipe used as the separator regex
	static final String DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR = "\\|";
	// When reading the child data sets in, need to escape the escape character itself also
	static final String DATA_SET_PROPERTIES_FILE_CHILD_DATA_SETS_SEPARATOR_ESCAPED = "\\\\\\|";

	static final String PROPERTY_ARTIFACTS_FOLDER = "artifactsFolder";
	static final String PROPERTY_DATA_FOLDER = "dataFolder";
	static final String PROPERTY_PERMISSIONS_FOLDER = "permissionsFolder";
	static final String PROPERTY_WORKFLOWS_FOLDER = "workflowsFolder";
	static final String PROPERTY_ADMIN_FOLDER = "adminFolder";
	static final String PROPERTY_COPY_ENVIRONMENT_FOLDER = "copyEnvironmentFolder";
	static final String PROPERTY_LINE_SEPARATOR = "lineSeparator";
	static final String PROPERTY_AUTHORIZED_ROLE = "authorizedRole";
	static final String PROPERTY_TENANT_POLICY = "tenantPolicy";
	static final String PROPERTY_MODULES = "modules";
	static final String PROPERTY_ENABLE_DOWNLOAD_TO_LOCAL = "enableDownloadToLocal";
	/** @deprecated Use {@link #PROPERTY_DEFAULT_ENVIRONMENT_COPY_EXPORT} or {@link #PROPERTY_DEFAULT_ENVIRONMENT_COPY_IMPORT} instead */
	@Deprecated
	static final String PROPERTY_DEFAULT_ENVIRONMENT_COPY = "defaultEnvironmentCopy";
	static final String PROPERTY_DEFAULT_ENVIRONMENT_COPY_EXPORT = "defaultEnvironmentCopyExport";
	static final String PROPERTY_DEFAULT_ENVIRONMENT_COPY_IMPORT = "defaultEnvironmentCopyImport";
	static final String PROPERTY_DEFAULT_PUBLISH_WORKFLOW_MODELS = "defaultPublishWorkflowModels";
	static final String PROPERTY_DEFAULT_REPLACE_MODE = "defaultReplaceMode";
	static final String PROPERTY_DEFAULT_SKIP_NON_EXISTING_FILES = "defaultSkipNonExistingFiles";
	static final String PROPERTY_DEFAULT_DOWNLOAD_TO_LOCAL = "defaultDownloadToLocal";
	static final String PROPERTY_CREATE_DATA_SPACES = "createDataSpaces";
	static final String PROPERTY_REFRESH_SCHEMAS = "refreshSchemas";
	static final String PROPERTY_CREATE_DATA_SETS = "createDataSets";
	static final String PROPERTY_TABLES_FOR_DATA = "tablesForData";
	static final String PROPERTY_DATA_SET_PERMISSIONS_IN_CHILD_DATA_SPACES = "dataSetPermissionsInChildDataSpaces";
	static final String PROPERTY_DATA_SETS_FOR_PERMISSIONS = "dataSetsForPermissions";
	static final String PROPERTY_DATA_SPACES_FOR_PERMISSIONS = "dataSpacesForPermissions";
	static final String PROPERTY_ADMIN_DATA_SET_PERMISSIONS = "adminDataSetPermissions";
	static final String PROPERTY_DIRECTORY = "directory";
	static final String PROPERTY_GLOBAL_PERMISSIONS = "globalPermissions";
	static final String PROPERTY_TASKS = "tasks";
	static final String PROPERTY_VIEWS = "views";
	static final String PROPERTY_TENANT_VIEW_PUBLICATIONS = "tenantViewPublications";
	static final String PROPERTY_TENANT_VIEW_PUBLICATIONS_PREFIX = "tenantViewPublicationsPrefix";
	static final String PROPERTY_PERSPECTIVES = "perspectives";
	static final String PROPERTY_TENANT_PERSPECTIVES = "tenantPerspectives";
	static final String PROPERTY_TENANT_PERSPECTIVES_PREFIX = "tenantPerspectivesPrefix";
	static final String PROPERTY_TENANT_SHARED_ROLES_PREDICATE = "tenantSharedRolesPredicate";
	static final String PROPERTY_TENANT_ROLES_PREDICATE = "tenantRolesPredicate";
	static final String PROPERTY_USERS_ROLES_PREDICATE = "usersRolesPredicate";
	static final String PROPERTY_REMOVE_ADMINISTRATOR_ROLE = "removeAdministratorRole";
	static final String PROPERTY_HISTORIZATION_PROFILES = "historizationProfiles";
	static final String PROPERTY_MASTER_WORKFLOW_MODELS = "masterWorkflowModels";
	static final String PROPERTY_WORKFLOW_MODELS_TO_NOT_PUBLISH = "workflowModelsToNotPublish";
	static final String PROPERTY_MESSAGE_TEMPLATES = "messageTemplates";
	static final String PROPERTY_TENANT_MESSAGE_TEMPLATE_RANGE = "tenantMessageTemplateRange";
	static final String PROPERTY_PERSPECTIVE_WINDOW_NAME = "perspectiveWindowName";
	static final String PROPERTY_IGNORE_PERSPECTIVE_LOGO = "ignorePerspectiveLogo";
	static final String PROPERTY_QUALIFY_DATASET_AND_TABLE_NAMES = "qualifyDataSetAndTableFileNames";

	static final String PROPERTY_ADDON_REGISTRATIONS = "addonRegistrations";
	static final String PROPERTY_ADDON_ADIX = "addonAdix";
	static final String PROPERTY_ADDON_ADIX_PREFERENCE_PREFIX = "addonAdixPreferencePrefix";
	static final String PROPERTY_ADDON_ADIX_TENANT_PREFIX = "addonAdixTenantPrefix";
	static final String PROPERTY_ADDON_DAMA = "addonDama";
	static final String PROPERTY_ADDON_DAMA_DRIVE_PATH_PREFIX = "addonDamaDrivePathPrefix";
	static final String PROPERTY_ADDON_DAMA_TENANT_PREFIX = "addonDamaTenantPrefix";
	/** @deprecated DAQA is no longer supported. */
	@Deprecated
	static final String PROPERTY_ADDON_DAQA = "addonDaqa";
	static final String PROPERTY_ADDON_DINT = "addonDint";
	static final String PROPERTY_ADDON_DINT_USER_TEMPLATE_PREFIX = "addonDintUserTemplatePrefix";
	static final String PROPERTY_ADDON_DINT_TENANT_PREFIX = "addonDintTenantPrefix";
	static final String PROPERTY_ADDON_DMDV = "addonDmdv";
	static final String PROPERTY_ADDON_DPRA = "addonDpra";
	static final String PROPERTY_ADDON_DPRA_TENANT_PREFIX = "addonDpraTenantPrefix";
	static final String PROPERTY_ADDON_DQID = "addonDqid";
	static final String PROPERTY_ADDON_DQID_TENANT_PREFIX = "addonDqidTenantPrefix";
	/** @deprecated HMFH is no longer supported. */
	@Deprecated
	static final String PROPERTY_ADDON_HMFH = "addonHmfh";
	static final String PROPERTY_ADDON_MAME = "addonMame";
	static final String PROPERTY_ADDON_MAME_TENANT_PREFIX = "addonMameTenantPrefix";
	/** @deprecated RPFL is no longer supported. */
	@Deprecated
	static final String PROPERTY_ADDON_RPFL = "addonRpfl";
	/** @deprecated TESE is no longer supported. */
	@Deprecated
	static final String PROPERTY_ADDON_TESE = "addonTese";

	static final String WILDCARD = "*";
	static final String FILE_NAME_SEPARATOR = "_";
	static final String LINE_SEP_TYPE_WINDOWS = "windows";
	static final String LINE_SEP_TYPE_UNIX = "unix";

	// Multiple add-ons use the same prefix to indicate a record that came pre-installed with the add-on
	static final String ADDON_BUILT_IN_RECORD_PREFIX = "[ON]";

	static final String TENANT_POLICY_SINGLE = "single";
	static final String TENANT_POLICY_MULTI = "multi";
	static final String TENANT_POLICY_MULTI_ADMIN = "multi-admin";
}
