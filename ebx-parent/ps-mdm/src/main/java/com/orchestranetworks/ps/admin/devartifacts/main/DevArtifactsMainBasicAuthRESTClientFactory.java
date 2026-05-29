package com.orchestranetworks.ps.admin.devartifacts.main;

import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.client.*;
import com.orchestranetworks.service.*;

/**
 * A Main client factory that invokes Dev Artifacts via its REST interface
 * and uses Basic Auth.
 */
public class DevArtifactsMainBasicAuthRESTClientFactory
	extends
	AbstractDevArtifactsMainRESTClientFactory
{
	protected static final String ARGUMENT_USERNAME = "USERNAME";
	protected static final String ARGUMENT_PASSWORD = "PASSWORD";

	@Override
	public DevArtifactsClient createClient(
		CommandLine commandLine,
		Iterator<String> argumentIterator)
		throws OperationException
	{
		String url = getURL(commandLine, argumentIterator);

		String username;
		if (argumentIterator.hasNext())
		{
			username = argumentIterator.next();
		}
		else
		{
			throw OperationException
				.createError("Argument " + ARGUMENT_USERNAME + " must be specified.");
		}

		String password = (argumentIterator.hasNext()) ? argumentIterator.next() : null;

		return new BasicAuthDevArtifactsRESTClient(url, username, password);
	}

	@Override
	public String getArgumentUsageSyntax()
	{
		return super.getArgumentUsageSyntax() + " " + ARGUMENT_USERNAME + " [" + ARGUMENT_PASSWORD
			+ "]";
	}
}
