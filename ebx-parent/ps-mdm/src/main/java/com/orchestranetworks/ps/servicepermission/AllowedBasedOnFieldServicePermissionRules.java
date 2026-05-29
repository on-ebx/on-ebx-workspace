package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.hierarchy.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * This encapsulates multiple service permission rules classes that are all related in behavior.
 * They prevent executing a service based on the value of a field. See the inner individual classes for details.
 * 
 * Each class relates to a particular <code>EntitySelection</code> class (with the exception of {@link AbstractOnHierarchy}).
 * The classes available are:
 * <ul>
 * <li>{@link OnTable} (abstract)</li>
 * <li>{@link OnTableView}</li>
 * <li>{@link OnRecord}</li>
 * <li>{@link AbstractOnHierarchy} (abstract)</li>
 * <li>{@link OnHierarchy}</li>
 * <li>{@link OnHierarchyNode}</li>
 * <li>{@link OnAssociationTable} (abstract)</li>
 * <li>{@link OnAssociation}</li>
 * </ul>
 * 
 * When referencing these classes, it is recommended that you import the package, and then when referencing the inner class,
 * qualify it with the outer package, like so:
 * <pre>
 * AllowedBasedOnFieldServicePermissionRules.OnRecord rule = new AllowedBasedOnFieldServicePermissionRules.OnRecord(
 *     My_Field_Path,
 *     AllowedBasedOnFieldServicePermissionRules.NOT_NULL);
 * </pre>
 */
public interface AllowedBasedOnFieldServicePermissionRules
{
	/** A constant representing that the value to match should not be <code>null</code> */
	public static String NOT_NULL = "<not-null>";

	/**
	 * When a non-match is found, it will be represented by an instance of this class.
	 * If it was a match on a hierarchy, <code>nonMatchingHierarchyNode</code> will contain
	 * the hierarchy node that didn't match and <code>nonMatchOnJoinRecord</code> will indicate
	 * if the match was being done on the join record instead of the target record.
	 * Otherwise, <code>nonMatchingRecord</code> will contain the record that didn't match.
	 */
	public class NonMatchingResult
	{
		private HierarchyNode nonMatchingHierarchyNode;
		private boolean nonMatchOnJoinRecord;
		private Adaptation nonMatchingRecord;

		public NonMatchingResult(
			HierarchyNode nonMatchingHierarchyNode,
			boolean nonMatchOnJoinRecord)
		{
			this.nonMatchingHierarchyNode = nonMatchingHierarchyNode;
			this.nonMatchOnJoinRecord = nonMatchOnJoinRecord;
		}

		public NonMatchingResult(Adaptation nonMatchingRecord)
		{
			this.nonMatchingRecord = nonMatchingRecord;
		}

		public HierarchyNode getNonMatchingHierarchyNode()
		{
			return nonMatchingHierarchyNode;
		}

		public void setNonMatchingHierarchyNode(HierarchyNode nonMatchingHierarchyNode)
		{
			this.nonMatchingHierarchyNode = nonMatchingHierarchyNode;
		}

		public boolean isNonMatchOnJoinRecord()
		{
			return nonMatchOnJoinRecord;
		}

		public void setNonMatchOnJoinRecord(boolean nonMatchOnJoinRecord)
		{
			this.nonMatchOnJoinRecord = nonMatchOnJoinRecord;
		}

		public Adaptation getNonMatchingRecord()
		{
			return nonMatchingRecord;
		}

		public void setNonMatchingRecord(Adaptation nonMatchingRecord)
		{
			this.nonMatchingRecord = nonMatchingRecord;
		}
	}

