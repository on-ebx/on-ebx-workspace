package com.orchestranetworks.ps.servicepermission;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * This encapsulates multiple service permission rules classes that are all related in behavior.
 * They allow a service to show up only when inside a workflow.
 * 
 * Each class relates to a particular <code>EntitySelection</code> class.
 * It can be expanded as needed to incorporate more classes, but for now they are:
 * <ul>
 * <li>{@link OnDataSpace}</li>
 * <li>{@link OnTableView}</li>
 * </ul>
 * 
 * By default, it assumes you want to ignore this rule for users in the role {@link CommonConstants#TECH_ADMIN},
 * but you can make it apply to them as well by calling {@link OnDataSpace#setAllowForTechAdmin(boolean)} with
 * <code>false</code>.
 * 
 * When referencing these classes, it is recommended that you import the package, and then when referencing the inner class,
 * qualify it with the outer package, like so:
 * <pre>
 * WorkflowOnlyServicePermissionRules.OnTableView rule = new WorkflowOnlyServicePermissionRules.OnTableView();
 * </pre>
 */
public interface WorkflowOnlyServicePermissionRules
{
	public class OnDataSpace<S extends DataspaceEntitySelection> implements ServicePermissionRule<S>
	{
		private boolean allowForTechAdmin = true;

		@Override
		public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
		{
			Session session = context.getSession();
			//the first time you set up the repository, need to bring in roles
			if ((allowForTechAdmin && session.isUserInRole(CommonConstants.TECH_ADMIN))
				|| session.isInWorkflowInteraction(true))
			{
				return UserServicePermission.getEnabled();
			}
			return UserServicePermission
				.getDisabled(UserMessage.createError("Disabled when not in a workflow session."));
		}

		public boolean isAllowForTechAdmin()
		{
			return allowForTechAdmin;
		}

		public void setAllowForTechAdmin(boolean allowForTechAdmin)
		{
			this.allowForTechAdmin = allowForTechAdmin;
		}
	}

	public class OnTableView extends OnDataSpace<TableViewEntitySelection>
	{
	}
}
