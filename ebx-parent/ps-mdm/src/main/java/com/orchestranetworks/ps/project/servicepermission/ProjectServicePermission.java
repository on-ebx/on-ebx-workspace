package com.orchestranetworks.ps.project.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public abstract class ProjectServicePermission extends MasterDataSpaceOnlyServicePermission
	implements ProjectPathCapable
{
	private String projectStatus;
	private boolean resumableOnly;

	@Override
	public ActionPermission getPermission(
		SchemaNode schemaNode,
		Adaptation adaptation,
		Session session)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();

		ActionPermission permission = super.getPermission(schemaNode, adaptation, session);
		if (!ActionPermission.getEnabled().equals(permission))
		{
			return permission;
		}
		if (adaptation.isSchemaInstance())
		{
			return ActionPermission.getEnabled();
		}

		if (projectStatus != null)
		{
			if (!projectStatus.equals(adaptation.getString(pathConfig.getProjectStatusFieldPath())))
			{
				return ActionPermission.getHidden(UserMessage.createInfo("This service is only applicable for projects with status "
					+ projectStatus + "."));
			}
		}

		if (resumableOnly)
		{
			if (!adaptation.get_boolean(pathConfig.getProjectIsResumableFieldPath()))
			{
				return ActionPermission.getHidden(UserMessage.createInfo("This service is only applicable for resumable projects."));
			}
			return ActionPermission.getEnabled();
		}
		// Only verify team member if it's not resumable only
		return verifyProjectTeamMember(adaptation, session.getUserReference().getUserId());
	}

	public ActionPermission verifyProjectTeamMember(Adaptation projectRecord, String userId)
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		List<Adaptation> projectTeamList = AdaptationUtil.getLinkedRecordList(
			projectRecord,
			pathConfig.getProjectTeamMembersFieldPath());
		for (Adaptation projectTeamMember : projectTeamList)
		{
			if (userId.equals(projectTeamMember.getString(pathConfig.getProjectTeamMemberUserFieldPath())))
			{
				return ActionPermission.getEnabled();
			}
		}
		return ActionPermission.getHidden(UserMessage.createInfo("You must be a member of the Project Team to launch this service."));
	}

	public String getProjectStatus()
	{
		return this.projectStatus;
	}

	public void setProjectStatus(String projectStatus)
	{
		this.projectStatus = projectStatus;
	}

	public boolean isResumableOnly()
	{
		return this.resumableOnly;
	}

	public void setResumableOnly(boolean resumableOnly)
	{
		this.resumableOnly = resumableOnly;
	}
}