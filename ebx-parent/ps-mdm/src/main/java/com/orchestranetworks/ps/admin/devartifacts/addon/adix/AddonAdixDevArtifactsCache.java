package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;

/**
 * A cache used by the Data Exchange Dev Artifacts processing.
 * Many tables are reliant on which records from other related tables are being processed.
 * That would be very inefficient to repeatedly query other tables that have already been processed,
 * so instead as they get processed, they get cached here for use in other filters.
 */
public class AddonAdixDevArtifactsCache
{
	private Set<String> applicationPrimaryKeys;
	private Set<String> applicationByTypePrimaryKeys;
	private Set<String> applicationInterfacePrimaryKeys;
	private Set<String> pathPrimaryKeys;
	private Set<String> objectClassPrimaryKeys;
	private Set<String> propertyPrimaryKeys;
	private Set<String> tablePrimaryKeys;
	private Set<String> tableMappingPrimaryKeys;
	private Set<String> tableMappingConfigurationPrimaryKeys;
	private Set<String> fieldMappingPrimaryKeys;

	public void load(DevArtifactsConfig config, Adaptation adixDataExchangeDataSet)
	{
		applicationPrimaryKeys = new HashSet<>();
		applicationByTypePrimaryKeys = new HashSet<>();
		applicationInterfacePrimaryKeys = new HashSet<>();
		pathPrimaryKeys = new HashSet<>();
		objectClassPrimaryKeys = new HashSet<>();
		propertyPrimaryKeys = new HashSet<>();
		tablePrimaryKeys = new HashSet<>();
		tableMappingPrimaryKeys = new HashSet<>();
		tableMappingConfigurationPrimaryKeys = new HashSet<>();
		fieldMappingPrimaryKeys = new HashSet<>();

		Set<Adaptation> applications = new HashSet<>();
		List<String> modules = config.getModules();
		String tenantPolicy = config.getTenantPolicy();
		AdaptationTable applicationTable = adixDataExchangeDataSet
			.getTable(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_TABLE_PATH);
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(tenantPolicy))
		{
			RequestResult applicationRequestResult = applicationTable.createRequestResult(null);
			try
			{
				AdaptationUtil.getRecords(applications, applicationRequestResult);
			}
			finally
			{
				applicationRequestResult.close();
			}
		}
		else
		{
			AdaptationTable applicationByTypeTable = adixDataExchangeDataSet
				.getTable(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_TABLE_PATH);
			Request applicationByTypeRequest = applicationByTypeTable.createRequest();
			applicationByTypeRequest
				.setSpecificFilter(new AddonAdixApplicationByTypeAdaptationFilter(modules));
			RequestResult applicationByTypeRequestResult = applicationByTypeRequest.execute();
			// Collect the Applications processed by the ApplicationByType table
			try
			{
				for (Adaptation applicationByType; (applicationByType = applicationByTypeRequestResult
					.nextAdaptation()) != null;)
				{
					Adaptation application = AdaptationUtil.followFK(
						applicationByType,
						AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_APPLICATION_PATH);
					if (application != null)
					{
						applications.add(application);
					}
				}
			}
			finally
			{
				applicationByTypeRequestResult.close();
			}
		}

		AdaptationTable applicationInterfaceTable = adixDataExchangeDataSet
			.getTable(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_TABLE_PATH);
		AdaptationTable applicationInterfaceConfigurationTable = adixDataExchangeDataSet
			.getTable(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_TABLE_PATH);
		AdaptationTable tableMappingTable = adixDataExchangeDataSet
			.getTable(AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_TABLE_PATH);

		String preferencePrefix = config.getAddonAdixPreferencePrefix();

