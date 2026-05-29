/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;

/**
 * Interface used to define whether or not a record should be copied or ignored.
 */
public interface DeepCopyRule
{
	/**
	 * Verifies if the record should be copied. If the record should be copied, true will be returned,
	 * otherwise false.
	 */
	public boolean executeCopy(Adaptation originalRecord, DeepCopyConfig config, Session session);
}
