package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class DuplicateRecordProcedure extends GenericProcedure
{
	/** The record to duplicate */
	private Adaptation adaptation;

	/** The target table to copy the record into (optional -- assumes the source record's table */
	private AdaptationTable targetTable;

	/** The resulting, duplicate record */
	private Adaptation createdRecord;

	/** The map between paths and values. */
	private Map<Path, Object> pathValueMap;

	/**
	 * @param adaptation record to copy
	 * @param pathValueMap values to change on new copy
	 * @param session user session
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Duplicate#execute(Session, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		Session session)
		throws OperationException
	{
		return Procedures.Duplicate.execute(session, adaptation, pathValueMap);
	}

	/**
	 * @param adaptation record to copy
	 * @param pathValueMap values to change on new copy
	 * @param session user session
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Duplicate#execute(Session, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		Session session,
		boolean enableAllPrivileges,
		boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Duplicate.execute(
			session,
			adaptation,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	/**
	 * @param pContext ProcedureContext in which to execute copy
	 * @param adaptation record to copy
	 * @param pathValueMap values to change on new copy
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Duplicate#execute(ProcedureContext, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		ProcedureContext pContext,
		Adaptation adaptation,
		final Map<Path, Object> pathValueMap)
		throws OperationException
	{
		return Procedures.Duplicate.execute(pContext, adaptation, pathValueMap);
	}

	/**
	 * @param pContext ProcedureContext in which to execute copy
	 * @param adaptation record to copy
	 * @param pathValueMap values to change on new copy
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Duplicate#execute(ProcedureContext, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		ProcedureContext pContext,
		Adaptation adaptation,
		final Map<Path, Object> pathValueMap,
		boolean enableAllPrivileges,
		boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Duplicate.execute(
			pContext,
			adaptation,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	public DuplicateRecordProcedure()
	{
	}

	public DuplicateRecordProcedure(
		final Adaptation adaptation,
		final Map<Path, Object> pathValueMap)
	{
		this.adaptation = adaptation;
		setPathValueMap(pathValueMap);
	}

	public Adaptation getAdaptation()
	{
		return adaptation;
	}

	public void setAdaptation(Adaptation adaptation)
	{
		this.adaptation = adaptation;
	}

	public Adaptation getCreatedRecord()
	{
		return this.createdRecord;
	}

	public void setCreatedRecord(Adaptation createdRecord)
	{
		this.createdRecord = createdRecord;
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

	/**
	 * Clear amp of values.
	 */
	public void clearValues()
	{
		this.pathValueMap.clear();
	}

	/**
	 * Return true if the pathValueMap is null or empty
	 */
	public boolean isEmpty()
	{
		return this.pathValueMap == null || this.pathValueMap.isEmpty();
	}

	@Override
	protected AdaptationHome getHome()
	{
		return getAdaptation().getHome();
	}

	@Override
	public void doExecute(ProcedureContext pContext) throws OperationException
	{
		if (this.adaptation == null)
		{
			throw OperationException.createError("Adaptation cannot be null");
		}
		AdaptationTable targetTable = getTargetTable();
		if (targetTable == null)
			targetTable = adaptation.getContainerTable();
		final ValueContextForUpdate vcfu = pContext
			.getContextForNewOccurrence(adaptation, targetTable);
		Map<Path, Object> pvm = getPathValueMap();
		if (pvm != null)
		{
			for (Map.Entry<Path, Object> entry : pvm.entrySet())
			{
				vcfu.setValue(entry.getValue(), entry.getKey());
			}
		}
		this.createdRecord = pContext.doCreateOccurrence(vcfu, targetTable);
	}

	@Override
	public TriggerAction[] getIgnoreTriggerActions()
	{
		return new TriggerAction[] { TriggerAction.CREATE };
	}

	public AdaptationTable getTargetTable()
	{
		return targetTable;
	}

	public void setTargetTable(AdaptationTable targetTable)
	{
		this.targetTable = targetTable;
	}

}
