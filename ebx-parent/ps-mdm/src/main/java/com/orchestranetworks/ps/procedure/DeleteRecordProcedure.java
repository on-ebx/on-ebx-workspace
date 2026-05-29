package com.orchestranetworks.ps.procedure;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.service.*;

/**
 *
 * Delete a record or a data set.
 * @author MCH
 */
public class DeleteRecordProcedure extends GenericProcedure
{

	/**
	 * @param adaptation record to delete
	 * @param session user session
	 * @throws OperationException
	 * @return created record
	 * @deprecated use instance methods or {@link Procedures.Delete#execute(Session, Adaptation)} instead.
	 */
	@Deprecated
	public static void execute(final Adaptation adaptation, final Session session)
		throws OperationException
	{
		Procedures.Delete.execute(session, adaptation);
	}

	/**
	 * @param adaptation record to delete
	 * @param session user session
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @param deletingChildren
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Delete#execute(Session, Adaptation, boolean, boolean, boolean)} instead.
	 */
	@Deprecated
	public static void execute(
		final Adaptation adaptation,
		final Session session,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation,
		final boolean deletingChildren)
		throws OperationException
	{
		Procedures.Delete.execute(
			session,
			adaptation,
			enableAllPrivileges,
			disableTriggerActivation,
			deletingChildren);
	}

	/**
	 * @param pContext procedure context in which to delete record
	 * @param adaptation record to delete
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Delete#execute(ProcedureContext, Adaptation)} instead.
	 */
	@Deprecated
	public static void execute(final ProcedureContext pContext, final Adaptation adaptation)
		throws OperationException
	{
		Procedures.Delete.execute(pContext, adaptation, true, false, false);
	}

	/**
	 * @param pContext procedure context in which to delete record
	 * @param adaptation record to delete
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @param deletingChildren
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Delete#execute(ProcedureContext, Adaptation, boolean, boolean, boolean)} instead.
	 */
	@Deprecated
	public static void execute(
		final ProcedureContext pContext,
		final Adaptation adaptation,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation,
		final boolean deletingChildren)
		throws OperationException
	{
		Procedures.Delete.execute(
			pContext,
			adaptation,
			enableAllPrivileges,
			disableTriggerActivation,
			deletingChildren);
	}

	/** The adaptation or record to delete. */
	private Adaptation adaptation;

	/** Deleting children. */
	private boolean deletingChildren;

	/**
	 * Instantiates a new procedure.
	 */
	public DeleteRecordProcedure()
	{

	}

	/**
	 * Instantiates a new procedure.
	 *
	 * @param pAdaptation the record or data set to delete
	 */
	public DeleteRecordProcedure(final Adaptation pAdaptation)
	{
		this.adaptation = pAdaptation;
	}

	/**
	 * Instantiates a new procedure.
	 *
	 * @param pAdaptation the record or data set to delete
	 * @param isDeletingChildren If true, the data set children will be deleted as well
	 */
	public DeleteRecordProcedure(final Adaptation pAdaptation, final boolean isDeletingChildren)
	{
		this.adaptation = pAdaptation;
		this.deletingChildren = isDeletingChildren;
	}

	/*
	 * @see
	 * com.orchestranetworks.ps.procedure.GenericProcedure#doExecute(com.orchestranetworks.service.
	 * ProcedureContext)
	 */
	@Override
	protected void doExecute(final ProcedureContext pContext) throws OperationException
	{
		if (this.adaptation == null)
		{
			throw OperationException.createError("Adaptation cannot be null");
		}
		pContext.doDelete(this.adaptation.getAdaptationName(), this.deletingChildren);
	}

	@Override
	public TriggerAction[] getIgnoreTriggerActions()
	{
		return new TriggerAction[] { TriggerAction.DELETE };
	}

	/**
	 * Gets the record or data set to delete.
	 *
	 * @return the adaptation
	 */
	public Adaptation getAdaptation()
	{
		return this.adaptation;
	}

	/**
	 * Checks if data set children will be deleted.
	 *
	 * @return true, if data set children will be deleted.
	 */
	public boolean isDeletingChildren()
	{
		return this.deletingChildren;
	}

	/**
	 * Sets the record or data set to delete.
	 *
	 * @param adaptation the record or data set to delete
	 */
	public void setAdaptation(final Adaptation adaptation)
	{
		this.adaptation = adaptation;
	}

	/**
	 * Define if data set children will be deleted as well.
	 *
	 * @param deletingChildren Set to true, if data set children must be deleted.
	 */
	public void setDeletingChildren(final boolean deletingChildren)
	{
		this.deletingChildren = deletingChildren;
	}

	@Override
	protected AdaptationHome getHome()
	{
		return getAdaptation().getHome();
	}
}
