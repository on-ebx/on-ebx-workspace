package com.orchestranetworks.ps.module;

import java.lang.reflect.*;
import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.module.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.admin.cleanworkflows.*;
import com.orchestranetworks.ps.admin.devartifacts.service.*;
import com.orchestranetworks.ps.messaging.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.validation.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.userservice.declaration.*;

/***
 * Registers standard Orchestra Networks Professional Services (PS) services, such as Dev Artifacts.
 * This should be extended to supply the module name in the constructor.
 * 
 * {@link PSRegisterServlet} still works, but in order to use the REST toolkit with EBX v. 5.9,
 * you need to use a {@link ModuleRegistrationListener}, so it's preferable to use this class.
 */
public class PSModuleRegistration implements ModuleRegistration
{
	public static final String ADMIN_GROUP = "Admin";
	public static final String ADMIN_GROUP_DESCRIPTION = "Dev artifacts and other deployment services";

	public static final String CONFIGURATION_GROUP = "Configuration";
	private static final String CONFIGURATION_GROUP_DESCRIPTION = "Configuration for LDAP, Messaging SSO and other configuration services";

	private static final String RESET_CUSTOM_DIRECTORY_SERVICE_CLASS_NAME = "com.orchestranetworks.ps.cache.ResetCustomDirectoryCacheDeclaration";
	private static final String CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME = "com.orchestranetworks.ps.service.ConfigurationUIUserServiceDeclaration";

	private String moduleName;
	private String adminGroupName = ADMIN_GROUP;
	private String adminGroupLabel = ADMIN_GROUP;
	private String adminGroupDescription = ADMIN_GROUP_DESCRIPTION;

