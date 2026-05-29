package com.orchestranetworks.ps.util;

import org.apache.commons.exec.*;

import com.orchestranetworks.scheduler.*;

/**
 * 
 * Utility class aims to be used in scheduled task.
 * @author MCH
 */
public class SchedulerUtils
{

	/**
	 * @author MCH
	 * 
	 * Execute a command line.
	 *
	 * @param pContext the context
	 * @param pCommandLine the command line
	 */
	public static void executeCommandLine(
		final ScheduledExecutionContext pContext,
		final String pCommandLine)
	{
		CommandLine command = CommandLine.parse(pCommandLine);
		DefaultExecutor executor = DefaultExecutor.builder().get();
		int exitValue = 0;
		try
		{
			exitValue = executor.execute(command);
		}
		catch (Exception ex)
		{
			pContext.addExecutionInformation("Failed to run command line '" + pCommandLine + "'.");
			pContext.addExecutionInformation(ex.getLocalizedMessage());
			return;
		}
		if (exitValue < 0)
		{
			pContext.addExecutionInformation("Failed to run command line '" + pCommandLine + "'.");
		}
		else
		{
			pContext
				.addExecutionInformation("Command line '" + pCommandLine + "' ran successfully.");
		}
	}
}
