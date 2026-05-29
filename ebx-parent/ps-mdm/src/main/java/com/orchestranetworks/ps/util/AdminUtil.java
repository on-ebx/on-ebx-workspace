/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.history.repository.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.service.preferences.*;
import com.orchestranetworks.ui.perspective.preferences.*;
import com.orchestranetworks.workflowLauncher.instance.constants.*;

/**
 * A utility class for use with administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AdminUtil
{
	private static final String DIRECTORY_DATA_SPACE = "ebx-directory";
	private static final String DIRECTORY_DATA_SET = "ebx-directory";
	private static final Path DIRECTORY_USERS_TABLE_PATH = DirectoryPaths._Directory_Users
		.getPathInSchema();
	private static final Path DIRECTORY_USERS_LOGIN_PATH = DirectoryPaths._Directory_Users._Login;
	private static final Path DIRECTORY_USERS_PASSWORD_LAST_UPDATE_PATH = DirectoryPaths._Directory_Users._PasswordLastUpdate;
	private static final Path DIRECTORY_USERS_EMAIL_PATH = DirectoryPaths._Directory_Users._Email;
	private static final Path DIRECTORY_USERS_BUILT_IN_ROLES_ADMINISTRATOR_PATH = DirectoryPaths._Directory_Users._BuiltInRoles_Administrator;
	private static final Path DIRECTORY_USERS_ROLES_TABLE_PATH = DirectoryPaths._Directory_UsersRoles
		.getPathInSchema();
	private static final Path DIRECTORY_USERS_ROLES_ROLE_PATH = DirectoryPaths._Directory_UsersRoles._Role;
	private static final Path DIRECTORY_USERS_ROLES_USER_PATH = DirectoryPaths._Directory_UsersRoles._User;
	private static final Path DIRECTORY_ROLES_TABLE_PATH = DirectoryPaths._Directory_Roles
		.getPathInSchema();
	private static final Path DIRECTORY_ROLES_NAME_PATH = DirectoryPaths._Directory_Roles._Name;
	private static final Path DIRECTORY_ROLES_EMAIL_PATH = DirectoryPaths._Directory_Roles._Email;
	private static final Path DIRECTORY_ROLES_INCLUSIONS_TABLE_PATH = DirectoryPaths._Directory_RolesInclusions
		.getPathInSchema();
	private static final Path DIRECTORY_ROLES_INCLUSIONS_ENCOMPASSING_ROLE_PATH = DirectoryPaths._Directory_RolesInclusions._EncompassingRole;
	private static final Path DIRECTORY_ROLES_INCLUSIONS_INCLUDED_ROLE_PATH = DirectoryPaths._Directory_RolesInclusions._IncludedRole;
	private static final Path DIRECTORY_SALUTATIONS_TABLE_PATH = DirectoryPaths._Directory_Salutations
		.getPathInSchema();
	private static final Path DIRECTORY_MAILING_LIST_GROUP_PATH = DirectoryPaths._Directory_MailingList;
	private static final Path DIRECTORY_POLICY_GROUP_PATH = DirectoryPaths._Directory_Policy;

	private static final String USER_PREFERENCES_DATA_SPACE = "ebx-preferences";
	private static final String USER_PREFERENCES_DATA_SET = "ebx-preferences";

	private static final String WORKFLOW_MODELS_DATA_SPACE = "ebx-workflow-definitions";
	private static final String WORKFLOW_MODELS_SCHEMA_LOCATION = "urn:ebx:module:ebx-root-1.0:/WEB-INF/ebx/schemas/workflow/workflow_definition_1.1.xsd";
	private static final Path WORKFLOW_MODEL_MODULE_NAME_PATH = Path
		.parse("/root/definition/process/moduleName");
	private static final String WORKFLOW_MODELS_CONFIGURATION_DATA_SET = "configuration";
	private static final Path WORKFLOW_MODELS_CONFIGURATION_MESSAGE_TEMPLATE_TABLE_PATH = Path
		.parse("/root/configuration/notificationMessage");
	private static final Path WORKFLOW_MODELS_CONFIGURATION_MESSAGE_TEMPLATE_ID_PATH = Path
		.parse("./id");

	private static final Path WORKFLOW_ADMIN_CONFIGURATION_INTERFACE_CUSTOMIZATION_GROUP_PATH = Path
		.parse("/root/technicalConfiguration/ViewsCustomization");
	private static final Path WORKFLOW_ADMIN_CONFIGURATION_PRIORITIES_CONFIGURATION_GROUP_PATH = Path
		.parse("/root/technicalConfiguration/PrioritiesConfigurationSection");

	private static final String DMA_DATA_SPACE = "ebx-dma";
	private static final String DMA_MODULE_NAME = "ebx-dma";
	private static final Path DMA_SCHEMA_MODULE_NAME_PATH = Path.parse("/root/schema/moduleName");

	private static final String GLOBAL_PERMISSIONS_DATA_SPACE = "ebx-globalPermissions";
	private static final String GLOBAL_PERMISSIONS_DATA_SET = "ebx-globalPermissions";
	private static final Path GLOBAL_PERMISSIONS_TABLE_PATH = Path
		.parse("/globalPermissions/globalPermissionsTable");

	private static final Path PERSPECTIVES_MENU_GROUP_PATH = Path.parse("/domain/menuItem");
	private static final Path PERSPECTIVES_ALLOWED_PROFILES_GROUP_PATH = Path
		.parse("/domain/properties");
	private static final Path PERSPECTIVES_ERGONOMICS_GROUP_PATH = Path
		.parse("/domain/ergonomicPolicy");
	private static final Path PERSPECTIVES_ERGONOMICS_HTML_EDITOR_HEIGHT_PATH = Path
		.parse("./domain/ergonomicPolicy/_customCSS_htmlEditorHeight");
	private static final Path PERSPECTIVES_ERGONOMICS_HTML_EDITOR_WIDTH_PATH = Path
		.parse("./domain/ergonomicPolicy/_customCSS_htmlEditorWidth");
	private static final Path PERSPECTIVES_ERGONOMICS_HTML_EDITOR_MAX_HEIGHT_PATH = Path
		.parse("./domain/ergonomicPolicy/_customCSS_htmlEditorMaxHeight");
	private static final Path PERSPECTIVES_ERGONOMICS_WELCOME_MESSAGE_PATH = Path
		.parse("./domain/ergonomicPolicy/welcomeMessage");
	private static final Path PERSPECTIVES_ERGONOMICS_WINDOW_NAME_PATH = Path
		.parse("./domain/ergonomicPolicy/windowName");
	private static final Path PERSPECTIVES_DEFAULT_OPTIONS_GROUP_PATH = Path
		.parse("/domain/optionsDefault");
	private static final Path PERSPECTIVES_COLORS_GROUP_PATH = Path.parse("/domain/customCSS");
	private static final Path PERSPECTIVES_COLORS_LOGO_URL_SVG_PATH = Path
		.parse("./domain/customCSS/customCSS_logoUrlSVG");
	private static final Path PERSPECTIVES_COLORS_LOGO_URL_PNG_PATH = Path
		.parse("./domain/customCSS/customCSS_logoUrlPNG");
	private static final Path PERSPECTIVES_COLORS_LOGO_URL_GIF_JPG_PATH = Path
		.parse("./domain/customCSS/customCSS_logoUrl");

	private static final String PERSPECTIVES_DATA_SPACE = "ebx-manager";
	private static final String PERSPECTIVES_DATA_SET = "ebx-manager";

	private static final String PERSPECTIVE_PREFS_DATA_SPACE = "ebx-perspectivesPreferences";
	private static final String PERSPECTIVE_PREFS_DATA_SET = "ebx-perspectivesPreferences";
	private static final Path PERSPECTIVE_RECOM_TABLE = PpvPreferencesPaths._Root_RecommendedPerspectives
		.getPathInSchema();

	private static final String VIEWS_DATA_SPACE = "ebx-views";
	private static final String VIEWS_DATA_SET = "ebx-views";
	private static final Path CUSTOM_VIEWS_TABLE_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews
		.getPathInSchema();
	private static final Path CUSTOM_VIEWS_SCHEMA_KEY_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._SchemaKey;
	private static final Path CUSTOM_VIEWS_OWNER_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._Owner;
	private static final Path CUSTOM_VIEWS_PUBLICATION_NAME_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._PublicationName;
	private static final Path CUSTOM_VIEWS_TABLE_PATH_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._TablePath;
	private static final Path CUSTOM_VIEWS_DOCUMENTATION_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._Documentation;
	private static final Path CUSTOM_VIEWS_FILTER_EXPRESSION_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViews._TabularViewOnSingleTable_Predicate_FilterExpression;
	private static final Path DEFAULT_VIEWS_TABLE_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViewsPreferences
		.getPathInSchema();
	private static final Path DEFAULT_VIEWS_SCHEMA_KEY_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViewsPreferences._SchemaKey;
	private static final Path DEFAULT_VIEWS_VIEW_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViewsPreferences._ViewId;
	private static final Path VIEWS_GROUPS_TABLE_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViewsGroup
		.getPathInSchema();
	private static final Path VIEWS_GROUPS_SCHEMA_KEY_PATH = ViewsPreferencesPaths_5_0._Preferences_TableViewsGroup._SchemaKey;
	private static final Path VIEWS_PERMISSIONS_TABLE_PATH = ViewsPreferencesPaths_5_0._Preferences_ViewsPermissions
		.getPathInSchema();
	private static final Path VIEWS_PERMISSIONS_SCHEMA_KEY_PATH = ViewsPreferencesPaths_5_0._Preferences_ViewsPermissions._Schema;

	private static final String INTERACTIONS_DATA_SPACE = "ebx-interactions";
	private static final String INTERACTIONS_DATA_SET = "ebx-interactions";

	private static final String WORKFLOW_HISTORY_DATA_SPACE = "ebx-workflow-history";
	private static final String WORKFLOW_HISTORY_DATA_SET = "history";

	private static final String WORKFLOW_EXECUTION_DATA_SPACE = "ebx-workflow-execution";
	private static final String WORKFLOW_EXECUTION_DATA_SET = "execution";

	private static final String WORKFLOW_LAUNCHERS_DATA_SPACE = "workflow_launchers";
	private static final String WORKFLOW_LAUNCHERS_DATA_SET = "workflow_launchers_1.0";
	private static final Path WORKFLOW_LAUNCHERS_LAUNCHERS_TABLE_PATH = WorkflowLaunchers_0_0._Workflow_launchers_Definition
		.getPathInSchema();
	private static final Path WORKFLOW_LAUNCHERS_LAUNCHERS_WORKFLOW_PUBLICATION_NAME_PATH = WorkflowLaunchers_0_0._Workflow_launchers_Definition._Workflow_publication_name;
	private static final Path WORKFLOW_LAUNCHERS_ACTIVATIONS_TABLE_PATH = WorkflowLaunchers_0_0._Workflow_launchers_Association
		.getPathInSchema();
	private static final Path WORKFLOW_LAUNCHERS_ACTIVATIONS_DATA_MODEL_PATH = WorkflowLaunchers_0_0._Workflow_launchers_Association._Data_model;

	private static final String LINEAGE_DATA_SPACE = "ebx-dataLineage";
	private static final String LINEAGE_DATA_SET = "ebx-dataLineage";

	private static final String AUTO_INCREMENTS_DATA_SPACE = "ebx-autoIncrements";
	private static final String AUTO_INCREMENTS_DATA_SET = "ebx-autoIncrements";

	private static final String DATA_SPACES_DATA_SPACE = "ebx-dataSpaces";
	private static final String DATA_SPACES_DATA_SET = "ebx-dataSpaces";

	private static final String DATABASE_MAPPING_DATA_SPACE = "ebx-dms";
	private static final String DATABASE_MAPPING_DATA_SET = "ebx-dms";

	private static final String HISTORY_DATA_SPACE = "ebx-history";
	private static final String HISTORY_DATA_SET = "ebx-history";
	private static final Path HISTORIZATION_PROFILE_TABLE_PATH = HvmHistory_1_0._History_Profiles
		.getPathInSchema();
	private static final Path HISTORIZATION_PROFILE_BRANCHES_CONFIGURATIONS_PATH = HvmHistory_1_0._History_Profiles._BranchesConfigurations;

	private static final String[] BUILT_IN_HISTORIZATION_PROFILES = { "ebx-allBranches",
			"ebx-allBranches-lax", "ebx-allBranches-strict", "ebx-referenceBranch",
			"ebx-referenceBranch-lax", "ebx-referenceBranch-strict" };

	private static final String ADDONS_REGISTRATION_DATA_SPACE = "ebx-addons";
	private static final String ADDONS_REGISTRATION_DATA_SET = "ebx-addons";
	private static final Path REGISTERED_ADDONS_TABLE_PATH = Path.parse("/addons/catalog");

	private static final String DATA_MODELING_DATA_SPACE = "ebx-dataModels-publications";
	private static final String DATA_MODELING_DATA_SET = "ebx-publications";

	private static final String EVENT_BROKER_DATA_SPACE = "ebx-event-broker";
	private static final String EVENT_BROKER_DATA_SET = "ebx-event-broker";

	private static final String TASK_SCHEDULER_DATA_SPACE = "ebx-scheduler";
	private static final String TASK_SCHEDULER_DATA_SET = "ebx-scheduler";
	private static final Path TASKS_TABLE_PATH = Path.parse("/scheduler/task");
	private static final Path TASKS_NAME_PATH = Path.parse("./name");
	private static final Path TASKS_MODULE_PATH = Path.parse("./spec/module");
	private static final String TASKS_NAME_VALUE_FOR_REPOSITORY_CLEANUP = "Repository clean-up";

	private static final String ADDON_DMDV_DATA_SPACE = "ebx-addon-dmdv-configuration";
	private static final String ADDON_DMDV_DATA_SET = "ebx-addon-dmdv-configuration";

	private static final String ADDON_DINT_DATA_SPACE = "ebx-addon-dint";
	private static final String ADDON_DINT_DATA_SET = "ebx-addon-dint-configuration";

	public static Set<AdaptationHome> getAdminDataSpaces(Repository repo)
	{
		Set<AdaptationHome> dataSpaces = new HashSet<>();
		dataSpaces.add(getDirectoryDataSpace(repo));
		dataSpaces.add(getUserPreferencesDataSpace(repo));
		dataSpaces.add(getGlobalPermissionsDataSpace(repo));
		dataSpaces.add(getPerspectivesDataSpace(repo));
		dataSpaces.add(getViewsDataSpace(repo));
		dataSpaces.add(getInteractionsDataSpace(repo));
		dataSpaces.add(getWorkflowHistoryDataSpace(repo));
		dataSpaces.add(getWorkflowExecutionDataSpace(repo));
		dataSpaces.add(getLineageDataSpace(repo));
		dataSpaces.add(getAutoIncrementsDataSpace(repo));
		dataSpaces.add(getDataSpacesDataSpace(repo));
		dataSpaces.add(getDatabaseMappingDataSpace(repo));
		dataSpaces.add(getHistoryDataSpace(repo));
		dataSpaces.add(getAddonsRegistrationDataSpace(repo));
		dataSpaces.add(getDataModelingDataSpace(repo));
		dataSpaces.add(getEventBrokerDataSpace(repo));
		dataSpaces.add(getTaskSchedulerDataSpace(repo));
		return dataSpaces;
	}

	public static Set<Adaptation> getAdminDataSets(Repository repo)
	{
		Set<Adaptation> dataSets = new HashSet<>();
		dataSets.add(getDirectoryDataSet(repo));
		dataSets.add(getUserPreferencesDataSet(repo));
		dataSets.add(getGlobalPermissionsDataSet(repo));
		dataSets.add(getPerspectivesDataSet(repo));
		dataSets.add(getViewsDataSet(repo));
		dataSets.add(getInteractionsDataSet(repo));
		dataSets.add(getWorkflowHistoryDataSet(repo));
		dataSets.add(getWorkflowExecutionDataSet(repo));
		dataSets.add(getLineageDataSet(repo));
		dataSets.add(getAutoIncrementsDataSet(repo));
		// Note that we can't include the "data spaces" data space itself, because importing into
		// that is forbidden & results in an exception. That has to be handled manually.
		// If it were allowed, we could add this line: dataSets.add(getDataSpacesDataSet(repo));
		dataSets.add(getDatabaseMappingDataSet(repo));
		dataSets.add(getHistoryDataSet(repo));
		dataSets.add(getAddonsRegistrationDataSet(repo));
		dataSets.add(getDataModelingDataSet(repo));
		dataSets.add(getEventBrokerDataSet(repo));
		dataSets.add(getTaskSchedulerDataSet(repo));
		return dataSets;
	}

	public static AdaptationHome getDirectoryDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(DIRECTORY_DATA_SPACE));
	}

	public static Adaptation getDirectoryDataSet(Repository repo)
	{
		AdaptationHome directoryDataSpace = getDirectoryDataSpace(repo);
		return getDirectoryDataSet(directoryDataSpace);
	}

	public static Adaptation getDirectoryDataSet(AdaptationHome directoryDataSpace)
	{
		return directoryDataSpace.findAdaptationOrNull(AdaptationName.forName(DIRECTORY_DATA_SET));
	}

	public static AdaptationTable getDirectoryUsersTable(Repository repo)
	{
		Adaptation directoryDataSet = getDirectoryDataSet(repo);
		return getDirectoryUsersTable(directoryDataSet);
	}

	public static AdaptationTable getDirectoryUsersTable(Adaptation directoryDataSet)
	{
		return directoryDataSet.getTable(DIRECTORY_USERS_TABLE_PATH);
	}

	public static Path getDirectoryUsersPasswordLastUpdatePath()
	{
		return DIRECTORY_USERS_PASSWORD_LAST_UPDATE_PATH;
	}

	public static Path getDirectoryUsersLoginPath()
	{
		return DIRECTORY_USERS_LOGIN_PATH;
	}

	public static Path getDirectoryUsersEmailPath()
	{
		return DIRECTORY_USERS_EMAIL_PATH;
	}

	public static Path getDirectoryUsersBuiltInRolesAdministratorPath()
	{
		return DIRECTORY_USERS_BUILT_IN_ROLES_ADMINISTRATOR_PATH;
	}

	public static AdaptationTable getDirectoryUsersRolesTable(Adaptation directoryDataSet)
	{
		return directoryDataSet.getTable(DIRECTORY_USERS_ROLES_TABLE_PATH);
	}

	public static Path getDirectoryUsersRolesRolePath()
	{
		return DIRECTORY_USERS_ROLES_ROLE_PATH;
	}

	public static Path getDirectoryUsersRolesUserPath()
	{
		return DIRECTORY_USERS_ROLES_USER_PATH;
	}

	public static AdaptationTable getDirectoryRolesInclusionsTable(Adaptation directoryDataSet)
	{
		return directoryDataSet.getTable(DIRECTORY_ROLES_INCLUSIONS_TABLE_PATH);
	}

	public static Path getDirectoryRolesInclusionsEncompassingRolePath()
	{
		return DIRECTORY_ROLES_INCLUSIONS_ENCOMPASSING_ROLE_PATH;
	}

	public static Path getDirectoryRolesInclusionsIncludedRolePath()
	{
		return DIRECTORY_ROLES_INCLUSIONS_INCLUDED_ROLE_PATH;
	}

	public static AdaptationTable getDirectoryRolesTable(Repository repo)
	{
		Adaptation directoryDataSet = getDirectoryDataSet(repo);
		return getDirectoryRolesTable(directoryDataSet);
	}

	public static AdaptationTable getDirectoryRolesTable(Adaptation directoryDataSet)
	{
		return directoryDataSet.getTable(DIRECTORY_ROLES_TABLE_PATH);
	}

	public static Path getDirectoryRolesNamePath()
	{
		return DIRECTORY_ROLES_NAME_PATH;
	}

	public static Path getDirectoryRolesEmailPath()
	{
		return DIRECTORY_ROLES_EMAIL_PATH;
	}

	public static AdaptationTable getDirectorySalutationsTable(Repository repo)
	{
		Adaptation directoryDataSet = getDirectoryDataSet(repo);
		return getDirectorySalutationsTable(directoryDataSet);
	}

	public static AdaptationTable getDirectorySalutationsTable(Adaptation directoryDataSet)
	{
		return directoryDataSet.getTable(DIRECTORY_SALUTATIONS_TABLE_PATH);
	}

	public static SchemaNode getDirectoryMailingListGroup(Adaptation directoryDataSet)
	{
		return directoryDataSet.getSchemaNode().getNode(DIRECTORY_MAILING_LIST_GROUP_PATH);
	}

	public static SchemaNode getDirectoryPolicyGroup(Adaptation directoryDataSet)
	{
		return directoryDataSet.getSchemaNode().getNode(DIRECTORY_POLICY_GROUP_PATH);
	}

	public static AdaptationHome getUserPreferencesDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(USER_PREFERENCES_DATA_SPACE));
	}

	public static Adaptation getUserPreferencesDataSet(Repository repo)
	{
		AdaptationHome userPreferencesDataSpace = getUserPreferencesDataSpace(repo);
		return getUserPreferencesDataSet(userPreferencesDataSpace);
	}

	public static Adaptation getUserPreferencesDataSet(AdaptationHome userPreferencesDataSpace)
	{
		return userPreferencesDataSpace
			.findAdaptationOrNull(AdaptationName.forName(USER_PREFERENCES_DATA_SET));
	}

	public static AdaptationHome getWorkflowModelsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(WORKFLOW_MODELS_DATA_SPACE));
	}

	public static Adaptation getWorkflowModelsConfigurationDataSet(Repository repo)
	{
		AdaptationHome workflowModelsDataSpace = getWorkflowModelsDataSpace(repo);
		return getWorkflowModelsConfigurationDataSet(workflowModelsDataSpace);
	}

	public static Adaptation getWorkflowModelsConfigurationDataSet(
		AdaptationHome workflowModelsDataSpace)
	{
		return workflowModelsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(WORKFLOW_MODELS_CONFIGURATION_DATA_SET));
	}

	public static SchemaLocation getWorkflowModelsSchemaLocation()
	{
		return SchemaLocation.parse(WORKFLOW_MODELS_SCHEMA_LOCATION);
	}

	public static Path getWorkflowModelModuleNamePath()
	{
		return WORKFLOW_MODEL_MODULE_NAME_PATH;
	}

	public static Path getWorkflowModelsConfigurationMessageTemplateTablePath()
	{
		return WORKFLOW_MODELS_CONFIGURATION_MESSAGE_TEMPLATE_TABLE_PATH;
	}

	public static Path getWorkflowModelsConfigurationMessageTemplateIdPath()
	{
		return WORKFLOW_MODELS_CONFIGURATION_MESSAGE_TEMPLATE_ID_PATH;
	}

	public static AdaptationHome getWorkflowAdminConfigurationDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(WORKFLOW_EXECUTION_DATA_SPACE));
	}

	public static Adaptation getWorkflowAdminConfigurationDataSet(Repository repo)
	{
		AdaptationHome workflowAdminConfigurationDataSpace = getWorkflowAdminConfigurationDataSpace(
			repo);
		return getWorkflowAdminConfigurationDataSet(workflowAdminConfigurationDataSpace);
	}

	public static Adaptation getWorkflowAdminConfigurationDataSet(
		AdaptationHome workflowAdminConfigurationDataSpace)
	{
		return workflowAdminConfigurationDataSpace
			.findAdaptationOrNull(AdaptationName.forName(WORKFLOW_EXECUTION_DATA_SET));
	}

	public static SchemaNode getWorkflowAdminConfigurationInterfaceCustomizationGroup(
		Adaptation workflowAdminConfigurationDataSet)
	{
		return workflowAdminConfigurationDataSet.getSchemaNode()
			.getNode(WORKFLOW_ADMIN_CONFIGURATION_INTERFACE_CUSTOMIZATION_GROUP_PATH);
	}

	public static SchemaNode getWorkflowAdminConfigurationPrioritiesConfigurationGroup(
		Adaptation workflowAdminConfigurationDataSet)
	{
		return workflowAdminConfigurationDataSet.getSchemaNode()
			.getNode(WORKFLOW_ADMIN_CONFIGURATION_PRIORITIES_CONFIGURATION_GROUP_PATH);
	}

	public static AdaptationHome getDMADataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(DMA_DATA_SPACE));
	}

	public static String getDMAModuleName()
	{
		return DMA_MODULE_NAME;
	}

	public static Path getDMASchemaModuleNamePath()
	{
		return DMA_SCHEMA_MODULE_NAME_PATH;
	}

	public static AdaptationHome getGlobalPermissionsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(GLOBAL_PERMISSIONS_DATA_SPACE));
	}

	public static Adaptation getGlobalPermissionsDataSet(Repository repo)
	{
		AdaptationHome globalPermissionsDataSpace = getGlobalPermissionsDataSpace(repo);
		return getGlobalPermissionsDataSet(globalPermissionsDataSpace);
	}

	public static Adaptation getGlobalPermissionsDataSet(AdaptationHome globalPermissionsDataSpace)
	{
		return globalPermissionsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(GLOBAL_PERMISSIONS_DATA_SET));
	}

	public static AdaptationTable getGlobalPermissionsTable(Adaptation globalPermissionsDataSet)
	{
		return globalPermissionsDataSet.getTable(GLOBAL_PERMISSIONS_TABLE_PATH);
	}

	public static AdaptationHome getPerspectivesDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(PERSPECTIVES_DATA_SPACE));
	}

	public static Adaptation getPerspectivesDataSet(Repository repo)
	{
		AdaptationHome perspectivesDataSpace = getPerspectivesDataSpace(repo);
		return getPerspectivesDataSet(perspectivesDataSpace);
	}

	public static Adaptation getPerspectivesDataSet(AdaptationHome perspectivesDataSpace)
	{
		return perspectivesDataSpace
			.findAdaptationOrNull(AdaptationName.forName(PERSPECTIVES_DATA_SET));
	}

	public static SchemaNode getPerspectivesAllowedProfilesGroup(Adaptation perspectivesDataSet)
	{
		return perspectivesDataSet.getSchemaNode()
			.getNode(PERSPECTIVES_ALLOWED_PROFILES_GROUP_PATH);
	}

	public static SchemaNode getPerspectivesMenuGroup(Adaptation perspectivesDataSet)
	{
		return perspectivesDataSet.getSchemaNode().getNode(PERSPECTIVES_MENU_GROUP_PATH);
	}

	public static SchemaNode getPerspectivesErgonomicsGroup(Adaptation perspectivesDataSet)
	{
		return perspectivesDataSet.getSchemaNode().getNode(PERSPECTIVES_ERGONOMICS_GROUP_PATH);
	}

	public static Path getPerspectivesErgonomicsHtmlEditorHeightPath()
	{
		return PERSPECTIVES_ERGONOMICS_HTML_EDITOR_HEIGHT_PATH;
	}

	public static Path getPerspectivesErgonomicsHtmlEditorWidthPath()
	{
		return PERSPECTIVES_ERGONOMICS_HTML_EDITOR_WIDTH_PATH;
	}

	public static Path getPerspectivesErgonomicsHtmlEditorMaxHeightPath()
	{
		return PERSPECTIVES_ERGONOMICS_HTML_EDITOR_MAX_HEIGHT_PATH;
	}

	public static Path getPerspectivesErgonomicsWelcomeMessagePath()
	{
		return PERSPECTIVES_ERGONOMICS_WELCOME_MESSAGE_PATH;
	}

	public static Path getPerspectivesErgonomicsWindowNamePath()
	{
		return PERSPECTIVES_ERGONOMICS_WINDOW_NAME_PATH;
	}

	public static SchemaNode getPerspectivesDefaultOptionsGroup(Adaptation perspectivesDataSet)
	{
		return perspectivesDataSet.getSchemaNode().getNode(PERSPECTIVES_DEFAULT_OPTIONS_GROUP_PATH);
	}

	public static SchemaNode getPerspectivesColorsGroup(Adaptation perspectivesDataSet)
	{
		return perspectivesDataSet.getSchemaNode().getNode(PERSPECTIVES_COLORS_GROUP_PATH);
	}

	public static Path getPerspectivesColorsLogoUrlSvgPath()
	{
		return PERSPECTIVES_COLORS_LOGO_URL_SVG_PATH;
	}

	public static Path getPerspectivesColorsLogoUrlPngPath()
	{
		return PERSPECTIVES_COLORS_LOGO_URL_PNG_PATH;
	}

	public static Path getPerspectivesColorsLogoUrlGifJpgPath()
	{
		return PERSPECTIVES_COLORS_LOGO_URL_GIF_JPG_PATH;
	}

	public static AdaptationHome getPerspectivePrefsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(PERSPECTIVE_PREFS_DATA_SPACE));
	}

	public static Adaptation getPerspectivePrefsDataSet(Repository repo)
	{
		AdaptationHome perspectivePrefsDataSpace = getPerspectivePrefsDataSpace(repo);
		return getPerspectivePrefsDataSet(perspectivePrefsDataSpace);
	}

	public static Adaptation getPerspectivePrefsDataSet(AdaptationHome perspectivePrefsDataSpace)
	{
		return perspectivePrefsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(PERSPECTIVE_PREFS_DATA_SET));
	}

	public static AdaptationTable getRecommendedPerspectivesTable(
		Adaptation perspectivePrefsDataSet)
	{
		return perspectivePrefsDataSet.getTable(PERSPECTIVE_RECOM_TABLE);
	}

	public static AdaptationHome getViewsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(VIEWS_DATA_SPACE));
	}

	public static Adaptation getViewsDataSet(Repository repo)
	{
		AdaptationHome viewsDataSpace = getViewsDataSpace(repo);
		return getViewsDataSet(viewsDataSpace);
	}

	public static Adaptation getViewsDataSet(AdaptationHome viewsDataSpace)
	{
		return viewsDataSpace.findAdaptationOrNull(AdaptationName.forName(VIEWS_DATA_SET));
	}

	public static AdaptationTable getCustomViewsTable(Adaptation viewsDataSet)
	{
		return viewsDataSet.getTable(CUSTOM_VIEWS_TABLE_PATH);
	}

	public static Path getCustomViewsSchemaKeyPath()
	{
		return CUSTOM_VIEWS_SCHEMA_KEY_PATH;
	}

	public static Path getCustomViewsOwnerPath()
	{
		return CUSTOM_VIEWS_OWNER_PATH;
	}

	public static Path getCustomViewsPublicationNamePath()
	{
		return CUSTOM_VIEWS_PUBLICATION_NAME_PATH;
	}

	public static Path getCustomViewsTablePathPath()
	{
		return CUSTOM_VIEWS_TABLE_PATH_PATH;
	}

	public static Path getCustomViewsDocumentationPath()
	{
		return CUSTOM_VIEWS_DOCUMENTATION_PATH;
	}

	public static Path getCustomViewsFilterExpressionPath()
	{
		return CUSTOM_VIEWS_FILTER_EXPRESSION_PATH;
	}

	public static AdaptationTable getDefaultViewsTable(Adaptation viewsDataSet)
	{
		return viewsDataSet.getTable(DEFAULT_VIEWS_TABLE_PATH);
	}

	/**
	 * @deprecated This isn't used any more
	 */
	@Deprecated
	public static Path getDefaultViewsSchemaKeyPath()
	{
		return DEFAULT_VIEWS_SCHEMA_KEY_PATH;
	}

	public static Path getDefaultViewsViewPath()
	{
		return DEFAULT_VIEWS_VIEW_PATH;
	}

	public static AdaptationTable getViewsGroupsTable(Adaptation viewsDataSet)
	{
		return viewsDataSet.getTable(VIEWS_GROUPS_TABLE_PATH);
	}

	public static Path getViewsGroupsSchemaKeyPath()
	{
		return VIEWS_GROUPS_SCHEMA_KEY_PATH;
	}

	public static AdaptationTable getViewsPermissionsTable(Adaptation viewsDataSet)
	{
		return viewsDataSet.getTable(VIEWS_PERMISSIONS_TABLE_PATH);
	}

	public static Path getViewsPermissionsSchemaKeyPath()
	{
		return VIEWS_PERMISSIONS_SCHEMA_KEY_PATH;
	}

	public static AdaptationHome getInteractionsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(INTERACTIONS_DATA_SPACE));
	}

	public static Adaptation getInteractionsDataSet(Repository repo)
	{
		AdaptationHome interactionsDataSpace = getInteractionsDataSpace(repo);
		return getInteractionsDataSet(interactionsDataSpace);
	}

	public static Adaptation getInteractionsDataSet(AdaptationHome interactionsDataSpace)
	{
		return interactionsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(INTERACTIONS_DATA_SET));
	}

	public static AdaptationHome getWorkflowHistoryDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(WORKFLOW_HISTORY_DATA_SPACE));
	}

	public static Adaptation getWorkflowHistoryDataSet(Repository repo)
	{
		AdaptationHome workflowHistoryDataSpace = getWorkflowHistoryDataSpace(repo);
		return getWorkflowHistoryDataSet(workflowHistoryDataSpace);
	}

	public static Adaptation getWorkflowHistoryDataSet(AdaptationHome workflowHistoryDataSpace)
	{
		return workflowHistoryDataSpace
			.findAdaptationOrNull(AdaptationName.forName(WORKFLOW_HISTORY_DATA_SET));
	}

	public static AdaptationHome getWorkflowExecutionDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(WORKFLOW_EXECUTION_DATA_SPACE));
	}

	public static Adaptation getWorkflowExecutionDataSet(Repository repo)
	{
		AdaptationHome workflowExecutionDataSpace = getWorkflowExecutionDataSpace(repo);
		return getWorkflowExecutionDataSet(workflowExecutionDataSpace);
	}

	public static Adaptation getWorkflowExecutionDataSet(AdaptationHome workflowExecutionDataSpace)
	{
		return workflowExecutionDataSpace
			.findAdaptationOrNull(AdaptationName.forName(WORKFLOW_EXECUTION_DATA_SET));
	}

	public static AdaptationHome getWorkflowLaunchersDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(WORKFLOW_LAUNCHERS_DATA_SPACE));
	}

	public static Adaptation getWorkflowLaunchersDataSet(Repository repo)
	{
		AdaptationHome workflowLaunchersDataSpace = getWorkflowLaunchersDataSpace(repo);
		return getWorkflowLaunchersDataSet(workflowLaunchersDataSpace);
	}

	public static Adaptation getWorkflowLaunchersDataSet(AdaptationHome workflowLaunchersDataSpace)
	{
		return workflowLaunchersDataSpace
			.findAdaptationOrNull(AdaptationName.forName(WORKFLOW_LAUNCHERS_DATA_SET));
	}

	public static AdaptationTable getWorkflowLaunchersLaunchersTable(
		Adaptation workflowLaunchersDataSet)
	{
		return workflowLaunchersDataSet.getTable(WORKFLOW_LAUNCHERS_LAUNCHERS_TABLE_PATH);
	}

	public static Path getWorkflowLaunchersLaunchersWorkflowPublicationNamePath()
	{
		return WORKFLOW_LAUNCHERS_LAUNCHERS_WORKFLOW_PUBLICATION_NAME_PATH;
	}

	public static Path getWorkflowLaunchersActivationsDataModelPath()
	{
		return WORKFLOW_LAUNCHERS_ACTIVATIONS_DATA_MODEL_PATH;
	}

	public static AdaptationTable getWorkflowLaunchersActivationsTable(
		Adaptation workflowLaunchersDataSet)
	{
		return workflowLaunchersDataSet.getTable(WORKFLOW_LAUNCHERS_ACTIVATIONS_TABLE_PATH);
	}

	public static List<String> getAllWorkflowModelNames(Repository repo)
	{
		AdaptationHome wfModelDataSpace = getWorkflowModelsDataSpace(repo);
		List<Adaptation> dataSets = wfModelDataSpace.findAllRoots();
		ArrayList<String> dataSetNames = new ArrayList<>();
		for (Adaptation dataSet : dataSets)
		{
			dataSetNames.add(dataSet.getAdaptationName().getStringName());
		}
		return dataSetNames;
	}

	public static AdaptationHome getLineageDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(LINEAGE_DATA_SPACE));
	}

	public static Adaptation getLineageDataSet(Repository repo)
	{
		AdaptationHome lineageDataSpace = getLineageDataSpace(repo);
		return getLineageDataSet(lineageDataSpace);
	}

	public static Adaptation getLineageDataSet(AdaptationHome lineageDataSpace)
	{
		return lineageDataSpace.findAdaptationOrNull(AdaptationName.forName(LINEAGE_DATA_SET));
	}

	public static AdaptationHome getAutoIncrementsDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(AUTO_INCREMENTS_DATA_SPACE));
	}

	public static Adaptation getAutoIncrementsDataSet(Repository repo)
	{
		AdaptationHome autoIncrementsDataSpace = getAutoIncrementsDataSpace(repo);
		return getAutoIncrementsDataSet(autoIncrementsDataSpace);
	}

	public static Adaptation getAutoIncrementsDataSet(AdaptationHome autoIncrementsDataSpace)
	{
		return autoIncrementsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(AUTO_INCREMENTS_DATA_SET));
	}

	public static AdaptationHome getDataSpacesDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(DATA_SPACES_DATA_SPACE));
	}

	public static Adaptation getDataSpacesDataSet(Repository repo)
	{
		AdaptationHome dataSpacesDataSpace = getDataSpacesDataSpace(repo);
		return getDataSpacesDataSet(dataSpacesDataSpace);
	}

	public static Adaptation getDataSpacesDataSet(AdaptationHome dataSpacesDataSpace)
	{
		return dataSpacesDataSpace
			.findAdaptationOrNull(AdaptationName.forName(DATA_SPACES_DATA_SET));
	}

	public static AdaptationHome getDatabaseMappingDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(DATABASE_MAPPING_DATA_SPACE));
	}

	public static Adaptation getDatabaseMappingDataSet(Repository repo)
	{
		AdaptationHome databaseMappingDataSpace = getDatabaseMappingDataSpace(repo);
		return getDatabaseMappingDataSet(databaseMappingDataSpace);
	}

	public static Adaptation getDatabaseMappingDataSet(AdaptationHome databaseMappingDataSpace)
	{
		return databaseMappingDataSpace
			.findAdaptationOrNull(AdaptationName.forName(DATABASE_MAPPING_DATA_SET));
	}

	public static AdaptationHome getHistoryDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(HISTORY_DATA_SPACE));
	}

	public static Adaptation getHistoryDataSet(Repository repo)
	{
		AdaptationHome historyDataSpace = getHistoryDataSpace(repo);
		return getHistoryDataSet(historyDataSpace);
	}

	public static Adaptation getHistoryDataSet(AdaptationHome historyDataSpace)
	{
		return historyDataSpace.findAdaptationOrNull(AdaptationName.forName(HISTORY_DATA_SET));
	}

	public static AdaptationTable getHistorizationProfileTable(Adaptation historyDataSet)
	{
		return historyDataSet.getTable(HISTORIZATION_PROFILE_TABLE_PATH);
	}

	public static Path getHistorizationProfileBranchesConfigurationsPath()
	{
		return HISTORIZATION_PROFILE_BRANCHES_CONFIGURATIONS_PATH;
	}

	public static String[] getBuiltInHistorizationProfiles()
	{
		return BUILT_IN_HISTORIZATION_PROFILES;
	}

	public static AdaptationHome getAddonsRegistrationDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDONS_REGISTRATION_DATA_SPACE));
	}

	public static Adaptation getAddonsRegistrationDataSet(Repository repo)
	{
		AdaptationHome addonsRegistrationDataSpace = getAddonsRegistrationDataSpace(repo);
		return getAddonsRegistrationDataSet(addonsRegistrationDataSpace);
	}

	public static Adaptation getAddonsRegistrationDataSet(
		AdaptationHome addonsRegistrationDataSpace)
	{
		return addonsRegistrationDataSpace
			.findAdaptationOrNull(AdaptationName.forName(ADDONS_REGISTRATION_DATA_SET));
	}

	public static AdaptationTable getRegisteredAddonsTable(Adaptation addonsRegistrationDataSet)
	{
		return addonsRegistrationDataSet.getTable(REGISTERED_ADDONS_TABLE_PATH);
	}

	public static AdaptationHome getDataModelingDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(DATA_MODELING_DATA_SPACE));
	}

	public static Adaptation getDataModelingDataSet(Repository repo)
	{
		AdaptationHome dataModelingDataSpace = getDataModelingDataSpace(repo);
		return getDataModelingDataSet(dataModelingDataSpace);
	}

	public static Adaptation getDataModelingDataSet(AdaptationHome dataModelingDataSpace)
	{
		return dataModelingDataSpace
			.findAdaptationOrNull(AdaptationName.forName(DATA_MODELING_DATA_SET));
	}

	public static AdaptationHome getEventBrokerDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(EVENT_BROKER_DATA_SPACE));
	}

	public static Adaptation getEventBrokerDataSet(Repository repo)
	{
		AdaptationHome eventBrokerDataSpace = getEventBrokerDataSpace(repo);
		return getEventBrokerDataSet(eventBrokerDataSpace);
	}

	public static Adaptation getEventBrokerDataSet(AdaptationHome eventBrokerDataSpace)
	{
		return eventBrokerDataSpace
			.findAdaptationOrNull(AdaptationName.forName(EVENT_BROKER_DATA_SET));
	}

	public static AdaptationHome getTaskSchedulerDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(TASK_SCHEDULER_DATA_SPACE));
	}

	public static Adaptation getTaskSchedulerDataSet(Repository repo)
	{
		AdaptationHome taskSchedulerDataSpace = getTaskSchedulerDataSpace(repo);
		return getTaskSchedulerDataSet(taskSchedulerDataSpace);
	}

	public static Adaptation getTaskSchedulerDataSet(AdaptationHome taskSchedulerDataSpace)
	{
		return taskSchedulerDataSpace
			.findAdaptationOrNull(AdaptationName.forName(TASK_SCHEDULER_DATA_SET));
	}

	public static AdaptationTable getTasksTable(Adaptation taskSchedulerDataSet)
	{
		return taskSchedulerDataSet.getTable(TASKS_TABLE_PATH);
	}

	public static Path getTasksNamePath()
	{
		return TASKS_NAME_PATH;
	}

	public static Path getTasksModulePath()
	{
		return TASKS_MODULE_PATH;
	}

	public static String getTasksNameValueForRepositoryCleanup()
	{
		return TASKS_NAME_VALUE_FOR_REPOSITORY_CLEANUP;
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataExchangeDataSpace(Repository)} instead
	 */
	@Deprecated
	public static AdaptationHome getAddonAdixDataExchangeDataSpace(Repository repo)
	{
		return AddonAdixAdminUtil.getAddonAdixDataExchangeDataSpace(repo);
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataExchangeDataSet(Repository)} instead
	 */
	@Deprecated
	public static Adaptation getAddonAdixDataExchangeDataSet(Repository repo)
	{
		return AddonAdixAdminUtil.getAddonAdixDataExchangeDataSet(repo);
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataExchangeDataSet(AdaptationHome)} instead
	 */
	@Deprecated
	public static Adaptation getAddonAdixDataExchangeDataSet(
		AdaptationHome addonAdixDataExchangeDataSpace)
	{
		return AddonAdixAdminUtil.getAddonAdixDataExchangeDataSet(addonAdixDataExchangeDataSpace);
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataModelingDataSpace(Repository)} instead
	 */
	@Deprecated
	public static AdaptationHome getAddonAdixDataModelingDataSpace(Repository repo)
	{
		return AddonAdixAdminUtil.getAddonAdixDataModelingDataSpace(repo);
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataModelingDataSet(Repository)} instead
	 */
	@Deprecated
	public static Adaptation getAddonAdixDataModelingDataSet(Repository repo)
	{
		return AddonAdixAdminUtil.getAddonAdixDataModelingDataSet(repo);
	}

	/**
	 * @deprecated Use {@link AddonAdixAdminUtil#getAddonAdixDataModelingDataSet(AdaptationHome)} instead
	 */
	@Deprecated
	public static Adaptation getAddonAdixDataModelingDataSet(
		AdaptationHome addonAdixDataModelingDataSpace)
	{
		return AddonAdixAdminUtil.getAddonAdixDataModelingDataSet(addonAdixDataModelingDataSpace);
	}

	/**
	 * @deprecated Use {@link AddonDqidAdminUtil#getAddonDqidDataSpace(Repository)} instead
	 */
	@Deprecated
	public static AdaptationHome getAddonDqidDataSpace(Repository repo)
	{
		return AddonDqidAdminUtil.getAddonDqidDataSpace(repo);
	}

	/**
	 * @deprecated Use {@link AddonDqidAdminUtil#getAddonDqidDataSet(Repository)} instead
	 */
	@Deprecated
	public static Adaptation getAddonDqidDataSet(Repository repo)
	{
		return AddonDqidAdminUtil.getAddonDqidDataSet(repo);
	}

	/**
	 * @deprecated Use {@link AddonDqidAdminUtil#getAddonDqidDataSet(AdaptationHome)} instead
	 */
	@Deprecated
	public static Adaptation getAddonDqidDataSet(AdaptationHome addonDqidDataSpace)
	{
		return AddonDqidAdminUtil.getAddonDqidDataSet(addonDqidDataSpace);
	}

	public static AdaptationHome getAddonDmdvDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DMDV_DATA_SPACE));
	}

	public static AdaptationHome getAddonDintDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DINT_DATA_SPACE));
	}

	public static Adaptation getAddonDmdvDataSet(Repository repo)
	{
		return getAddonDmdvDataSet(getAddonDmdvDataSpace(repo));
	}

	public static Adaptation getAddonDmdvDataSet(AdaptationHome addonDmdvDataSpace)
	{
		return addonDmdvDataSpace.findAdaptationOrNull(AdaptationName.forName(ADDON_DMDV_DATA_SET));
	}

	public static Adaptation getAddonDintDataSet(Repository repo)
	{
		return getAddonDintDataSet(getAddonDintDataSpace(repo));
	}

	public static Adaptation getAddonDintDataSet(AdaptationHome addonDintDataSpace)
	{
		return addonDintDataSpace.findAdaptationOrNull(AdaptationName.forName(ADDON_DINT_DATA_SET));
	}

	/**
	 * @deprecated This was only used by Dev Artifacts, and isn't needed any longer
	 */
	@Deprecated
	public static boolean isAdminDataSpace(AdaptationHome dataSpace)
	{
		String dataSpaceName = dataSpace.getKey().getName();
		return DIRECTORY_DATA_SPACE.equals(dataSpaceName) || DMA_DATA_SPACE.equals(dataSpaceName)
			|| GLOBAL_PERMISSIONS_DATA_SPACE.equals(dataSpaceName)
			|| TASK_SCHEDULER_DATA_SPACE.equals(dataSpaceName)
			|| PERSPECTIVES_DATA_SPACE.equals(dataSpaceName)
			|| PERSPECTIVE_PREFS_DATA_SPACE.equals(dataSpaceName)
			|| VIEWS_DATA_SPACE.equals(dataSpaceName)
			|| WORKFLOW_MODELS_DATA_SPACE.equals(dataSpaceName)
			|| AddonAdixAdminUtil.ADDON_ADIX_DATA_EXCHANGE_DATA_SPACE.equals(dataSpaceName)
			|| AddonAdixAdminUtil.ADDON_ADIX_DATA_MODELING_DATA_SPACE.equals(dataSpaceName)
			|| AddonDamaAdminUtil.ADDON_DAMA_DATA_SPACE.equals(dataSpaceName)
			|| AddonDqidAdminUtil.ADDON_DQID_DATA_SPACE.equals(dataSpaceName)
			|| ADDON_DMDV_DATA_SPACE.equals(dataSpaceName)
			|| ADDON_DINT_DATA_SPACE.equals(dataSpaceName);
	}

	/**
	 * Looks up a record in the directory's users table based on the supplied predicate.
	 * It will return the first record that matches the predicate.
	 *
	 * @param repo the repository
	 * @param predicate the predicate
	 * @return the user record, or null if not found
	 */
	public static Adaptation lookupUserRecord(Repository repo, String predicate)
	{
		AdaptationTable usersTable = getDirectoryUsersTable(repo);
		RequestResult reqRes = usersTable.createRequestResult(predicate);
		Adaptation userRecord;
		try
		{
			userRecord = reqRes.nextAdaptation();
		}
		finally
		{
			reqRes.close();
		}
		return userRecord;
	}

	/**
	 * Looks up a user id in the directory from the email address
	 *
	 * @param repo the repository
	 * @param email the email address
	 * @return the user id
	 */
	public static String lookupUserIdFromEmail(Repository repo, String email)
	{
		Adaptation userRecord = lookupUserRecord(
			repo,
			"osd:is-equal-case-insensitive(" + getDirectoryUsersEmailPath().format() + ",'" + email
				+ "')");
		return userRecord == null ? null
			: userRecord.getString(AdminUtil.getDirectoryUsersLoginPath());
	}

	private AdminUtil()
	{
	}
}
