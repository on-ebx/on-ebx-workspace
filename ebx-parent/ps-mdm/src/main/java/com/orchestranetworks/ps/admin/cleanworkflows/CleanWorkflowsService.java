/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.workflow.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * A service that terminates open workflows and closes child data spaces.
 * 
 * @deprecated Use {@link CleanWorkflowsUserService} instead
 */
@Deprecated
public class CleanWorkflowsService extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private CleanWorkflowsConfigFactory configFactory;

	public CleanWorkflowsService()
	{
		this(
			CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FOLDER,
			CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FILE);
	}

	public CleanWorkflowsService(String propertiesFile)
	{
		this(CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FOLDER, propertiesFile);
	}

	public CleanWorkflowsService(String propertiesFolder, String propertiesFile)
	{
		this(new DefaultCleanWorkflowsConfigFactory(propertiesFolder, propertiesFile));
	}

	public CleanWorkflowsService(CleanWorkflowsConfigFactory configFactory)
	{
		this.configFactory = configFactory;
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);
		Repository repo;
		Session session;
		// This will only be null when called from outside of EBX
		if (sContext == null)
		{
			repo = Repository.getDefault();
			session = repo.createSessionFromLoginPassword(
				request.getParameter("login"),
				request.getParameter("password"));
		}
		else
		{
			repo = sContext.getCurrentHome().getRepository();
			session = sContext.getSession();
		}

		// Make sure only admins can execute
		if (!session.isUserInRole(Role.ADMINISTRATOR))
		{
			throw new ServletException("User doesn't have permission to execute service.");
		}

		try
		{
			CleanWorkflowsConfig config = configFactory.createConfig(repo, session);

			cleanWorkflows(repo, session, config);
			cleanDataSpaces(repo, session, config);
		}
		catch (OperationException ex)
		{
			throw new ServletException(ex);
		}

		// When called from outside EBX simply print this on the response
		if (sContext == null)
		{
			response.getWriter().print("Service complete.");
			response.getWriter().flush();
		}
		else
		{
			UIComponentWriter writer = sContext.getUIComponentWriter();
			writer.addJS("alert('Service complete.');");
			writer.addJS("window.location.href='" + sContext.getURLForEndingService() + "';");
		}
	}

	protected void cleanWorkflows(Repository repo, Session session, CleanWorkflowsConfig config)
		throws OperationException
	{
		WorkflowEngine wfEngine = WorkflowEngine.getFromRepository(repo, session);
		for (PublishedProcess workflowPublication : config.getWorkflowPublications())
		{
			List<ProcessInstanceKey> processInstanceKeys = wfEngine
				.getProcessInstanceKeys(workflowPublication.getPublishedProcessKey());
			for (ProcessInstanceKey processInstanceKey : processInstanceKeys)
			{
				wfEngine.terminateProcessInstance(processInstanceKey);
			}
		}
	}

	protected void cleanDataSpaces(Repository repo, Session session, CleanWorkflowsConfig config)
		throws OperationException
	{
		List<AdaptationHome> masterDataSpaces = config.getMasterDataSpaces();
		for (AdaptationHome masterDataSpace : masterDataSpaces)
		{
			List<AdaptationHome> snapshots = masterDataSpace.getVersionChildren();
			for (AdaptationHome snapshot : snapshots)
			{
				if (snapshot.isInitialVersion())
				{
					List<AdaptationHome> dataSpaces = snapshot.getBranchChildren();
					for (AdaptationHome dataSpace : dataSpaces)
					{
						if (dataSpace.isOpen()
							&& !config.getChildDataSpacesToSkip().contains(dataSpace))
						{
							repo.closeHome(dataSpace, session);
						}
					}
				}
			}
		}
	}
}
