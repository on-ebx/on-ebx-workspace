package com.orchestranetworks.ps.admin.devartifacts.config;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.service.*;

/**
 * An abstract factory that can initialize a config from http parameters.
 * A concrete subclass should handle initializing the config from whatever mechanism is used
 * for the stored configuration (e.g. a properties file).
 */
public abstract class AbstractDevArtifactsConfigFactory implements DevArtifactsConfigFactory
{
	/**
	 * Initialize the configuration
	 * 
	 * @param config the config to initialize
	 * @param repo the repository
	 * @param session the user's session
	 * @param paramMap the map containing the http parameters.
	 *                 See {@link javax.servlet.ServletRequest#getParameterMap()}.
	 */
	protected abstract void initConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException;

	@Override
	public void updateConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException
	{
		String[] environmentCopyValues = paramMap.get(DevArtifactsBase.PARAM_ENVIRONMENT_COPY);
		Boolean envCopy = getBooleanParam(environmentCopyValues);
		if (envCopy != null)
		{
			config.setEnvironmentCopy(envCopy.booleanValue());
		}
	}

	/**
	 * Convenience method for getting an http parameter's boolean value.
	 * The parameter map contains an array of strings for each parameter's value,
	 * but for the boolean parameters we know it will only contain at most one value with either "false" or "true".
	 * This will return the Boolean object representing either false, true, or <code>null</code> if not specified.
	 * 
	 * @param paramValues the array of values. See {@link javax.servlet.ServletRequest#getParameterMap()}.
	 * @return the Boolean value, or <code>null</code>
	 */
	protected static Boolean getBooleanParam(String[] paramValues)
	{
		if (paramValues == null || paramValues.length == 0)
		{
			return null;
		}
		return Boolean.valueOf(paramValues[0]);
	}
}
