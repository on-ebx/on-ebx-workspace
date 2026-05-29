/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public class FieldNotNullableOutsideWorkflowTableTrigger extends FieldNotNullableTableTrigger
{
	@Override
	protected boolean rejectNullValue(Session session, ValueContext valueContext, Path fieldPath)
	{
		return super.rejectNullValue(session, valueContext, fieldPath)
			&& session.getInteraction(true) == null;
	}

}
