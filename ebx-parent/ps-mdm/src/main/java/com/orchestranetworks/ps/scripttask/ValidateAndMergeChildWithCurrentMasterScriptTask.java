package com.orchestranetworks.ps.scripttask;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.*;

/**
 * Validate a child dataspace and merge it into the current master.
 * <pre>{@code
<bean className="<your package and subclass name prefix goes here>ValidateAndMergeChildWithCurrentMasterScriptTask">
	<documentation xml:lang="en-US">
		<label>Validate versus Current Master and Merge Changes if Valid</label>
		<description>
		    Validate changes from this workflow with changes from the current Master and Merge if valid
		</description>
	</documentation>
	<properties>
		<property name="masterDataSpace" input="true">
		    <documentation xml:lang="en-US">
				<label>Master Data space</label>
				<description>
				   Master Dataspace for this workflow
				</description>
		    </documentation>
		</property>
		<property name="workingDataSpace" input="true" output="true">
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
		<property name="currentUserId" input="true">
			<documentation xml:lang="en-US">
				<label>Current UserId</label>
				<description>
					Current UserId for this workflow
				</description>
		    </documentation>
		</property>
		<property name="tablePathsToBeValidatedField" input="true">
		    <documentation xml:lang="en-US">
				 <label>Table Paths to be Validated Field</label>
				<description>
				   Optional: ClassName.FieldName containing the List of Paths to be validated by this workflow				   
				</description>
		    </documentation>
		</property>
		<property name="validateChangedRecordsOnly" input="true">
		    <documentation xml:lang="en-US">
				 <label>Validate Changed Records Only</label>
				<description>
				    Boolean indicating that only the records that have been changed for the dataset in the working dataspace should be validated				   
				</description>
		    </documentation>
		</property>
		<property name="isValidWithMaster" output="true">
			<documentation xml:lang="en-US">
				<label>Is Valid With Master</label>
				<description>
					Boolean indicating if this workflow's changes are valid with the current Master 
				</description>
		    </documentation>
		</property>
	</properties>
</bean>
 * }</pre>
 */
public class ValidateAndMergeChildWithCurrentMasterScriptTask extends ScriptTaskBean
{
	// NOTE1: Be sure to give the Domain Role permission to Import a Dataspace Archive in the Dataspace Workflow Permissions Template

	// NOTE2: It is recommended to create a subclass for each Master Data Space to be Validated and Merged,
	// overriding the execute method with a block that is "synchronized" on the Class object and calls super.execute (see EXAMPLE)	
	// -- This will insure that if 2 workflows are approved within a short timeframe, that they will be validated and merged sequentially
	/** EXAMPLE **
		@Override
		public void executeScript(ScriptTaskBeanContext context) throws OperationException
		{
			synchronized (<your subclass name prefix goes here>ValidateAndMergeChildWithCurrentMasterScriptTask.class)
			{
				super.executeScript(context);
			}
		}
	*** End EXAMPLE */

	// NOTE3:  It is required that you have a Custom Directory that implements the following method:
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

	private String masterDataSpace;
	private String workingDataSpace;
	private String dataset;
	private boolean isValidWithMaster;
	private String tablePathsToBeValidatedField;
	private boolean validateChangedRecordsOnly = false;
	private String currentUserId;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		// Create a new child dataspace from the current Master.
		// Export the change set from the Workflow’s child dataspace and import it into the new Child DataSpace
		// Validate the new child dataspace
		// If Valid, then Merge the original Working Dataspace into the Master
		// If Not Valid Make the new Child DataSpace the Workflow’s child data space 
		// Set a Data Context Variable with the result of the validation, so the workflow can either send completion notification or redirect back to the update step

		Repository repo = context.getRepository();
		AdaptationHome workingDataSpaceHome = AdaptationUtil
			.getDataSpaceOrThrowOperationException(repo, workingDataSpace);
		AdaptationHome masterDataSpaceHome = AdaptationUtil
			.getDataSpaceOrThrowOperationException(repo, masterDataSpace);

		// Get change set from Workflow's Child DataSpace
		DifferenceBetweenHomes differenceBetweenHomes = DifferenceHelper
			.compareHomes(workingDataSpaceHome.getParent(), workingDataSpaceHome, false);
		if (differenceBetweenHomes.isEmpty())
		{
			isValidWithMaster = true;
			return;
		}

