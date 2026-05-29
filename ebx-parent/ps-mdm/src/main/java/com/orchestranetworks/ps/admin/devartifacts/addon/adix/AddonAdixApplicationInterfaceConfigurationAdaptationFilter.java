package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.adaptationfilter.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * A filter for the Application Interface Configuration table.
 * In addition to the parent class's behavior, it also checks that the name
 * starts with the preference prefix.
 */
public class AddonAdixApplicationInterfaceConfigurationAdaptationFilter
	extends
	PrimaryKeySetContainsAdaptationFilter
{
	private String preferencePrefix;

	public AddonAdixApplicationInterfaceConfigurationAdaptationFilter(
		Set<String> applicationInterfacePrimaryKeys,
		String preferencePrefix)
	{
		super(
			applicationInterfacePrimaryKeys,
			AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_APPLICATION_INTERFACE_PATH,
			null);
		this.preferencePrefix = preferencePrefix;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		// No application interface configuration records are processed if there's no prefix.
		// We could skip the table entirely, but for completeness, we'll export an empty file
		// and this will accomplish that.
		if (preferencePrefix == null)
		{
			return false;
		}
		String name = adaptation
			.getString(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_INTERFACE_CONFIGURATION_NAME_PATH);
		return name != null && name.startsWith(preferencePrefix) && super.accept(adaptation);
	}
}
