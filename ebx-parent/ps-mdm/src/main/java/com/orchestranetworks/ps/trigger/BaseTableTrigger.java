/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: we have introduced a new parameter, <enforcePermissions>, to this class
 *  -- Turning this on will now allow EBX to enforce User Permissions during Imports when using the Base Table Trigger
 *  -- There is a new class in the PS Library, <BaseTableTriggerEnforcingPermissions>, 
 *        that defaults this value to <true> and should be used a the base class for Table Triggers going forward.
 *  -- All Subclasses of the BaseTableTrigger defined in the PS Library have been updated to use this new Base Class
 *  -- By default this variable is set to <false> to provide backward compatibility with existing solutions,
 *       however we recommend changing your existing subclasses to use the new <BaseTableTriggerEnforcingPermissions> subclass
 *       or setting this to <true> either in your existing subclass or in the DMA, 
 *       providing you are using <valueContext.setValueEnablingAllPrivilegesOnNode> whenever you are setting a value programmatically
 *       that needs to be set regardless of the end users permissions (which is most of the time).
 *  -- When in the context of a cascade delete action, the permissions will be enforced only on the parent record that the delete
 *       action is being performed on. It will be assumed that if the user has permission to delete that record, the child
 *       records can also be deleted. This accounts for almost all use cases, but if for some reason, the child permissions need
 *       to be enforced independent from the parent's, then an alternate workaround will have to be implemented (such as enforcement
 *       via trigger exception).
 */

/**
 * This trigger enables the developer to configure whether to enable cascade delete and if enabled,
 * which related records should be deleted.  Subclasses can also control availability of modify/create/delete
 * actions for a table/occurrence.
 *
 * Typically, in order to enable cascading delete, you would use a BaseTableTrigger and set the enableCascadeDelete
 * parameter to true.  With no other parameters configured, this will result in the behavior that all records
 * associated via an association link or selection node will be deleted. Target records that are associated over a link
 * table won't be deleted unless it's configured explicitly to delete over a link table. In order to delete just the link
 * records themselves, you must have an association directly to that join table. (It can be hidden if you don't wish to
 * show it to the user.) That way, that association will automatically be included, and the association over the link table
 * won't.
 *
 * Optionally, you can specify association/selection node paths to skip. Furthermore, you can specify foreign key fields to
 * delete in the off chance that deleting a record in this table should result in the deletion of those fk-related records.
 *
 * When specifying paths, they should be separated by the default separator defined in {@link PathUtils#DEFAULT_SEPARATOR}
 * and represent the absolute path to the field (i.e. relative to the root, starting with "/").
 *
 * This trigger also allows subclasses to define a {@link TriggerActionValidator} instance that will be invoked to
 * cancel an action. (This is called a "functional guard" in the EBX documentation.) Often, other EBX components can be used
 * instead to more effectively enact these restrictions, so use this feature only when your specific conditions require it.
 * See the javadoc on {@link TriggerActionValidator} for details.
 */

/**
 * @deprecated Use {@link BaseTableTriggerEnforcingPermissions} instead
 */
@Deprecated

public class BaseTableTrigger extends TableTrigger
{
	public static final String CASCADE_DELETE_SESSION_ATTRIBUTE_PREFIX = "cascadeDelete_";
	public static final String IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE = "ignoreValidateAction";
	private static final String ORIG_IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE_PREFIX = "origIgnoreValidateAction_";

	public static final String INVOKING_CASCADE_DELETE_SESSION_ATTRIBUTE = "invokingCascadeDelete";
	public static final String ROOT_OF_CASCADE_DELETE = "rootOfCascadeDelete";

	// IMPORTANT NOTE: we have introduced this new parameter, <enforcePermissions>
	protected boolean enforcePermissions = false;
	// -- Turning this on will allow EBX to enforce User Permissions during Imports when using the Base Table Trigger

	protected boolean enableCascadeDelete;
	protected boolean alwaysEnableCascadeDelete;
	protected String foreignKeysToCascadeDelete;
	protected String selectionNodesToNotCascadeDelete;
	protected String associationsToNotCascadeDelete;
	protected String associationsOverLinkTableToCascadeDelete;

	private Set<Path> foreignKeyPathsToProcess;
	private Set<Path> selectionNodePathsToProcess;
	private Set<Path> associationPathsToProcess;

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		// Enable all Privileges so that any hidden fields assigned programmatically can be written
		if (!enforcePermissions)
		{
			context.setAllPrivileges();
		}
		super.handleBeforeCreate(context);

