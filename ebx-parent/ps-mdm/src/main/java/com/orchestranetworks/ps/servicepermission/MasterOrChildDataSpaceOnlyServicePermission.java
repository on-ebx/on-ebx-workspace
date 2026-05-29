package com.orchestranetworks.ps.servicepermission;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class MasterOrChildDataSpaceOnlyServicePermission implements ServicePermission
{
	protected boolean allowInMaster = true;
	protected boolean allowInChild = true;
	protected boolean allowInsideWorkflow = true;
	protected boolean allowOutsideWorkflow = true;

	@Override
	public ActionPermission getPermission(
		SchemaNode schemaNode,
		Adaptation adaptation,
		Session session)
	{
		AdaptationHome dataSpace = adaptation.getHome();
		boolean canUserLaunch;
		String msgParam;
		if (isMasterDataSpace(dataSpace))
		{
			canUserLaunch = canUserLaunchInMasterDataSpace(dataSpace, session);
			msgParam = "master";
		}
		else
		{
			canUserLaunch = canUserLaunchInChildDataSpace(dataSpace, session);
			msgParam = "child";
		}
		if (canUserLaunch)
		{
			SessionInteraction interaction = session.getInteraction(true);
			if ((interaction != null && canUserLaunchInsideWorkflow(dataSpace, session))
				|| (interaction == null && canUserLaunchOutsideWorkflow(dataSpace, session)))
			{
				return ActionPermission.getEnabled();
			}
			return ActionPermission.getHidden(UserMessage.createError("This service can't be invoked from "
				+ (interaction == null ? "inside" : "outside") + " of a workflow."));
		}
		return ActionPermission.getHidden(UserMessage.createError("Not allowed to launch service from a "
			+ msgParam + " data space."));
	}

	protected boolean isMasterDataSpace(AdaptationHome dataSpace)
	{
		AdaptationHome parentBranch = dataSpace.getParentBranch();
		return parentBranch != null && parentBranch.isBranchReference();
	}

	protected boolean canUserLaunchInMasterDataSpace(AdaptationHome dataSpace, Session session)
	{
		return allowInMaster;
	}

	protected boolean canUserLaunchInChildDataSpace(AdaptationHome dataSpace, Session session)
	{
		return allowInChild;
	}

	protected boolean canUserLaunchInsideWorkflow(AdaptationHome dataSpace, Session session)
	{
		return allowInsideWorkflow;
	}

	protected boolean canUserLaunchOutsideWorkflow(AdaptationHome dataSpace, Session session)
	{
		return allowOutsideWorkflow;
	}

	public boolean isAllowInMaster()
	{
		return this.allowInMaster;
	}

	public void setAllowInMaster(boolean allowInMaster)
	{
		this.allowInMaster = allowInMaster;
	}

	public boolean isAllowInChild()
	{
		return this.allowInChild;
	}

	public void setAllowInChild(boolean allowInChild)
	{
		this.allowInChild = allowInChild;
	}

	public boolean isAllowInsideWorkflow()
	{
		return this.allowInsideWorkflow;
	}

	public void setAllowInsideWorkflow(boolean allowInsideWorkflow)
	{
		this.allowInsideWorkflow = allowInsideWorkflow;
	}

	public boolean isAllowOutsideWorkflow()
	{
		return this.allowOutsideWorkflow;
	}

	public void setAllowOutsideWorkflow(boolean allowOutsideWorkflow)
	{
		this.allowOutsideWorkflow = allowOutsideWorkflow;
	}
}