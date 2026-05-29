/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scheduledtask;

import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;

/**
 * A scheduled task that clears the session interaction record cache used by {@link WorkflowUtilities}.
 * It shouldn't need to be cleared because it uses a WeakHashMap, but this can be used to force it to
 * clear on a period schedule if that is preferable to waiting for the garbage collector to collect closed sessions.
 */
public class ClearSessionInteractionCacheScheduledTask extends ScheduledTask
{
	@Override
	public void execute(ScheduledExecutionContext context)
		throws OperationException, ScheduledTaskInterruption
	{
		WorkflowUtilities.clearSessionInteractionCache();
	}
}
