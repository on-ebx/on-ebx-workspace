/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * A general service that will execute a procedure.  Construct with the procedure to execute.
 * It also allows you to execute the procedure in a new child data space which merges upon
 * completion of the procedure. In this case, you can also supply the label prefix for the
 * child data space (which will be followed by "at [timestamp]" and a data space name
 * to use for a permissions template.
 */
public class ProcedureExecutingUserService extends AbstractUserService<DataspaceEntitySelection>
{
	private final Procedure procedure;
	private final boolean useChildDataSpace;
	private final String childDataSpaceLabelPrefix;
	private final String permissionsTemplateDataSpaceName;
	private final boolean allowNonAdministrators;

	public ProcedureExecutingUserService(Procedure procedure)
	{
		this(procedure, false, null, null, false);
	}

	public ProcedureExecutingUserService(
		Procedure procedure,
		boolean useChildDataSpace,
		String childDataSpaceLabelPrefix,
		String permissionsTemplateDataSpaceName,
		boolean allowNonAdministrators)
	{
		this.procedure = procedure;
		this.useChildDataSpace = useChildDataSpace;
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
		this.allowNonAdministrators = allowNonAdministrators;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		if (!allowNonAdministrators)
		{
			// Make sure only admins can execute. Servlets are exposed for anyone to call if they
			// know the URL,
			// and we don't want any EBX user to execute it. If this isn't sufficient, we'll need to
			// come up with a more elaborate
			// way to secure them.
			if (!(session.isUserInRole(Role.ADMINISTRATOR)
				|| session.isUserInRole(CommonConstants.TECH_ADMIN)))
			{
				throw OperationException
					.createError("User doesn't have permission to execute service.");
			}
		}

		if (useChildDataSpace)
		{
			ProcedureExecutor.executeProcedureInChild(
				procedure,
				session,
				context.getEntitySelection().getDataspace(),
				childDataSpaceLabelPrefix,
				permissionsTemplateDataSpaceName);
		}
		else
		{
			ProcedureExecutor
				.executeProcedure(procedure, session, context.getEntitySelection().getDataspace());
		}
	}
}
