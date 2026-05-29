package com.orchestranetworks.ps.messaging;

import java.util.*;

import org.apache.commons.configuration2.*;
import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;

public abstract class MessagingConfiguration implements PropertiesConsumer
{

	public static final String EBX_PROPS = "ebx.properties";

	public final static String MESSAGING_ENABLE = "ebx.messaging.enable";
	public final static String PUBLISH_SUBSCRIBE_CONFIGURATION_CLASS_NAME = "ebx.messaging.configurationclassname";

	public static boolean isMessagingEnabled = false;

	private static final String[] secretProperties = new String[] {
			MessagingConstants.MESSAGING_MQ_PASSWORD_PROPERTY };

	protected static PropertiesFileListener propertiesLoader = PropertiesFileListener
		.getPropertyLoader(getConfigurationFileName(), secretProperties);

	private static Map<String, MessagingClient> messagingClients = new HashMap<>();
	private static final String DEFAULT_SERVER_ID = "DEFAULT";
	private static Configuration configuration = null;

	private String serverId;

	/**
	 * Iterate all client being used and call the shutdown method on each one of them.
	 */
	public static void shutdown()
	{
		if (isMessagingEnabled)
		{
			for (MessagingClient client : messagingClients.values())
			{
				client.shutdown();
			}
		}
	}

	public String getServerId()
	{
		if (StringUtils.isEmpty(serverId))
		{
			this.serverId = DEFAULT_SERVER_ID;
		}
		return serverId;
	}

	public MessagingConfiguration()
	{
		this.serverId = null;
	}

	public MessagingConfiguration(String serverID)
	{
		this.serverId = serverID;
	}

	public abstract String getMessageClientClassName();
	public abstract String getHostURL();
	public abstract String getHostPort();

	public String getFullAddress()
	{
		StringBuilder address = new StringBuilder();
		address.append(getHostURL());
		address.append(":");
		address.append(getHostPort());
		return address.toString();
	}

	public abstract String getUserName();
	public abstract String getPassword();

	public abstract String getInboundDestination();
	public abstract String getInboundMessageWaitingTime();
	//	public abstract String getInboundRoutingKey();

	public abstract String getOutboundDestination();
	//	public abstract String getOutboundRoutingKey();

	public abstract Integer getNumberOfRetries();
	public abstract Long getDelayBetweenRetries();

	public abstract Integer getConnectionTimeout();
	public abstract Boolean toReuseConnection();
	public abstract String getJNDIConnectionFactoryName();

	public abstract Boolean usePrettyPrint();

	public abstract String getDeadLetterDestiantion();

	public static MessagingClient getMessagingClient()
	{
		return getMessagingClient(DEFAULT_SERVER_ID);
	}

	/**
	 * @param serverID - the configuration to be used to get a messaging client for. 
	 * @return
	 */
	public static MessagingClient getMessagingClient(String serverID)
	{
		String serverLookup = StringUtils.isEmpty(serverID) ? DEFAULT_SERVER_ID : serverID;
		String propertyPrefix = getPropertyPrefix(serverID);
		MessagingClient messagingClient = null;
		if (DEFAULT_SERVER_ID.equals(serverID) || isValidServerIDs(serverID))
		{
			messagingClient = messagingClients.get(serverLookup);
		}
		if (messagingClient != null
			&& Objects.equals(configuration, propertiesLoader.getConfiguration()))
		{
			return messagingClient;
		}

		try
		{
			isMessagingEnabled = propertiesLoader.getBooleanProperty(MESSAGING_ENABLE, false);
			if (!isMessagingEnabled)
			{
				return null;
			}

			String configurationClassName = propertiesLoader
				.getProperty(propertyPrefix + PUBLISH_SUBSCRIBE_CONFIGURATION_CLASS_NAME);
			if (StringUtils.isEmpty(configurationClassName))
			{
				throw new RuntimeException(
					"Must specify a valid class name for property : " + propertyPrefix
						+ PUBLISH_SUBSCRIBE_CONFIGURATION_CLASS_NAME);
			}
			MessagingConfiguration configuration = (MessagingConfiguration) com.orchestranetworks.ps.util.ClassLoaderUtils
				.newInstance(
					configurationClassName,
					new Class[] { String.class },
					new Object[] { serverLookup });

			messagingClient = new MessagingClient(configuration);

			String clientClassName = configuration.getMessageClientClassName();
			if (StringUtils.isEmpty(clientClassName))
			{
				throw new RuntimeException("Client Class Name can't be null or empty string.");
			}
			MessagingClientInterface client = (MessagingClientInterface) com.orchestranetworks.ps.util.ClassLoaderUtils
				.newInstance(
					clientClassName,
					new Class[] { MessagingConfiguration.class },
					new Object[] { configuration });

			messagingClient.setClient(client);
			messagingClients.put(serverLookup, messagingClient);

			return messagingClient;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}

	public String getPropertyPrefix()
	{
		if (StringUtils.isEmpty(serverId) || DEFAULT_SERVER_ID.equals(serverId))
		{
			return "";
		}
		return serverId + ".";
	}

	public static String getPropertyPrefix(String serverID)
	{
		if (StringUtils.isEmpty(serverID) || DEFAULT_SERVER_ID.equals(serverID))
		{
			return "";
		}
		return serverID + ".";
	}

	private static boolean isValidServerIDs(String serverID)
	{
		String[] servers = propertiesLoader
			.getStringArrayProperty(MessagingConstants.MESSAGING_SERVERS_PROPERTY_NAME);
		if (servers == null)
		{
			return false;
		}
		for (int i = 0; i < servers.length; i++)
		{
			if (servers[i] != null && servers[i].equals(serverID))
			{
				return true;
			}
		}
		return false;
	}

	public static String getConfigurationFileName()
	{
		String propPath = System
			.getProperty(MessagingConstants.MESSAGING_PROPERTY_FILE_SYSTEM_PROPERTY);
		if (StringUtils.isEmpty(propPath))
		{
			propPath = System.getProperty(CommonConstants.EBX_PROPERTIES_SYSTEM_PROPERTY_NAME);
		}
		return propPath;
	}

	public PropertiesFileListener getPropertiesLoader()
	{
		return propertiesLoader;
	}

}
