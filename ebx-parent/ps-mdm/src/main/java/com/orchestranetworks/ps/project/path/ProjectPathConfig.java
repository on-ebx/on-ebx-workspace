/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.path;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * Specifies project related paths
 */
public interface ProjectPathConfig
{
	SubjectPathConfig getSubjectPathConfig(String projectType);

	Path getProjectTablePath();
	Path getProjectNameFieldPath();
	Path getProjectProjectTypeFieldPath();
	Path getProjectStatusFieldPath();
	Path getProjectCancelReasonFieldPath();
	Path getProjectTeamMembersFieldPath();

	/** For use when a link table is used. Otherwise, returns null. */
	Path getProjectProjectSubjectsFieldPath(String projectType);

	/** For use when a link table is not used. Otherwise, returns null. */
	Path getProjectSubjectFieldPath(String projectType);
	Path getProjectIsResumableFieldPath();

	Path getProjectRoleTablePath();
	Path getProjectRoleRolesFieldPath();
	Path[] getProjectPathsForProjectRolePK();

	boolean isProjectTeamMemberDeletionAllowed();

	// Note: Some filter code relies on fields being at same level
	Path getProjectTeamMemberTablePath();
	Path getProjectTeamMemberProjectFieldPath();
	Path getProjectTeamMemberProjectRoleFieldPath();
	Path getProjectTeamMemberCalculatedProjectRoleNameFieldPath();
	Path getProjectTeamMemberUserFieldPath();
	Path getProjectTeamMemberCalculatedUserIdFieldPath();

	String getCancelledProjectStatus(String projectType);
	String getCompletedProjectStatus(String projectType);

	boolean isNewSubjectProjectType(String projectType);
	boolean isInProcessProjectStatus(String projectStatus);

	Adaptation getAdminDataSet(Repository repo);
	String getMasterDataSpaceName();

	/** For use with the ProjectWorkflowEvent Table. */
	Path getProjectWorkflowEventTablePath();
	Path getProjectWorkflowEventEventTypeFieldPath();
	Path getProjectWorkflowEventProjectFieldPath();
	Path getProjectWorkflowEventWorkflowNameFieldPath();
	Path getProjectWorkflowEventWorkflowLabelFieldPath();
	Path getProjectWorkflowEventWorkflowInstanceKeyFieldPath();
	Path getProjectWorkflowEventMasterWorkflowInstanceKeyFieldPath();
	Path getProjectWorkflowEventCreateDateTimeFieldPath();
	Path getProjectWorkflowEventCompleteDateTimeFieldPath();
	Path getProjectWorkflowEventWorkItemStepIdFieldPath();
	Path getProjectWorkflowEventWorkItemLabelFieldPath();
	Path getProjectWorkflowEventWorkItemInstanceKeyFieldPath();
	Path getProjectWorkflowEventWorkItemRoleFieldPath();
	Path getProjectWorkflowEventWorkItemUserFieldPath();
	Path getProjectWorkflowEventWorkItemIsAcceptedFieldPath();

}
