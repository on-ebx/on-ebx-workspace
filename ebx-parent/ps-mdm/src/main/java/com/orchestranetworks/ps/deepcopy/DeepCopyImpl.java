/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public class DeepCopyImpl
{
	public static final String DEEP_COPY_SESSION_ATTRIBUTE = "deepCopy";
	public static final String DEEP_COPY_PROCESSED_RECORD_MAP_SESSION_ATTRIBUTE = "deepCopyProcessedRecordMap";
	public static final String DEEP_COPY_TARGET_RECORD_SESSION_ATTRIBUTE = "deepCopyTargetRecord";

	private final DeepCopyConfigFactory configFactory;
	private final boolean openNewRecord;
	private Adaptation targetRecord;

	public DeepCopyImpl(DeepCopyConfigFactory configFactory)
	{
		this(configFactory, false);
	}

	public DeepCopyImpl(DeepCopyConfigFactory configFactory, boolean openNewRecord)
	{
		this.configFactory = configFactory;
		this.openNewRecord = openNewRecord;
	}

	protected void execute(Session session, Adaptation record, DeepCopyDataModifier dataModifier)
		throws OperationException
	{
		targetRecord = deepCopy(record, null, dataModifier, null, null, session);
	}

	/**
	 * @deprecated Use {@link deepCopy(ProcedureContext,Adaptation,AdaptationHome,DeepCopyDataModifier,DeepCopyRule,String)} instead.
	 */
	@Deprecated
	public Adaptation deepCopy(
		ProcedureContext pContext,
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		DeepCopyDataModifier dataModifier,
		String fkToParentSelectionNodeOrAssociationValue)
		throws OperationException
	{
		return deepCopy(
			pContext,
			record,
			dataSpaceToCopyTo,
			dataModifier,
			null,
			fkToParentSelectionNodeOrAssociationValue);
	}

	/**
	 * Call the deep copy when within an existing procedure context
	 * -- if copying to an Existing Record, set the DEEP_COPY_TARGET_RECORD_SESSION_ATTRIBUTE session attribute
	 *
	 * @param pContext the procedure context
	 * @param record the record to copy
	 * @param dataSpaceToCopyTo the data space to copy to,
	 *        or <code>null</code> if copying to same data space as the record
	 * @param dataModifier the data modifier, or <code>null</code>
	 * @param rule the rule determining whether to copy, or <code>null</code>
	 * @param fkToParentSelectionNodeOrAssociationValue the value of the foreign key to the parent node,
	 *        if this is a record of a table referencing a copied parent, or <code>null</code>
	 * @return the copied record
	 * @throws OperationException if an error occurs
	 */
	public Adaptation deepCopy(
		ProcedureContext pContext,
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		DeepCopyDataModifier dataModifier,
		DeepCopyRule rule,
		String fkToParentSelectionNodeOrAssociationValue)
		throws OperationException
	{
		return executeInProcedureContext(
			pContext,
			configFactory,
			record,
			dataSpaceToCopyTo,
			dataModifier,
			rule,
			fkToParentSelectionNodeOrAssociationValue);
	}

	/**
	 * @deprecated Use {@link deepCopy(Adaptation,AdaptationHome,DeepCopyDataModifier,DeepCopyRule,String,Session)} instead.
	 */
	@Deprecated
	public Adaptation deepCopy(
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		DeepCopyDataModifier dataModifier,
		String fkToParentSelectionNodeOrAssociationValue,
		Session session)
		throws OperationException
	{
		return deepCopy(
			record,
			dataSpaceToCopyTo,
			dataModifier,
			null,
			fkToParentSelectionNodeOrAssociationValue,
			session);
	}

	/**
	 * Call the deep copy in a new procedure
	 *
	 * @param record the record to copy
	 * @param dataSpaceToCopyTo the data space to copy to,
	 *        or <code>null</code> if copying to same data space as the record
	 * @param dataModifier the data modifier, or <code>null</code>
	 * @param rule the rule determining whether to copy, or <code>null</code>
	 * @param fkToParentSelectionNodeOrAssociationValue the value of the foreign key to the parent node,
	 *        if this is a record of a table referencing a copied parent, or <code>null</code>
	 * @param session the session
	 * @return the copied record
	 * @throws OperationException if an error occurs
	 */
	public Adaptation deepCopy(
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		DeepCopyDataModifier dataModifier,
		DeepCopyRule rule,
		String fkToParentSelectionNodeOrAssociationValue,
		Session session)
		throws OperationException
	{
		DeepCopyProcedure proc = new DeepCopyProcedure(
			record,
			dataSpaceToCopyTo,
			dataModifier,
			rule,
			fkToParentSelectionNodeOrAssociationValue);
		ProcedureExecutor.executeProcedure(
			proc,
			session,
			dataSpaceToCopyTo == null ? record.getHome() : dataSpaceToCopyTo);
		return proc.getCreatedRecord();
	}

	protected Adaptation doCopy(
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		String fkToParentSelectionNodeOrAssociationValue,
		DeepCopyConfig config,
		DeepCopyDataModifier globalDataModifier,
		DeepCopyRule globalDeepCopyRule,
		ProcedureContext pContext)
		throws OperationException
	{

		// allow the config to override the passed in dataModifier/rule
		DeepCopyDataModifier modifierToUse = globalDataModifier;
		DeepCopyDataModifier configModifier = config.getDataModifier();
		if (configModifier != null)
		{
			modifierToUse = configModifier;
		}
		DeepCopyRule ruleToUse = globalDeepCopyRule;
		DeepCopyRule configRule = config.getRule();
		if (configRule != null)
		{
			ruleToUse = configRule;
		}

		AdaptationTable table = record.getContainerTable();
		Path tablePath = table.getTablePath();
		if (dataSpaceToCopyTo != null)
		{
			Adaptation dataSet = dataSpaceToCopyTo.findAdaptationOrNull(
				AdaptationName.forName(record.getContainer().getAdaptationName().getStringName()));
			table = dataSet.getTable(tablePath);
		}

		// Check for copying over an Existing record
		Session session = pContext.getSession();
		Adaptation target = (Adaptation) session
			.getAttribute(DEEP_COPY_TARGET_RECORD_SESSION_ATTRIBUTE);
		session.setAttribute(DEEP_COPY_TARGET_RECORD_SESSION_ATTRIBUTE, null);

		if (target == null)
		{
			@SuppressWarnings("unchecked")
			Map<Path, Map<PrimaryKey, PrimaryKey>> processedRecordMap = (Map<Path, Map<PrimaryKey, PrimaryKey>>) session
				.getAttribute(DEEP_COPY_PROCESSED_RECORD_MAP_SESSION_ATTRIBUTE);
			// Create a map for storing keys of records already deep-copied, if it hasn't been
			// created yet
			if (processedRecordMap == null)
			{
				processedRecordMap = new HashMap<>();
				// Put it in the session
				session.setAttribute(
					DEEP_COPY_PROCESSED_RECORD_MAP_SESSION_ATTRIBUTE,
					processedRecordMap);
			}
			// Get the map of primary keys for this table, from the processed record map
			Map<PrimaryKey, PrimaryKey> pkMap = processedRecordMap.get(tablePath);
			// If it's not been created yet, create it
			if (pkMap == null)
			{
				pkMap = new HashMap<>();
				processedRecordMap.put(tablePath, pkMap);
			}
			// Get the primary key of the record that was copied for this record being processed
			PrimaryKey recordPK = record.getOccurrencePrimaryKey();
			PrimaryKey processedRecordPK = pkMap.get(recordPK);
			// If there was one saved, then lookup its record
			if (processedRecordPK != null)
			{
				target = table.lookupAdaptationByPrimaryKey(processedRecordPK);
			}

			// If we still have no target record, then none was found in the map and we need to
			// create it
			if (target == null)
			{
				ValueContextForUpdate vc = pContext.getContextForNewOccurrence(record, table);

				// If this is a record that was copied via a selection node or association,
				// need to set its foreign key back to the new parent record
				if (fkToParentSelectionNodeOrAssociationValue != null)
				{
					Path pathToParent = config.getPathToParentSelectionNodeOrAssociation();
					vc.setValue(fkToParentSelectionNodeOrAssociationValue, pathToParent);
				}

				// If there is a deep copy rule, check before proceeding.
				if (ruleToUse != null)
				{
					if (!ruleToUse.executeCopy(record, config, session))
					{
						return null;
					}
				}

				// If there is a data modifier, apply its modifications
				if (modifierToUse != null)
				{
					modifierToUse.modifyDuplicateRecordContext(vc, record, config, session);
				}

				// Copy records referenced via foreign key for any indicated
				Map<Path, DeepCopyConfig> foreignKeyConfigMap = config.getForeignKeyConfigMap();
				for (Map.Entry<Path, DeepCopyConfig> entry : foreignKeyConfigMap.entrySet())
				{
					Path path = entry.getKey();
					DeepCopyConfig fkConfig = entry.getValue();
					if (fkConfig != null)
					{
						Adaptation foreignRecord = AdaptationUtil.followFK(record, path);
						if (foreignRecord != null)
						{
							Adaptation newForeignRecord = doCopy(
								foreignRecord,
								dataSpaceToCopyTo,
								null,
								fkConfig,
								globalDataModifier,
								globalDeepCopyRule,
								pContext);
							// Set the fk of the new record to the new foreign record
							if (newForeignRecord != null)
							{
								vc.setValue(
									newForeignRecord.getOccurrencePrimaryKey().format(),
									path);
							}
						}
					}
				}

				// Create the record
				target = pContext.doCreateOccurrence(vc, table);
				// Store the record created in the map so we don't recreate it
				pkMap.put(recordPK, target.getOccurrencePrimaryKey());
			}
		}

		String newRecordPK = target.getOccurrencePrimaryKey().format();

		// Follow any specified selection nodes and for all records linked,
		// duplicate them and set them to link back to this new record
		Map<Path, DeepCopyConfig> selectionNodeConfigMap = config.getSelectionNodeConfigMap();
		for (Map.Entry<Path, DeepCopyConfig> entry : selectionNodeConfigMap.entrySet())
		{
			Path path = entry.getKey();
			DeepCopyConfig selNodeConfig = entry.getValue();
			if (selNodeConfig != null)
			{
				Path selNodeFKPath = selNodeConfig.getPathToParentSelectionNodeOrAssociation();
				if (selNodeFKPath == null)
				{
					throw OperationException.createError(
						"Config for copy of " + path.format()
							+ " doesn't specify the foreign key back to parent selection node.");
				}
				List<Adaptation> selNodeRecords = AdaptationUtil.getLinkedRecordList(record, path);
				for (Adaptation selNodeRecord : selNodeRecords)
				{
					doCopy(
						selNodeRecord,
						dataSpaceToCopyTo,
						newRecordPK,
						selNodeConfig,
						globalDataModifier,
						globalDeepCopyRule,
						pContext);
				}
			}
		}

		// Follow any specified associations and for all records linked,
		// duplicate them and set them to link back to this new record.
		// If the association uses a link table, handle that link table.
		Map<Path, DeepCopyConfig> associationConfigMap = config.getAssociationConfigMap();
		for (Map.Entry<Path, DeepCopyConfig> entry : associationConfigMap.entrySet())
		{
			Path path = entry.getKey();
			DeepCopyConfig associationConfig = entry.getValue();
			if (associationConfig != null)
			{
				Path associationFKPath = associationConfig
					.getPathToParentSelectionNodeOrAssociation();
				if (associationFKPath == null)
				{
					throw OperationException.createError(
						"Config for copy of " + path.format()
							+ " doesn't specify the foreign key back to parent association.");
				}
				AdaptationTable linkTable = associationConfig.getAssociationLinkTable();
				// There is no link table so duplicate the record and point back to parent,
				// same as we do for selection nodes
				if (linkTable == null)
				{
					List<Adaptation> associationRecords = AdaptationUtil
						.getLinkedRecordList(record, path);
					for (Adaptation associationRecord : associationRecords)
					{
						doCopy(
							associationRecord,
							dataSpaceToCopyTo,
							newRecordPK,
							associationConfig,
							globalDataModifier,
							globalDeepCopyRule,
							pContext);
					}
				}
				// Else find all records in the link table that point back to the parent and
				// do the copy on those
				else
				{
					RequestResult reqRes = linkTable.createRequestResult(
						associationFKPath.format() + "='"
							+ record.getOccurrencePrimaryKey().format() + "'");
					try
					{
						Adaptation linkRecord;
						while ((linkRecord = reqRes.nextAdaptation()) != null)
						{
							doCopy(
								linkRecord,
								dataSpaceToCopyTo,
								newRecordPK,
								associationConfig,
								globalDataModifier,
								globalDeepCopyRule,
								pContext);
						}
					}
					finally
					{
						reqRes.close();
					}
				}
			}
		}
		return target;
	}

	@SuppressWarnings("deprecation")
	private Adaptation executeInProcedureContext(
		ProcedureContext pContext,
		DeepCopyConfigFactory deepCopyConfigFactory,
		Adaptation record,
		AdaptationHome dataSpaceToCopyTo,
		DeepCopyDataModifier dataModifier,
		DeepCopyRule rule,
		String fkToParentSelectionNodeOrAssociationValue)
		throws OperationException
	{
		DeepCopyConfig config = deepCopyConfigFactory.createConfig(record.getContainerTable());

		Session session = pContext.getSession();
		boolean origAllPrivileges = pContext.isAllPrivileges();
		pContext.setAllPrivileges(true);
		// Need to set a session attribute to tell triggers not to fire.
		// Can't just turn off trigger activation because some aspects of
		// the triggers we do want to fire.
		// Trigger code needs to be implemented to check for this attribute.
		session.setAttribute(DEEP_COPY_SESSION_ATTRIBUTE, Boolean.TRUE);

		TriggerAction[] ignoreActions = (TriggerAction[]) session
			.getAttribute(BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE);
		session.setAttribute(
			BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
			new TriggerAction[] { TriggerAction.CREATE });
		Adaptation target = null;
		try
		{
			target = doCopy(
				record,
				dataSpaceToCopyTo,
				fkToParentSelectionNodeOrAssociationValue,
				config,
				dataModifier,
				rule,
				pContext);
		}
		finally
		{
			// Always set these back before exiting
			session.setAttribute(DEEP_COPY_SESSION_ATTRIBUTE, Boolean.FALSE);
			session.setAttribute(DEEP_COPY_PROCESSED_RECORD_MAP_SESSION_ATTRIBUTE, null);
			session.setAttribute(
				BaseTableTrigger.IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
				ignoreActions);
			pContext.setAllPrivileges(origAllPrivileges);
		}
		return target;
	}

	public DeepCopyConfigFactory getConfigFactory()
	{
		return this.configFactory;
	}

	private class DeepCopyProcedure implements Procedure
	{
		private Adaptation record;
		private AdaptationHome dataSpaceToCopyTo;
		private DeepCopyDataModifier dataModifier;
		private DeepCopyRule rule;
		private String fkToParentSelectionNodeOrAssociationValue;
		private Adaptation target;

		public DeepCopyProcedure(
			Adaptation record,
			AdaptationHome dataSpaceToCopyTo,
			DeepCopyDataModifier dataModifier,
			DeepCopyRule rule,
			String fkToParentSelectionNodeOrAssociationValue)
		{
			this.record = record;
			this.dataSpaceToCopyTo = dataSpaceToCopyTo;
			this.dataModifier = dataModifier;
			this.rule = rule;
			this.fkToParentSelectionNodeOrAssociationValue = fkToParentSelectionNodeOrAssociationValue;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			target = executeInProcedureContext(
				pContext,
				configFactory,
				record,
				dataSpaceToCopyTo,
				dataModifier,
				rule,
				fkToParentSelectionNodeOrAssociationValue);
		}

		public Adaptation getCreatedRecord()
		{
			return this.target;
		}
	}

	public Adaptation getTargetRecord()
	{
		return targetRecord;
	}

	public void setTargetRecord(Adaptation targetRecord)
	{
		this.targetRecord = targetRecord;
	}

	public boolean isOpenNewRecord()
	{
		return openNewRecord;
	}
}
