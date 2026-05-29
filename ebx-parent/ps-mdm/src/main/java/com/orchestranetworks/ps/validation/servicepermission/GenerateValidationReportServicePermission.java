/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.validation.servicepermission;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;

/**
 * @deprecated Don't believe this class is in use
 */
@Deprecated
public class GenerateValidationReportServicePermission extends MasterDataSpaceOnlyServicePermission
{
	@Override
	protected boolean canUserLaunchInChildDataSpace(AdaptationHome dataSpace, Session session)
	{
		return super.canUserLaunchInChildDataSpace(dataSpace, session)
			|| session.isUserInRole(CommonConstants.TECH_ADMIN);
	}

}
