package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with dama addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonDamaAdminUtil
{
	public static final String ADDON_DAMA_DATA_SPACE = "ebx-addon-dama";
	public static final String ADDON_DAMA_DATA_SET = "ebx-addon-dama";

	public static final Path ADDON_DAMA_DRIVE_TABLE_PATH = Path.parse("/root/driveGroup/drive");
	public static final Path ADDON_DAMA_DRIVE_LABEL_PATH = Path
		.parse("./name/localizedDocumentations/label");
	public static final Path ADDON_DAMA_DRIVE_MAX_USABLE_SPACE_PATH = Path
		.parse("./maxUsableSpace");
	public static final Path ADDON_DAMA_DRIVE_PHYSICAL_ROOT_PATH_PATH = Path
		.parse("./physicalRootPath");
	public static final Path ADDON_DAMA_DRIVE_PHYSICAL_USED_SPACE_PATH = Path
		.parse("./physicalUsedSpace");
	public static final Path ADDON_DAMA_DRIVE_UUID_PATH = Path.parse("./uuid");

	public static final Path ADDON_DAMA_IMAGE_CONFIGURATION_TABLE_PATH = Path
		.parse("/root/driveGroup/imageConfiguration");
	public static final Path ADDON_DAMA_IMAGE_CONFIGURATION_CODE_PATH = Path.parse("./code");

	public static final Path ADDON_DAMA_DIGITAL_ASSET_COMPONENT_TABLE_PATH = Path
		.parse("/root/digitalAssetComponentGroup/digitalAssetComponent");
	public static final Path ADDON_DAMA_DIGITAL_ASSET_COMPONENT_DATA_MODEL_PATH = Path
		.parse("./dataModel");

	public static final Path ADDON_DAMA_VIEW_CONFIGURATION_TABLE_PATH = Path
		.parse("/root/digitalAssetComponentGroup/carouselConfiguration");
	public static final Path ADDON_DAMA_VIEW_CONFIGURATION_CODE_PATH = Path.parse("./code");
	public static final Path ADDON_DAMA_VIEW_CONFIGURATION_LABEL_PATH = Path
		.parse("./name/localizedDocumentations/label");

	public static final Path ADDON_DAMA_METADATA_TYPE_TABLE_PATH = Path
		.parse("/root/referenceData/metaDataType");
	public static final Path ADDON_DAMA_METADATA_TYPE_CODE_PATH = Path.parse("./code");

	public static final Path ADDON_DAMA_DIGITAL_ASSET_TYPE_TABLE_PATH = Path
		.parse("/root/referenceData/digitalAssetType");
	public static final Path ADDON_DAMA_DIGITAL_ASSET_TYPE_BUSINESS_CODE_PATH = Path
		.parse("./businessCode");

	public static final Path ADDON_DAMA_DRIVE_TYPE_TABLE_PATH = Path
		.parse("/root/referenceData/driveType");
	public static final Path ADDON_DAMA_DRIVE_TYPE_CODE_PATH = Path.parse("./code");

	public static final Path ADDON_DAMA_FILE_EXTENSION_TABLE_PATH = Path
		.parse("/root/referenceData/mimeType");
	public static final Path ADDON_DAMA_FILE_EXTENSION_BUSINESS_CODE_PATH = Path
		.parse("./businessCode");

	public static final Path ADDON_DAMA_STATE_TABLE_PATH = Path.parse("/root/referenceData/state");
	public static final Path ADDON_DAMA_STATE_CODE_PATH = Path.parse("./code");

	public static AdaptationHome getAddonDamaDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_DAMA_DATA_SPACE));
	}

	public static Adaptation getAddonDamaDataSet(Repository repo)
	{
		return getAddonDamaDataSet(getAddonDamaDataSpace(repo));
	}

	public static Adaptation getAddonDamaDataSet(AdaptationHome addonDamaDataSpace)
	{
		return addonDamaDataSpace.findAdaptationOrNull(AdaptationName.forName(ADDON_DAMA_DATA_SET));
	}

	private AddonDamaAdminUtil()
	{
	}
}
