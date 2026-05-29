package com.orchestranetworks.ps.procedure;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.service.*;

/**
 * Abstract class extended by specific procedures.
 * Manage reset of features activation state after execution.
 * Offer a shortcut to execute it via a ProgrammaticService.
 * @author MCH
 */
public abstract class GenericProcedure implements Procedure
{

	/** All privileges */
	private boolean allPrivileges = false;

	/** Trigger activation. */
	private boolean triggerActivation = true;

	/** History activation. */
	private boolean historyActivation = true;

	/** History activation. */
	private boolean databaseHistoryActivation = true;

	/** Blocking constraint disabled. */
	private boolean blockingConstraintDisabled = false;

	/** Batch. */
	private boolean batch = false;

	/**
	 * Execute the specific job of a procedure.
	 *
	 * @param pContext the context
	 * @throws OperationException the exception
	 */
	protected abstract void doExecute(final ProcedureContext pContext) throws OperationException;

	/*
	 * @see com.orchestranetworks.service.Procedure#execute(com.orchestranetworks.service.
	 * ProcedureContext)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public final void execute(final ProcedureContext pContext) throws OperationException
	{
		boolean originalPrivileges = pContext.isAllPrivileges();
		boolean originalTriggerActivation = pContext.isTriggerActivation();
		boolean originalHistoryActivation = pContext.isHistoryActivation();
		boolean originalDatabaseHistoryActivation = pContext.isDatabaseHistoryActivation();
		boolean originalBlockingConstraintDisabled = pContext.isBlockingConstraintsDisabled();
		try
		{
			pContext.setAllPrivileges(this.allPrivileges);
			pContext.setTriggerActivation(this.triggerActivation);
			pContext.setHistoryActivation(this.historyActivation);
			pContext.setDatabaseHistoryActivation(this.databaseHistoryActivation);
			pContext.setBlockingConstraintsDisabled(this.blockingConstraintDisabled);
			if (this.batch)
			{
				pContext.setBatch();
			}
			TriggerAction[] triggerActions = getIgnoreTriggerActions();
			if (this.allPrivileges && triggerActions != null)
			{
				Session session = pContext.getSession();
				TriggerAction[] ignoreActions = (TriggerAction[]) session
					.getAttribute(BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE);
				session.setAttribute(
					BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
					triggerActions);
				try
				{
					doExecute(pContext);
				}
				finally
				{
					session.setAttribute(
						BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
						ignoreActions);
				}
			}
			else
			{
				this.doExecute(pContext);
			}
		}
		finally
		{
			pContext.setAllPrivileges(originalPrivileges);
			pContext.setTriggerActivation(originalTriggerActivation);
			pContext.setHistoryActivation(originalHistoryActivation);
			pContext.setDatabaseHistoryActivation(originalDatabaseHistoryActivation);
			pContext.setBlockingConstraintsDisabled(originalBlockingConstraintDisabled);
		}
	}

	public TriggerAction[] getIgnoreTriggerActions()
	{
		return null;
	}

	/**
	 * Execute with programmatic service.
	 * Note that if the procedure throws an exception, this method will not itself throw it.
	 * It's expected that the caller inspects the result returned to see if there's an
	 * exception. If you wish to have the exception thrown automatically then use
	 * {@link #execute(Session, AdaptationHome)} or
	 * {@link ProcedureExecutor#executeProcedure(Procedure, Session, AdaptationHome)}.
	 *
	 * @param pHome the home
	 * @param pSession the session
	 * @return the procedure result
	 */
	public ProcedureResult executeWithProgrammaticService(
		final AdaptationHome pHome,
		final Session pSession)
	{
		ProgrammaticService srv = ProgrammaticService.createForSession(pSession, pHome);
		return srv.execute(this);
	}

	/**
	 * Execute given a session.
	 * This is the equivalent of calling
	 * {@link ProcedureExecutor#executeProcedure(Procedure, Session, AdaptationHome)}
	 *
	 * @param pSession the session
	 * @param home the data space in which to execute the procedure
	 * @throws OperationException if an error occurs in the procedure
	 */
	public void execute(final Session pSession, AdaptationHome home) throws OperationException
	{
		if (home == null)
			throw new IllegalArgumentException(
				"Home is required to execute a procedure with a session");
		ProcedureExecutor.executeProcedure(this, pSession, home);
	}

