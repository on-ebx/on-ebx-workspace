package com.orchestranetworks.ps.rest.workflow;

import com.orchestranetworks.rest.*;
import com.orchestranetworks.rest.annotation.*;

import jakarta.ws.rs.*;

/**
 * Defines the base URL for the Workflow REST Services, using the {@code @ApplicationPath} annotation and the set of packages to scan for REST service classes.
 *
 */
@ApplicationPath(value = "/rest/v1/ps/workflow")
@Documentation("Workflow REST Application")
public class WorkflowRESTApplication extends RESTApplicationAbstract
{
	public WorkflowRESTApplication()
	{
		super(
			(cfg) -> cfg.addPackages(WorkflowRESTApplication.class.getPackage())
				.register(WorkflowRESTService.class));
	}
}
