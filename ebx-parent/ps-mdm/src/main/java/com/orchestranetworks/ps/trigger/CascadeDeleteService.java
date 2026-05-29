/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * A service that cascade deletes records. This would be used in conjunction with a
 * {@link com.orchestranetworks.ps.trigger.BaseTableTrigger}, set with <code>enableCascadeDelete</code> to <code>true</code>.
 * The trigger handles cascade deleting automatically when in a workflow, but outside a workflow we require it to be done
 * via a service. (A Tech Admin or web service may want to delete just one record, not cascade delete.)
 */
public class CascadeDeleteService<S extends TableViewEntitySelection> extends AbstractUserService<S>
{
	public static final String CASCADE_DELETE_SERVICE_TRACKING_INFO = "CascadeDeleteService";

	@Override
	public void execute(Session session) throws OperationException
	{
		S selection = context.getEntitySelection();
		List<Adaptation> records = AdaptationUtil
			.getRecords(selection.getSelectedRecords().execute());

		CascadeDeleteServiceProcedure proc = new CascadeDeleteServiceProcedure(records);
		ProcedureExecutor.executeProcedure(
			proc,
			session,
			selection.getDataspace(),
			CASCADE_DELETE_SERVICE_TRACKING_INFO);
	}

	private class CascadeDeleteServiceProcedure implements Procedure
	{
		private List<Adaptation> records;

		public CascadeDeleteServiceProcedure(List<Adaptation> records)
		{
			this.records = records;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			for (Adaptation record : records)
			{
				CascadeDeleter.invokeCascadeDelete(record, pContext);
			}
		}
	}
}