	/**
	 * Execute given a session. This method requires that you implement {@link #getHome()}
	 * to return a data space to execute the procedure on.
	 * This is the equivalent of calling {@link #execute(Session, AdaptationHome)} or
	 * {@link ProcedureExecutor#executeProcedure(Procedure, Session, AdaptationHome)} with the
	 * result of {@link #getHome()}.
	 *
	 * @param pSession the session
	 * @throws OperationException if an error occurs in the procedure
	 */
	public void execute(final Session pSession) throws OperationException
	{
		execute(pSession, getHome());
	}

	/**
	 * Get the data space to execute the procedure on. By default, this returns <code>null</code>
	 * but can be overridden to return a data space.
	 * If you're going to call {@link #execute(Session)}, then you must override this.
	 * Otherwise, you can call {@link #execute(Session, AdaptationHome)} or
	 * {@link ProcedureExecutor#executeProcedure(Procedure, Session, AdaptationHome)}.
	 * 
	 * @return the data space
	 */
	protected AdaptationHome getHome()
	{
		return null;
	}

	/**
	 * Checks if is all privileges are granted.
	 *
	 * @return true, if is all privileges
	 */
	public boolean isAllPrivileges()
	{
		return this.allPrivileges;
	}

	/**
	 * Checks if is blocking constraint are disabled.
	 *
	 * @return true, if is blocking constraint disabled
	 */
	public boolean isBlockingConstraintDisabled()
	{
		return this.blockingConstraintDisabled;
	}

	/**
	 * Checks if is database history is active.
	 *
	 * @return true, if  database history is active.
	 */
	public boolean isDatabaseHistoryActivation()
	{
		return this.databaseHistoryActivation;
	}

	/**
	 * Checks if is history is active.
	 *
	 * @return true, if history is active.
	 */
	public boolean isHistoryActivation()
	{
		return this.historyActivation;
	}

	/**
	 * Checks if is triggers are active.
	 *
	 * @return true, if is triggers are active
	 */
	public boolean isTriggerActivation()
	{
		return this.triggerActivation;
	}

	/**
	 * Sets all privileges.
	 *
	 * @param allPrivileges the new all privileges
	 */
	public void setAllPrivileges(final boolean allPrivileges)
	{
		this.allPrivileges = allPrivileges;
	}

	/**
	 * Sets the blocking constraint disabled.
	 *
	 * @param blockingConstraintDisabled the new blocking constraint disabled
	 */
	public void setBlockingConstraintDisabled(final boolean blockingConstraintDisabled)
	{
		this.blockingConstraintDisabled = blockingConstraintDisabled;
	}

	/**
	 * Sets the database history activation.
	 *
	 * @param databaseHistoryActivation the new database history activation
	 */
	public void setDatabaseHistoryActivation(final boolean databaseHistoryActivation)
	{
		this.databaseHistoryActivation = databaseHistoryActivation;
	}

	/**
	 * Sets the history activation.
	 *
	 * @param historyActivation the new history activation
	 */
	public void setHistoryActivation(final boolean historyActivation)
	{
		this.historyActivation = historyActivation;
	}

	/**
	 * Sets the trigger activation.
	 *
	 * @param triggerActivation the new trigger activation
	 */
	public void setTriggerActivation(final boolean triggerActivation)
	{
		this.triggerActivation = triggerActivation;
	}

	/**
	 * Gets whether the procedure is in batch mode
	 * 
	 * @return whether the procedure is in batch mode
	 */
	public boolean isBatch()
	{
		return batch;
	}

	/**
	 * Sets the procedure to batch mode. Once set, it can't be unset, which is consistent with how
	 * {@link ProcedureContext#setBatch()} works.
	 */
	public void setBatch()
	{
		this.batch = true;
	}
}