	/**
	 * A rule for the a service that should be put on an table.
	 * It prevents executing the service based on the value of a specified field.
	 * 
	 * By default, it assumes a match should allow the service. If a match should NOT
	 * allow the service, then set <code>rejectWhenMatches</code> to <code>true</code>.
	 * 
	 * To specify any value as a match, use {@link #NOT_NULL} as the <code>valueToMatch</code>.
	 * To specify that the value must be one of multiple possible values, pass in a Collection
	 * for <code>valueToMatch</code>.
	 * 
	 * Alternatively, you can specify to match a value pulled from the session, rather than a static value.
	 * This can either come from a session attribute or a session interaction parameter (when in a workflow).
	 * To specify a session attribute, call {@link OnTable#setSessionAttributeToMatch(String)}, passing in
	 * the name of the attribute to pull the value from.
	 * To specify a session interaction parameter, call {@link OnTable#setSessionInteractionParameterToMatch(String)},
	 * passing in the name of the session interaction parameter to pull the value from. It will first look for an
	 * internal parameter, and if not found, will look for an input parameter. If the user isn't in a session that has
	 * that parameter defined, then the rule won't be applied (will be considered enabled).
	 * 
	 * When either is specified, <code>valueToMatch</code> will be ignored. (Therefore, you can
	 * pass in <code>null</code> to the constructor.)
	 * 
	 * If both a session interaction parameter and a session attribute are specified, it will ignore the session attribute.
	 * 
	 * This class is abstract and can't be used directly. You must instantiate a concrete subclass.
	 */
	public abstract class OnTable<S extends TableEntitySelection>
		implements ServicePermissionRule<S>
	{
		protected Path fieldPath;
		protected Object valueToMatch;
		protected boolean rejectWhenMatches;
		protected String sessionAttributeToMatch;
		protected String sessionInteractionParameterToMatch;

		protected OnTable(Path fieldPath, Object valueToMatch)
		{
			this.fieldPath = fieldPath;
			this.valueToMatch = valueToMatch;
		}

		/**
		 * Process the selection to determine if there are nodes/records that don't match
		 * 
		 * @param selection the selection
		 * @param session the session
		 * @return a result representing either the node or record that doesn't match.
		 *         (If selection is over multiple nodes/records, this is the first one that doesn't match.)
		 */
		protected abstract NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session);

		@Override
		public UserServicePermission getPermission(ServicePermissionRuleContext<S> context)
		{
			UserServicePermission override = getPermissionOverride(context);
			if (override != null)
			{
				return override;
			}

			Session session = context.getSession();
			// If using a session interaction parameter and we're not in a workflow with that parameter
			// defined, then we should not apply the rule.
			if (sessionInteractionParameterToMatch != null && !WorkflowUtilities
				.isSessionInteractionParameterDefined(session, sessionInteractionParameterToMatch))
			{
				return UserServicePermission.getEnabled();
			}

			TableEntitySelection selection = context.getEntitySelection();
			NonMatchingResult nonMatchingResult = processSelection(selection, session);
			if (nonMatchingResult == null)
			{
				return UserServicePermission.getEnabled();
			}
			return UserServicePermission.getDisabled(
				UserMessage.createError(
					createErrorMessage(
						context.getServiceKey(),
						selection.getTable().getTableNode(),
						session,
						nonMatchingResult)));
		}

		/**
		 * This returns a permission to always use based on the context, regardless of if the record matches.
		 * By default, it always allows the service if the session user is in the Tech Admin role.
		 * This can be overridden in subclasses to always allow or disallow based on other conditions.
		 * A response of <code>null</code> indicates that there is no override and the rest of the logic needs
		 * to be checked.
		 * 
		 * @param context the context
		 * @return the permission, or <code>null</code> if there is no override
		 */
		protected UserServicePermission getPermissionOverride(
			ServicePermissionRuleContext<S> context)
		{
			if (context.getSession().isUserInRole(CommonConstants.TECH_ADMIN))
			{
				return UserServicePermission.getEnabled();
			}

			return null;
		}

		/**
		 * Determine if there's a match with the given record.
		 * This will call {@link #matchValue(Object, Session)} to do the actual value comparison.
		 * 
		 * @param record the record to get the value from
		 * @param session the session
		 * @return whether it matches
		 */
		protected boolean matchRecord(Adaptation record, Session session)
		{
			Object value = record.get(fieldPath);
			return matchValue(value, session);
		}

		/**
		 * Return whether the given value matches the specified value to match
		 * (either statically defined or pulled from the session).
		 * 
		 * If the value to match is {@link #NOT_NULL}, this will return <code>true</code>
		 * when the value is not <code>null</code>.
		 * 
		 * @param value the value
		 * @param session the session
		 * @return whether it matches
		 */
		protected boolean matchValue(Object value, Session session)
		{
			boolean matches;
			Object objToMatch = determineValueToMatch(session);
			if (NOT_NULL.equals(objToMatch))
			{
				matches = (value != null);
			}
			else
			{
				// If matching against a collection of possible values,
				// then need to loop over those values until we find a match
				if (objToMatch instanceof Collection)
				{
					boolean found = false;
					Iterator<?> iter = ((Collection<?>) objToMatch).iterator();
					while (!found && iter.hasNext())
					{
						Object obj = iter.next();
						found = Objects.equals(value, obj);
					}
					matches = found;
				}
				// Otherwise just a straight compare
				else
				{
					matches = Objects.equals(value, objToMatch);
				}
			}
			return rejectWhenMatches != matches;
		}

