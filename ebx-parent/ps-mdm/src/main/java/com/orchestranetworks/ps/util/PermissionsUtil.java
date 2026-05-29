/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * A utility class to do some permissions-related things
 */
public class PermissionsUtil
{
	public static boolean isUserPermittedToCreateRecord(Session session, AdaptationTable table)
	{
		Adaptation dataSet = table.getContainerAdaptation();
		SessionPermissions permissions = session.getPermissions();
		ActionPermission createPermission = permissions.getTableActionPermissionToCreateRootOccurrence(table);
		// If they have permissions in the "create record" section of permissions
		if (ActionPermission.getEnabled().equals(createPermission))
		{
			SchemaNode tableNode = table.getTableNode();
			AccessPermission accessPermission = permissions.getNodeAccessPermission(
				tableNode,
				dataSet);
			// If the table node has write permissions (which can occur if any fields within
			// the table have write permission)
			if (AccessPermission.getReadWrite().equals(accessPermission))
			{
				// Get the primary key fields and check each one to see if it's writable.
				// Stop if you get to one that's not.
				Path[] pkPaths = table.getPrimaryKeySpec();
				SchemaNode tableOccNode = tableNode.getTableOccurrenceRootNode();
				for (int i = 0; i < pkPaths.length
					&& AccessPermission.getReadWrite().equals(accessPermission); i++)
				{
					accessPermission = permissions.getNodeAccessPermission(
						tableOccNode.getNode(Path.SELF.add(pkPaths[i])),
						dataSet);
				}
				return AccessPermission.getReadWrite().equals(accessPermission);
			}
			return false;

		}
		return false;
	}

	private PermissionsUtil()
	{
	}
}
