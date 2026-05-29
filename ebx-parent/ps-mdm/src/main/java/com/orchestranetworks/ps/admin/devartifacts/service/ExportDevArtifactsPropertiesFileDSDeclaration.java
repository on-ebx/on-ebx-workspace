package com.orchestranetworks.ps.admin.devartifacts.service;

import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;

/**
 * A service declaration for Export Dev Artifacts that allows specifying a properties file
 */
public class ExportDevArtifactsPropertiesFileDSDeclaration
	extends
	AbstractExportDevArtifactsDSDeclaration
{
	private String propertiesFileSystemProperty;

	public ExportDevArtifactsPropertiesFileDSDeclaration(String moduleName)
	{
		super(moduleName);
	}

	public ExportDevArtifactsPropertiesFileDSDeclaration(
		String moduleName,
		String serviceKey,
		String title)
	{
		super(moduleName, serviceKey, title);
	}

	@Override
	protected DevArtifactsConfigFactory createConfigFactory()
	{
		return new ExportDevArtifactsPropertiesFileConfigFactory();
	}

	@Override
	protected Map<String, String[]> createParamMap()
	{
		Map<String, String[]> paramMap = new HashMap<>();
		DevArtifactsPropertyFileHelper
			.updateParametersWithPropertiesFile(paramMap, propertiesFileSystemProperty);
		return paramMap;
	}

	/**
	 * @see {@link #setPropertiesFileSystemProperty(String)}
	 */
	public String getPropertiesFileSystemProperty()
	{
		return propertiesFileSystemProperty;
	}

	/**
	 * Set the system property used to specify a properties file.
	 * For example, if this is specified as a JVM parameter:
	 * <code>"-Dmy.dev.artifacts.properties=/opt/ebx/my-dev-artifacts.properties"</code>,
	 * the system property would be <code>"my.dev.artifacts.properties"</code>.
	 * 
	 * @param propertiesFileSystemProperty the system property
	 */
	public void setPropertiesFileSystemProperty(String propertiesFileSystemProperty)
	{
		this.propertiesFileSystemProperty = propertiesFileSystemProperty;
	}
}
