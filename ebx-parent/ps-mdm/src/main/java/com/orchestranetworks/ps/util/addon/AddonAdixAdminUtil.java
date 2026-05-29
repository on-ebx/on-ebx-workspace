package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.addon.dex.*;
import com.orchestranetworks.addon.dml.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with adix addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonAdixAdminUtil
{
	public static final String ADDON_ADIX_DATA_EXCHANGE_DATA_SPACE = "ebx-addon-adix-dataexchange";
	public static final String ADDON_ADIX_DATA_EXCHANGE_DATA_SET = "ebx-addon-adix-dataexchange";
	public static final String ADDON_ADIX_DATA_MODELING_DATA_SPACE = "ebx-addon-adix-datamodeler";
	public static final String ADDON_ADIX_DATA_MODELING_DATA_SET = "ebx-addon-adix-datamodeler";

	public static final Path ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_AdditionalFieldMapping
		.getPathInSchema();
	public static final Path ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_FIELD_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_AdditionalFieldMapping._FKFieldMapping;

	public static final Path ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TRANSFORMATION_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_AdditionalFieldMappingTransformation
		.getPathInSchema();
	public static final Path ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TRANSFORMATION_ADDITIONAL_FIELD_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_AdditionalFieldMappingTransformation._FKAdditionalFieldMapping;

	public static final Path ADDON_ADIX_APPLICATION_TABLE_PATH = DataExchangePaths._DataExchange_Application_Application
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_ASSOCIATION_APPLICATION_BY_TYPE_PATH = DataExchangePaths._DataExchange_Application_Application._AssociationApplicationByType;
	public static final Path ADDON_ADIX_APPLICATION_ASSOCIATION_APPLICATION_OBJECT_CLASS_PATH = DataExchangePaths._DataExchange_Application_Application._AssociationApplicationObjectClass;
	public static final Path ADDON_ADIX_APPLICATION_ASSOCIATION_TABLE_PATH = DataExchangePaths._DataExchange_Application_Application._AssociationTable;

	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_TABLE_PATH = DataExchangePaths._DataExchange_Application_ApplicationByType
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_APPLICATION_PATH = DataExchangePaths._DataExchange_Application_ApplicationByType._FKApplication;
	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_NAME_PATH = DataExchangePaths._DataExchange_Application_ApplicationByType._Name;
	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_ROOT_PATH_PATH = DataExchangePaths._DataExchange_Application_ApplicationByType._FKRootPath;

	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_VERSION_TABLE_PATH = DataExchangePaths._DataExchange_Application_ApplicationByTypeVersion
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_BY_TYPE_VERSION_APPLICATION_BY_TYPE_PATH = DataExchangePaths._DataExchange_Application_ApplicationByTypeVersion._FKApplicationByType;

	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_TABLE_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterface
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_SOURCE_APPLICATION_BY_TYPE_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterface._FKApplicationByTypeFrom;
	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_TARGET_APPLICATION_BY_TYPE_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterface._FKApplicationByTypeTo;

	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_TABLE_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterfaceConfiguration
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_APPLICATION_INTERFACE_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterfaceConfiguration._FKApplicationInterface;
	public static final Path ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_NAME_PATH = DataExchangePaths._DataExchange_Application_ApplicationInterfaceConfiguration._Name;

	public static final Path ADDON_ADIX_APPLICATION_OBJECT_CLASS_TABLE_PATH = DataExchangePaths._DataExchange_Application_ApplicationObjectClass
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_OBJECT_CLASS_APPLICATION_PATH = DataExchangePaths._DataExchange_Application_ApplicationObjectClass._FKApplication;
	public static final Path ADDON_ADIX_APPLICATION_OBJECT_CLASS_OBJECT_CLASS_PATH = DataExchangePaths._DataExchange_Application_ApplicationObjectClass._FKObjectClass;

	public static final Path ADDON_ADIX_APPLICATION_TYPE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_ApplicationType
		.getPathInSchema();
	public static final Path ADDON_ADIX_APPLICATION_TYPE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_ApplicationType._Code;

	public static final Path ADDON_ADIX_DATA_TYPE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_DataType
		.getPathInSchema();
	public static final Path ADDON_ADIX_DATA_TYPE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_DataType._Code;

	public static final Path ADDON_ADIX_DATE_TIME_PATTERN_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_DateTimePattern
		.getPathInSchema();

	public static final Path ADDON_ADIX_FIELD_TABLE_PATH = DataExchangePaths._DataExchange_DataModel_Field
		.getPathInSchema();
	public static final Path ADDON_ADIX_FIELD_TABLE_FK_PATH = DataExchangePaths._DataExchange_DataModel_Field._FKTable;
	public static final Path ADDON_ADIX_FIELD_PATH_FIELD_PATH = DataExchangePaths._DataExchange_DataModel_Field._FKPath;
	public static final Path ADDON_ADIX_FIELD_PROPERTY_PATH = DataExchangePaths._DataExchange_DataModel_Field._FKProperty;

	public static final Path ADDON_ADIX_FIELD_HIERARCHICAL_VIEW_TABLE_PATH = DataExchangePaths._DataExchange_TechnicalTables_FieldHierarchicalView
		.getPathInSchema();
	public static final Path ADDON_ADIX_FIELD_HIERARCHICAL_VIEW_TABLE_FK_PATH = DataExchangePaths._DataExchange_TechnicalTables_FieldHierarchicalView._FKTable;

	public static final Path ADDON_ADIX_FIELD_MAPPING_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMapping
		.getPathInSchema();
	public static final Path ADDON_ADIX_FIELD_MAPPING_ASSOCIATION_FIELD_MAPPING_CONFIGURATION_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMapping._AssociationFieldMappingPreference;
	public static final Path ADDON_ADIX_FIELD_MAPPING_TABLE_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMapping._FkTableMapping;

	public static final Path ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMappingConfiguration
		.getPathInSchema();
	public static final Path ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_TABLE_MAPPING_CONFIGURATION_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMappingConfiguration._FKTableMappingConfiguration;
	public static final Path ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_FIELD_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMappingConfiguration._FKFieldMapping;

	public static final Path ADDON_ADIX_FIELD_MAPPING_TRANSFORMATION_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMappingTransformation
		.getPathInSchema();
	public static final Path ADDON_ADIX_FIELD_MAPPING_TRANSFORMATION_FIELD_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_FieldMappingTransformation._FKFieldMapping;

	public static final Path ADDON_ADIX_DEFAULT_OPTION_IMPORT_EXPORT_TABLE_PATH = DataExchangePaths._DataExchange_AdditionalConfiguration_DefaultOptionValues_ImportExportServices
		.getPathInSchema();
	public static final Path ADDON_ADIX_DEFAULT_OPTION_IMPORT_EXPORT_UUID_PATH = DataExchangePaths._DataExchange_AdditionalConfiguration_DefaultOptionValues_ImportExportServices._Uuid;

	// TODO: Doesn't exist in version 5.9.5 & earlier. Should be defined as so, when we stop supporting the earlier versions.
	//	public static final Path ADDON_ADIX_GLOBAL_PERMISSION_TABLE_PATH = DataExchangePaths._DataExchange_AdditionalConfiguration_GlobalPermission
	//		.getPathInSchema();
	//	public static final Path ADDON_ADIX_GLOBAL_PERMISSION_PROFILE_PATH = DataExchangePaths._DataExchange_AdditionalConfiguration_GlobalPermission._UserProfile;
	public static final Path ADDON_ADIX_GLOBAL_PERMISSION_TABLE_PATH = Path
		.parse("/DataExchange/AdditionalConfiguration/GlobalPermission");
	public static final Path ADDON_ADIX_GLOBAL_PERMISSION_PROFILE_PATH = Path
		.parse("./userProfile");

	public static final Path ADDON_ADIX_IMPORT_PREFERENCE_GROUP_PATH = DataExchangePaths._DataExchange_AdditionalConfiguration_ImportPreference;

	public static final Path ADDON_ADIX_JNDI_DATA_SOURCE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_JNDIDataSource
		.getPathInSchema();
	public static final Path ADDON_ADIX_JNDI_DATA_SOURCE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_JNDIDataSource._Code;

	public static final Path ADDON_ADIX_MAPPING_TYPE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_MappingType
		.getPathInSchema();
	public static final Path ADDON_ADIX_MAPPING_TYPE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_MappingType._Code;

	public static final Path ADDON_ADIX_OBJECT_CLASS_TABLE_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClass
		.getPathInSchema();
	public static final Path ADDON_ADIX_OBJECT_CLASS_ASSOCIATION_OBJECT_CLASS_PROPERTY_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClass._AssociationObjectClassProperty;
	public static final Path ADDON_ADIX_OBJECT_CLASS_CODE_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClass._Code;

	public static final Path ADDON_ADIX_OBJECT_CLASS_PROPERTY_TABLE_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClassProperty
		.getPathInSchema();
	public static final Path ADDON_ADIX_OBJECT_CLASS_PROPERTY_OBJECT_CLASS_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClassProperty._FKObjectClass;
	public static final Path ADDON_ADIX_OBJECT_CLASS_PROPERTY_PROPERTY_PATH = DataExchangePaths._DataExchange_SemanticModel_ObjectClassProperty._FKProperty;

	public static final Path ADDON_ADIX_PATH_TABLE_PATH = DataExchangePaths._DataExchange_Path_Path
		.getPathInSchema();
	public static final Path ADDON_ADIX_PATH_APPLICATION_BY_TYPE_PATH = DataExchangePaths._DataExchange_Path_Path._FKApplicationType;
	public static final Path ADDON_ADIX_PATH_DATA_MODEL_PATH = DataExchangePaths._DataExchange_Path_Path._DataModel;

	public static final Path ADDON_ADIX_PROPERTY_TABLE_PATH = DataExchangePaths._DataExchange_SemanticModel_Property
		.getPathInSchema();
	public static final Path ADDON_ADIX_PROPERTY_ASSOCIATION_OBJECT_CLASS_PROPERTY_PATH = DataExchangePaths._DataExchange_SemanticModel_Property._AssociationObjectClassProperty;
	public static final Path ADDON_ADIX_PROPERTY_CODE_PATH = DataExchangePaths._DataExchange_SemanticModel_Property._Code;

	public static final Path ADDON_ADIX_SQL_DATA_SOURCE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_SQLDataSource
		.getPathInSchema();
	public static final Path ADDON_ADIX_SQL_DATA_SOURCE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_SQLDataSource._Code;

	public static final Path ADDON_ADIX_STYLE_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_Style
		.getPathInSchema();
	public static final Path ADDON_ADIX_STYLE_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_Style._Code;

	public static final Path ADDON_ADIX_TABLE_TABLE_PATH = DataExchangePaths._DataExchange_DataModel_Table
		.getPathInSchema();
	public static final Path ADDON_ADIX_TABLE_ASSOCIATION_FIELD_PATH = DataExchangePaths._DataExchange_DataModel_Table._AssociationField;
	public static final Path ADDON_ADIX_TABLE_OBJECT_CLASS_PATH = DataExchangePaths._DataExchange_DataModel_Table._FKObjectClass;
	public static final Path ADDON_ADIX_TABLE_PATH_FIELD_PATH = DataExchangePaths._DataExchange_DataModel_Table._FKPath;

	public static final Path ADDON_ADIX_TABLE_HIERARCHICAL_VIEW_TABLE_PATH = DataExchangePaths._DataExchange_TechnicalTables_TableHierarchicalView
		.getPathInSchema();
	public static final Path ADDON_ADIX_TABLE_HIERARCHICAL_VIEW_TABLE_FK_PATH = DataExchangePaths._DataExchange_TechnicalTables_TableHierarchicalView._FKTable;

	public static final Path ADDON_ADIX_TABLE_MAPPING_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_TableMapping
		.getPathInSchema();
	public static final Path ADDON_ADIX_TABLE_MAPPING_ASSOCIATION_TABLE_MAPPING_CONFIGURATION_PATH = DataExchangePaths._DataExchange_DataMapping_TableMapping._AssociationTableMappingPreference;
	public static final Path ADDON_ADIX_TABLE_MAPPING_ASSOCIATION_FIELD_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_TableMapping._AssociationFieldMapping;
	public static final Path ADDON_ADIX_TABLE_MAPPING_TABLE_SOURCE_PATH = DataExchangePaths._DataExchange_DataMapping_TableMapping._FKTableSource;
	public static final Path ADDON_ADIX_TABLE_MAPPING_TABLE_TARGET_PATH = DataExchangePaths._DataExchange_DataMapping_TableMapping._FKTableTarget;

	public static final Path ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_TABLE_PATH = DataExchangePaths._DataExchange_DataMapping_TableMappingConfiguration
		.getPathInSchema();
	public static final Path ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_APPLICATION_INTERFACE_CONFIGURATION_PATH = DataExchangePaths._DataExchange_DataMapping_TableMappingConfiguration._FKApplicationInterfaceConfiguration;
	public static final Path ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_TABLE_MAPPING_PATH = DataExchangePaths._DataExchange_DataMapping_TableMappingConfiguration._FKTableMapping;

	public static final Path ADDON_ADIX_TRANSFORMATION_FUNCTION_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_TransformationFunction
		.getPathInSchema();
	public static final Path ADDON_ADIX_TRANSFORMATION_FUNCTION_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_TransformationFunction._Code;

	public static final Path ADDON_ADIX_VALIDATOR_TABLE_PATH = DataExchangePaths._DataExchange_ReferenceData_Validator
		.getPathInSchema();
	public static final Path ADDON_ADIX_VALIDATOR_CODE_PATH = DataExchangePaths._DataExchange_ReferenceData_Validator._Code;

	public static final Path ADDON_ADIX_DATA_MODELING_DATA_MODEL_TABLE_PATH = DataModelPaths._DataModel
		.getPathInSchema();
	public static final Path ADDON_ADIX_DATA_MODELING_DATA_MODEL_CODE_PATH = DataModelPaths._DataModel._Code;

	public static final Path ADDON_ADIX_DATA_MODELING_TABLE_TABLE_PATH = DataModelPaths._Table
		.getPathInSchema();
	public static final Path ADDON_ADIX_DATA_MODELING_TABLE_DATA_MODEL_PATH = DataModelPaths._Table._FKDataModel;

	public static final Path ADDON_ADIX_DATA_MODELING_FIELD_TABLE_PATH = DataModelPaths._Field
		.getPathInSchema();
	public static final Path ADDON_ADIX_DATA_MODELING_FIELD_TABLE_FIELD_PATH = DataModelPaths._Field._FKTable;

	public static AdaptationHome getAddonAdixDataExchangeDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_ADIX_DATA_EXCHANGE_DATA_SPACE));
	}

	public static Adaptation getAddonAdixDataExchangeDataSet(Repository repo)
	{
		return getAddonAdixDataExchangeDataSet(getAddonAdixDataExchangeDataSpace(repo));
	}

	public static Adaptation getAddonAdixDataExchangeDataSet(
		AdaptationHome addonAdixDataExchangeDataSpace)
	{
		return addonAdixDataExchangeDataSpace
			.findAdaptationOrNull(AdaptationName.forName(ADDON_ADIX_DATA_EXCHANGE_DATA_SET));
	}

	public static AdaptationHome getAddonAdixDataModelingDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_ADIX_DATA_MODELING_DATA_SPACE));
	}

	public static Adaptation getAddonAdixDataModelingDataSet(Repository repo)
	{
		return getAddonAdixDataModelingDataSet(getAddonAdixDataModelingDataSpace(repo));
	}

	public static Adaptation getAddonAdixDataModelingDataSet(
		AdaptationHome addonAdixDataModelingDataSpace)
	{
		return addonAdixDataModelingDataSpace
			.findAdaptationOrNull(AdaptationName.forName(ADDON_ADIX_DATA_MODELING_DATA_SET));
	}

	private AddonAdixAdminUtil()
	{
	}
}
