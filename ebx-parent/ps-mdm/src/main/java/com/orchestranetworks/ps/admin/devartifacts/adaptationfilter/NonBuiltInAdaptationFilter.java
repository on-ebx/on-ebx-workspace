package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.schema.*;

/**
 * A filter that removes built-in records, used by many add-ons
 */
public class NonBuiltInAdaptationFilter implements AdaptationFilter
{
	private Path builtInFieldPath;

	public NonBuiltInAdaptationFilter(Path builtInFieldPath)
	{
		this.builtInFieldPath = builtInFieldPath;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		// Don't include the record if it's a built-in record
		if (builtInFieldPath != null)
		{
			String builtInValue = adaptation.getString(builtInFieldPath);
			if (builtInValue != null
				&& builtInValue.startsWith(DevArtifactsConstants.ADDON_BUILT_IN_RECORD_PREFIX))
			{
				return false;
			}
		}
		return true;
	}
}
