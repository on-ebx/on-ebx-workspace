/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public abstract class ProjectRecordMatchesTriggerActionValidator
	extends
	RecordMatchesTriggerActionValidator
	implements ProjectPathCapable
{
	protected ProjectRecordMatchesTriggerActionValidator()
	{
		super();
	}

	protected ProjectRecordMatchesTriggerActionValidator(Path recordFieldPath)
	{
		super(recordFieldPath, null);
	}

	protected ProjectRecordMatchesTriggerActionValidator(
		Path recordFieldPath,
		TriggerActionValidator nestedTriggerActionValidator)
	{
		super(recordFieldPath, nestedTriggerActionValidator);
	}

	@Override
	protected List<Adaptation> getRecordsToMatchFromSession(Session session, Repository repo)
		throws OperationException
	{
		Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecordFromSessionInteraction(
			session.getInteraction(true),
			repo,
			getProjectPathConfig());
		if (projectRecord != null)
		{
			List<Adaptation> records = new ArrayList<>();
			records.add(projectRecord);
			return records;
		}
		return null;
	}
}
