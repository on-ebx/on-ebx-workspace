/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;

/**
 */
public interface DeepCopyConfigFactory
{
	DeepCopyConfig createConfig(AdaptationTable table);
}
