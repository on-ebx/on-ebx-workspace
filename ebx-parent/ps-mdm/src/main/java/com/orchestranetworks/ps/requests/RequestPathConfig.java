/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.requests;

import com.orchestranetworks.schema.*;

/**
 * Specifies request related paths for mass approval capabilities
 */
public interface RequestPathConfig
{
	Path getRequestTablePath();
	Path getRequestNameFieldPath();
	Path getRequestProcessInstanceKeyPath();
	Path getRequestStatusFieldPath();
}
