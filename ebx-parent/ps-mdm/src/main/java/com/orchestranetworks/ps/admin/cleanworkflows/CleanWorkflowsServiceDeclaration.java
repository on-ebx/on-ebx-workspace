package com.orchestranetworks.ps.admin.cleanworkflows;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.servicepermission.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.schema.types.dataspace.DataspaceSet.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 * Declares the {@link CleanWorkflowsUserService}.
 *
 * This service can be enabled only for certain environments or data spaces.
 * See {@link #defineActivation(ActivationContextOnDataspace)}.
 */
public class CleanWorkflowsServiceDeclaration extends TechAdminOnlyServiceDeclaration
{
	public static final String CLEAN_WORKFLOWS_SERVICE_KEY = "CleanWorkflows";
	public static final String CLEAN_WORKFLOWS_SERVICE_TITLE = "Clean Workflows";

	private String backendModesToDisable;
	private String propertiesFolder = CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FOLDER;
	private String propertiesFile = CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FILE;
	private Set<HomeKey> includedDataSpaces = new HashSet<>();
	private DataspaceChildrenPolicy includedDataSpaceChildrenPolicy = DataspaceChildrenPolicy.NONE;

	/**
	 * Create the service declaration, using the given module name, using default service name and service title,
	 * and make it not available in production environments.
	 *
	 * This is equivalent of calling {@link #CleanWorkflowsServiceDeclaration(String, String, String, String)}
	 * with {@link #CLEAN_WORKFLOWS_SERVICE_KEY}, {@link #CLEAN_WORKFLOWS_SERVICE_TITLE}, and
	 * {@link com.orchestranetworks.ps.constants.CommonConstants#BACKEND_MODE_PRODUCTION}.
	 *
	 * @param moduleName the module name
	 * @param serviceName the service name, which will be used by the service key. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_KEY} if <code>null</code> is passed in.
	 * @param serviceTitle the title of the service. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_TITLE} if <code>null</code> is passed in.
	 */
	public CleanWorkflowsServiceDeclaration(String moduleName)
	{
		this(
			moduleName,
			CLEAN_WORKFLOWS_SERVICE_KEY,
			CLEAN_WORKFLOWS_SERVICE_TITLE,
			CommonConstants.BACKEND_MODE_PRODUCTION);
	}

	/**
	 * Create the service declaration, using the given module name, service name, and service title,
	 * and make it not available in production environments.
	 *
	 * This is equivalent of calling {@link #CleanWorkflowsServiceDeclaration(String, String, String, String)}
	 * with {@link com.orchestranetworks.ps.constants.CommonConstants#BACKEND_MODE_PRODUCTION}.
	 *
	 * @param moduleName the module name
	 * @param serviceName the service name, which will be used by the service key. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_KEY} if <code>null</code> is passed in.
	 * @param serviceTitle the title of the service. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_TITLE} if <code>null</code> is passed in.
	 */
	public CleanWorkflowsServiceDeclaration(
		String moduleName,
		String serviceName,
		String serviceTitle)
	{
		this(moduleName, serviceName, serviceTitle, CommonConstants.BACKEND_MODE_PRODUCTION);
	}

	/**
	 * Create the service declaration, using the given module name, service name, and service title
	 *
	 * @param moduleName the module name
	 * @param serviceName the service name, which will be used by the service key. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_KEY} if <code>null</code> is passed in.
	 * @param serviceTitle the title of the service. Will be
	 *                   {@link #CLEAN_WORKFLOWS_SERVICE_TITLE} if <code>null</code> is passed in.
	 * @param backendModesToDisable the backend modes to disable the service for
	 */
	public CleanWorkflowsServiceDeclaration(
		String moduleName,
		String serviceName,
		String serviceTitle,
		String backendModesToDisable)
	{
		super(
			moduleName == null
				? ServiceKey
					.forName(serviceName == null ? CLEAN_WORKFLOWS_SERVICE_KEY : serviceName)
				: ServiceKey.forModuleServiceName(
					moduleName,
					serviceName == null ? CLEAN_WORKFLOWS_SERVICE_KEY : serviceName),
			null,
			serviceTitle == null ? CLEAN_WORKFLOWS_SERVICE_TITLE : serviceTitle,
			null,
			"");
		this.backendModesToDisable = backendModesToDisable;
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		CleanWorkflowsUserService service = new CleanWorkflowsUserService(
			propertiesFolder,
			propertiesFile);
		return service;
	}

	@Override
	public void defineActivation(ActivationContextOnDataspace context)
	{
		// If no backend modes were specified, just do what the super class does,
		// which will make it available only to Tech Admins
		if (backendModesToDisable == null)
		{
			super.defineActivation(context);
		}
		// If they were specified, need to combine the Tech Admin restriction with additional
		// check that backend mode matches one of the specified backend modes.
		else
		{
			CompoundServicePermissionRule<DataspaceEntitySelection> compoundRule = new CompoundServicePermissionRule<>();
			compoundRule.setMessageSeparator(" ");
			compoundRule.appendRule(new TechAdminOnlyServicePermissionRules.OnDataSpace<>());
			compoundRule.appendRule(new BackendModeServicePermissionRule<>(backendModesToDisable));
			context.setPermissionRule(compoundRule);
		}
		for (HomeKey key : includedDataSpaces)
		{
			context.includeDataspacesMatching(key, includedDataSpaceChildrenPolicy);
		}
	}

	public String getPropertiesFolder()
	{
		return this.propertiesFolder;
	}

	public void setPropertiesFolder(String propertiesFolder)
	{
		this.propertiesFolder = propertiesFolder;
	}

	public String getPropertiesFile()
	{
		return this.propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}

	public Set<HomeKey> getIncludedDataSpaces()
	{
		return this.includedDataSpaces;
	}

	public void setIncludedDataSpaces(Set<HomeKey> includedDataSpaces)
	{
		this.includedDataSpaces = includedDataSpaces;
	}

	public DataspaceChildrenPolicy getIncludedDataSpaceChildrenPolicy()
	{
		return this.includedDataSpaceChildrenPolicy;
	}

	public void setIncludedDataSpaceChildrenPolicy(
		DataspaceChildrenPolicy includedDataSpaceChildrenPolicy)
	{
		this.includedDataSpaceChildrenPolicy = includedDataSpaceChildrenPolicy;
	}
}
