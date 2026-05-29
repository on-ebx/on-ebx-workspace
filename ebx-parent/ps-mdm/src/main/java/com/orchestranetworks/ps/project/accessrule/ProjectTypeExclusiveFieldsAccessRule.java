/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Hide or show attributes based on the Project Type.
 * (i.e. If Menu Item Project, show the menu item related nodes but not Offer or Other Item or Commodity Exchange related nodes.)
 * This must be applied on a node within the Project Table in the schema extensions.
 * 
 * This is applicable outside of the workflow, unlike the ProjectType permissions user.
 */
public abstract class ProjectTypeExclusiveFieldsAccessRule
	implements AccessRule, ProjectPathCapable
{
	protected abstract boolean doesProjectTypeMatch(String projectType);

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}

		// if the adaptation is a schema instance, then either it is the Table View(and we should show all fields), or this is a new row being created.
		// if this is a new row being created from within a Workflow, get data from the Session.
		// if the Session does not have the required data, then  either it is the Table View 
		//   or a row is being created outside the workflow by an Administrator in which case we should not restrict any fields
		String projectType = null;

		if (adaptation.isSchemaInstance())
		{
			projectType = WorkflowUtilities.getSessionInteractionParameter(
				session,
				ProjectWorkflowConstants.SESSION_PARAM_PROJECT_TYPE);
		}
		else
		{
			projectType = adaptation.getString(getProjectPathConfig().getProjectProjectTypeFieldPath());
		}

		return projectType == null || doesProjectTypeMatch(projectType) ? AccessPermission.getReadWrite()
			: AccessPermission.getHidden();
	}
}
