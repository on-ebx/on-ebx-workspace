package com.orchestranetworks.ps.messaging.beans.configuration;

import com.orchestranetworks.ps.ui.annotations.*;

@ConfigurationType(systemPropertyName = "ebx.messaging.properties", 
	serviceDescription = "A service to configure the properties required for JNDI Messaging integration.\r\n", 
	serviceKey = "JNDIMessagingConfigurationKey", 
	nameInMenu = "JNDI Messaging Configuration")
public class JNDIMessagingConfiguration extends MessagingConfiguration
{
	@Property(propertyName = "ebx.messaging.host", 
		label = "JNDI Host Name", 
		dataType = "string", 
		description = "JNDI Messaging server host name.", 
		order = 20)
	public String hostName;

	@Property(propertyName = "ebx.messaging.port", 
		label = "JNDI Host Port Number", 
		dataType = "integer", 
		description = "JNDI Messaging server host port number.", 
		order = 21)
	
	public String hostPortNumber;
		
	@Property(propertyName = "ebx.messaging.connection.jndifactoryname", 
		label = "Certificate Alias in the JKS File", 
		dataType = "string", 
		description = "Specify the connection factory jndi name\r\n" + 
			"with tomcat the ConnectionFactory name needs to be configured in tomact context.xml. and the Connection Factory name needs to be prefixed with \"java:/comp/env/\". \r\n" + 
			"for example if your connection factory name is \"jms/ConnectionFactory\" then the value need to be \"java:/comp/env/jms/ConnectionFactory\"",  
		order = 22)
	public String jndifactoryname;
	
}
