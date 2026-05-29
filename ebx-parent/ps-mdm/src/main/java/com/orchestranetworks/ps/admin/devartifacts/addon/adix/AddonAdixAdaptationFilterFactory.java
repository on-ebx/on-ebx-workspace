package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.adaptationfilter.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;

/**
 * Many of the Data Exchange tables require complex filters for Dev Artifacts processing.
 * Some utilize a common cache. This handles creating the filters using the cache and table path.
 */
public abstract class AddonAdixAdaptationFilterFactory
{
	public static AdaptationFilter createFilter(
		AddonAdixDevArtifactsCache cache,
		String preferencePrefix,
		Path tablePath)
	{
		AdaptationFilter filter;
		if (AddonAdixAdminUtil.ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getFieldMappingPrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_FIELD_MAPPING_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TRANSFORMATION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new AddonAdixAdditionalFieldMappingTransformationAdaptationFilter(
				cache.getFieldMappingPrimaryKeys());
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getApplicationPrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getApplicationByTypePrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_VERSION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getApplicationByTypePrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_VERSION_APPLICATION_BY_TYPE_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getApplicationInterfacePrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new AddonAdixApplicationInterfaceConfigurationAdaptationFilter(
				cache.getApplicationInterfacePrimaryKeys(),
				preferencePrefix);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_OBJECT_CLASS_TABLE_PATH
			.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getApplicationPrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_OBJECT_CLASS_APPLICATION_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_TYPE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_TYPE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_DATA_TYPE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_DATA_TYPE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_DATE_TIME_PATTERN_TABLE_PATH.equals(tablePath))
		{
			filter = null;
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_DEFAULT_OPTION_IMPORT_EXPORT_TABLE_PATH
			.equals(tablePath))
		{
			filter = null;
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_FIELD_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTablePrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_FIELD_TABLE_FK_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_FIELD_HIERARCHICAL_VIEW_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTablePrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_FIELD_HIERARCHICAL_VIEW_TABLE_FK_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTableMappingPrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_TABLE_MAPPING_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new AddonAdixFieldMappingConfigurationAdaptationFilter(
				cache.getFieldMappingPrimaryKeys(),
				cache.getTableMappingConfigurationPrimaryKeys());
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_TRANSFORMATION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getFieldMappingPrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_TRANSFORMATION_FIELD_MAPPING_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_JNDI_DATA_SOURCE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_JNDI_DATA_SOURCE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_MAPPING_TYPE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_MAPPING_TYPE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getObjectClassPrimaryKeys(),
				null,
				AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_PROPERTY_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getObjectClassPrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_PROPERTY_OBJECT_CLASS_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_PATH_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getPathPrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_PROPERTY_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getPropertyPrimaryKeys(),
				null,
				AddonAdixAdminUtil.ADDON_ADIX_PROPERTY_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_SQL_DATA_SOURCE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_SQL_DATA_SOURCE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_STYLE_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(AddonAdixAdminUtil.ADDON_ADIX_STYLE_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_TABLE_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTablePrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_TABLE_HIERARCHICAL_VIEW_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTablePrimaryKeys(),
				AddonAdixAdminUtil.ADDON_ADIX_TABLE_HIERARCHICAL_VIEW_TABLE_FK_PATH,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_TABLE_PATH.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTableMappingPrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_TABLE_PATH
			.equals(tablePath))
		{
			filter = new PrimaryKeySetContainsAdaptationFilter(
				cache.getTableMappingConfigurationPrimaryKeys(),
				null,
				null);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_TRANSFORMATION_FUNCTION_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_TRANSFORMATION_FUNCTION_CODE_PATH);
		}
		else if (AddonAdixAdminUtil.ADDON_ADIX_VALIDATOR_TABLE_PATH.equals(tablePath))
		{
			filter = new NonBuiltInAdaptationFilter(
				AddonAdixAdminUtil.ADDON_ADIX_VALIDATOR_CODE_PATH);
		}
		else
		{
			throw new IllegalArgumentException(
				"tablePath " + tablePath.format() + " is not supported.");
		}
		return filter;
	}
}
