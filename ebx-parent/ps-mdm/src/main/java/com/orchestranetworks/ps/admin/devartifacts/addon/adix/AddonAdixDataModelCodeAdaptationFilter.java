package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;

/**
 * A filter based on the DataModel Code field, that checks if it starts with a given prefix.
 * If on another table, the paths of the foreign keys to the Data Model table must be specifed.
 * Otherwise, just specify <code>null</code>.
 */
public class AddonAdixDataModelCodeAdaptationFilter implements AdaptationFilter
{
	private String tenantPrefix;
	private Path[] foreignKeyPaths;

	public AddonAdixDataModelCodeAdaptationFilter(String tenantPrefix, Path[] foreignKeyPaths)
	{
		this.tenantPrefix = tenantPrefix;
		this.foreignKeyPaths = foreignKeyPaths;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		// Follow the foreign keys to the data model record
		Adaptation dataModelRecord = DevArtifactsUtil
			.followForeignKeyChain(foreignKeyPaths, adaptation);
		if (dataModelRecord == null)
		{
			return false;
		}
		String value = dataModelRecord
			.getString(AddonAdixAdminUtil.ADDON_ADIX_DATA_MODELING_DATA_MODEL_CODE_PATH);
		if (value == null)
		{
			return false;
		}
		return value.startsWith(tenantPrefix);
	}
}
