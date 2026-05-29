package com.orchestranetworks.ps.admin.devartifacts.addon.dint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * A cache used by the New Data Exchange Dev Artifacts processing.
 * Many tables are reliant on which records from other related tables are being processed.
 * That would be very inefficient to repeatedly query other tables that have already been processed,
 * so instead as they get processed, they get cached here for use in other filters.
 */
public class AddonDintDevArtifactsCache
{
	private Set<String> userTemplatePrimaryKeys;
	private Set<String> pathPrimaryKeys;
	private Set<String> visualMappingPrimaryKeys;
	private Set<String> assetPrimaryKeys;
	private Set<String> transformPrimaryKeys;

	public void load(DevArtifactsConfig config, Adaptation dintConfigurationDataSet)
	{
		userTemplatePrimaryKeys = new HashSet<>();
		pathPrimaryKeys = new HashSet<>();
		visualMappingPrimaryKeys = new HashSet<>();
		assetPrimaryKeys = new HashSet<>();
		transformPrimaryKeys = new HashSet<>();
		pathPrimaryKeys = new HashSet<>();

		String userTemplatePrefix = config.getAddonDintUserTemplatePrefix();
		if (userTemplatePrefix != null)
		{
			AdaptationTable userTemplateTable = dintConfigurationDataSet
				.getTable(AddonDintAdminUtil.ADDON_DINT_USER_TEMPLATE_TABLE_PATH);
			String userTemplatePredicate = DevArtifactsUtil.createAddonPrefixPredicate(
				false,
				null,
				false,
				AddonDintAdminUtil.ADDON_DINT_USER_TEMPLATE_NAME_PATH,
				userTemplatePrefix);
			RequestResult userTemplateRequestResult = userTemplateTable
				.createRequestResult(userTemplatePredicate);
			try
			{
				for (Adaptation userTemplate; (userTemplate = userTemplateRequestResult
					.nextAdaptation()) != null;)
				{
					userTemplatePrimaryKeys.add(userTemplate.getOccurrencePrimaryKey().format());

					String servicePathPK = userTemplate
						.getString(AddonDintAdminUtil.ADDON_DINT_USER_TEMPLATE_SERVICE_PATH_PATH);
					if (servicePathPK != null)
					{
						pathPrimaryKeys.add(servicePathPK);
					}

					List<Adaptation> visualMappings = AdaptationUtil.followFKList(
						userTemplate,
						AddonDintAdminUtil.ADDON_DINT_USER_TEMPLATE_VISUAL_MAPPING_PATH);
					if (visualMappings != null)
					{
						for (Adaptation visualMapping : visualMappings)
						{
							visualMappingPrimaryKeys
								.add(visualMapping.getOccurrencePrimaryKey().format());

							RequestResult assetRequestResult = AdaptationUtil.linkedRecordLookup(
								visualMapping,
								AddonDintAdminUtil.ADDON_DINT_VISUAL_MAPPING_ASSETS_PATH);
							try
							{
								for (Adaptation asset; (asset = assetRequestResult
									.nextAdaptation()) != null;)
								{
									assetPrimaryKeys.add(asset.getOccurrencePrimaryKey().format());

									String pathPK = asset.getString(
										AddonDintAdminUtil.ADDON_DINT_ASSET_PATH_FIELD_PATH);
									if (pathPK != null)
									{
										pathPrimaryKeys.add(pathPK);
									}
								}
							}
							finally
							{
								assetRequestResult.close();
							}

							RequestResult transformRequestResult = AdaptationUtil
								.linkedRecordLookup(
									visualMapping,
									AddonDintAdminUtil.ADDON_DINT_VISUAL_MAPPING_TRANSFORMATIONS_PATH);
							try
							{
								for (Adaptation transform; (transform = transformRequestResult
									.nextAdaptation()) != null;)
								{
									transformPrimaryKeys
										.add(transform.getOccurrencePrimaryKey().format());
								}
							}
							finally
							{
								transformRequestResult.close();
							}
						}
					}
				}
			}
			finally
			{
				userTemplateRequestResult.close();
			}
		}
	}

	public Set<String> getUserTemplatePrimaryKeys()
	{
		return userTemplatePrimaryKeys;
	}

	public void setUserTemplatePrimaryKeys(Set<String> userTemplatePrimaryKeys)
	{
		this.userTemplatePrimaryKeys = userTemplatePrimaryKeys;
	}

	public Set<String> getPathPrimaryKeys()
	{
		return pathPrimaryKeys;
	}

	public void setPathPrimaryKeys(Set<String> pathPrimaryKeys)
	{
		this.pathPrimaryKeys = pathPrimaryKeys;
	}

	public Set<String> getVisualMappingPrimaryKeys()
	{
		return visualMappingPrimaryKeys;
	}

	public void setVisualMappingPrimaryKeys(Set<String> visualMappingPrimaryKeys)
	{
		this.visualMappingPrimaryKeys = visualMappingPrimaryKeys;
	}

	public Set<String> getAssetPrimaryKeys()
	{
		return assetPrimaryKeys;
	}

	public void setAssetPrimaryKeys(Set<String> assetPrimaryKeys)
	{
		this.assetPrimaryKeys = assetPrimaryKeys;
	}

	public Set<String> getTransformPrimaryKeys()
	{
		return transformPrimaryKeys;
	}

	public void setTransformPrimaryKeys(Set<String> transformPrimaryKeys)
	{
		this.transformPrimaryKeys = transformPrimaryKeys;
	}
}
