package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;

/**
 * A filter for the Path table
 */
public class AddonAdixPathAdaptationFilter implements AdaptationFilter
{
	private String tenantPolicy;
	private List<String> modules;

	AddonAdixPathAdaptationFilter(String tenantPolicy, List<String> modules)
	{
		this.tenantPolicy = tenantPolicy;
		this.modules = modules;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		String dataModel = adaptation.getString(AddonAdixAdminUtil.ADDON_ADIX_PATH_DATA_MODEL_PATH);
		if (dataModel == null
			|| dataModel.startsWith(DevArtifactsConstants.ADDON_BUILT_IN_RECORD_PREFIX))
		{
			return DevArtifactsConstants.TENANT_POLICY_MULTI_ADMIN.equals(tenantPolicy);
		}
		SchemaLocation schemaLocation = SchemaLocation.parse(dataModel);
		String module = schemaLocation.getModuleName();
		return (module != null && modules != null && modules.contains(module));
	}
}
