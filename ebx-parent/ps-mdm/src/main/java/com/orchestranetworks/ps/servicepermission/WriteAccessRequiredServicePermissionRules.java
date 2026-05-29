package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * These are service permission rules that can be used for services that should be disabled
 * whenever the user's access to the entity is restricted (read only or hidden).
 * For example, if a record is read-only (due to built-in permissions or access rules),
 * you may want to prevent a Delete service, or if a table is read-only, you may want to
 * prevent an import service.
 * 
 * Each class relates to a particular <code>EntitySelection</code> class (with the exception of {@link AbstractOnDataSpace}).
 * The classes available are:
 * <ul>
 * <li>{@link AbstractOnDataSpace} (abstract)</li>
 * <li>{@link OnDataSpace}</li>
 * <li>{@link OnDataSet}</li>
 * <li>{@link OnTable} (abstract)</li>
 * <li>{@link OnTableView}</li>
 * <li>{@link OnRecord}</li>
 * </ul>
 * 
 * When referencing these classes, it is recommended that you import the package, and then when referencing the inner class,
 * qualify it with the outer package, like so:
 * <pre>
 * WriteAccessRequiredServicePermissionRules.OnRecord rule = new WriteAccessRequiredServicePermissionRules.OnRecord();
 * </pre>
 */
public interface WriteAccessRequiredServicePermissionRules
{
	public abstract class AbstractOnDataSpace<S extends DataspaceEntitySelection>
		implements ServicePermissionRule<S>
	{
		/**
		 * Define how to retrieve the {@link AccessPermission} for the given entity selection
		 * by looking up its permissions in the given {@link SessionPermissions}.
		 * 
		 * @param selection the entity selection
		 * @param permissions session permissions for the user
		 * @return the access permission
		 */
		protected abstract AccessPermission processSelection(
			S selection,
			SessionPermissions permissions);

		@Override
		public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
		{
			AccessPermission accessPermission = processSelection(
				context.getEntitySelection(),
				context.getSession().getPermissions());
			if (AccessPermission.getReadWrite().equals(accessPermission))
			{
				return UserServicePermission.getEnabled();
			}
			return getDisabledPermission(accessPermission);
		}

		private static UserServicePermission getDisabledPermission(
			AccessPermission restrictedAccessPermission)
		{
			UserMessage reason = restrictedAccessPermission.getReadOnlyReason();
			// If the access permission specified a reason for the restriction,
			// then pass that message through so that it can possibly be displayed on the screen.
			// Otherwise, just use a generic message saying that the user doesn't have permission.
			if (reason == null)
			{
				return UserServicePermission.getDisabled(
					UserMessage.createError(
						"User has " + restrictedAccessPermission.getLabel()
							+ " permission on record."));
			}
			return UserServicePermission.getDisabled(reason);
		}
	}

	public class OnDataSpace<S extends DataspaceEntitySelection> extends AbstractOnDataSpace<S>
	{
		@Override
		protected AccessPermission processSelection(S selection, SessionPermissions permissions)
		{
			AdaptationHome dataspace = selection.getDataspace();
			return permissions.getHomeAccessPermission(dataspace);
		}
	}

	public class OnDataSet<S extends DatasetEntitySelection> extends AbstractOnDataSpace<S>
	{
		@Override
		protected AccessPermission processSelection(S selection, SessionPermissions permissions)
		{
			Adaptation dataset = selection.getDataset();
			return permissions.getAdaptationAccessPermission(dataset);
		}
	}

	public abstract class OnTable<S extends TableEntitySelection> extends AbstractOnDataSpace<S>
	{
		@Override
		protected AccessPermission processSelection(S selection, SessionPermissions permissions)
		{
			AdaptationTable table = selection.getTable();
			return permissions
				.getNodeAccessPermission(table.getTableNode(), table.getContainerAdaptation());
		}
	}

	public class OnTableView extends OnTable<TableViewEntitySelection>
	{
	}

	public class OnRecord extends AbstractOnDataSpace<RecordEntitySelection>
	{
		@Override
		protected AccessPermission processSelection(
			RecordEntitySelection selection,
			SessionPermissions permissions)
		{
			Adaptation record = selection.getRecord();
			if (record == null)
			{
				return AccessPermission.getReadWrite();
			}
			return permissions.getAdaptationAccessPermission(record);
		}
	}
}