		// Loop through the applications, determining whether to include them based on the interface configuration
		for (Adaptation application : applications)
		{
			boolean includeApplication;
			RequestResult applicationByTypeRequestResult = AdaptationUtil.linkedRecordLookup(
				application,
				AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_ASSOCIATION_APPLICATION_BY_TYPE_PATH);
			try
			{
				if (applicationByTypeRequestResult.isEmpty())
				{
					includeApplication = true;
				}
				else
				{
					includeApplication = false;
					for (Adaptation applicationByType; (applicationByType = applicationByTypeRequestResult
						.nextAdaptation()) != null;)
					{
						if (!AddonAdixDevArtifactsUtil
							.ignoreApplicationByTypeBasedOnInterfaceConfiguration(
								applicationByType,
								applicationInterfaceTable,
								applicationInterfaceConfigurationTable,
								preferencePrefix))
						{
							includeApplication = true;
							includeApplicationByType(applicationByType, applicationInterfaceTable);
						}
					}
				}
			}
			finally
			{
				applicationByTypeRequestResult.close();
			}
			if (includeApplication)
			{
				includeApplication(application, tableMappingTable, preferencePrefix);
			}
		}
	}

	private void includeApplication(
		Adaptation application,
		AdaptationTable tableMappingTable,
		String preferencePrefix)
	{
		String applicationPK = application.getOccurrencePrimaryKey().format();
		applicationPrimaryKeys.add(applicationPK);

		RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
			application,
			AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_ASSOCIATION_APPLICATION_OBJECT_CLASS_PATH);
		try
		{
			for (Adaptation applicationObjectClass; (applicationObjectClass = requestResult
				.nextAdaptation()) != null;)
			{
				Adaptation objectClass = AdaptationUtil.followFK(
					applicationObjectClass,
					AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_OBJECT_CLASS_OBJECT_CLASS_PATH);
				if (objectClass != null)
				{
					includeObjectClass(objectClass);
				}
			}
		}
		finally
		{
			requestResult.close();
		}

		requestResult = AdaptationUtil.linkedRecordLookup(
			application,
			AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_ASSOCIATION_TABLE_PATH);
		try
		{
			for (Adaptation tableRecord; (tableRecord = requestResult.nextAdaptation()) != null;)
			{
				includeTable(tableRecord, tableMappingTable, preferencePrefix);
			}
		}
		finally
		{
			requestResult.close();
		}
	}

	private void includeApplicationByType(
		Adaptation applicationByType,
		AdaptationTable applicationInterfaceTable)
	{
		String applicationByTypePK = applicationByType.getOccurrencePrimaryKey().format();
		applicationByTypePrimaryKeys.add(applicationByTypePK);

		// Add its related path record to the cache
		String pathPK = applicationByType
			.getString(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_ROOT_PATH_PATH);
		if (pathPK != null)
		{
			pathPrimaryKeys.add(pathPK);
		}

		RequestResult requestResult = AddonAdixDevArtifactsUtil
			.findApplicationInterfacesForApplicationByType(
				applicationByType,
				applicationInterfaceTable);
		try
		{
			for (Adaptation applicationInterface; (applicationInterface = requestResult
				.nextAdaptation()) != null;)
			{
				applicationInterfacePrimaryKeys
					.add(applicationInterface.getOccurrencePrimaryKey().format());
			}
		}
		finally
		{
			requestResult.close();
		}
	}

	private void includeObjectClass(Adaptation objectClass)
	{
		if (!isBuiltInRecord(objectClass, AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_CODE_PATH))
		{
			String objectClassPK = objectClass.getOccurrencePrimaryKey().format();
			objectClassPrimaryKeys.add(objectClassPK);

			RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
				objectClass,
				AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_ASSOCIATION_OBJECT_CLASS_PROPERTY_PATH);
			try
			{
				for (Adaptation objectClassProperty; (objectClassProperty = requestResult
					.nextAdaptation()) != null;)
				{
					Adaptation property = AdaptationUtil.followFK(
						objectClassProperty,
						AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_PROPERTY_PROPERTY_PATH);
					if (property != null)
					{
						includeProperty(property);
					}
				}
			}
			finally
			{
				requestResult.close();
			}
		}
	}

	private static boolean isBuiltInRecord(Adaptation record, Path builtInFieldPath)
	{
		String builtInFieldValue = record.getString(builtInFieldPath);
		return builtInFieldValue != null
			&& builtInFieldValue.startsWith(DevArtifactsConstants.ADDON_BUILT_IN_RECORD_PREFIX);
	}

	private void includeProperty(Adaptation property)
	{
		if (!isBuiltInRecord(property, AddonAdixAdminUtil.ADDON_ADIX_PROPERTY_CODE_PATH))
		{
			String propertyPK = property.getOccurrencePrimaryKey().format();
			if (!propertyPrimaryKeys.contains(propertyPK))
			{
				propertyPrimaryKeys.add(propertyPK);

				// Need to also check all associated Object Class records, even when we already processed an
				// Object Class, because its property might also be associated with another Object Class
				// which should also therefore be included
				RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
					property,
					AddonAdixAdminUtil.ADDON_ADIX_PROPERTY_ASSOCIATION_OBJECT_CLASS_PROPERTY_PATH);
				try
				{
					for (Adaptation objectClassProperty; (objectClassProperty = requestResult
						.nextAdaptation()) != null;)
					{
						Adaptation objectClass = AdaptationUtil.followFK(
							objectClassProperty,
							AddonAdixAdminUtil.ADDON_ADIX_OBJECT_CLASS_PROPERTY_OBJECT_CLASS_PATH);
						if (objectClass != null && !objectClassPrimaryKeys
							.contains(objectClass.getOccurrencePrimaryKey().format()))
						{
							includeObjectClass(objectClass);
						}
					}
				}
				finally
				{
					requestResult.close();
				}
			}
		}
	}

	private void includeTable(
		Adaptation tableRecord,
		AdaptationTable tableMappingTable,
		String preferencePrefix)
	{
		final String tablePK = tableRecord.getOccurrencePrimaryKey().format();
		tablePrimaryKeys.add(tablePK);

		// Add its related path record to the cache
		String tablePathPK = tableRecord
			.getString(AddonAdixAdminUtil.ADDON_ADIX_TABLE_PATH_FIELD_PATH);
		if (tablePathPK != null)
		{
			pathPrimaryKeys.add(tablePathPK);
		}

		Adaptation objectClass = AdaptationUtil
			.followFK(tableRecord, AddonAdixAdminUtil.ADDON_ADIX_TABLE_OBJECT_CLASS_PATH);
		if (objectClass != null)
		{
			includeObjectClass(objectClass);
		}

		RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
			tableRecord,
			AddonAdixAdminUtil.ADDON_ADIX_TABLE_ASSOCIATION_FIELD_PATH);
		try
		{
			for (Adaptation fieldRecord; (fieldRecord = requestResult.nextAdaptation()) != null;)
			{
				// Add its related path record to the cache
				String fieldPathPK = fieldRecord
					.getString(AddonAdixAdminUtil.ADDON_ADIX_FIELD_PATH_FIELD_PATH);
				if (fieldPathPK != null)
				{
					pathPrimaryKeys.add(fieldPathPK);
				}

				Adaptation property = AdaptationUtil
					.followFK(fieldRecord, AddonAdixAdminUtil.ADDON_ADIX_FIELD_PROPERTY_PATH);
				if (property != null)
				{
					includeProperty(property);
				}
			}
		}
		finally
		{
			requestResult.close();
		}

		Request tableMappingRequest = tableMappingTable.createRequest();
		tableMappingRequest.setSpecificFilter(new AdaptationFilter()
		{
			@Override
			public boolean accept(Adaptation adaptation)
			{
				String targetTablePK = adaptation
					.getString(AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_TABLE_TARGET_PATH);
				if (tablePK.equals(targetTablePK))
				{
					return !AddonAdixDevArtifactsUtil
						.ignoreTableMappingBasedOnInterfaceConfiguration(
							adaptation,
							preferencePrefix);
				}
				return false;
			}
		});
		requestResult = tableMappingRequest.execute();
		try
		{
			for (Adaptation tableMapping; (tableMapping = requestResult.nextAdaptation()) != null;)
			{
				includeTableMapping(tableMapping, preferencePrefix);
			}
		}
		finally
		{
			requestResult.close();
		}
	}

	private void includeTableMapping(Adaptation tableMapping, String preferencePrefix)
	{
		String tableMappingPK = tableMapping.getOccurrencePrimaryKey().format();
		tableMappingPrimaryKeys.add(tableMappingPK);
		RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
			tableMapping,
			AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_ASSOCIATION_TABLE_MAPPING_CONFIGURATION_PATH);
		try
		{
			for (Adaptation tableMappingConfiguration; (tableMappingConfiguration = requestResult
				.nextAdaptation()) != null;)
			{
				Adaptation applicationInterfaceConfiguration = AdaptationUtil.followFK(
					tableMappingConfiguration,
					AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_APPLICATION_INTERFACE_CONFIGURATION_PATH);
				boolean include;
				if (applicationInterfaceConfiguration == null)
				{
					include = true;
				}
				else
				{
					String applicationInterfaceConfigurationName = applicationInterfaceConfiguration
						.getString(
							AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_NAME_PATH);
					if (applicationInterfaceConfigurationName != null
						&& applicationInterfaceConfigurationName.startsWith(preferencePrefix))
					{
						include = true;
					}
					else
					{
						include = false;
					}
				}

				if (include)
				{
					tableMappingConfigurationPrimaryKeys
						.add(tableMappingConfiguration.getOccurrencePrimaryKey().format());
				}
			}
		}
		finally
		{
			requestResult.close();
		}

		requestResult = AdaptationUtil.linkedRecordLookup(
			tableMapping,
			AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_ASSOCIATION_FIELD_MAPPING_PATH);
		try
		{
			for (Adaptation fieldMapping; (fieldMapping = requestResult.nextAdaptation()) != null;)
			{
				fieldMappingPrimaryKeys.add(fieldMapping.getOccurrencePrimaryKey().format());
			}
		}
		finally
		{
			requestResult.close();
		}
	}

	public Set<String> getApplicationPrimaryKeys()
	{
		return applicationPrimaryKeys;
	}

	public Set<String> getApplicationByTypePrimaryKeys()
	{
		return applicationByTypePrimaryKeys;
	}

	public Set<String> getApplicationInterfacePrimaryKeys()
	{
		return applicationInterfacePrimaryKeys;
	}

	public Set<String> getPathPrimaryKeys()
	{
		return pathPrimaryKeys;
	}

	public Set<String> getObjectClassPrimaryKeys()
	{
		return objectClassPrimaryKeys;
	}

	public Set<String> getPropertyPrimaryKeys()
	{
		return propertyPrimaryKeys;
	}

	public Set<String> getTablePrimaryKeys()
	{
		return tablePrimaryKeys;
	}

	public Set<String> getTableMappingPrimaryKeys()
	{
		return tableMappingPrimaryKeys;
	}

	public Set<String> getTableMappingConfigurationPrimaryKeys()
	{
		return tableMappingConfigurationPrimaryKeys;
	}

	public Set<String> getFieldMappingPrimaryKeys()
	{
		return fieldMappingPrimaryKeys;
	}
}
