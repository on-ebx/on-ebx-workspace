/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.util.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 */
public abstract class SubjectTableRefFilter implements TableRefFilter, SubjectPathCapable
{
	protected abstract String getMasterDataSpaceName();

	protected abstract boolean acceptProjectStatus(ValueContext projectContext);

	protected abstract boolean acceptSubjectStatus(
		ValueContext projectContext,
		Adaptation subjectRecord);

	protected abstract String getSubjectsNameForMessage();

	protected String getFilterMessage()
	{
		return "Only valid " + getSubjectsNameForMessage()
			+ " with the correct status for this project type can be chosen.";
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		context.addFilterErrorMessage(getFilterMessage());
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext context)
		throws InvalidSchemaException
	{
		return getFilterMessage();
	}

	@Override
	public boolean accept(Adaptation adaptation, ValueContext context)
	{
		SubjectPathConfig pathConfig = getSubjectPathConfig();

		// can't be associated with any other in-process project (unless it's the one already assigned)
		String currentProjectType = ProjectUtil.getCurrentProjectType(
			adaptation,
			getMasterDataSpaceName(),
			pathConfig);
		if (currentProjectType != null)
		{
			Adaptation savedContextRecord = AdaptationUtil.getRecordForValueContext(context);
			String subjectFK = (String) context.getValue(Path.SELF);
			if (savedContextRecord == null
				|| !adaptation.getOccurrencePrimaryKey().format().equals(subjectFK))
			{
				return false;
			}
		}

		Path projectSubjectProjectFieldPath = pathConfig.getProjectSubjectProjectFieldPath();
		ValueContext projectContext;
		if (projectSubjectProjectFieldPath == null)
		{
			projectContext = context;
		}
		else
		{
			Adaptation projectRecord = AdaptationUtil.followFK(
				context,
				Path.PARENT.add(projectSubjectProjectFieldPath));
			if (projectRecord == null)
			{
				return false;
			}
			projectContext = projectRecord.createValueContext();
		}

		if (acceptProjectStatus(projectContext))
		{
			return true;
		}

		return acceptSubjectStatus(projectContext, adaptation);
	}
}
