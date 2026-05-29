/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.workflow.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.procedure.Procedures.*;
import com.orchestranetworks.ps.project.actionpermissions.*;
import com.orchestranetworks.ps.project.constants.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.UserTask.*;

/**
 * A trigger for project-related workflows. It stores information about the users and roles associated
 * with a workflow into a cache for faster checking of permissions.
 */
public class ProjectWorkflowTrigger extends WorkflowTriggerBean implements ProjectPathCapable
{
	/**
	 * This should be abstract but for backwards-compatibility with existing workflows,
	 * can't do that. This class is intended to be extended.
	 */
	@Override
	public ProjectPathConfig getProjectPathConfig()
	{
		return null;
	}

	protected ProjectWorkflowPermissionsInfoFactory getPermissionsInfoFactory()
	{
		return DefaultProjectWorkflowPermissionsInfoFactory.getInstance();
	}

	@Override
	public void handleAfterProcessInstanceStart(
		WorkflowTriggerAfterProcessInstanceStartContext context)
		throws OperationException
	{
		final ProcessInstance processInstance = context.getProcessInstance();
		Repository repo = context.getRepository();
		Session session = context.getSession();
		// If this is a subworkflow, refresh cache for its parent workflow so that it will contain
		// the keys for its subworkflows
		if (processInstance.isSubWorkflow())
		{
			WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
			ProcessInstance parentWorkflow = wfEngine
				.getProcessInstance(processInstance.getParentKey());
			ProjectWorkflowPermissionsCache.getInstance()
				.refreshCache(
					parentWorkflow,
					session,
					repo,
					null,
					null,
					getPermissionsInfoFactory());
		}

		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;

			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					createWorkflowEvent(
						pContext,
						dataContext,
						processInstance,
						null,
						null,
						new Date());
				}
			};
			ProcedureExecutor
				.executeProcedure(proc, session, lookupWorkflowEventDataSpace(dataContext, repo));
		}
	}

	@Override
	public void handleAfterWorkItemCreation(WorkflowTriggerAfterWorkItemCreationContext context)
		throws OperationException
	{
		HashSet<UserReference> users = null;
		UserReference workItemUser = context.getWorkItem().getUserReference();
		if (workItemUser != null)
		{
			users = new HashSet<>();
			users.add(workItemUser);
		}
		final ProcessInstance processInstance = context.getProcessInstance();
		Repository repo = context.getRepository();
		Session session = context.getSession();
		ProjectWorkflowPermissionsCache.getInstance()
			.refreshCache(processInstance, session, repo, users, null, getPermissionsInfoFactory());

		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;
			final WorkItem workItem = context.getWorkItem();
			final Integer stepId = Integer.valueOf(context.getCurrentStepId());
			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					// beforeStart can get called before afterCreation, so it's possible that the
					// record already was created
					// This is handled by createWorkflowEvent also, but best to capture it here
					// since it's an expected situation
					// due to nature of trigger events
					Adaptation existingRecord = lookupWorkItemWorkflowEventRecord(
						dataContext,
						workItem,
						pContext.getAdaptationHome().getRepository());
					if (existingRecord == null)
					{
						createWorkflowEvent(
							pContext,
							dataContext,
							processInstance,
							workItem,
							stepId,
							new Date());
					}
				}
			};
			ProcedureExecutor
				.executeProcedure(proc, session, lookupWorkflowEventDataSpace(dataContext, repo));
		}
	}

	protected boolean isTrackingWorkflowEvents()
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		// For backwards-compatibility, need to allow for the path config not being specified
		return pathConfig != null && pathConfig.getProjectWorkflowEventTablePath() != null;
	}

	private Adaptation createWorkflowEvent(
		ProcedureContext pContext,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		WorkItem workItem,
		Integer stepId,
		Date date)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Repository repo = pContext.getAdaptationHome().getRepository();
		Session session = pContext.getSession();
		// See if a workflow event already exists
		Adaptation workflowEventRecord = lookupWorkflowWorkflowEventRecord(
			dataContext,
			processInstance,
			repo);
		if (workflowEventRecord == null)
		{
			// If this is a work item creation event then create the workflow event record
			// (This could happen with workflows that existed already before adding this code)
			if (workItem != null)
			{
				Locale locale = session.getLocale();
				LoggingCategory.getWorkflow()
					.warn(
						"No event record found for workflow "
							+ processInstance.getLabel().formatMessage(locale)
							+ " while creating event record for work item "
							+ workItem.getLabel().formatMessage(locale) + ". Created record.");
				workflowEventRecord = createWorkflowEvent(
					pContext,
					dataContext,
					processInstance,
					null,
					null,
					null);
			}
		}
		// Else if this is a workflow creation event, just return since it's already created
		else if (workItem == null)
		{
			LoggingCategory.getWorkflow()
				.warn(
					"Event record already found for workflow "
						+ processInstance.getLabel().formatMessage(session.getLocale())
						+ " while creating event record.");
			return workflowEventRecord;
		}

		Adaptation dataSet = WorkflowUtilities
			.getDataSet(dataContext, repo, WorkflowConstants.PARAM_WORKING_DATA_SPACE);

		HashMap<Path, Object> pathValueMap = new HashMap<>();
		String eventType;
		if (workItem == null)
		{
			eventType = CommonProjectConstants.PROJECT_WORKFLOW_EVENT_TYPE_WORKFLOW;
		}
		else
		{
			eventType = CommonProjectConstants.PROJECT_WORKFLOW_EVENT_TYPE_WORKITEM;
			addWorkItemEventValues(
				pathValueMap,
				dataContext,
				processInstance,
				workItem,
				stepId,
				repo,
				session);
		}
		addCommonWorkflowEventValues(
			pathValueMap,
			eventType,
			dataContext,
			processInstance,
			repo,
			session,
			date);

		AdaptationTable workflowEventTable = dataSet
			.getTable(pathConfig.getProjectWorkflowEventTablePath());
		Adaptation record = Create.execute(pContext, workflowEventTable, pathValueMap);

		updateWorkflowEventsWithProject(
			pContext,
			workflowEventTable,
			(String) pathValueMap.get(pathConfig.getProjectWorkflowEventProjectFieldPath()),
			(String) pathValueMap
				.get(pathConfig.getProjectWorkflowEventMasterWorkflowInstanceKeyFieldPath()));

		return record;
	}

	private void endWorkflowEvent(
		ProcedureContext pContext,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		WorkItem workItem,
		Integer stepId,
		Boolean isAccepted)
		throws OperationException
	{
		Date now = new Date();
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Repository repo = pContext.getAdaptationHome().getRepository();
		Session session = pContext.getSession();
		HashMap<Path, Object> pathValueMap = new HashMap<>();
		Adaptation record;
		if (workItem == null)
		{
			record = lookupWorkflowWorkflowEventRecord(dataContext, processInstance, repo);
			if (record == null)
			{
				LoggingCategory.getWorkflow()
					.warn(
						"No event record found for workflow "
							+ processInstance.getLabel().formatMessage(session.getLocale())
							+ " while ending event record. Created record.");
				record = createWorkflowEvent(
					pContext,
					dataContext,
					processInstance,
					null,
					null,
					null);
			}
		}
		else
		{
			record = lookupWorkItemWorkflowEventRecord(dataContext, workItem, repo);
			if (record == null)
			{
				Locale locale = session.getLocale();
				LoggingCategory.getWorkflow()
					.warn(
						"No event record found for work item "
							+ workItem.getLabel().formatMessage(locale) + " in workflow "
							+ processInstance.getLabel().formatMessage(locale)
							+ " while ending event record. Created record.");
				record = createWorkflowEvent(
					pContext,
					dataContext,
					processInstance,
					workItem,
					stepId,
					null);
			}
			pathValueMap
				.put(pathConfig.getProjectWorkflowEventWorkItemIsAcceptedFieldPath(), isAccepted);
			// Clear out the work item reference so that next time this user task is hit,
			// it will not find the same record and thus create a new one.
			// (Work item records only exist during scope of the work item and pk is reused next
			// time through.)
			pathValueMap
				.put(pathConfig.getProjectWorkflowEventWorkItemInstanceKeyFieldPath(), null);
		}

		pathValueMap.put(pathConfig.getProjectWorkflowEventCompleteDateTimeFieldPath(), now);
		Modify.execute(pContext, record, pathValueMap);
	}

	private Adaptation lookupWorkflowWorkflowEventRecord(
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		Repository repo)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		AdaptationTable workflowEventTable = lookupWorkflowEventTable(dataContext, repo);
		String processInstancePK = convertProcessInstanceKeyToPK(
			processInstance.getProcessInstanceKey());
		String predicate = pathConfig.getProjectWorkflowEventEventTypeFieldPath().format() + "='"
			+ CommonProjectConstants.PROJECT_WORKFLOW_EVENT_TYPE_WORKFLOW + "' and "
			+ pathConfig.getProjectWorkflowEventWorkflowInstanceKeyFieldPath().format() + "='"
			+ processInstancePK + "'";
		return workflowEventTable.lookupFirstRecordMatchingPredicate(predicate);
	}

	private Adaptation lookupWorkItemWorkflowEventRecord(
		DataContextReadOnly dataContext,
		WorkItem workItem,
		Repository repo)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		AdaptationTable workflowEventTable = lookupWorkflowEventTable(dataContext, repo);
		String workItemPK = workItem.getWorkItemKey().toStringForLog();
		String predicate = pathConfig.getProjectWorkflowEventWorkItemInstanceKeyFieldPath().format()
			+ "='" + workItemPK + "'";
		return workflowEventTable.lookupFirstRecordMatchingPredicate(predicate);
	}

	private List<Adaptation> lookupAllWorkflowEventRecordsForMasterWorkflow(
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		Repository repo)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		AdaptationTable workflowEventTable = lookupWorkflowEventTable(dataContext, repo);
		String processInstancePK = convertProcessInstanceKeyToPK(
			processInstance.getProcessInstanceKey());
		String predicate = pathConfig.getProjectWorkflowEventMasterWorkflowInstanceKeyFieldPath()
			.format() + "='" + processInstancePK + "'";
		RequestResult reqRes = workflowEventTable.createRequestResult(predicate);
		try
		{
			return AdaptationUtil.getRecords(reqRes);
		}
		finally
		{
			reqRes.close();
		}
	}

	private AdaptationHome lookupWorkflowEventDataSpace(
		DataContextReadOnly dataContext,
		Repository repo)
		throws OperationException
	{
		return WorkflowUtilities
			.getDataSpace(dataContext, repo, WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	private Adaptation lookupWorkflowEventDataSet(DataContextReadOnly dataContext, Repository repo)
		throws OperationException
	{
		return WorkflowUtilities
			.getDataSet(dataContext, repo, WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	private AdaptationTable lookupWorkflowEventTable(
		DataContextReadOnly dataContext,
		Repository repo)
		throws OperationException
	{
		Adaptation dataSet = lookupWorkflowEventDataSet(dataContext, repo);
		return dataSet.getTable(getProjectPathConfig().getProjectWorkflowEventTablePath());
	}

	private void updateWorkflowEventUser(
		ProcedureContext pContext,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		WorkItem workItem,
		Integer stepId,
		UserReference user)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Adaptation record = lookupWorkItemWorkflowEventRecord(
			dataContext,
			workItem,
			pContext.getAdaptationHome().getRepository());
		if (record == null)
		{
			Locale locale = pContext.getSession().getLocale();
			LoggingCategory.getWorkflow()
				.warn(
					"No event record found for work item "
						+ workItem.getLabel().formatMessage(locale) + " in workflow "
						+ processInstance.getLabel().formatMessage(locale)
						+ " while updating event record user. Created record.");
			record = createWorkflowEvent(
				pContext,
				dataContext,
				processInstance,
				workItem,
				stepId,
				null);
		}
		Map<Path, Object> pathValueMap = new HashMap<>();
		pathValueMap.put(
			pathConfig.getProjectWorkflowEventWorkItemUserFieldPath(),
			user == null ? null : user.getUserId());
		Modify.execute(pContext, record, pathValueMap);
	}

	private void addCommonWorkflowEventValues(
		Map<Path, Object> pathValueMap,
		String eventType,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		Repository repo,
		Session session,
		Date date)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();

		pathValueMap.put(pathConfig.getProjectWorkflowEventEventTypeFieldPath(), eventType);

		pathValueMap.put(pathConfig.getProjectWorkflowEventCreateDateTimeFieldPath(), date);

		Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecord(
			dataContext,
			null,
			repo,
			pathConfig,
			WorkflowConstants.PARAM_WORKING_DATA_SPACE,
			false);
		if (projectRecord != null)
		{
			pathValueMap.put(
				pathConfig.getProjectWorkflowEventProjectFieldPath(),
				projectRecord.getOccurrencePrimaryKey().format());
		}

		String processInstancePK = convertProcessInstanceKeyToPK(
			processInstance.getProcessInstanceKey());
		String wfModelName = getWorkflowModelNameFromProcessInstanceKey(processInstancePK);
		String wfModelLabel = lookupWorkflowModelLabel(wfModelName, repo, session.getLocale());

		pathValueMap.put(pathConfig.getProjectWorkflowEventWorkflowNameFieldPath(), wfModelName);
		pathValueMap.put(pathConfig.getProjectWorkflowEventWorkflowLabelFieldPath(), wfModelLabel);
		pathValueMap.put(
			pathConfig.getProjectWorkflowEventWorkflowInstanceKeyFieldPath(),
			processInstancePK);
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
		ProcessInstance masterProcessInstance = getMasterProcessInstance(wfEngine, processInstance);
		pathValueMap.put(
			pathConfig.getProjectWorkflowEventMasterWorkflowInstanceKeyFieldPath(),
			convertProcessInstanceKeyToPK(masterProcessInstance.getProcessInstanceKey()));
	}

	private static ProcessInstance getMasterProcessInstance(
		WorkflowEngine workflowEngine,
		ProcessInstance processInstance)
	{
		ProcessInstanceKey parentKey = processInstance.getParentKey();
		if (parentKey == null)
		{
			return processInstance;
		}
		ProcessInstance parent = workflowEngine.getProcessInstance(parentKey);
		if (parent == null)
		{
			return null;
		}
		return getMasterProcessInstance(workflowEngine, parent);
	}

	private void addWorkItemEventValues(
		Map<Path, Object> pathValueMap,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance,
		WorkItem workItem,
		Integer stepId,
		Repository repo,
		Session session)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();

		pathValueMap.put(pathConfig.getProjectWorkflowEventWorkItemStepIdFieldPath(), stepId);
		pathValueMap.put(
			pathConfig.getProjectWorkflowEventWorkItemLabelFieldPath(),
			workItem.getLabel().formatMessage(session.getLocale()));
		String workItemKeyStr = workItem.getWorkItemKey().toStringForLog();
		pathValueMap
			.put(pathConfig.getProjectWorkflowEventWorkItemInstanceKeyFieldPath(), workItemKeyStr);
		Role offeredToRole = WorkflowUtilities.getWorkItemOfferedToRole(workItem);
		if (offeredToRole != null)
		{
			pathValueMap.put(
				pathConfig.getProjectWorkflowEventWorkItemRoleFieldPath(),
				offeredToRole.getRoleName());
		}
		UserReference user = workItem.getUserReference();
		if (user != null)
		{
			pathValueMap
				.put(pathConfig.getProjectWorkflowEventWorkItemUserFieldPath(), user.getUserId());
		}
	}

	// For some reason when you call format(), it constructs the string backwards from the pk
	private static String convertProcessInstanceKeyToPK(ProcessInstanceKey processInstanceKey)
	{
		String keyStr = processInstanceKey.format();
		int firstSepInd = keyStr.indexOf(PrimaryKey.SEPARATOR_CHAR);
		return keyStr.substring(firstSepInd + 1) + PrimaryKey.SEPARATOR_CHAR
			+ keyStr.substring(0, firstSepInd);
	}

	// Ideally we'd have a way to get the model name from the context, but since we don't,
	// we can derive it from the primary key
	private static final String getWorkflowModelNameFromProcessInstanceKey(String processInstancePK)
	{
		// model name is the middle section
		return processInstancePK.substring(
			processInstancePK.indexOf(PrimaryKey.SEPARATOR_CHAR) + 1,
			processInstancePK.lastIndexOf(PrimaryKey.SEPARATOR_CHAR));
	}

	// Ideally we'd have a way to look this up from the context, but since we don't,
	// we will look up the workflow model in the workflow models data space. This is not technically
	// supported by the API.
	private static String lookupWorkflowModelLabel(
		String workflowModelName,
		Repository repo,
		Locale locale)
	{
		AdaptationHome wfDataSpace = AdminUtil.getWorkflowModelsDataSpace(repo);
		Adaptation wfDataSet = wfDataSpace
			.findAdaptationOrNull(AdaptationName.forName(workflowModelName));
		return wfDataSet == null ? null : wfDataSet.getLabel(locale);
	}

	@Override
	public void handleBeforeProcessInstanceTermination(
		WorkflowTriggerBeforeProcessInstanceTerminationContext context)
		throws OperationException
	{
		final ProcessInstance processInstance = context.getProcessInstance();
		// Clear the cache for this workflow when it's terminated
		ProjectWorkflowPermissionsCache.getInstance().clearCache(processInstance);

		Repository repo = context.getRepository();
		final Session session = context.getSession();
		// If this is a subworkflow, refresh cache for its parent workflow so that it will no longer
		// contain the key for this subworkflow
		if (processInstance.isSubWorkflow())
		{
			WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
			ProcessInstance parentWorkflow = wfEngine
				.getProcessInstance(processInstance.getParentKey());
			ProjectWorkflowPermissionsCache.getInstance()
				.refreshCache(
					parentWorkflow,
					session,
					repo,
					null,
					processInstance.getProcessInstanceKey(),
					getPermissionsInfoFactory());
		}

		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;

			AdaptationHome workingDataSpace = null;

			// Workflows should set workingDataSpace back to the master after merging or closing.
			// We don't want to error out, for backwards compatibility with existing workflows that
			// don't have that step
			// and if an admin manually closes a data space and terminates its workflow.
			// So we'll log a warning instead.
			try
			{
				workingDataSpace = WorkflowUtilities
					.getDataSpace(dataContext, repo, WorkflowConstants.PARAM_WORKING_DATA_SPACE);
			}
			catch (OperationException ex)
			{
				// Do nothing, will be handled below
			}
			if (workingDataSpace == null || !workingDataSpace.isOpen())
			{
				String workingDataSpaceName = dataContext
					.getVariableString(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
				if (workingDataSpaceName == null)
				{
					LoggingCategory.getWorkflow()
						.warn(
							"Working data space of workflow "
								+ processInstance.getLabel()
									.formatMessage(context.getSession().getLocale())
								+ " is not specified. Can't write termination events to workflow events table.");
				}
				else
				{
					LoggingCategory.getWorkflow()
						.warn(
							"Working data space " + workingDataSpaceName + " of workflow "
								+ processInstance.getLabel()
									.formatMessage(context.getSession().getLocale())
								+ " is not found or closed. Can't write termination events to workflow events table.");
				}
			}
			else
			{
				Procedure proc = new Procedure()
				{
					@Override
					public void execute(ProcedureContext pContext) throws Exception
					{
						Adaptation projectRecord = ProjectWorkflowUtilities.getProjectRecord(
							dataContext,
							session.getInteraction(true),
							pContext.getAdaptationHome().getRepository(),
							getProjectPathConfig(),
							WorkflowConstants.PARAM_WORKING_DATA_SPACE,
							false);
						// If there's no project record then it must have been canceled before
						// creating the project or
						// before merging it to master.
						// If it's the master workflow, delete it, which will delete its child
						// records.
						// If it's a subworkflow, ignore because when the master ends it will clean
						// it up.
						if (projectRecord == null)
						{
							if (processInstance.getParentKey() == null)
							{
								deleteMasterWorkflowEvent(pContext, dataContext, processInstance);
							}
						}
						// Otherwise we're keeping the workflow event record so end it
						else
						{
							endWorkflowEvent(
								pContext,
								dataContext,
								processInstance,
								null,
								null,
								null);
						}
					}
				};
				try
				{
					ProcedureExecutor.executeProcedure(proc, session, workingDataSpace);
				}
				catch (Exception ex)
				{
					LoggingCategory.getWorkflow()
						.error(
							"Error happened while writing workflow event upon termination of workflow "
								+ processInstance.getLabel()
									.formatMessage(context.getSession().getLocale())
								+ ". Workflow allowed to be terminated.",
							ex);
					// Deliberately squashing exceptions because it's more important to allow workflow to be terminated
					// than cancel it due to not being able to write the workflow event for some reason.
				}
			}
		}
	}
	private void deleteMasterWorkflowEvent(
		ProcedureContext pContext,
		DataContextReadOnly dataContext,
		ProcessInstance processInstance)
		throws OperationException
	{
		List<Adaptation> workflowEventRecords = lookupAllWorkflowEventRecordsForMasterWorkflow(
			dataContext,
			processInstance,
			pContext.getAdaptationHome().getRepository());
		for (Adaptation workflowEventRecord : workflowEventRecords)
		{
			Delete.execute(pContext, workflowEventRecord);
		}
	}

	@Override
	public void handleBeforeWorkItemAllocation(
		WorkflowTriggerBeforeWorkItemAllocationContext context)
		throws OperationException
	{
		handleAllocationOrReallocation(context);
	}

	private void handleAllocationOrReallocation(WorkflowTriggerWorkItemAllocationContext context)
		throws OperationException
	{
		Set<UserReference> users = new HashSet<>();
		final UserReference user = context.getUserReference();
		users.add(user);
		Session session = context.getSession();
		Repository repo = context.getRepository();
		final ProcessInstance processInstance = context.getProcessInstance();
		ProjectWorkflowPermissionsCache.getInstance()
			.refreshCache(processInstance, session, repo, users, null, getPermissionsInfoFactory());
		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;
			final WorkItem workItem = context.getWorkItem();
			final Integer stepId = Integer.valueOf(context.getCurrentStepId());

			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					updateWorkflowEventUser(
						pContext,
						dataContext,
						processInstance,
						workItem,
						stepId,
						user);
				}
			};
			ProcedureExecutor.executeProcedure(
				proc,
				context.getSession(),
				lookupWorkflowEventDataSpace(dataContext, context.getRepository()));
		}
	}

	@Override
	public void handleBeforeWorkItemDeallocation(
		WorkflowTriggerBeforeWorkItemDeallocationContext context)
		throws OperationException
	{
		Session session = context.getSession();
		Repository repo = context.getRepository();
		final ProcessInstance processInstance = context.getProcessInstance();
		ProjectWorkflowPermissionsCache.getInstance()
			.refreshCache(processInstance, session, repo, null, null, getPermissionsInfoFactory());
		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;
			final WorkItem workItem = context.getWorkItem();
			final Integer stepId = Integer.valueOf(context.getCurrentStepId());
			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					updateWorkflowEventUser(
						pContext,
						dataContext,
						processInstance,
						workItem,
						stepId,
						null);
				}
			};
			ProcedureExecutor
				.executeProcedure(proc, session, lookupWorkflowEventDataSpace(dataContext, repo));
		}
	}

	@Override
	public void handleBeforeWorkItemReallocation(
		WorkflowTriggerBeforeWorkItemReallocationContext context)
		throws OperationException
	{
		handleAllocationOrReallocation(context);
	}

	@Override
	public void handleBeforeWorkItemStart(WorkflowTriggerBeforeWorkItemStartContext context)
		throws OperationException
	{
		HashSet<UserReference> users = null;
		final UserReference workItemUser = context.getWorkItem().getUserReference();
		if (workItemUser != null)
		{
			users = new HashSet<>();
			users.add(workItemUser);
		}
		Session session = context.getSession();
		Repository repo = context.getRepository();
		final ProcessInstance processInstance = context.getProcessInstance();
		ProjectWorkflowPermissionsCache.getInstance()
			.refreshCache(processInstance, session, repo, users, null, getPermissionsInfoFactory());
		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;
			final WorkItem workItem = context.getWorkItem();
			final Integer stepId = Integer.valueOf(context.getCurrentStepId());

			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					// beforeStart can get called before afterCreation, so it's possible that the
					// record already was created
					// This is handled by createWorkflowEvent also, but best to capture it here
					// since it's an expected situation
					// due to nature of trigger events
					Adaptation record = lookupWorkItemWorkflowEventRecord(
						dataContext,
						workItem,
						pContext.getAdaptationHome().getRepository());
					if (record == null)
					{
						createWorkflowEvent(
							pContext,
							dataContext,
							processInstance,
							workItem,
							stepId,
							new Date());
					}
					else
					{
						updateWorkflowEventUser(
							pContext,
							dataContext,
							processInstance,
							workItem,
							stepId,
							workItemUser);
					}
				}

			};
			ProcedureExecutor
				.executeProcedure(proc, session, lookupWorkflowEventDataSpace(dataContext, repo));
		}
	}

	@Override
	public void handleBeforeWorkItemTermination(
		WorkflowTriggerBeforeWorkItemTerminationContext context)
		throws OperationException
	{
		if (isTrackingWorkflowEvents())
		{
			final DataContextReadOnly dataContext = context;
			final ProcessInstance processInstance = context.getProcessInstance();
			final WorkItem workItem = context.getWorkItem();
			final Integer stepId = Integer.valueOf(context.getCurrentStepId());
			final Boolean isAccepted = Boolean.valueOf(context.isAccepted());

			Procedure proc = new Procedure()
			{
				@Override
				public void execute(ProcedureContext pContext) throws Exception
				{
					endWorkflowEvent(
						pContext,
						dataContext,
						processInstance,
						workItem,
						stepId,
						isAccepted);
				}
			};
			ProcedureExecutor.executeProcedure(
				proc,
				context.getSession(),
				lookupWorkflowEventDataSpace(dataContext, context.getRepository()));
		}
	}

	private void updateWorkflowEventsWithProject(
		ProcedureContext pContext,
		AdaptationTable workflowEventTable,
		String projectPK,
		String masterWorkflowInstancePK)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		if (projectPK != null)
		{
			Path projectFieldPath = pathConfig.getProjectWorkflowEventProjectFieldPath();
			RequestResult reqRes = workflowEventTable.createRequestResult(
				"osd:is-null(" + projectFieldPath.format() + ") and "
					+ pathConfig.getProjectWorkflowEventMasterWorkflowInstanceKeyFieldPath()
						.format()
					+ "='" + masterWorkflowInstancePK + "'");
			try
			{
				Map<Path, Object> pathValueMap = new HashMap<>();
				pathValueMap.put(projectFieldPath, projectPK);

				Adaptation record;
				while ((record = reqRes.nextAdaptation()) != null)
				{
					Modify.execute(pContext, record, pathValueMap);
				}
			}
			finally
			{
				reqRes.close();
			}
		}
	}
}
