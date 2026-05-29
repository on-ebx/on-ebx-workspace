package com.orchestranetworks.ps.util.addon;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * A utility class for use with mame addon administration data. Many of its functions rely on things that aren't
 * part of the public API and are subject to change. Defining them here at least keeps it all in one place.
 */
public class AddonMameAdminUtil
{
	public static final String ADDON_MAME_DATA_SPACE = "ebx-addon-mame";
	public static final String ADDON_MAME_CONFIGURATION_DATA_SET = "ebx-addon-mame-configuration";

	public static final Path ADDON_MAME_TABLE_CONFIGURATION_TABLE_PATH = Path
		.parse("/root/TableConfiguration");
	public static final Path ADDON_MAME_TABLE_CONFIGURATION_DATA_MODEL_PATH = Path
		.parse("./dataModel");

	public static final Path ADDON_MAME_BUSINESS_OBJECT_CONFIGURATION_TABLE_PATH = Path
		.parse("/root/BusinessObjectConfiguration");
	public static final Path ADDON_MAME_BUSINESS_OBJECT_CONFIGURATION_DATA_MODEL_PATH = Path
		.parse("./dataModel");

	//
	// MatchAndMergeConfiguration group
	//
	public static final Path ADDON_MAME_MATCHING_FIELD_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/MatchingField");
	public static final Path ADDON_MAME_MATCHING_FIELD_MATCHING_PROCESS_PATH = Path
		.parse("./FKTableMatchingProcess");

	public static final Path ADDON_MAME_MATCHING_PROCESS_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/MatchingProcess");
	public static final Path ADDON_MAME_MATCHING_PROCESS_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_MERGE_POLICY_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/MergePolicy");
	public static final Path ADDON_MAME_MERGE_POLICY_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_MERGE_RELATIONS_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/MergeRelations");
	public static final Path ADDON_MAME_MERGE_RELATIONS_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_REPLICATION_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/Replication");
	public static final Path ADDON_MAME_REPLICATION_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_SURVIVOR_FIELD_TABLE_PATH = Path
		.parse("/root/MatchAndMergeConfiguration/SurvivorField");
	public static final Path ADDON_MAME_SURVIVOR_FIELD_MERGE_POLICY_PATH = Path
		.parse("./FKMergePolicy");

	//
	// ReferenceData group
	//
	public static final Path ADDON_MAME_MATCHING_ALGORITHM_TABLE_PATH = Path
		.parse("/root/ReferenceData/MatchingAlgorithm");
	public static final Path ADDON_MAME_MATCHING_ALGORITHM_IS_PREBUILT_PATH = Path
		.parse("./isPrebuilt");
	public static final Path ADDON_MAME_MATCHING_ALGORITHM_NAME_PATH = Path.parse("./name");

	public static final Path ADDON_MAME_MERGE_FUNCTION_TABLE_PATH = Path
		.parse("/root/ReferenceData/MergeFunction");
	public static final Path ADDON_MAME_MERGE_FUNCTION_IS_PREBUILT_PATH = Path
		.parse("./isPrebuilt");
	public static final Path ADDON_MAME_MERGE_FUNCTION_NAME_PATH = Path.parse("./name");

	public static final Path ADDON_MAME_NODE_FUNCTION_TABLE_PATH = Path
		.parse("/root/ReferenceData/NodeFunction");
	public static final Path ADDON_MAME_NODE_FUNCTION_IS_PREBUILT_PATH = Path.parse("./isPrebuilt");
	public static final Path ADDON_MAME_NODE_FUNCTION_NAME_PATH = Path.parse("./name");

	public static final Path ADDON_MAME_RECORD_SELECTION_POLICY_TABLE_PATH = Path
		.parse("/root/ReferenceData/RecordSelectionPolicy");
	public static final Path ADDON_MAME_RECORD_SELECTION_POLICY_IS_PREBUILT_PATH = Path
		.parse("./isPrebuilt");
	public static final Path ADDON_MAME_RECORD_SELECTION_POLICY_NAME_PATH = Path.parse("./name");

	//
	// ReferenceData/DecisionTree group
	//
	public static final Path ADDON_MAME_DECISION_NODE_TABLE_PATH = Path
		.parse("/root/ReferenceData/DecisionTree/DecisionNode");
	public static final Path ADDON_MAME_DECISION_NODE_DECISION_TREE_PATH = Path
		.parse("./FKDecisionTree");

	public static final Path ADDON_MAME_DECISION_TREE_TABLE_PATH = Path
		.parse("/root/ReferenceData/DecisionTree/DecisionTree");
	public static final Path ADDON_MAME_DECISION_TREE_MATCHING_PROCESS_PATH = Path
		.parse("./FKTableMatchingProcess");

	//
	// ReferenceData/TrustedSource group
	//
	public static final Path ADDON_MAME_FIELD_TRUSTED_SOURCE_TABLE_PATH = Path
		.parse("/root/ReferenceData/TrustedSource/FieldTrustedSource");
	public static final Path ADDON_MAME_FIELD_TRUSTED_SOURCE_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_SOURCE_TABLE_PATH = Path
		.parse("/root/ReferenceData/TrustedSource/Source");
	public static final Path ADDON_MAME_SOURCE_IS_PREBUILT_PATH = Path.parse("./isPrebuilt");
	public static final Path ADDON_MAME_SOURCE_NAME_PATH = Path.parse("./name");

	public static final Path ADDON_MAME_TABLE_TRUSTED_SOURCE_TABLE_PATH = Path
		.parse("/root/ReferenceData/TrustedSource/TableTrustedSource");
	public static final Path ADDON_MAME_TABLE_TRUSTED_SOURCE_TABLE_CONFIGURATION_PATH = Path
		.parse("./FKTableConfiguration");

	public static final Path ADDON_MAME_LINKED_FIELD_GROUP_PATH = Path
		.parse("./Linked_Field_GroupId");
	public static final Path ADDON_MAME_LINKED_FIELD_STATE_PATH = Path
		.parse("./Linked_Field_State");
	public static final Path ADDON_MAME_LINKED_FIELD_AUTO_CREATED_PATH = Path
		.parse("./Linked_Field_AutoCreated");
	public static final Path ADDON_MAME_LINKED_FIELD_TARGET_RECORD_PATH = Path
		.parse("./Linked_Field_GoldenId");

	public static AdaptationHome getAddonMameDataSpace(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(ADDON_MAME_DATA_SPACE));
	}

	public static Adaptation getAddonMameConfigurationDataSet(Repository repo)
	{
		return getAddonMameConfigurationDataSet(getAddonMameDataSpace(repo));
	}

	public static Adaptation getAddonMameConfigurationDataSet(AdaptationHome addonMameDataSpace)
	{
		return addonMameDataSpace
			.findAdaptationOrNull(AdaptationName.forName(ADDON_MAME_CONFIGURATION_DATA_SET));
	}

	private AddonMameAdminUtil()
	{
	}
}
