package com.orchestranetworks.ps.workflow;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.interactions.InteractionHelper.*;
import com.orchestranetworks.ps.accessrule.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.workflow.*;
import com.orchestranetworks.workflow.UserTask.*;
import com.orchestranetworks.workflow.impl.execution.*;

/**
 * Methods that help interact with workflow data context values
 */
public class WorkflowUtilities
{
	private static final boolean USE_CACHE = isSessionInteractionCacheUsed();
	// This is a WeakHashMap, which means if it's the only reference left to the key, it will be
	// freed up for the garbage collector.
	// The key is a SessionInteraction, so this means if its session is cleared out by EBX, it will
	// be removed from this map.
	// The value is a Map itself with key = record xpath and value = an object pairing a table path
	// with a primary key.
	// This way, we can look up a record by xpath and store the info needed to do a direct pk look
	// up next time it's needed within the same session interaction.
	// (A look up by pk is faster than by xpath.)
	// Note that WeakHashMaps must be declared as such (not as Map) or else they won't work
	// correctly as WeakHashMaps.
	private static final WeakHashMap<SessionInteraction, Map<String, TablePathPrimaryKeyPair>> sessionRecordPKMap = new WeakHashMap<>();

	private static final String ROLE_PERMISSIONS_USER = "Permissions User";

	private static final String PROP_USE_SESSION_INTERACTION_CACHE = "useSessionInteractionCache";
	private static final String PROP_IGNORE_COMPLETION_CRITERIA = "ignoreCompletionCriteria";

