package com.orchestranetworks.ps.usertask;

import java.util.*;
import java.util.regex.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class BasicUserTask extends UserTask implements WorkflowConstants
{
	protected static int maxErrorsToDisplay = 100;

	protected static final String DEFAULT_VALIDATION_MESSAGE = "*** All Validation Errors must be corrected before proceeding ***";

	protected static final String FIELD_LABEL_MESSAGE_PARAM = "<fieldLabel>";
	protected static final String TABLE_LABEL_MESSAGE_PARAM = "<tableLabel>";

	private static final String SINGLE_VALUE_FIELD_REQUIRED_MESSAGE_TEMPLATE = FIELD_LABEL_MESSAGE_PARAM
		+ " must be specified.";
	private static final String MULTI_VALUE_FIELD_REQUIRED_MESSAGE_TEMPLATE = "At least one "
		+ SINGLE_VALUE_FIELD_REQUIRED_MESSAGE_TEMPLATE;
	private static final String AT_LEAST_ONE_OF_FIELDS_REQUIRED_MESSAGE_TEMPLATE = "Either "
		+ FIELD_LABEL_MESSAGE_PARAM + " must be specified.";
	private static final String ONLY_ONE_OF_FIELDS_ALLOWED_MESSAGE_TEMPLATE = "No more than one of "
		+ FIELD_LABEL_MESSAGE_PARAM + " may be specified.";

	private static final String SINGLE_VALUE_FIELD_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE = FIELD_LABEL_MESSAGE_PARAM
		+ " must be specified for " + TABLE_LABEL_MESSAGE_PARAM + ".";
	private static final String MULTI_VALUE_FIELD_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE = "At least one "
		+ SINGLE_VALUE_FIELD_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE;
	private static final String AT_LEAST_ONE_OF_FIELDS_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE = "Either "
		+ FIELD_LABEL_MESSAGE_PARAM + " must be specified for " + TABLE_LABEL_MESSAGE_PARAM + ".";
	private static final String ONLY_ONE_OF_FIELDS_ALLOWED_FOR_TABLE_MESSAGE_TEMPLATE = "No more than one of "
		+ FIELD_LABEL_MESSAGE_PARAM + " may be specified for " + TABLE_LABEL_MESSAGE_PARAM + ".";

	protected boolean defaultIncludeTableLabel = false;
	protected boolean defaultIncludeGroupLabels = false;
	protected Severity minimumValidationSeverity = Severity.ERROR;
	protected boolean neverDefaultAllocationToCreator = false;

	@Override
	public void handleCreate(final UserTaskCreationContext context) throws OperationException
	{
		WorkflowUtilities.setUserTaskCreateDateTime(context);
		super.handleCreate(context);
	}

	protected void addUserAndRole(UserTaskCreationContext context, Role userRole)
		throws OperationException
	{
		// Assign User as Current User if there is one already established
		// Else Assign Workflow Launcher if he plays the correct role
		// Else Assign Default Role from Subclass if one exists
		UserReference userReference = null;
		if (context.isVariableDefined(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES))
		{
			userReference = getUserReferenceForCurrentRole(
				userRole,
				context,
				context.getRepository());
		}
		else
		{
			userReference = WorkflowUtilities
				.getCurrentUserReference(context, context.getRepository());
		}

		if (userReference == null && !neverDefaultAllocationToCreator)
		{
			userReference = context.getProcessInstance().getCreator();
		}
		addUserReferenceAndRole(context, userRole, userReference);
	}

	protected UserReference getUserReferenceForCurrentRole(
		Role userRole,
		DataContext context,
		Repository repository)
		throws OperationException
	{
		String currentUsersForRoles = context
			.getVariableString(WorkflowConstants.PARAM_CURRENT_USERS_FOR_ROLES);
		List<String> currentUserForRoleList = CollectionUtils
			.splitString(currentUsersForRoles, PARAM_CURRENT_USERS_FOR_ROLES_DELIMITER);
		for (String currentUserForRole : currentUserForRoleList)
		{
			List<String> roleAndUser = CollectionUtils
				.splitString(currentUserForRole, PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER);
			if (userRole.getRoleName().equals(roleAndUser.get(0)))
			{
				return WorkflowUtilities.getUserReference(roleAndUser.get(1), repository);
			}
		}
		return null;
	}

	protected void addUserReferenceAndRole(
		UserTaskCreationContext context,
		Role userRole,
		UserReference userReference)
		throws OperationException
	{
		// Assign UserReference as Current User if there is one already
		// established and he is still in the correct role
		// Else Assign Default Role from Subclass if one exists
		if (userReference == null
			|| !context.getSession().getDirectory().isUserInRole(userReference, userRole)
			|| !isUserValidForAutoAllocate(context, userRole, userReference))
		{
			userReference = getDefaultUserForRole(context, userRole);
		}
		addUserAndRole(context, userRole, userReference);
	}

	/**
	 * Determine whether a user is valid to be automatically allocated to. It is assumed that the user is
	 * in the role passed in. By default, this method returns <code>true</code> but can be overridden
	 * if you want to customize whether a user is valid in a particular context.
	 *
	 * @param context the context
	 * @param role the role
	 * @param user the user, which is in the role specified
	 * @return whether the user is valid to be automatically allocated to
	 * @throws OperationException if an error occurs
	 */
	protected boolean isUserValidForAutoAllocate(
		UserTaskCreationContext context,
		Role role,
		UserReference user)
		throws OperationException
	{
		return true;
	}

	// Override this method if you want to define a Default User
	protected UserReference getDefaultUserForRole(UserTaskCreationContext context, Role role)
		throws OperationException
	{
		return null;
	}

	protected void addUserAndRole(
		UserTaskCreationContext context,
		Role userRole,
		UserReference userReference)
		throws OperationException
	{

		CreationWorkItemSpec spec = null;
		if (userReference != null
			&& context.getSession().getDirectory().isUserInRole(userReference, userRole))
		{
			spec = CreationWorkItemSpec
				.forAllocationWithPossibleReallocation(userReference, userRole);
			spec.setNotificationMail(getAllocatedToNotificationMail(context));
		}
		else
		{
			spec = CreationWorkItemSpec.forOffer(userRole);
			spec.setNotificationMail(getOfferedToNotificationMail(context));
		}

		spec.setProfileMailCC(this.getCCProfile(context, userRole));

		context.createWorkItem(spec);
	}

	// Override this method to specify an alternative Allocated To Notification Template if desired (i.e. Rejection Notification)
	protected String getAllocatedToNotificationMail(UserTaskCreationContext context)
	{
		return context.getAllocatedToNotificationMail();
	}

	// Override this method to specify an alternative Offered To Notification Template if desired (i.e. Rejection Notification)
	protected String getOfferedToNotificationMail(UserTaskCreationContext context)
	{
		return context.getOfferedToNotificationMail();
	}

	@Override
	public void handleWorkItemCompletion(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{

		if (context.getCompletedWorkItem().isAccepted()
			&& context.isVariableDefined(WorkflowConstants.PARAM_RECORD)
			&& context.isVariableDefined(WorkflowConstants.PARAM_RECORD_NAME_VALUE))
		{
			// set recordNameValue variable in the Data Context
			Adaptation record = getRecord(context, context.getRepository());
			if (record != null)
			{
				String recordLabel = getRecordLabel(record, context.getSession());
				context.setVariableString(WorkflowConstants.PARAM_RECORD_NAME_VALUE, recordLabel);
			}
		}
		super.handleWorkItemCompletion(context);
	}

	public Adaptation getRecord(DataContextReadOnly context, Repository repo)
		throws OperationException
	{
		return WorkflowUtilities.getRecord(context, null, repo);
	}

	protected String getRecordLabel(Adaptation record, Session session)
	{
		return record.getLabel(session.getLocale());
	}

	public void setCurrentUserIdAndLabel(final UserTaskWorkItemCompletionContext context)
		throws OperationException
	{
		WorkflowUtilities.setCurrentUserIdAndLabel(context);
	}

	protected boolean isCompletionCriteriaIgnored()
	{
		return WorkflowUtilities.isCompletionCriteriaIgnored();
	}

	// Method available to be called from the checkBeforeWorkItemCompletion
	// Method if the User Task requires Validation on the Working DataSpace
	// before Accept
	protected void performValidationOnWorkingDataset(
		UserTaskBeforeWorkItemCompletionContext context)
	{
		performValidationOnWorkingDataset(context, null);
	}

	// Method available to be called from the checkBeforeWorkItemCompletion
	// Method if the User Task requires Validation on the Working DataSpace (Dataset or a subset of tables)
	// before Accept
	protected void performValidationOnWorkingDataset(
		UserTaskBeforeWorkItemCompletionContext context,
		List<Path> tablePathsToBeValidated)
	{
		performValidationOnDataset(
			context,
			context.getVariableString(WorkflowConstants.PARAM_WORKING_DATA_SPACE),
			context.getVariableString(WorkflowConstants.PARAM_DATA_SET),
			DEFAULT_VALIDATION_MESSAGE,
			tablePathsToBeValidated);
	}

	// Method available to be called from the checkBeforeWorkItemCompletion Method 
	// if the User Task requires Validation on the Working DataSpace for only the records that have changed in the working dataspace)
	// before Accept
	protected void performValidationOnWorkingDatasetForChangedRecordsOnly(
		UserTaskBeforeWorkItemCompletionContext context)
	{
		Adaptation dataset = getDataSetForCheckBeforeWorkItemCompletion(context);
		if (dataset == null)
		{
			return;
		}
		int count = 0;
		String errorMessageText = DEFAULT_VALIDATION_MESSAGE;
		List<Adaptation> recordsToBeValidated = getChangedRecordsForValidation(dataset);
		for (Adaptation record : recordsToBeValidated)
		{
			count = performValidation(context, errorMessageText, record, count, null);
		}
	}

	// Get the changed records in the data set, for use in validating changed records only
	// in the data set. This simply returns all changed records but can be overridden if
	// different logic is required.
	protected List<Adaptation> getChangedRecordsForValidation(Adaptation dataset)
	{
		return AdaptationUtil.getChangedRecords(dataset);
	}

	// Method available to be called from the checkBeforeWorkItemCompletion Method
	// if the User Task requires Validation on a Dataset before Accept
	protected void performValidationOnDataset(
		UserTaskBeforeWorkItemCompletionContext context,
		String dataSpaceId,
		String dataSetId,
		String errorMessageText)
	{
		performValidationOnDataset(context, dataSpaceId, dataSetId, errorMessageText, null);

	}

	// Method available to be called from the checkBeforeWorkItemCompletion Method
	// if the User Task requires Validation on a Dataset (or a subset of tables) before Accept
	protected void performValidationOnDataset(
		UserTaskBeforeWorkItemCompletionContext context,
		String dataSpaceId,
		String dataSetId,
		String errorMessageText,
		List<Path> tablePathsToBeValidated)
	{
		// Perform Validation on Dataset on on a subset of Tables specified in the User Task
		Adaptation dataset = getDataSetForCheckBeforeWorkItemCompletion(
			context,
			dataSpaceId,
			dataSetId);
		if (dataset == null)
		{
			return;
		}
		int count = 0;
		performValidation(context, errorMessageText, dataset, count, tablePathsToBeValidated);
	}

	// Perform Validation on Dataset or Record or on a subset of Tables specified in the User Task
	protected int performValidation(
		UserTaskBeforeWorkItemCompletionContext context,
		String errorMessageText,
		Adaptation adaptation,
		int count,
		List<Path> tablePathsToBeValidated)
	{
		if (!adaptation.isSchemaInstance() || tablePathsToBeValidated == null
			|| tablePathsToBeValidated.isEmpty())
		{
			ValidationReport validationReport = adaptation.getValidationReport();
			count = processValidationReport(
				context,
				adaptation,
				errorMessageText,
				count,
				validationReport);
		}
		else
		{
			for (Path tablePath : tablePathsToBeValidated)
			{
				// TODO:  [FK] Add support for Groups of Tables (needs to be recursive to handle Groups of groups)
				ValidationReport validationReport = adaptation.getTable(tablePath)
					.getValidationReport();
				count = processValidationReport(context, errorMessageText, count, validationReport);
			}
		}
		return count;
	}

	protected int processValidationReport(
		UserTaskBeforeWorkItemCompletionContext context,
		String errorMessageText,
		int count,
		ValidationReport validationReport)
	{
		return processValidationReport(context, null, errorMessageText, count, validationReport);
	}

	// TODO: Ideally the validation report item would indicate the adaptation, but sometimes it doesn't.
	//       Have a ticket open with support about that. In meantime, creating a version that takes in an adaptation.
	protected int processValidationReport(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation subjectAdaptation,
		String errorMessageText,
		int count,
		ValidationReport validationReport)
	{
		if (validationReport.hasItemsOfSeverityOrMore(minimumValidationSeverity))
		{
			if (count == 0 && errorMessageText != null)
			{
				context.reportMessage(UserMessage.createError(errorMessageText));
			}
			ValidationReportItemIterator iter = validationReport
				.getItemsOfSeverityOrMore(minimumValidationSeverity);
			//Only Display the first 100 messages
			while (iter.hasNext() && count < maxErrorsToDisplay) // run the while-loop for only maxErrorsToDisplay iterations
			{
				ValidationReportItem item = iter.nextItem();
				count++;

				Adaptation adaptation = subjectAdaptation;
				// If a subject was not provided, try to get it from the validation report itself.
				// Or if it was provided and it's a data set, we still want to try to get it from the
				// validation report because the validation report's subject could be a record.
				if (adaptation == null || adaptation.isSchemaInstance())
				{
					ValidationReportItemSubjectForAdaptation subjectForAdaptation = item
						.getSubjectForAdaptation();
					if (subjectForAdaptation != null)
					{
						Adaptation subjectForAdaptationAdaptation = subjectForAdaptation
							.getAdaptation();
						if (subjectForAdaptationAdaptation != null
							&& subjectForAdaptationAdaptation.isTableOccurrence())
						{
							adaptation = subjectForAdaptationAdaptation;
						}
					}
				}
				// If there is still no adaptation, or the validation report's subject is a data set,
				// then we want to report the validation message prepended with the table's label
				if (adaptation == null || adaptation.isSchemaInstance())
				{
					String message = item.getMessage().formatMessage(Locale.getDefault());

					if (null != item.getSubjectForTable()) // this could be null for unique constraint validations.
					{
						String tableLabel = item.getSubjectForTable()
							.getAdaptationTable()
							.getTableNode()
							.getLabel(Locale.getDefault());

						message = tableLabel + ": " + message;
					}

					context
						.reportMessage(AdaptationUtil.createUserMessage(message, Severity.ERROR));
				}
				// Otherwise, it's a record, so report a message prepended with the record's info, as well as the field info
				else
				{

					String message = item.getMessage()
						.formatMessage(context.getSession().getLocale());

					if (null != item.getSubjectForAdaptation()) // this could be null for unique constraint validations.
					{
						String fieldLabel = item.getSubjectForAdaptation()
							.getSchemaNode()
							.getLabel(Locale.getDefault());

						message = fieldLabel + ": " + message;
					}

					context.reportMessage(
						AdaptationUtil.createUserMessage(adaptation, message, Severity.ERROR));
				}
			}

			count = validationReport.countItemsOfSeverity(Severity.ERROR);
			if (count > maxErrorsToDisplay)
			{
				context.reportMessage(
					UserMessage.createError(
						".../... " + String.valueOf(count - maxErrorsToDisplay)
							+ " more errors exist."));
			}

			iter.close();

		}

		return count;
	}

	protected Adaptation getDataSetForCheckBeforeWorkItemCompletion(
		UserTaskBeforeWorkItemCompletionContext context)
	{
		String dataSpaceId = context.getVariableString(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
		String dataSetId = context.getVariableString(WorkflowConstants.PARAM_DATA_SET);
		return getDataSetForCheckBeforeWorkItemCompletion(context, dataSpaceId, dataSetId);
	}

	protected Adaptation getDataSetForCheckBeforeWorkItemCompletion(
		UserTaskBeforeWorkItemCompletionContext context,
		String dataSpaceId,
		String dataSetId)
	{
		Repository repo = context.getRepository();
		AdaptationHome home = repo.lookupHome(HomeKey.forBranchName(dataSpaceId));
		if (home == null)
		{
			context.reportMessage(
				UserMessage.createError("DataSpace " + dataSpaceId + " has not been found"));
			return null;
		}
		Adaptation dataset = home.findAdaptationOrNull(AdaptationName.forName(dataSetId));
		if (dataset == null)
		{
			context.reportMessage(
				UserMessage.createError("DataSet " + dataSetId + " has not been found"));
			return null;
		}
		return dataset;
	}

	protected UserMessage checkFieldRequired(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation adaptation,
		Path fieldPath)
	{
		return checkFieldRequired(
			context,
			adaptation,
			fieldPath,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage checkFieldRequired(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation adaptation,
		Path fieldPath,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		UserMessage msg = null;
		if (isFieldMissing(adaptation, fieldPath))
		{
			msg = getFieldRequiredMessage(
				adaptation,
				fieldPath,
				context.getSession(),
				includeTableLabel,
				includeGroupLabels);
			context.reportMessage(msg);
		}
		return msg;
	}

	protected UserMessage checkAtLeastOneOfFieldsRequired(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation adaptation,
		Path[] fieldPaths)
	{
		return checkAtLeastOneOfFieldsRequired(
			context,
			new Adaptation[] { adaptation },
			fieldPaths,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage checkAtLeastOneOfFieldsRequired(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation[] adaptations,
		Path[] fieldPaths)
	{
		return checkAtLeastOneOfFieldsRequired(
			context,
			adaptations,
			fieldPaths,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage checkAtLeastOneOfFieldsRequired(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation[] adaptations,
		Path[] fieldPaths,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		UserMessage msg = null;
		if (isOneOfFieldsMissing(adaptations, fieldPaths))
		{
			msg = getAtLeastOneOfFieldsRequiredMessage(
				adaptations,
				fieldPaths,
				context.getSession(),
				includeTableLabel,
				includeGroupLabels);
			context.reportMessage(msg);
		}
		return msg;
	}

	protected UserMessage checkOnlyOneOfFieldsAllowed(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation adaptation,
		Path[] fieldPaths)
	{
		return checkOnlyOneOfFieldsAllowed(
			context,
			new Adaptation[] { adaptation },
			fieldPaths,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage checkOnlyOneOfFieldsAllowed(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation[] adaptations,
		Path[] fieldPaths)
	{
		return checkOnlyOneOfFieldsAllowed(
			context,
			adaptations,
			fieldPaths,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage checkOnlyOneOfFieldsAllowed(
		UserTaskBeforeWorkItemCompletionContext context,
		Adaptation[] adaptations,
		Path[] fieldPaths,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		UserMessage msg = null;
		if (isMoreThanOneOfFieldsSet(adaptations, fieldPaths))
		{
			msg = getOnlyOneOfFieldsAllowedMessage(
				adaptations,
				fieldPaths,
				context.getSession(),
				includeTableLabel,
				includeGroupLabels);
			context.reportMessage(msg);
		}
		return msg;
	}

	protected boolean isFieldMissing(Adaptation adaptation, Path fieldPath)
	{
		if (adaptation == null)
		{
			return false;
		}
		SchemaNode fieldNode = adaptation.getSchemaNode().getNode(fieldPath);
		if (fieldNode.isSelectNode() || fieldNode.isAssociationNode())
		{
			return AdaptationUtil.isLinkedRecordListEmpty(adaptation, fieldPath);
		}
		Object value = adaptation.get(fieldPath);
		if (value == null)
		{
			return true;
		}
		if (fieldNode.getMaxOccurs() > 1)
		{
			@SuppressWarnings("unchecked")
			List<Object> listValue = (List<Object>) value;
			return listValue.isEmpty();
		}
		// For Strings, check if there is a non-blank value
		if (value instanceof String)
		{
			return ((String) value).trim().isEmpty();
		}

		return false;
	}

	protected boolean isOneOfFieldsMissing(Adaptation adaptation, Path[] fieldPaths)
	{
		return isOneOfFieldsMissing(new Adaptation[] { adaptation }, fieldPaths);
	}

	protected boolean isOneOfFieldsMissing(Adaptation[] adaptations, Path[] fieldPaths)
	{
		boolean sameAdaptation = adaptations.length == 1;
		boolean valueFound = false;
		for (int i = 0; i < fieldPaths.length && !valueFound; i++)
		{
			Path fieldPath = fieldPaths[i];
			Adaptation adaptation = sameAdaptation ? adaptations[0] : adaptations[i];
			valueFound = !isFieldMissing(adaptation, fieldPath);
		}
		return !valueFound;
	}

	protected boolean isMoreThanOneOfFieldsSet(Adaptation adaptation, Path[] fieldPaths)
	{
		return isMoreThanOneOfFieldsSet(new Adaptation[] { adaptation }, fieldPaths);
	}

	protected boolean isMoreThanOneOfFieldsSet(Adaptation[] adaptations, Path[] fieldPaths)
	{
		boolean sameAdaptation = adaptations.length == 1;
		int valueCount = 0;
		for (int i = 0; i < fieldPaths.length && valueCount < 2; i++)
		{
			Path fieldPath = fieldPaths[i];
			Adaptation adaptation = sameAdaptation ? adaptations[0] : adaptations[i];
			if (!isFieldMissing(adaptation, fieldPath))
			{
				valueCount++;
			}
		}
		return valueCount > 1;
	}

	protected UserMessage getFieldRequiredMessage(
		Adaptation adaptation,
		Path fieldPath,
		Session session)
	{
		return getFieldRequiredMessage(
			adaptation,
			fieldPath,
			session,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage getFieldRequiredMessage(
		Adaptation adaptation,
		Path fieldPath,
		Session session,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		SchemaNode fieldNode = adaptation.getSchemaNode().getNode(fieldPath);
		String templateWithoutTable;
		String templateWithTable;
		if (fieldNode.getMaxOccurs() > 1 || fieldNode.isSelectNode()
			|| fieldNode.isAssociationNode())
		{
			templateWithoutTable = getMultiValueFieldRequiredMessageTemplate(fieldPath);
			templateWithTable = getMultiValueFieldRequiredForTableMessageTemplate(fieldPath);
		}
		else
		{
			templateWithoutTable = getSingleValueFieldRequiredMessageTemplate(fieldPath);
			templateWithTable = getSingleValueFieldRequiredForTableMessageTemplate(fieldPath);
		}
		return getTokensReplacedMessage(
			new Adaptation[] { adaptation },
			new Path[] { fieldPath },
			session,
			includeTableLabel,
			includeGroupLabels,
			templateWithoutTable,
			templateWithTable);
	}

	protected UserMessage getAtLeastOneOfFieldsRequiredMessage(
		Adaptation adaptation,
		Path[] fieldPaths,
		Session session)
	{
		return getAtLeastOneOfFieldsRequiredMessage(
			new Adaptation[] { adaptation },
			fieldPaths,
			session,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage getAtLeastOneOfFieldsRequiredMessage(
		Adaptation[] adaptations,
		Path[] fieldPaths,
		Session session)
	{
		return getAtLeastOneOfFieldsRequiredMessage(
			adaptations,
			fieldPaths,
			session,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage getAtLeastOneOfFieldsRequiredMessage(
		Adaptation[] adaptations,
		Path[] fieldPaths,
		Session session,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		return getTokensReplacedMessage(
			adaptations,
			fieldPaths,
			session,
			includeTableLabel,
			includeGroupLabels,
			getAtLeastOneOfFieldsRequiredMessageTemplate(fieldPaths),
			getAtLeastOneOfFieldsRequiredForTableMessageTemplate(fieldPaths));
	}

	protected UserMessage getOnlyOneOfFieldsAllowedMessage(
		Adaptation adaptation,
		Path[] fieldPaths,
		Session session)
	{
		return getOnlyOneOfFieldsAllowedMessage(
			new Adaptation[] { adaptation },
			fieldPaths,
			session,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage getOnlyOneOfFieldsAllowedMessage(
		Adaptation[] adaptations,
		Path[] fieldPaths,
		Session session)
	{
		return getOnlyOneOfFieldsAllowedMessage(
			adaptations,
			fieldPaths,
			session,
			defaultIncludeTableLabel,
			defaultIncludeGroupLabels);
	}

	protected UserMessage getOnlyOneOfFieldsAllowedMessage(
		Adaptation[] adaptations,
		Path[] fieldPaths,
		Session session,
		boolean includeTableLabel,
		boolean includeGroupLabels)
	{
		return getTokensReplacedMessage(
			adaptations,
			fieldPaths,
			session,
			includeTableLabel,
			includeGroupLabels,
			getOnlyOneOfFieldsAllowedMessageTemplate(fieldPaths),
			getOnlyOneOfFieldsAllowedForTableMessageTemplate(fieldPaths));
	}

	// TODO: Can only show one table in the string currently
	private UserMessage getTokensReplacedMessage(
		Adaptation[] adaptations,
		Path[] fieldPaths,
		Session session,
		boolean includeTableLabel,
		boolean includeGroupLabels,
		String templateWithoutTable,
		String templateWithTable)
	{
		boolean sameAdaptation = adaptations.length == 1;
		StringBuilder bldr = new StringBuilder();
		for (int i = 0; i < fieldPaths.length; i++)
		{
			bldr.append("'");
			Adaptation adaptation = sameAdaptation ? adaptations[0] : adaptations[i];
			bldr.append(getFieldLabel(adaptation, fieldPaths[i], session, includeGroupLabels));
			bldr.append("'");
			if (i < fieldPaths.length - 1)
			{
				bldr.append(" or ");
			}
		}
		String template = includeTableLabel ? templateWithTable : templateWithoutTable;
		String msg = template.replaceAll(FIELD_LABEL_MESSAGE_PARAM, bldr.toString());
		if (includeTableLabel)
		{
			msg = msg.replaceAll(
				TABLE_LABEL_MESSAGE_PARAM,
				Matcher.quoteReplacement(getTableString(adaptations[0], session)));
		}
		return UserMessage.createError(msg);
	}

	protected String getTableString(Adaptation adaptation, Session session)
	{
		return getTableLabel(adaptation, session) + " "
			+ adaptation.getLabelOrName(session.getLocale());
	}

	protected String getSingleValueFieldRequiredMessageTemplate(Path fieldPath)
	{
		return SINGLE_VALUE_FIELD_REQUIRED_MESSAGE_TEMPLATE;
	}

	protected String getMultiValueFieldRequiredMessageTemplate(Path fieldPath)
	{
		return MULTI_VALUE_FIELD_REQUIRED_MESSAGE_TEMPLATE;
	}

	protected String getAtLeastOneOfFieldsRequiredMessageTemplate(Path[] fieldPaths)
	{
		return AT_LEAST_ONE_OF_FIELDS_REQUIRED_MESSAGE_TEMPLATE;
	}

	protected String getOnlyOneOfFieldsAllowedMessageTemplate(Path[] fieldPaths)
	{
		return ONLY_ONE_OF_FIELDS_ALLOWED_MESSAGE_TEMPLATE;
	}

	protected String getSingleValueFieldRequiredForTableMessageTemplate(Path fieldPath)
	{
		return SINGLE_VALUE_FIELD_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE;
	}

	protected String getMultiValueFieldRequiredForTableMessageTemplate(Path fieldPath)
	{
		return MULTI_VALUE_FIELD_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE;
	}

	protected String getAtLeastOneOfFieldsRequiredForTableMessageTemplate(Path[] fieldPaths)
	{
		return AT_LEAST_ONE_OF_FIELDS_REQUIRED_FOR_TABLE_MESSAGE_TEMPLATE;
	}

	protected String getOnlyOneOfFieldsAllowedForTableMessageTemplate(Path[] fieldPaths)
	{
		return ONLY_ONE_OF_FIELDS_ALLOWED_FOR_TABLE_MESSAGE_TEMPLATE;
	}

	protected String getFieldLabel(
		Adaptation adaptation,
		Path fieldPath,
		Session session,
		boolean includeGroupLabels)
	{
		return AdaptationUtil.getFieldLabel(adaptation, fieldPath, session, includeGroupLabels);
	}

	protected String getTableLabel(Adaptation adaptation, Session session)
	{
		return adaptation.getContainerTable().getTableNode().getLabel(session.getLocale());
	}

	/**
	 * Defined by User Tasks that require sending a CC email notification.
	 *
	 * By Default, it will return the CC Profile specified in the Workflow Model, but can be overriden if desired;
	 */
	protected Profile getCCProfile(UserTaskCreationContext context, Role userRole)
	{
		return context.getProfileMailCC();
	}

	public boolean isNeverDefaultAllocationToCreator()
	{
		return neverDefaultAllocationToCreator;
	}

	public void setNeverDefaultAllocationToCreator(boolean neverDefaultAllocationToCreator)
	{
		this.neverDefaultAllocationToCreator = neverDefaultAllocationToCreator;
	}
}
