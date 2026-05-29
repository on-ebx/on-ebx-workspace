package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *
 * Create a record in a table with a map between Paths and values as input.
 * @author MCH
 *
 */
public class CreateRecordProcedure extends GenericProcedure
{
	/**
	 * @param adaptationTable table in which to create a record
	 * @param pathValueMap values for the new record
	 * @param session user session
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Create#execute(Session, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final AdaptationTable adaptationTable,
		final Map<Path, Object> pathValueMap,
		final Session session)
		throws OperationException
	{
		return Procedures.Create.execute(session, adaptationTable, pathValueMap, true, false);
	}

	/**
	 * @param adaptationTable table in which to create a record
	 * @param pathValueMap values for the new record
	 * @param session user session
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Create#execute(Session, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final AdaptationTable adaptationTable,
		final Map<Path, Object> pathValueMap,
		final Session session,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Create.execute(
			session,
			adaptationTable,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	/**
	 * @param pContext procedure context in which to create the record
	 * @param adaptationTable table in which to create the record
	 * @param pathValueMap values for the new record
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Create#execute(ProcedureContext, Adaptation, Map)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final ProcedureContext pContext,
		final AdaptationTable adaptationTable,
		final Map<Path, Object> pathValueMap)
		throws OperationException
	{
		return Procedures.Create.execute(pContext, adaptationTable, pathValueMap);
	}

	/**
	 * @param pContext procedure context in which to create the record
	 * @param adaptationTable table in which to create the record
	 * @param pathValueMap values for the new record
	 * @param enableAllPrivileges
	 * @param disableTriggerActivation
	 * @return created record
	 * @throws OperationException
	 * @deprecated use instance methods or {@link Procedures.Create#execute(ProcedureContext, Adaptation, Map, boolean, boolean)} instead.
	 */
	@Deprecated
	public static Adaptation execute(
		final ProcedureContext pContext,
		final AdaptationTable adaptationTable,
		final Map<Path, Object> pathValueMap,
		final boolean enableAllPrivileges,
		final boolean disableTriggerActivation)
		throws OperationException
	{
		return Procedures.Create.execute(
			pContext,
			adaptationTable,
			pathValueMap,
			enableAllPrivileges,
			disableTriggerActivation);
	}

	/** The created record. */
	private Adaptation createdRecord;

	/** The table. */
	private AdaptationTable table;

	/** Map between paths and values. */
	private Map<Path, Object> pathValueMap;

	/** Reset value context between creations. */
	private boolean resetValueContextBetweenCreations = true;

	/** The vcfu. */
	private ValueContextForUpdate vcfu;

	/**
	 * Instantiates a new creates the record procedure.
	 */
	public CreateRecordProcedure()
	{

	}

	/**
	 * Instantiates a new procedure.
	 *
	 * @param pTable the table
	 */
	public CreateRecordProcedure(final AdaptationTable pTable)
	{
		this.table = pTable;
	}

	/**
	 * Instantiates a new procedure.
	 *
	 * @param pTable the table
	 * @param pPathValueMap the map between paths and values
	 */
	public CreateRecordProcedure(
		final AdaptationTable pTable,
		final Map<Path, Object> pPathValueMap)
	{
		this.table = pTable;
		this.pathValueMap = pPathValueMap;
	}

	/**
	 * Clear the map of values.
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
		if (this.table == null)
		{
			throw OperationException.createError("Table cannot be null");
		}
		if (this.vcfu == null || this.resetValueContextBetweenCreations)
		{
			this.vcfu = pContext.getContextForNewOccurrence(this.table);
		}

		Map<Path, Object> pvm = getPathValueMap();
		if (pvm != null)
		{
			for (Map.Entry<Path, Object> entry : pvm.entrySet())
			{
				this.vcfu.setValue(entry.getValue(), entry.getKey());
			}
		}

		this.createdRecord = pContext.doCreateOccurrence(this.vcfu, this.table);
	}

	@Override
	public TriggerAction[] getIgnoreTriggerActions()
	{
		return new TriggerAction[] { TriggerAction.CREATE };
	}

	/**
	 * Gets the created record.
	 *
	 * @return the created record
	 */
	public Adaptation getCreatedRecord()
	{
		return this.createdRecord;
	}

	/**
	 * Gets the map between paths and values
	 *
	 * @return the path value map
	 */
	public Map<Path, Object> getPathValueMap()
	{
		return this.pathValueMap;
	}

	/**
	 * Gets the table.
	 *
	 * @return the table
	 */
	public AdaptationTable getTable()
	{
		return this.table;
	}

	/**
	 * Gets the vcfu.
	 *
	 * @return the vcfu
	 */
	public ValueContextForUpdate getVcfu()
	{
		return this.vcfu;
	}

	/**
	 * If true, a new value context will be created at each execution.
	 * If false, the same value context can be kept for all execution. It can improve performance as value context creation are expensive.
	 * However, the developer must ensure that all values in the map are relevant for further creation with the same procedure.
	 *
	 * @return true, if value context are reset between creations
	 */
	public boolean isResetValueContextBetweenCreations()
	{
		return this.resetValueContextBetweenCreations;
	}

	/**
	 * Replace the whole map between paths and values.
	 *
	 * @param pathValueMap the new map
	 */
	public void setPathValueMap(final Map<Path, Object> pathValueMap)
	{
		this.pathValueMap = pathValueMap;
	}

	/**
	 * If true, a new value context will be created at each execution.
	 * If false, the same value context can be kept for all execution. It can improve performance as value context creation are expensive.
	 * However, the developer must ensure that all values in the map are relevant for further creation with the same procedure.
	 *
	 * @param resetValueContextBetweenCreations the new reset value context between creations
	 */
	public void setResetValueContextBetweenCreations(
		final boolean resetValueContextBetweenCreations)
	{
		this.resetValueContextBetweenCreations = resetValueContextBetweenCreations;
	}

	/**
	 * Sets the table.
	 *
	 * @param table the new table
	 */
	public void setTable(final AdaptationTable table)
	{
		this.table = table;
	}

	/**
	 * Add a value to the map or change it
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
	 * Sets the value context to use to create the record.
	 *
	 * @param vcfu the new valut context
	 */
	public void setVcfu(final ValueContextForUpdate vcfu)
	{
		this.vcfu = vcfu;
	}

	@Override
	protected AdaptationHome getHome()
	{
		return getTable().getContainerAdaptation().getHome();
	}
}
