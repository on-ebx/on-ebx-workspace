/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * This uses a non-public API method. It is provided for use with the Dev Artifacts service,
 * since there is no other way to access tables that are responsible for permissions.
 * The methods it uses should NOT be invoked unless given the go-ahead by support.
 */
public abstract class AdminPermissionsUtil
{
	public static final Path DATA_SPACE_PERMISSIONS_DATA_SPACE_KEY_PATH = Path.parse("./homeKey");
	public static final Path DATA_SPACE_PERMISSIONS_PROFILE_PATH = Path.parse("./profile");

	private static final String DATA_SPACE_PERMISSIONS_DATA_SPACE = "ebx-dataSpaces";
	private static final String DATA_SPACE_PERMISSIONS_DATA_SET = "ebx-dataSpaces";

	private static final Path DATA_SET_PERMISSIONS_TABLE_PATH = Path
		.parse("/accessRights/accessRightsTable");
	private static final Path DATA_SPACE_PERMISSIONS_TABLE_PATH = Path
		.parse("/repository/permissions");

	public static AdaptationTable getDataSetPermissionsTable(Adaptation dataSet)
	{
		Adaptation permissionsDataSet = AdaptationBridge.getInstanceAccessRights(dataSet);
		return permissionsDataSet.getTable(DATA_SET_PERMISSIONS_TABLE_PATH);
	}

	public static AdaptationTable getDataSpacesPermissionsTable(Repository repo)
	{
		AdaptationHome permissionsDataSpace = repo
			.lookupHome(HomeKey.forBranchName(DATA_SPACE_PERMISSIONS_DATA_SPACE));
		Adaptation permissionsDataSet = permissionsDataSpace
			.findAdaptationOrNull(AdaptationName.forName(DATA_SPACE_PERMISSIONS_DATA_SET));
		return permissionsDataSet.getTable(DATA_SPACE_PERMISSIONS_TABLE_PATH);
	}

	private AdminPermissionsUtil()
	{
	}
}