		/**
		 * Determine the actual value to match, either from the static <code>valueToMatch</code>
		 * or from the session.
		 * 
		 * @param session the session
		 * @return the actual value to match
		 */
		protected Object determineValueToMatch(Session session)
		{
			Object value;
			if (sessionInteractionParameterToMatch == null)
			{
				if (sessionAttributeToMatch == null)
				{
					value = valueToMatch;
				}
				else
				{
					value = session.getAttribute(sessionAttributeToMatch);
				}
			}
			else
			{
				value = WorkflowUtilities
					.getSessionInteractionParameter(session, sessionInteractionParameterToMatch);
			}
			return value;
		}

		/**
		 * Get the label to show in the error message for the service key.
		 * In most cases, it's just the service key name but when that's not very user-friendly,
		 * this can return a different value.
		 * 
		 * @param serviceKey the service key
		 * @return the label
		 */
		protected static String getServiceKeyLabel(ServiceKey serviceKey)
		{
			if (ServiceKey.MASSDELETE.equals(serviceKey))
			{
				return "delete";
			}
			return serviceKey.getServiceName();
		}

		/**
		 * Create the error message to return when the value doesn't match,
		 * or when <code>rejectWhenMatches</code> is specified and the value does match.
		 * 
		 * @param serviceKey the service this rule is for
		 * @param selectionTableNode the table node of the table from the selection
		 * @param session the session
		 * @param nonMatchingResult the result representing the node/record that doesn't match. Note that this doesn't necessarily
		 *        have to be from the selection's table.
		 * @return the error message
		 */
		protected String createErrorMessage(
			ServiceKey serviceKey,
			SchemaNode selectionTableNode,
			Session session,
			NonMatchingResult nonMatchingResult)
		{
			Locale locale = session.getLocale();
			String serviceKeyLabel = getServiceKeyLabel(serviceKey);
			StringBuilder bldr = new StringBuilder("Cannot ").append(serviceKeyLabel)
				.append(" ")
				.append(selectionTableNode.getLabel(locale));
			HierarchyNode hierarchyNode = nonMatchingResult.getNonMatchingHierarchyNode();
			if (hierarchyNode == null)
			{
				bldr.append(" record ")
					.append(nonMatchingResult.getNonMatchingRecord().getLabel(locale));
			}
			else
			{
				if (hierarchyNode.isEnumerationNode())
				{
					bldr.append(" node ").append(hierarchyNode.getEnumerationValue());
				}
				else
				{
					bldr.append(" record ");
					Adaptation record;
					if (nonMatchingResult.isNonMatchOnJoinRecord())
					{
						record = hierarchyNode.getJoinOccurrence();
					}
					else
					{
						record = hierarchyNode.getOccurrence();
					}
					bldr.append(record.getLabel(locale));
				}
			}
			bldr.append(". ");
			if (rejectWhenMatches)
			{
				bldr.append("Cannot ");
			}
			else
			{
				bldr.append("Can only ");
			}
			Adaptation record;
			if (hierarchyNode == null)
			{
				record = nonMatchingResult.getNonMatchingRecord();
			}
			else if (hierarchyNode.isEnumerationNode())
			{
				record = hierarchyNode.getParentHierarchyNode().getOccurrence();
			}
			else if (nonMatchingResult.isNonMatchOnJoinRecord())
			{
				record = hierarchyNode.getJoinOccurrence();
			}
			else
			{
				record = hierarchyNode.getOccurrence();
			}
			SchemaNode fieldNode = record.getSchemaNode().getNode(fieldPath);
			bldr.append(serviceKeyLabel).append(" when ").append(fieldNode.getLabel(locale));
			Object objToMatch = determineValueToMatch(session);
			if (NOT_NULL.equals(objToMatch))
			{
				bldr.append(" has a value");
			}
			else if (objToMatch == null)
			{
				bldr.append(" has no value");
			}
			else
			{
				bldr.append(" is ");
				if (objToMatch instanceof Collection)
				{
					bldr.append("one of the values [");
					Iterator<?> iter = ((Collection<?>) objToMatch).iterator();
					while (iter.hasNext())
					{
						bldr.append(iter.next());
						if (iter.hasNext())
						{
							bldr.append(", ");
						}
					}
					bldr.append("]");
				}
				else
				{
					bldr.append(objToMatch);
				}
			}
			bldr.append(".");
			return bldr.toString();
		}

