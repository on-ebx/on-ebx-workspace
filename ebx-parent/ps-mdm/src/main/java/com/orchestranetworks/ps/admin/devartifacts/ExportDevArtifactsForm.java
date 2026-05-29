package com.orchestranetworks.ps.admin.devartifacts;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

/**
 * Presents a form to the user that allows them to configure the export before launching it
 * 
 * @deprecated Use the user service instead. See {@link com.orchestranetworks.ps.admin.devartifacts.service}.
 */
@Deprecated
public class ExportDevArtifactsForm extends DevArtifactsForm
{
	private static final String DEFAULT_SERVLET_PATH = "/ExportDevArtifacts";

	private String servletPath = DEFAULT_SERVLET_PATH;

	@Override
	public String getServletPath()
	{
		return servletPath;
	}

	public void setServletPath(String servletPath)
	{
		this.servletPath = servletPath;
	}

	protected void writeInformationPanel(ServiceContext sContext)
	{
		UIComponentWriter writer = sContext.getUIComponentWriter();
		writer.add_cr("<div style='margin: 5px'>");
		writer.add_cr(
			"  <p>Artifacts specified in properties file will be exported, as well as all workflows.</p>");
		writer.add_cr("</div>");
	}
}
