package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * If your user service is inherently not read-only (like an import service, or anything else that
 * would modify the data it acts on) then you can use this permission rule to disable the service
 * whenever it is clear that access would be denied.
 * 
 * @deprecated Use {@link WriteAccessRequiredServicePermissionRules} instead
 */
@Deprecated
public class WriteAccessRequiredServicePermissionRule<S extends DataspaceEntitySelection>
	implements ServicePermissionRule<S>
{

	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> arg0)
	{
		S selection = arg0.getEntitySelection();
		Session session = arg0.getSession();
		SessionPermissions permissions = session.getPermissions();
		AdaptationHome branch = selection.getDataspace();
		if (!AccessPermission.getReadWrite().equals(permissions.getHomeAccessPermission(branch)))
			return UserServicePermission.getDisabled();
		if (selection instanceof DatasetEntitySelection)
		{
			Adaptation dataset = ((DatasetEntitySelection) selection).getDataset();
			if (!AccessPermission.getReadWrite()
				.equals(permissions.getAdaptationAccessPermission(dataset)))
				return UserServicePermission.getDisabled();
		}
		if (selection instanceof TableEntitySelection)
		{
			AdaptationTable table = ((TableEntitySelection) selection).getTable();
			if (!AccessPermission.getReadWrite().equals(
				permissions
					.getNodeAccessPermission(table.getTableNode(), table.getContainerAdaptation())))
				return UserServicePermission.getDisabled();
		}
		if (selection instanceof RecordEntitySelection)
		{
			Adaptation record = ((RecordEntitySelection) selection).getRecord();
			if (!AccessPermission.getReadWrite()
				.equals(permissions.getAdaptationAccessPermission(record)))
				return UserServicePermission.getDisabled();

		}
		return UserServicePermission.getEnabled();
	}

}
