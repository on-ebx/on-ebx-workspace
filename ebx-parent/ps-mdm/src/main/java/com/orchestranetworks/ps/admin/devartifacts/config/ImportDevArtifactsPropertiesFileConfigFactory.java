package com.orchestranetworks.ps.admin.devartifacts.config;

import java.io.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * A factory for creating a {@link ImportDevArtifactConfig} from a properties file
 */
public class ImportDevArtifactsPropertiesFileConfigFactory
	extends
	AbstractImportDevArtifactsConfigFactory
{
	protected void initConfig(
		DevArtifactsConfig config,
		Repository repo,
		Session session,
		Map<String, String[]> paramMap)
		throws OperationException
	{
		try
		{
			DevArtifactsPropertyFileHelper helper = new DevArtifactsPropertyFileHelper(paramMap);
			helper.initConfig(config, repo, session);
		}
		catch (IOException ex)
		{
			throw OperationException.createError(ex);
		}
	}
}
