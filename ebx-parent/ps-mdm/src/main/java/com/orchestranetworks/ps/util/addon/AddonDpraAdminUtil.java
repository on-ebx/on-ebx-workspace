package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with dpra addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonDpraAdminUtil
{
	public static final String ADDON_DPRA_DATA_SPACE = "ebx-addon-dpra-configuration";
	public static final String ADDON_DPRA_DATA_SET = "ebx-addon-dpra-configuration";

	public static final Path ADDON_DPRA_FUNCTION_TABLE_PATH = Path.parse("/root/Function");

	public static final Path ADDON_DPRA_ASSET_TABLE_PATH = Path.parse("/root/Asset");
	public static final Path ADDON_DPRA_ASSET_DATASPACE_PATH = Path.parse("./dataspace");
	public static final Path ADDON_DPRA_ASSET_DATASET_PATH = Path.parse("./dataset");

	public static final Path ADDON_DPRA_INDICATOR_TABLE_PATH = Path.parse("/root/Indicator");
	public static final Path ADDON_DPRA_INDICATOR_ASSET_PATH = Path.parse("./asset");

	public static final Path ADDON_DPRA_DASHBOARD_TABLE_PATH = Path
		.parse("/root/DashboardConfiguration/Dashboard");
	public static final Path ADDON_DPRA_DASHBOARD_LABEL_GROUP_PATH = Path
		.parse("./labelDescription");

	public static final Path ADDON_DPRA_SECTION_TABLE_PATH = Path
		.parse("/root/DashboardConfiguration/Section");
	public static final Path ADDON_DPRA_SECTION_DASHBOARD_PATH = Path.parse("./dashboard");

	public static final Path ADDON_DPRA_TILE_TABLE_PATH = Path
		.parse("/root/DashboardConfiguration/Tile");
	public static final Path ADDON_DPRA_TILE_SECTION_PATH = Path.parse("./section");

	public static final Path ADDON_DPRA_THEME_TABLE_PATH = Path
		.parse("/root/DashboardConfiguration/Theme");
	public static final Path ADDON_DPRA_THEME_LABEL_GROUP_PATH = Path.parse("./label");

	public static final Path ADDON_DPRA_GLOBAL_PERMISSION_TABLE_PATH = Path
		.parse("/root/Permission/GlobalPermission");
	public static final Path ADDON_DPRA_GLOBAL_PERMISSION_PROFILE_PATH = Path.parse("./profile");

	public static AdaptationHome getAddonDpraDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DPRA_DATA_SPACE));
	}

	public static Adaptation getAddonDpraDataSet(Repository repo)
	{
		return getAddonDpraDataSet(getAddonDpraDataSpace(repo));
	}

	public static Adaptation getAddonDpraDataSet(AdaptationHome addonDpraDataSpace)
	{
		return addonDpraDataSpace.findAdaptationOrNull(AdaptationName.forName(ADDON_DPRA_DATA_SET));
	}

	private AddonDpraAdminUtil()
	{
	}
}
