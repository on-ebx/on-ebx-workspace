package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with dint addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonDintAdminUtil
{
	public static final String ADDON_DINT_DATA_SPACE = "ebx-addon-dint";
	public static final String ADDON_DINT_CONFIGURATION_DATA_SET = "ebx-addon-dint-configuration";

	public static final Path ADDON_DINT_ASSET_TABLE_PATH = Path
		.parse("/DataIntegration/VisualMapping/Asset");
	public static final Path ADDON_DINT_ASSET_PATH_FIELD_PATH = Path.parse("./Path");

	public static final Path ADDON_DINT_DATA_TYPE_TABLE_PATH = Path
		.parse("/DataIntegration/ReferenceData/DataType");

	public static final Path ADDON_DINT_DATABASE_TABLE_PATH = Path
		.parse("/DataIntegration/ReferenceData/DatabaseDataSource");
	public static final Path ADDON_DINT_DATABASE_CODE_PATH = Path.parse("./code");
	public static final Path ADDON_DINT_DATABASE_URL_PATH = Path.parse("./url");

	public static final Path ADDON_DINT_DATE_TIME_PATTERN_TABLE_PATH = Path
		.parse("/DataIntegration/ReferenceData/DateTimePattern");

	public static final Path ADDON_DINT_LINK_TABLE_PATH = Path
		.parse("/DataIntegration/VisualMapping/Link");
	public static final Path ADDON_DINT_LINK_END_ASSET_PATH = Path.parse("./endAsset");
	public static final Path ADDON_DINT_LINK_END_TRANSFORM_PATH = Path.parse("./endTransform");

	public static final Path ADDON_DINT_PATH_TABLE_PATH = Path.parse("/DataIntegration/Path/Path");

	public static final Path ADDON_DINT_SQL_DATA_SOURCE_TABLE_PATH = Path
		.parse("/DataIntegration/ReferenceData/SQLDataSource");
	public static final Path ADDON_DINT_SQL_DATA_SOURCE_DATA_MODEL_PATH = Path
		.parse("./ebxDataModel");

	public static final Path ADDON_DINT_TRANSFORM_TABLE_PATH = Path
		.parse("/DataIntegration/VisualMapping/Transform");

	// TODO: Skip these tables?
	//	public static final Path ADDON_DINT_TRANSFORMATION_FUNCTION_TABLE_PATH = Path
	//		.parse("/DataIntegration/ReferenceData/TransformationFunction");
	//	public static final Path ADDON_DINT_TRANSFORMATION_FUNCTION_BUILT_IN_PATH = Path
	//		.parse("./builtIn");
	//	public static final Path ADDON_DINT_TRANSFORMATION_FUNCTION_CODE_PATH = Path.parse("./code");

	public static final Path ADDON_DINT_USER_TEMPLATE_TABLE_PATH = Path
		.parse("/DataIntegration/AdditionalConfiguration/UserTemplate");
	public static final Path ADDON_DINT_USER_TEMPLATE_NAME_PATH = Path.parse("./name");
	public static final Path ADDON_DINT_USER_TEMPLATE_SERVICE_PATH_PATH = Path
		.parse("./fkServicePath");
	public static final Path ADDON_DINT_USER_TEMPLATE_VISUAL_MAPPING_PATH = Path
		.parse("./fkVisualMapping");

	public static final Path ADDON_DINT_VISUAL_MAPPING_TABLE_PATH = Path
		.parse("/DataIntegration/VisualMapping/VisualMapping");
	public static final Path ADDON_DINT_VISUAL_MAPPING_ASSETS_PATH = Path.parse("./asserts");
	public static final Path ADDON_DINT_VISUAL_MAPPING_TRANSFORMATIONS_PATH = Path
		.parse("./transformations");

	public static AdaptationHome getAddonDintDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DINT_DATA_SPACE));
	}

	public static Adaptation getAddonDintConfigurationDataSet(Repository repo)
	{
		return getAddonDintConfigurationDataSet(getAddonDintDataSpace(repo));
	}

	public static Adaptation getAddonDintConfigurationDataSet(AdaptationHome addonDintDataSpace)
	{
		return addonDintDataSpace
			.findAdaptationOrNull(AdaptationName.forName(ADDON_DINT_CONFIGURATION_DATA_SET));
	}

	private AddonDintAdminUtil()
	{
	}
}