		public Path getFieldPath()
		{
			return fieldPath;
		}

		public void setFieldPath(Path fieldPath)
		{
			this.fieldPath = fieldPath;
		}

		public Object getValueToMatch()
		{
			return valueToMatch;
		}

		public void setValueToMatch(Object valueToMatch)
		{
			this.valueToMatch = valueToMatch;
		}

		public boolean isRejectWhenMatches()
		{
			return rejectWhenMatches;
		}

		public void setRejectWhenMatches(boolean rejectWhenMatches)
		{
			this.rejectWhenMatches = rejectWhenMatches;
		}

		public String getSessionAttributeToMatch()
		{
			return sessionAttributeToMatch;
		}

		public void setSessionAttributeToMatch(String sessionAttributeToMatch)
		{
			this.sessionAttributeToMatch = sessionAttributeToMatch;
		}

		public String getSessionInteractionParameterToMatch()
		{
			return sessionInteractionParameterToMatch;
		}

		public void setSessionInteractionParameterToMatch(String sessionInteractionParameterToMatch)
		{
			this.sessionInteractionParameterToMatch = sessionInteractionParameterToMatch;
		}
	}

	/**
	 * A subclass that should be used for a table view selection. It will allow the service
	 * only when all records in the selection meet the criteria.
	 */
	public class OnTableView extends OnTable<TableViewEntitySelection>
	{
		public OnTableView(Path fieldPath, Object valueToMatch)
		{
			super(fieldPath, valueToMatch);
		}

		@Override
		protected NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session)
		{
			Adaptation nonMatchingRecord = null;
			Request request = ((TableViewEntitySelection) selection).getSelectedRecords();
			RequestResult requestResult = request.execute();
			try
			{
				for (Adaptation record; nonMatchingRecord == null
					&& (record = requestResult.nextAdaptation()) != null;)
				{
					if (!matchRecord(record, session))
					{
						nonMatchingRecord = record;
					}
				}
			}
			finally
			{
				requestResult.close();
			}
			if (nonMatchingRecord == null)
			{
				return null;
			}
			return new NonMatchingResult(nonMatchingRecord);
		}
	}

	/**
	 * A subclass that should be used for a record selection. It will allow the service
	 * only when the selected record meets the criteria.
	 */
	public class OnRecord extends OnTable<RecordEntitySelection>
	{
		public OnRecord(Path fieldPath, Object valueToMatch)
		{
			super(fieldPath, valueToMatch);
		}

		@Override
		protected NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session)
		{
			Adaptation record = ((RecordEntitySelection) selection).getRecord();
			if (record == null || matchRecord(record, session))
			{
				return null;
			}
			return new NonMatchingResult(record);
		}
	}

	/**
	 * A subclass that encapsulates logic specific to hierarchy and hierarchy node selections.
	 * 
	 * When a hierarchy node is an enumeration node, it is assumed that it represents a value in the
	 * field to match in the parent node's record.
	 * When it is a record node, it will use either the target record or join record (when over a link
	 * table), based on the value of <code>useJoinRecord</code>.
	 * <code>useJoinRecord</code> is ignored when it is a hierarchy node.
	 * 
	 * This class is abstract and can't be used directly. You must instantiate a concrete subclass.
	 * 
	 * <b>NOTE: This class and its subclasses were added to put rules on built-in services for hierarchies,
	 * but that capability isn't supported by EBX yet. It should still be able to be used on custom services,
	 * but be aware that it hasn't been fully tested in that scenario.</b>
	 */
	public abstract class AbstractOnHierarchy<S extends TableEntitySelection> extends OnTable<S>
	{
		protected boolean useJoinRecord;

		protected AbstractOnHierarchy(Path fieldPath, Object valueToMatch, boolean useJoinRecord)
		{
			super(fieldPath, valueToMatch);
			this.useJoinRecord = useJoinRecord;
		}

		/**
		 * Determine if there's a match with the given hierarchy node.
		 * This will call {@link #matchValue(Object, Session)} or {@link #matchRecord(Adaptation, Session)}
		 * to do the actual comparison, depending on if it's an enumeration or record node,
		 * respectively.
		 * 
		 * @param hierarchyNode the hierarchy node to get the value from
		 * @param session the session
		 * @return whether it matches
		 */
		protected boolean matchHierarchyNode(HierarchyNode hierarchyNode, Session session)
		{
			if (hierarchyNode.isEnumerationNode())
			{
				Object value = hierarchyNode.getEnumerationValue();
				return matchValue(value, session);
			}
			Adaptation record;
			if (useJoinRecord)
			{
				record = hierarchyNode.getJoinOccurrence();
			}
			else
			{
				record = hierarchyNode.getOccurrence();
			}
			return (record == null || matchRecord(record, session));
		}

		public boolean isUseJoinRecord()
		{
			return useJoinRecord;
		}

		public void setUseJoinRecord(boolean useJoinRecord)
		{
			this.useJoinRecord = useJoinRecord;
		}
	}

	/**
	 * A subclass that should be used for a hierarchy selection. It will allow the service
	 * only when all records in the selection meet the criteria.
	 */
	public class OnHierarchy extends AbstractOnHierarchy<HierarchyEntitySelection>
	{
		public OnHierarchy(Path fieldPath, Object valueToMatch, boolean useJoinRecord)
		{
			super(fieldPath, valueToMatch, useJoinRecord);
		}

		@Override
		protected NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session)
		{
			// Loop over all selected hierarchy nodes until a non-matching one is found
			Iterator<HierarchyNode> hierarchyNodeIter = ((HierarchyEntitySelection) selection)
				.getSelectedHierarchyNodes();
			HierarchyNode nonMatchingHierarchyNode = null;
			while (nonMatchingHierarchyNode == null && hierarchyNodeIter.hasNext())
			{
				HierarchyNode hierarchyNode = hierarchyNodeIter.next();
				if (!matchHierarchyNode(hierarchyNode, session))
				{
					nonMatchingHierarchyNode = hierarchyNode;
				}
			}

			if (nonMatchingHierarchyNode == null)
			{
				return null;
			}
			return new NonMatchingResult(nonMatchingHierarchyNode, useJoinRecord);
		}
	}

	/**
	 * A subclass that should be used for a hierarchy node selection. It will allow the service
	 * only when the selected node meets the criteria.
	 */
	public class OnHierarchyNode extends AbstractOnHierarchy<HierarchyNodeEntitySelection>
	{
		public OnHierarchyNode(Path fieldPath, Object valueToMatch, boolean useJoinRecord)
		{
			super(fieldPath, valueToMatch, useJoinRecord);
		}

		@Override
		protected NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session)
		{
			HierarchyNode hierarchyNode = ((HierarchyNodeEntitySelection) selection)
				.getHierarchyNode();
			if (hierarchyNode == null || matchHierarchyNode(hierarchyNode, session))
			{
				return null;
			}
			return new NonMatchingResult(hierarchyNode, useJoinRecord);
		}
	}

	/**
	 * A abstract subclass that should be used for an association selection. It can be used in the same
	 * manner as a table view selection, where the logic is based on the selected records, or it can
	 * be based on the source record of the association. For that behavior, set <code>useAssociationSourceRecord</code>
	 * to <code>true</code>.
	 * 
	 * This class is abstract and can't be used directly. You must instantiate a concrete subclass.
	 */
	public abstract class OnAssociationTable<S extends AssociationTableEntitySelection>
		extends
		OnTableView
	{
		protected boolean useAssociationSourceRecord;

		protected OnAssociationTable(Path fieldPath, Object valueToMatch)
		{
			super(fieldPath, valueToMatch);
		}

		@Override
		protected NonMatchingResult processSelection(
			TableEntitySelection selection,
			Session session)
		{
			if (useAssociationSourceRecord)
			{
				Adaptation sourceRecord = ((AssociationTableEntitySelection) selection)
					.getAssociationSourceRecord();
				if (matchRecord(sourceRecord, session))
				{
					return null;
				}
				return new NonMatchingResult(sourceRecord);
			}
			return super.processSelection(selection, session);
		}

		public boolean isUseAssociationSourceRecord()
		{
			return useAssociationSourceRecord;
		}

		public void setUseAssociationSourceRecord(boolean useAssociationSourceRecord)
		{
			this.useAssociationSourceRecord = useAssociationSourceRecord;
		}
	}

	/**
	 * A subclass that should be used for an association selection. It will allow the service
	 * only when all records in the selection meet the criteria, or when the association
	 * source record meets the criteria, depending on the configuration.
	 */
	public class OnAssociation extends OnAssociationTable<AssociationEntitySelection>
	{
		public OnAssociation(Path fieldPath, Object valueToMatch)
		{
			super(fieldPath, valueToMatch);
		}
	}
}
