/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.uibeaneditor;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 */
@Deprecated
public class ProfileLabelUIBeanEditor extends UIBeanEditor
{
	private String profileType;

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		String value = (String) context.getValue();
		if (value == null)
		{
			UIWidget widget = context.newBestMatching(Path.SELF);
			widget.setEditorDisabled(true);
			context.addWidget(widget);
		}
		else
		{
			addProfileLabel(context);
		}
	}

	@Override
	public void addForEdit(UIResponseContext context)
	{
		context.addWidget(Path.SELF);
	}

	@Override
	public void addForDisplayInTable(UIResponseContext context)
	{
		String value = (String) context.getValue();
		if (value == null)
		{
			super.addForDisplayInTable(context);
		}
		else
		{
			addProfileLabel(context);
		}
	}

	private void addProfileLabel(UIResponseContext context)
	{
		String value = (String) context.getValue();
		Profile profile = Profile.parse(profileType + value);
		DirectoryHandler dirHandler = context.getSession().getDirectory();
		context.add(dirHandler.displayProfile(profile, context.getLocale()));
	}

	public String getProfileType()
	{
		return this.profileType;
	}

	public void setProfileType(String profileType)
	{
		this.profileType = profileType;
	}
}
