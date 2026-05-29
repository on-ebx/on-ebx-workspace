package com.orchestranetworks.ps.admin.devartifacts.util;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;

/**
 * An abstract procedure that allows the subclass to declare the execution method to throw a
 * {@link DevArtifactsException}. When it does so, it gets stored and can be retrieved via a
 * getter method. In most cases, Dev Artifacts doesn't need to use this class but since
 * executing a procedure normally wraps everything inside an {@link OperationException},
 * this can be useful when you want to retrieve the original DevArtifactsException (and the
 * additional data it stores) from outside of the procedure execution.
 * 
 * @see {@link DevArtifactsUtil#executeProcedure(DevArtifactsProcedure, Session, AdaptationHome)}
 */
public abstract class DevArtifactsProcedure implements Procedure
{
	private DevArtifactsException devArtifactsException;

	/**
	 * Perform the logic to be executed by this procedure
	 * 
	 * @param pContext the procedure context
	 * @throws DevArtifactsException if an exception occurs that should be enhanced with additional Dev Artifacts data
	 * @throws OperationException if another exception occurs
	 */
	protected abstract void doExecute(ProcedureContext pContext)
		throws DevArtifactsException, OperationException;

	@Override
	public void execute(ProcedureContext pContext) throws Exception
	{
		try
		{
			doExecute(pContext);
		}
		catch (DevArtifactsException ex)
		{
			// If a DevArtifactsException is thrown, simply store it for later retrieval
			// before rethrowing
			this.devArtifactsException = ex;
			throw ex;
		}
	}

	/**
	 * Get the DevArtifactsException that was thrown by the procedure execution
	 * 
	 * @return the DevArtifactsException
	 */
	public DevArtifactsException getDevArtifactsException()
	{
		return devArtifactsException;
	}
}
