package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *
 * Modify a record or a data set based on a map between paths and values.
 * @author MCH
 */
public class ModifyValuesProcedure extends GenericProcedure
{

	/**
	 * @param adaptation record to modify
	 * @param pathValueMap value map for modifications
	 * @param session user session
	 * @return modified adaptation
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Modify#execute(Session, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		final Session session)
		throws OperationException
	{
		return Procedures.Modify.execute(session, adaptation, pathValueMap);
	}

	/**
	 * @param adaptation record to modify
	 * @param pathValueMap value map for modifications
	 * @param session user session
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return modified adaptation
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Modify#execute(Session, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		final Session session,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Modify.execute(
			session,
			adaptation,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	/**
	 * @param pContext ProcedureContext in which to execute modification
	 * @param adaptation record to be modified
	 * @param pathValueMap map of new path-values
	 * @return modified adaptation
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Modify#execute(ProcedureContext, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final ProcedureContext pContext,
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap)
		throws OperationException
	{
		return Procedures.Modify.execute(pContext, adaptation, pathValueMap);
	}

	/**
	 * @param pContext ProcedureContext in which to execute modification
	 * @param adaptation record to be modified
	 * @param pathValueMap map of new path-values
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return modified adaptation
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Modify#execute(ProcedureContext, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final ProcedureContext pContext,
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Modify.execute(
			pContext,
			adaptation,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	/** The record or data set to modify. */
	private Adaptation adaptation;

	/** The map between paths and values. */
	private Map<Path, Object> pathValueMap;

	/**
	 * Instantiates a new procedure.
	 */
	public ModifyValuesProcedure()
	{

	}

	/**
	 * Instantiates a new  procedure.
	 *
	 * @param pAdaptation the record or data set to modify
	 */
	public ModifyValuesProcedure(final Adaptation pAdaptation)
	{
		this.adaptation = pAdaptation;
	}

	/**
	 * Instantiates a new procedure.
	 *
	 * @param pAdaptation the record or data set to modify.
	 * @param pPathValueMap the map between path and values.
	 */
	public ModifyValuesProcedure(
		final Adaptation pAdaptation,
		final Map<Path, Object> pPathValueMap)
	{
		this.adaptation = pAdaptation;
		this.pathValueMap = pPathValueMap;
	}

	/**
	 * Clear amp of values.
	 */
	public void clearValues()
	{
		if (this.pathValueMap != null)
			this.pathValueMap.clear();
	}

	/**
	 * Return true if the pathValueMap is null or empty
	 */
	public boolean isEmpty()
	{
		return this.pathValueMap == null || this.pathValueMap.isEmpty();
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
		final ValueContextForUpdate vcfu = pContext.getContext(this.adaptation.getAdaptationName());
		Map<Path, Object> pvm = getPathValueMap();
		if (pvm == null)
		{
			throw OperationException.createError("No values provided for modification");
		}
		for (Map.Entry<Path, Object> entry : pvm.entrySet())
		{
			vcfu.setValue(entry.getValue(), entry.getKey());
		}
		this.adaptation = pContext.doModifyContent(this.adaptation, vcfu);
	}

	@Override
	public TriggerAction[] getIgnoreTriggerActions()
	{
		return new TriggerAction[] { TriggerAction.MODIFY };
	}

	/**
	 * Gets the record or data set to modify.
	 *
	 * @return the record or data set to modify.
	 */
	public Adaptation getAdaptation()
	{
		return this.adaptation;
	}

	@Deprecated
	public Adaptation getModifiedAdaptation()
	{
		return this.adaptation;
	}

	/**
	 * Gets map between paths and values.
	 *
	 * @return the map between paths and values.
	 */
	public Map<Path, Object> getPathValueMap()
	{
		return this.pathValueMap;
	}

	/**
	 * Sets the the record or data set to modify.
	 *
	 * @param adaptation the record or data set to modify
	 */
	public void setAdaptation(final Adaptation adaptation)
	{
		this.adaptation = adaptation;
	}

	@Deprecated
	public void setModifiedAdaptation(final Adaptation modifiedAdaptation)
	{
		this.adaptation = modifiedAdaptation;
	}

	/**
	 * Sets the map between paths and values.
	 *
	 * @param pathValueMap the map between paths and values.
	 */
	public void setPathValueMap(final Map<Path, Object> pathValueMap)
	{
		this.pathValueMap = pathValueMap;
	}

	/**
	 * Add a value to the map between paths and values or modify it.
	 *
	 * @param pPath the path
	 * @param pValue the value
	 */
	public void setValue(final Path pPath, final Object pValue)
	{
		if (this.pathValueMap == null)
			this.pathValueMap = new HashMap<>();
		this.pathValueMap.put(pPath, pValue);
	}

	@Override
	protected AdaptationHome getHome()
	{
		return getAdaptation().getHome();
	}
}
