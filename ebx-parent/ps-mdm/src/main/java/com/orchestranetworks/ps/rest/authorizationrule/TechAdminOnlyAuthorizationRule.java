package com.orchestranetworks.ps.rest.authorizationrule;

import java.util.*;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;

/**
 * A rule for REST services that requires user to be in the role Tech Admin in order to execute
 */
public class TechAdminOnlyAuthorizationRule extends RolesOnlyAuthorizationRule
{
	@Override
	protected List<Role> getRoles()
	{
		return Arrays.asList(CommonConstants.TECH_ADMIN);
	}
}
