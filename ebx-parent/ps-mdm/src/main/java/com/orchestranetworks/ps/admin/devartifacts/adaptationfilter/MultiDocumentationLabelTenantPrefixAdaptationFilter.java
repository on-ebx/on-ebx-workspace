package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.bean.*;
import com.orchestranetworks.schema.*;

/**
 * This should only be used on tables that contain a multi-occurring Documentation group
 * (i.e. the standard group containing a label & optionally description that EBX uses).
 * It accepts the record only if at least one of its labels starts with the given prefix.
 * Because it is multi-occurring, a standard predicate can't accomplish this.
 * 
 * You configure it with the path to the Documentations group that contains the multi-occurring labels.
 */
public class MultiDocumentationLabelTenantPrefixAdaptationFilter implements AdaptationFilter
{
	private Path documentationsPath;
	private String tenantPrefix;

	public MultiDocumentationLabelTenantPrefixAdaptationFilter(
		Path documentationsPath,
		String tenantPrefix)
	{
		this.documentationsPath = documentationsPath;
		this.tenantPrefix = tenantPrefix;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		// Label and LabelForLocale are not part of the public API, yet there's no other way to accomplish this
		Label label = (Label) adaptation.get(documentationsPath);
		if (label == null)
		{
			return false;
		}
		List<LabelForLocale> labelForLocales = label.getLocalizedDocumentations();
		if (labelForLocales == null)
		{
			return false;
		}

		boolean found = false;
		Iterator<LabelForLocale> iter = labelForLocales.iterator();
		while (!found && iter.hasNext())
		{
			LabelForLocale labelForLocale = iter.next();
			String labelStr = labelForLocale.getLabel();
			found = (labelStr != null && labelStr.startsWith(tenantPrefix));
		}
		return found;
	}
}
