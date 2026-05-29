/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * Manages the PermissionsUser's permissions. 
 * The default implementation to get the user's session permissions is available in {@link DefaultPermissionsUserManager}. 
 *
 */
public interface PermissionsUserManager
{
	SessionPermissions getSessionPermissions(Repository repo, UserReference user);
}
