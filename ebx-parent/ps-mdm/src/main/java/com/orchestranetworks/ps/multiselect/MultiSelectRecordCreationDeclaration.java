/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 */
public class MultiSelectRecordCreationDeclaration
extends
GenericServiceDeclaration<TableViewEntitySelection, ActivationContextOnTableView>
implements UserServiceDeclaration.OnTableView
{
	private Path tablePath;
	private MultiSelectRecordCreationServicePermissionRule permissionRule;
	private TrackingInfoHelper trackingInfoHelper;

	public MultiSelectRecordCreationDeclaration(
		Path tablePath,
		MultiSelectRecordCreationServicePermissionRule permissionRule,
		TrackingInfoHelper trackingInfoHelper,
		String serviceName,
		String serviceTitle)
	{
		super(ServiceKey.forName(serviceName), null, serviceTitle, null, null);
		this.tablePath = tablePath;
		this.permissionRule = permissionRule;
		this.trackingInfoHelper = trackingInfoHelper;
	}

	@Override
	public void defineActivation(ActivationContextOnTableView aContext)
	{
		super.defineActivation(aContext);
		aContext.includeSchemaNodesMatching(tablePath);
		aContext.forbidEmptyRecordSelection();
		aContext.setPermissionRule(permissionRule);
	}
	@Override
	public UserService<TableViewEntitySelection> createUserService()
	{
		return new MultiSelectRecordCreationService(trackingInfoHelper);
	}
}
