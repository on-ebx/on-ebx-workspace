package com.orchestranetworks.ps.admin.devartifacts.config;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.service.*;

/**
 * Contains configuration for a dev artifacts import/export
 */
public abstract class DevArtifactsConfig
{
	private File artifactsFolder;
	private File dataFolder;
	private File permissionsFolder;
	private File workflowsFolder;
	private File adminFolder;
	private File perspectivesFolder;
	private File addonAdixFolder;
	private File addonDamaFolder;
	private File addonDintFolder;
	private File addonDpraFolder;
	private File addonDqidFolder;
	private File addonMameFolder;
	private File copyEnvironmentFolder;
	private File copyEnvironmentDataFolder;
	private File copyEnvironmentPermissionsFolder;
	private File copyEnvironmentWorkflowsFolder;
	private File copyEnvironmentAdminFolder;
	private File copyEnvironmentPerspectivesFolder;
	private File copyEnvironmentAddonAdixFolder;
	private File copyEnvironmentAddonDamaFolder;
	private File copyEnvironmentAddonDintFolder;
	private File copyEnvironmentAddonDpraFolder;
	private File copyEnvironmentAddonDqidFolder;
	private File copyEnvironmentAddonMameFolder;
	private File copyEnvironmentDMAFolder;
	private String lineSeparator;
	private Role authorizedRole;
	private String tenantPolicy;
	private List<String> modules;
	private List<AdaptationTablePredicate> tablesForData;
	private boolean refreshSchemas;
	private boolean processDataSetPermissionsInChildDataSpaces;
	private List<Adaptation> dataSetsForPermissions;
	private List<AdaptationHome> dataSpacesForPermissions;
	private List<String> workflowModels;
	private boolean processMessageTemplates;
	private List<Integer[]> tenantMessageTemplateRange;
	private boolean processAdminDataSetPermissions;
	private boolean processDirectoryData;
	private boolean processGlobalPermissionsData;
	private boolean processViewsData;
	private List<String> tenantViewPublications;
	private String tenantViewPublicationsPrefix;
	private boolean processTasksData;
	private boolean processPerspectivesData;
	private List<String> tenantPerspectives;
	private String tenantPerspectivesPrefix;
	private boolean qualifyDataSetAndTableFileNames;
	private String perspectiveWindowName;
	private boolean ignorePerspectiveLogo;
	private boolean processHistorizationProfiles;
	private boolean environmentCopy;
	private String tenantSharedRolesPredicate;
	private String tenantRolesPredicate;
	private String usersRolesPredicate;
	private boolean processAddonRegistrations;
	private boolean processAddonAdixData;
	private String addonAdixPreferencePrefix;
	private String addonAdixTenantPrefix;
	private boolean processAddonDamaData;
	private String addonDamaDrivePathPrefix;
	private String addonDamaTenantPrefix;
	private boolean processAddonDintData;
	private String addonDintUserTemplatePrefix;
	private String addonDintTenantPrefix;
	private boolean processAddonDmdvData;
	private boolean processAddonDpraData;
	private String addonDpraTenantPrefix;
	private boolean processAddonDqidData;
	private String addonDqidTenantPrefix;
	private boolean processAddonMameData;
	private String addonMameTenantPrefix;

	public File getArtifactsFolder()
	{
		return artifactsFolder;
	}