	/**
	 * @deprecated Use {@link getRecord(DataContextReadOnly, SessionInteraction, Repository)} instead.
	 *             If you don't have a SessionInteraction, then just pass <code>null</code>.
	 *             For example, there's no SessionInteraction when in UserTask.handleCreate, UserTask.handleWorkItemCompletion, a ScriptTask, or a Condition.
	 */
	@Deprecated
	public static Adaptation getRecord(DataContextReadOnly dataContext, Repository repo)
		throws OperationException
	{
		return getRecord(
			dataContext,
			null,
			repo,
			WorkflowConstants.PARAM_RECORD,
			WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	/**
	 * Get a record from the data context.
	 * This is equivalent to calling {@link getRecord(DataContextReadOnly, SessionInteraction, Repository, String, String)} for the param
	 * <code>PARAM_RECORD</code> and data space <code>PARAM_WORKING_DATA_SPACE</code>.
	 *
	 * The record is retrieved from the data context. The session interaction is optional but if not null, and using the session interaction cache,
	 * then the record will be retrieved using the primary key that is stored in the cache for the record xpath.
	 * It is recommended to pass the session interaction in if you have one so that if cache is turned on, it can be taken advantage of.
	 *
	 * @param dataContext the data context
	 * @param interaction the session interaction
	 * @param repo the repository
	 * @throws OperationException if an error occurs
	 */
	public static Adaptation getRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo)
		throws OperationException
	{
		return getRecord(
			dataContext,
			interaction,
			repo,
			WorkflowConstants.PARAM_RECORD,
			WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	/**
	 * @deprecated Use {@link getRecord(DataContextReadOnly, SessionInteraction, Repository, String, String)} instead.
	 *             If you don't have a SessionInteraction, then just pass <code>null</code>.
	 *             For example, there's no SessionInteraction when in UserTask.handleCreate, UserTask.handleWorkItemCompletion, a ScriptTask, or a Condition.
	 */
	@Deprecated
	public static Adaptation getRecord(
		DataContextReadOnly dataContext,
		Repository repo,
		String recordParamName,
		String dataSpaceParam)
		throws OperationException
	{
		return getRecord(dataContext, null, repo, recordParamName, dataSpaceParam, true);
	}

	/**
	 * Get a record from the data context.
	 * This is equivalent to calling {@link getRecord(DataContextReadOnly, SessionInteraction, Repository, String, String, boolean)}
	 * with <errorIfNotFound> set to <code>true</code>.
	 *
	 * The record is retrieved from the data context. The session interaction is optional but if not null, and using the session interaction cache,
	 * then the record will be retrieved using the primary key that is stored in the cache for the record xpath.
	 * It is recommended to pass the session interaction in if you have one so that if cache is turned on, it can be taken advantage of.
	 *
	 * @param dataContext the data context
	 * @param interaction the session interaction
	 * @param repo the repository
	 * @param recordParamName the name of the data context parameter for the record
	 * @param dataSpaceParam the name of the data context parameter for the data space
	 * @throws OperationException if an error occurs
	 */
	public static Adaptation getRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		String recordParamName,
		String dataSpaceParam)
		throws OperationException
	{
		return getRecord(dataContext, interaction, repo, recordParamName, dataSpaceParam, true);
	}

	/**
	 * @deprecated Use {@link getRecord(DataContextReadOnly, SessionInteraction, Repository, String, String, boolean)} instead.
	 *             If you don't have a SessionInteraction, then just pass <code>null</code>.
	 *             For example, there's no SessionInteraction when in UserTask.handleCreate, UserTask.handleWorkItemCompletion, a ScriptTask, or a Condition.
	 */
	@Deprecated
	public static Adaptation getRecord(
		DataContextReadOnly dataContext,
		Repository repo,
		String recordParamName,
		String dataSpaceParam,
		boolean errorIfNotFound)
		throws OperationException
	{
		return getRecord(dataContext, null, repo, recordParamName, dataSpaceParam, errorIfNotFound);
	}

	/**
	 * Get a record from the data context.
	 *
	 * The record is retrieved from the data context. The session interaction is optional but if not null, and using the session interaction cache,
	 * then the record will be retrieved using the primary key that is stored in the cache for the record xpath.
	 * It is recommended to pass the session interaction in if you have one so that if cache is turned on, it can be taken advantage of.
	 *
	 * @param dataContext the data context
	 * @param interaction the session interaction
	 * @param repo the repository
	 * @param recordParamName the name of the data context parameter for the record
	 * @param dataSpaceParam the name of the data context parameter for the data space
	 * @param errorIfNotFound whether to throw an error if the record is not found
	 * @throws OperationException if an error occurs
	 */
	public static Adaptation getRecord(
		DataContextReadOnly dataContext,
		SessionInteraction interaction,
		Repository repo,
		String recordParamName,
		String dataSpaceParam,
		boolean errorIfNotFound)
		throws OperationException
	{
		String recordXPath = dataContext.getVariableString(recordParamName);
		Adaptation dataSet = getDataSet(dataContext, repo, dataSpaceParam);
		if (USE_CACHE && interaction != null)
		{
			return getRecordFromSessionInteractionCache(
				interaction,
				recordXPath,
				dataSet,
				errorIfNotFound);
		}
		return AdaptationUtil.getRecord(recordXPath, dataSet, true, errorIfNotFound);
	}

	/**
	 * Get the record from a workflow service, which uses a session interaction.
	 * This assumes the standard EBX parameters to workflow services.
	 *
	 * @param interaction the session interaction which contains the parameters for the workflow service
	 * @param repo the repository
	 * @return the record
	 * @throws OperationException if an error occurs retrieving the record
	 */
	public static Adaptation getRecordFromSessionInteraction(
		SessionInteraction interaction,
		Repository repo)
		throws OperationException
	{
		return getRecordFromSessionInteraction(
			interaction,
			getDataSetFromSessionInteraction(interaction, repo));
	}

	/**
	 * Get the record from a workflow service, which uses a session interaction.
	 * This uses the data set passed in instead of the standard EBX parameters to workflow services.
	 *
	 * @param interaction the session interaction which contains the parameters for the workflow service
	 * @param dataSet the data set
	 * @param useCache whether to use a cache to store/retrieve the records
	 * @return the record
	 * @throws OperationException if an error occurs retrieving the record
	 */
	public static Adaptation getRecordFromSessionInteraction(
		SessionInteraction interaction,
		Adaptation dataSet)
		throws OperationException
	{
		String recordXPath = null;
		// First try to get it from the record being created, for a create step
		if (ServiceKey.CREATE.equals(interaction.getServiceKey()))
		{
			ParametersMap internalParamMap = interaction.getInternalParameters();
			if (internalParamMap != null)
			{
				recordXPath = internalParamMap
					.getVariableString(WorkflowConstants.SESSION_PARAM_CREATED);
			}
		}
		// If null, it might not be a create step, or created yet, so get it from the input param
		if (recordXPath == null)
		{
			ParametersMap inputParamMap = interaction.getInputParameters();
			if (inputParamMap != null)
			{
				// Custom services can use a special record param so check that next
				String xpath = inputParamMap
					.getVariableString(WorkflowConstants.SESSION_PARAM_WORKFLOW_RECORD);
				if (isXPathForRecord(xpath))
				{
					recordXPath = xpath;
				}
				// Else use the normal xpath param
				else
				{
					xpath = inputParamMap.getVariableString(WorkflowConstants.SESSION_PARAM_XPATH);
					if (isXPathForRecord(xpath))
					{
						recordXPath = xpath;
					}
				}
			}
		}
		if (recordXPath == null)
		{
			return null;
		}
		if (USE_CACHE)
		{
			return getRecordFromSessionInteractionCache(interaction, recordXPath, dataSet, true);
		}
		return AdaptationUtil.getRecord(recordXPath, dataSet, true, true);
	}

	public static void clearSessionInteractionCache()
	{
		if (USE_CACHE)
		{
			sessionRecordPKMap.clear();
		}
	}

	// Get the primary key of the record represented by the given xpath from the cache, look up the
	// record,
	// and store it in the cache if it wasn't there already.
	private static Adaptation getRecordFromSessionInteractionCache(
		SessionInteraction interaction,
		String recordXPath,
		Adaptation dataSet,
		boolean errorIfNotFound)
		throws OperationException
	{
		Map<String, TablePathPrimaryKeyPair> xpathPKMap = sessionRecordPKMap.get(interaction);
		// If there's nothing cached for this session interaction yet
		if (xpathPKMap == null)
		{
			// Create a new map of xpath & table path / pk pair
			xpathPKMap = new HashMap<>();
			// Put it in the outer map
			synchronized (sessionRecordPKMap)
			{
				sessionRecordPKMap.put(interaction, xpathPKMap);
			}
		}

		// Get the table path / pk pair from the cached map
		TablePathPrimaryKeyPair pair = xpathPKMap.get(recordXPath);
		// If no table path / pk pair was cached yet for this xpath
		if (pair == null)
		{
			// Look up the record by xpath
			Adaptation record = AdaptationUtil
				.getRecord(recordXPath, dataSet, true, errorIfNotFound);
			// If the record is found
			if (record != null)
			{
				// Create a new table path / pk pair and put it in the map, associating it to the
				// xpath
				pair = new TablePathPrimaryKeyPair(
					record.getContainerTable().getTablePath(),
					record.getOccurrencePrimaryKey());
				synchronized (xpathPKMap)
				{
					xpathPKMap.put(recordXPath, pair);
				}
			}
			// Return the record that was found
			return record;
		}

		// Otherwise the table path / pk pair was already cached so look up the record by pk
		AdaptationTable table = dataSet.getTable(pair.getTablePath());
		return table.lookupAdaptationByPrimaryKey(pair.getPrimaryKey());
	}

	/**
	 * Get the value of the given parameter from the session interaction,
	 * by first looking in the internal parameter map (updated by the current workflow step),
	 * and if not found there, by looking in the input parameter map.
	 * 
	 * Will return <code>null</code> if not found in either, or if the session is not currently in a workflow step.
	 * 
	 * @param session the session
	 * @param parameterName the parameter name
	 * @return the value of the parameter, or <code>null</code>
	 */
	public static String getSessionInteractionParameter(Session session, String parameterName)
	{
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction == null)
		{
			// Not in a workflow
			return null;
		}
		String value = null;
		// Look through both internal params & input params, in that order, until a value is found.
		// Note that this is different than saying until the value isn't null, because a param
		// with a value in the input parameters map could be overridden with a null value in the
		// internal parameters map, and in that case we'd want to return null.
		ParametersMap[] maps = { interaction.getInternalParameters(),
				interaction.getInputParameters() };
		boolean found = false;
		for (int i = 0; !found && i < maps.length; i++)
		{
			ParametersMap map = maps[i];
			// If the parameter name is defined in the map then we found it
			// and will want to return its value (even if it's null)
			if (map != null && isSessionInteractionParameterDefined(map, parameterName))
			{
				value = map.getVariableString(parameterName);
				found = true;
			}
		}
		return value;
	}

	/**
	 * This returns the session interaction's internal parameters unless they are <code>null</code>,
	 * in which case it returns the session interaction's input parameters.
	 * 
	 * @param session the session
	 * @return the internal or input parameter map
	 * @deprecated This isn't used internally by ps library any longer and seems of limited use.
	 *             Typically you'd want to search for a parameter in both maps, not return the
	 *             input parameters map only if there's no internal parameters map.
	 */
	@Deprecated
	public static ParametersMap getSessionInteractionInternalParameters(Session session)
	{
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction == null)
		{
			// Not in a workflow
			return null;
		}
		ParametersMap map = interaction.getInternalParameters();
		// If the internal parameter map has not yet been set, then return the input parameter map
		if (map == null)
		{
			map = interaction.getInputParameters();
		}
		return map;
	}

