/**
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.workflow;

import org.apache.commons.lang3.*;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

@Deprecated
public class DataSetWorkflowLauncherForm extends WorkflowLauncherForm
{
	private static final String DEFAULT_WORKFLOW_INSTANCE_NAME_LABEL = "Name";
	private static final String DEFAULT_WORKFLOW_INSTANCE_DESCRIPTION_LABEL = "Description";
	private static final String DATASET_WORKFLOW_LAUNCHER = "/DataSetWorkflowLauncher";

	protected String workflowInstanceNameLabel = DEFAULT_WORKFLOW_INSTANCE_NAME_LABEL;
	protected String workflowInstanceDescriptionLabel = DEFAULT_WORKFLOW_INSTANCE_DESCRIPTION_LABEL;
	private String xpathToTable = null;
	private String workflowKey = null;

	public DataSetWorkflowLauncherForm(String workflowKey, String xpathToTable)
	{

		super();
		this.xpathToTable = xpathToTable;
		this.workflowKey = workflowKey;
	}

	public DataSetWorkflowLauncherForm()
	{
		super();
	}

	@Override
	protected void writeForm(ServiceContext sContext) throws OperationException
	{
		writeWorkflowInstanceNameRow(sContext);
		writeWorkflowInstanceDescriptionRow(sContext);

		if (xpathToTable != null)
		{
			writeHiddenInput(sContext, WorkflowConstants.PARAM_XPATH_TO_TABLE, xpathToTable);
		}
		if (workflowKey != null)
		{
			writeHiddenInput(sContext, WorkflowConstants.PARAM_WORKFLOW_NAME, workflowKey);
		}
	}

	protected void writeWorkflowInstanceNameRow(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec(workflowInstanceNameLabel));
		String initialWorkflowInstanceName = getInitialWorkflowInstanceName(sContext);
		String value = (initialWorkflowInstanceName == null ? ""
			: StringEscapeUtils.escapeHtml4(initialWorkflowInstanceName));

		writeInputTextField(
			sContext,
			workflowInstanceNameLabel,
			DataSetWorkflowLauncher.PARAM_WORKFLOW_INSTANCE_NAME,
			DataSetWorkflowLauncher.PARAM_WORKFLOW_INSTANCE_NAME,
			Integer.toString(getWorkflowInstanceNameFieldMaxLength(sContext)),
			value,
			true);

		writer.endFormRow();
	}

	protected void writeWorkflowInstanceDescriptionRow(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec(workflowInstanceDescriptionLabel));
		String initialWorkflowInstanceDescription = getInitialWorkflowInstanceDescription(sContext);
		String value = (initialWorkflowInstanceDescription == null ? ""
			: StringEscapeUtils.escapeHtml4(initialWorkflowInstanceDescription));

		writeInputTextAreaField(
			sContext,
			workflowInstanceDescriptionLabel,
			DataSetWorkflowLauncher.PARAM_WORKFLOW_INSTANCE_DESCRIPTION,
			DataSetWorkflowLauncher.PARAM_WORKFLOW_INSTANCE_DESCRIPTION,
			Integer.toString(getWorkflowInstanceNameFieldMaxLength(sContext)),
			value,
			"60",
			"5",
			false);

		writer.endFormRow();
	}

	protected String getInitialWorkflowInstanceName(ServiceContext sContext)
	{
		return null;
	}

	protected String getInitialWorkflowInstanceDescription(ServiceContext sContext)
	{
		return null;
	}

	protected int getWorkflowInstanceNameFieldMaxLength(ServiceContext sContext)
	{
		return 80;
	}

	protected int getWorkflowInstanceDescriptionFieldMaxLength(ServiceContext sContext)
	{
		return 2000;
	}

	@Override
	protected String getServletName()
	{
		return DATASET_WORKFLOW_LAUNCHER;
	}
}