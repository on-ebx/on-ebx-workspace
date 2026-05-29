package com.orchestranetworks.ps.project.servicepermission;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public abstract class SubjectStatusOnlyServicePermission
	extends
	SubjectAlreadyInWorkflowServicePermission
{
	private static final String STATUS_SEPARATOR = ";";

	protected String status;

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
		return verifySubjectStatus(adaptation, session);
	}

	protected ActionPermission verifySubjectStatus(Adaptation adaptation, Session session)
	{
		if (status == null)
		{
			return ActionPermission.getEnabled();
		}
		SubjectPathConfig subjectPathConfig = getSubjectPathConfig(adaptation);
		Path subjectStatusFieldPath = subjectPathConfig.getSubjectStatusFieldPath();
		if (subjectStatusFieldPath == null)
		{
			return ActionPermission.getEnabled();
		}
		String subjectStatus = adaptation.getString(subjectStatusFieldPath);
		String[] statuses = status.split(STATUS_SEPARATOR);
		boolean statusMatches = false;
		for (int i = 0; i < statuses.length && !statusMatches; i++)
		{
			statusMatches = statuses[i].trim().equals(subjectStatus);
		}
		if (!statusMatches)
		{
			String subjectTableLabel = adaptation.getContainerTable()
				.getTableNode()
				.getLabel(session.getLocale());
			StringBuilder bldr = new StringBuilder();
			for (int i = 0; i < statuses.length; i++)
			{
				bldr.append(statuses[i]);
				if (i < statuses.length - 1)
				{
					bldr.append(" or ");
				}
			}
			return ActionPermission.getHidden(UserMessage.createInfo("This service is only applicable for a "
				+ subjectTableLabel + " with status " + bldr.toString() + "."));
		}
		return ActionPermission.getEnabled();
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}