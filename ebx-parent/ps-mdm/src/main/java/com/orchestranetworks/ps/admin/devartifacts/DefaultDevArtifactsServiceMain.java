package com.orchestranetworks.ps.admin.devartifacts;

/**
 * An abstract subclass of {@link DevArtifactsServiceMain} which assumes the standard
 * service names and just requires defining the module name in the subclass.
 * You can override the service names in the subclass also if you don't want the standard
 * ones (must match what you called them in your application server).
 * 
 * @deprecated Use {@link com.orchestranetworks.ps.admin.devartifacts.main.DefaultDevArtifactsMain} instead
 */
@Deprecated
public abstract class DefaultDevArtifactsServiceMain extends DevArtifactsServiceMain
{
	private static final String DEFAULT_EXPORT_SERVICE_NAME = "ExportDevArtifacts";
	private static final String DEFAULT_IMPORT_SERVICE_NAME = "ImportDevArtifacts";

	/**
	 * Define the module name, which will be used in the path to the service that gets invoked.
	 * 
	 * @return the module name
	 */
	protected abstract String getModuleName();

	protected String getExportServiceName()
	{
		return DEFAULT_EXPORT_SERVICE_NAME;
	}

	protected String getImportServiceName()
	{
		return DEFAULT_IMPORT_SERVICE_NAME;
	}

	/**
	 * Overridden to define the paths as "/moduleName/serviceName", where
	 * moduleName is specified by {@link #getModuleName()} and the service names
	 * are defined by {@link #getExportServiceName()} and {@link #getImportServiceName()}.
	 * 
	 * @param serviceType Either <code>EXPORT</code> or <code>IMPORT</code>
	 */
	@Override
	public String getServicePath(ServiceType serviceType)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("/");
		bldr.append(getModuleName());
		bldr.append("/");
		if (serviceType == ServiceType.EXPORT)
		{
			bldr.append(getExportServiceName());
		}
		else if (serviceType == ServiceType.IMPORT)
		{
			bldr.append(getImportServiceName());
		}
		else
		{
			throw new IllegalArgumentException("Unsupported serviceType " + serviceType);
		}
		return bldr.toString();
	}
}
