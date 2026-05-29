/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.workflow.launcher;

import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

/**
 */
@Deprecated
public abstract class NewProjectWorkflowLauncherForm extends WorkflowLauncherForm
{
	private static final String DEFAULT_PROJECT_NAME_LABEL = "Project Name";

	private String projectNameLabel;

	protected NewProjectWorkflowLauncherForm()
	{
		this(DEFAULT_PROJECT_NAME_LABEL);
	}

	protected NewProjectWorkflowLauncherForm(String projectNameLabel)
	{
		this.projectNameLabel = projectNameLabel;
	}

	protected abstract String getXPathToTable();

	protected abstract String getProjectType();

	protected void writeForm(ServiceContext sContext) throws OperationException
	{
		writeProjectNameRow(sContext);
		writeLauncherHiddenInputs(sContext);
	}

	protected void writeProjectNameRow(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec(projectNameLabel));
		String initialProjectName = getInitialProjectName(sContext);
		writer.add_cr(
			"<input id='" + SubjectWorkflowLauncher.PARAM_PROJECT_NAME + "' name='"
				+ SubjectWorkflowLauncher.PARAM_PROJECT_NAME
				+ "' type='text' class='ebx_APV' maxLength='"
				+ getProjectNameFieldMaxLength(sContext) + "'"
				// TODO: Apache doesn't handle escaping apostrophes (says it's not technically part
				// of standard)
				// so need to enclose in a quotation mark. Perhaps should go through rest of code
				// and do the same
				+ (initialProjectName == null ? ""
					: " value=\"" + StringEscapeUtils.escapeHtml4(initialProjectName) + "\"")
				+ "/>");
		writer.endFormRow();
	}

	protected String getInitialProjectName(ServiceContext sContext)
	{
		return null;
	}

	protected int getProjectNameFieldMaxLength(ServiceContext sContext)
	{
		return 80;
	}

	protected void writeLauncherHiddenInputs(ServiceContext sContext)
	{
		writeHiddenInput(sContext, WorkflowConstants.PARAM_XPATH_TO_TABLE, getXPathToTable());
		writeHiddenInput(sContext, SubjectWorkflowLauncher.PARAM_PROJECT_TYPE, getProjectType());
	}
}
