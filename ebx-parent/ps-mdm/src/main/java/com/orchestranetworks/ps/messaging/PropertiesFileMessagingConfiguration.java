package com.orchestranetworks.ps.messaging;

import java.io.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;

public class PropertiesFileMessagingConfiguration extends MessagingConfiguration
	implements PropertiesConsumer
{

	private Boolean toReuseConnection = null;
	private Boolean usePrettyPrint = null;

	private static final String[] secretProperties = new String[] {
			MessagingConstants.MESSAGING_MQ_PASSWORD_PROPERTY };

	protected PropertiesFileListener propertiesLoader = PropertiesFileListener
		.getPropertyLoader(getConfigurationFileName(), secretProperties, this);

	public PropertiesFileMessagingConfiguration(String serverID) throws IOException
	{
		super(serverID);
		String propPath = System
			.getProperty(MessagingConstants.MESSAGING_PROPERTY_FILE_SYSTEM_PROPERTY);
		if (StringUtils.isEmpty(propPath))
		{
			propPath = System.getProperty(CommonConstants.EBX_PROPERTIES_SYSTEM_PROPERTY_NAME);
		}
	}

	@Override
	public String getMessageClientClassName()
	{
		return propertiesLoader
			.getProperty(getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CLINET_CLASS_NAME);
	}

	@Override
	public String getHostURL()
	{
		return propertiesLoader
			.getProperty(getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_HOST_PROPERTY);
	}

	@Override
	public String getHostPort()
	{
		return propertiesLoader
			.getProperty(getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_PORT_PROPERTY);
	}

	@Override
	public String getOutboundDestination()
	{
		return propertiesLoader.getProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_OUTBOUND_QUEUE_NAME_PROPERTY);
	}

	@Override
	public String getUserName()
	{
		return propertiesLoader
			.getProperty(getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_USERNAME_PROPERTY);
	}

	@Override
	public String getPassword()
	{
		return propertiesLoader
			.getProperty(getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_PASSWORD_PROPERTY);
	}

	@Override
	public String getInboundMessageWaitingTime()
	{
		return propertiesLoader.getProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_INBOUND_WAIT_TIME_PROPERTY,
			"1000");
	}

	@Override
	public Integer getNumberOfRetries()
	{
		return propertiesLoader.getIntegerProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CONNECTION_RETRIES,
			3);
	}

	@Override
	public Long getDelayBetweenRetries()
	{
		return propertiesLoader.getLongProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CONNECTION_RETRIES_DELAY,
			1000l);
	}

	@Override
	public Integer getConnectionTimeout()
	{
		return propertiesLoader.getIntegerProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CONNECTION_TIMEOUT_PROPERTY,
			6000);
	}

	@Override
	public String getDeadLetterDestiantion()
	{
		return propertiesLoader.getProperty(
			getPropertyPrefix()
				+ MessagingConstants.MESSAGING_MQ_DEAD_LETTER_DESTINATION_NAME_PROPERTY);
	}

	@Override
	public Boolean toReuseConnection()
	{
		if (toReuseConnection == null)
		{
			toReuseConnection = propertiesLoader.getBooleanProperty(
				getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CONNECTION_REUSE);
		}

		return this.toReuseConnection;
	}

	@Override
	public Boolean usePrettyPrint()
	{
		if (usePrettyPrint == null)
		{
			usePrettyPrint = propertiesLoader.getBooleanProperty(
				getPropertyPrefix()
					+ MessagingConstants.MESSAGING_MQ_OUTBOUND_USEPRETTYPRINT_PROPERTY,
				false);
		}

		return this.usePrettyPrint;
	}

	//	@Override
	@Override
	public PropertiesFileListener getPropertiesLoader()
	{
		return propertiesLoader;
	}

	@Override
	public String getInboundDestination()
	{
		return propertiesLoader.getProperty(
			getPropertyPrefix()
				+ MessagingConstants.MESSAGING_MQ_INBOUND_DESTINATION_NAME_PROPERTY);
	}

	@Override
	public void processProperies()
	{

	}

	@Override
	public String getJNDIConnectionFactoryName()
	{
		return propertiesLoader.getProperty(
			getPropertyPrefix() + MessagingConstants.MESSAGING_MQ_CONNECTION_JNDI_FACTORY_NAME);
	}

}
