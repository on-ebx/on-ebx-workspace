package com.orchestranetworks.ps.admin.devartifacts.config;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * An abstract factory for creating a {@link ExportDevArtifactConfig}.
 */
public abstract class AbstractExportDevArtifactsConfigFactory
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
		ExportDevArtifactsConfig config = new ExportDevArtifactsConfig();
		initConfig(config, repo, session, paramMap);
		return config;
	}

	@Override
	protected void initConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException
	{
		// This handles initializing the workflow models in the configuration,
		// but the rest of the configuration is handled by the concrete subclass
		Adaptation workflowModelsConfigurationDataSet = AdminUtil
			.getWorkflowModelsConfigurationDataSet(repo);
		String workflowModelsConfigurationDataSetName = workflowModelsConfigurationDataSet
			.getAdaptationName()
			.getStringName();
		List<String> wfModelNames;
		String tenantPolicy = config.getTenantPolicy();
		// Single tenant exports all workflow models except for the configuration one,
		// which is built-in and is never exported
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(tenantPolicy))
		{
			wfModelNames = AdminUtil.getAllWorkflowModelNames(repo);
			wfModelNames.remove(workflowModelsConfigurationDataSetName);
		}
		// Otherwise, need to export only those that are associated with the tenant's modules
		else
		{
			List<String> modules = config.getModules();
			AdaptationHome wfModelDataSpace = AdminUtil.getWorkflowModelsDataSpace(repo);
			List<Adaptation> wfModelDataSets = wfModelDataSpace.findAllRoots();
			Path moduleNamePath = AdminUtil.getWorkflowModelModuleNamePath();
			wfModelNames = new ArrayList<>();
			for (Adaptation wfModelDataSet : wfModelDataSets)
			{
				String wfModelName = wfModelDataSet.getAdaptationName().getStringName();
				boolean include;
				// Never export the configuration one
				if (workflowModelsConfigurationDataSetName.equals(wfModelName))
				{
					include = false;
				}
				else
				{
					String moduleName = wfModelDataSet.getString(moduleNamePath);
					// Most workflow models are associated with a module, but any that aren't
					// are considered part of the admin tenant
					if (moduleName == null)
					{
						include = DevArtifactsConstants.TENANT_POLICY_MULTI_ADMIN
							.equals(tenantPolicy);
					}
					else
					{
						include = modules.contains(moduleName);
					}
				}
				if (include)
				{
					wfModelNames.add(wfModelName);
				}
			}
		}

		config.setWorkflowModels(wfModelNames);
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

		// Cast the config and set additional params
		ExportDevArtifactsConfig exportConfig = (ExportDevArtifactsConfig) config;

		Boolean downloadToLocal = getBooleanParam(
			paramMap.get(ExportDevArtifactsImpl.PARAM_DOWNLOAD_TO_LOCAL));
		if (downloadToLocal != null)
		{
			exportConfig.setDownloadToLocal(downloadToLocal.booleanValue());
		}
	}
}
