package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.schema.*;

/**
 * Filter on tables that relate to a schema location, that ensure the module of the
 * schema location matches the tenant module.
 * 
 * This should only be used when in multi-tenant mode.
 * 
 * It takes in an array of foreign key paths to the record containing the schema location field.
 * If it's on the record itself, then should pass in <code>null</code> or an empty array.
 * Otherwise, pass in the foreign keys, which will be followed in order of the array.
 */
public class SchemaLocationAdaptationFilter implements AdaptationFilter
{
	private DevArtifactsConfig config;
	private Path[] foreignKeyPaths;
	private Path schemaLocationPath;

	public SchemaLocationAdaptationFilter(
		DevArtifactsConfig config,
		Path[] foreignKeyPaths,
		Path schemaLocationPath)
	{
		this.config = config;
		this.foreignKeyPaths = foreignKeyPaths;
		this.schemaLocationPath = schemaLocationPath;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		Adaptation recordToUse = DevArtifactsUtil
			.followForeignKeyChain(foreignKeyPaths, adaptation);
		if (recordToUse == null)
		{
			return false;
		}
		String schemaLocationStr = recordToUse.getString(schemaLocationPath);
		SchemaLocation schemaLocation = SchemaLocation.parse(schemaLocationStr);
		return DevArtifactsUtil.matchesTenantModule(
			config.getTenantPolicy(),
			config.getModules(),
			schemaLocation.getModuleName());
	}
}
