package com.orchestranetworks.ps.admin.devartifacts.config;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.service.*;

/**
 * A config for importing dev artifacts.
 */
public class ImportDevArtifactsConfig extends DevArtifactsConfig
{
	private ImportSpecMode importMode;
	private boolean skipNonExistingFiles;
	private List<DataSetCreationInfo> dataSetsToCreate;
	private Map<DataSetCreationKey, List<String>> createdDataSetTableSpecs;
	private boolean publishWorkflowModels;
	private Map<String, Set<String>> masterWorkflowModels;
	private List<String> workflowModelsToNotPublish;
	private boolean removeAdministratorRole;

	public ImportSpecMode getImportMode()
	{
		return this.importMode;
	}

	public void setImportMode(ImportSpecMode importMode)
	{
		this.importMode = importMode;
	}

	public boolean isSkipNonExistingFiles()
	{
		return this.skipNonExistingFiles;
	}

	public void setSkipNonExistingFiles(boolean skipNonExistingFiles)
	{
		this.skipNonExistingFiles = skipNonExistingFiles;
	}

	public List<DataSetCreationInfo> getDataSetsToCreate()
	{
		return this.dataSetsToCreate;
	}

	public void setDataSetsToCreate(List<DataSetCreationInfo> dataSetsToCreate)
	{
		this.dataSetsToCreate = dataSetsToCreate;
	}

	public Map<DataSetCreationKey, List<String>> getCreatedDataSetTableSpecs()
	{
		return this.createdDataSetTableSpecs;
	}

	public void setCreatedDataSetTableSpecs(
		Map<DataSetCreationKey, List<String>> createdDataSetTableSpecs)
	{
		this.createdDataSetTableSpecs = createdDataSetTableSpecs;
	}

	public boolean isPublishWorkflowModels()
	{
		return this.publishWorkflowModels;
	}

	public void setPublishWorkflowModels(boolean publishWorkflowModels)
	{
		this.publishWorkflowModels = publishWorkflowModels;
	}

	public Map<String, Set<String>> getMasterWorkflowModels()
	{
		return this.masterWorkflowModels;
	}

	public void setMasterWorkflowModels(Map<String, Set<String>> masterWorkflowModels)
	{
		this.masterWorkflowModels = masterWorkflowModels;
	}

	public List<String> getWorkflowModelsToNotPublish()
	{
		return this.workflowModelsToNotPublish;
	}

	public void setWorkflowModelsToNotPublish(List<String> workflowModelsToNotPublish)
	{
		this.workflowModelsToNotPublish = workflowModelsToNotPublish;
	}

	public boolean isRemoveAdministratorRole()
	{
		return removeAdministratorRole;
	}

	public void setRemoveAdministratorRole(boolean removeAdministratorRole)
	{
		this.removeAdministratorRole = removeAdministratorRole;
	}
}
