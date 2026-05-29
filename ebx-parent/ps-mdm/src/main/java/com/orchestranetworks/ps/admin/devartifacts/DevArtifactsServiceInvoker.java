package com.orchestranetworks.ps.admin.devartifacts;

import java.io.*;
import java.net.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;

/**
 * @deprecated Use {@link com.orchestranetworks.ps.admin.devartifacts.client.BasicAuthDevArtifactsRESTClient} instead
 */
@Deprecated
public class DevArtifactsServiceInvoker
{
	private String servletURL;
	private String login;
	private String password;
	private String propertiesFileSystemProperty;
	private boolean copyEnvironment;
	private boolean replaceMode;
	private boolean skipNonExistingFiles;
	private boolean publishWorkflowModels;
	private String[] workflowModels = new String[0];

	/**
	 * Construct the invoker
	 * 
	 * @param servletURL the url of the Dev Artifacts Service servlet
	 * @param login the username to log in as
	 * @param password the password
	 */
	public DevArtifactsServiceInvoker(String servletURL, String login, String password)
	{
		this.servletURL = servletURL;
		this.login = login;
		this.password = password;
	}

	public String getPropertiesFileSystemProperty()
	{
		return propertiesFileSystemProperty;
	}

	public void setPropertiesFileSystemProperty(String propertiesFileSystemProperty)
	{
		this.propertiesFileSystemProperty = propertiesFileSystemProperty;
	}

	public boolean isCopyEnvironment()
	{
		return this.copyEnvironment;
	}

	public void setCopyEnvironment(boolean copyEnvironment)
	{
		this.copyEnvironment = copyEnvironment;
	}

	public boolean isReplaceMode()
	{
		return this.replaceMode;
	}

	public void setReplaceMode(boolean replaceMode)
	{
		this.replaceMode = replaceMode;
	}

	public boolean isSkipNonExistingFiles()
	{
		return this.skipNonExistingFiles;
	}

	public void setSkipNonExistingFiles(boolean skipNonExistingFiles)
	{
		this.skipNonExistingFiles = skipNonExistingFiles;
	}

	public boolean isPublishWorkflowModels()
	{
		return publishWorkflowModels;
	}

	public void setPublishWorkflowModels(boolean publishWorkflowModels)
	{
		this.publishWorkflowModels = publishWorkflowModels;
	}

	public String[] getWorkflowModels()
	{
		return this.workflowModels;
	}

	public void setWorkflowModels(String[] workflowModels)
	{
		this.workflowModels = workflowModels;
	}

	/**
	 * Execute the servlet
	 * 
	 * @return the input stream from the servlet connection
	 * @throws IOException if an exception occurred
	 */
	public InputStream execute() throws IOException
	{
		// Construct the url to the servlet
		String urlStr = servletURL + "?";
		if (propertiesFileSystemProperty != null)
		{
			urlStr += ("&" + DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE_SYSTEM_PROPERTY
				+ "=" + propertiesFileSystemProperty);
		}
		if (copyEnvironment)
		{
			urlStr += ("&" + DevArtifactsBase.PARAM_ENVIRONMENT_COPY + "=true");
		}
		if (replaceMode)
		{
			urlStr += ("&" + ImportDevArtifactsImpl.PARAM_REPLACE_MODE + "=true");
		}
		if (skipNonExistingFiles)
		{
			urlStr += ("&" + ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES + "=true");
		}
		if (publishWorkflowModels)
		{
			urlStr += ("&" + ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS + "=true");
		}

		for (String workflowModel : workflowModels)
		{
			urlStr += ("&" + ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX + workflowModel
				+ "=true");
		}
		// Open a connection to the servlet and return its input stream
		URL url = new URL(urlStr);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
		writer.write(login + " " + password);
		writer.newLine();
		writer.flush();
		writer.close();
		return conn.getInputStream();
	}
}
