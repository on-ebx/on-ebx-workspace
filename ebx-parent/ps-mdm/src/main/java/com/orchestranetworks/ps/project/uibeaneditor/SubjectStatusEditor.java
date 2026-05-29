/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.uibeaneditor;

import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.uibeaneditor.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 */
@Deprecated
public abstract class SubjectStatusEditor extends ReadOnlyUIBeanEditor
{
	protected abstract boolean isStatusEditable(String status);

	protected abstract List<String> getAvailableStatuses(String currentStatus);

	@Override
	protected boolean isReadOnly(UIContext context)
	{
		boolean readOnly = super.isReadOnly(context);
		if (readOnly)
		{
			String status = (String) context.getValue();
			if (isStatusEditable(status))
			{
				return false;
			}
		}
		return readOnly;
	}

	@Override
	protected void doAddForEdit(UIResponseContext context)
	{
		if (allowAllValues(context))
		{
			super.doAddForEdit(context);
		}
		else
		{
			UIComboBox comboBox = context.newComboBox(Path.SELF);
			comboBox.setAjaxPrevalidationEnabled(true);
			comboBox.setSpecificNomenclature(createNomenclature((String) context.getValue()));
			comboBox.setAjaxPrevalidationEnabled(true);
			context.addWidget(comboBox);
		}
	}

	protected boolean allowAllValues(UIResponseContext context)
	{
		// Allow all values if we're not in a workflow or there's no current value
		// (shouldn't really happen if you default it and make it required)
		return context.getSession().getInteraction(true) == null || context.getValue() == null;
	}

	protected Nomenclature<String> createNomenclature(String currentStatus)
	{
		// This assumes status is required. If it's not for some reason, we should add an empty
		// nomenclature.
		// We may be able to determine this by checking the schema facet.
		Nomenclature<String> nomenclature = new Nomenclature<>();
		if (isStatusEditable(currentStatus))
		{
			List<String> availStatuses = getAvailableStatuses(currentStatus);
			for (String availStatus : availStatuses)
			{
				nomenclature.addItemValue(availStatus, availStatus);
			}
		}
		return nomenclature;
	}
}
