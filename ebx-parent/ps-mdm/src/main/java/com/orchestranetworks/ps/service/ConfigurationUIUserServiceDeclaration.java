package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.ui.annotations.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class ConfigurationUIUserServiceDeclaration extends ConfigurationOnlyServiceDeclaration
{

	private String beanClassName;

	public ConfigurationUIUserServiceDeclaration(String moduleName, String beanClassName)
	{
		super(
			moduleName == null ? ServiceKey.forName(getServiceKey(beanClassName))
				: ServiceKey.forModuleServiceName(moduleName, getServiceKey(beanClassName)),
			null,
			getServiceTitle(beanClassName),
			getServiceDescription(beanClassName),
			null);
		this.beanClassName = beanClassName;
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		try
		{
			Class<?> beanClass = ClassLoaderUtils.loadClass(this.beanClassName);
			return new ConfigurationUIUserService<>(beanClass);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}

	}

	public static Class<?> getBeanClass(String beanClassName)
	{
		try
		{
			return ClassLoaderUtils.loadClass(beanClassName);
		}
		catch (ClassNotFoundException e)
		{
			LoggingCategory.getKernel()
				.error("Bean class \"" + beanClassName + "\" for configuration not found.", e);
		}
		return null;
	}

	private static String getServiceKey(String beanClassName)
	{
		return getServiceAnnotation(beanClassName).serviceKey();
	}

	private static String getServiceDescription(String beanClassName)
	{
		return getServiceAnnotation(beanClassName).serviceDescription();
	}

	private static String getServiceTitle(String beanClassName)
	{
		return getServiceAnnotation(beanClassName).nameInMenu();
	}

	private static ConfigurationType getServiceAnnotation(String beanClassName)
	{
		return ConfigurationAnnotationUtils
			.getTypeAnnotation(getBeanClass(beanClassName), ConfigurationType.class);
	}

}
