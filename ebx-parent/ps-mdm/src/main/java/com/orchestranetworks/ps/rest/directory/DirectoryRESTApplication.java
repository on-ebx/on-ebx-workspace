package com.orchestranetworks.ps.rest.directory;

import com.orchestranetworks.rest.*;
import com.orchestranetworks.rest.annotation.*;

import jakarta.ws.rs.*;

/**
 * Defines the base URL for the Directory REST Services, using the {@code @ApplicationPath} annotation and the set of packages to scan for REST service classes.
 *
 */
@ApplicationPath(value = "/rest/v1/ps/directory")
@Documentation("Directory REST Application")
public class DirectoryRESTApplication extends RESTApplicationAbstract
{
	public DirectoryRESTApplication()
	{
		super(
			(cfg) -> cfg.addPackages(DirectoryRESTApplication.class.getPackage())
				.register(DirectoryRESTService.class));
	}
}
