package com.orchestranetworks.ps.admin.devartifacts.rest;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;

public abstract class AbstractImportDevArtifactsRESTService extends AbstractDevArtifactsRESTService
{
	@Override
	protected DevArtifactsBase createImpl()
	{
		return new ImportDevArtifactsImpl();
	}

	protected void updateParamMap(Map<String, String[]> paramMap, DevArtifactsDTO dto)
	{
		super.updateParamMap(paramMap, dto);
		updateBooleanParameter(
			paramMap,
			ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS,
			dto.getPublishWorkflowModels());
		updateBooleanParameter(
			paramMap,
			ImportDevArtifactsImpl.PARAM_REPLACE_MODE,
			dto.getReplaceMode());
		updateBooleanParameter(
			paramMap,
			ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES,
			dto.getSkipNonExistingFiles());
		List<String> workflowModels = dto.getWorkflowModels();
		if (workflowModels != null)
		{
			for (String workflowModel : workflowModels)
			{
				if (workflowModel != null)
				{
					paramMap.put(
						ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX + workflowModel,
						new String[] { Boolean.TRUE.toString() });
				}
			}
		}
	}

	private static void updateBooleanParameter(
		Map<String, String[]> paramMap,
		String parameter,
		Boolean value)
	{
		if (value != null)
		{
			paramMap.put(parameter, new String[] { value.toString() });
		}
	}
}