package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.addon.dqid.model.paths.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with dqid addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonDqidAdminUtil
{
	public static final String ADDON_DQID_DATA_SPACE = "ebx-addon-dqid-configuration";
	public static final String ADDON_DQID_DATA_SET = "ebx-addon-dqid-configuration";

	public static final Path ADDON_DQID_DATA_ELEMENT_DEFINITION_GROUP_PATH = DQIdConfigurationPaths._DataElementDefinition;
	public static final Path ADDON_DQID_DEC_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_DataElementConcept
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_BUSINESS_CODE_PATH = DQIdConfigurationPaths._DataElementDefinition_DataElementConcept._BusinessCode;
	public static final Path ADDON_DQID_DEC_CLASSIFICATION_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_DecClassification
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_CLASSIFICATION_DEC_PATH = DQIdConfigurationPaths._DataElementDefinition_DecClassification._FKDataElementConcept;
	public static final Path ADDON_DQID_DEC_REFERENCE_CLASSIFICATION_NATURE_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_ClassificationNature
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_REFERENCE_CLASSIFICATION_NATURE_CODE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_ClassificationNature._Code;
	public static final Path ADDON_DQID_DEC_REFERENCE_CLASSIFICATION_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_Classification
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_REFERENCE_CLASSIFICATION_CODE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_Classification._Code;
	public static final Path ADDON_DQID_DEC_REFERENCE_VALIDITY_STATUS_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_ValidityStatus
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_REFERENCE_VALIDITY_STATUS_CODE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_ValidityStatus._Code;
	public static final Path ADDON_DQID_DEC_REFERENCE_BUSINESS_TYPE_TABLE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_BusinessType
		.getPathInSchema();
	public static final Path ADDON_DQID_DEC_REFERENCE_BUSINESS_TYPE_CODE_PATH = DQIdConfigurationPaths._DataElementDefinition_ReferenceData_BusinessType._Code;

	public static final Path ADDON_DQID_INDICATOR_GROUP_PATH = DQIdConfigurationPaths._Indicator;
	public static final Path ADDON_DQID_INDICATOR_TABLE_PATH = DQIdConfigurationPaths._Indicator_Indicator
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_CODE_PATH = DQIdConfigurationPaths._Indicator_Indicator._Code;
	public static final Path ADDON_DQID_INDICATOR_VIEW_TABLE_PATH = DQIdConfigurationPaths._Indicator_IndicatorView
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_VIEW_INDICATOR_PATH = DQIdConfigurationPaths._Indicator_IndicatorView._FKIndicator;
	public static final Path ADDON_DQID_INDICATOR_DEC_TABLE_PATH = DQIdConfigurationPaths._Indicator_IndicatorDec
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_DEC_DEC_PATH = DQIdConfigurationPaths._Indicator_IndicatorDec._FKDataElementConcept;
	public static final Path ADDON_DQID_INDICATOR_CLASSIFICATION_TABLE_PATH = DQIdConfigurationPaths._Indicator_IndicatorClassification
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_CLASSIFICATION_INDICATOR_PATH = DQIdConfigurationPaths._Indicator_IndicatorClassification._FKIndicator;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_CLASSIFICATION_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_Classification
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_CLASSIFICATION_CLASSIFICATION_NATURE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_Classification._FKClassificationNature;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_CLASSIFICATION_NATURE_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_ClassificationNature
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_CLASSIFICATION_NATURE_CODE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_ClassificationNature._Code;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_PERIODICITY_OF_CONTROL_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_PeriodicityOfControl
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_PERIODICITY_OF_CONTROL_CODE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_PeriodicityOfControl._Code;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_COMPUTATION_FREQUENCY_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_ComputationFrequency
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_COMPUTATION_FREQUENCY_CODE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_ComputationFrequency._Code;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_DATA_SET_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_DataSet
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_DATA_SET_DATA_SPACE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_DataSet._DataSpace;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_DATA_SET_DATA_SET_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_DataSet._DataSet;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_EMAIL_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_EmailTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_EMAIL_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_EmailTemplate._Code;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_STORAGE_TABLE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_Storage
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_STORAGE_DATA_SPACE_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_Storage._DataSpace;
	public static final Path ADDON_DQID_INDICATOR_REFERENCE_STORAGE_DATA_SET_PATH = DQIdConfigurationPaths._Indicator_ReferenceData_Storage._DataSet;

	public static final Path ADDON_DQID_WATCHDOG_GROUP_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration;
	public static final Path ADDON_DQID_WATCHDOG_INDICATOR_TABLE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_WatchdogIndicator
		.getPathInSchema();
	public static final Path ADDON_DQID_WATCHDOG_INDICATOR_CODE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_WatchdogIndicator._Code;
	public static final Path ADDON_DQID_WATCHDOG_THRESHOLD_TABLE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_Context
		.getPathInSchema();
	public static final Path ADDON_DQID_WATCHDOG_THRESHOLD_WATCHDOG_INDICATOR_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_Context._FkWatchdogIndicator;
	public static final Path ADDON_DQID_THRESHOLD_STATEMENT_TABLE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_SimpleExpression
		.getPathInSchema();
	public static final Path ADDON_DQID_THRESHOLD_STATEMENT_CODE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_SimpleExpression._Code;
	public static final Path ADDON_DQID_CORRELATED_WATCHDOG_INDICATOR_TABLE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_CorrelatedWatchdogIndicator
		.getPathInSchema();
	public static final Path ADDON_DQID_CORRELATED_WATCHDOG_INDICATOR_CODE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_CorrelatedWatchdogIndicator._Code;
	public static final Path ADDON_DQID_WATCHDOG_REFERENCE_EMAIL_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_ReferenceData_EmailTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_WATCHDOG_REFERENCE_EMAIL_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._WatchdogIndicatorConfiguration_ReferenceData_EmailTemplate._Code;

	public static final Path ADDON_DQID_PERMISSION_GROUP_PATH = DQIdConfigurationPaths._PermissionDomain;
	public static final Path ADDON_DQID_DEFAULT_PERMISSION_TABLE_PATH = DQIdConfigurationPaths._PermissionDomain_IndicatorUserProfileDefaultPermission
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_PERMISSION_TABLE_PATH = DQIdConfigurationPaths._PermissionDomain_IndicatorPermission
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_PERMISSION_USER_PROFILE_PATH = DQIdConfigurationPaths._PermissionDomain_IndicatorPermission._UserProfile;
	public static final Path ADDON_DQID_INDICATOR_PERMISSION_BY_DEC_TABLE_PATH = DQIdConfigurationPaths._PermissionDomain_IndicatorPermissionByDEC
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_PERMISSION_BY_DEC_INDICATOR_PERMISSION_PATH = DQIdConfigurationPaths._PermissionDomain_IndicatorPermissionByDEC._FkIndicatorPermission;
	public static final Path ADDON_DQID_PERMISSION_DATA_SET_TABLE_PATH = DQIdConfigurationPaths._PermissionDomain_DataSet
		.getPathInSchema();
	public static final Path ADDON_DQID_PERMISSION_DATA_SET_DATA_SPACE_PATH = DQIdConfigurationPaths._PermissionDomain_DataSet._DataSpace;
	public static final Path ADDON_DQID_PERMISSION_DATA_SET_DATA_SET_PATH = DQIdConfigurationPaths._PermissionDomain_DataSet._DataSet;

	public static final Path ADDON_DQID_PREFERENCE_GROUP_PATH = DQIdConfigurationPaths._Preference;
	public static final Path ADDON_DQID_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._Preference_IndicatorsPreference
		.getPathInSchema();
	public static final Path ADDON_DQID_PREFERENCE_NAME_PATH = DQIdConfigurationPaths._Preference_IndicatorsPreference._Name;
	public static final Path ADDON_DQID_INDICATORS_BY_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._Preference_PreferenceIndicatorsList
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATORS_BY_PREFERENCE_PREFERENCE_PATH = DQIdConfigurationPaths._Preference_PreferenceIndicatorsList._FKIndicatorsPreference;

	public static final Path ADDON_DQID_SYNTHESIS_GROUP_PATH = DQIdConfigurationPaths._Synthesis;
	public static final Path ADDON_DQID_SYNTHESIS_CLASSIFICATION_TABLE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisClassification
		.getPathInSchema();
	public static final Path ADDON_DQID_SYNTHESIS_CLASSIFICATION_CODE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisClassification._Code;
	public static final Path ADDON_DQID_SYNTHESIS_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisPreference
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_SYNTHESIS_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._Synthesis_IndicatorSynthesisPreference
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATOR_SYNTHESIS_PREFERENCE_USER_PROFILE_PATH = DQIdConfigurationPaths._Synthesis_IndicatorSynthesisPreference._UserProfile;
	public static final Path ADDON_DQID_SYNTHESIS_INDICATOR_LABEL_TABLE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisIndicatorLabel
		.getPathInSchema();
	public static final Path ADDON_DQID_SYNTHESIS_INDICATOR_LABEL_LABEL_PATH = DQIdConfigurationPaths._Synthesis_SynthesisIndicatorLabel._Label_LocalizedDocumentations_Label;
	public static final Path ADDON_DQID_SYNTHESIS_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisDefaultTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_SYNTHESIS_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._Synthesis_SynthesisDefaultTemplate._Code;

	public static final Path ADDON_DQID_DASHBOARD_GROUP_PATH = DQIdConfigurationPaths._Dashboard;
	public static final Path ADDON_DQID_DASHBOARD_TABLE_PATH = DQIdConfigurationPaths._Dashboard_Dashboard
		.getPathInSchema();
	public static final Path ADDON_DQID_DASHBOARD_LABEL_PATH = DQIdConfigurationPaths._Dashboard_Dashboard._Label_LocalizedDocumentations_Label;
	public static final Path ADDON_DQID_SECTION_TABLE_PATH = DQIdConfigurationPaths._Dashboard_DashboardSection
		.getPathInSchema();
	public static final Path ADDON_DQID_SECTION_LABEL_GROUP_PATH = DQIdConfigurationPaths._Dashboard_DashboardSection._Label;
	public static final Path ADDON_DQID_INDICATORS_IN_SECTION_TABLE_PATH = DQIdConfigurationPaths._Dashboard_DashboardSectionIndicatorTile
		.getPathInSchema();
	public static final Path ADDON_DQID_INDICATORS_IN_SECTION_DASHBOARD_SECTION_PATH = DQIdConfigurationPaths._Dashboard_DashboardSectionIndicatorTile._FKDashboardSection;
	public static final Path ADDON_DQID_SECTIONS_IN_DASHBOARD_TABLE_PATH = DQIdConfigurationPaths._Dashboard_DashboardSectionComposition
		.getPathInSchema();
	public static final Path ADDON_DQID_SECTIONS_IN_DASHBOARD_DASHBOARD_SECTION_PATH = DQIdConfigurationPaths._Dashboard_DashboardSectionComposition._FKDashboardSection;
	public static final Path ADDON_DQID_DASHBOARD_PERMISSION_TABLE_PATH = DQIdConfigurationPaths._Dashboard_DashboardUserProfile
		.getPathInSchema();
	public static final Path ADDON_DQID_DASHBOARD_PERMISSIONS_USER_PROFILE_PATH = DQIdConfigurationPaths._Dashboard_DashboardUserProfile._UserProfile;
	public static final Path ADDON_DQID_DASHBOARD_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._Dashboard_DashboardTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_DASHBOARD_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._Dashboard_DashboardTemplate._Code;
	public static final Path ADDON_DQID_DASHBOARD_REFERENCE_EXPORT_CONFIGURATION_TABLE_PATH = DQIdConfigurationPaths._Dashboard_ReferenceData_ExportConfiguration
		.getPathInSchema();
	public static final Path ADDON_DQID_DASHBOARD_REFERENCE_EXPORT_CONFIGURATION_CODE_PATH = DQIdConfigurationPaths._Dashboard_ReferenceData_ExportConfiguration._Code;
	public static final Path ADDON_DQID_DASHBOARD_REFERENCE_EXPORT_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._Dashboard_ReferenceData_ExportTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_DASHBOARD_REFERENCE_EXPORT_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._Dashboard_ReferenceData_ExportTemplate._Code;

	public static final Path ADDON_DQID_USER_GRAPH_PREFERENCE_GROUP_PATH = DQIdConfigurationPaths._UserGraphPreference;
	public static final Path ADDON_DQID_CHART_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._UserGraphPreference_ChartPreferences
		.getPathInSchema();
	public static final Path ADDON_DQID_CHART_PREFERENCE_USER_PROFILE_PATH = DQIdConfigurationPaths._UserGraphPreference_ChartPreferences._UserProfile;
	public static final Path ADDON_DQID_TILE_PREFERENCE_TABLE_PATH = DQIdConfigurationPaths._UserGraphPreference_TilePreference
		.getPathInSchema();
	public static final Path ADDON_DQID_TILE_PREFERENCE_USER_PROFILE_PATH = DQIdConfigurationPaths._UserGraphPreference_TilePreference._UserProfile;
	public static final Path ADDON_DQID_CHART_PREFERENCE_TEMPLATE_TABLE_PATH = DQIdConfigurationPaths._UserGraphPreference_ChartPreferenceTemplate
		.getPathInSchema();
	public static final Path ADDON_DQID_CHART_PREFERENCE_TEMPLATE_CODE_PATH = DQIdConfigurationPaths._UserGraphPreference_ChartPreferenceTemplate._Code;

	public static AdaptationHome getAddonDqidDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DQID_DATA_SPACE));
	}

	public static Adaptation getAddonDqidDataSet(Repository repo)
	{
		return getAddonDqidDataSet(getAddonDqidDataSpace(repo));
	}

	public static Adaptation getAddonDqidDataSet(AdaptationHome addonDqidDataSpace)
	{
		return addonDqidDataSpace.findAdaptationOrNull(AdaptationName.forName(ADDON_DQID_DATA_SET));
	}

	private AddonDqidAdminUtil()
	{
	}
}