		validateTriggerAction(
			context.getSession(),
			context.getOccurrenceContext(),
			null,
			TriggerAction.CREATE);
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		// Enable all Privileges so that any hidden fields assigned programmatically can be written
		if (!enforcePermissions)
		{
			context.setAllPrivileges();
		}
		super.handleBeforeModify(context);

		validateTriggerAction(
			context.getSession(),
			context.getOccurrenceContext(),
			context.getChanges(),
			TriggerAction.MODIFY);
	}

	@Override
	public void handleBeforeDelete(BeforeDeleteOccurrenceContext context) throws OperationException
	{
		Session session = context.getSession();
		validateTriggerAction(session, context.getOccurrenceContext(), null, TriggerAction.DELETE);

		// This is for backwards compatibility but allowDelete is deprecated
		UserMessage deleteErrorMsg = allowDelete(context);
		if (deleteErrorMsg != null)
		{
			throw OperationException.createError(deleteErrorMsg);
		}

		if (shouldCascadeDelete(context))
		{
			// If we're cascade deleting and there's no root record, this must be the root,
			// so set it in the session
			Adaptation record = context.getAdaptationOccurrence();
			if (session.getAttribute(ROOT_OF_CASCADE_DELETE) == null)
			{
				session.setAttribute(ROOT_OF_CASCADE_DELETE, record.getOccurrencePrimaryKey());
			}

			// AfterDeleteOccurrenceContext lets you get the ValueContext of the deleted record,
			// but you can't follow an association or selection line on a ValueContext.
			// Therefore we need to save the adaptation in the session before deleting so we can
			// access it after.
			String tablePathStr = context.getTable().getTablePath().format();
			session.setAttribute(CASCADE_DELETE_SESSION_ATTRIBUTE_PREFIX + tablePathStr, record);
			session.setAttribute(
				ORIG_IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE_PREFIX + tablePathStr,
				session.getAttribute(IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE));
			session.setAttribute(
				IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
				new TriggerAction[] { TriggerAction.DELETE });
		}
	}

	/**
	 * Validate the operation and throw an exception if it should be canceled,
	 * unless the session indicates to ignore validation.
	 * This method invokes {@link #createTriggerActionValidator(TriggerAction)} to do the validation.
	 *
	 * Note that throwing an exception from a trigger operation should only be done in specific cases.
	 * See the javadoc in {@link TriggerActionValidator} for details.
	 *
	 * @param session the session
	 * @param valueContext the value context
	 * @param valueChanges the changes, when a modify is being performed
	 * @param triggerAction the action (create, delete, or modify)
	 * @throws OperationException if the validate fails and the operation should be canceled
	 */
	protected void validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction triggerAction)
		throws OperationException
	{
		TriggerAction[] ignoreActions = (TriggerAction[]) session
			.getAttribute(IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE);
		if (ignoreActions == null || !ArrayUtils.contains(ignoreActions, triggerAction))
		{
			TriggerActionValidator triggerActionValidator = createTriggerActionValidator(
				triggerAction);
			if (triggerActionValidator != null)
			{
				UserMessage msg = triggerActionValidator
					.validateTriggerAction(session, valueContext, valueChanges, triggerAction);
				if (msg != null)
				{
					throw OperationException.createError(msg);
				}
			}
		}
	}

	/**
	 * Create a validator. It can be specific to a particular action or could ignore the
	 * <code>triggerAction</code> param and handle all actions. By default, this returns <code>null</code>.
	 *
	 * A {@link TriggerActionValidator} should be used only in specific cases. Refer to its javadoc for details.
	 *
	 * @param triggerAction the action to create it for
	 * @return a validator
	 */
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return null;
	}

	@Override
	public void handleAfterDelete(AfterDeleteOccurrenceContext context) throws OperationException
	{
		if (shouldCascadeDelete(context))
		{
			Session session = context.getSession();
			String tablePathStr = context.getTable().getTablePath().format();
			String sessionAttributeName = CASCADE_DELETE_SESSION_ATTRIBUTE_PREFIX + tablePathStr;

			try
			{
				// Ideally this would be done in setup but we can't guarantee that the schema has
				// completed compilation there.
				initCascadeDelete(context.getTable().getTableNode());

				Adaptation deletedRecord = (Adaptation) session.getAttribute(sessionAttributeName);
				PrimaryKey deletedRecordPK = deletedRecord.getOccurrencePrimaryKey();
				try
				{
					cascadeDelete(deletedRecord, context);
				}
				finally
				{
					// If this was the root of the cascade delete, clear it from the session
					PrimaryKey rootPK = (PrimaryKey) session.getAttribute(ROOT_OF_CASCADE_DELETE);
					if (deletedRecordPK.equals(rootPK))
					{
						session.setAttribute(ROOT_OF_CASCADE_DELETE, null);
					}
				}
			}
			finally
			{
				try
				{
					session.setAttribute(
						IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE,
						session.getAttribute(
							ORIG_IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE_PREFIX + tablePathStr));
				}
				finally
				{
					session.setAttribute(sessionAttributeName, null);
					session.setAttribute(
						ORIG_IGNORE_VALIDATE_ACTION_SESSION_ATTRIBUTE_PREFIX + tablePathStr,
						null);
				}
			}
		}
	}

	/**
	 * Indicates whether a cascade delete should be performed. By default, it is performed
	 * if the user is in a workflow and if the enableCascadeDelete field is set to true,
	 * but this can be subclassed for different behavior.
	 */
	protected boolean shouldCascadeDelete(TableTriggerExecutionContext context)
	{
		if (alwaysEnableCascadeDelete)
			return true;
		Session session = context.getSession();
		return enableCascadeDelete
			&& (Boolean.TRUE.equals(session.getAttribute(INVOKING_CASCADE_DELETE_SESSION_ATTRIBUTE))
				|| session.getInteraction(true) != null);
	}

	/**
	 * Specify whether to allow deletions on this table. Returning an error message indicates
	 * that that error message should be displayed to the user (via an exception).
	 * Returning null indicates there is no error that should prevent the delete from
	 * occurring. Default returns null and it can be overridden to return a different value.
	 *
	 * @return the error message to use, or null if deletes should be allowed
	 * @deprecated Implement {@link #createTriggerActionValidator(TriggerAction)} instead
	 */
	@Deprecated
	protected UserMessage allowDelete(BeforeDeleteOccurrenceContext context)
	{
		return null;
	}

	protected void cascadeDelete(Adaptation deletedRecord, AfterDeleteOccurrenceContext context)
		throws OperationException
	{
		ProcedureContext pContext = context.getProcedureContext();
		// Call the getter because subclasses can override
		DeleteRecordProcedure drp = new DeleteRecordProcedure();
		drp.setAllPrivileges(true);
		drp.setDeletingChildren(true);
		for (Path fkPath : getForeignKeyPathsToProcess(deletedRecord, context))
		{
			Adaptation foreignRecord = AdaptationUtil
				.followFK(deletedRecord, Path.SELF.add(fkPath));
			if (foreignRecord != null)
			{
				if (shouldDeleteForeignKeyRecord(context, foreignRecord, deletedRecord, fkPath))
				{
					drp.setAdaptation(foreignRecord);
					drp.execute(pContext);
				}
			}
		}

		deleteLinkedRecords(
			getSelectionNodePathsToProcess(deletedRecord, context),
			deletedRecord,
			pContext);
		deleteLinkedRecords(
			getAssociationPathsToProcess(deletedRecord, context),
			deletedRecord,
			pContext);
	}

	// Override this method if there are conditions in which a Foreign Key record should NOT be deleted
	//  i.e. If is is being referenced by other records
	protected boolean shouldDeleteForeignKeyRecord(
		AfterDeleteOccurrenceContext context,
		Adaptation foreignRecord,
		Adaptation deletedRecord,
		Path fkPath)
	{
		return true;
	}

	private void deleteLinkedRecords(
		Set<Path> paths,
		Adaptation deletedRecord,
		ProcedureContext pContext)
		throws OperationException
	{
		DeleteRecordProcedure drp = new DeleteRecordProcedure();
		drp.setAllPrivileges(true);
		drp.setDeletingChildren(true);
		for (Path path : paths)
		{
			List<Adaptation> relatedRecords = AdaptationUtil
				.getLinkedRecordList(deletedRecord, Path.SELF.add(path));
			for (Adaptation relatedRecord : relatedRecords)
			{
				drp.setAdaptation(relatedRecord);
				drp.execute(pContext);
			}
		}
	}

	protected Set<Path> getForeignKeyPathsToProcess(
		Adaptation deletedRecord,
		AfterDeleteOccurrenceContext context)
	{
		return foreignKeyPathsToProcess;
	}

	protected Set<Path> getSelectionNodePathsToProcess(
		Adaptation deletedRecord,
		AfterDeleteOccurrenceContext context)
	{
		return selectionNodePathsToProcess;
	}

	protected Set<Path> getAssociationPathsToProcess(
		Adaptation deletedRecord,
		AfterDeleteOccurrenceContext context)
	{
		return associationPathsToProcess;
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		String[] validPrefixes = new String[] { Path.ROOT.format() };
		PathUtils.validatePathString(context, foreignKeysToCascadeDelete, null, validPrefixes);
		PathUtils
			.validatePathString(context, selectionNodesToNotCascadeDelete, null, validPrefixes);
		PathUtils.validatePathString(context, associationsToNotCascadeDelete, null, validPrefixes);
		PathUtils.validatePathString(
			context,
			associationsOverLinkTableToCascadeDelete,
			null,
			validPrefixes);

		// Ideally we'd init the cascade delete fields also but we can't because can't be assured
		// that compilation has completed.
	}

	protected void initCascadeDelete(SchemaNode tableNode)
	{
		// We can't guarantee that the same table trigger instance is used in between
		// trigger transactions, so each time we have to check if it's null and initialize
		// the attributes again.
		if (foreignKeyPathsToProcess == null)
		{
			foreignKeyPathsToProcess = PathUtils
				.convertStringToPathSet(getForeignKeysToCascadeDelete(), null);

			Set<Path> selectionNodePathsToSkip = PathUtils
				.convertStringToPathSet(getSelectionNodesToNotCascadeDelete(), null);
			selectionNodePathsToProcess = getTableSelectionNodeOrAssociationPaths(
				tableNode.getTableOccurrenceRootNode(),
				selectionNodePathsToSkip,
				new HashSet<Path>(),
				false);

			Set<Path> associationPathsToSkip = PathUtils
				.convertStringToPathSet(getAssociationsToNotCascadeDelete(), null);
			Set<Path> associationOverLinkTablePathsToProcess = PathUtils
				.convertStringToPathSet(getAssociationsOverLinkTableToCascadeDelete(), null);
			associationPathsToProcess = getTableSelectionNodeOrAssociationPaths(
				tableNode.getTableOccurrenceRootNode(),
				associationPathsToSkip,
				associationOverLinkTablePathsToProcess,
				true);
		}
	}

	/**
	 * When a field is added to a model, all existing rows will have that value set to "inherited". Then when the record is saved,
	 * it will get the value of <code>null</code> (unless the user gave it a value), and it will be considered a change.
	 * This method will consider a change from "inherited" to <code>null</code> as not really being a change.
	 * This should only be used for fields that have <code>null</code> as their default value.
	 *
	 * @param change the change
	 * @return whether it changed
	 */
	protected static boolean isValueChangedIgnoringInheritedToNull(ValueChange change)
	{
		return change != null && !(AdaptationValue.INHERIT_VALUE.equals(change.getValueBefore())
			&& change.getValueAfter() == null);
	}

	private static Set<Path> getTableSelectionNodeOrAssociationPaths(
		SchemaNode node,
		Set<Path> pathsToSkip,
		Set<Path> pathsToProcess,
		boolean association)
	{
		Set<Path> paths = new HashSet<>();
		collectTableSelectionNodeOrAssociationPaths(
			paths,
			node,
			pathsToSkip,
			pathsToProcess,
			association);
		return paths;
	}

	private static void collectTableSelectionNodeOrAssociationPaths(
		Set<Path> paths,
		SchemaNode node,
		Set<Path> pathsToSkip,
		Set<Path> pathsToCascadeDelete,
		boolean association)
	{
		SchemaNode[] children = node.getNodeChildren();
		for (SchemaNode child : children)
		{
			if (child.isTerminalValue())
			{
				Path childPath = child.getPathInAdaptation();
				boolean processChild = false;
				if (association)
				{
					AssociationLink assocLink = child.getAssociationLink();
					if (assocLink != null)
					{
						if (!assocLink.isLinkTable() || pathsToCascadeDelete.contains(childPath))
						{
							processChild = true;
						}
					}
				}
				else if (child.getSelectionLink() != null)
				{
					processChild = true;
				}
				if (processChild && !pathsToSkip.contains(childPath))
				{
					paths.add(childPath);
				}
			}
			else
			{
				collectTableSelectionNodeOrAssociationPaths(
					paths,
					child,
					pathsToSkip,
					pathsToCascadeDelete,
					association);
			}
		}
	}

	/**
	 * {@link #setEnableCascadeDelete(boolean)}
	 *
	 * @return whether cascade delete is enabled
	 */
	public boolean isEnableCascadeDelete()
	{
		return this.enableCascadeDelete;
	}

	/**
	 * Set whether cascade delete is enabled on this table. If <code>false</code>,
	 * no cascade deleting will occur regardless of the other settings.
	 *
	 * @param enableCascadeDelete whether cascade delete is enabled
	 */
	public void setEnableCascadeDelete(boolean enableCascadeDelete)
	{
		this.enableCascadeDelete = enableCascadeDelete;
	}

	/**
	 * {@link #setForeignKeysToCascadeDelete(String)}
	 *
	 * @return the list of foreign keys
	 */
	public String getForeignKeysToCascadeDelete()
	{
		return this.foreignKeysToCascadeDelete;
	}

	/**
	 * Set a list of paths for foreign keys to navigate through to cascade delete.
	 * By default, no foreign keys are cascade deleted.
	 * See class javadoc for details on specifying paths.
	 *
	 * @param foreignKeysToCascadeDelete a string with the foreign key paths
	 */
	public void setForeignKeysToCascadeDelete(String foreignKeysToCascadeDelete)
	{
		this.foreignKeysToCascadeDelete = foreignKeysToCascadeDelete;
	}

	/**
	 * {@link #setSelectionNodesToNotCascadeDelete(String)}
	 *
	 * @return a string with the selection node paths
	 */
	public String getSelectionNodesToNotCascadeDelete()
	{
		return this.selectionNodesToNotCascadeDelete;
	}

	/**
	 * Set a list of paths for selection nodes to not navigate through to cascade delete.
	 * By default, all selection nodes are cascade deleted.
	 * See class javadoc for details on specifying paths.
	 *
	 * @param selectionNodesToNotCascadeDelete a string with the selection node paths
	 */
	public void setSelectionNodesToNotCascadeDelete(String selectionNodesToNotCascadeDelete)
	{
		this.selectionNodesToNotCascadeDelete = selectionNodesToNotCascadeDelete;
	}

	/**
	 * {@link #setAssociationsToNotCascadeDelete(String)}
	 *
	 * @return a string with the association paths
	 */
	public String getAssociationsToNotCascadeDelete()
	{
		return this.associationsToNotCascadeDelete;
	}

	/**
	 * Set a list of paths for associations to not navigate through to cascade delete.
	 * By default, all associations that are not over a link table are cascade deleted.
	 * See class javadoc for details on specifying paths.
	 *
	 * @param associationsToNotCascadeDelete a string with the association paths
	 */
	public void setAssociationsToNotCascadeDelete(String associationsToNotCascadeDelete)
	{
		this.associationsToNotCascadeDelete = associationsToNotCascadeDelete;
	}

	/**
	 * {@link #setAssociationsOverLinkTableToCascadeDelete(String)}
	 *
	 * @return a string with the association paths
	 */
	public String getAssociationsOverLinkTableToCascadeDelete()
	{
		return this.associationsOverLinkTableToCascadeDelete;
	}

	/**
	 * Set a list of paths for associations that are over link tables to cascade delete.
	 * By default, no associations over link tables are cascade deleted.
	 * See class javadoc for details on specifying paths.
	 *
	 * @param associationsOverLinkTableToCascadeDelete a string with the association paths
	 */
	public void setAssociationsOverLinkTableToCascadeDelete(
		String associationsOverLinkTableToCascadeDelete)
	{
		this.associationsOverLinkTableToCascadeDelete = associationsOverLinkTableToCascadeDelete;
	}

	public boolean isAlwaysEnableCascadeDelete()
	{
		return alwaysEnableCascadeDelete;
	}

	/**
	 * Whereas 'enableCascadeDelete' enables it under certain circumstances (like, from a workflow), alwaysEnableCascadeDelete
	 * means that the to-be-deleted children really make no sense without the parent and should be deleted also.
	 * @param alwaysEnableCascadeDelete
	 */
	public void setAlwaysEnableCascadeDelete(boolean alwaysEnableCascadeDelete)
	{
		this.alwaysEnableCascadeDelete = alwaysEnableCascadeDelete;
	}

	public boolean isEnforcePermissions()
	{
		return enforcePermissions;
	}

	public void setEnforcePermissions(boolean applyUserPermissions)
	{
		this.enforcePermissions = applyUserPermissions;
	}
}