	/**
	 * Determine if the given parameter is defined as a parameter of the session's interaction
	 * by first checking the internal parameters map, then the input parameters map.
	 * This doesn't necessarily mean the value isn't <code>null</code>, just that it's defined.
	 * 
	 * @param session the session
	 * @param parameterName the parameter name
	 * @return whether it's defined
	 */
	public static boolean isSessionInteractionParameterDefined(
		Session session,
		String parameterName)
	{
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction == null)
		{
			// Execution is not part of a workflow
			return false;
		}
		if (isSessionInteractionParameterDefined(
			interaction.getInternalParameters(),
			parameterName))
		{
			return true;
		}
		return isSessionInteractionParameterDefined(
			interaction.getInputParameters(),
			parameterName);
	}

	// Get whether the given parameter is defined in the specific map passed in
	private static boolean isSessionInteractionParameterDefined(
		ParametersMap parametersMap,
		String parameterName)
	{
		if (parametersMap == null)
		{
			return false;
		}
		Iterator<String> iter = parametersMap.getVariableNames();
		boolean found = false;
		// Only way to tell if a parameter is defined is to loop through its iterator until we find it
		while (!found && iter.hasNext())
		{
			if (parameterName.equals(iter.next()))
			{
				found = true;
			}
		}
		return found;
	}

	/**
	 * Set the value of a parameter into the internal parameters map of the session's interaction.
	 * If the internal parameters map doesn't exist yet, create it.
	 * 
	 * @see SessionInteraction#setInternalParameters(ParametersMap)
	 * @see ParametersMap#setVariableString(String, String)
	 * 
	 * @param session the session
	 * @param parameterName the parameter name
	 * @param value the value to set
	 * @throws IllegalStateException if there is no interaction for the given session
	 */
	public static void setSessionInteractionInternalParameter(
		Session session,
		String parameterName,
		String value)
	{
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction == null)
		{
			throw new IllegalStateException(
				"Cannot set internal parameter " + parameterName
					+ " because session has no interaction.");
		}
		ParametersMap map = interaction.getInternalParameters();
		if (map == null)
		{
			map = new ParametersMap();
		}
		map.setVariableString(parameterName, value);
		// Note that you must call this AFTER setting the variable.
		// i.e. Just updating the map variable afterwards won't be sufficient,
		// so there must be some logic in this method that tells EBX to store the values.
		interaction.setInternalParameters(map);
	}

	public static boolean isXPathForRecord(String xpath)
	{
		return xpath != null && xpath.contains("[");
	}

	public static AdaptationTable getAdaptationTable(
		DataContextReadOnly dataContext,
		Repository repo,
		String dataSpaceParam)
		throws OperationException
	{
		final String xpathToTable = dataContext
			.getVariableString(WorkflowConstants.PARAM_XPATH_TO_TABLE);
		if (xpathToTable == null)
		{
			return null;
		}
		return getAdaptationTable(dataContext, repo, dataSpaceParam, Path.parse(xpathToTable));
	}

	public static AdaptationTable getAdaptationTable(
		DataContextReadOnly dataContext,
		Repository repo,
		String dataSpaceParam,
		Path tablePath)
		throws OperationException
	{
		Adaptation currentDataSet = getDataSet(dataContext, repo, dataSpaceParam);
		if (tablePath == null)
		{
			return null;
		}
		// get table adaptation
		return currentDataSet.getTable(tablePath);
	}

	/**
	 * Get the data space from the data context
	 *
	 * @param dataContext the data context
	 * @param repo the repository
	 * @param dataSpaceParam the data space data context parameter
	 * @return the data space
	 * @throws OperationException if an error occurs retrieving the data space,
	 *         or if the data space can't be found
	 */
	public static AdaptationHome getDataSpace(
		DataContextReadOnly dataContext,
		Repository repo,
		String dataSpaceParam)
		throws OperationException
	{
		String dataSpaceId = dataContext.getVariableString(dataSpaceParam);

		AdaptationHome home = repo.lookupHome(HomeKey.forBranchName(dataSpaceId));
		if (home == null)
		{
			throw OperationException
				.createError("DataSpace " + dataSpaceId + " has not been found");
		}

		return home;
	}

	/**
	 * Get the data space from a workflow service, which uses a session interaction
	 *
	 * @param interaction the session interaction which contains the parameters for the workflow service
	 * @param repo the repository
	 * @return the data space
	 * @throws OperationException if an error occurs retrieving the data space,
	 *         or if the data space can't be found
	 */
	public static AdaptationHome getDataSpaceFromSessionInteraction(
		SessionInteraction interaction,
		Repository repo)
		throws OperationException
	{
		ParametersMap paramMap = interaction.getInputParameters();
		if (paramMap == null)
		{
			throw OperationException.createError("No input parameters found for session");
		}
		// Custom services can use a special data space param so check that first
		String dataSpaceId = paramMap
			.getVariableString(WorkflowConstants.SESSION_PARAM_WORKFLOW_DATA_SPACE);
		if (dataSpaceId == null)
		{
			dataSpaceId = paramMap.getVariableString(WorkflowConstants.SESSION_PARAM_DATA_SPACE);
			if (dataSpaceId == null)
			{
				dataSpaceId = paramMap.getVariableString(WorkflowConstants.SESSION_PARAM_SNAPSHOT);
				if (dataSpaceId == null)
				{
					throw OperationException.createError(
						"Could not find input parameter "
							+ WorkflowConstants.SESSION_PARAM_WORKFLOW_DATA_SPACE + " or "
							+ WorkflowConstants.SESSION_PARAM_DATA_SPACE + " or "
							+ WorkflowConstants.SESSION_PARAM_SNAPSHOT + ".");
				}
			}
		}

		AdaptationHome home = repo.lookupHome(HomeKey.forBranchName(dataSpaceId));
		if (home == null)
		{
			throw OperationException
				.createError("DataSpace " + dataSpaceId + " has not been found");
		}

		return home;
	}

	/**
	 * Get the data set from the data context
	 *
	 * @param dataContext the data context
	 * @param repo the repository
	 * @param dataSpaceParam the data space parameter
	 * @return the data set
	 * @throws OperationException if an error occurs retrieving the data set,
	 *         or if the data set can't be found
	 */
	public static Adaptation getDataSet(
		DataContextReadOnly dataContext,
		Repository repo,
		String dataSpaceParam)
		throws OperationException
	{
		AdaptationHome home = getDataSpace(dataContext, repo, dataSpaceParam);
		String dataSet = dataContext.getVariableString(WorkflowConstants.PARAM_DATA_SET);
		Adaptation currentDataSet = home.findAdaptationOrNull(AdaptationName.forName(dataSet));
		if (currentDataSet == null)
		{
			throw OperationException.createError(dataSet + " DataSet has not been found");
		}

		return currentDataSet;
	}

	/**
	 * Get the data set from a workflow service, which uses a session interaction
	 *
	 * @param interaction the session interaction which contains the parameters for the workflow service
	 * @param repo the repository
	 * @return the data set
	 * @throws OperationException if an error occurs retrieving the data set,
	 *         or if the data set can't be found
	 */
	public static Adaptation getDataSetFromSessionInteraction(
		SessionInteraction interaction,
		Repository repo)
		throws OperationException
	{
		AdaptationHome home = getDataSpaceFromSessionInteraction(interaction, repo);
		ParametersMap paramMap = interaction.getInputParameters();
		if (paramMap == null)
		{
			throw OperationException.createError("No input parameters found for session");
		}
		// Custom services can use a special data set param so check that first
		String dataSetName = paramMap
			.getVariableString(WorkflowConstants.SESSION_PARAM_WORKFLOW_DATA_SET);
		if (dataSetName == null)
		{
			dataSetName = paramMap.getVariableString(WorkflowConstants.SESSION_PARAM_DATA_SET);
			if (dataSetName == null)
			{
				throw OperationException.createError(
					"Could not find input parameter "
						+ WorkflowConstants.SESSION_PARAM_WORKFLOW_DATA_SET + " or "
						+ WorkflowConstants.SESSION_PARAM_DATA_SET + ".");
			}
		}
		Adaptation dataSet = home.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		if (dataSet == null)
		{
			throw OperationException.createError(dataSet + " DataSet has not been found");
		}

		return dataSet;
	}

	public static UserReference getCurrentUserReference(
		DataContextReadOnly dataContext,
		Repository repo)
		throws OperationException
	{
		return getDesiredUserReference(dataContext, repo, WorkflowConstants.PARAM_CURRENT_USER_ID);
	}

	public static UserReference getCurrentApproverReference(
		DataContextReadOnly dataContext,
		Repository repo)
		throws OperationException
	{
		return getDesiredUserReference(
			dataContext,
			repo,
			WorkflowConstants.PARAM_CURRENT_APPROVER_ID);
	}

	public static UserReference getDesiredUserReference(
		DataContextReadOnly dataContext,
		Repository repo,
		String paramUserId)
		throws OperationException
	{
		if (!dataContext.isVariableDefined(paramUserId))
		{
			return null;
		}
		String currentUserid = dataContext.getVariableString(paramUserId);
		if (currentUserid == null)
		{
			return null;
		}
		return getUserReference(currentUserid, repo);
	}

	public static UserReference getUserReference(String userid, Repository repo)
		throws OperationException
	{
		if (userid == null)
		{
			return null;
		}
		UserReference userReference = UserReference.forUser(userid);
		DirectoryHandler directoryHandler = DirectoryHandler.getInstance(repo);
		if (!directoryHandler.isUserDefined(userReference))
		{
			return null;
		}
		return userReference;
	}

	public static void setWorkflowInstanceCreateDateTime(
		DataContext dataContext,
		ProcessExecutionContext processContext)
	{
		if (dataContext
			.isVariableDefined(WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME))
		{
			String workflowInstanceCreateDateTime = dataContext
				.getVariableString(WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME);
			if (workflowInstanceCreateDateTime == null || workflowInstanceCreateDateTime.isEmpty()
				|| workflowInstanceCreateDateTime.equals(WorkflowConstants.DATA_CONTEXT_NULL_VALUE))
			{
				workflowInstanceCreateDateTime = (new SimpleDateFormat(
					WorkflowConstants.DATA_CONTEXT_DATE_TIME_FORMAT_STRING))
						.format(processContext.getProcessInstance().getCreationDate());
				dataContext.setVariableString(
					WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME,
					workflowInstanceCreateDateTime);
			}
		}
		//Populating another data context variable which could be used as datetime filter criteria
		if (dataContext.isVariableDefined(
			WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME_UNFORMATTED))
		{
			String workflowInstanceCreateDateTimeUnformatted = dataContext.getVariableString(
				WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME_UNFORMATTED);
			if (workflowInstanceCreateDateTimeUnformatted == null
				|| workflowInstanceCreateDateTimeUnformatted.isEmpty()
				|| workflowInstanceCreateDateTimeUnformatted
					.equals(WorkflowConstants.DATA_CONTEXT_NULL_VALUE))
			{
				workflowInstanceCreateDateTimeUnformatted = (new SimpleDateFormat(
					WorkflowConstants.DATA_CONTEXT_DATE_TIME_FORMAT))
						.format(processContext.getProcessInstance().getCreationDate());
				dataContext.setVariableString(
					WorkflowConstants.PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME_UNFORMATTED,
					workflowInstanceCreateDateTimeUnformatted);
			}
		}

	}

	public static void setWorkflowCurrentUser(
		DataContext dataContext,
		ProcessExecutionContext processContext)
	{
		if (dataContext.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USER_ID))
		{
			String workflowCreator = dataContext
				.getVariableString(WorkflowConstants.PARAM_CURRENT_USER_ID);
			if (workflowCreator == null || workflowCreator.isEmpty())
			{
				UserReference user = processContext.getProcessInstanceCreator();
				workflowCreator = user.getUserId();
				dataContext
					.setVariableString(WorkflowConstants.PARAM_CURRENT_USER_ID, workflowCreator);
				if (dataContext.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USER_LABEL))
					dataContext.setVariableString(
						WorkflowConstants.PARAM_CURRENT_USER_LABEL,
						user.getLabel());
			}
		}
	}

	public static void setUserTaskCreateDateTime(DataContext context)
	{
		Calendar cal = Calendar.getInstance();
		String userTaskCreateDateTime = (new SimpleDateFormat(
			WorkflowConstants.DATA_CONTEXT_DATE_TIME_FORMAT_STRING)).format(cal.getTime());
		context.setVariableString(
			WorkflowConstants.PARAM_USER_TASK_CREATE_DATE_TIME,
			userTaskCreateDateTime);
	}

	public static void setCurrentUserIdAndLabel(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		// set the currentUserId variable in the Data Context and label if present
		String currentUserId = context.getCompletedWorkItem().getUserReference().getUserId();
		context.setVariableString(WorkflowConstants.PARAM_CURRENT_USER_ID, currentUserId);
		if (context.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USER_LABEL))
		{
			Session session = context.getSession();
			DirectoryHandler dirHandler = session.getDirectory();
			String currentUserLabel = dirHandler
				.displayUser(UserReference.forUser(currentUserId), session.getLocale());
			context.setVariableString(WorkflowConstants.PARAM_CURRENT_USER_LABEL, currentUserLabel);
		}
	}

	public static void setCurrentApprover(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		// set the currentApproverId variable in the Data Context, and label if present
		String currentUserId = context.getCompletedWorkItem().getUserReference().getUserId();
		context.setVariableString(WorkflowConstants.PARAM_CURRENT_APPROVER_ID, currentUserId);
		if (context.isVariableDefined(WorkflowConstants.PARAM_CURRENT_APPROVER_LABEL))
		{
			Session session = context.getSession();
			DirectoryHandler dirHandler = session.getDirectory();
			String currentUserLabel = dirHandler
				.displayUser(UserReference.forUser(currentUserId), session.getLocale());
			context.setVariableString(
				WorkflowConstants.PARAM_CURRENT_APPROVER_LABEL,
				currentUserLabel);
		}
	}

	/**
	 * Gets the permissions user from the tracking info, assuming it is the first segment of the tracking info
	 *
	 * @param session the session
	 * @return the user from the tracking info
	 */
	public static String getTrackingInfoPermissionsUser(Session session)
	{
		return getTrackingInfoPermissionsUser(
			session,
			new FirstSegmentTrackingInfoHelper(
				WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USERS));
	}

	/**
	 * Gets the permissions user from the tracking info. A tracking info helper is passed in that defines
	 * the structure of the tracking info. It must define a segment with key
	 * <code>WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USER</code>.
	 *
	 * @param session the session
	 * @param trackingInfoHelper the tracking info helper
	 * @return the user from the tracking info
	 */
	public static String getTrackingInfoPermissionsUser(
		Session session,
		TrackingInfoHelper trackingInfoHelper)
	{
		String trackingInfo = session.getTrackingInfo();
		if (trackingInfo == null)
		{
			return null;
		}
		trackingInfoHelper.initTrackingInfo(trackingInfo);
		return trackingInfoHelper
			.getTrackingInfoSegment(WorkflowAccessRule.SEGMENT_WORKFLOW_PERMISSIONS_USERS);
	}

	public static boolean isPermissionsUser(Session session)
	{
		return session.isUserInRole(Role.forSpecificRole(ROLE_PERMISSIONS_USER));
	}

	/**
	 * If you have a user session, it is preferable to call
	 * {@link #isPermissionsUser(Session)}
	 */
	public static boolean isPermissionsUser(Repository repository, UserReference user)
	{
		return DirectoryHandler.getInstance(repository)
			.isUserInRole(user, Role.forSpecificRole(ROLE_PERMISSIONS_USER));
	}

	// Returns subworkflows at every level below the given workflow (not just one level down)
	public static List<ProcessInstance> getCurrentSubWorkflows(ProcessInstance processInstance)
	{
		ArrayList<ProcessInstance> allSubworkflows = new ArrayList<>();
		List<ProcessInstance> subWorkflows = processInstance.getCurrentSubWorkflows();
		for (ProcessInstance subWorkflow : subWorkflows)
		{
			allSubworkflows.add(subWorkflow);
			allSubworkflows.addAll(getCurrentSubWorkflows(subWorkflow));
		}
		return allSubworkflows;
	}

	public static List<WorkItem> getCurrentWorkItems(ProcessInstance processInstance)
	{
		if (processInstance.isCompleted())
		{
			return new ArrayList<>();
		}

		List<WorkItem> workItems = processInstance.getWorkItems();
		// If the workflow has no work items currently running
		if (workItems.isEmpty())
		{
			ArrayList<WorkItem> allWorkItems = new ArrayList<>();
			// Loop through its currently running subworkflows.
			// (A workflow can't have both a running work item and a running subworkflow.)
			List<ProcessInstance> subWorkflows = processInstance.getCurrentSubWorkflows();
			for (ProcessInstance subWorkflow : subWorkflows)
			{
				// If there are any currently running work items in the subworkflow, then
				// add it to the list (will recurse through its subworkflows as well)ß
				List<WorkItem> subWorkflowWorkItems = getCurrentWorkItems(subWorkflow);
				if (!subWorkflowWorkItems.isEmpty())
				{
					allWorkItems.addAll(subWorkflowWorkItems);
				}
			}
			return allWorkItems;
		}
		return workItems;
	}

	public static Set<UserReference> getWorkItemUsers(List<WorkItem> workItems)
	{
		HashSet<UserReference> users = new HashSet<>();
		for (WorkItem workItem : workItems)
		{
			UserReference user = workItem.getUserReference();
			if (user != null)
			{
				users.add(user);
			}
		}
		return users;
	}

	// Determine if we should use the session interaction cache based on a Java environment property
	public static boolean isSessionInteractionCacheUsed()
	{
		String useCacheStr = System.getProperty(PROP_USE_SESSION_INTERACTION_CACHE);
		return useCacheStr != null && "true".equalsIgnoreCase(useCacheStr);
	}

	public static List<Role> getRolesForLookupTableUserTask(
		AdaptationTable workflowRoleTable,
		PrimaryKey[] primaryKeys,
		Path rolePath)
		throws OperationException
	{
		ArrayList<Role> roles = new ArrayList<>();
		for (PrimaryKey primaryKey : primaryKeys)
		{
			Adaptation record = workflowRoleTable.lookupAdaptationByPrimaryKey(primaryKey);
			if (record == null)
			{
				throw OperationException.createError(
					"No role found for workflow role with key '" + primaryKey.format() + "'");
			}
			String roleName = record.getString(rolePath);
			roles.add(Role.forSpecificRole(roleName));
		}
		return roles;
	}

	public static boolean isCompletionCriteriaIgnored()
	{
		String ignoreStr = System.getProperty(PROP_IGNORE_COMPLETION_CRITERIA);
		return ignoreStr != null && "true".equalsIgnoreCase(ignoreStr);
	}

	/**
	 * As of EBX 5.7, work items can be offered to multiple profiles.
	 * So far our code relies on each work item being offered to one and only one profile, which is a role.
	 * This method is used to retrieve that role, assuming the work item passed in conforms to that.
	 *
	 * @param workItem the work item
	 * @return the role
	 */
	public static Role getWorkItemOfferedToRole(WorkItem workItem)
	{
		Iterator<Profile> iterator = workItem.getOfferedToProfiles().iterator();
		return iterator.hasNext() ? (Role) iterator.next() : null;
	}

	// TODO: This method uses methods that aren't part of the public EBX API.
	// It is subject to be changed without backwards-compatibility with future versions of EBX.
	// Should replace code once we have an official way of accomplishing this via the API.
	public static WorkItem getWorkItemFromSession(Repository repo, Session session)
	{
		SessionInteraction si = session.getInteraction(true);
		if (si == null)
			return null;
		InteractionKey interactionKey = null;
		if (si instanceof SessionInteractionImpl)
			interactionKey = ((SessionInteractionImpl) si).getInteractionKey();
		if (interactionKey == null)
			return null;
		WorkflowEngine we = WorkflowEngine.getFromRepository(repo, session);
		List<WorkItem> workItems = we.getWorkItemsAllocatedToUser(session.getUserReference());
		for (WorkItem workItem : workItems)
		{
			if (workItem instanceof WorkItemImpl)
			{
				WorkItemImpl item = (WorkItemImpl) workItem;
				InteractionKey itemInteractionKey = item.getInteractionKey();
				if (interactionKey.equals(itemInteractionKey))
					return workItem;
			}
		}
		return null;
	}

	public static boolean isUserAssociatedWithProcessInstance(
		Session session,
		ProcessInstance processInstance)
	{
		UserReference user = session.getUserReference();
		if (user.equals(processInstance.getCreator()))
			return true;
		List<WorkItem> workitems = processInstance.getWorkItems();
		DirectoryHandler directory = session.getDirectory();
		for (WorkItem workItem : workitems)
		{
			if (isWorkItemForUser(user, workItem, directory))
				return true;
		}
		List<ProcessInstance> subPis = processInstance.getSubWorkflows();
		for (ProcessInstance subPi : subPis)
		{
			List<WorkItem> swis = subPi.getWorkItems();
			for (WorkItem workItem : swis)
			{
				if (isWorkItemForUser(user, workItem, directory))
					return true;
			}
		}
		return false;
	}

	public static boolean isUserAssociatedWithProcessInstance(
		UserReference user,
		DirectoryHandler directory,
		ProcessInstance processInstance)
	{
		if (user.equals(processInstance.getCreator()))
			return true;
		List<WorkItem> workitems = processInstance.getWorkItems();
		for (WorkItem workItem : workitems)
		{
			if (isWorkItemForUser(user, workItem, directory))
				return true;
		}
		List<ProcessInstance> subPis = processInstance.getSubWorkflows();
		for (ProcessInstance subPi : subPis)
		{
			if (isUserAssociatedWithProcessInstance(user, directory, subPi))
				return true;
		}
		return false;
	}

	public static boolean isUserAssociatedWithWorkItem(Session session, WorkItem workItem)
	{
		UserReference user = session.getUserReference();
		DirectoryHandler directory = session.getDirectory();
		return isWorkItemForUser(user, workItem, directory);
	}

	private static boolean isWorkItemForUser(
		UserReference user,
		WorkItem workItem,
		DirectoryHandler directory)
	{
		if (user.equals(workItem.getUserReference()))
			return true;
		List<Profile> profiles = workItem.getOfferedToProfiles();
		for (Profile profile : profiles)
		{
			if (profile.isUserReference() && user.equals(profile))
				return true;
			else if (profile.isSpecificRole() && directory.isUserInRole(user, (Role) profile))
				return true;
			else if (profile.isBuiltInAdministrator() && user.isBuiltInAdministrator())
				return true;
		}
		return false;
	}

	/**
	 * Checks if is in workflow.
	 *
	 * @author MCH
	 *
	 * @param pSession the session
	 * @return true, if is in workflow
	 */
	public static boolean isInWorkflow(final Session session)
	{
		return session.getInteraction(true) != null;
	}

	/**
	 * Gets the table.
	 *
	 * @author MCH
	 *
	 * @param pDataContext the process data context
	 * @param pRepository the repository
	 * @param dataSapceParam the data space requested
	 * @return the table
	 * @throws OperationException If the data space or the data set or the table is not defined or cannot be found.
	 */
	public static AdaptationTable getTableFromDataContext(
		final DataContextReadOnly pDataContext,
		final Repository pRepository,
		final String dataSpaceParam)
		throws OperationException
	{

		String tablePath = getVariableFromDataContext(pDataContext, WorkflowConstants.PARAM_TABLE);
		Adaptation currentDataSet = getDataSet(pDataContext, pRepository, dataSpaceParam);

		AdaptationTable table = currentDataSet.getTable(Path.parse(tablePath));

		if (table == null)
		{
			throw OperationException.createError("Table not found at '" + tablePath + ".");
		}
		return table;
	}

	/**
	 * Gets the variable from data context.
	 *
	 * @param pDataContext the data context
	 * @param pVariableName the variable name
	 * @return the variable from data context
	 * @throws OperationException the operation exception
	 */
	public static String getVariableFromDataContext(
		final DataContextReadOnly pDataContext,
		final String pVariableName)
		throws OperationException
	{
		String dataValue = pDataContext.getVariableString(pVariableName);
		if (dataValue == null)
		{
			throw OperationException
				.createError("Data context variable '" + pVariableName + "' is not defined");
		}
		return dataValue;
	}

	public static List<ProcessInstanceKey> getProcessInstancesForWorkflow(
		WorkflowEngine wfe,
		String workflowName)
	{
		return wfe.getProcessInstanceKeys(workflowName);
	}

	public static List<ProcessInstance> getActiveProcessInstancesForDataContextVariable(
		String workflowName,
		String varName,
		String varValue,
		boolean returnFirstOneFound)
	{
		WorkflowEngine wfe = WorkflowEngine.getFromRepository(Repository.getDefault(), null);
		return getActiveProcessInstancesForDataContextVariable(
			wfe,
			workflowName,
			varName,
			varValue,
			returnFirstOneFound);
	}

	public static List<ProcessInstance> getActiveProcessInstancesForDataContextVariable(
		WorkflowEngine wfe,
		String workflowName,
		String varName,
		String varValue,
		boolean returnFirstOneFound)
	{
		if (workflowName == null || varName == null)
		{
			return null;
		}

		List<ProcessInstance> processInstances = new ArrayList<>();
		List<ProcessInstanceKey> piks = wfe.getProcessInstanceKeys(workflowName);
		for (ProcessInstanceKey pik : piks)
		{
			if (wfe.isProcessInstanceRunning(pik))
			{
				ProcessInstance processInstance = wfe.getProcessInstance(pik);
				DataContextReadOnly dataContext = processInstance.getDataContext();
				if (dataContext.isVariableDefined(varName))
				{
					String dataContextValue = dataContext.getVariableString(varName);
					if ((dataContextValue == null && varValue == null)
						|| (dataContextValue != null && dataContextValue.equals(varValue)))
					{
						processInstances.add(processInstance);
						if (returnFirstOneFound)
						{
							return processInstances;
						}
					}
				}
			}
		}

		return processInstances;
	}
}
