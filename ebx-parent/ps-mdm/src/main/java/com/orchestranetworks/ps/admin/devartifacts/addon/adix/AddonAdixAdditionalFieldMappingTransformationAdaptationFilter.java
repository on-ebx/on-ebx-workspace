package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.adaptationfilter.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * A filter for the Additional Field Mapping Transformation table
 */
public class AddonAdixAdditionalFieldMappingTransformationAdaptationFilter
	extends
	PrimaryKeySetContainsAdaptationFilter
{
	AddonAdixAdditionalFieldMappingTransformationAdaptationFilter(
		Set<String> fieldMappingPrimaryKeys)
	{
		super(
			fieldMappingPrimaryKeys,
			AddonAdixAdminUtil.ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_FIELD_MAPPING_PATH,
			null);
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		Adaptation additionalFieldMapping = AdaptationUtil.followFK(
			adaptation,
			AddonAdixAdminUtil.ADDON_ADIX_ADDITIONAL_FIELD_MAPPING_TRANSFORMATION_ADDITIONAL_FIELD_MAPPING_PATH);
		return (additionalFieldMapping != null && super.accept(additionalFieldMapping));
	}
}
