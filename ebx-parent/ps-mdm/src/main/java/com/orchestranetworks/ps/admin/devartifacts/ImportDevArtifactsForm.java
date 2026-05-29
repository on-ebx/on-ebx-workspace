package com.orchestranetworks.ps.admin.devartifacts;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

/**
 * Presents a form to the user that allows them to configure the import before launching it
 * 
 * @deprecated Use the user service instead. See {@link com.orchestranetworks.ps.admin.devartifacts.service}.
 */
@Deprecated
public class ImportDevArtifactsForm extends DevArtifactsForm
{
	private static final String DEFAULT_SERVLET_PATH = "/ImportDevArtifacts";

	private String servletPath = DEFAULT_SERVLET_PATH;

	@Override
	public String getServletPath()
	{
		return servletPath;
	}

	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}

	@Override
	protected void writeExtraInputs(ServiceContext sContext) throws OperationException
	{
		writeCheckboxRow(
			sContext,
			"Replace Mode (Will OVERWRITE your changes)",
			ImportDevArtifactsImpl.PARAM_REPLACE_MODE,
			true,
			"replaceModeSelected();");
		writeCheckboxRow(
			sContext,
			"Skip non-existing files",
			ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES,
			false,
			null);
		writeCheckboxRow(
			sContext,
			"Publish workflow models",
			ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS,
			true,
			null);
		writeWorkflowSelectionPanel(sContext);
		writeReplaceModeJS(sContext);
	}

	protected void writeReplaceModeJS(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.addJS_cr("function replaceModeSelected()");
		writer.addJS_cr("{");
		// Find whether replace mode was checked
		writer.addJS_cr(
			"  var chk = document.getElementById('" + ImportDevArtifactsImpl.PARAM_REPLACE_MODE
				+ "').checked;");
		// Get all the input elements for the form
		writer.addJS_cr("  var allInputs = document.getElementsByTagName('input');");

		// Loop through each input element
		writer.addJS_cr("  for (var i = 0; i < allInputs.length; i++)");
		writer.addJS_cr("  {");
		// If it's a checkbox and it's a workflow checkbox
		writer.addJS_cr(
			"    if (allInputs[i].getAttribute('type') == 'checkbox' && (allInputs[i].getAttribute('id') == '"
				+ ImportDevArtifactsImpl.PARAM_SELECT_ALL_WORKFLOWS
				+ "' || (allInputs[i].getAttribute('id').length > "
				+ ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX.length()
				+ " && allInputs[i].getAttribute('id').substring(0,"
				+ ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX.length() + ") == '"
				+ ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX + "')))");
		writer.addJS_cr("    {");
		// Check or uncheck it and enable or disable it
		writer.addJS_cr("      allInputs[i].checked = chk;");
		writer.addJS_cr("      allInputs[i].disabled = chk;");
		writer.addJS_cr("    }");
		writer.addJS_cr("  }");
		writer.addJS_cr("}");
		// It's selected by default so want to call the logic from the onclick up front
		writer.addJS_cr("replaceModeSelected();");
	}

	protected void writeWorkflowSelectionPanel(ServiceContext sContext) throws OperationException
	{
		UIComponentWriter writer = sContext.getUIComponentWriter();
		writer.startBorder(false);
		writer.startTableFormRow();

		writeCheckboxRow(
			sContext,
			"Select All",
			ImportDevArtifactsImpl.PARAM_SELECT_ALL_WORKFLOWS,
			false,
			"selectAllWorkflowsSelected();");

		writeSeparator(sContext);

		List<CheckboxConfig> cbConfigs = getWorkflowCheckboxConfigs(sContext);
		for (CheckboxConfig cbConfig : cbConfigs)
		{
			writeCheckboxRow(
				sContext,
				cbConfig.getLabel(),
				cbConfig.getComponentName(),
				false,
				null);
		}

		writer.endTableFormRow();
		writer.endBorder();

		writeWorkflowSelectionJS(sContext, cbConfigs);
	}

	protected List<CheckboxConfig> getWorkflowCheckboxConfigs(ServiceContext sContext)
		throws OperationException
	{
		// The workflows to show will be based on which files are available
		File workflowsFolder;
		try
		{
			workflowsFolder = getWorkflowsFolder();
		}
		catch (IOException ex)
		{
			throw OperationException.createError(ex);
		}
		List<String> workflowNames = DevArtifactsUtil.getWorkflowsFromFolder(workflowsFolder);
		Repository repo = sContext.getCurrentHome().getRepository();
		AdaptationHome wfDataSpace = AdminUtil.getWorkflowModelsDataSpace(repo);
		Locale locale = sContext.getLocale();

		List<CheckboxConfig> cbConfigs = new ArrayList<>();
		// Loop through each name found in the folder
		for (String workflowName : workflowNames)
		{
			// Find the workflow model associated with that name
			Adaptation wfDataSet = wfDataSpace
				.findAdaptationOrNull(AdaptationName.forName(workflowName));
			String componentName = ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX + workflowName;
			String label;
			// If none found, then it's new so indicate that in the label
			if (wfDataSet == null)
			{
				label = workflowName + " (NEW)";
			}
			// Otherwise get the label for the workflow model
			else
			{
				label = wfDataSet.getLabelOrName(locale);
			}
			cbConfigs.add(new CheckboxConfig(label, componentName));
		}
		return cbConfigs;
	}

	protected void writeWorkflowSelectionJS(
		ServiceContext sContext,
		List<CheckboxConfig> workflowCBConfigs)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.addJS_cr("function selectAllWorkflowsSelected()");
		writer.addJS_cr("{");
		// Select or unselect all the checkboxes when Select All is clicked
		for (CheckboxConfig cbConfig : workflowCBConfigs)
		{
			writer.addJS_cr(
				"  document.getElementById('" + cbConfig.getComponentName()
					+ "').checked = document.getElementById('"
					+ ImportDevArtifactsImpl.PARAM_SELECT_ALL_WORKFLOWS + "').checked;");

		}
		writer.addJS_cr("}");
	}

	@Override
	protected void writeEnvironmentCopyPanel(ServiceContext sContext)
	{
		writeCheckboxRow(
			sContext,
			"Environment Copy",
			DevArtifactsBase.PARAM_ENVIRONMENT_COPY,
			false,
			"copyEnvSelected();");

		writeEnvironmentCopyJS(sContext);
	}

	protected void writeEnvironmentCopyJS(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.addJS_cr("function copyEnvSelected()");
		writer.addJS_cr("{");
		// Find whether env copy was checked
		writer.addJS_cr(
			"  var chk = document.getElementById('" + DevArtifactsBase.PARAM_ENVIRONMENT_COPY
				+ "').checked;");
		// Get all the input elements for the form
		writer.addJS_cr("  var allInputs = document.getElementsByTagName('input');");

		// Loop through each input element
		writer.addJS_cr("  for (var i = 0; i < allInputs.length; i++)");
		writer.addJS_cr("  {");
		// If it's a checkbox and it's not the env copy checkbox itself
		writer.addJS_cr(
			"    if (allInputs[i].getAttribute('type') == 'checkbox' && allInputs[i].getAttribute('id') != '"
				+ DevArtifactsBase.PARAM_ENVIRONMENT_COPY + "')");
		writer.addJS_cr("    {");
		// Uncheck it and enable or disable it
		writer.addJS_cr("      allInputs[i].checked = false;");
		writer.addJS_cr("      allInputs[i].disabled = chk;");
		writer.addJS_cr("    }");
		writer.addJS_cr("  }");
		writer.addJS_cr("}");
	}

	protected void writeInformationPanel(ServiceContext sContext)
	{
		UIComponentWriter writer = sContext.getUIComponentWriter();
		writer.add_cr("<div style='margin: 5px'>");
		writer.add_cr(
			"  <p>In replace mode, all workflow models will be imported. Otherwise only the ones selected below will be imported (unless environment copy is selected).</p>");
		writer.add_cr("</div>");
	}

	private File getWorkflowsFolder() throws IOException
	{
		// Get properties file from system property
		String propertiesFile = System
			.getProperty(DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY);
		// If it wasn't specified as a system property, then use the configured value
		if (propertiesFile == null)
		{
			propertiesFile = getPropertiesFile();
		}
		DevArtifactsPropertyFileHelper helper = new DevArtifactsPropertyFileHelper(propertiesFile);
		Properties props = helper.getProperties();

		String workflowsFoldername;
		String artifactsFoldername = props
			.getProperty(DevArtifactsPropertyFileHelper.PROPERTY_ARTIFACTS_FOLDER);
		// Backwards compatibility - eventually will get rid of these options
		if (artifactsFoldername == null || "".equals(artifactsFoldername))
		{
			workflowsFoldername = props
				.getProperty(DevArtifactsPropertyFileHelper.PROPERTY_WORKFLOWS_FOLDER);
		}
		else
		{
			workflowsFoldername = artifactsFoldername + File.separator
				+ DevArtifactsConstants.FOLDER_NAME_WORKFLOWS;
		}
		return new File(workflowsFoldername);
	}
}
