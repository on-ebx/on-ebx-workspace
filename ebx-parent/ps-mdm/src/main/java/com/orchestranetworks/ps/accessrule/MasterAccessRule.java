package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Constructed with the name of a dataSpace and a dataSet as well as an instance of MasterAccessRulePathConfig
 * which is used to determine the permission path as well as other dynamic settings, such as the name of the table
 * on which to access the various dynamic values (including xpath predicate to determine if this rule is applicable
 * for a given record and/or user), this access rule can be used to create permissions that are dynamic, based on
 * values in a reference table.
 * @see MasterAccessRulePathConfig
 */
public final class MasterAccessRule implements AccessRule
{
	private final String dataSpaceName;
	private final String dataSetName;
	private final MasterAccessRulePathConfig pathConfig;

	public MasterAccessRule(
		String dataSpaceName,
		String dataSetName,
		MasterAccessRulePathConfig pathConfig)
	{
		this.dataSpaceName = dataSpaceName;
		this.dataSetName = dataSetName;
		this.pathConfig = pathConfig;
	}

	@Override
	public final AccessPermission getPermission(
		Adaptation aAdaptation,
		Session aSession,
		SchemaNode aNode)
	{
		final AdaptationHome home = aAdaptation.getHome();
		final Repository repository = home.getRepository();
		AdaptationHome apHome = repository.lookupHome(HomeKey.forBranchName(dataSpaceName));
		if (apHome == null)
		{
			return AccessPermission.getReadWrite();
		}
		final Adaptation apInstance = apHome
			.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		if (apInstance == null)
		{
			return AccessPermission.getReadWrite();
		}
		final AdaptationTable apTable = apInstance.getTable(pathConfig.getAccessPermissionPath());
		String predicate = getPredicateForAccessPermission(aAdaptation, home);

		AccessPermission accessPermission = AccessPermission.getReadWrite();
		RequestResult result = apTable.createRequestResult(predicate);
		try
		{
			Adaptation ap = null;
			AccessPermission dynamicAccessPermission = null;
			SessionPermissions sessionPermissions = null;
			String user = null;
			while ((ap = result.nextAdaptation()) != null)
			{
				if (!this.isAdaptationConcerned(ap, aAdaptation))
				{
					continue;
				}
				if (!this.isUserConcerned(ap, aSession))
				{
					continue;
				}
				user = ap.getString(pathConfig.getAccessPermissionPermissionPath());
				sessionPermissions = repository
					.createSessionPermissionsForUser(UserReference.forUser(user));
				dynamicAccessPermission = sessionPermissions
					.getNodeAccessPermission(aNode, aAdaptation);
				accessPermission = accessPermission.min(dynamicAccessPermission);
			}
		}
		finally
		{
			result.close();
		}

		return accessPermission;
	}

	private String getPredicateForAccessPermission(Adaptation aAdaptation, AdaptationHome aHome)
	{
		StringBuilder predicate = new StringBuilder();
		predicate.append("(")
			.append(pathConfig.getAccessPermissionDataSpacePath().format())
			.append(" = '")
			.append(aHome.getKey().getName())
			.append("' or osd:is-null(")
			.append(pathConfig.getAccessPermissionDataSpacePath().format())
			.append(")) and (osd:is-null(")
			.append(pathConfig.getAccessPermissionDataSetPath().format())
			.append(") or ")
			.append(pathConfig.getAccessPermissionDataSetPath().format());
		if (aAdaptation.isSchemaInstance())
		{
			predicate.append(" = '").append(aAdaptation.getAdaptationName().getStringName()).append(
				"')");
		}
		else
		{
			predicate.append(" = '")
				.append(aAdaptation.getContainer().getAdaptationName().getStringName())
				.append("') and ")
				.append(pathConfig.getAccessPermissionTablePath().format())
				.append(" = '")
				.append(aAdaptation.getContainerTable().getTablePath().format())
				.append("'");
		}
		return predicate.toString();
	}

	private boolean isAdaptationConcerned(Adaptation ap, Adaptation aAdaptation)
	{
		final String condition = ap.getString(pathConfig.getAccessPermissionConditionPath());
		return aAdaptation.matches(condition);
	}

	private boolean isUserConcerned(Adaptation ap, Session aSession)
	{
		final List<String> roles = ap.getList(pathConfig.getAccessPermissionRolePath());
		for (String role : roles)
		{
			if (aSession.isUserInRole(Role.forSpecificRole(role)))
			{
				return true;
			}
		}
		return false;
	}
}
