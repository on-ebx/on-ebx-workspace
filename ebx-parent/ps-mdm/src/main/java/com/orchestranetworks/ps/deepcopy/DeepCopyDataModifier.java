/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;

/**
 */
public interface DeepCopyDataModifier
{
	void modifyDuplicateRecordContext(
		ValueContextForUpdate context,
		Adaptation origRecord,
		DeepCopyConfig config,
		Session session);
}
