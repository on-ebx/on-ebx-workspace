package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * A DTO factory that specifies a properties file for the DTO, in addition to the standard options
 */
public class DevArtifactsMainPropertiesFileDTOFactory extends AbstractDevArtifactsMainDTOFactory
{
	protected static final String OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_SHORT = "f";
	protected static final String OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_LONG = "prop-file-sys-prop";

	protected static final String OPTION_ARGUMENT_PROPERTIES_FILE_SYSTEM_PROPERTY = "PROPERTY";

	@Override
	public DevArtifactsDTO createDTO(CommandLine commandLine, Iterator<String> argumentIterator)
	{
		return new DevArtifactsPropertiesFileDTO();
	}

	@Override
	public void addOptions(Options options)
	{
		super.addOptions(options);

		options.addOption(
			Option.builder(OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_SHORT)
				.longOpt(OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_LONG)
				.argName(OPTION_ARGUMENT_PROPERTIES_FILE_SYSTEM_PROPERTY)
				.hasArg()
				.desc("System property specifying the Dev Artifacts properties file")
				.build());
	}

	@Override
	public String getArgumentUsageSyntax()
	{
		return null;
	}

	@Override
	public String getArgumentUsageFooter()
	{
		return null;
	}

	@Override
	public void configureDTO(
		DevArtifactsDTO dto,
		CommandLine commandLine,
		Iterator<String> argumentIterator)
		throws OperationException
	{
		super.configureDTO(dto, commandLine, argumentIterator);

		if (commandLine.hasOption(OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_SHORT))
		{
			String property = commandLine
				.getOptionValue(OPTION_PROPERTIES_FILE_SYSTEM_PROPERTY_SHORT);
			((DevArtifactsPropertiesFileDTO) dto).setPropertiesFileSystemProperty(property);
		}
	}
}
