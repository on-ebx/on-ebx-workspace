package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * An abstract DTO Factory for Dev Artifacts that specifies the standard options for dev artifacts
 */
public abstract class AbstractDevArtifactsMainDTOFactory implements DevArtifactsMainDTOFactory
{
	protected static final String OPTION_ENV_COPY_SHORT = "e";
	protected static final String OPTION_ENV_COPY_LONG = "env-copy";
	protected static final String OPTION_NO_ENV_COPY_SHORT = "E";
	protected static final String OPTION_NO_ENV_COPY_LONG = "no-env-copy";
	protected static final String OPTION_REPLACE_MODE_SHORT = "r";
	protected static final String OPTION_REPLACE_MODE_LONG = "replace-mode";
	protected static final String OPTION_NO_REPLACE_MODE_SHORT = "R";
	protected static final String OPTION_NO_REPLACE_MODE_LONG = "no-replace-mode";
	protected static final String OPTION_SKIP_NON_EXISTING_FILES_SHORT = "s";
	protected static final String OPTION_SKIP_NON_EXISTING_FILES_LONG = "skip-nonexisting";
	protected static final String OPTION_NO_SKIP_NON_EXISTING_FILES_SHORT = "S";
	protected static final String OPTION_NO_SKIP_NON_EXISTING_FILES_LONG = "no-skip-nonexisting";
	protected static final String OPTION_PUBLISH_WORKFLOW_MODELS_SHORT = "p";
	protected static final String OPTION_PUBLISH_WORKFLOW_MODELS_LONG = "publish-workflows";
	protected static final String OPTION_NO_PUBLISH_WORKFLOW_MODELS_SHORT = "P";
	protected static final String OPTION_NO_PUBLISH_WORKFLOW_MODELS_LONG = "no-publish-workflows";
	protected static final String OPTION_WORKFLOW_MODELS_SHORT = "w";
	protected static final String OPTION_WORKFLOW_MODELS_LONG = "workflows";

	protected static final String OPTION_ARGUMENT_WORKFLOW_MODELS = "WORKFLOWS";

	@Override
	public void addOptions(Options options)
	{
		OptionGroup copyEnvGroup = new OptionGroup();
		copyEnvGroup.addOption(
			new Option(
				OPTION_ENV_COPY_SHORT,
				OPTION_ENV_COPY_LONG,
				false,
				"Override default Environment Copy value to true"));
		copyEnvGroup.addOption(
			new Option(
				OPTION_NO_ENV_COPY_SHORT,
				OPTION_NO_ENV_COPY_LONG,
				false,
				"Override default Environment Copy value to false"));
		options.addOptionGroup(copyEnvGroup);

		OptionGroup replaceModeGroup = new OptionGroup();
		replaceModeGroup.addOption(
			new Option(
				OPTION_REPLACE_MODE_SHORT,
				OPTION_REPLACE_MODE_LONG,
				false,
				"Override default Replace Mode value to true"));
		replaceModeGroup.addOption(
			new Option(
				OPTION_NO_REPLACE_MODE_SHORT,
				OPTION_NO_REPLACE_MODE_LONG,
				false,
				"Override default Replace Mode value to false"));
		options.addOptionGroup(replaceModeGroup);

		OptionGroup skipNonExistingFilesGroup = new OptionGroup();
		skipNonExistingFilesGroup.addOption(
			new Option(
				OPTION_SKIP_NON_EXISTING_FILES_SHORT,
				OPTION_SKIP_NON_EXISTING_FILES_LONG,
				false,
				"Override default Skip Non-existing Files value to true"));
		skipNonExistingFilesGroup.addOption(
			new Option(
				OPTION_NO_SKIP_NON_EXISTING_FILES_SHORT,
				OPTION_NO_SKIP_NON_EXISTING_FILES_LONG,
				false,
				"Override default Skip Non-existing Files value to false"));
		options.addOptionGroup(skipNonExistingFilesGroup);

		OptionGroup publishWorkflowModelsGroup = new OptionGroup();
		publishWorkflowModelsGroup.addOption(
			new Option(
				OPTION_PUBLISH_WORKFLOW_MODELS_SHORT,
				OPTION_PUBLISH_WORKFLOW_MODELS_LONG,
				false,
				"Override default Publish Workflow Models value to true"));
		publishWorkflowModelsGroup.addOption(
			new Option(
				OPTION_NO_PUBLISH_WORKFLOW_MODELS_SHORT,
				OPTION_NO_PUBLISH_WORKFLOW_MODELS_LONG,
				false,
				"Override default Publish Workflow Models value to false"));
		options.addOptionGroup(publishWorkflowModelsGroup);

		options.addOption(
			Option.builder(OPTION_WORKFLOW_MODELS_SHORT)
				.longOpt(OPTION_WORKFLOW_MODELS_LONG)
				.argName(OPTION_ARGUMENT_WORKFLOW_MODELS)
				.hasArg()
				.desc("Comma-separated list of workflow models")
				.build());
	}

	@Override
	public void configureDTO(
		DevArtifactsDTO dto,
		CommandLine commandLine,
		Iterator<String> argumentIterator)
		throws OperationException
	{
		if (commandLine.hasOption(OPTION_ENV_COPY_SHORT))
		{
			dto.setEnvironmentCopy(Boolean.TRUE);
		}
		else if (commandLine.hasOption(OPTION_NO_ENV_COPY_SHORT))
		{
			dto.setEnvironmentCopy(Boolean.FALSE);
		}

		if (commandLine.hasOption(OPTION_REPLACE_MODE_SHORT))
		{
			dto.setReplaceMode(Boolean.TRUE);
		}
		else if (commandLine.hasOption(OPTION_NO_REPLACE_MODE_SHORT))
		{
			dto.setReplaceMode(Boolean.FALSE);
		}

		if (commandLine.hasOption(OPTION_SKIP_NON_EXISTING_FILES_SHORT))
		{
			dto.setSkipNonExistingFiles(Boolean.TRUE);
		}
		else if (commandLine.hasOption(OPTION_NO_SKIP_NON_EXISTING_FILES_SHORT))
		{
			dto.setSkipNonExistingFiles(Boolean.FALSE);
		}

		if (commandLine.hasOption(OPTION_PUBLISH_WORKFLOW_MODELS_SHORT))
		{
			dto.setPublishWorkflowModels(Boolean.TRUE);
		}
		else if (commandLine.hasOption(OPTION_NO_PUBLISH_WORKFLOW_MODELS_SHORT))
		{
			dto.setPublishWorkflowModels(Boolean.FALSE);
		}

		if (commandLine.hasOption(OPTION_WORKFLOW_MODELS_SHORT))
		{
			String workflowModels = commandLine.getOptionValue(OPTION_WORKFLOW_MODELS_SHORT);
			String[] workflowModelArr = workflowModels.split(",");
			List<String> workflowModelList = Arrays.asList(workflowModelArr);
			dto.setWorkflowModels(workflowModelList);
		}
	}
}
