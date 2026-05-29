package com.orchestranetworks.ps.messaging.beans.configuration;

import com.orchestranetworks.ps.ui.annotations.*;

public class HostMessagingConfiguration extends MessagingConfiguration
{
	@Property(propertyName = "ebx.messaging.host", 
		label = "Host Name", 
		dataType = "string", 
		description = "Messaging server host name.", 
		order = 20)
	public String hostName;

	@Property(propertyName = "ebx.messaging.port", 
		label = "Host Port Number", 
		dataType = "integer", 
		description = "Messaging server host port number.", 
		order = 21)
	
	public String hostPortNumber;
		
//	
//	@Property(propertyName = "ebx.directory.sso.idp.certificate.file", 
//		label = "Certificate File", 
//		dataType = "string", 
//		description = "If the property @ebx.directory.sso.idp.isjksfile is set to true, this should be the path to a JKS file otherwise a path to a certificate file.\r\n" + 
//			"This needs to be a path that exist on the SERVER. \r\n" +
//			"Java includes a tool to load certificate files into a JKS file.\r\n" + 
//			"from the java bin folder you can execute the following command:\r\n" + 
//			"keytool -import -alias {an alias name} -keystore [path to a JKS file \r\n" + 
//			"e.g. Java runtime /.../jdk1.8/jre/lib/security/cacerts -file [path to a certificate file e.g. /home/gugrim/tmp/sunas.der]\r\n" + 
//			"\r\n" + 
//			"useful command to check the certificates stored on a file\r\n" + 
//			"keytool -v -list -keystore .keystorekeytool -v -list -keystore [path to the file that contain certificates]", 
//		order = 16)
//	
//	public String certificateFile;
//
//	@Property(propertyName = "ebx.directory.sso.sp.issuerid", 
//		label = "EBX (SP) Issuer ID", 
//		dataType = "string", 
//		description = 	"This is the Service Provider issuer ID. \r\n" +
//						"It is a value representing EBX. this value needs to be set on the IDP server\r\n"+
//						"(application configuration) and here with the same value\r\n" + 
//						"The IDP server uses this value to verify that EBX is sending the authentication request and not someone else. ",
//		order = 17)
//	
//	public String spIssuerId;
//
//	@Property(propertyName = "ebx.directory.sso.saml.user.email", 
//		label = "User Email Attribute Name", 
//		dataType = "string", 
//		description = 	"Part of the SAML response will have user information in it. in the form of attributes in the assertion portion.\r\n" + 
//						"This property tell EBX SSO Integration what is the property name for the user email.", 
//		order = 18)
//	
//	public String userEmail;
//
//	@Property(propertyName = "ebx.directory.sso.saml.user.firstname", 
//		label = "User First Name Attribute Name", 
//		dataType = "string", 
//		description = 	"Part of the SAML response will have user information in it. in the form of attributes in the assertion portion.\r\n" + 
//						"This property tell EBX SSO Integration what is the property name for the user first name.", 
//		order = 19)
//	
//	public String userFirstName;
//
//	@Property(propertyName = "ebx.directory.sso.saml.user.lastname", 
//		label = "User Last Name Attribute Name", 
//		dataType = "string", 
//		description = 	"Part of the SAML response will have user information in it. in the form of attributes in the assertion portion.\r\n" + 
//						"This property tell EBX SSO Integration what is the property name for the user last name.", 
//		order = 20)
//	
//	public String userTastName;
//	
//	@Property(propertyName = "ebx.directory.sso.useroles", 
//		label = "Enable User Roles", 
//		dataType = "string", 
//		description = "Enable User Roles Support - Authorization", 
//		possibleValuesClassName = "com.orchestranetworks.ps.ui.annotations.beans.possiblevalues.YesNoPossibleValues", 
//		order = 21)
//	
//	public String isUserRoles;
//	
//	
//	@Property(propertyName = "ebx.directory.sso.saml.user.roles", 
//		label = "User Roles Attribute Name", 
//		dataType = "string", 
//		description = 	"Part of the SAML response will have user information in it. in the form of attributes in the assertion portion.\r\n" + 
//						"This property tell EBX SSO Integration what is the property name for the user roles/groups.", 
//		order = 22)
//	
//	public String userRoles;
//
//	@Property(propertyName = "ebx.directory.sso.idp.nameid.policy", 
//		label = "Namig Policy", 
//		dataType = "string", 
//		description = "this defines the policy for storing user info. for SAML 2 \r\n" + 
//					  "Default is \"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified\" ", 
//		defaultValue = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
//		order = 23)
//	
//	public String namingPolicy;		
	
}
