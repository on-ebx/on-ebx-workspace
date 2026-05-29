package com.orchestranetworks.ps.admin.devartifacts.main;

/**
 * The default Main class for invoking Dev Artifacts via the command line.
 * It's configured to create the DTO from a properties file
 * and invoke Dev Artifacts via its REST interface, using Basic Auth.
 */
public class DefaultDevArtifactsMain extends AbstractDevArtifactsMain
{
	private DefaultDevArtifactsMain()
	{
		super(
			new DevArtifactsMainBasicAuthRESTClientFactory(),
			new DevArtifactsMainPropertiesFileDTOFactory());
	}

	public static void main(String[] args)
	{
		doMain(args, new DefaultDevArtifactsMain());
	}
}
