/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.io.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 */
@Deprecated
public class DeepCopyService extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private final DeepCopyImpl impl;

	public DeepCopyService(DeepCopyConfigFactory configFactory)
	{
		this(configFactory, false);
	}

	public DeepCopyService(DeepCopyConfigFactory configFactory, boolean openNewRecord)
	{
		this.impl = createImpl(configFactory, openNewRecord);
	}

	protected DeepCopyImpl createImpl(DeepCopyConfigFactory configFactory, boolean openNewRecord)
	{
		return new DeepCopyImpl(configFactory, openNewRecord);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(req);
		Session session = sContext.getSession();
		Adaptation record = getRecordToCopy(req);
		try
		{
			impl.execute(session, record, createDataModifier());
		}
		catch (OperationException e)
		{
			throw new ServletException(e);
		}

		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		String url = getRedirectURL(sContext, impl.getTargetRecord());
		writer.addJS_cr("window.location.href='" + url + "';");
	}

	protected DeepCopyDataModifier createDataModifier()
	{
		return new DefaultDeepCopyDataModifier();
	}

	// Default Behavior it to copy the selected record
	protected Adaptation getRecordToCopy(HttpServletRequest req)
	{
		ServiceContext sContext = ServiceContext.getServiceContext(req);
		return sContext.getCurrentAdaptation();
	}

	protected String getRedirectURL(ServiceContext sContext, Adaptation createdRecord)
	{
		// TODO: Doesn't work when called from association. Need feedback from engineering.
		if (impl.isOpenNewRecord())
			return sContext.getURLForSelection(createdRecord);
		return sContext.getURLForEndingService();
	}

}
