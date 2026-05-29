package com.orchestranetworks.ps.admin.devartifacts.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * A user service for exporting of the dev artifacts
 */
public class ExportDevArtifactsUserService<S extends DataspaceEntitySelection>
	extends
	DevArtifactsUserService<S>
{
	private File downloadFile;

	public ExportDevArtifactsUserService(
		ExportDevArtifactsImpl impl,
		DevArtifactsConfigFactory configFactory,
		Map<String, String[]> paramMap)
	{
		super(impl, configFactory, paramMap);
	}

	@Override
	protected void addExtraInputs(
		UserServiceSetupObjectContext<S> aContext,
		BeanDefinition def,
		ObjectKey key)
	{
		super.addExtraInputs(aContext, def, key);

		ExportDevArtifactsConfig exportConfig = (ExportDevArtifactsConfig) config;
		if (exportConfig.isEnableDownloadToLocal())
		{
			Path downloadToLocal = Path.SELF
				.add(Path.parse(ExportDevArtifactsImpl.PARAM_DOWNLOAD_TO_LOCAL));
			defineElement(
				def,
				downloadToLocal,
				"Download to local",
				SchemaTypeName.XS_BOOLEAN,
				Boolean.valueOf(exportConfig.isDownloadToLocal()));
		}
	}

	@Override
	public List<File> getFiles()
	{
		if (downloadFile == null)
		{
			return null;
		}
		List<File> files = new ArrayList<>();
		files.add(downloadFile);
		return files;
	}

	@Override
	protected String getInformation()
	{
		return "Artifacts specified in properties file will be exported, as well as all workflows.";
	}

	@Override
	protected void writeNode(UserServicePaneWriter aWriter, SchemaNode node, boolean top)
	{
		Step lastStep = node.getPathInAdaptation().getLastStep();
		String paramStr = lastStep.format();
		if (DevArtifactsBase.PARAM_ENVIRONMENT_COPY.equals(paramStr)
			|| ExportDevArtifactsImpl.PARAM_DOWNLOAD_TO_LOCAL.equals(paramStr))
		{
			writeCheckBox(aWriter, lastStep.toSelfPath(), paramStr, null);
		}
		else
		{
			super.writeNode(aWriter, node, top);
		}
	}

	@Override
	protected void doExecute(Repository repo, Session session) throws OperationException
	{
		ExportDevArtifactsConfig exportConfig = (ExportDevArtifactsConfig) config;
		File origArtifactsFolder = exportConfig.getArtifactsFolder();
		File origCopyEnvFolder = exportConfig.getCopyEnvironmentFolder();
		String folderNameToZip = null;
		File folderToZip = null;
		File tmpArtifactsFolder = null;
		File tmpCopyEnvFolder = null;

		// If downloading to local, then need to create the folders in the temp dir and
		// set them in the export config rather than the normal folders
		if (exportConfig.isDownloadToLocal())
		{
			if (origArtifactsFolder == null)
			{
				throw OperationException.createError(
					"Must specify an artifacts folder when using Download to Local feature.");
			}
			String tmpArtifactsFolderName = origArtifactsFolder.getName();
			tmpArtifactsFolder = createTemporaryFolder(tmpArtifactsFolderName);
			String tmpCopyEnvFolderName = origCopyEnvFolder.getName();
			tmpCopyEnvFolder = createTemporaryFolder(tmpCopyEnvFolderName);
			if (exportConfig.isEnvironmentCopy())
			{
				folderNameToZip = tmpCopyEnvFolderName;
				folderToZip = tmpCopyEnvFolder;
			}
			else
			{
				folderNameToZip = tmpArtifactsFolderName;
				folderToZip = tmpArtifactsFolder;
			}

			exportConfig.setArtifactsFolder(tmpArtifactsFolder);
			exportConfig.setCopyEnvironmentFolder(tmpCopyEnvFolder);
		}

		// Put this in a try/finally so that if anything happens, the folders can be be set back
		// to their original values. (Probably doesn't matter since the service will be done,
		// but good practice in case configs are used somewhere else down the line by an extension.)
		try
		{
			super.doExecute(repo, session);
			downloadFile = null;
			if (exportConfig.isDownloadToLocal())
			{
				// Zip up the appropriate folder and make that the download file for the link
				List<File> fileList = new ArrayList<>();
				fileList.add(folderToZip);
				try
				{
					downloadFile = zipFiles(fileList, folderNameToZip);
				}
				catch (IOException ex)
				{
					throw OperationException.createError(
						"Error zipping up folder " + folderToZip.getAbsolutePath() + ".",
						ex);
				}
			}
		}
		finally
		{
			if (exportConfig.isDownloadToLocal())
			{
				exportConfig.setArtifactsFolder(origArtifactsFolder);
				exportConfig.setCopyEnvironmentFolder(origCopyEnvFolder);
			}
		}
	}

	private static File createTemporaryFolder(String folderPrefix) throws OperationException
	{
		File tmpFolder;
		try
		{
			tmpFolder = Files.createTempDirectory(folderPrefix).toFile();
		}
		catch (IOException ex)
		{
			throw OperationException
				.createError("Error creating temp folder " + folderPrefix + ".", ex);
		}
		tmpFolder.deleteOnExit();
		return tmpFolder;
	}
}
