/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.deepcopy.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.util.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */


/**
 */
public abstract class ProjectSubjectTableTrigger extends BaseTableTriggerEnforcingPermissions
	implements ProjectPathCapable, SubjectPathCapable
{
	//	private static final String DELETED_PROJECT_SUBJECT_PROJECT_TYPE_PREFIX = "deletedProjectSubjectProjectType_";
	private static final String DELETED_PROJECT_SUBJECT_SUBJECT_PK_PREFIX = "deletedProjectSubjectSubjectPK_";
	public static final String ALLOW_SUBJECT_DELETION_PREFIX = "allowSubjectDeletion_";

	protected abstract String getMasterDataSpaceName();

	//
	// This is a stopgap measure to prevent someone from adding a subject to a non-new subject
	// project,
	// changing the subject, then removing it from the project. That would result in the changes
	// being merged
	// to the master even though they were never approved.
	//
	// Long-term strategy should be to back out the changes they made when they remove it, but that
	// can require some
	// extensive changes to be implemented so this is the default behavior. If that is done, then
	// extend this method
	// to not return a <code>ProjectSubjectTriggerActionValidator</code>.
	//
	@Override
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return new ProjectSubjectTriggerActionValidator();
	}

	/**
	 * Return whether to check that the project's type is a New Subject type.
	 * By default always checks if you're in a workflow, but can be overridden to customize the behavior.
	 *
	 * @param session the session
	 * @return whether to check if it's a new subject type
	 */
	protected boolean checkIfNewSubjectProjectType(Session session)
	{
		return session.getInteraction(true) != null;
	}

	/**
	 * Override this in order to specify to cascade delete when it's not a New Menu/Other Item
	 * project and when the item hasn't already been deleted. (This will occur when a Detach
	 * happens on the association.)
	 */
	// TODO: Can't do this because it will lead to error in EBX when it goes to delete
	//       the menu item in a Delete context. Waiting to hear back from engineering on
	//       how we can accomplish different behavior between Detach & Delete.
	//       For now, a tech admin will just have to delete the unattached menu item.
	//       If we end up doing this, will also need to set enableCascadeDelete = true and
	//       foreignKeysToDelete = /menuItem or /offerOrOtherItem
	//	@Override
	//	protected boolean shouldCascadeDelete(TableTriggerExecutionContext context)
	//	{
	//		if (super.shouldCascadeDelete(context))
	//		{
	//			String tablePathStr = context.getTable().getTablePath().format();
	//			String projectType = (String) context.getSession()
	//				.getAttribute(DELETED_PROJECT_SUBJECT_PROJECT_TYPE_PREFIX + tablePathStr);
	//			ProjectPathConfig projectPathConfig = getProjectPathConfig();
	//			if (projectPathConfig.isNewSubjectProjectType(projectType))
	//			{
	//				Adaptation subjectRecord = getSubjectRecordFromSessionAttribute(context);
	//				if (subjectRecord != null)
	//				{
	//					return true;
	//				}
	//			}
	//		}
	//		return false;
	//	}

	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		Session session = context.getSession();
		if (session.getInteraction(true) != null)
		{
			SubjectPathConfig subjectPathConfig = getSubjectPathConfig();

			Adaptation projectSubjectRecord = context.getAdaptationOccurrence();

			Adaptation projectRecord = AdaptationUtil.followFK(
				projectSubjectRecord,
				subjectPathConfig.getProjectSubjectProjectFieldPath());
			Adaptation subjectRecord = AdaptationUtil.followFK(
				projectSubjectRecord,
				subjectPathConfig.getProjectSubjectSubjectFieldPath());
			// TODO: This is a workaround for the fact that EBX allows link table records to be
			// created even when
			//       their fk filter is violated. Once that is addressed in EBX, this can be removed.
			// It needs to be done in afterCreate since we don't have a record to validate until
			// then.
			ValidationReport validationReport = projectSubjectRecord.getValidationReport();
			if (validationReport.hasItemsOfSeverityOrMore(Severity.ERROR))
			{
				throw OperationException.createError(
					"Association to "
						+ projectRecord.getContainerTable().getTableNode().getLabel(
							session.getLocale())
						+ " can't be created. "
						+ subjectRecord.getContainerTable().getTableNode().getLabel(
							session.getLocale())
						+ " may violate a constraint.");
			}
			modifySubjectRecordAfterCreate(
				projectRecord,
				subjectRecord,
				context.getProcedureContext(),
				session);
		}
	}

	protected void modifySubjectRecordAfterCreate(
		Adaptation projectRecord,
		Adaptation subjectRecord,
		ProcedureContext pContext,
		Session session)
		throws OperationException
	{
		ProjectPathConfig projectPathConfig = getProjectPathConfig();
		String projectType = projectRecord
			.getString(projectPathConfig.getProjectProjectTypeFieldPath());

		setCurrentProjectType(projectType, pContext, session, projectRecord, subjectRecord);
	}

	@Override
	public void handleBeforeDelete(BeforeDeleteOccurrenceContext context) throws OperationException
	{
		Session session = context.getSession();
		Set<String> sessionAttributes = null;
		if (session.getInteraction(true) != null)
		{
			sessionAttributes = new HashSet<>();
			SubjectPathConfig subjectPathConfig = getSubjectPathConfig();
			Adaptation projectSubjectRecord = context.getAdaptationOccurrence();
			String subjectPK = projectSubjectRecord
				.getString(subjectPathConfig.getProjectSubjectSubjectFieldPath());
			String tablePathStr = context.getTable().getTablePath().format();
			String sessionAttribute = DELETED_PROJECT_SUBJECT_SUBJECT_PK_PREFIX + tablePathStr;
			session.setAttribute(sessionAttribute, subjectPK);
			sessionAttributes.add(sessionAttribute);

			ProjectPathConfig projectPathConfig = getProjectPathConfig();
			String projectType = (String) AdaptationUtil.followFK(
				projectSubjectRecord,
				subjectPathConfig.getProjectSubjectProjectFieldPath(),
				projectPathConfig.getProjectProjectTypeFieldPath());
			if (projectPathConfig.isNewSubjectProjectType(projectType))
			{
				// Put this in the session to be cleared out by the subject trigger.
				// If in a New Subject workflow, they detach the subject instead of
				// delete, then it won't get cleared but shouldn't really matter.
				sessionAttribute = ALLOW_SUBJECT_DELETION_PREFIX
					+ subjectPathConfig.getSubjectTablePath().format() + "_" + subjectPK;
				session.setAttribute(sessionAttribute, "true");
				sessionAttributes.add(sessionAttribute);
			}
			//			sessionAttribute = DELETED_PROJECT_SUBJECT_PROJECT_TYPE_PREFIX + tablePathStr;
			//			session.setAttribute(sessionAttribute, projectType);
			//			sessionAttributes.add(sessionAttribute);
		}
		try
		{
			super.handleBeforeDelete(context);
		}
		catch (Exception ex)
		{
			if (sessionAttributes != null)
			{
				for (String sessionAttribute : sessionAttributes)
				{
					session.setAttribute(sessionAttribute, null);
				}
			}
			throw ex;
		}
	}

	@Override
	public void handleAfterDelete(AfterDeleteOccurrenceContext context) throws OperationException
	{
		Session session = context.getSession();
		String tablePathStr = context.getTable().getTablePath().format();
		try
		{
			if (session.getInteraction(true) != null)
			{
				Adaptation subjectRecord = getSubjectRecordFromSessionAttribute(context);
				if (subjectRecord != null)
				{
					SubjectPathConfig subjectPathConfig = getSubjectPathConfig();
					Adaptation projectRecord = AdaptationUtil.followFK(
						context.getOccurrenceContext(),
						subjectPathConfig.getProjectSubjectProjectFieldPath());
					setCurrentProjectType(
						null,
						context.getProcedureContext(),
						session,
						projectRecord,
						subjectRecord);
				}
			}
			super.handleAfterDelete(context);
		}
		finally
		{
			session.setAttribute(DELETED_PROJECT_SUBJECT_SUBJECT_PK_PREFIX + tablePathStr, null);
			//			session.setAttribute(DELETED_PROJECT_SUBJECT_PROJECT_TYPE_PREFIX + tablePathStr, null);
		}
	}

	protected void setCurrentProjectType(
		String currentProjectType,
		ProcedureContext pContext,
		Session session,
		Adaptation projectRecord,
		Adaptation subjectRecord)
		throws OperationException
	{
		ProjectUtil.setCurrentProjectType(
			currentProjectType,
			subjectRecord,
			pContext,
			session,
			getProjectPathConfig(),
			getSubjectPathConfig());
	}

	protected UserMessage checkCurrentProjectType(
		Session session,
		Adaptation projectRecord,
		Adaptation subjectRecord)
	{
		// TODO: This is a workaround for the fact that EBX allows link table
		// records to be created even when
		// their fk filter is violated. Once that is addressed in EBX, this can be
		// removed.
		String currentProjectType = ProjectUtil
			.getCurrentProjectType(subjectRecord, getMasterDataSpaceName(), getSubjectPathConfig());
		if (currentProjectType != null)
		{
			Locale locale = session.getLocale();
			return UserMessage.createError(
				"Association to "
					+ projectRecord.getContainerTable().getTableNode().getLabel(locale)
					+ " can't be created. "
					+ subjectRecord.getContainerTable().getTableNode().getLabel(locale)
					+ " is associated with another project.");
		}
		return null;
	}

	private Adaptation getSubjectRecordFromSessionAttribute(TableTriggerExecutionContext context)
	{
		Session session = context.getSession();
		String tablePathStr = context.getTable().getTablePath().format();
		String subjectPK = (String) session
			.getAttribute(DELETED_PROJECT_SUBJECT_SUBJECT_PK_PREFIX + tablePathStr);

		AdaptationTable subjectTable = context.getTable().getContainerAdaptation().getTable(
			getSubjectPathConfig().getSubjectTablePath());
		return subjectTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(subjectPK));
	}

	protected class ProjectSubjectTriggerActionValidator implements TriggerActionValidator
	{
		@Override
		public UserMessage validateTriggerAction(
			Session session,
			ValueContext valueContext,
			ValueChanges valueChanges,
			TriggerAction action)
			throws OperationException
		{
			ProjectPathConfig projectPathConfig = getProjectPathConfig();
			SubjectPathConfig subjectPathConfig = getSubjectPathConfig();

			Adaptation projectRecord = AdaptationUtil
				.followFK(valueContext, subjectPathConfig.getProjectSubjectProjectFieldPath());
			if (projectRecord != null)
			{
				if (action == TriggerAction.CREATE)
				{
					// If not deep copying, check the currentProjectType
					Boolean deepCopy = (Boolean) session
						.getAttribute(DeepCopyImpl.DEEP_COPY_SESSION_ATTRIBUTE);
					if (!Boolean.TRUE.equals(deepCopy))
					{
						Adaptation subjectRecord = AdaptationUtil.followFK(
							valueContext,
							subjectPathConfig.getProjectSubjectSubjectFieldPath());
						UserMessage msg = checkCurrentProjectType(
							session,
							projectRecord,
							subjectRecord);
						if (msg != null)
						{
							return msg;
						}
					}
				}
				else if (action == TriggerAction.DELETE)
				{
					if (checkIfNewSubjectProjectType(session))
					{
						String projectType = projectRecord
							.getString(projectPathConfig.getProjectProjectTypeFieldPath());
						if (!projectPathConfig.isNewSubjectProjectType(projectType))
						{
							return UserMessage.createError(
								"Record cannot be removed from a " + projectType
									+ " project once it has been added. You must cancel the project and relaunch it.");
						}
					}
				}
			}
			return null;
		}
	}
}
