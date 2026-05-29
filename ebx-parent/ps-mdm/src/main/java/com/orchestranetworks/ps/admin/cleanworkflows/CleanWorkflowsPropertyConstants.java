/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

/**
 * Constants for Dev Artifacts
 */
public interface CleanWorkflowsPropertyConstants
{
	/**
	 * @deprecated Use {@link PROPERTY_MASTER_DATA_SPACES} instead
	 */
	@Deprecated
	static final String PROPERTY_MASTER_DATA_SPACE = "masterDataSpace";

	static final String PROPERTY_USE_WORKING_DATA_SPACE = "useWorkingDataSpace";
	static final String PROPERTY_DATA_SPACE_CLOSE_POLICY = "dataSpaceClosePolicy";
	static final String PROPERTY_MASTER_DATA_SPACES = "masterDataSpaces";
	static final String PROPERTY_CHILD_DATA_SPACES_TO_SKIP = "childDataSpacesToSkip";
	static final String PROPERTY_WORKFLOW_PUBLICATIONS = "workflowPublications";
	static final String PROPERTY_CREATED_BEFORE_DATE = "createdBeforeDate";
	static final String PROPERTY_CREATED_BEFORE_NUM_OF_DAYS = "createdBeforeNumOfDays";
	static final String PROPERTY_INCLUDE_COMPLETED = "includeCompleted";
	static final String PROPERTY_INCLUDE_ACTIVE = "includeActive";
}
