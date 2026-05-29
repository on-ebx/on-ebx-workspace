/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 */
public abstract class ProjectIsResumableValueFunction implements ValueFunction, ProjectPathCapable
{
	// By default can resume any "new subject" projects
	protected boolean isProjectTypeResumable(String projectType)
	{
		return getProjectPathConfig().isNewSubjectProjectType(projectType);
	}

	// By default can resume any "in process" projects
	protected boolean isProjectStatusResumable(String projectStatus)
	{
		return getProjectPathConfig().isInProcessProjectStatus(projectStatus);
	}

	// By default can resume if each subject doesn't have a currentProjectType set
	protected boolean isSubjectResumable(Adaptation subjectRecord, String projectType)
	{
		SubjectPathConfig subjectPathConfig = getProjectPathConfig()
			.getSubjectPathConfig(projectType);
		Path currentProjectTypePath = subjectPathConfig.getSubjectCurrentProjectTypeFieldPath();
		return currentProjectTypePath == null ? true
			: (subjectRecord.getString(currentProjectTypePath) == null);
	}

	@Override
	public Object getValue(Adaptation adaptation)
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		String projectType = adaptation
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		String projectStatus = adaptation.getString(projectPathConfig.getProjectStatusFieldPath());
		if (!(isProjectTypeResumable(projectType) && isProjectStatusResumable(projectStatus)))
		{
			return Boolean.FALSE;
		}

		List<Adaptation> subjectRecords;

		Path projectSubjectsFieldPath = projectPathConfig
			.getProjectProjectSubjectsFieldPath(projectType);
		if (projectSubjectsFieldPath == null)
		{
			subjectRecords = new ArrayList<>();
			Path projectSubjectFieldPath = projectPathConfig
				.getProjectSubjectFieldPath(projectType);
			if (projectSubjectFieldPath != null)
			{
				Adaptation subjectRecord = AdaptationUtil
					.followFK(adaptation, projectSubjectFieldPath);
				if (subjectRecord != null)
				{
					subjectRecords.add(subjectRecord);
				}
			}
		}
		else
		{
			subjectRecords = AdaptationUtil
				.getLinkedRecordList(adaptation, projectSubjectsFieldPath);
		}
		if (subjectRecords.isEmpty())
		{
			return Boolean.FALSE;
		}
		boolean resumable = true;
		for (int i = 0; resumable && i < subjectRecords.size(); i++)
		{
			resumable = isSubjectResumable(subjectRecords.get(i), projectType);
		}
		return Boolean.valueOf(resumable);
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// do nothing
	}
}
