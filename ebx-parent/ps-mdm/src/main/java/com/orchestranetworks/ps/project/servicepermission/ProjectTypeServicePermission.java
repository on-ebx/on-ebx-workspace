package com.orchestranetworks.ps.project.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public abstract class ProjectTypeServicePermission extends MasterDataSpaceOnlyServicePermission
	implements ProjectPathCapable
{
	protected static final String PROJECT_TYPE_SEPARATOR = ";";

	private String projectType;
	private List<String> projectTypeList;

	@Override
	public ActionPermission getPermission(
		SchemaNode schemaNode,
		Adaptation adaptation,
		Session session)
	{
		ActionPermission permission = super.getPermission(schemaNode, adaptation, session);

		if (!ActionPermission.getEnabled().equals(permission))
		{
			return permission;
		}

		if (adaptation.isSchemaInstance())
		{
			return ActionPermission.getEnabled();
		}

		return verifyProjectType(adaptation);
	}

	public ActionPermission verifyProjectType(Adaptation projectRecord)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();

		String recordProjectType = projectRecord.getString(pathConfig.getProjectProjectTypeFieldPath());

		if (recordProjectType != null && projectTypeList != null
			&& projectTypeList.contains(recordProjectType))
		{
			return ActionPermission.getEnabled();
		}

		return ActionPermission.getHidden(UserMessage.createInfo("This service is only applicable for projects with project type "
			+ this.getProjectType()));
	}

	public String getProjectType()
	{
		return this.projectType;
	}

	public void setProjectType(String projectType)
	{
		this.projectType = projectType;
		projectTypeList = (projectType == null) ? null
			: Arrays.asList(projectType.split(PROJECT_TYPE_SEPARATOR));
	}
}
