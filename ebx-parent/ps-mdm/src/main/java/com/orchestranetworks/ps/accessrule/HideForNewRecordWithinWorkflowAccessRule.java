package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

//
//TODO: This will also hide columns in a table, which would be an issue if you're showing a table view in a workflow.
//    We currently don't have a way to detect when it's a table view vs. a new record context.
//
/**
 * Access Rule to hide a field when creating a new record within a workflow.
 * 
 * @deprecated Use {@link HideWithinWorkflowAccessRuleForCreate} instead.
 */
@Deprecated
public class HideForNewRecordWithinWorkflowAccessRule implements AccessRule
{
	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}
		// Hide field for New context within a Workflow
		if (adaptation.isSchemaInstance() && session.getInteraction(true) != null)
		{
			return AccessPermission.getHidden();
		}
		else
		{
			return AccessPermission.getReadWrite();
		}
	}
}
