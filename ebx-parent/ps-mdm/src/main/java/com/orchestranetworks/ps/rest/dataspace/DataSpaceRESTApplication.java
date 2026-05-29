package com.orchestranetworks.ps.rest.dataspace;

import com.orchestranetworks.rest.*;
import com.orchestranetworks.rest.annotation.*;

import jakarta.ws.rs.*;

/**
 * Defines the base URL for the DataSpace REST Services, using the {@code @ApplicationPath} annotation and the set of packages to scan for REST service classes.
 *
 */
@ApplicationPath(value = "/rest/v1/ps/dataspace")
@Documentation("Dataspace REST Application")
public class DataSpaceRESTApplication extends RESTApplicationAbstract
{
	public DataSpaceRESTApplication()
	{
		super(
			(cfg) -> cfg.addPackages(DataSpaceRESTApplication.class.getPackage())
				.register(DataSpaceRESTService.class));
	}
}
