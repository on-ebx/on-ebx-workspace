package com.orchestranetworks.ps.admin.devartifacts.service;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.form.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * Presents a form to the user that allows them to configure the import before launching it
 */
public class ImportDevArtifactsUserService<S extends DataspaceEntitySelection>
	extends
	DevArtifactsUserService<S>
{
	private static final String WORKFLOW_GROUP_NAME = "workflow";
	private final Path workflowGroupPath = Path.SELF.add(Path.parse(WORKFLOW_GROUP_NAME));

	public ImportDevArtifactsUserService(
		ImportDevArtifactsImpl impl,
		DevArtifactsConfigFactory configFactory,
		Map<String, String[]> paramMap)
	{
		super(impl, configFactory, paramMap);
	}

	@Override
	public List<File> getFiles()
	{
		return null;
	}

	@Override
	protected void addExtraInputs(
		UserServiceSetupObjectContext<S> aContext,
		BeanDefinition def,
		ObjectKey key)
	{
		super.addExtraInputs(aContext, def, key);

		ImportDevArtifactsConfig importConfig = (ImportDevArtifactsConfig) config;
		Path publishWFModels = Path.SELF
			.add(Path.parse(ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS));
		defineElement(
			def,
			publishWFModels,
			"Publish workflow models",
			SchemaTypeName.XS_BOOLEAN,
			Boolean.valueOf(importConfig.isPublishWorkflowModels()));
		Path replaceModePath = Path.SELF.add(Path.parse(ImportDevArtifactsImpl.PARAM_REPLACE_MODE));
		defineElement(
			def,
			replaceModePath,
			"Replace Mode (Will OVERWRITE your changes)",
			SchemaTypeName.XS_BOOLEAN,
			Boolean.valueOf(ImportSpecMode.REPLACE.equals(importConfig.getImportMode())));
		Path skipNeFiles = Path.SELF
			.add(Path.parse(ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES));
		defineElement(
			def,
			skipNeFiles,
			"Skip non-existing files",
			SchemaTypeName.XS_BOOLEAN,
			Boolean.valueOf(importConfig.isSkipNonExistingFiles()));
		BeanElement workflowGroup = def.createComplexElement(workflowGroupPath);
		workflowGroup.setLabel("Workflow Selection");
		workflowGroup.setDefaultCollapseMode(false);
		addWorkflows(aContext, def, key);
	}

	protected void addWorkflows(
		UserServiceSetupObjectContext<S> aContext,
		BeanDefinition def,
		ObjectKey key)
	{
		// The workflows to show will be based on which files are available
		File workflowsFolder = config.getWorkflowsFolder();
		List<String> workflowNames = DevArtifactsUtil.getWorkflowsFromFolder(workflowsFolder);
		Repository repo = aContext.getRepository();
		AdaptationHome wfDataSpace = AdminUtil.getWorkflowModelsDataSpace(repo);
		Locale locale = Locale.getDefault();

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
			Path path = workflowGroupPath.add(Path.parse(componentName));
			defineElement(def, path, label, SchemaTypeName.XS_BOOLEAN, Boolean.TRUE);
		}
	}

	protected String getInformation()
	{
		return "Only the workflow models selected below will be imported. (All are selected by default.)";
	}

	@Override
	protected void writeForm(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter,
		ObjectKey objectKey)
	{
		super.writeForm(aPaneContext, aWriter, objectKey);

		// Write out javascript so that the checkboxes will show/hide based on what's selected
		SchemaNode rootNode = aPaneContext.getValueContext(objectKey).getNode();
		writeEnvironmentCopySelectedJS(aWriter, rootNode);
		writeReplaceModeJS(aWriter, rootNode);

		// If replace is selected by default then we want to call the logic from the onclick up front
		if (ImportSpecMode.REPLACE.equals(((ImportDevArtifactsConfig) config).getImportMode()))
		{
			aWriter.addJS_cr("replaceModeSelected();");
		}
		// Similar to replace, do the onclick for copy environment
		if (config.isEnvironmentCopy())
		{
			aWriter.addJS_cr("environmentCopySelected();");
		}
	}

	@Override
	protected void writeNode(UserServicePaneWriter aWriter, SchemaNode node, boolean top)
	{
		Step lastStep = node.getPathInAdaptation().getLastStep();
		String paramStr = lastStep.format();
		if (DevArtifactsBase.PARAM_ENVIRONMENT_COPY.equals(paramStr))
		{
			writeCheckBox(aWriter, lastStep.toSelfPath(), paramStr, "environmentCopySelected");
		}
		else if (ImportDevArtifactsImpl.PARAM_REPLACE_MODE.equals(paramStr))
		{
			writeCheckBox(aWriter, lastStep.toSelfPath(), paramStr, "replaceModeSelected");
		}
		else if (ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES.equals(paramStr)
			|| ImportDevArtifactsImpl.PARAM_PUBLISH_WORKFLOW_MODELS.equals(paramStr))
		{
			writeCheckBox(aWriter, lastStep.toSelfPath(), paramStr, null);
		}
		else if (WORKFLOW_GROUP_NAME.equals(paramStr))
		{
			writeWorkflowGroup(aWriter, node);
		}
		else if (paramStr.startsWith(ImportDevArtifactsImpl.PARAM_WORKFLOW_PREFIX))
		{
			writeCheckBox(aWriter, workflowGroupPath.add(lastStep), paramStr, null);
		}
		else
		{
			super.writeNode(aWriter, node, top);
		}
	}

	private void writeWorkflowGroup(UserServicePaneWriter aWriter, SchemaNode node)
	{
		UIFormGroup formGroup = aWriter.newFormGroup(workflowGroupPath);
		formGroup.setId(WORKFLOW_GROUP_NAME);
		aWriter.startFormGroup(formGroup);
		SchemaNode[] children = node.getNodeChildren();
		for (SchemaNode childNode : children)
		{
			writeNode(aWriter, childNode, false);
		}
		aWriter.endFormGroup();
	}

	private void writeEnvironmentCopySelectedJS(UserServicePaneWriter aWriter, SchemaNode rootNode)
	{
		aWriter.addJS_cr("function environmentCopySelected()");
		aWriter.addJS_cr("{");

		writeJSInitChkVis(aWriter);

		writeJSSetChkVis(aWriter, Path.SELF.add(DevArtifactsBase.PARAM_ENVIRONMENT_COPY));

		writeJSSetFieldVisibility(aWriter, ImportDevArtifactsImpl.PARAM_REPLACE_MODE);
		writeJSSetFieldVisibility(aWriter, ImportDevArtifactsImpl.PARAM_SKIP_NONEXISTING_FILES);

		aWriter.addJS_cr("  if (chk.length == 0)");
		aWriter.addJS_cr("  {");
		writeJSSetChkVis(aWriter, Path.SELF.add(ImportDevArtifactsImpl.PARAM_REPLACE_MODE));
		aWriter.addJS_cr("  }");

		writeJSSetWorkflowsVisibility(aWriter, rootNode.getNode(workflowGroupPath));

		aWriter.addJS_cr("}");
	}

	private void writeReplaceModeJS(UserServicePaneWriter aWriter, SchemaNode rootNode)
	{
		aWriter.addJS_cr("function replaceModeSelected()");
		aWriter.addJS_cr("{");

		writeJSInitChkVis(aWriter);
		writeJSSetChkVis(aWriter, Path.SELF.add(ImportDevArtifactsImpl.PARAM_REPLACE_MODE));

		writeJSSetWorkflowsVisibility(aWriter, rootNode.getNode(workflowGroupPath));

		aWriter.addJS_cr("}");
		// It's selected by default so want to call the logic from the onclick up front
		aWriter.addJS_cr("replaceModeSelected();");
	}

	private void writeJSInitChkVis(UserServicePaneWriter aWriter)
	{
		aWriter.addJS_cr("  var chk;");
		aWriter.addJS_cr("  var vis;");
	}

	private void writeJSSetChkVis(UserServicePaneWriter aWriter, Path paramPath)
	{
		// Get the value of the controlling checkbox
		aWriter.addJS("  chk = ");
		aWriter.addJS_getNodeValue(paramPath);
		aWriter.addJS_cr(";");

		// It will be checked if the result is not an empty array.
		// Make visibility hidden if checked and visible if not checked.
		aWriter.addJS_cr("  if (chk.length > 0)");
		aWriter.addJS_cr("  {");
		aWriter.addJS_cr("    vis = 'hidden';");

		aWriter.addJS_cr("  }");
		aWriter.addJS_cr("  else");
		aWriter.addJS_cr("  {");
		aWriter.addJS_cr("    vis = 'visible';");
		aWriter.addJS_cr("  }");
	}

	private void writeJSSetFieldVisibility(UserServicePaneWriter aWriter, String fieldId)
	{
		aWriter.addJS("  document.getElementById('" + fieldId + "').style.visibility=vis;");
	}

	private void writeJSSetWorkflowsVisibility(
		UserServicePaneWriter aWriter,
		SchemaNode wfGroupNode)
	{
		writeJSSetFieldVisibility(aWriter, workflowGroupPath.getLastStep().format());

		for (SchemaNode wfModelNode : wfGroupNode.getNodeChildren())
		{
			writeJSSetFieldVisibility(
				aWriter,
				wfModelNode.getPathInAdaptation().getLastStep().format());
		}
	}
}
