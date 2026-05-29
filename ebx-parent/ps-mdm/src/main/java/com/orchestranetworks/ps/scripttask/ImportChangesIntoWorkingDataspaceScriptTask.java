package com.orchestranetworks.ps.scripttask;

import java.io.*;
import java.nio.file.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * Import Changes from an Archive File Into Working Dataspace.
 * <pre>{@code
		<bean className="com.orchestranetworks.ps.scripttask.ImportChangesIntoWorkingDataspaceScriptTask">
			<documentation xml:lang="en-US">
				<label>Import Changes from an Archive File Into Working Dataspace.</label>
				<description>
				    Import Changes from an Archive File Into Working Dataspace, where the file is stored in a file location from the data context.
				</description>
			</documentation>
			<properties>
				<property name="workingDataSpace" input="true">
				    <documentation xml:lang="en-US">
						<label>Working Data space</label>
						<description>
						   Working Dataspace for this workflow
						</description>
				    </documentation>
				</property>
				<property name="dataset" input="true">
				    <documentation xml:lang="en-US">
						<label>Data set</label>
						<description>
						    Data Set
						</description>
				    </documentation>
				</property>
				<property name="exportFileLocationInfo" input="true">
					<documentation xml:lang="en-US">
						<label>Export File Location Info</label>
						<description>
							the file location within the default directory 
						</description>
				    </documentation>
				</property>
				<property name="currentUserId" input="true">
					<documentation xml:lang="en-US">
						<label>Current UserId</label>
						<description>
							Current UserId for this workflow
						</description>
				    </documentation>
				</property>
				<property name="numberOfChangedRecords" output="true">
					<documentation xml:lang="en-US">
						<label>Number of Changed Records</label>
						<description>
							Number of Changed Records (includes adds, modifications, and deletes) 
						</description>
				    </documentation>
				</property>
			</properties>
		</bean>
 * }</pre>
 */
public class ImportChangesIntoWorkingDataspaceScriptTask extends ScriptTaskBean
{
	// NOTE1: Be sure to give the Domain Role permission to Import a Dataspace Archive in the Dataspace Workflow Permissions Template

	// NOTE2:  It is required that you have a Custom Directory that implements the following method:
	//          (if you are using the current ps-directory Project it should already have this method)
	/** EXAMPLE **
	@Override
	public UserReference authenticateUserFromArray(final Object[] args)
	{
		if (args != null && args[0] != null && args[0] instanceof UserReference)
		{
			final UserReference user = (UserReference) args[0];
			return user;
		}
		return super.authenticateUserFromArray(args);
	}
	*** End EXAMPLE */

	private String workingDataSpace;
	private String dataset;
	private String exportFileLocationInfo;
	private String currentUserId;
	private String numberOfChangedRecords = "0";

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		// Export the change set from the Workflow’s child dataspace
		// Set a Data Context Variable with location of the exported file

		Repository repo = context.getRepository();
		AdaptationHome workingDataSpaceHome = AdaptationUtil
			.getDataSpaceOrThrowOperationException(repo, workingDataSpace);
		Adaptation datasetAdaptation = AdaptationUtil
			.getDataSetOrThrowOperationException(workingDataSpaceHome, dataset);

		if (exportFileLocationInfo != null)
		{
			// import changes from Export File into the Working Datasapce
			importChangesIntoWorkingDataspace(context, workingDataSpaceHome);
			numberOfChangedRecords = String.valueOf(
				AdaptationUtil.getChangedRecords(datasetAdaptation).size()
					+ AdaptationUtil.getDeletedRecords(datasetAdaptation).size());

		}

	}

	private void importChangesIntoWorkingDataspace(
		ScriptTaskBeanContext context,
		AdaptationHome workingDataSpaceHome)
		throws OperationException
	{

		// Create Session for Data context current user
		final Object[] args = new Object[] {
				WorkflowUtilities.getUserReference(currentUserId, context.getRepository()) };
		Session userSession = context.getRepository().createSessionFromArray(args);

		// Import change set into Working DataSpace
		ArchiveImportSpec archiveImportSpec = new ArchiveImportSpec();
		archiveImportSpec.setMode(ArchiveImportSpecMode.CHANGESET);
		archiveImportSpec.setArchive(Archive.forFileInDefaultDirectory(exportFileLocationInfo));
		ImportArchiveProcedure importProc = new ImportArchiveProcedure(archiveImportSpec);
		ProcedureExecutor.executeProcedure(importProc, userSession, workingDataSpaceHome);

		// Delete the file from the default directory when done
		try
		{
			Files.deleteIfExists(Paths.get(archiveImportSpec.getArchive().getLocationInfo()));
		}
		catch (IOException e)
		{
			throw OperationException.createError(e.getMessage(), e);
		}

	}

	private class ImportArchiveProcedure implements Procedure
	{
		private ArchiveImportSpec spec;
		public ImportArchiveProcedure(ArchiveImportSpec spec)
		{
			this.spec = spec;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			pContext.doImportArchive(spec);
		}
	}

	public String getWorkingDataSpace()
	{
		return workingDataSpace;
	}

	public void setWorkingDataSpace(String workingDataSpace)
	{
		this.workingDataSpace = workingDataSpace;
	}

	public String getDataset()
	{
		return dataset;
	}

	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	public String getExportFileLocationInfo()
	{
		return exportFileLocationInfo;
	}

	public void setExportFileLocationInfo(String fileLocationInfo)
	{
		this.exportFileLocationInfo = fileLocationInfo;
	}

	public String getCurrentUserId()
	{
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId)
	{
		this.currentUserId = currentUserId;
	}

	public String getNumberOfChangedRecords()
	{
		return numberOfChangedRecords;
	}

	public void setNumberOfChangedRecords(String numberOfChangedRecords)
	{
		this.numberOfChangedRecords = numberOfChangedRecords;
	}

}
