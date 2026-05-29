/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public abstract class ProjectRecordMatchesAccessRule extends RecordMatchesAccessRule
	implements ProjectPathCapable
{
	protected ProjectRecordMatchesAccessRule()
	{
		this(null);
	}

	protected ProjectRecordMatchesAccessRule(Path recordFieldPath)
	{
		super(recordFieldPath);
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
