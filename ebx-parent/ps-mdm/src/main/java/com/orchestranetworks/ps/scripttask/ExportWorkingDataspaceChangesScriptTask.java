package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.*;

/**
 * Export the working dataspace changes to an archive file.
 * <pre>{@code
		<bean className="com.orchestranetworks.ps.scripttask.ExportWorkingDataspaceChangesScriptTask">
			<documentation xml:lang="en-US">
				<label>Export the working dataspace changes to an archive file.</label>
				<description>
				    Export the working dataspace changes to an archive file and store the file location in the data context.
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
				<property name="exportFileLocationInfo" output="true">
					<documentation xml:lang="en-US">
						<label>Export File Location Info</label>
						<description>
							the file location within the default directory 
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
public class ExportWorkingDataspaceChangesScriptTask extends ScriptTaskBean
{

	private String workingDataSpace;
	private String dataset;
	private String exportFileLocationInfo = null;
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

		// Get change set from Workflow's Child DataSpace
		DifferenceBetweenHomes differenceBetweenHomes = DifferenceHelper
			.compareHomes(workingDataSpaceHome.getParent(), workingDataSpaceHome, false);
		if (!differenceBetweenHomes.isEmpty())
		{
			// export changes from Workflow Child DataSpace
			exportFileLocationInfo = exportChangesFromWorkingDataspace(
				context,
				workingDataSpaceHome,
				differenceBetweenHomes);
			numberOfChangedRecords = String.valueOf(
				AdaptationUtil.getChangedRecords(datasetAdaptation).size()
					+ AdaptationUtil.getDeletedRecords(datasetAdaptation).size());
		}

	}

	private String exportChangesFromWorkingDataspace(
		ScriptTaskBeanContext context,
		AdaptationHome workingDataSpaceHome,
		DifferenceBetweenHomes differenceBetweenHomes)
		throws OperationException
	{
		// Export change set from Workflow Child DataSpace to archive file
		ArchiveExportSpec archiveExportSpec = new ArchiveExportSpec();
		String fileName = "ExportWorkingDataspaceChangesScriptTask" + System.currentTimeMillis();
		archiveExportSpec.setArchive(Archive.forFileInDefaultDirectory(fileName));
		archiveExportSpec.setDifferencesBetweenHomes(differenceBetweenHomes);
		archiveExportSpec.setDifferencesWithMinimalContentsOnRight(true);
		ExportArchiveProcedure exportProc = new ExportArchiveProcedure(archiveExportSpec);
		ProcedureExecutor.executeProcedure(exportProc, context.getSession(), workingDataSpaceHome);

		// return the file location within the default directory
		return fileName;

	}

	private class ExportArchiveProcedure implements Procedure
	{
		private ArchiveExportSpec spec;
		public ExportArchiveProcedure(ArchiveExportSpec spec)
		{
			this.spec = spec;
		}
		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			pContext.doExportArchive(spec);
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

	public String getNumberOfChangedRecords()
	{
		return numberOfChangedRecords;
	}

	public void setNumberOfChangedRecords(String numberOfChangedRecords)
	{
		this.numberOfChangedRecords = numberOfChangedRecords;
	}

}
