/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;

/**
 * A factory for <code>CleanWorkflowsConfig</code>s.
 */
public interface CleanWorkflowsConfigFactory
{
	/**
	 * Create the configuration
	 * 
	 * @param repo the repository
	 * @param session the session
	 * @return the configuration
	 * @throws OperationException
	 */
	CleanWorkflowsConfig createConfig(Repository repo, Session session) throws OperationException;
}
