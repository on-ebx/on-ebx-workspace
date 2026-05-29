/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.validation.servicepermission;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.service.*;

/**
 * @deprecated Use new user service instead, and set permissions appropriately
 */
@Deprecated
public class DisplayValidationReportServicePermission
	extends
	MasterOrChildDataSpaceOnlyServicePermission
{
	public DisplayValidationReportServicePermission()
	{
		allowInMaster = false;
	}

	@Override
	protected boolean canUserLaunchInMasterDataSpace(AdaptationHome dataSpace, Session session)
	{
		return super.canUserLaunchInMasterDataSpace(dataSpace, session)
			|| session.isUserInRole(CommonConstants.TECH_ADMIN);
	}

}