		// create new child dataspace from current master
		AdaptationHome newChildDataSpace = createNewChildDataSpace(
			context,
			workingDataSpaceHome,
			masterDataSpaceHome);

		// export changes from Workflow Child DataSpace and Import into the new Child DataSpace
		exportChangesFromOldChildToNewChild(
			context,
			workingDataSpaceHome,
			differenceBetweenHomes,
			newChildDataSpace);

		// Validate the new child dataspace, limiting validation to the subset of tables defined for the workflow
		Adaptation datasetInstance = AdaptationUtil
			.getDataSetOrThrowOperationException(newChildDataSpace, dataset);
		if (hasValidationErrors(
			datasetInstance,
			getTablePathsToBeValidated(),
			isValidateChangedRecordsOnly()))
		{
			// Make the new Child dataspace the Workflow’s child dataspace and set "isValidWithMaster" context variable to false
			workingDataSpace = newChildDataSpace.getKey().getName();
			repo.closeHome(workingDataSpaceHome, context.getSession());
			isValidWithMaster = false;
		}
		else
		{
			// Close the new Child Data Space and Perform Merge on the Working Data Space 
			repo.closeHome(newChildDataSpace, context.getSession());
			HomeUtils.mergeDataSpaceToParent(context.getSession(), workingDataSpaceHome);
			isValidWithMaster = true;
		}

	}

	private void exportChangesFromOldChildToNewChild(
		ScriptTaskBeanContext context,
		AdaptationHome workingDataSpaceHome,
		DifferenceBetweenHomes differenceBetweenHomes,
		AdaptationHome newChildDataSpace)
		throws OperationException
	{
		// Export change set from Workflow Child DataSpace to archive file
		ArchiveExportSpec archiveExportSpec = new ArchiveExportSpec();
		String fileName = "ValidateAndMergeChildWithCurrentMasterScriptTask"
			+ System.currentTimeMillis();
		archiveExportSpec.setArchive(Archive.forFileInDefaultDirectory(fileName));
		archiveExportSpec.setDifferencesBetweenHomes(differenceBetweenHomes);
		archiveExportSpec.setDifferencesWithMinimalContentsOnRight(true);
		ExportArchiveProcedure exportProc = new ExportArchiveProcedure(archiveExportSpec);
		ProcedureExecutor.executeProcedure(exportProc, context.getSession(), workingDataSpaceHome);

		// Create Session for Data context current user if there is a current user
		Session userSession = context.getSession();
		if (currentUserId != null && !currentUserId.trim().isEmpty())
		{
			final Object[] args = new Object[] {
					WorkflowUtilities.getUserReference(currentUserId, context.getRepository()) };
			userSession = context.getRepository().createSessionFromArray(args);
		}

		// Import change set into new Child DataSpace
		ArchiveImportSpec archiveImportSpec = new ArchiveImportSpec();
		archiveImportSpec.setMode(ArchiveImportSpecMode.CHANGESET);
		archiveImportSpec.setArchive(archiveExportSpec.getArchive());
		ImportArchiveProcedure importProc = new ImportArchiveProcedure(archiveImportSpec);
		ProcedureExecutor.executeProcedure(importProc, userSession, newChildDataSpace);

		// Delete the file from the default directory when done
		String fileLocationInfo = archiveExportSpec.getArchive().getLocationInfo();

		try
		{
			Files.deleteIfExists(Paths.get(fileLocationInfo));
		}
		catch (IOException e)
		{
			throw OperationException.createError(e.getMessage(), e);
		}
	}

	private AdaptationHome createNewChildDataSpace(
		ScriptTaskBeanContext context,
		AdaptationHome workingDataSpaceHome,
		AdaptationHome masterDataSpaceHome)
		throws OperationException
	{
		HomeCreationSpec homeCreationSpec = new HomeCreationSpec();
		homeCreationSpec.setKey(
			HomeKey.parse(
				"B" + (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")).format(new Date())));
		homeCreationSpec.setParent(masterDataSpaceHome);
		homeCreationSpec.setHomeToCopyPermissionsFrom(workingDataSpaceHome);
		homeCreationSpec.setOwner(CommonConstants.TECH_ADMIN);
		if (workingDataSpaceHome.getLabel() != null)
		{
			homeCreationSpec.setLabel(
				UserMessage.createInfo(
					workingDataSpaceHome.getLabel().formatMessage(Locale.getDefault()) + " +1"));
		}
		return context.getRepository().createHome(homeCreationSpec, context.getSession());
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

	protected boolean hasValidationErrors(
		Adaptation datasetAdaptation,
		List<Path> tablePathsToBeValidated)
	{
		return hasValidationErrors(datasetAdaptation, tablePathsToBeValidated, false);
	}

	protected boolean hasValidationErrors(
		Adaptation datasetAdaptation,
		List<Path> tablePathsToBeValidated,
		boolean validateChangedRecordsOnly)
	{
		if (validateChangedRecordsOnly == true)
		{
			List<Adaptation> recordsToBeValidated = getChangedRecordsForValidation(
				datasetAdaptation);
			for (Adaptation record : recordsToBeValidated)
			{
				if (record.getValidationReport().hasItemsOfSeverity(Severity.ERROR))
				{
					return true;
				}
			}
			return false;

		}
		else if (tablePathsToBeValidated == null || tablePathsToBeValidated.isEmpty())
		{
			return datasetAdaptation.getValidationReport().hasItemsOfSeverityOrMore(Severity.ERROR);
		}
		else
		{
			boolean hasValidationErrors = false;
			for (Path tablePath : tablePathsToBeValidated)
			{
				if (datasetAdaptation.getTable(tablePath)
					.getValidationReport()
					.hasItemsOfSeverityOrMore(Severity.ERROR))
				{
					hasValidationErrors = true;
					// Continue to validate the rest of the tables so that when the workflow is redirected to the user data entry task, any errors will be displayed.
				}
			}
			return hasValidationErrors;
		}
	}

	// Get the changed records in the data set, for use in validating changed records only
	// in the data set. This simply returns all changed records but can be overridden if
	// different logic is required.
	protected List<Adaptation> getChangedRecordsForValidation(Adaptation dataset)
	{
		return AdaptationUtil.getChangedRecords(dataset);
	}

	@SuppressWarnings("unchecked")
	protected List<Path> getTablePathsToBeValidated() throws OperationException
	{
		if (tablePathsToBeValidatedField == null || tablePathsToBeValidatedField.isEmpty())
		{
			return null;
		}
		// Use reflection to get the List of Table Paths to be validated from a static variable in a Class that is used by the rest of the workflow
		try
		{
			Class<?> tablePathsToBeValidatedClass = Class
				.forName(StringUtils.substringBeforeLast(tablePathsToBeValidatedField, "."));
			java.lang.reflect.Field fieldContainingList = tablePathsToBeValidatedClass
				.getField(StringUtils.substringAfterLast(tablePathsToBeValidatedField, "."));
			return (List<Path>) fieldContainingList.get(null);
		}
		catch (Exception e)
		{
			throw OperationException.createError(
				"ScriptTask Bean Parameter 'tablePathsToBeValidatedField' must be a valid List of Paths.",
				e);
		}
	}

	public String getMasterDataSpace()
	{
		return masterDataSpace;
	}

	public void setMasterDataSpace(String masterDataSpace)
	{
		this.masterDataSpace = masterDataSpace;
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

	public String getIsValidWithMaster()
	{
		return Boolean.toString(isValidWithMaster);
	}

	public void setIsValidWithMaster(String isValidWithMaster)
	{
		this.isValidWithMaster = Boolean.parseBoolean(isValidWithMaster);

	}

	public String getTablePathsToBeValidatedField()
	{
		return tablePathsToBeValidatedField;
	}

	public void setTablePathsToBeValidatedField(String tablePathsToBeValidatedField)
	{
		this.tablePathsToBeValidatedField = tablePathsToBeValidatedField;
	}

	public boolean isValidateChangedRecordsOnly()
	{
		return validateChangedRecordsOnly;
	}

	public void setValidateChangedRecordsOnly(boolean performIncrementalDatasetValidation)
	{
		this.validateChangedRecordsOnly = performIncrementalDatasetValidation;
	}

	public String getCurrentUserId()
	{
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId)
	{
		this.currentUserId = currentUserId;
	}

}
