package com.orchestranetworks.ps.admin.devartifacts;

import java.io.*;
import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * A servlet for processing of the dev artifacts.
 * This is no longer used by the User Service but it still needed for the command line interface.
 * 
 * @deprecated Use the REST service or user service instead. See {@link com.orchestranetworks.ps.admin.devartifacts.rest.AbstractDevArtifactsRESTService}
 *             or {@link com.orchestranetworks.ps.admin.devartifacts.service.DevArtifactsUserService}.
 */
@Deprecated
public abstract class DevArtifactsService extends HttpServlet implements DevArtifactsConstants
{
	private static final long serialVersionUID = 1L;

	private DevArtifactsBase impl;

	protected abstract DevArtifactsBase createImpl();

	protected abstract DevArtifactsConfigFactory createConfigFactory();

	/**
	 * Execute the servlet
	 */
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response)
		throws ServletException, IOException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);
		Repository repo;
		Session session = null;
		// This will only be null when called from outside of EBX
		if (sContext == null)
		{
			repo = Repository.getDefault();
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(request.getInputStream()));
			String line;
			try
			{
				line = reader.readLine();
			}
			finally
			{
				reader.close();
			}
			String login = null;
			if (line != null)
			{
				int sepInd = line.indexOf(' ');
				login = line.substring(0, sepInd);
				String password = line.substring(sepInd + 1);

				session = repo.createSessionFromLoginPassword(login, password);
			}
			if (session == null)
			{
				String msg = "Failed to login user.";
				DevArtifactsUtil.getLog().error(msg);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
				return;
			}
		}
		else
		{
			repo = sContext.getCurrentHome().getRepository();
			session = sContext.getSession();
		}

		this.impl = this.createImpl();
		// Make sure only admins can execute
		if (!session.isUserInRole(Role.ADMINISTRATOR))
		{
			String msg = "User doesn't have permission to execute service.";
			DevArtifactsUtil.getLog().error(msg);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
			return;
		}

		try
		{
			DevArtifactsConfigFactory configFactory = createConfigFactory();
			Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
			if (!paramMap.containsKey(DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE))
			{
				String[] propertiesFileSystemPropertyValues = paramMap
					.get(DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE_SYSTEM_PROPERTY);
				if (propertiesFileSystemPropertyValues == null
					|| propertiesFileSystemPropertyValues.length == 0
					|| "".equals(propertiesFileSystemPropertyValues[0]))
				{
					propertiesFileSystemPropertyValues = new String[] {
							DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE_SYSTEM_PROPERTY };
				}
				DevArtifactsPropertyFileHelper.updateParametersWithPropertiesFile(
					paramMap,
					propertiesFileSystemPropertyValues[0]);
			}
			DevArtifactsConfig config = configFactory.createConfig(repo, session, paramMap);
			configFactory.updateConfig(config, repo, session, paramMap);
			impl.setConfig(config);

			if (config.isEnvironmentCopy())
			{
				this.impl.processArtifacts(repo, session, true);
			}
			// Note that for imports, this will do nothing but for exports, we want to additionally export the normal artifacts
			this.impl.processArtifacts(repo, session, false);

			this.impl.postProcess(repo, session);
		}
		catch (OperationException ex)
		{
			String msg = "Error processing artifacts.";
			DevArtifactsUtil.getLog().error(msg, ex);
			response.sendError(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				msg + ": " + ex.getMessage());
			return;
		}

		// When called from outside EBX simply print this on the response
		if (sContext == null)
		{
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(SERVICE_COMPLETE_MSG);
			response.getWriter().flush();
		}
		else
		{
			UIComponentWriter writer = sContext.getUIComponentWriter();
			writer.addJS("alert('" + SERVICE_COMPLETE_MSG + "');");
			writer.addJS("window.location.href='" + sContext.getURLForEndingService() + "';");
		}
	}
}
