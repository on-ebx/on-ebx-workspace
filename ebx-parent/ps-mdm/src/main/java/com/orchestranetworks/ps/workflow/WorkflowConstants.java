/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.workflow;

/**
 */
public interface WorkflowConstants
{
	public static final String DATA_CONTEXT_NULL_VALUE = "null";

	public static String PARAM_WORKFLOW_NAME = "workflowName";
	public static String PARAM_WORKING_DATA_SPACE = "workingDataSpace";
	public static String PARAM_RECORD_NAME_VALUE = "recordNameValue";
	public static String PARAM_CURRENT_USER_ID = "currentUserId";
	public static String PARAM_CURRENT_USER_LABEL = "currentUserLabel";
	public static String PARAM_CURRENT_APPROVER_ID = "currentApproverId";
	public static String PARAM_CURRENT_APPROVER_LABEL = "currentApproverLabel";
	public static String PARAM_CURRENT_USERS_FOR_ROLES = "currentUsersForRoles";
	public static String PARAM_CURRENT_USERS_FOR_ROLES_DELIMITER = ",";
	public static String PARAM_CURRENT_USERS_FOR_ROLES_SUBDELIMITER = "|";

	public static String PARAM_XPATH_TO_TABLE = "xpathToTable";
	public static String PARAM_MASTER_DATA_SPACE = "masterDataSpace";
	public static String PARAM_RECORD = "record";
	public static String PARAM_TABLE = "table";
	public static String PARAM_DATA_SET = "dataSet";
	public static String PARAM_DATA_SPACE = "dataSpace";
	public static String PARAM_USER_TASK_CREATE_DATE_TIME = "userTaskCreateDateTime";
	public static String PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME = "workflowInstanceCreateDateTime";
	public static String PARAM_WORKFLOW_INSTANCE_CREATE_DATE_TIME_UNFORMATTED = "workflowInstanceCreateDateTimeUnformatted";
	public static String PARAM_CURRENT_PERMISSIONS_USER = "currentPermissionsUser";
	public static String PARAM_ADDITIONAL_NOTIFICATION_INFO = "additionalNotificationInfo";
	public static String PARAM_TABLE_PATHS_TO_BE_VALIDATED_FIELD = "tablePathsToBeValidatedField";
	public static final String PARAM_APPROVAL_STEP = "isApprovalStep";
	public static final String PARAM_SELF_APPROVER_ROLE = "selfApproverRole";

	public static final String SESSION_PARAM_CREATED = "created";
	public static final String SESSION_PARAM_XPATH = "xpath";
	public static final String SESSION_PARAM_RECORD = "record";;
	public static final String SESSION_PARAM_DATA_SPACE = "branch";
	public static final String SESSION_PARAM_SNAPSHOT = "version";
	public static final String SESSION_PARAM_DATA_SET = "instance";
	// These can be used by custom services when the built-in params are already
	// in use
	public static String SESSION_PARAM_WORKFLOW_RECORD = "workflowRecord";
	public static String SESSION_PARAM_WORKFLOW_DATA_SPACE = "workflowDataSpace";
	public static String SESSION_PARAM_WORKFLOW_DATA_SET = "workflowDataSet";

	public static final String DATA_CONTEXT_DATE_TIME_FORMAT_STRING = "MM/dd/yyyy 'at' HH:mm:ss";
	public static final String DATA_CONTEXT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	// Standard workflows use ROLE_WORKFLOW_ADMINISTRATOR now. Manager & Monitor are still used by the project-related
	// classes and are still in use by customers so keeping for now.
	public static String ROLE_WORKFLOW_MANAGER = "Workflow Manager";
	public static String ROLE_WORKFLOW_MONITOR = "Workflow Monitor";

	public static String ROLE_WORKFLOW_ADMINISTRATOR = "Workflow Administrator";
}
