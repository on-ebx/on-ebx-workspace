/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.path;

import com.orchestranetworks.schema.*;

/**
 */
public interface SubjectPathConfig
{
	Path getProjectSubjectTablePath();
	Path getProjectSubjectSubjectFieldPath();
	Path getProjectSubjectProjectFieldPath();

	String getNewSubjectProjectType();

	Path getSubjectTablePath();
	Path getSubjectCurrentProjectTypeFieldPath();
	Path getSubjectStatusFieldPath();
	Path getSubjectNameFieldPath();
	Path getSubjectProjectSubjectsFieldPath();
	Path getSubjectProjectsFieldPath();
}
