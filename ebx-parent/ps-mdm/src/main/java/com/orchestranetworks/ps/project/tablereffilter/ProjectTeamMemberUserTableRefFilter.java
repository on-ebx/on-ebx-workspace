/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.tablereffilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

/**
 */
public abstract class ProjectTeamMemberUserTableRefFilter
	implements TableRefFilter, ProjectPathCapable
{
	private static final String MESSAGE = "User must be in the specified role.";

	@Override
	public boolean accept(Adaptation adaptation, ValueContext valueContext)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		String projectStatus = (String) AdaptationUtil.followFK(
			valueContext,
			Path.PARENT.add(pathConfig.getProjectTeamMemberProjectFieldPath()),
			pathConfig.getProjectStatusFieldPath());
		String userPK = adaptation.getOccurrencePrimaryKey().format();
		// If it's not in process then allow the user if he's already set on the saved project team member
		if (!pathConfig.isInProcessProjectStatus(projectStatus))
		{
			Adaptation savedProjectTeamMemberRecord = AdaptationUtil.getRecordForValueContext(valueContext);
			if (savedProjectTeamMemberRecord != null
				&& userPK.equals(savedProjectTeamMemberRecord.getString(pathConfig.getProjectTeamMemberUserFieldPath())))
			{
				return true;
			}
		}
		String projectRoleName = (String) valueContext.getValue(Path.PARENT.add(pathConfig.getProjectTeamMemberProjectRoleFieldPath()));
		if (projectRoleName == null)
		{
			return false;
		}
		return isUserInRole(
			valueContext,
			UserReference.forUser(userPK),
			Role.forSpecificRole(projectRoleName));
	}

	// Default behavior is to lookup if user is in role using directory, but can be overrriden for different behavior
	protected boolean isUserInRole(ValueContext valueContext, UserReference user, Role role)
	{
		DirectoryHandler dirHandler = DirectoryHandler.getInstance(valueContext.getHome()
			.getRepository());
		return dirHandler.isUserInRole(user, role);
	}

	protected String createMessage()
	{
		return MESSAGE;
	}

	@Override
	public void setup(TableRefFilterContext context)
	{
		context.addFilterErrorMessage(createMessage());
	}

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return createMessage();
	}
}
