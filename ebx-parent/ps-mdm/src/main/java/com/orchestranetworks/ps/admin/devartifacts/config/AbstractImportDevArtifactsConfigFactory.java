package com.orchestranetworks.ps.admin.devartifacts.config;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.service.*;

/**
 * A factory that creates <code>ImportDevArtifactConfig</code>s.
 */
public abstract class AbstractImportDevArtifactsConfigFactory
	extends
	AbstractDevArtifactsConfigFactory
{
	@Override
	public DevArtifactsConfig createConfig(
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException
	{
		ImportDevArtifactsConfig config = new ImportDevArtifactsConfig();
		initConfig(config, repo, session, paramMap);
		return config;
	}

	@Override
	public void updateConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException
	{
		super.updateConfig(config, repo, session, paramMap);

		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;

		Boolean replaceMode = getBooleanParam(
			paramMap.get(ImportDevArtifactsImpl.PARAM_REPLACE_MODE));
		if (Boolean.TRUE.equals(replaceMode))
		{
			importConfig.setImportMode(ImportSpecMode.REPLACE);
		}
		else if (Boolean.FALSE.equals(replaceMode))
		{
			importConfig.setImportMode(ImportSpecMode.UPDATE_OR_INSERT);
		}
		// replaceMode can be null, in which case stick with the default that was already set

		// If environment copy mode, import all workflow models from the environment copy workflows folder
		if (config.isEnvironmentCopy())
		{
			config.setWorkflowModels(
				DevArtifactsUtil
					.getWorkflowsFromFolder(config.getCopyEnvironmentWorkflowsFolder()));
		}
		// Otherwise if replace mode, import all models from the normal workflows folder
		else if (ImportSpecMode.REPLACE.equals(importConfig.getImportMode()))
		{
			config.setWorkflowModels(
				DevArtifactsUtil.getWorkflowsFromFolder(config.getWorkflowsFolder()));
		}
		// Otherwise just import the ones specified by the http params
		else
		{
			config.setWorkflowModels(getWorkflowModelsFromParamMap(paramMap));
		}

		Boolean skipNonExistingFiles = getBooleanParam(
			paramMap.get(ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES));
		if (skipNonExistingFiles != null)
		{
			importConfig.setSkipNonExistingFiles(skipNonExistingFiles.booleanValue());
		}

		Boolean publishWorkflowModels = getBooleanParam(
			paramMap.get(ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS));
		if (publishWorkflowModels != null)
		{
			importConfig.setPublishWorkflowModels(publishWorkflowModels.booleanValue());
		}
	}

	// Get the list of workflow model names from the HTTP parameter map.
	// They will start with <code>PARAM_WORKFLOW_PREFIX</code>.
	private List<String> getWorkflowModelsFromParamMap(Map<String, String[]> paramMap)
	{
		ArrayList<String> workflows = new ArrayList<>();
		for (Map.Entry<String, String[]> entry : paramMap.entrySet())
		{
			String paramName = entry.getKey();
			if (paramName.startsWith(ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX))
			{
				if (Boolean.TRUE.equals(getBooleanParam(entry.getValue())))
				{
					workflows.add(
						paramName.substring(ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX.length()));
				}
			}
		}
		return workflows;
	}
}
