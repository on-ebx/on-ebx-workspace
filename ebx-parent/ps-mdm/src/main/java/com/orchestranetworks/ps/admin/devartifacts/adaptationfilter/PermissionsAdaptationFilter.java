package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * A filter that accepts a record if its profile is a role for the tenant.
 * This never allows user profiles, only roles. If the tenant policy is single, then the only thing
 * this will enforce is that it's a role. If it's multi, then the acceptable roles are passed in.
 * 
 * The <code>profilePath</code> specifies the field for the profile. If the profile is the primary key of the
 * record, then you can pass <code>null</code> for that.
 */
public class PermissionsAdaptationFilter implements AdaptationFilter
{
	private Set<Role> roles;
	private String tenantPolicy;
	private Path profilePath;

	public PermissionsAdaptationFilter(Set<Role> roles, String tenantPolicy, Path profilePath)
	{
		this.roles = roles;
		this.tenantPolicy = tenantPolicy;
		this.profilePath = profilePath;
	}

	@Override
	public boolean accept(Adaptation record)
	{
		String profileName = (profilePath == null) ? record.getOccurrencePrimaryKey().format()
			: record.getString(profilePath);
		Profile profile = Profile.parse(profileName);
		if (profile.isUserReference())
		{
			return false;
		}
		if (DevArtifactsConstants.TENANT_POLICY_SINGLE.equals(tenantPolicy))
		{
			return true;
		}
		// Built-in roles are handled by the admin tenant
		if (profile.isBuiltIn())
		{
			return DevArtifactsConstants.TENANT_POLICY_MULTI_ADMIN.equals(tenantPolicy);
		}
		if (profile.isSpecificRole())
		{
			String roleName = profileName.substring(1);
			return roles.contains(Role.forSpecificRole(roleName));
		}
		return false;
	}
}
