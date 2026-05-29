package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.adaptationfilter.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * A filter for the Field Mapping Configuration table
 */
public class AddonAdixFieldMappingConfigurationAdaptationFilter
	extends
	PrimaryKeySetContainsAdaptationFilter
{
	private AdaptationFilter tableMappingConfigurationFilter;

	AddonAdixFieldMappingConfigurationAdaptationFilter(
		Set<String> fieldMappingPrimaryKeys,
		Set<String> tableMappingConfigurationPrimaryKeys)
	{
		super(
			fieldMappingPrimaryKeys,
			AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_FIELD_MAPPING_PATH,
			null);
		tableMappingConfigurationFilter = new PrimaryKeySetContainsAdaptationFilter(
			tableMappingConfigurationPrimaryKeys,
			null,
			null);
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		if (super.accept(adaptation))
		{
			Adaptation tableMappingConfiguration = AdaptationUtil.followFK(
				adaptation,
				AddonAdixAdminUtil.ADDON_ADIX_FIELD_MAPPING_CONFIGURATION_TABLE_MAPPING_CONFIGURATION_PATH);
			return tableMappingConfiguration != null
				&& tableMappingConfigurationFilter.accept(tableMappingConfiguration);
		}
		return false;
	}
}
