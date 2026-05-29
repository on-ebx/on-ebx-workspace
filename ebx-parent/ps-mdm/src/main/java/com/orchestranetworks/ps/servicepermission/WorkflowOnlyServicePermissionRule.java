package com.orchestranetworks.ps.servicepermission;

import com.orchestranetworks.ui.selection.*;

/**
 * @deprecated Use {@link WorkflowOnlyServicePermissionRules.OnDataSpace} instead
 */
@Deprecated
public class WorkflowOnlyServicePermissionRule<S extends DataspaceEntitySelection>
	extends
	WorkflowOnlyServicePermissionRules.OnDataSpace<S>
{
	public WorkflowOnlyServicePermissionRule()
	{
		super();
	}

	public WorkflowOnlyServicePermissionRule(boolean allowForTechAdmin)
	{
		super();
		setAllowForTechAdmin(allowForTechAdmin);
	}
}