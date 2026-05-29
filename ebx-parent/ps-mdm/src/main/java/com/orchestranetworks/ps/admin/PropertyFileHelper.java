/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * A class to help read values from the properties file.
 * It can be subclassed to handle specific properties.
 * 
 * This will operate over properties loaded at construction, and therefore shouldn't be persisted for long-term use
 * if the intention is for updates to the properties file to be reflected in the application. If it is reused,
 * {@link #setProperties(Properties)} should be called to update its properties. But, it's probably just as easy
 * to create a new instance as needed.
 */
public class PropertyFileHelper
{
	// Separates multiple values for the property
	private static final String PROPERTY_VALUE_SEPARATOR = ",";
	// Matches any space character before or after the value separator, but not if separator is escaped
	// i.e. A,B would be interpreted as 2 values A and B, but A\\,B would be interpreted as one value of A,B
	private static final String PROPERTY_VALUE_SEPARATOR_REGEX = "\\s*(?<![\\\\])"
		+ PROPERTY_VALUE_SEPARATOR + "\\s*";
	// Separates multiple tokens within a property value
	private static final String PROPERTY_TOKEN_SEPARATOR = "|";
	// Matches any space character before or after the token separator, but not if separator is escaped
	// i.e. A|B would be interpreted as 2 tokens A and B, but A\\|B would be interpreted as one value of A|B
	private static final String PROPERTY_TOKEN_SEPARATOR_REGEX = "\\s*(?<![\\\\])\\"
		+ PROPERTY_TOKEN_SEPARATOR + "\\s*";

	protected static final int PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME = 0;
	protected static final int PROPERTY_TOKEN_INDEX_DATA_SET_NAME = 1;
	protected static final int PROPERTY_TOKEN_INDEX_TABLE_NAME = 2;
	protected static final int PROPERTY_TOKEN_INDEX_DATA_MODEL_XSD = 2;

	protected Properties props;

	/**
	 * Create the helper with a properties file
	 * 
	 * @param propertiesFile a string representing the path to the properties file
	 * @throws IOException if an error occurred loading the properties file
	 */
	public PropertyFileHelper(String propertiesFile) throws IOException
	{
		this(loadProperties(propertiesFile));
	}

	/**
	 * Create the helper with an already populated properties object
	 * 
	 * @param properties the properties object
	 */
	public PropertyFileHelper(Properties properties) throws IOException
	{
		this.props = properties;
	}

	/**
	 * Get a property and throw an exception if it's not found or has no value
	 * 
	 * @param propertyName the property name
	 * @return the property
	 * @throws IOException if the property couldn't be found or has no value
	 */
	public String getRequiredProperty(String propertyName) throws IOException
	{
		String propertyValue = props.getProperty(propertyName);
		if (propertyValue == null || "".equals(propertyValue))
		{
			throw new IOException("Value must be specified for property " + propertyName + ".");
		}
		return propertyValue;
	}

	/**
	 * Is a property defined
	 * 
	 * @param propertyName the property name
	 * @return if it's defined
	 */
	public boolean isPropertyDefined(String propertyName)
	{
		return props.getProperty(propertyName) != null;
	}

	/**
	 * Get a property as a boolean
	 * 
	 * @param propertyName the property name
	 * @param defaultWhenMissing the default value if the property is missing or has no value defined
	 * @return the boolean value
	 */
	public boolean getBooleanProperty(String propertyName, boolean defaultWhenMissing)
	{
		String booleanValue = props.getProperty(propertyName);
		if (booleanValue == null)
		{
			return defaultWhenMissing;
		}
		return Boolean.valueOf(booleanValue.trim()).booleanValue();
	}

	/**
	 * Get a comma-separated property value as an array of strings.
	 * If the property is missing or has an empty value, an empty array will be returned.
	 * 
	 * @param propertyName the property name
	 * @return an array of strings
	 */
	public String[] getPropertyAsArray(String propertyName)
	{
		String propertyValue = props.getProperty(propertyName);
		if (propertyValue == null || "".equals(propertyValue))
		{
			return new String[0];
		}
		String[] arr = propertyValue.split(PROPERTY_VALUE_SEPARATOR_REGEX);
		return unescapeSeparators(arr, PROPERTY_VALUE_SEPARATOR);
	}

	/**
	 * Split a given pipe-delimited property value into an array of strings
	 * 
	 * @param propertyValue the pipe-delimited property value
	 * @return an array of strings
	 */
	public static String[] getPropertyValueTokens(String propertyValue)
	{
		String[] tokens = propertyValue.split(PROPERTY_TOKEN_SEPARATOR_REGEX);
		return unescapeSeparators(tokens, PROPERTY_TOKEN_SEPARATOR);
	}

	private static String[] unescapeSeparators(String[] arr, String separator)
	{
		String escapedSep = "\\" + separator;
		int len = arr.length;
		String[] unescapedArr = new String[len];
		for (int i = 0; i < len; i++)
		{
			unescapedArr[i] = arr[i].replace(escapedSep, separator);
		}
		return unescapedArr;
	}

	/**
	 * Get the data space represented by the given property value
	 * 
	 * @param propertyValue a string representing the data space name
	 * @param repo the repository
	 * @return the data space, or null if not found
	 */
	public static AdaptationHome getDataSpaceFromProperty(String propertyValue, Repository repo)
	{
		// Split the string since the property may contain other things,
		// but it should at least contain the data space
		String[] tokens = getPropertyValueTokens(propertyValue);
		// Look up the data space represented by the typical index used for data spaces
		return repo.lookupHome(HomeKey.forBranchName(tokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME]));
	}

	/**
	 * Get the data set represented by the given property value
	 * 
	 * @param propertyValue a string representing the data space name and data set name
	 * @param repo the repository
	 * @return the data set, or null if not found
	 */
	public static Adaptation getDataSetFromProperty(String propertyValue, Repository repo)
	{
		// Split the string. It should at least contain the data space and data set.
		String[] tokens = getPropertyValueTokens(propertyValue);
		// Look up the data space represented by the typical index used for data spaces
		AdaptationHome dataSpace = repo
			.lookupHome(HomeKey.forBranchName(tokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME]));
		// Look up the data set represented by the typical index used for data sets,
		// if the data space was found
		return dataSpace == null ? null
			: dataSpace.findAdaptationOrNull(
				AdaptationName.forName(tokens[PROPERTY_TOKEN_INDEX_DATA_SET_NAME]));
	}

	/**
	 * Get the table represented by the given property value
	 * 
	 * @param propertyValue a string representing the data space name, data set name, and table path
	 * @param repo the repository
	 * @return the table, or null if not found
	 */
	public static AdaptationTable getTableFromProperty(String propertyValue, Repository repo)
	{
		// Split the string. It should at least contain the data space, data set, and table.
		String[] tokens = getPropertyValueTokens(propertyValue);
		// Look up the data space represented by the typical index used for data spaces
		AdaptationHome dataSpace = repo
			.lookupHome(HomeKey.forBranchName(tokens[PROPERTY_TOKEN_INDEX_DATA_SPACE_NAME]));
		if (dataSpace == null)
		{
			return null;
		}
		// Look up the data set represented by the typical index used for data sets
		Adaptation dataSet = dataSpace.findAdaptationOrNull(
			AdaptationName.forName(tokens[PROPERTY_TOKEN_INDEX_DATA_SET_NAME]));
		// Look up the table represented by the typical index used for tables,
		// if the data set was found
		return (dataSet == null || dataSet.hasSevereError()) ? null
			: dataSet.getTable(Path.parse(tokens[PROPERTY_TOKEN_INDEX_TABLE_NAME]));
	}

	/**
	 * Get the workflow publications represented by the given property value
	 * 
	 * @param propertyValue a string representing the workflow model name
	 * @param repo the repository
	 * @param session the session
	 * @return the list of publications, or an empty list if none found
	 */
	public static List<PublishedProcess> getWorkflowPublicationsFromProperty(
		String propertyValue,
		Repository repo,
		Session session)
	{
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
		List<PublishedProcess> publishedProcesses = new ArrayList<>();

		// Loop through all published keys
		List<PublishedProcessKey> publishedKeys = wfEngine.getPublishedKeys(false);
		for (PublishedProcessKey publishedKey : publishedKeys)
		{
			// Get the published process for the key, and if its name is equal to
			// the workflow model name we're looking for, then add it to the list.
			PublishedProcess publishedProcess = wfEngine.getPublishedProcess(publishedKey);
			if (propertyValue.equals(publishedProcess.getAdaptationName().getStringName()))
			{
				publishedProcesses.add(publishedProcess);
			}
		}
		return publishedProcesses;
	}

	// Load the properties file into a Properties object
	private static Properties loadProperties(String propertiesFile) throws IOException
	{
		Properties p = new Properties();
		InputStream in = new FileInputStream(propertiesFile);
		try
		{
			p.load(in);
		}
		finally
		{
			in.close();
		}
		return p;
	}

	/**
	 * Get the properties object representing the properties
	 * 
	 * @return the properties object
	 */
	public Properties getProperties()
	{
		return props;
	}

	/**
	 * Set the properties object representing the properties
	 * 
	 * @param properties the properties object
	 */
	public void setProperties(Properties properties)
	{
		this.props = properties;
	}
}
