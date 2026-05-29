package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

public class SetChildDataSpaceLabelScriptTask extends ScriptTask
{
	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		// Update Child Data Space Label to contain the Name of the Workflow + the WorkingDataSpace Name
		String dataSpaceId = getDataSpaceId(context);
		Repository repository = context.getRepository();
		Session session = context.getSession();
		AdaptationHome home = repository.lookupHome(HomeKey.forBranchName(dataSpaceId));
		if (home == null)
		{
			throw OperationException.createError("DataSpace " + dataSpaceId + " has not been found");
		}
		ProcessInstance processInstance = context.getProcessInstance();
		String workflowLabel = getWorkflowLabel(processInstance, repository, session);
		String childDataSpaceLabel = getChildDataSpaceLabel(workflowLabel, dataSpaceId);
		repository.setDocumentationLabel(home, childDataSpaceLabel, session.getLocale(), session);
	}

	protected String getDataSpaceId(DataContextReadOnly dataContext)
	{
		return dataContext.getVariableString(WorkflowConstants.PARAM_WORKING_DATA_SPACE);
	}

	protected boolean includeLaunchedBy()
	{
		return true;
	}

	protected String getWorkflowLabel(
		ProcessInstance processInstance,
		Repository repository,
		Session session) throws OperationException
	{
		String workflowLabel = processInstance.getLabel().formatMessage(session.getLocale());
		if (includeLaunchedBy())
		{
			String launchedByString = ", launched by ";
			UserReference creator = processInstance.getCreator();
			if (!workflowLabel.contains(launchedByString) && creator != null)
			{
				workflowLabel += (launchedByString + creator.getUserId());
			}
		}
		return workflowLabel;
	}

	protected String getChildDataSpaceLabel(String workflowLabel, String dataSpaceId)
	{
		return workflowLabel + " (" + dataSpaceId + ")";
	}
}
