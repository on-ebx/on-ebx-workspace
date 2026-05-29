package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

@SuppressWarnings("deprecation")
public class BaseTableTriggerEnforcingPermissions extends BaseTableTrigger
{
	protected static final String ALL_DATA_SPACES_PREVENT_DELETE = "all";
	private static final String DATA_SPACES_PREVENTING_DELETE_SEPARATOR = ",";

	protected boolean preventDeleteWhenReferenced;
	protected String dataSpacesPreventingDelete;

	private boolean checkExternalToDataSetWhenPreventingDelete;
	private Set<String> dataSpacesPreventingDeleteSet;

	/**
	 *  IMPORTANT NOTE: This class should be used as the base class for Table Triggers going forward. 
	 *  -- This will enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
	 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
	 *       when setting fields that the end-user might not have permission to update
	 *  -- All Subclasses of the BaseTableTrigger defined in the PS Library have been updated to use this new Base Class 
	 *       and to use <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue>
	 */
	public BaseTableTriggerEnforcingPermissions()
	{
		super();

		// by default, this trigger will enforce user permissions.  This can be turned off in the DMA by setting this parameter to false if needed for some reason
		setEnforcePermissions(true);
	}

	// The following methods are declared here to avoid having deprecation warnings show up in Subclasses of this class

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);
		setupPreventDeleteWhenReferenced(context);
	}

	// Do the setup needed for the prevent delete when referenced functionality
	private void setupPreventDeleteWhenReferenced(TriggerSetupContext context)
	{
		checkExternalToDataSetWhenPreventingDelete = false;
		dataSpacesPreventingDeleteSet = null;
		if (preventDeleteWhenReferenced)
		{
			if (dataSpacesPreventingDelete != null)
			{
				// Set this flag so it's easier to check at runtime
				checkExternalToDataSetWhenPreventingDelete = true;
				// If the value for this field isn't all, then split the string and fill in the set of
				// data space names. If it is all, don't need to do anything since we're defaulting
				// the set to null which will mean all will be found.
				if (!ALL_DATA_SPACES_PREVENT_DELETE.equalsIgnoreCase(dataSpacesPreventingDelete))
				{
					String[] dataSpaceArr = dataSpacesPreventingDelete
						.split(DATA_SPACES_PREVENTING_DELETE_SEPARATOR);
					dataSpacesPreventingDeleteSet = new HashSet<>(Arrays.asList(dataSpaceArr));
				}
			}
		}
		// Doesn't make sense to specify data spaces when the flag is false, so if that's done, show an error
		else if (dataSpacesPreventingDelete != null)
		{
			context.addWarning(
				"dataSpacesPreventingDelete is only applicable when preventDeleteWhenReferenced is true.");
		}
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		super.handleBeforeModify(context);
	}

	@Override
	public void handleBeforeDelete(BeforeDeleteOccurrenceContext context) throws OperationException
	{
		super.handleBeforeDelete(context);

		Adaptation record = context.getAdaptationOccurrence();
		// Only check this if this trigger is set to enforce user privileges
		// and we're not already in a cascade delete context, or we are but it's the root record.
		// (i.e. For the immediate parent record being deleted, we want to enforce permissions
		// but not the ones it subsequently deletes.)
		if (enforcePermissions)
		{
			PrimaryKey rootRecordPK = (PrimaryKey) context.getSession()
				.getAttribute(ROOT_OF_CASCADE_DELETE);
			if (rootRecordPK == null || record.getOccurrencePrimaryKey().equals(rootRecordPK))
			{
				checkUserPermissionsAfterUpdate(context, null, record, "delete");
			}
		}

		// If we should prevent deletion when referenced and we're not cascade deleting,
		// then handle the prevention.
		// If we are cascade deleting, this is handled in cascadeDelete method, which is
		// called from handleAfterDelete.
		if (shouldPreventDeleteWhenReferenced(context) && !shouldCascadeDelete(context))
		{
			handlePreventDeleteWhenReferenced(record, context.getSession());
		}
	}

	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		// This will be skipped if this trigger is NOT set to enforce user privileges OR If we are being called from a Procedure where all Privileges have been enabled
		ProcedureContext pContext = context.getProcedureContext();
		if (enforcePermissions && !pContext.isAllPrivileges())
		{
			checkUserPermissionsAfterUpdate(
				context,
				pContext,
				context.getAdaptationOccurrence(),
				"create");
		}
		super.handleAfterCreate(context);
	}

	@Override
	public void handleAfterModify(AfterModifyOccurrenceContext context) throws OperationException
	{
		// This will be skipped if this trigger is NOT set to enforce user privileges OR If we are being called from a Procedure where all Privileges have been enabled
		ProcedureContext pContext = context.getProcedureContext();
		if (enforcePermissions && !pContext.isAllPrivileges())
		{
			checkUserPermissionsAfterUpdate(
				context,
				pContext,
				context.getAdaptationOccurrence(),
				"perform the requested modification to");
		}
		super.handleAfterModify(context);
	}

	// By Default, this will verify that the user has Read/Write Access for the RESULTING Record that has been Created or Modified or for the Record being Deleted.
	// -- This method should be overridden if alternative behavior is desired. 
	//       i.e. if there are use cases where it is actually intended that the Record should be Read Only (or Hidden) after this update takes place
	protected void checkUserPermissionsAfterUpdate(
		TableTriggerExecutionContext context,
		ProcedureContext pContext,
		Adaptation record,
		String operationMessageText)
		throws OperationException
	{
		Session session = context.getSession();
		SessionPermissions sessionPermissions = session.getPermissions();
		if (AccessPermission.getReadWrite() != sessionPermissions
			.getAdaptationAccessPermission(record))
		{
			UserMessage msg = UserMessage.createError(
				"You do not have permission to " + operationMessageText + " record "
					+ record.getLabel(session.getLocale()) + " in the "
					+ record.getContainerTable().getTableNode().getLabel(session) + " Table.");
			throw OperationException.createError(msg);
		}
	}

	// Just a convenience method since it's invoked from a couple places
	private boolean shouldPreventDeleteWhenReferenced(TableTriggerExecutionContext context)
	{
		return preventDeleteWhenReferenced && !isDeleteWhenReferencedAlwaysAllowed(context);
	}

	/**
	 * Get whether deleting the record when it is being referenced by other records should always
	 * be allowed, even when <code>preventDeleteWhenReferenced</code> is <code>true</code>.
	 * The standard behavior is to always allow it when the session's user belongs to the
	 * Tech Admin role (defined by {@link CommonConstants#TECH_ADMIN}), but this can be
	 * overridden to allow it in other situations.
	 * 
	 * @param context the context of the trigger. This can be called from both the before and after delete.
	 * @return whether it's always allowed
	 */
	protected boolean isDeleteWhenReferencedAlwaysAllowed(TableTriggerExecutionContext context)
	{
		return context.getSession().isUserInRole(CommonConstants.TECH_ADMIN);
	}

	/**
	 * When it's been determined that the deletion should be prevented when the record is
	 * referenced by other records, this will determine if other records reference this one,
	 * and throw an exception when they do.
	 * 
	 * @param record the record being deleted
	 * @param session the session
	 * @throws OperationException if the record is referenced by at least one other record
	 */
	protected void handlePreventDeleteWhenReferenced(Adaptation record, Session session)
		throws OperationException
	{
		Adaptation referrer = AdaptationUtil.getFirstFoundReferrer(
			record,
			checkExternalToDataSetWhenPreventingDelete,
			dataSpacesPreventingDeleteSet);
		if (referrer != null)
		{
			throw OperationException
				.createError(createReferrerFoundErrorMessage(record, referrer, session));
		}
	}

	// Creates the error message that will be returned when a record is referenced by another record.
	// There may be more than one referencing the record, but only details of one of them will be included.
	private String createReferrerFoundErrorMessage(
		Adaptation record,
		Adaptation referrer,
		Session session)
	{
		StringBuilder bldr = new StringBuilder(
			"Cannot delete record because it is referenced by one or more other records.");
		// Only include the details of the other record that's referencing this one if it's not hidden from the user
		SessionPermissions sessionPermissions = session.getPermissions();
		if (AccessPermission.getHidden() != sessionPermissions
			.getAdaptationAccessPermission(referrer))
		{
			Locale locale = session.getLocale();
			bldr.append(" First record found: ")
				.append(referrer.getLabel(locale))
				.append(" in table ")
				.append(referrer.getContainerTable().getTableNode().getLabel(locale));
			// If checking external to data set then may need to also include data space and data set.
			// If not, then that's not really necessary info and can keep message cleaner.
			if (checkExternalToDataSetWhenPreventingDelete)
			{
				AdaptationHome referrerDataSpace = referrer.getHome();
				Adaptation referrerDataSet = referrer.getContainer();
				AdaptationHome recordDataSpace = record.getHome();
				Adaptation recordDataSet = record.getContainer();
				// If the referrer's data space is the same as the record's
				if (referrerDataSpace.getKey().equals(recordDataSpace.getKey()))
				{
					// If it's in a different data set, then include the data set label.
					// Otherwise, not really necessary to include that info.
					if (!referrerDataSet.equals(recordDataSet))
					{
						bldr.append(" in data set ").append(referrerDataSet.getLabelOrName(locale));
					}
				}
				// They're in different data spaces so we'll want to include the data space in the message
				else
				{
					String referrerDataSetName = referrerDataSet.getAdaptationName()
						.getStringName();
					String recordDataSetName = recordDataSet.getAdaptationName().getStringName();
					// If the referrer is in a data set with a different name than this one,
					// then include the data set name. Otherwise, not really important to include that info.
					// Note that this is comparing the name, not the actual data set, since it will be a
					// different data set reference.
					if (!referrerDataSetName.equals(recordDataSetName))
					{
						bldr.append(" in data set ").append(referrerDataSet.getLabelOrName(locale));
					}
					bldr.append(" in data space ").append(referrerDataSpace.getLabelOrName(locale));
				}
			}
			bldr.append(".");
		}
		return bldr.toString();
	}

	@Override
	public void handleAfterDelete(AfterDeleteOccurrenceContext context) throws OperationException
	{
		super.handleAfterDelete(context);
	}

	@Override
	protected void cascadeDelete(Adaptation deletedRecord, AfterDeleteOccurrenceContext context)
		throws OperationException
	{
		super.cascadeDelete(deletedRecord, context);

		// If we should prevent deletion when referenced, then handle the prevention.
		// If we are not cascade deleting, this is handled in handleBeforeDelete.
		// This needs to be done after the cascade delete but before the session var storing the
		// record being deleted is cleared, which is why it's done inside this method and not
		// in handleBeforeDelete. Also, we have the deleted record reference already and know
		// we're cascade deleting at this point.
		// Because cascade delete happens first, those records won't get included when querying
		// for references. If an exception should occur, it's all in the same procedure context,
		// so it should back out the deletions that occurred from the cascade delete.
		if (shouldPreventDeleteWhenReferenced(context))
		{
			handlePreventDeleteWhenReferenced(deletedRecord, context.getSession());
		}
	}

	@Override
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return super.createTriggerActionValidator(triggerAction);
	}

	/**
	 * @see #setPreventDeleteWhenReferenced(boolean)
	 */
	public boolean isPreventDeleteWhenReferenced()
	{
		return preventDeleteWhenReferenced;
	}

	/**
	 * Get whether to prevent delete of the record when it is being referenced by other records
	 * 
	 * @param preventDeleteWhenReferenced whether to prevent delete
	 */
	public void setPreventDeleteWhenReferenced(boolean preventDeleteWhenReferenced)
	{
		this.preventDeleteWhenReferenced = preventDeleteWhenReferenced;
	}

	/**
	 * @see #setDataSpacesPreventingDelete(String)
	 */
	public String getDataSpacesPreventingDelete()
	{
		return dataSpacesPreventingDelete;
	}

	/**
	 * Set the data spaces to prevent delete of the record when
	 * <code>preventDeleteWhenReferenced</code> is <code>true</code>.
	 * 
	 * If no value is specified, then only the record's own data set will be searched.
	 * Keep in mind, that means if it's in a child data space, its master data space won't be searched,
	 * unless it is explicitly listed.
	 * 
	 * If {@link #ALL_DATA_SPACES_PREVENT_DELETE} is specified as the value, then it will search for
	 * a reference from any data space. Depending on the number of data spaces, this could be an expensive
	 * operation. It would be searching not just the master data spaces, but all children of the masters.
	 * 
	 * @param dataSpacesPreventingDelete a comma-separated list of data spaces to search,
	 *        or {@link #ALL_DATA_SPACES_PREVENT_DELETE} to search all,
	 *        or <code>null</code> if only the record's own data set should be searched or if
	 *        <code>preventDeleteWhenReferenced</code> is <code>false</code>.
	 */
	public void setDataSpacesPreventingDelete(String dataSpacesPreventingDelete)
	{
		this.dataSpacesPreventingDelete = dataSpacesPreventingDelete;
	}
}
