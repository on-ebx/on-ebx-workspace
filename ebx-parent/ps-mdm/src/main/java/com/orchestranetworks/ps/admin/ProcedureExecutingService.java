/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin;

import java.io.*;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * A general service that will execute a procedure.  Construct with the procedure to execute.
 * It also allows you to execute the procedure in a new child data space which merges upon
 * completion of the procedure. In this case, you can also supply the label prefix for the
 * child data space (which will be followed by "at [timestamp]" and a data space name
 * to use for a permissions template.
 */
@Deprecated
public class ProcedureExecutingService extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private Procedure procedure;
	private boolean useChildDataSpace;
	private String childDataSpaceLabelPrefix;
	private String permissionsTemplateDataSpaceName;
	private boolean allowNonAdministrators;

	public ProcedureExecutingService(Procedure procedure)
	{
		this(procedure, false, null, null);
	}

	public ProcedureExecutingService(
		Procedure procedure,
		boolean useChildDataSpace,
		String childDataSpaceLabelPrefix,
		String permissionsTemplateDataSpaceName)
	{
		this.procedure = procedure;
		this.useChildDataSpace = useChildDataSpace;
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);
		Session session = sContext.getSession();

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
				throw new ServletException("User doesn't have permission to execute service.");
			}
		}

		try
		{
			if (useChildDataSpace)
			{
				ProcedureExecutor.executeProcedureInChild(
					procedure,
					session,
					sContext.getCurrentHome(),
					childDataSpaceLabelPrefix,
					permissionsTemplateDataSpaceName);
			}
			else
			{
				ProcedureExecutor.executeProcedure(procedure, session, sContext.getCurrentHome());
			}
		}
		catch (OperationException ex)
		{
			throw new ServletException(ex);
		}

		UIComponentWriter writer = sContext.getUIComponentWriter();
		writer.addJS("alert('Service complete.');");
		writer.addJS("window.location.href='" + sContext.getURLForEndingService() + "';");
	}

	public boolean isUseChildDataSpace()
	{
		return this.useChildDataSpace;
	}

	public void setUseChildDataSpace(boolean useChildDataSpace)
	{
		this.useChildDataSpace = useChildDataSpace;
	}

	public String getChildDataSpaceLabelPrefix()
	{
		return this.childDataSpaceLabelPrefix;
	}

	public void setChildDataSpaceLabelPrefix(String childDataSpaceLabelPrefix)
	{
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
	}

	public String getPermissionsTemplateDataSpaceName()
	{
		return this.permissionsTemplateDataSpaceName;
	}

	public void setPermissionsTemplateDataSpaceName(String permissionsTemplateDataSpaceName)
	{
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}

	public boolean isAllowNonAdministrators()
	{
		return this.allowNonAdministrators;
	}

	public void setAllowNonAdministrators(boolean allowNonAdministrators)
	{
		this.allowNonAdministrators = allowNonAdministrators;
	}
}
