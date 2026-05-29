package com.orchestranetworks.ps.admin.devartifacts;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.http.*;

/**
 * An abstract servlet class for presenting a form to the user, allowing them to configure the import/export
 * before launching it
 * 
 * @deprecated Use the user service instead. See {@link com.orchestranetworks.ps.admin.devartifacts.service}.
 */
@Deprecated
public abstract class DevArtifactsForm
{
	private String propertiesFile = DevArtifactsPropertyFileHelper.DEFAULT_PROPERTIES_FILE;

	/**
	 * The path to the servlet to invoke
	 *
	 * @return the servlet path
	 */
	protected abstract String getServletPath();

	/**
	 * Execute the servlet, which will display the form
	 *
	 * @param request the servlet request
	 * @throws OperationException if an exception occurs
	 */
	public void execute(HttpServletRequest request) throws OperationException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);

		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		UIFormSpec formSpec = createFormSpec(sContext);
		writer.startForm(formSpec);
		writeForm(sContext);
		writer.endForm();

		writeFormButtonsJS(sContext);
	}

	public String getPropertiesFile()
	{
		return this.propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}

	/**
	 * This allows subclasses to define an information panel at the top of the form.
	 * Default does nothing.
	 *
	 * @param sContext the service context
	 */
	protected void writeInformationPanel(ServiceContext sContext)
	{
		// default impl does nothing
	}

	/**
	 * This allows subclasses to define an panel with extra input fields.
	 * Default does nothing.
	 *
	 * @param sContext the service context
	 * @throws OperationException if an exception occurs
	 */
	protected void writeExtraInputs(ServiceContext sContext) throws OperationException
	{
		// default impl does nothing
	}

	/**
	 * Write the form
	 *
	 * @param sContext the service context
	 * @throws OperationException if an exception occurs
	 */
	protected void writeForm(ServiceContext sContext) throws OperationException
	{
		writeInformationPanel(sContext);
		writeHiddenInputs(sContext);
		writeEnvironmentCopyPanel(sContext);
		writeExtraInputs(sContext);
	}

	/**
	 * Create the form spec used by the form
	 *
	 * @param sContext the service context
	 * @return the form spec
	 */
	protected UIFormSpec createFormSpec(ServiceContext sContext)
	{
		UIFormSpec formSpec = new UIFormSpec();
		String servletURL = sContext.getURLForIncludingResource(getServletPath());
		formSpec.setURLForAction(servletURL);
		UIButtonSpec cancelButtonSpec = new UIButtonSpecJSAction(
			UserMessage.createInfo("Cancel"),
			"cancelForm();");
		formSpec.addActionBackInBottomBar(cancelButtonSpec);
		UIButtonSpec submitButtonSpec = new UIButtonSpecSubmit(
			UserMessage.createInfo("Submit"),
			"submit",
			"submit");
		formSpec.addActionForwardInBottomBar(submitButtonSpec);
		return formSpec;
	}

	/**
	 * Write hidden input fields needed by the form
	 * @param sContext the service context
	 */
	protected void writeHiddenInputs(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		if (propertiesFile != null)
		{
			writer.add_cr(
				"<input type='hidden' id='" + DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE
					+ "' name='" + DevArtifactsPropertyFileHelper.PARAM_PROPERTIES_FILE
					+ "' value='" + propertiesFile + "'/>");
		}
	}

	/**
	 * Write the environment copy panel
	 *
	 * @param sContext the service context
	 */
	protected void writeEnvironmentCopyPanel(ServiceContext sContext)
	{
		writeCheckboxRow(
			sContext,
			"Environment Copy",
			DevArtifactsBase.PARAM_ENVIRONMENT_COPY,
			false,
			null);
	}

	/**
	 * Write the javascript associated with the form buttons
	 *
	 * @param sContext the service context
	 */
	protected void writeFormButtonsJS(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		// On cancel, just redirect the screen
		writer.addJS_cr("function cancelForm()");
		writer.addJS_cr("{");
		writer.addJS_cr("  window.location.href='" + sContext.getURLForEndingService() + "';");
		writer.addJS_cr("}");
	}

	/**
	 * Write a form row for a checkbox
	 *
	 * @param sContext the service context
	 * @param label the label for the checkbox
	 * @param onclick the javascript to call upon clicking of the checkbox
	 */
	protected void writeCheckboxRow(
		ServiceContext sContext,
		String label,
		String id,
		boolean defaultValue,
		String onclick)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec(label));
		writer.add("<input type='checkbox' id='");
		writer.add(id);
		writer.add("' name='");
		writer.add(id);
		writer.add("' value='true'");
		if (onclick != null)
		{
			writer.add(" onclick='");
			writer.add(onclick);
			writer.add("'");
		}
		if (defaultValue)
		{
			writer.add(" checked='true'");
		}
		writer.add_cr("/>");
		writer.endFormRow();
	}

	/**
	 * Write a separator on the screen
	 *
	 * @param sContext the service context
	 */
	protected void writeSeparator(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec(""));
		writer.endFormRow();
	}

	/**
	 * A class that encapsulates information about a checkbox
	 */
	protected class CheckboxConfig
	{
		private String label;
		private String componentName;

		public CheckboxConfig(String label, String componentName)
		{
			this.label = label;
			this.componentName = componentName;
		}

		public String getLabel()
		{
			return this.label;
		}

		public void setLabel(String label)
		{
			this.label = label;
		}

		public String getComponentName()
		{
			return this.componentName;
		}

		public void setComponentName(String componentName)
		{
			this.componentName = componentName;
		}
	}
}