	public void setArtifactsFolder(File artifactsFolder)
	{
		this.artifactsFolder = artifactsFolder;
		// For backwards compatibility, we will handle the individual folder specifications,
		// but going forward, should supply artifactsFolder
		if (artifactsFolder == null)
		{
			this.adminFolder = null;
			this.dataFolder = null;
			this.permissionsFolder = null;
			this.workflowsFolder = null;
			this.perspectivesFolder = null;
			this.addonAdixFolder = null;
			this.addonDamaFolder = null;
			this.addonDintFolder = null;
			this.addonDpraFolder = null;
			this.addonDqidFolder = null;
			this.addonMameFolder = null;
		}
		else
		{
			this.adminFolder = new File(artifactsFolder, DevArtifactsConstants.FOLDER_NAME_ADMIN);
			this.dataFolder = new File(artifactsFolder, DevArtifactsConstants.FOLDER_NAME_DATA);
			this.permissionsFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_PERMISSIONS);
			this.workflowsFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_WORKFLOWS);
			this.perspectivesFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_PERSPECTIVES);
			this.addonAdixFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_ADIX);
			this.addonDamaFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DAMA);
			this.addonDintFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DINT);
			this.addonDpraFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DPRA);
			this.addonDqidFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DQID);
			this.addonMameFolder = new File(
				artifactsFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_MAME);
		}
	}

	public File getDataFolder()
	{
		return this.dataFolder;
	}

	/**
	 * @deprecated Use {@link #setArtifactsFolder(File)} instead
	 */
	@Deprecated
	public void setDataFolder(File dataFolder)
	{
		this.dataFolder = dataFolder;
	}

	public File getPermissionsFolder()
	{
		return this.permissionsFolder;
	}

	/**
	 * @deprecated Use {@link #setArtifactsFolder(File)} instead
	 */
	@Deprecated
	public void setPermissionsFolder(File permissionsFolder)
	{
		this.permissionsFolder = permissionsFolder;
	}

	public File getWorkflowsFolder()
	{
		return this.workflowsFolder;
	}

	/**
	 * @deprecated Use {@link #setArtifactsFolder(File)} instead
	 */
	@Deprecated
	public void setWorkflowsFolder(File workflowsFolder)
	{
		this.workflowsFolder = workflowsFolder;
	}

	public File getAdminFolder()
	{
		return this.adminFolder;
	}

	/**
	 * @deprecated Use {@link #setArtifactsFolder(File)} instead
	 */
	@Deprecated
	public void setAdminFolder(File adminFolder)
	{
		this.adminFolder = adminFolder;
	}

	public File getPerspectivesFolder()
	{
		return this.perspectivesFolder;
	}

	/**
	 * @deprecated Use {@link #setArtifactsFolder(File)} instead
	 */
	@Deprecated
	public void setPerspectivesFolder(File perspectivesFolder)
	{
		this.perspectivesFolder = perspectivesFolder;
	}

	public File getAddonAdixFolder()
	{
		return this.addonAdixFolder;
	}

	public File getAddonDamaFolder()
	{
		return this.addonDamaFolder;
	}

	public File getAddonDintFolder()
	{
		return this.addonDintFolder;
	}

	public File getAddonDpraFolder()
	{
		return this.addonDpraFolder;
	}

	public File getAddonDqidFolder()
	{
		return this.addonDqidFolder;
	}

	public File getAddonMameFolder()
	{
		return this.addonMameFolder;
	}

	public File getCopyEnvironmentFolder()
	{
		return this.copyEnvironmentFolder;
	}

	public void setCopyEnvironmentFolder(File copyEnvironmentFolder)
	{
		this.copyEnvironmentFolder = copyEnvironmentFolder;
		if (copyEnvironmentFolder == null)
		{
			this.copyEnvironmentAdminFolder = null;
			this.copyEnvironmentDataFolder = null;
			this.copyEnvironmentPermissionsFolder = null;
			this.copyEnvironmentWorkflowsFolder = null;
			this.copyEnvironmentDMAFolder = null;
			this.copyEnvironmentPerspectivesFolder = null;
			this.copyEnvironmentAddonAdixFolder = null;
			this.copyEnvironmentAddonDamaFolder = null;
			this.copyEnvironmentAddonDintFolder = null;
			this.copyEnvironmentAddonDpraFolder = null;
			this.copyEnvironmentAddonDqidFolder = null;
			this.copyEnvironmentAddonMameFolder = null;
		}
		else
		{
			this.copyEnvironmentAdminFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADMIN);
			this.copyEnvironmentDataFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_DATA);
			this.copyEnvironmentPermissionsFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_PERMISSIONS);
			this.copyEnvironmentWorkflowsFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_WORKFLOWS);
			this.copyEnvironmentDMAFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_DMA);
			this.copyEnvironmentPerspectivesFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_PERSPECTIVES);
			this.copyEnvironmentAddonAdixFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_ADIX);
			this.copyEnvironmentAddonDamaFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DAMA);
			this.copyEnvironmentAddonDintFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DINT);
			this.copyEnvironmentAddonDpraFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DPRA);
			this.copyEnvironmentAddonDqidFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_DQID);
			this.copyEnvironmentAddonMameFolder = new File(
				copyEnvironmentFolder,
				DevArtifactsConstants.FOLDER_NAME_ADDON_MAME);
		}
	}

	public File getCopyEnvironmentDataFolder()
	{
		return copyEnvironmentDataFolder;
	}

	public File getCopyEnvironmentPermissionsFolder()
	{
		return copyEnvironmentPermissionsFolder;
	}

	public File getCopyEnvironmentWorkflowsFolder()
	{
		return copyEnvironmentWorkflowsFolder;
	}

	public File getCopyEnvironmentAdminFolder()
	{
		return copyEnvironmentAdminFolder;
	}

	public File getCopyEnvironmentDMAFolder()
	{
		return copyEnvironmentDMAFolder;
	}

	public File getCopyEnvironmentPerspectivesFolder()
	{
		return copyEnvironmentPerspectivesFolder;
	}

	public File getCopyEnvironmentAddonAdixFolder()
	{
		return copyEnvironmentAddonAdixFolder;
	}

	public File getCopyEnvironmentAddonDamaFolder()
	{
		return copyEnvironmentAddonDamaFolder;
	}

	public File getCopyEnvironmentAddonDintFolder()
	{
		return copyEnvironmentAddonDintFolder;
	}

	public File getCopyEnvironmentAddonDpraFolder()
	{
		return copyEnvironmentAddonDpraFolder;
	}

	public File getCopyEnvironmentAddonDqidFolder()
	{
		return copyEnvironmentAddonDqidFolder;
	}

	public File getCopyEnvironmentAddonMameFolder()
	{
		return copyEnvironmentAddonMameFolder;
	}

	public String getLineSeparator()
	{
		return this.lineSeparator;
	}

	public void setLineSeparator(String lineSeparator)
	{
		this.lineSeparator = lineSeparator;
	}

	public Role getAuthorizedRole()
	{
		return authorizedRole;
	}

	public void setAuthorizedRole(Role authorizedRole)
	{
		this.authorizedRole = authorizedRole;
	}

	public String getTenantPolicy()
	{
		return tenantPolicy;
	}

	public void setTenantPolicy(String tenantPolicy)
	{
		this.tenantPolicy = tenantPolicy;
	}

	public List<String> getModules()
	{
		return modules;
	}

	public void setModules(List<String> modules)
	{
		this.modules = modules;
	}

	public List<AdaptationTablePredicate> getTablesForData()
	{
		return this.tablesForData;
	}

	public void setTablesForData(List<AdaptationTablePredicate> tablesForData)
	{
		this.tablesForData = tablesForData;
	}

	public boolean isRefreshSchemas()
	{
		return refreshSchemas;
	}

	public void setRefreshSchemas(boolean refreshSchemas)
	{
		this.refreshSchemas = refreshSchemas;
	}

	public boolean isProcessDataSetPermissionsInChildDataSpaces()
	{
		return this.processDataSetPermissionsInChildDataSpaces;
	}

	public void setProcessDataSetPermissionsInChildDataSpaces(
		boolean processDataSetPermissionsInChildDataSpaces)
	{
		this.processDataSetPermissionsInChildDataSpaces = processDataSetPermissionsInChildDataSpaces;
	}

	public List<Adaptation> getDataSetsForPermissions()
	{
		return this.dataSetsForPermissions;
	}

	public void setDataSetsForPermissions(List<Adaptation> dataSetsForPermissions)
	{
		this.dataSetsForPermissions = dataSetsForPermissions;
	}

	public List<AdaptationHome> getDataSpacesForPermissions()
	{
		return this.dataSpacesForPermissions;
	}

	public void setDataSpacesForPermissions(List<AdaptationHome> dataSpacesForPermissions)
	{
		this.dataSpacesForPermissions = dataSpacesForPermissions;
	}

	public List<String> getWorkflowModels()
	{
		return this.workflowModels;
	}

	public void setWorkflowModels(List<String> workflowModels)
	{
		this.workflowModels = workflowModels;
	}

	public boolean isProcessMessageTemplates()
	{
		return processMessageTemplates;
	}

	public void setProcessMessageTemplates(boolean processMessageTemplates)
	{
		this.processMessageTemplates = processMessageTemplates;
	}

	public List<Integer[]> getTenantMessageTemplateRange()
	{
		return tenantMessageTemplateRange;
	}

	public void setTenantMessageTemplateRange(List<Integer[]> tenantMessageTemplateRange)
	{
		this.tenantMessageTemplateRange = tenantMessageTemplateRange;
	}

	public boolean isProcessAdminDataSetPermissions()
	{
		return this.processAdminDataSetPermissions;
	}

	public void setProcessAdminDataSetPermissions(boolean processAdminDataSetPermissions)
	{
		this.processAdminDataSetPermissions = processAdminDataSetPermissions;
	}

	public boolean isProcessDirectoryData()
	{
		return this.processDirectoryData;
	}

	public void setProcessDirectoryData(boolean processDirectoryData)
	{
		this.processDirectoryData = processDirectoryData;
	}

	public boolean isProcessGlobalPermissionsData()
	{
		return this.processGlobalPermissionsData;
	}

	public void setProcessGlobalPermissionsData(boolean processGlobalPermissionsData)
	{
		this.processGlobalPermissionsData = processGlobalPermissionsData;
	}

	public boolean isProcessViewsData()
	{
		return this.processViewsData;
	}

	public void setProcessViewsData(boolean processViewsData)
	{
		this.processViewsData = processViewsData;
	}

	public List<String> getTenantViewPublications()
	{
		return tenantViewPublications;
	}

	public void setTenantViewPublications(List<String> tenantViewPublications)
	{
		this.tenantViewPublications = tenantViewPublications;
	}

	public String getTenantViewPublicationsPrefix()
	{
		return tenantViewPublicationsPrefix;
	}

	public void setTenantViewPublicationsPrefix(String tenantViewPublicationsPrefix)
	{
		this.tenantViewPublicationsPrefix = tenantViewPublicationsPrefix;
	}

	public boolean isProcessTasksData()
	{
		return this.processTasksData;
	}

	public void setProcessTasksData(boolean processTasksData)
	{
		this.processTasksData = processTasksData;
	}

	public boolean isProcessPerspectivesData()
	{
		return this.processPerspectivesData;
	}

	public void setProcessPerspectivesData(boolean processPerspectivesData)
	{
		this.processPerspectivesData = processPerspectivesData;
	}

	public boolean isQualifyDataSetAndTableFileNames()
	{
		return this.qualifyDataSetAndTableFileNames;
	}

	public List<String> getTenantPerspectives()
	{
		return tenantPerspectives;
	}

	public void setTenantPerspectives(List<String> tenantPerspectives)
	{
		this.tenantPerspectives = tenantPerspectives;
	}

	public String getTenantPerspectivesPrefix()
	{
		return tenantPerspectivesPrefix;
	}

	public void setTenantPerspectivesPrefix(String tenantPerspectivesPrefix)
	{
		this.tenantPerspectivesPrefix = tenantPerspectivesPrefix;
	}

	public void setQualifyDataSetAndTableFileNames(boolean qualifyDataSetAndTableFileNames)
	{
		this.qualifyDataSetAndTableFileNames = qualifyDataSetAndTableFileNames;
	}

	public boolean isProcessHistorizationProfiles()
	{
		return processHistorizationProfiles;
	}

	public void setProcessHistorizationProfiles(boolean processHistorizationProfiles)
	{
		this.processHistorizationProfiles = processHistorizationProfiles;
	}

	public String getPerspectiveWindowName()
	{
		return this.perspectiveWindowName;
	}

	public void setPerspectiveWindowName(String perspectiveWindowName)
	{
		this.perspectiveWindowName = perspectiveWindowName;
	}

	public boolean isIgnorePerspectiveLogo()
	{
		return ignorePerspectiveLogo;
	}

	public void setIgnorePerspectiveLogo(boolean ignorePerspectiveLogo)
	{
		this.ignorePerspectiveLogo = ignorePerspectiveLogo;
	}

	public boolean isEnvironmentCopy()
	{
		return this.environmentCopy;
	}

	public void setEnvironmentCopy(boolean environmentCopy)
	{
		this.environmentCopy = environmentCopy;
	}

	public String getTenantSharedRolesPredicate()
	{
		return tenantSharedRolesPredicate;
	}

	public void setTenantSharedRolesPredicate(String tenantSharedRolesPredicate)
	{
		this.tenantSharedRolesPredicate = tenantSharedRolesPredicate;
	}

	public String getTenantRolesPredicate()
	{
		return tenantRolesPredicate;
	}

	public void setTenantRolesPredicate(String tenantRolesPredicate)
	{
		this.tenantRolesPredicate = tenantRolesPredicate;
	}

	public String getUsersRolesPredicate()
	{
		return this.usersRolesPredicate;
	}

	public void setUsersRolesPredicate(String usersRolesPredicate)
	{
		this.usersRolesPredicate = usersRolesPredicate;
	}

	public boolean isProcessAddonRegistrations()
	{
		return processAddonRegistrations;
	}

	public void setProcessAddonRegistrations(boolean processAddonRegistrations)
	{
		this.processAddonRegistrations = processAddonRegistrations;
	}

	public boolean isProcessAddonAdixData()
	{
		return this.processAddonAdixData;
	}

	public void setProcessAddonAdixData(boolean processAddonAdixData)
	{
		this.processAddonAdixData = processAddonAdixData;
	}

	public String getAddonAdixPreferencePrefix()
	{
		return addonAdixPreferencePrefix;
	}

	public void setAddonAdixPreferencePrefix(String addonAdixPreferencePrefix)
	{
		this.addonAdixPreferencePrefix = addonAdixPreferencePrefix;
	}

	public String getAddonAdixTenantPrefix()
	{
		return addonAdixTenantPrefix;
	}

	public void setAddonAdixTenantPrefix(String addonAdixTenantPrefix)
	{
		this.addonAdixTenantPrefix = addonAdixTenantPrefix;
	}

	public boolean isProcessAddonDamaData()
	{
		return processAddonDamaData;
	}

	public void setProcessAddonDamaData(boolean processAddonDamaData)
	{
		this.processAddonDamaData = processAddonDamaData;
	}

	public String getAddonDamaDrivePathPrefix()
	{
		return addonDamaDrivePathPrefix;
	}

	public void setAddonDamaDrivePathPrefix(String addonDamaDrivePathPrefix)
	{
		this.addonDamaDrivePathPrefix = addonDamaDrivePathPrefix;
	}

	public String getAddonDamaTenantPrefix()
	{
		return addonDamaTenantPrefix;
	}

	public void setAddonDamaTenantPrefix(String addonDamaTenantPrefix)
	{
		this.addonDamaTenantPrefix = addonDamaTenantPrefix;
	}

	public boolean isProcessAddonDintData()
	{
		return processAddonDintData;
	}

	public void setProcessAddonDintData(boolean processAddonDintData)
	{
		this.processAddonDintData = processAddonDintData;
	}

	public String getAddonDintUserTemplatePrefix()
	{
		return addonDintUserTemplatePrefix;
	}

	public void setAddonDintUserTemplatePrefix(String addonDintUserTemplatePrefix)
	{
		this.addonDintUserTemplatePrefix = addonDintUserTemplatePrefix;
	}

	public String getAddonDintTenantPrefix()
	{
		return addonDintTenantPrefix;
	}

	public void setAddonDintTenantPrefix(String addonDintTenantPrefix)
	{
		this.addonDintTenantPrefix = addonDintTenantPrefix;
	}

	public boolean isProcessAddonDmdvData()
	{
		return processAddonDmdvData;
	}

	public void setProcessAddonDmdvData(boolean processAddonDmdvData)
	{
		this.processAddonDmdvData = processAddonDmdvData;
	}

	public boolean isProcessAddonDpraData()
	{
		return this.processAddonDpraData;
	}

	public void setProcessAddonDpraData(boolean processAddonDpraData)
	{
		this.processAddonDpraData = processAddonDpraData;
	}

	public String getAddonDpraTenantPrefix()
	{
		return addonDpraTenantPrefix;
	}

	public void setAddonDpraTenantPrefix(String addonDpraTenantPrefix)
	{
		this.addonDpraTenantPrefix = addonDpraTenantPrefix;
	}

	public boolean isProcessAddonDqidData()
	{
		return this.processAddonDqidData;
	}

	public void setProcessAddonDqidData(boolean processAddonDqidData)
	{
		this.processAddonDqidData = processAddonDqidData;
	}

	public String getAddonDqidTenantPrefix()
	{
		return addonDqidTenantPrefix;
	}

	public void setAddonDqidTenantPrefix(String addonDqidTenantPrefix)
	{
		this.addonDqidTenantPrefix = addonDqidTenantPrefix;
	}

	public boolean isProcessAddonMameData()
	{
		return processAddonMameData;
	}

	public void setProcessAddonMameData(boolean processAddonMameData)
	{
		this.processAddonMameData = processAddonMameData;
	}

	public String getAddonMameTenantPrefix()
	{
		return addonMameTenantPrefix;
	}

	public void setAddonMameTenantPrefix(String addonMameTenantPrefix)
	{
		this.addonMameTenantPrefix = addonMameTenantPrefix;
	}
}