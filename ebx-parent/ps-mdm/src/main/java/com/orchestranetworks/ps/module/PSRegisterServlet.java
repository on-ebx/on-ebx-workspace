package com.orchestranetworks.ps.module;

import com.orchestranetworks.module.*;
import com.orchestranetworks.service.*;

/***
 * Registers standard Orchestra Networks Professional Services (PS) services, such as Dev Artifacts.
 * This should be extended to supply the module name in the constructor.
 * 
 * As of EBX v 5.9, {@link PSModuleRegistrationListener} is preferable, because it can take advantage of
 * the REST toolkit. However, it's still valid to use this class.
 */
public class PSRegisterServlet extends ModuleRegistrationServlet
{
	/**
	 * @deprecated Use {@link PSModuleRegistration#ADMIN_GROUP} instead
	 */
	@Deprecated
	public static final String ADMIN_GROUP = PSModuleRegistration.ADMIN_GROUP;

	private static final long serialVersionUID = 1L;

	private PSModuleRegistration impl;

	/**
	 * Create the servlet with the given module name
	 * 
	 * @param moduleName the module name
	 */
	public PSRegisterServlet(String moduleName)
	{
		impl = new PSModuleRegistration(moduleName);
	}

	@Override
	public void handleServiceRegistration(ModuleServiceRegistrationContext aContext)
	{
		super.handleServiceRegistration(aContext);
		impl.handleServiceRegistration(aContext);
	}

	@Override
	public void destroyBeforeUnregisterModule()
	{
		super.destroyBeforeUnregisterModule();
		impl.destroyBeforeUnregisterModule();
	}

	@Override
	public void handleRepositoryShutdown()
	{
		super.handleRepositoryShutdown();
		impl.handleRepositoryShutdown();
	}

	@Override
	public void handleRepositoryStartup(ModuleContextOnRepositoryStartup aContext)
		throws OperationException
	{
		super.handleRepositoryStartup(aContext);
		impl.handleRepositoryStartup(aContext);
	}

	public PSModuleRegistration getImpl()
	{
		return impl;
	}
}