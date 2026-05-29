package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.service.*;

/**
 * Abstract Main client factory that invokes Dev Artifacts via its REST interface.
 * A concrete subclass must specify the authentication mechanism.
 */
public abstract class AbstractDevArtifactsMainRESTClientFactory
	implements DevArtifactsMainClientFactory
{
	protected static final String ARGUMENT_URL = "URL";

	@Override
	public void addOptions(Options options)
	{
		// do nothing
	}

	@Override
	public String getArgumentUsageSyntax()
	{
		return ARGUMENT_URL;
	}

	@Override
	public String getArgumentUsageFooter()
	{
		return ARGUMENT_URL + " is the full URL to the Dev Artifacts REST service."
			+ System.lineSeparator()
			+ "i.e. http://<host>:<port>/<module>/rest/v1/ps/devartifacts/export/execute";
	}

	protected String getURL(CommandLine commandLine, Iterator<String> argumentIterator)
		throws OperationException
	{
		String url;
		if (argumentIterator.hasNext())
		{
			url = argumentIterator.next();
		}
		else
		{
			throw OperationException
				.createError("Argument " + ARGUMENT_URL + " must be specified.");
		}
		return url;
	}
}
