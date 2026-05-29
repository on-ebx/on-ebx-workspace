/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public abstract class SubjectRecordMatchesAccessRule extends ProjectRecordMatchesAccessRule
{
	protected SubjectRecordMatchesAccessRule()
	{
		this(null);
	}

	protected SubjectRecordMatchesAccessRule(Path recordFieldPath)
	{
		super(recordFieldPath);
	}

	@Override
	protected List<Adaptation> getRecordsToMatchFromSession(Session session, Repository repo)
		throws OperationException
	{
		List<Adaptation> projectRecords = super.getRecordsToMatchFromSession(session, repo);
		if (projectRecords == null)
		{
			return null;
		}
		List<Adaptation> subjectRecords = new ArrayList<>();
		if (projectRecords.isEmpty())
		{
			return subjectRecords;
		}
		Adaptation projectRecord = projectRecords.iterator().next();
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		String projectType = projectRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());
		Path projectSubjectsFieldPath = projectPathConfig
			.getProjectProjectSubjectsFieldPath(projectType);
		if (projectSubjectsFieldPath == null)
		{
			Path projectSubjectFieldPath = projectPathConfig
				.getProjectSubjectFieldPath(projectType);
			if (projectSubjectFieldPath != null)
			{
				Adaptation subjectRecord = AdaptationUtil
					.followFK(projectRecord, projectSubjectFieldPath);
				if (subjectRecord != null)
				{
					subjectRecords.add(subjectRecord);
				}
			}
		}
		else
		{
			subjectRecords = AdaptationUtil
				.getLinkedRecordList(projectRecord, projectSubjectsFieldPath);
		}
		return subjectRecords;
	}
}
