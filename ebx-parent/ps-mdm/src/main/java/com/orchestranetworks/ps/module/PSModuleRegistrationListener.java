package com.orchestranetworks.ps.module;

import java.util.*;

import com.orchestranetworks.module.*;
import com.orchestranetworks.ps.admin.devartifacts.rest.*;
import com.orchestranetworks.ps.logging.*;
import com.orchestranetworks.ps.rest.dataspace.*;
import com.orchestranetworks.ps.rest.directory.*;
import com.orchestranetworks.ps.rest.workflow.*;
import com.orchestranetworks.rest.*;
import com.orchestranetworks.service.*;

/***
 * Registers standard Orchestra Networks Professional Services (PS) services, such as Dev Artifacts.
 * This should be extended to supply the module name in the constructor.
 * 
 * {@link PSRegisterServlet} still works, but in order to use the REST toolkit with EBX v. 5.9,
 * you need to use a {@link ModuleRegistrationListener}, so it's preferable to use this class
 * and construct with your module name.
 * 
 * For a multi-tenant Dev Artifacts solution, an instance of {@link PSMultiTenantAdminModuleRegistration}
 * should be used and passed into the constructor for the admin tenant. The other tenants shouldn't extend
 * this class and should instead extend {@link PSMultiTenantModuleRegistrationListener}.
 */
public abstract class PSModuleRegistrationListener extends ModuleRegistrationListener
{
	private PSModuleRegistration impl;

	protected PSModuleRegistrationListener(String moduleName)
	{
		impl = new PSModuleRegistration(moduleName);
	}

	protected PSModuleRegistrationListener(PSModuleRegistration psModuleRegistration)
	{
		impl = psModuleRegistration;
	}

	@Override
	public void handleContextInitialized(ModuleInitializedContext context)
	{
		Set<Class<? extends RESTApplicationAbstract>> applications = getRESTApplicationsToRegister();
		for (Class<? extends RESTApplicationAbstract> application : applications)
		{
			context.registerRESTApplication(application);
		}
	}

	protected Set<Class<? extends RESTApplicationAbstract>> getRESTApplicationsToRegister()
	{
		Set<Class<? extends RESTApplicationAbstract>> applications = new HashSet<>();
		applications.add(DirectoryRESTApplication.class);
		applications.add(DataSpaceRESTApplication.class);
		applications.add(WorkflowRESTApplication.class);
		applications.add(DefaultDevArtifactsRESTApplication.class);
		return applications;
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
		impl.destroyBeforeUnregisterModule();
		super.destroyBeforeUnregisterModule();
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
		CustomLogger.setCustomLogger(aContext.getLoggingCategory());
		super.handleRepositoryStartup(aContext);
		impl.handleRepositoryStartup(aContext);
	}

	public PSModuleRegistration getImpl()
	{
		return impl;
	}
}
