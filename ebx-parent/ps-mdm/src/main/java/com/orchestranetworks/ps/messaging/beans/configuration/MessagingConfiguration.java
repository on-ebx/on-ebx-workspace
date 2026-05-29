package com.orchestranetworks.ps.messaging.beans.configuration;

import com.orchestranetworks.ps.ui.annotations.*;

public class MessagingConfiguration
{
	@Property(propertyName = "ebx.messaging.enable", 
		label = "Enable Messaing", 
		dataType = "string", 
		description = "Enable messagin framework.", 
		possibleValuesClassName = "com.orchestranetworks.ps.ui.annotations.beans.possiblevalues.YesNoPossibleValues",
		order = 1)
	public String messagingEnabled;

	@Property(propertyName = "ebx.messaging.configurationclassname", 
		label = "Messaging Configuration Class name", 
		dataType = "string", 
		description = "Specify the Messaging Configuration CLass name to be used.", 
		order = 2)
	public String messagingConfigurationClassName;

	@Property(propertyName = "ebx.messaging.clientclassname", 
		label = "Messaging Client Class name", 
		dataType = "string", 
		description = "Specify the Messaging Client CLass name to be used.", 
		order = 3)
	public String messagingClientClassName;
	
	@Property(propertyName = "ebx.messaging.username", 
		label = "User Name", 
		dataType = "string", 
		description = "User name to use to connect to the messaging server", 
		order = 6)
	
	public String userName;

	@Property(propertyName = "ebx.messaging.password", 
		label = "Password", 
		dataType = "password", 
		description = "Password to be used to connect to the messaging server.", 
		order = 7)
	public String password;

	@Property(propertyName = "tibco.ebx.messaging.connection.timeout", 
		label = "Connection Timeout", 
		dataType = "string", 
		description = "Connection Timeout.",  
		order = 8)
	public String connectionTimeout;
	
	@Property(propertyName = "tibco.ebx.messaging.connection.reuse", 
		label = "Reuse connection", 
		dataType = "string", 
		description = "Reuse same connection for all transactions.",  
		possibleValuesClassName = "com.orchestranetworks.ps.ui.annotations.beans.possiblevalues.YesNoPossibleValues",
		order = 9)
	public String connectionReuse;
	
	
	@Property(propertyName = "ebx.messaging.outbound.destinationname", 
		label = "Outbound Destination", 
		dataType = "string", 
		description = "A default destination (Topic/Queue) to use for sending/publishing messages.",  
		order = 10)
	public String outboundDestinationname;

	@Property(propertyName = "ebx.messaging.inbound.destinationname", 
		label = "Inbound Destination", 
		dataType = "string", 
		description = "A default destination (Topic/Queue) to use for reading/Subscribing.",  
		order = 11)
	public String inboundDestinationname;
	
}
