/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.procedure;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;

/**
 * Executes procedures in a {@link ProgrammaticService}. If the result contains an exception, it throws
 * the exception rather than return the result.
 * 
 * It also has the capability of passing through a temporary tracking info into the service.
 * On completion of the service, the old tracking info will be set back.
 * 
 * You can execute the service inside a temporary child data space as well.
 * 
 * If you are already in a session and have a {@link ProcedureContext}, then you should call the procedure's
 * execute method directly, passing in the procedure context.
 */
public abstract class ProcedureExecutor
{
	/**
	 * Execute the procedure with the given data set or record. This is equivalent to
	 * {@link #executeProcedure(Procedure, Session, AdaptationHome)}, passing in the
	 * data set or record's data space.
	 * 
	 * @param procedure the procedure
	 * @param session the session
	 * @param adaptation the data set or record
	 * @throws OperationException if an exception occurred
	 */
	public static void executeProcedure(
		final Procedure procedure,
		final Session session,
		final Adaptation adaptation)
		throws OperationException
	{
		executeProcedure(procedure, session, adaptation.getHome());
	}

	/**
	 * Execute the procedure with the given data space. This is equivalent to
	 * {@link #executeProcedure(Procedure, Session, AdaptationHome, String)}, passing in
	 * <code>null</code> for the tracking info.
	 * 
	 * @param procedure the procedure
	 * @param session the session
	 * @param dataSpace the data space
	 * @throws OperationException if an exception occurred
	 */
	public static void executeProcedure(
		final Procedure procedure,
		final Session session,
		final AdaptationHome dataSpace)
		throws OperationException
	{
		executeProcedure(procedure, session, dataSpace, null);
	}

	/**
	 * Execute the procedure with the given data space and temporary tracking info.
	 * If not <code>null</code>, the tracking info will be passed through to the service that gets created.
	 * 
	 * @param procedure the procedure
	 * @param session the session
	 * @param dataSpace the data space
	 * @param trackingInfo the tracking info, or <code>null</code>
	 * @throws OperationException if an exception occurred
	 */
	public static void executeProcedure(
		final Procedure procedure,
		final Session session,
		final AdaptationHome dataSpace,
		final String trackingInfo) throws OperationException
	{
		String oldTrackingInfo = session.getTrackingInfo();
		ProgrammaticService svc = ProgrammaticService.createForSession(session, dataSpace);
		if (trackingInfo != null)
		{
			svc.setSessionTrackingInfo(trackingInfo);
		}
		OperationException ex;
		try
		{
			// Execute the procedure and save the exception from the result (or null if there is none)
			ProcedureResult procResult = svc.execute(procedure);
			ex = procResult.getException();
		}
		finally
		{
			if (trackingInfo != null)
			{
				svc.setSessionTrackingInfo(oldTrackingInfo);
			}
		}
		// If there was an exception in the result, then throw it
		if (ex != null)
		{
			throw ex;
		}
	}

	/**
	 * Execute a procedure in a new child data space.
	 * This is equivalent of calling {@link #executeProcedureInChild(Procedure, Session, AdaptationHome, String, String, String)},
	 * passing in <code>null</code> for the tracking info.
	 * 
	 * @param procedure the procedure
	 * @param session the session
	 * @param dataSpace the data space
	 * @param childDataSpaceLabelPrefix a prefix to prepend to the timestamp and set as the label. If <code>null</code>,
	 *        the data space won't be given a label. The name will be the normal timestamp.
	 * @param permissionsTemplateDataSpaceName a data space to use as the permissions template of the child data space,
	 *        or <code>null</code>
	 * @throws OperationException if an error occurs
	 */
	public static void executeProcedureInChild(
		final Procedure procedure,
		final Session session,
		final AdaptationHome dataSpace,
		final String childDataSpaceLabelPrefix,
		final String permissionsTemplateDataSpaceName) throws OperationException
	{
		executeProcedureInChild(
			procedure,
			session,
			dataSpace,
			childDataSpaceLabelPrefix,
			permissionsTemplateDataSpaceName,
			null);
	}

