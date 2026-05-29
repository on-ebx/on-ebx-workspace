package com.orchestranetworks.ps.util;

import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.nio.file.WatchEvent.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.*;
import org.apache.commons.configuration2.builder.fluent.*;
import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.security.*;
import com.orchestranetworks.service.*;

/**
 * @author David Dahan
 * This class loads a property file into a property object and monitor for any changes to the file.
 * It will reload the properties in case of a change.
 * The properties can be accessed by calling getProperties() on the instance; 
 * Caller must obtain a reference to the loader object or access the loader via getPropertyLoader(filePath) which will return the loader associated with a file path.
 * this feature is useful when multiple parts of a solution are using the same file e.g. ebx.properties it will prevent loading and spinning threads for the same file path.
 * To check if a properties object is new, one can keep a reference to the properties and compare them to the properties object returned from getProperties().
 * Each time the properties file is getting updated this class will reload them into a new property object.  
 *   
 */
public class PropertiesFileListener implements FileListener
{
	public static final String EBX_PROPS = "ebx.properties";
	public static final String DIRECTORY_PASSWORDS_ENCRYPTED = "ebx.directory.passwordencrypted";
	public static final String DEFAULT_DELIMITER = ",";

	private Set<String> secretProperties = new HashSet<>();

	private static Map<String, PropertiesFileListener> propertyFiles = new ConcurrentHashMap<>();

	//	private Properties combinedProperties = new Properties();
	//	private Properties storedProperties = new Properties();
	private String propPath;
	private FileWatcher watcher = new FileWatcher();
	private List<PropertiesConsumer> consumers = new ArrayList<>();
	private FileBasedConfiguration configuration;

	/**
	 * 
	 * @param filePath - the path to the property file to load and monitor for changes
	 *
	 * @return an existing loader if the path was already used or a new one setup with the filePath
	 */
	public static PropertiesFileListener getPropertyLoader(String filePath)
	{
		if (filePath == null)
		{
			throw new NullPointerException("filePath can't be null.");
		}
		String absolutePath = getCanonicalPath(filePath);
		PropertiesFileListener loader = propertyFiles.get(absolutePath);
		if (loader == null)
		{
			loader = new PropertiesFileListener(absolutePath);
			propertyFiles.put(absolutePath, loader);
		}
		return loader;
	}

	/**
	 * 
	 * @param filePath - the path to the property file to load and monitor for changes
	 *
	 * @return an existing loader if the path was already used or a new one setup with the filePath
	 */
	public static PropertiesFileListener getPropertyLoader(
		String filePath,
		String[] secretProperties)
	{
		PropertiesFileListener loader = getPropertyLoader(filePath);
		loader.setSecretProperties(secretProperties);
		return loader;
	}

	/**
	 * 
	 * @param filePath - the path to the property file to load and monitor for changes
	 *
	 * @return an existing loader if the path was already used or a new one setup with the filePath
	 */
	public static PropertiesFileListener getPropertyLoader(
		String filePath,
		String[] secretProperties,
		PropertiesConsumer consumer)
	{
		PropertiesFileListener loader = getPropertyLoader(filePath);
		loader.setSecretProperties(secretProperties);
		loader.setConsumer(consumer);
		return loader;
	}

