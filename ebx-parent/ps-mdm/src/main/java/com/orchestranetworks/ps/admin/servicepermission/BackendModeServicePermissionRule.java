package com.orchestranetworks.ps.admin.servicepermission;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Configured with a semi-colon separated list of backend modes to disable. Uses ebx.properties
 * to look for what the actual backend.mode property value is, and if the current backend mode is in the collection
 * of modes to disable, then the service permission will be disabled. Otherwise the service will be enabled.
 * 
 * If instantiating via the API (for use with a data space service for example), then you can use the constructor
 * that takes in the backend modes as a parameter, but this can also be configured via the UI when associated with
 * a data model.
 * 
 * The backend modes are defined as constants in {@link com.orchestranetworks.ps.constants.CommonConstants} and are
 * discussed in the EBX documentation.
 */
public class BackendModeServicePermissionRule<S extends EntitySelection>
	implements ServicePermissionRule<S>
{
	private static final String PROPERTY_BACKEND_MODE = "backend.mode";
	private static final String BACKEND_MODE_SEPARATOR = ";";

	private String backendModesToDisable;
	private String[] backendModesToDisableArr;

	/**
	 * Create the rule. An empty constructor is needed to configure this via the UI, but if calling this
	 * programmatically, should either use {@link #BackendModeServicePermissionRule(String)} or invoke
	 * {@link #setBackendModesToDisable(String)} after constructing it.
	 */
	public BackendModeServicePermissionRule()
	{
		this(null);
	}

	/**
	 * Create the rule
	 * 
	 * @param backendModesToDisable the semi-colon separated list of backend modes to not allow the service
	 *        to be invoked for
	 */
	public BackendModeServicePermissionRule(String backendModesToDisable)
	{
		setBackendModesToDisable(backendModesToDisable);
	}

	@Override
	public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
	{
		// It's pointless to use this class without any backend modes specified,
		// but if it was, then it's simply enabled
		if (backendModesToDisableArr.length == 0)
		{
			return UserServicePermission.getEnabled();
		}
		// Load the ebx.properties file. If it can't be loaded, then consider it disabled
		Properties props;
		try
		{
			props = CommonConstants.getEBXProperties();
		}
		catch (IOException ex)
		{
			String str = "Error loading ebx.properties file.";
			LoggingCategory.getKernel().error(str, ex);
			return UserServicePermission.getDisabled(UserMessage.createInfo(str));
		}

		// Get the backend mode property from the properties and if it's one of the
		// backend modes we're disabling for, then it's disabled
		String backendMode = props.getProperty(PROPERTY_BACKEND_MODE);
		if (ArrayUtils.contains(backendModesToDisableArr, backendMode))
		{
			return UserServicePermission.getDisabled(
				UserMessage.createInfo("Can't launch service on a " + backendMode + " instance."));
		}
		return UserServicePermission.getEnabled();
	}

	/**
	 * {@link #setBackendModesToDisable(String)}
	 */
	public String getBackendModesToDisable()
	{
		return this.backendModesToDisable;
	}

	/**
	 * Set the backend modes to disable the service for
	 * 
	 * @param backendModesToDisable a string containing a semi-colon separated list of backend modes
	 */
	public void setBackendModesToDisable(String backendModesToDisable)
	{
		this.backendModesToDisable = backendModesToDisable;
		if (backendModesToDisable == null)
		{
			backendModesToDisableArr = new String[0];
		}
		else
		{
			// Split the string by the separator and store it as an array for use when determining permissions
			backendModesToDisableArr = backendModesToDisable.split(BACKEND_MODE_SEPARATOR);
		}
	}
}
