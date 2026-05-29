package com.orchestranetworks.ps.admin.devartifacts.config;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * A factory for <code>DevArtifactConfig</code>s.
 */
public interface DevArtifactsConfigFactory
{
	/**
	 * Create the configuration
	 * 
	 * @param repo the repository
	 * @param session the user's session
	 * @param paramMap the map containing the http parameters.
	 *                 See {@link javax.servlet.ServletRequest#getParameterMap()}.
	 * @return the configuration
	 */
	DevArtifactsConfig createConfig(
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException;

	/**
	 * Update the configuration from http params. This can be called to update the configuration with
	 * additional information from a user service or servlet.
	 * 
	 * @param config the config to update
	 * @param repo the repository
	 * @param session the user's session
	 * @param paramMap the map containing the http parameters.
	 *                 See {@link javax.servlet.ServletRequest#getParameterMap()}.
	 */
	void updateConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException;
}
