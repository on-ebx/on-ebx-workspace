package com.orchestranetworks.ps.messaging;

public abstract class AbstractMessagingClientInterface implements MessagingClientInterface
{
	private MessagingConfiguration messagingConfiguration;

	public AbstractMessagingClientInterface(MessagingConfiguration messagingConfiguration)
	{
		this.messagingConfiguration = messagingConfiguration;
	}

	public MessagingConfiguration getMessagingConfiguration()
	{
		return messagingConfiguration;
	}

}
