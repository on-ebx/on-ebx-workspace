/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 */
public class RecordMatchesTriggerActionValidator implements TriggerActionValidator
{
	private Path recordFieldPath;
	protected TriggerActionValidator nestedTriggerActionValidator;

	public RecordMatchesTriggerActionValidator()
	{
		this(null, null);
	}

	public RecordMatchesTriggerActionValidator(
		Path recordFieldPath,
		TriggerActionValidator nestedTriggerActionValidator)
	{
		this.recordFieldPath = recordFieldPath;
		this.nestedTriggerActionValidator = nestedTriggerActionValidator;
	}

	/**
	 * Get the records from the session that we should match against. This returns the workflow's record
	 * by default, if it's a record-based workflow, or <code>null</code>. <code>null</code> indicates
	 * that no matching needs to happen, i.e. everything should be considered matching.
	 * An empty list, on the other hand, indicates that matching needs to happen and there is currently
	 * nothing to match against so therefore nothing should be considered matching. The default behavior
	 * will never return an empty list, but this can be overridden to do so.
	 *
	 * @param session the session
	 * @param repo the repository
	 * @return a list of records to match against (possibly empty), or <code>null</code> if no matching needs to occur
	 * @throws OperationException if an error occurs getting the records from the session
	 */
	protected List<Adaptation> getRecordsToMatchFromSession(Session session, Repository repo)
		throws OperationException
	{
		Adaptation record = WorkflowUtilities
			.getRecordFromSessionInteraction(session.getInteraction(true), repo);
		if (record != null)
		{
			ArrayList<Adaptation> records = new ArrayList<>();
			records.add(record);
			return records;
		}
		return null;
	}

	protected Adaptation getRecordToMatchFromValueContext(ValueContext valueContext)
		throws OperationException
	{
		return recordFieldPath == null ? AdaptationUtil.getRecordForValueContext(valueContext)
			: AdaptationUtil.followFK(valueContext, recordFieldPath);
	}

	@Override
	public UserMessage validateTriggerAction(
		Session session,
		ValueContext valueContext,
		ValueChanges valueChanges,
		TriggerAction action)
		throws OperationException
	{
		if (nestedTriggerActionValidator != null)
		{
			UserMessage msg = nestedTriggerActionValidator
				.validateTriggerAction(session, valueContext, valueChanges, action);
			if (msg != null)
			{
				return msg;
			}
		}
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction != null)
		{
			List<Adaptation> sessionRecordsToMatch = getRecordsToMatchFromSession(
				session,
				valueContext.getAdaptationInstance().getHome().getRepository());
			if (sessionRecordsToMatch == null)
			{
				return null;
			}

			AdaptationTable table = valueContext.getAdaptationTable();
			SchemaNode matchingTableNode;
			if (recordFieldPath == null)
			{
				matchingTableNode = table.getTableNode();
			}
			else
			{
				SchemaNode recordFieldNode = valueContext.getNode(recordFieldPath);
				matchingTableNode = recordFieldNode.getFacetOnTableReference().getTableNode();
			}

			// If it's a create and it's on the target table itself
			if (action == TriggerAction.CREATE && recordFieldPath == null
				&& table.getTablePath().equals(matchingTableNode.getPathInSchema()))
			{
				// If there's no session records already then allow the create because
				// this is the initial creation.
				if (sessionRecordsToMatch.isEmpty())
				{
					return null;
				}
				// If there's a session record already, possibly prevent the create
				return preventCreateOnTargetTable(session, valueContext);
			}

			Locale locale = session.getLocale();

			// If it's already saved, get its label. Otherwise, or if it has none, get its pk.
			Adaptation valueContextRecord = AdaptationUtil.getRecordForValueContext(valueContext);
			String valueContextLabel = null;
			if (valueContextRecord != null)
			{
				valueContextLabel = valueContextRecord.getLabel(locale);
			}
			if (valueContextLabel == null)
			{
				valueContextLabel = table.computePrimaryKey(valueContext).format();
			}

			// Either it's a create that didn't match the above criteria, or it's
			// a modify or delete for a record that doesn't match the session records
			// (because there are no session records)
			if (sessionRecordsToMatch.isEmpty())
			{
				return UserMessage.createError(
					"Can't " + action + " record " + valueContextLabel
						+ ". There are no records to match against.");
			}

			String matchingTableLabel = matchingTableNode.getLabel(locale);
			Adaptation recordToMatch = getRecordToMatchFromValueContext(valueContext);
			if (recordToMatch == null)
			{
				return UserMessage.createError(
					"Can't find " + matchingTableLabel + " record for "
						+ table.computePrimaryKey(valueContext).format() + ".");
			}
			StringBuilder sessionRecordsBldr = new StringBuilder();
			sessionRecordsBldr.append(matchingTableLabel);
			sessionRecordsBldr.append(" record(s) ");
			for (int i = 0; i < sessionRecordsToMatch.size(); i++)
			{
				Adaptation sessionRecordToMatch = sessionRecordsToMatch.get(i);
				if (sessionRecordToMatch.equals(recordToMatch))
				{
					return null;
				}
				// Get the session record to match's label, or pk if null
				String sessionRecordToMatchLabel = sessionRecordToMatch.getLabel(locale);
				if (sessionRecordToMatchLabel == null)
				{
					sessionRecordToMatchLabel = sessionRecordToMatch.getOccurrencePrimaryKey()
						.format();
				}
				sessionRecordsBldr.append(sessionRecordToMatchLabel);
				if (i < sessionRecordsToMatch.size() - 1)
				{
					sessionRecordsBldr.append(" or ");
				}
			}
			// Get the record to match's label, or pk if null
			String recordToMatchLabel = recordToMatch.getLabel(locale);
			if (recordToMatchLabel == null)
			{
				recordToMatchLabel = recordToMatch.getOccurrencePrimaryKey().format();
			}
			return UserMessage.createError(
				"Can't " + action + " record " + valueContextLabel + ". The "
					+ recordToMatch.getContainerTable().getTableNode().getLabel(locale) + " record "
					+ recordToMatchLabel + " doesn't match " + sessionRecordsBldr + ".");
		}
		return null;
	}

	/**
	 * Determine whether creation should be prevented on the target table.
	 * By default, it is prevented but this can be overridden for different behavior.
	 * When it should be prevented, this will return an error message.
	 * Otherwise it will return <code>null</code>.
	 *
	 * @param session the session
	 * @param valueContext the value context of the record being created
	 * @return the error message, or <code>null</code> if creation shouldn't be prevented
	 */
	protected UserMessage preventCreateOnTargetTable(Session session, ValueContext valueContext)
	{
		String tableLabel = valueContext.getAdaptationTable().getTableNode().getLabel(
			session.getLocale());
		return UserMessage.createError(
			"Not allowed to " + TriggerAction.CREATE + " record in table " + tableLabel + ".");
	}

	public Path getRecordFieldPath()
	{
		return this.recordFieldPath;
	}

	public void setRecordFieldPath(Path recordFieldPath)
	{
		this.recordFieldPath = recordFieldPath;
	}
}