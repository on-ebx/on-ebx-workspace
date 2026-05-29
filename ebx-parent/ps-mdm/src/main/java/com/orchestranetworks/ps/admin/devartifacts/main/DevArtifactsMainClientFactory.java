package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.client.*;
import com.orchestranetworks.service.*;

/**
 * An interface for creating a Dev Artifacts client, for use with a Main command line invocation
 */
public interface DevArtifactsMainClientFactory extends DevArtifactsMainCommandLineCapable
{
	/**
	 * Create the client
	 * 
	 * @param commandLine the command line object
	 * @param argumentIterator an iterator over the command line arguments. If arguments need to be processed
	 *                         prior to creation of the client, then it can perhaps be advanced past the first
	 *                         element by the time this gets invoked. Likewise, it can be advanced in this method
	 *                         so that arguments that aren't related to creation of the client, but occur after,
	 *                         can then be processed.
	 * @return the client
	 * @throws OperationException if an error occurs while creating the client
	 */
	DevArtifactsClient createClient(CommandLine commandLine, Iterator<String> argumentIterator)
		throws OperationException;
}
