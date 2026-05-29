package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * A utility class for the Data Exchange Dev Artifacts processing
 */
public abstract class AddonAdixDevArtifactsUtil
{
	/**
	 * We don't want to process Application By Type records that are only related to
	 * Interface Configurations whose codes don't start with the preference prefix.
	 * If there's at least one that starts with the prefix, or if there are none at all,
	 * then we'll include it.
	 * 
	 * @param applicationByType the Application By Type record
	 * @param applicationInterfaceTable the Application Interface table
	 * @param applicationInterfaceConfigurationTable the Application Interface Configuration table
	 * @param preferencePrefix the preference prefix
	 * @return whether it should be ignored
	 */
	public static boolean ignoreApplicationByTypeBasedOnInterfaceConfiguration(
		Adaptation applicationByType,
		AdaptationTable applicationInterfaceTable,
		AdaptationTable applicationInterfaceConfigurationTable,
		String preferencePrefix)
	{
		boolean foundPreference = false;
		boolean foundIncludedPreference = false;

		RequestResult applicationInterfaceRequestResult = findApplicationInterfacesForApplicationByType(
			applicationByType,
			applicationInterfaceTable);
		try
		{
			if (applicationInterfaceRequestResult.isEmpty())
			{
				return false;
			}

			// Loop through each application interface for this application by type
			// until we loop through all or we find one that indicates it should be included
			for (Adaptation applicationInterface; !foundIncludedPreference
				&& (applicationInterface = applicationInterfaceRequestResult
					.nextAdaptation()) != null;)
			{
				// Find all the application interface configurations for this application interface
				RequestResult applicationInterfaceConfigurationRequestResult = findApplicationInterfaceConfigurationsForApplicationInterface(
					applicationInterface,
					applicationInterfaceConfigurationTable);
				try
				{
					// If there was at least one found
					if (!applicationInterfaceConfigurationRequestResult.isEmpty())
					{
						foundPreference = true;
						// Find whether any of the configurations contain the prefix. If so, it means we
						// found at least one that should indicate the application interface should be included.
						foundIncludedPreference = containsPreferencePrefix(
							applicationInterfaceConfigurationRequestResult,
							preferencePrefix);
					}
				}
				finally
				{
					applicationInterfaceConfigurationRequestResult.close();
				}
			}
		}
		finally
		{
			applicationInterfaceRequestResult.close();
		}
		// application by type should be ignored if there is a preference for it but not an included preference
		return foundPreference && !foundIncludedPreference;
	}

	public static RequestResult findApplicationInterfacesForApplicationByType(
		Adaptation applicationByType,
		AdaptationTable applicationInterfaceTable)
	{
		// Include application interfaces that have this application by type as the target.
		// Don't include those that have it as the source, because those could have targets in
		// other tenants, which would be owned by those tenants.
		String predicate = new StringBuilder(
			AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_TARGET_APPLICATION_BY_TYPE_PATH
				.format()).append("='")
					.append(applicationByType.getOccurrencePrimaryKey().format())
					.append("'")
					.toString();
		return applicationInterfaceTable.createRequestResult(predicate);
	}

	private static RequestResult findApplicationInterfaceConfigurationsForApplicationInterface(
		Adaptation applicationInterface,
		AdaptationTable applicationInterfaceConfigurationTable)
	{
		StringBuilder bldr = new StringBuilder(
			AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_APPLICATION_INTERFACE_PATH
				.format()).append("='")
					.append(applicationInterface.getOccurrencePrimaryKey().format())
					.append("'");
		return applicationInterfaceConfigurationTable.createRequestResult(bldr.toString());
	}

	// This loops through the given request result of application interface configurations
	// and checks if one of them has a name that starts with the given prefix.
	// It assumes the caller will close the request result.
	// If preferencePrefix is null, will simply return false.
	private static boolean containsPreferencePrefix(
		RequestResult applicationInterfaceConfigurationRequestResult,
		String preferencePrefix)
	{
		if (preferencePrefix == null)
		{
			return false;
		}
		boolean found = false;
		for (Adaptation applicationInterfaceConfiguration; !found
			&& (applicationInterfaceConfiguration = applicationInterfaceConfigurationRequestResult
				.nextAdaptation()) != null;)
		{
			String name = applicationInterfaceConfiguration.getString(
				AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_NAME_PATH);
			// We can stop looping if there's a name and it starts with the prefix
			found = (name != null && name.startsWith(preferencePrefix));
		}
		return found;
	}

	public static boolean ignoreTableMappingBasedOnInterfaceConfiguration(
		Adaptation tableMapping,
		String preferencePrefix)
	{
		boolean found = false;
		RequestResult requestResult = AdaptationUtil.linkedRecordLookup(
			tableMapping,
			AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_ASSOCIATION_TABLE_MAPPING_CONFIGURATION_PATH);
		try
		{
			// Don't ignore if there are no table mapping configurations
			if (requestResult.isEmpty())
			{
				return false;
			}

			// Ignore if there are are table mapping configurations but no prefix is specified
			if (preferencePrefix == null)
			{
				return true;
			}

			// Otherwise don't ignore if there is at least one table mapping configuration that points to
			// an application interface configuration that starts with the prefix
			for (Adaptation tableMappingConfiguration; !found
				&& (tableMappingConfiguration = requestResult.nextAdaptation()) != null;)
			{
				Adaptation applicationInterfaceConfiguration = AdaptationUtil.followFK(
					tableMappingConfiguration,
					AddonAdixAdminUtil.ADDON_ADIX_TABLE_MAPPING_CONFIGURATION_APPLICATION_INTERFACE_CONFIGURATION_PATH);
				if (applicationInterfaceConfiguration != null)
				{
					String name = applicationInterfaceConfiguration.getString(
						AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_NAME_PATH);
					found = name != null && name.startsWith(preferencePrefix);
				}
			}
		}
		finally
		{
			requestResult.close();
		}
		return !found;
	}
}
