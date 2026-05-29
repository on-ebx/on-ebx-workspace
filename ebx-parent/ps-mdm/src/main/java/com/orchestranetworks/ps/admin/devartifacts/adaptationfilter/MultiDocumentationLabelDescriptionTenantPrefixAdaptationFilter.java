package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.bean.*;
import com.orchestranetworks.schema.*;

/**
 * This is exactly the same as {@link MultiDocumentationLabelTenantPrefixAdaptationFilter}, but
 * some tables use a <code>LabelDescription</code> bean instead of a <code>Label/code> bean,
 * so we need to basically code it twice
 */
public class MultiDocumentationLabelDescriptionTenantPrefixAdaptationFilter
	implements AdaptationFilter
{
	private Path documentationsPath;
	private String tenantPrefix;

	public MultiDocumentationLabelDescriptionTenantPrefixAdaptationFilter(
		Path documentationsPath,
		String tenantPrefix)
	{
		this.documentationsPath = documentationsPath;
		this.tenantPrefix = tenantPrefix;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		// LabelDescription and LabelDescriptionForLocale are not part of the public API, yet there's no other way to accomplish this
		LabelDescription labelDescription = (LabelDescription) adaptation.get(documentationsPath);
		if (labelDescription == null)
		{
			return false;
		}
		List<LabelDescriptionForLocale> labelDescriptionForLocales = labelDescription
			.getLocalizedDocumentations();
		if (labelDescriptionForLocales == null)
		{
			return false;
		}

		boolean found = false;
		Iterator<LabelDescriptionForLocale> iter = labelDescriptionForLocales.iterator();
		while (!found && iter.hasNext())
		{
			LabelDescriptionForLocale labelDescriptionForLocale = iter.next();
			String labelDescriptionStr = labelDescriptionForLocale.getLabel();
			found = (labelDescriptionStr != null && labelDescriptionStr.startsWith(tenantPrefix));
		}
		return found;
	}
}
