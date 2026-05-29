package com.orchestranetworks.ps.admin.devartifacts;

import java.io.*;
import java.util.*;

import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;

/**
 * @deprecated Use {@link ImportDevArtifactsPropertiesFileConfigFactory} instead
 */
@Deprecated
public class ImportDevArtifactsConfigFactory extends ImportDevArtifactsPropertiesFileConfigFactory
{
	public static List<String> getWorkflowsFromFolder(File folder)
	{
		return DevArtifactsUtil.getWorkflowsFromFolder(folder);
	}

	public static List<String> getArtifactsFromFolder(
		File folder,
		DevArtifactsFilenameFilter filenameFilter)
	{
		return DevArtifactsUtil.getArtifactsFromFolder(folder, filenameFilter);
	}
}
