package com.orchestranetworks.ps.admin.devartifacts.main;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import com.orchestranetworks.ps.admin.devartifacts.client.*;
import com.orchestranetworks.ps.admin.devartifacts.dto.*;
import com.orchestranetworks.service.*;

/**
 * An abstract class for the command line functionality.
 * Concrete subclasses specify the client & DTO factories.
 */
public abstract class AbstractDevArtifactsMain implements DevArtifactsMainCommandLineCapable
{
	protected static final String OPTION_HELP_SHORT = "h";
	protected static final String OPTION_HELP_LONG = "help";

	private static final String DEFAULT_USAGE_HEADER = "Execute Dev Artifacts";

	private static final int EXIT_STATUS_COULD_NOT_INITIALIZE_CLIENT = 1;
	private static final int EXIT_STATUS_EXCEPTION_EXECUTING = 2;

	protected DevArtifactsMainClientFactory clientFactory;
	protected DevArtifactsMainDTOFactory dtoFactory;

	protected AbstractDevArtifactsMain(
		DevArtifactsMainClientFactory clientFactory,
		DevArtifactsMainDTOFactory dtoFactory)
	{
		this.clientFactory = clientFactory;
		this.dtoFactory = dtoFactory;
	}

	@Override
	public void addOptions(Options options)
	{
		// Help is available regardless of what factories are used
		options.addOption(OPTION_HELP_SHORT, OPTION_HELP_LONG, false, "Print this message");

		clientFactory.addOptions(options);
		dtoFactory.addOptions(options);
	}

	@Override
	public String getArgumentUsageSyntax()
	{
		// Combine whatever argument usage syntax is specified by the client & DTO factories
		List<String> argumentUsageSyntaxes = new ArrayList<>();
		String argumentUsageSyntax = clientFactory.getArgumentUsageSyntax();
		if (argumentUsageSyntax != null)
		{
			argumentUsageSyntaxes.add(argumentUsageSyntax);
		}
		argumentUsageSyntax = dtoFactory.getArgumentUsageSyntax();
		if (argumentUsageSyntax != null)
		{
			argumentUsageSyntaxes.add(argumentUsageSyntax);
		}
		if (argumentUsageSyntaxes.isEmpty())
		{
			return null;
		}
		return String.join(" ", argumentUsageSyntaxes);
	}

	@Override
	public String getArgumentUsageFooter()
	{
		// Combine whatever argument usage footer is specified by the client & DTO factories
		List<String> argumentUsageFooters = new ArrayList<>();
		String argumentUsageFooter = clientFactory.getArgumentUsageFooter();
		if (argumentUsageFooter != null)
		{
			argumentUsageFooters.add(argumentUsageFooter);
		}
		argumentUsageFooter = dtoFactory.getArgumentUsageFooter();
		if (argumentUsageFooter != null)
		{
			argumentUsageFooters.add(argumentUsageFooter);
		}
		if (argumentUsageFooters.isEmpty())
		{
			return null;
		}
		return String.join(System.lineSeparator(), argumentUsageFooters);
	}

	protected String getUsageSyntax()
	{
		StringBuilder bldr = new StringBuilder("java ").append(getClass().getCanonicalName());
		String argumentUsageSyntax = getArgumentUsageSyntax();
		if (argumentUsageSyntax != null)
		{
			bldr.append(" ").append(argumentUsageSyntax);
		}
		return bldr.toString();
	}

	protected String getUsageHeader()
	{
		return DEFAULT_USAGE_HEADER;
	}

	protected String getUsageFooter()
	{
		return getArgumentUsageFooter();
	}

	protected void printUsage(PrintStream out, Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		PrintWriter writer = new PrintWriter(out);
		try
		{
			formatter.printHelp(
				writer,
				HelpFormatter.DEFAULT_WIDTH,
				getUsageSyntax(),
				getUsageHeader(),
				options,
				HelpFormatter.DEFAULT_LEFT_PAD,
				HelpFormatter.DEFAULT_DESC_PAD,
				getUsageFooter(),
				true);
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Static method that does the logic of the main method
	 * 
	 * @param args the command line arguments
	 * @svcMain the instance of this class to use
	 */
	protected static void doMain(String[] args, AbstractDevArtifactsMain main)
	{
		Options options = new Options();
		main.addOptions(options);

		CommandLine commandLine = null;
		try
		{
			commandLine = (new DefaultParser()).parse(options, args);
		}
		catch (ParseException ex)
		{
			main.exit(EXIT_STATUS_COULD_NOT_INITIALIZE_CLIENT, System.err, options, ex);
		}

		if (commandLine.hasOption(OPTION_HELP_SHORT))
		{
			main.exit(0, System.out, options, null);
		}

		Iterator<String> argumentIterator = commandLine.getArgList().iterator();
		DevArtifactsClient client = null;
		DevArtifactsDTO dto = null;

		try
		{
			client = main.getClientFactory().createClient(commandLine, argumentIterator);
			DevArtifactsMainDTOFactory dtoFactory = main.getDTOFactory();
			dto = dtoFactory.createDTO(commandLine, argumentIterator);
			dtoFactory.configureDTO(dto, commandLine, argumentIterator);
		}
		catch (OperationException ex)
		{
			main.exit(EXIT_STATUS_COULD_NOT_INITIALIZE_CLIENT, System.err, options, ex);
		}

		try
		{
			client.execute(System.out, dto);
		}
		catch (OperationException ex)
		{
			main.exit(EXIT_STATUS_EXCEPTION_EXECUTING, System.err, options, ex);
		}

		System.exit(0);
	}

	private void exit(int code, PrintStream out, Options options, Exception exception)
	{
		if (exception != null)
		{
			exception.printStackTrace(out);
		}
		if (options != null)
		{
			printUsage(out, options);
		}
		System.exit(code);
	}

	public DevArtifactsMainClientFactory getClientFactory()
	{
		return clientFactory;
	}

	public void setClientFactory(DevArtifactsMainClientFactory clientFactory)
	{
		this.clientFactory = clientFactory;
	}

	public DevArtifactsMainDTOFactory getDTOFactory()
	{
		return dtoFactory;
	}

	public void setDTOFactory(DevArtifactsMainDTOFactory dtoFactory)
	{
		this.dtoFactory = dtoFactory;
	}
}