	private static String getCanonicalPath(String path)
	{
		File file = new File(path);
		if (!file.isFile())
		{
			throw new RuntimeException("File path " + path + " must be a file");
		}
		try
		{
			return file.getCanonicalPath();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean hasPropertiesForPath(String filePath)
	{
		String absolutePath = getCanonicalPath(filePath);
		return propertyFiles.containsKey(absolutePath);
	}

	private PropertiesFileListener(String propertyFileName)
	{
		propPath = propertyFileName;
		try
		{
			loadProperties();
			registerForChanges();
			logProperties();

		}
		catch (Exception ex)
		{
			if (LoggingCategory.getKernel().isInfo())
				LoggingCategory.getKernel().error("Error loading properties", ex);
		}
	}

	public void saveStoredProperties()
	{
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
			String dateTiem = dateFormat.format(new Date());
			Path source = Paths.get(propPath);
			Path target = Paths.get(propPath + "-" + dateTiem + ".backup");

			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			FileWriter writer = new FileWriter(propPath);
			configuration.write(writer);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	private void loadProperties()
	{
		LoggingCategory logger = LoggingCategory.getKernel();
		FileInputStream is = null;
		try
		{
			Parameters params = new Parameters();
			FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
				PropertiesConfiguration.class)
					.configure(params.properties().setFileName(propPath).setEncoding("UTF-8"));

			this.configuration = builder.getConfiguration();

			//			is = new FileInputStream(propPath);
			//			PropertiesConfiguration config = new PropertiesConfiguration();
			//			PropertiesConfigurationLayout layout = new PropertiesConfigurationLayout();
			//			layout.load(config, new InputStreamReader(is));
			//
			//			//			Properties properties = new Properties();
			//			//			properties.load(is);
			//
			//			this.configuration = config;

		}
		catch (Exception e)
		{
			if (logger.isDebug())
				logger.error("Exception: " + e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					if (logger.isDebug())
						logger.error("Exception: " + e);
				}
			}
		}

	}

	//	private void loadProperties()
	//	{
	//		Properties env = System.getProperties();
	//		FileInputStream is = null;
	//		try
	//		{
	//			is = new FileInputStream(propPath);
	//			Properties properties = new Properties();
	//			properties.load(is);
	//
	//			is = new FileInputStream(propPath);
	//			storedProperties = new Properties();
	//			storedProperties.load(is);
	//
	//			properties.putAll(env);
	//
	//			this.combinedProperties = properties;
	//
	//		}
	//		catch (Exception e)
	//		{
	//			LoggingCategory.getKernel().debug(e);
	//			throw new RuntimeException(e);
	//		}
	//		finally
	//		{
	//			if (is != null)
	//			{
	//				try
	//				{
	//					is.close();
	//				}
	//				catch (IOException e)
	//				{
	//					LoggingCategory.getKernel().debug(e);
	//				}
	//			}
	//		}
	//
	//	}
	//	

	private void logProperties()
	{
		LoggingCategory logger = LoggingCategory.getKernel();
		if (configuration != null)
		{
			logger.info("Properties from : - " + propPath);
			logger.info("==================================================================");
			Iterator<String> keys = configuration.getKeys();
			for (Iterator<String> key = keys; keys.hasNext();)
			{
				String keyValue = key.next();
				if (secretProperties.contains(keyValue))
				{
					logger.info(keyValue + " ==> ### Hidden value ###");
				}
				else
				{
					logger.info(keyValue + " ==> " + configuration.getProperty(keyValue));
				}
			}
		}

	}

	private void registerForChanges() throws Exception
	{
		watcher.setListener(this);
		watcher.start();
	}

	@Override
	public void processEvent(Kind<?> kind)
	{
		if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind))
		{
			loadProperties();
			logProperties();
			for (PropertiesConsumer propertiesConsumer : consumers)
			{
				propertiesConsumer.processProperies();
			}
		}
	}

	@Override
	public String getFileName()
	{
		return propPath;
	}

	private void setSecretProperties(String[] newSecretProperties)
	{
		Collections.addAll(secretProperties, newSecretProperties);
	}

	public void setConsumer(PropertiesConsumer consumer)
	{
		if (!consumers.contains(consumer))
		{
			consumers.add(consumer);
		}
	}

	public String getProperty(String propertyName, String defaultValue)
	{
		String stringValue = getProperty(propertyName);
		if (stringValue == null)
		{
			return defaultValue;
		}
		return stringValue;
	}

	public Integer getIntegerProperty(String propertyName, Integer defaultInteger)
	{
		Integer integerValue = getIntegerProperty(propertyName);
		if (integerValue == null)
		{
			return defaultInteger;
		}
		return integerValue;
	}

	public Integer getIntegerProperty(String propertyName)
	{
		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value))
		{
			return null;
		}
		else
		{
			try
			{
				return Integer.valueOf(value.trim());
			}
			catch (NumberFormatException ex)
			{
				LoggingCategory.getKernel()
					.error("Property: " + propertyName + " expecting an Integer value.", ex);
				return null;
			}
		}
	}

	public Long getLongProperty(String propertyName, Long defaultLong)
	{
		Long longValue = getLongProperty(propertyName);
		if (longValue == null)
		{
			return defaultLong;
		}
		return longValue;
	}

	public Long getLongProperty(String propertyName)
	{
		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value))
		{
			return null;
		}
		else
		{
			try
			{
				return Long.valueOf(value.trim());
			}
			catch (NumberFormatException ex)
			{
				LoggingCategory.getKernel()
					.error("Property: " + propertyName + " expecting a Long value.", ex);
				return null;
			}
		}
	}

	public BigDecimal getDecimalProperty(String propertyName)
	{
		Long longValue = getLongProperty(propertyName);
		if (longValue == null)
		{
			return null;
		}
		else
		{
			try
			{

				return BigDecimal.valueOf(longValue);
			}
			catch (NumberFormatException ex)
			{
				LoggingCategory.getKernel()
					.error("Property: " + propertyName + " expecting a Long/Decimal value.", ex);
				return null;
			}
		}
	}

	public Boolean getBooleanProperty(String propertyName, boolean defaultBoolean)
	{
		Boolean booleanValue = getBooleanProperty(propertyName);
		if (booleanValue == null)
		{
			return defaultBoolean;
		}
		return booleanValue;
	}

	public Boolean getBooleanProperty(String propertyName)
	{
		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value))
		{
			return null;
		}
		else
		{
			return Boolean.valueOf(value.trim());
		}
	}

	public Date getDateProperty(String propertyName, Date defaultDate)
	{
		Date date = getDateProperty(propertyName);
		if (date == null)
		{
			return defaultDate;
		}
		return date;
	}

	public Date getDateProperty(String propertyName)
	{
		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value))
		{
			return null;
		}
		else
		{
			SimpleDateFormat format = new SimpleDateFormat();
			try
			{
				return format.parse(value);
			}
			catch (ParseException ex)
			{
				LoggingCategory.getKernel()
					.error(
						"Property: " + propertyName + " expecting a Date value in the format of "
							+ format.toLocalizedPattern(),
						ex);
				return null;
			}
		}
	}

	public String getProperty(String propertyName)
	{
		String systemValue = System.getProperty(propertyName);
		if (!StringUtils.isEmpty(systemValue))
		{
			return systemValue;
		}
		String value = configuration.getString(propertyName);
		if (Strings.CI.contains(value, "${"))
		{
			String beforeValue = StringUtils.substringBefore(value, "${");
			String afterValue = StringUtils.substringAfter(value, "}");
			String propertyInValue = StringUtils.substringBetween(value, "${", "}");
			String inValue = getProperty(propertyInValue);
			if (StringUtils.isEmpty(inValue))
			{
				LoggingCategory.getKernel().warn("Property " + propertyInValue + " have no value.");
			}
			return beforeValue + inValue + afterValue;
		}
		return value;
	}

	public boolean isPasswordsEncrypted()
	{
		return getBooleanProperty(DIRECTORY_PASSWORDS_ENCRYPTED, false);
	}

	public String getDecryptValue(String propertyName)
	{
		String originalValue = getProperty(propertyName);
		if (StringUtils.isEmpty(originalValue))
		{
			return null;
		}
		if (isPasswordsEncrypted())
		{
			return EncryptionTool.decryptFromBase64(originalValue);
		}
		else
		{
			return originalValue;
		}
	}

	public String[] getStringArrayProperty(String propertyName)
	{
		return getStringArrayProperty(propertyName, DEFAULT_DELIMITER);
	}

	public String[] getStringArrayProperty(String propertyName, String delimiter)
	{
		String value = getProperty(propertyName);
		if (StringUtils.isEmpty(value))
		{
			return null;
		}
		return value.split(delimiter);
	}
}
