package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * An interface for specifying how to create/configure a DTO for the Dev Artifacts commend line
 */
public interface DevArtifactsMainDTOFactory extends DevArtifactsMainCommandLineCapable
{
	/**
	 * Create the DTO
	 * 
	 * @param commandLine the command line object
	 * @param argumentIterator an interator over the command line arguments
	 * @return the dto
	 */
	DevArtifactsDTO createDTO(CommandLine commandLine, Iterator<String> argumentIterator);

	/**
	 * Configure the DTO. This will be called after it's been created, to possibly further configure it
	 * with information that wasn't related to its creation, or wasn't available at the time of its creation.
	 * 
	 * @param dto the DTO
	 * @param commandLine the command line object
	 * @param argumentIterator an interator over the command line arguments
	 * @throws OperationException if an error occurs while configuring it
	 */
	void configureDTO(
		DevArtifactsDTO dto,
		CommandLine commandLine,
		Iterator<String> argumentIterator)
		throws OperationException;
}
