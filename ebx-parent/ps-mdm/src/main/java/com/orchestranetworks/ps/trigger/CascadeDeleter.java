/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.service.*;

/**
 * This class can be used when you want to explicitly invoke a cascade delete on a record,
 * outside of the normal invocation that happens inside a workflow. (For example, if you want
 * to invoke it from a service.)
 */
public class CascadeDeleter
{
	/**
	 * Invoke a cascade delete on a record
	 * 
	 * @param record the record
	 * @param session the session
	 * @throws OperationException if an error occurs while deleting the record
	 */
	public static void invokeCascadeDelete(Adaptation record, Session session)
		throws OperationException
	{
		doInvokeCascadeDelete(record, session, null);
	}

	/**
	 * Invoke a cascade delete on a record
	 * 
	 * @param record the record
	 * @param pContext the procedure context
	 * @throws OperationException if an error occurs while deleting the record
	 */
	public static void invokeCascadeDelete(Adaptation record, ProcedureContext pContext)
		throws OperationException
	{
		doInvokeCascadeDelete(record, pContext.getSession(), pContext);
	}

	@SuppressWarnings("deprecation")
	private static void doInvokeCascadeDelete(
		Adaptation record,
		Session session,
		ProcedureContext pContext)
		throws OperationException
	{
		session.setAttribute(
			BaseTableTriggerEnforcingPermissions.INVOKING_CASCADE_DELETE_SESSION_ATTRIBUTE,
			Boolean.TRUE);
		try
		{
			if (pContext == null)
			{
				DeleteRecordProcedure delProc = new DeleteRecordProcedure(record, true);
				delProc.setAllPrivileges(true);
				try
				{
					delProc.executeWithProgrammaticService(record.getHome(), session);
				}
				catch (Exception e)
				{
					throw OperationException.createError(e);
				}
			}
			else
			{
				DeleteRecordProcedure delProc = new DeleteRecordProcedure(record, true);
				delProc.setAllPrivileges(true);
				try
				{
					delProc.execute(pContext);
				}
				catch (Exception e)
				{
					throw OperationException.createError(e);
				}
			}
		}
		finally
		{
			session.setAttribute(
				BaseTableTriggerEnforcingPermissions.INVOKING_CASCADE_DELETE_SESSION_ATTRIBUTE,
				null);
		}
	}

	private CascadeDeleter()
	{
	}
}
