package com.orchestranetworks.ps.servicepermission;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * This encapsulates multiple service permission rules classes that are all related in behavior.
 * They allow a service to show up only when the session user is in the {@link CommonConstants#TECH_ADMIN} role.
 * 
 * Each class relates to a particular <code>EntitySelection</code> class.
 * It can be expanded as needed to incorporate more classes, but for now they are:
 * <ul>
 * <li>{@link OnDataSpace}</li>
 * <li>{@link OnTableView}</li>
 * <li>{@link OnAssociation}</li>
 * </ul>
 * 
 * When referencing these classes, it is recommended that you import the package, and then when referencing the inner class,
 * qualify it with the outer package, like so:
 * <pre>
 * TechAdminOnlyServicePermissionRules.OnTableView rule = new WorkflowOnlyServicePermissionRules.OnTableView();
 * </pre>
 */
public interface TechAdminOnlyServicePermissionRules
{
	public class OnDataSpace<S extends DataspaceEntitySelection> implements ServicePermissionRule<S>
	{
		@Override
		public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
		{
			// The first time you set up the repository, you need to create the Tech Admin role
			if (context.getSession().isUserInRole(CommonConstants.TECH_ADMIN))
			{
				return UserServicePermission.getEnabled();
			}
			return UserServicePermission.getDisabled(
				UserMessage
					.createError("Must be in role " + CommonConstants.ROLE_TECH_ADMIN + "."));
		}
	}

	public class OnTableView extends OnDataSpace<TableViewEntitySelection>
	{
	}

	public class OnAssociation extends OnDataSpace<AssociationEntitySelection>
	{
	}
}
