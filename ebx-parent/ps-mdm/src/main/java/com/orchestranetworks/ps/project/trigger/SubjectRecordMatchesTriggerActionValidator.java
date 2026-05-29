/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 */
public abstract class SubjectRecordMatchesTriggerActionValidator
	extends
	RecordMatchesTriggerActionValidator
	implements ProjectPathCapable
{
	protected SubjectRecordMatchesTriggerActionValidator()
	{
		super();
	}

	protected SubjectRecordMatchesTriggerActionValidator(Path subjectRecordFieldPath)
	{
		super(subjectRecordFieldPath, null);
	}

	protected SubjectRecordMatchesTriggerActionValidator(
		Path subjectRecordFieldPath,
		TriggerActionValidator nestedTriggerActionValidator)
	{
		super(subjectRecordFieldPath, nestedTriggerActionValidator);
	}

	@Override
	public UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action)
		throws OperationException
	{
		if (action == TriggerAction.DELETE)
		{
			AdaptationTable table = valueContext.getAdaptationTable();
			PrimaryKey pk = table.computePrimaryKey(valueContext);
			String sessionAttribute = ProjectSubjectTableTrigger.ALLOW_SUBJECT_DELETION_PREFIX
				+ table.getTablePath().format() + "_" + pk.format();
			String sessionAttributeVal = (String) session.getAttribute(sessionAttribute);
			// If a Project Subject record was deleted for this subject in this workflow,
			// then don't check for record matches because we know it matched.
			// Just allow delete (unless a nested validator prevents it).
			if (sessionAttributeVal != null)
			{
				try
				{
					UserMessage msg = null;
					// Always need to call this even when not calling super method for record matches.
					if (nestedTriggerActionValidator != null)
					{
						msg = nestedTriggerActionValidator
							.validateTriggerAction(session, valueContext, valueChanges, action);
					}
					return msg;
				}
				finally
				{
					// Clear the value out
					session.setAttribute(sessionAttribute, null);
				}
			}
		}
		return super.validateTriggerAction(session, valueContext, valueChanges, action);
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
			Adaptation subjectRecord = AdaptationUtil
				.followFK(projectRecord, projectPathConfig.getProjectSubjectFieldPath(projectType));
			if (subjectRecord != null)
			{
				subjectRecords.add(subjectRecord);
			}
		}
		else
		{
			subjectRecords = AdaptationUtil
				.getLinkedRecordList(projectRecord, projectSubjectsFieldPath);
		}
		return subjectRecords;
	}

	@Override
	protected UserMessage preventCreateOnTargetTable(Session session, ValueContext valueContext)
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		String projectType = ProjectWorkflowUtilities
			.getSessionInteractionParameter(session, ProjectWorkflowConstants.PARAM_PROJECT_TYPE);
		Path projectSubjectsFieldPath = projectPathConfig
			.getProjectProjectSubjectsFieldPath(projectType);
		// If there's a single subject per project, then we want to prevent the create
		// so return the message created in the super class
		if (projectSubjectsFieldPath == null)
		{
			return super.preventCreateOnTargetTable(session, valueContext);
		}
		// Otherwise allow it
		return null;
	}
}