	/**
	 * Execute a procedure in a new child data space and with a temporary tracking info.
	 * If not <code>null</code>, the tracking info will be passed through to the service that gets created.
	 * 
	 * @param procedure the procedure
	 * @param session the session
	 * @param dataSpace the data space
	 * @param childDataSpaceLabelPrefix a prefix to prepend to the timestamp and set as the label. If <code>null</code>,
	 *        the data space won't be given a label. The name will be the normal timestamp.
	 * @param permissionsTemplateDataSpaceName a data space to use as the permissions template of the child data space,
	 *        or <code>null</code>
	 * @param trackingInfo the tracking info, or <code>null</code>
	 * @throws OperationException if an error occurs
	 */
	public static void executeProcedureInChild(
		final Procedure procedure,
		final Session session,
		final AdaptationHome dataSpace,
		final String childDataSpaceLabelPrefix,
		final String permissionsTemplateDataSpaceName,
		final String trackingInfo) throws OperationException
	{
		AdaptationHome childDataSpace = createChildDataSpace(
			session,
			dataSpace,
			childDataSpaceLabelPrefix,
			permissionsTemplateDataSpaceName);
		try
		{
			executeProcedure(procedure, session, childDataSpace, trackingInfo);
		}
		catch (OperationException ex)
		{
			// If an error occurred while executing the procedure, then close the child data space
			// before throwing the exception
			try
			{
				closeChildDataSpace(session, childDataSpace);
			}
			catch (OperationException ex2)
			{
				// If an error occurred while closing the child, we're already throwing an exception,
				// so just log it
				LoggingCategory.getKernel().error(
					"Error closing child data space "
						+ childDataSpace.getLabelOrName(session.getLocale()) + ".",
					ex2);
			}
			throw ex;
		}
		// No error occurred so merge the child data space after the procedure is done
		mergeChildDataSpace(session, dataSpace, childDataSpace);
	}

	private static AdaptationHome createChildDataSpace(
		final Session session,
		final AdaptationHome dataSpace,
		final String childDataSpaceLabelPrefix,
		final String permissionsTemplateDataSpaceName) throws OperationException
	{
		UserReference user = session.getUserReference();

		HomeCreationSpec homeCreationSpec = new HomeCreationSpec();
		// Use the typical timestamp for the new data space name
		SimpleDateFormat dateFormat = new SimpleDateFormat(
			CommonConstants.DATA_SPACE_NAME_DATE_TIME_FORMAT);
		String childDataSpaceDateTimeStr = dateFormat.format(new Date());
		homeCreationSpec.setKey(HomeKey.forBranchName(childDataSpaceDateTimeStr));
		// Set the owner of this temporary data space to the session's user
		homeCreationSpec.setOwner(user);
		homeCreationSpec.setParent(dataSpace);
		if (childDataSpaceLabelPrefix != null)
		{
			homeCreationSpec.setLabel(
				UserMessage.createInfo(childDataSpaceLabelPrefix + childDataSpaceDateTimeStr));
		}

		Repository repo = dataSpace.getRepository();
		if (permissionsTemplateDataSpaceName != null)
		{
			AdaptationHome templateDataSpace = repo
				.lookupHome(HomeKey.forBranchName(permissionsTemplateDataSpaceName));
			// If the template data space that was specified can't be found, then log an error,
			// but we'll continue to create the data space anyway. Since it's a temporary data
			// space anyway, it probably is better to just let it create it than stop the process.
			if (templateDataSpace == null)
			{
				LoggingCategory.getKernel().error(
					"Permissions template data space " + permissionsTemplateDataSpaceName
						+ " not found.");
			}
			else
			{
				homeCreationSpec.setHomeToCopyPermissionsFrom(templateDataSpace);
			}
		}
		return repo.createHome(homeCreationSpec, session);
	}

	private static void closeChildDataSpace(Session session, AdaptationHome childDataSpace)
		throws OperationException
	{
		childDataSpace.getRepository().closeHome(childDataSpace, session);
	}

	private static void mergeChildDataSpace(
		final Session session,
		final AdaptationHome dataSpace,
		final AdaptationHome childDataSpace) throws OperationException
	{
		Procedure mergeProcedure = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				// We could allow passing in of a Merge Spec if we need more fine-grained capabilities.
				// By default merges everything from all data sets
				pContext.doMergeToParent(childDataSpace);
			}
		};
		executeProcedure(mergeProcedure, session, dataSpace);
	}
}
