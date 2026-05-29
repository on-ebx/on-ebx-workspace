/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.io.*;
import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.*;
import com.orchestranetworks.ps.admin.cleanworkflows.CleanWorkflowsConfig.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * A class to help read values from the properties file.
 * <code>useWorkingDataSpace</code> is <code>false</code> if not specified.
 * <code>dataSpaceClosePolicy</code> is {@link DataSpaceClosePolicy#CLOSE} if not specified.
 * All other properties are <code>null</code> or empty lists if not specified.
 */
public class CleanWorkflowsPropertyFileHelper extends PropertyFileHelper
{
	public static final String PROPERTIES_FOLDER_SYSTEM_PROPERTY = "clean.workflows.properties.folder";
	public static final String PROPERTIES_FILE_SYSTEM_PROPERTY = "clean.workflows.properties.file";
	public static final String DEFAULT_PROPERTIES_FOLDER = initDefaultPropertiesFolder();
	public static final String DEFAULT_PROPERTIES_FILE = "clean-workflows.properties";

	private static final String ALL_WORKFLOW_PUBLICATIONS = "all";

	/**
	 * Create the helper
	 *
	 * @param propertiesFile the full path to the properties file to load from
	 * @throws IOException if an i/o error occurs loading the properties
	 */
	public CleanWorkflowsPropertyFileHelper(String propertiesFile) throws IOException
	{
		super(propertiesFile);
	}

	/**
	 * Initialize the configuration from the loaded properties
	 *
	 * @param config the configuration
	 * @param repo the repository
	 * @param session the session
	 *
	 * @throws ParseException if an error occurred parsing a date
	 */
	public void initConfig(CleanWorkflowsConfig config, Repository repo, Session session)
		throws ParseException
	{
		initUseWorkingDataSpace(config, repo);
		initDataSpaceClosePolicy(config, repo);
		initMasterDataSpaces(config, repo);
		initChildDataSpacesToSkip(config, repo);
		initWorkflowPublications(config, repo, session);
		initCreatedBeforeDate(config, repo, session);
		initCreatedBeforeNumOfDays(config, repo, session);
		initIncludeCompleted(config, repo);
		initIncludeActive(config, repo);
	}

	private void initUseWorkingDataSpace(CleanWorkflowsConfig config, Repository repo)
	{
		config.setUseWorkingDataSpace(
			getBooleanProperty(
				CleanWorkflowsPropertyConstants.PROPERTY_USE_WORKING_DATA_SPACE,
				false));
	}

	private void initDataSpaceClosePolicy(CleanWorkflowsConfig config, Repository repo)
	{
		// Get the string from the property file and default to CLOSE if not specified
		String closePolicyStr = props.getProperty(
			CleanWorkflowsPropertyConstants.PROPERTY_DATA_SPACE_CLOSE_POLICY,
			DataSpaceClosePolicy.CLOSE.toString());
		// Convert to the enum and set it on the config
		DataSpaceClosePolicy closePolicy = DataSpaceClosePolicy
			.valueOf(closePolicyStr.toUpperCase());
		config.setDataSpaceClosePolicy(closePolicy);
	}

	// Suppress the deprecation warning for now because we have to use the deprecated
	// constant for backward compatibility, but eventually we can get rid of this suppression
	@SuppressWarnings("deprecation")
	private void initMasterDataSpaces(CleanWorkflowsConfig config, Repository repo)
	{
		String[] dataSpaceValues;
		// For backwards compatibility, we're still supporting the old property constant.
		// So first check if the constant that replaced it is defined. If so, use that
		// but if not, check for the old one.
		// At some point we'll get rid of support for the old one.
		if (isPropertyDefined(CleanWorkflowsPropertyConstants.PROPERTY_MASTER_DATA_SPACES))
		{
			dataSpaceValues = getPropertyAsArray(
				CleanWorkflowsPropertyConstants.PROPERTY_MASTER_DATA_SPACES);
		}
		else
		{
			dataSpaceValues = getPropertyAsArray(
				CleanWorkflowsPropertyConstants.PROPERTY_MASTER_DATA_SPACE);
		}
		ArrayList<AdaptationHome> dataSpaces = new ArrayList<>();
		for (String dataSpaceValue : dataSpaceValues)
		{
			dataSpaces.add(getDataSpaceFromProperty(dataSpaceValue, repo));
		}
		config.setMasterDataSpaces(dataSpaces);
	}

	private void initChildDataSpacesToSkip(CleanWorkflowsConfig config, Repository repo)
	{
		String[] dataSpaceValues = getPropertyAsArray(
			CleanWorkflowsPropertyConstants.PROPERTY_CHILD_DATA_SPACES_TO_SKIP);
		ArrayList<AdaptationHome> dataSpaces = new ArrayList<>();
		for (String dataSpaceValue : dataSpaceValues)
		{
			dataSpaces.add(getDataSpaceFromProperty(dataSpaceValue, repo));
		}
		config.setChildDataSpacesToSkip(dataSpaces);
	}

	private void initWorkflowPublications(
		CleanWorkflowsConfig config,
		Repository repo,
		Session session)
	{
		String[] publicationValues = getPropertyAsArray(
			CleanWorkflowsPropertyConstants.PROPERTY_WORKFLOW_PUBLICATIONS);
		ArrayList<PublishedProcess> publications = new ArrayList<>();
		// The workflow publications could have been specified as "all", in which case
		// we want to find all publications and add them to the list
		if (isAllWorkflowPublications(publicationValues))
		{
			WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
			List<PublishedProcessKey> publishedProcessKeys = wfEngine.getPublishedKeys(false);
			for (PublishedProcessKey publishedProcessKey : publishedProcessKeys)
			{
				publications.add(wfEngine.getPublishedProcess(publishedProcessKey));
			}
		}
		// Otherwise only add the publications specified by the property
		else
		{
			for (String publicationValue : publicationValues)
			{
				List<PublishedProcess> publishedProcesses = getWorkflowPublicationsFromProperty(
					publicationValue,
					repo,
					session);
				for (PublishedProcess publishedProcess : publishedProcesses)
				{
					publications.add(publishedProcess);
				}
			}
		}
		config.setWorkflowPublications(publications);
	}

	private void initCreatedBeforeDate(
		CleanWorkflowsConfig config,
		Repository repo,
		Session session)
		throws ParseException
	{
		String dateStr = props
			.getProperty(CleanWorkflowsPropertyConstants.PROPERTY_CREATED_BEFORE_DATE);
		if (dateStr != null)
		{
			SimpleDateFormat format = new SimpleDateFormat(CommonConstants.EBX_DATE_TIME_FORMAT);
			Date date = format.parse(dateStr);
			config.setCreatedBeforeDate(date);
		}
	}

	private void initCreatedBeforeNumOfDays(
		CleanWorkflowsConfig config,
		Repository repo,
		Session session)
	{
		String numOfDaysStr = props
			.getProperty(CleanWorkflowsPropertyConstants.PROPERTY_CREATED_BEFORE_NUM_OF_DAYS);
		if (numOfDaysStr != null && numOfDaysStr.length() != 0)
		{
			Integer numOfDays = Integer.valueOf(numOfDaysStr);
			config.setCreatedBeforeNumOfDays(numOfDays);
		}
	}

	private void initIncludeCompleted(CleanWorkflowsConfig config, Repository repo)
	{
		config.setIncludeCompleted(
			getBooleanProperty(CleanWorkflowsPropertyConstants.PROPERTY_INCLUDE_COMPLETED, false));
	}

	private void initIncludeActive(CleanWorkflowsConfig config, Repository repo)
	{
		config.setIncludeActive(
			getBooleanProperty(CleanWorkflowsPropertyConstants.PROPERTY_INCLUDE_ACTIVE, false));
	}

	private static boolean isAllWorkflowPublications(String[] publicationValues)
	{
		return publicationValues.length == 1
			&& ALL_WORKFLOW_PUBLICATIONS.equals(publicationValues[0].toLowerCase());
	}

	private static String initDefaultPropertiesFolder()
	{
		String folder = null;
		try
		{
			folder = CommonConstants.getEBXHome();
		}
		catch (IOException ex)
		{
			LoggingCategory.getKernel().error("Error looking up EBX Home.", ex);
		}
		return folder;
	}
}
