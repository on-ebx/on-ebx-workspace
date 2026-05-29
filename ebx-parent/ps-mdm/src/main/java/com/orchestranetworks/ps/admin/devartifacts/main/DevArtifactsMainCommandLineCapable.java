package com.orchestranetworks.ps.admin.devartifacts.main;

import org.apache.commons.cli.*;

/**
 * An interface for any Dev Artifacts related class that's capable of being configured via command line
 */
public interface DevArtifactsMainCommandLineCapable
{
	/**
	 * Add the given command line options
	 * 
	 * @param options the options
	 */
	void addOptions(Options options);

	/**
	 * Get the syntax to print for the usage of the arguments
	 * 
	 * @return the syntax, or <code>null</code> if no syntax should be printed
	 */
	String getArgumentUsageSyntax();

	/**
	 * Get a footer string to print after the argument usage syntax
	 * 
	 * @return the footer, or <code>null</code> if there is no footer
	 */
	String getArgumentUsageFooter();
}
