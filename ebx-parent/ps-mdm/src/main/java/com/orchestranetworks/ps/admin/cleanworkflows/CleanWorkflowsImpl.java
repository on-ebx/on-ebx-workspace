/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.cleanworkflows.CleanWorkflowsConfig.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class CleanWorkflowsImpl
{
	private enum WarningMessageType {
		MISSING_DATA_SPACE_PARAM, EMPTY_DATA_SPACE_PARAM, DATA_SPACE_NOT_FOUND
	}

	public void execute(Session session, Repository repository, CleanWorkflowsConfig config)
		throws OperationException
	{
		// Make sure only admins can execute since this service can have serious consequences.
		// Also, it utilizes things that usually only an administrator has the ability to do.
		if (!session.isUserInRole(Role.ADMINISTRATOR))
		{
			throw OperationException
				.createError("User doesn't have permission to execute service.");
		}

		// Collect the workflows (process instances) that should be terminated and cleaned
		Set<ProcessInstance> processInstances = getProcessInstancesToClean(
			repository,
			session,
			config);
		// Clean the data spaces first. This should be done before cleaning the workflows
		// because it possibly utilizes the workingDataSpace params of the workflows, and
		// the workflows would be gone if we cleaned those first.
		cleanDataSpaces(repository, session, processInstances, config);
		// Now clean the workflows
		cleanWorkflows(repository, session, processInstances, config);
	}

	/**
	 * Collect all of the workflows (process instances) that are going to be cleaned.
	 * These will be used to terminate and clean them, but also to find the working data spaces
	 * that need to be closed.
	 *
	 * @param repo the repository
	 * @param session the session
	 * @param config the config
	 * @return the process instances to clean
	 * @throws OperationException if an error occurred while looking up the process instances
	 */
	protected Set<ProcessInstance> getProcessInstancesToClean(
		Repository repo,
		Session session,
		CleanWorkflowsConfig config)
		throws OperationException
	{
		Set<ProcessInstance> workflowsToClean = new HashSet<>();
		Date latestDateToKeep = getLatestDateToKeep(config);
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
		// For each publication, find all of its process instance keys that should be cleaned and add them to the set
		for (PublishedProcess workflowPublication : config.getWorkflowPublications())
		{
			List<ProcessInstanceKey> processInstanceKeys = wfEngine
				.getProcessInstanceKeys(workflowPublication.getPublishedProcessKey());
			for (ProcessInstanceKey processInstanceKey : processInstanceKeys)
			{
				ProcessInstance processInstance = wfEngine.getProcessInstance(processInstanceKey);
				if (shouldCleanProcessInstance(processInstance, latestDateToKeep))
				{
					workflowsToClean.add(processInstance);
				}
			}
		}
		return workflowsToClean;
	}

	private Date getLatestDateToKeep(CleanWorkflowsConfig config)
	{
		Date latestDateToKeep = null;
		Integer createdBeforeNumOfDays = config.getCreatedBeforeNumOfDays();
		if (createdBeforeNumOfDays != null)
		{
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			cal.add(Calendar.DAY_OF_MONTH, -1 * (1 + createdBeforeNumOfDays.intValue()));
			latestDateToKeep = cal.getTime();
		}

		Date createdBeforeDate = config.getCreatedBeforeDate();
		if (latestDateToKeep == null
			|| (createdBeforeDate != null && latestDateToKeep.after(createdBeforeDate)))
		{
			latestDateToKeep = createdBeforeDate;
		}
		return latestDateToKeep;
	}

	private static boolean shouldCleanProcessInstance(
		ProcessInstance processInstance,
		Date latestDateToKeep)
	{
		Date creationDate = processInstance.getCreationDate();
		return creationDate.before(latestDateToKeep);
	}

	/**
	 * Terminate and clean the given workflows
	 *
	 * @param repo the repository
	 * @param session the session
	 * @param processInstances the process instances to terminate and clean
	 * @param config the config
	 * @throws OperationException if an error occurred while terminating and cleaning the workflows
	 */
	protected void cleanWorkflows(
		Repository repo,
		Session session,
		Set<ProcessInstance> processInstances,
		CleanWorkflowsConfig config)
		throws OperationException
	{
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
		for (ProcessInstance processInstance : processInstances)
		{
			if (!processInstance.isSubWorkflow() && shouldCleanWorkflow(processInstance, config))
			{
				cleanWorkflow(repo, session, wfEngine, processInstance);
			}
		}
		// TODO: Would be nice to also clear out the published processes that no longer have workflows associated with them,
		//      but I don't see a way to do this via the API.
	}

	protected boolean shouldCleanWorkflow(
		ProcessInstance processInstance,
		CleanWorkflowsConfig config)
	{
		return (config.isIncludeActive() && !processInstance.isCompleted())
			|| (config.isIncludeCompleted() && processInstance.isCompleted());
	}

	/**
	 * Terminate and clean the given workflow
	 *
	 * @param repository the repository
	 * @param session the session
	 * @param workflowEngine a {@link WorkflowEngine} instance
	 * @param processInstance the workflow to clean
	 * @throws OperationException if an error occurs cleaning the workflow
	 */
	protected void cleanWorkflow(
		Repository repository,
		Session session,
		WorkflowEngine workflowEngine,
		ProcessInstance processInstance)
		throws OperationException
	{
		workflowEngine.terminateProcessInstance(processInstance.getProcessInstanceKey());
	}

	/**
	 * Close the data spaces. If the configuration specifies to use the working data space parameter,
	 * then this will loop through the process instances to clean and close their working data spaces,
	 * if they are child data spaces. It will then close any data spaces that are children of the
	 * data spaces specified as masters in the configuration. In all cases, it won't close a data space
	 * if it's listed as a child data space to skip in the configuration.
	 *
	 * @param repo the repository
	 * @param session the session
	 * @param processInstances the process instances to look for working data spaces in
	 * @param config the config
	 * @throws OperationException if an error occurs closing a data space
	 */
	protected void cleanDataSpaces(
		Repository repo,
		Session session,
		Set<ProcessInstance> processInstances,
		CleanWorkflowsConfig config)
		throws OperationException
	{
		if (config.isUseWorkingDataSpace())
		{
			for (ProcessInstance processInstance : processInstances)
			{
				DataContextReadOnly dataContext = processInstance.getDataContext();
				// If the workflow has a workingDataSpace parameter
				if (dataContext.isVariableDefined(WorkflowConstants.PARAM_WORKING_DATA_SPACE))
				{
					String workingDataSpaceName = dataContext
						.getVariableString(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
					// If the workingDataSpace parameter doesn't have a value then show a warning,
					// since it's expected that it does. But continue processing.
					if (workingDataSpaceName == null)
					{
						logDataContextWarning(
							WorkflowConstants.PARAM_WORKING_DATA_SPACE,
							WarningMessageType.EMPTY_DATA_SPACE_PARAM,
							session.getLocale(),
							processInstance);
					}
					else
					{
						AdaptationHome workingDataSpace = null;
						try
						{
							workingDataSpace = WorkflowUtilities.getDataSpace(
								dataContext,
								repo,
								WorkflowConstants.PARAM_WORKING_DATA_SPACE);
						}
						// If the working data space wasn't found, an exception will be thrown.
						// Log it as a warning but continue processing. (Someone may have cleaned
						// it up already.)
						catch (OperationException ex)
						{
							logDataContextWarning(
								WorkflowConstants.PARAM_WORKING_DATA_SPACE,
								WarningMessageType.DATA_SPACE_NOT_FOUND,
								session.getLocale(),
								processInstance);
						}
						if (workingDataSpace != null && dataContext
							.isVariableDefined(WorkflowConstants.PARAM_MASTER_DATA_SPACE))
						{
							String masterDataSpaceName = dataContext
								.getVariableString(WorkflowConstants.PARAM_MASTER_DATA_SPACE);
							// If the master data space param is empty, log a warning,
							// but keep processing
							if (masterDataSpaceName == null)
							{
								logDataContextWarning(
									WorkflowConstants.PARAM_MASTER_DATA_SPACE,
									WarningMessageType.EMPTY_DATA_SPACE_PARAM,
									session.getLocale(),
									processInstance);
							}
							else
							{
								// This will throw an exception if it's not found, but in the case of the
								// master data space, that's what we want to happen. There are bigger problems
								// here if the master isn't found so we want the whole process to end.
								AdaptationHome masterDataSpace = WorkflowUtilities.getDataSpace(
									dataContext,
									repo,
									WorkflowConstants.PARAM_MASTER_DATA_SPACE);
								// Close the data space only if the working data space is different from
								// the master. (We don't want to close the master data space!)
								if (!masterDataSpace.getKey().equals(workingDataSpace.getKey()))
								{
									closeDataSpace(workingDataSpace, repo, session, config);
								}
							}
						}
						// The master data space param is missing entirely, so log a warning,
						// but keep processing
						else
						{
							logDataContextWarning(
								WorkflowConstants.PARAM_MASTER_DATA_SPACE,
								WarningMessageType.MISSING_DATA_SPACE_PARAM,
								session.getLocale(),
								processInstance);
						}
					}
				}

				else
				{
					logDataContextWarning(
						WorkflowConstants.PARAM_WORKING_DATA_SPACE,
						WarningMessageType.MISSING_DATA_SPACE_PARAM,
						session.getLocale(),
						processInstance);
				}
			}
		}

		// After closing the data spaces associated with the workflows,
		// now close the ones that are children of the masters specified,
		// if any masters were specified
		List<AdaptationHome> masterDataSpaces = config.getMasterDataSpaces();
		for (AdaptationHome masterDataSpace : masterDataSpaces)
		{
			// Loop through all snapshots underneath the master, because there is
			// always a snapshot between the master and its child
			List<AdaptationHome> snapshots = masterDataSpace.getVersionChildren();
			for (AdaptationHome snapshot : snapshots)
			{
				// If the snapshot is the initial snapshot created by the child data space
				// then loop through the child data spaces of this snapshot and close them
				if (snapshot.isInitialVersion())
				{
					List<AdaptationHome> dataSpaces = snapshot.getBranchChildren();
					for (AdaptationHome dataSpace : dataSpaces)
					{
						closeDataSpace(dataSpace, repo, session, config);
					}
				}
			}
		}
	}

	private void closeDataSpace(
		AdaptationHome dataSpace,
		Repository repo,
		Session session,
		CleanWorkflowsConfig config)
		throws OperationException
	{
		if (dataSpace.isOpen() && !config.getChildDataSpacesToSkip().contains(dataSpace))
		{
			// Close it
			repo.closeHome(dataSpace, session);

			// If it's DELETE or DELETE_HISTORY
			DataSpaceClosePolicy closePolicy = config.getDataSpaceClosePolicy();
			if (closePolicy != DataSpaceClosePolicy.CLOSE)
			{
				// If we're also deleting history, then mark it for history purge
				if (closePolicy == DataSpaceClosePolicy.DELETE_HISTORY)
				{
					repo.getPurgeDelegate().markHomeForHistoryPurge(dataSpace, session);
				}
				// Delete the data space, which will really just mark it for deletion on the next purge
				repo.deleteHome(dataSpace, session);
			}
		}
	}

	private static void logDataContextWarning(
		String paramName,
		WarningMessageType warningMessageType,
		Locale locale,
		ProcessInstance processInstance)
	{
		StringBuilder bldr = new StringBuilder();
		if (warningMessageType == WarningMessageType.DATA_SPACE_NOT_FOUND)
		{
			bldr.append("Data space ");
			DataContextReadOnly dataContext = processInstance.getDataContext();
			String dataSpaceId = dataContext.getVariableString(paramName);
			bldr.append(dataSpaceId);
			bldr.append(" specified by param ");
			bldr.append(paramName);
			bldr.append(" in data context was not found.");
		}
		else
		{
			bldr.append("Param ");
			bldr.append(paramName);
			bldr.append(" was ");
			if (warningMessageType == WarningMessageType.MISSING_DATA_SPACE_PARAM)
			{
				bldr.append("not found");
			}
			else
			{
				bldr.append("empty");
			}
			bldr.append(" in data context.");
		}
		bldr.append(" Workflow = ");
		bldr.append(processInstance.getLabel().formatMessage(locale));
		bldr.append(", key = ");
		bldr.append(processInstance.getProcessInstanceKey().format());
		bldr.append(".");
		LoggingCategory.getKernel().warn(bldr.toString());
	}
}
