/*
 * Copyright Orchestra Networks 2000-2015. All rights reserved.
 */
package com.orchestranetworks.ps.project.valuefunction;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.project.constants.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.schema.*;

/**
 * Retrieves the current work item for a project from a ProjectWorkflowEvent table.
 */
public abstract class ProjectCurrentWorkItemValueFunction
	implements ValueFunction, ProjectPathCapable
{
	/**
	 * Retrieves the Project's current work item's PK. If there is no PK, null is returned.
	 * <p>
	 * The criteria to return a PK are as follows:
	 * <ul>
	 * <li>Must be associated with the Project record</li>
	 * <li>Must have an Event Type of "Work Item"</li>
	 * <li>Must not contain a Completion Date</li>
	 * </ul>
	 *
	 * @see com.orchestranetworks.schema.ValueFunction#getValue(com.onwbp.adaptation.Adaptation)
	 */
	@Override
	public Object getValue(Adaptation adaptation)
	{
		String firstOccurancePK = null;

		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		PrimaryKey projectPK = adaptation.getOccurrencePrimaryKey();

		AdaptationTable projectWorkflowEventTable = adaptation.getContainer().getTable(
			projectPathConfig.getProjectWorkflowEventTablePath());

		String predicate = projectPathConfig.getProjectWorkflowEventProjectFieldPath().format()
			+ "='" + projectPK.format() + "' and "
			+ projectPathConfig.getProjectWorkflowEventEventTypeFieldPath().format() + "='"
			+ CommonProjectConstants.PROJECT_WORKFLOW_EVENT_TYPE_WORKITEM + "' and "
			+ "osd:is-null("
			+ projectPathConfig.getProjectWorkflowEventCompleteDateTimeFieldPath().format() + ")";

		Adaptation projectWorkflowEventRecord = projectWorkflowEventTable.lookupFirstRecordMatchingPredicate(predicate);

		if (projectWorkflowEventRecord != null)
		{
			firstOccurancePK = projectWorkflowEventRecord.getOccurrencePrimaryKey().format();
		}

		return firstOccurancePK;
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		// Do  nothing
	}

}