	protected PSModuleRegistration(String moduleName)
	{
		this.moduleName = moduleName;

		redefineStaticConstants();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void handleServiceRegistration(ModuleServiceRegistrationContext aContext)
	{
		// Register the Admin group
		registerServiceGroup(aContext);

		// Create a map and initialize it with the ps services
		Map<String, UserServiceDeclaration> serviceDeclarations = new HashMap<>();
		initServiceDeclarations(serviceDeclarations);

		// Register each service
		for (UserServiceDeclaration serviceDeclaration : serviceDeclarations.values())
		{
			if (!ADMIN_GROUP.equals(adminGroupName)
				&& serviceDeclaration instanceof TechAdminOnlyServiceDeclaration)
			{
				((TechAdminOnlyServiceDeclaration) serviceDeclaration)
					.setServiceGroupName(adminGroupName);
			}
			aContext.registerUserService(serviceDeclaration);
		}
	}

	/**
	 * Register the service group that the services will belong to.
	 * This registers an Admin group with a standard description,
	 * but can be overridden in order to register a different group or none at all.
	 * 
	 * @param aContext the context
	 */
	protected void registerServiceGroup(ModuleServiceRegistrationContext aContext)
	{
		aContext.registerServiceGroup(
			ServiceGroupKey.forServiceGroupInModule(moduleName, adminGroupName),
			UserMessage.createInfo(adminGroupLabel),
			UserMessage.createInfo(adminGroupDescription));

		aContext.registerServiceGroup(
			ServiceGroupKey.forServiceGroupInModule(moduleName, CONFIGURATION_GROUP),
			UserMessage.createInfo(CONFIGURATION_GROUP),
			UserMessage.createInfo(CONFIGURATION_GROUP_DESCRIPTION));
	}

	/**
	 * Initialize the given map with the ps service declarations to register.
	 * This adds all the standard services, but can be overridden to remove certain services,
	 * or configure them differently, or add new ones.
	 * 
	 * @param serviceDeclarations the map of service declarations, with a key of the service name
	 */
	@SuppressWarnings("rawtypes")
	protected void initServiceDeclarations(Map<String, UserServiceDeclaration> serviceDeclarations)
	{
		addServiceDeclaration(
			serviceDeclarations,
			new ImportDevArtifactsPropertiesFileDSDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new ExportDevArtifactsPropertiesFileDSDeclaration(moduleName));
		addServiceDeclaration(serviceDeclarations, new DownloadEBXLogFilesDeclaration(moduleName));
		addServiceDeclaration(serviceDeclarations, new DownloadEBXArchivesDeclaration(moduleName));
		addServiceDeclaration(serviceDeclarations, new EncryptionServiceDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new CleanWorkflowsServiceDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new ClearPermissionsUserCacheServiceDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateDataDictionaryDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateDataSetValidationReportDeclarations.OnDataSet(
				moduleName,
				Severity.WARNING));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateTableValidationReportDeclarations.OnTableView(
				moduleName,
				Severity.WARNING));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateTableCompareReportDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateDataSetCompareReportDeclarations.OnDataSet(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new GenerateDataSpaceCompareReportDeclaration(moduleName));
		addServiceDeclaration(
			serviceDeclarations,
			new InitializeDefaultFieldValuesDeclaration(moduleName));
		addServiceDeclaration(serviceDeclarations, new ShowNodeDeclaration(moduleName));
		addServiceDeclaration(serviceDeclarations, new ShowTechnicalFieldsDeclaration(moduleName));
		// not all services are defined on the PS lib.
		Object resetCustomCacheClass = loadServiceClass(RESET_CUSTOM_DIRECTORY_SERVICE_CLASS_NAME);
		if (resetCustomCacheClass != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) resetCustomCacheClass);
		}
		Object ldapConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.beans.configuration.LDAPConfiguration");
		if (ldapConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) ldapConfigurationService);
		}
		Object ssoConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.beans.configuration.SSOConfiguration");
		if (ssoConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) ssoConfigurationService);
		}
		Object messagingConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.messaging.beans.configuration.JNDIMessagingConfiguration");
		if (messagingConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) messagingConfigurationService);
		}

		Object emsMessagingConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.messaging.beans.configuration.JNDIMessagingConfiguration");
		if (emsMessagingConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) emsMessagingConfigurationService);
		}

		Object rabbitMQMessagingConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.messaging.client.rabbitmq.beans.RabbitMQMessagingConfiguration");
		if (rabbitMQMessagingConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) rabbitMQMessagingConfigurationService);
		}

		Object tibcoEMSMessagingConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.messaging.client.tibco.beans.TibcoEMSMessagingConfiguration");
		if (tibcoEMSMessagingConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) tibcoEMSMessagingConfigurationService);
		}

		Object kafkaMessagingConfigurationService = loadServiceClass(
			CUSTOM_CONFIGURATION_SERVICE_CLASS_NAME,
			"com.orchestranetworks.ps.messaging.client.kafka.beans.KafkaMessagingConfiguration");
		if (kafkaMessagingConfigurationService != null)
		{
			addServiceDeclaration(
				serviceDeclarations,
				(UserServiceDeclaration) kafkaMessagingConfigurationService);
		}
	}

	/**
	 * A convenience method to add a service declaration to the given map, using its service key's service name as the key
	 * 
	 * @param serviceDeclarations the map of service declarations, with the service name as the key
	 * @param serviceDeclaration the service declaration to add
	 */
	@SuppressWarnings("rawtypes")
	protected static final void addServiceDeclaration(
		Map<String, UserServiceDeclaration> serviceDeclarations,
		UserServiceDeclaration serviceDeclaration)
	{
		serviceDeclarations
			.put(serviceDeclaration.getServiceKey().getServiceName(), serviceDeclaration);
	}

	/**
	 * A convenience method to remove a service declaration from the given map, using its service key's service name as the key
	 *
	 * @param serviceDeclarations the map of service declarations, with the service name as the key
	 * @param serviceKeyName the service key name of the service declaration to remove
	 */
	@SuppressWarnings("rawtypes")
	protected static final void removeServiceDeclaration(
		Map<String, UserServiceDeclaration> serviceDeclarations,
		String serviceKeyName)
	{
		serviceDeclarations.remove(serviceKeyName);
	}

	/**
	 * Redefine any static (non-final) constants that will be used by your module.
	 * For example, {@link com.orchestranetworks.ps.workflow.WorkflowConstants} defines standard data context parameter names,
	 * but here you can redefine what the parameter names are for your module.
	 * 
	 * By default, this does nothing because the constants are set to their defaults already. This method exists so that
	 * subclasses can more easily define their constants.
	 * 
	 * Note that these constants are static across the JVM instance, so if more than one module extends this class, they should be careful
	 * not to define the constants differently.
	 */
	protected synchronized void redefineStaticConstants()
	{
		// Does nothing by default, but an example of what you'd do in a subclass would be:
		// WorkflowConstants.PARAM_WORKING_DATA_SPACE = "dataSpace";
	}

	private Object loadServiceClass(String className)
	{
		try
		{
			Class<?> classToload = Class.forName(className);
			Constructor<?> constructor = classToload.getConstructor(String.class);
			return constructor.newInstance(moduleName);
		}
		catch (Exception ex)
		{
			// how to log a warning to the logs from here???
			return null;
		}
	}

	private Object loadServiceClass(String className, String beanClassName)
	{
		try
		{
			Class<?> classToload = Class.forName(className);
			// if the bean class not present we should not load the service
			Class.forName(beanClassName);
			Constructor<?> constructor = classToload.getConstructor(String.class, String.class);
			return constructor.newInstance(moduleName, beanClassName);
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public String getModuleName()
	{
		return moduleName;
	}

	@Override
	public void destroyBeforeUnregisterModule()
	{
		try
		{
			MessagingConfiguration.shutdown();
		}
		catch (Exception e)
		{

			LoggingCategory.getKernel().error(
				"[Register Servlet Shutdown] - error closing messaging client connection.",
				e);
		}
	}

	public String getAdminGroupName()
	{
		return adminGroupName;
	}

	public void setAdminGroupName(String adminGroupName)
	{
		this.adminGroupName = adminGroupName;
	}

	public String getAdminGroupLabel()
	{
		return adminGroupLabel;
	}

	public void setAdminGroupLabel(String adminGroupLabel)
	{
		this.adminGroupLabel = adminGroupLabel;
	}

	public String getAdminGroupDescription()
	{
		return adminGroupDescription;
	}

	public void setAdminGroupDescription(String adminGroupDescription)
	{
		this.adminGroupDescription = adminGroupDescription;
	}
}
