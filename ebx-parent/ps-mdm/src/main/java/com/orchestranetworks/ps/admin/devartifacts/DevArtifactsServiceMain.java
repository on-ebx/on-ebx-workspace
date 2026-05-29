package com.orchestranetworks.ps.admin.devartifacts;

import java.io.*;
import java.util.*;

/**
 * An abstract class that contains the logic for a main method that invokes the Dev Artifacts Service.
 * Subclasses need to define the path to the services. Recommended to use
 * {@link DefaultDevArtifactsServiceMain}, which assumes the standard service names and just requires
 * sub-classing to define the module name.
 * 
 * The concrete subclass must implement a main method that calls the {@link #doMain(String[], DevArtifactsServiceMain)} method,
 * passing through the command-line parameters and a new instance of the concrete subclass.
 * 
 * @deprecated Use {@link com.orchestranetworks.ps.admin.devartifacts.main.AbstractDevArtifactsMain} instead
 */
@Deprecated
public abstract class DevArtifactsServiceMain
{
	private static final int EXIT_STATUS_COULD_NOT_INITIALIZE_INVOKER = 1;
	private static final int EXIT_STATUS_SERVICE_ERROR_RESPONSE = 2;
	private static final int EXIT_STATUS_EXCEPTION_EXECUTING_SERVICE = 3;
	private static final int EXIT_STATUS_EXCEPTION_READING_SERVICE_RESPONSE = 4;

	/**
	 * The types of service available: either <code>EXPORT</code> or <code>IMPORT</code>
	 */
	public enum ServiceType {
		EXPORT, IMPORT
	}

	/**
	 * Get the path to the servlet based on its type
	 * 
	 * @param serviceType the type of service
	 */
	public abstract String getServicePath(ServiceType serviceType);

	/**
	 * Create the invoker based on the command line arguments
	 * 
	 * @param args the command line arguments. See {@link #printUsage() printUsage}.
	 * @return the invoker
	 */
	protected DevArtifactsServiceInvoker createInvoker(String[] args)
	{
		if (args.length == 0 || "-h".equals(args[0]))
		{
			return null;
		}

		ServiceType serviceType;
		if ("export".equals(args[0]))
		{
			serviceType = ServiceType.EXPORT;
		}
		else if ("import".equals(args[0]))
		{
			serviceType = ServiceType.IMPORT;
		}
		else
		{
			return null;
		}

		DevArtifactsServiceInvoker invoker = new DevArtifactsServiceInvoker(
			args[1] + getServicePath(serviceType),
			args[2],
			args[3]);

		int wfIndex = -1;
		String propFileSysProp = null;
		boolean copy = false;
		boolean replace = false;
		boolean skip = false;
		boolean publish = false;
		for (int i = 4; wfIndex == -1 && i < args.length; i++)
		{
			if (args[i].startsWith("-"))
			{
				if ("-propFileSysProp".equals(args[i]))
				{
					i++;
					if (i < args.length)
					{
						propFileSysProp = args[i];
					}
				}
				else if ("-copy".equals(args[i]))
				{
					copy = true;
				}
				else if ("-replace".equals(args[i]))
				{
					replace = true;
				}
				else if ("-skip".equals(args[i]))
				{
					skip = true;
				}
				else if ("-publish".equals(args[i]))
				{
					publish = true;
				}
				else
				{
					return null;
				}
			}
			else
			{
				wfIndex = i;
			}
		}

		invoker.setPropertiesFileSystemProperty(propFileSysProp);
		invoker.setCopyEnvironment(copy);
		invoker.setReplaceMode(replace);
		invoker.setSkipNonExistingFiles(skip);
		invoker.setPublishWorkflowModels(publish);

		if (wfIndex != -1)
		{
			// Copy the args representing the workflow models into its own array
			String[] wfModels = Arrays.copyOfRange(args, wfIndex, args.length);
			invoker.setWorkflowModels(wfModels);
		}
		return invoker;
	}

	/**
	 * Print the command line usage
	 */
	public void printUsage(PrintStream out)
	{
		System.out.println(
			"Usage: java " + getClass().getCanonicalName()
				+ " {export | import} <url> <login> <password> [-propFileSysProp <sysProp>] [-publish] {[-copy] | [-replace] [-skip] [<workflow>...]}"
				+ System.getProperty("line.separator")
				+ "Ex: http://localhost:8080/company-mdm/ImportDevArtifacts admin admin -propFileSysProp company-dev-artifacts -publish -copy"
				+ System.getProperty("line.separator")
				+ "  (where company-mdm = module name, company-dev-artifacts = system property specifying location of properties file for module)");
	}

	/**
	 * Static method that does the logic of the main method
	 * 
	 * @param args the command line arguments
	 * @svcMain the instance of this class to use
	 */
	protected static void doMain(String[] args, DevArtifactsServiceMain svcMain)
	{
		DevArtifactsServiceInvoker invoker = svcMain.createInvoker(args);
		if (invoker == null)
		{
			svcMain.printUsage(System.err);
			System.exit(EXIT_STATUS_COULD_NOT_INITIALIZE_INVOKER);
		}
		else
		{
			// Execute the invoker
			InputStream in = null;
			try
			{
				in = invoker.execute();
			}
			catch (IOException ex)
			{
				svcMain.printUsage(System.err);
				ex.printStackTrace();
				System.exit(EXIT_STATUS_EXCEPTION_EXECUTING_SERVICE);
			}
			try
			{
				// Read from the connection's input stream and print each line out
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line = reader.readLine();
				boolean errorInOutput = (line == null
					|| !line.startsWith(DevArtifactsConstants.SERVICE_COMPLETE_MSG));
				PrintStream out;
				if (errorInOutput)
				{
					out = System.err;
					out.println("Error executing service.");
				}
				else
				{
					out = System.out;
				}
				out.println(line);
				while ((line = reader.readLine()) != null)
				{
					out.println(line);
				}
				if (errorInOutput)
				{
					System.exit(EXIT_STATUS_SERVICE_ERROR_RESPONSE);
				}
			}
			catch (IOException ex)
			{
				svcMain.printUsage(System.err);
				ex.printStackTrace();
				System.exit(EXIT_STATUS_EXCEPTION_READING_SERVICE_RESPONSE);
			}
		}
	}
}
