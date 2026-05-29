package com.orchestranetworks.ps.admin.devartifacts.addon.adix;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.addon.*;
import com.orchestranetworks.schema.*;

/**
 * A filter for the Application By Type table
 */
public class AddonAdixApplicationByTypeAdaptationFilter implements AdaptationFilter
{
	private List<String> modules;

	AddonAdixApplicationByTypeAdaptationFilter(List<String> modules)
	{
		this.modules = modules;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		String name = adaptation
			.getString(AddonAdixAdminUtil.ADDON_ADIX_APPLICATION_BY_TYPE_NAME_PATH);
		if (name == null)
		{
			return false;
		}
		int index = name.indexOf("urn:");
		if (index == -1)
		{
			return false;
		}
		String urn = (index == 0) ? name : name.substring(index);
		SchemaLocation schemaLocation = SchemaLocation.parse(urn);
		String module = schemaLocation.getModuleName();
		return modules != null && modules.contains(module);
	}
}
