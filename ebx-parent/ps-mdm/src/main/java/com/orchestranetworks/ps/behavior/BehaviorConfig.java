/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.behavior;

import java.util.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.service.*;

/**
 */
public abstract class BehaviorConfig
{
	public static final String MULTIPLE_VALUES_SEPARATOR = ";";

	private Map<String, BehaviorConfig> includedConfigMap = new HashMap<>();
	private Properties props = new Properties();
	private boolean valid;

	protected abstract void initIncludedConfigs(Map<String, BehaviorConfig> includedConfigs);

	protected abstract void initProperties(Properties properties);

	public Map<String, BehaviorConfig> getIncludedConfigs()
	{
		return includedConfigMap;
	}

	public Properties getProperties()
	{
		return props;
	}

	public final boolean isValid()
	{
		return valid;
	}

	protected Set<String> getRequiredIncludedConfigKeys()
	{
		return new HashSet<>();
	}

	protected Set<String> getRequiredPropertyKeys()
	{
		return new HashSet<>();
	}

	protected boolean validate()
	{
		boolean v = true;
		for (String key : getRequiredIncludedConfigKeys())
		{
			if (!includedConfigMap.containsKey(key))
			{
				logError("Must specify a Behavior Config for key " + key + ".");
				v = false;
			}
		}

		for (String key : includedConfigMap.keySet())
		{
			if (!includedConfigMap.get(key).validate())
			{
				v = false;
			}
		}

		for (String key : getRequiredPropertyKeys())
		{
			if (props.getProperty(key) == null)
			{
				logError("Must specify " + key + " property.");
				v = false;
			}
		}
		return v;
	}

	/**
	 * Joins values for a multiple-value property together using the default separator
	 *
	 * @param values the values
	 * @return the joined string
	 */
	protected String joinMultipleValues(String... values)
	{
		return StringUtils.join(values, MULTIPLE_VALUES_SEPARATOR);
	}

	protected final void logError(String message)
	{
		LoggingCategory.getKernel().error(this.getClass().getName() + ": " + message);
	}

	protected BehaviorConfig()
	{
		initIncludedConfigs(includedConfigMap);
		for (String includedConfigKey : includedConfigMap.keySet())
		{
			BehaviorConfig includedConfig = includedConfigMap.get(includedConfigKey);
			props.putAll(includedConfig.getProperties());
		}
		initProperties(props);
		valid = validate();
	}
}
