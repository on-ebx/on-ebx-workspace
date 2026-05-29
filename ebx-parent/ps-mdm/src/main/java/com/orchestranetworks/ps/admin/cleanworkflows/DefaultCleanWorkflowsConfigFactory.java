/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.io.*;
import java.text.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * A factory that can initialize configs from a properties file.
 * The file and its folder can be specified in the constructor, or can be retrieved
 * from system variables that are set at start-up. System variables will always take
 * precedence, even if passed into the constructor.
 *
 * If specified by system variables, they are defined by
 * {@link CleanWorkflowsPropertyFileHelper#DEFAULT_PROPERTIES_FOLDER} and
 * {@link CleanWorkflowsPropertyFileHelper#DEFAULT_PROPERTIES_FILE}.
 */
public class DefaultCleanWorkflowsConfigFactory implements CleanWorkflowsConfigFactory
{
	protected String propertiesFolder;
	protected String propertiesFile;

	/**
	 * Create the factory, using the default folder and properties file specified as environment variables.
	 */
	public DefaultCleanWorkflowsConfigFactory()
	{
		this(null, null);
	}

	/**
	 * Create the factory with the given folder and properties file, unless the defaults have been specified
	 * as environment variables
	 *
	 * @param propertiesFolder the folder
	 * @param propertiesFile the properties file
	 */
	public DefaultCleanWorkflowsConfigFactory(String propertiesFolder, String propertiesFile)
	{
		this.propertiesFolder = propertiesFolder;
		this.propertiesFile = propertiesFile;
	}

	@Override
	public CleanWorkflowsConfig createConfig(Repository repo, Session session)
		throws OperationException
	{
		CleanWorkflowsConfig config = new CleanWorkflowsConfig();
		initConfig(config, repo, session);
		return config;
	}

	/**
	 * Initialize the configuration by reading from the properties file
	 *
	 * @param config the config to initialize
	 * @param repo the repository
	 * @param session the session
	 * @throws OperationException if there is an error loading the properties
	 */
	protected void initConfig(CleanWorkflowsConfig config, Repository repo, Session session)
		throws OperationException
	{
		// Get properties file from system property
		String folder = System
			.getProperty(CleanWorkflowsPropertyFileHelper.PROPERTIES_FOLDER_SYSTEM_PROPERTY);
		// If it wasn't specified as a system property, then use the one passed in
		if (folder == null)
		{
			folder = propertiesFolder;
		}
		// Do the same for the file
		String file = System
			.getProperty(CleanWorkflowsPropertyFileHelper.PROPERTIES_FILE_SYSTEM_PROPERTY);
		if (file == null)
		{
			file = propertiesFile;
		}
		loadProperties(config, repo, session, folder + "/" + file);
	}

	/**
	 * Load properties from the given file into the config
	 *
	 * @param config the config
	 * @param repo the repository
	 * @param session the session
	 * @param fullFilePath the path to the file, including the folder
	 * @throws OperationException if an error occurs loading the properties
	 */
	protected void loadProperties(
		CleanWorkflowsConfig config,
		Repository repo,
		Session session,
		String fullFilePath)
		throws OperationException
	{
		CleanWorkflowsPropertyFileHelper helper;
		try
		{
			helper = new CleanWorkflowsPropertyFileHelper(fullFilePath);
		}
		catch (IOException ex)
		{
			throw OperationException.createError(ex);
		}
		try
		{
			helper.initConfig(config, repo, session);
		}
		catch (ParseException ex)
		{
			throw OperationException.createError(ex);
		}
	}
}
