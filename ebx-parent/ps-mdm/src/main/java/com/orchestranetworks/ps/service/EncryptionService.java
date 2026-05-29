package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.security.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class EncryptionService<S extends DataspaceEntitySelection> extends AbstractUserService<S>
{
	private WizardStep step = new WizardStepMain(this);
	private String stringToEncrypt;

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<S> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		this.step.setupDisplay(aContext, aConfigurator);
	}

	@Override
	protected void writeInputPane(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter)
	{
		step.writeInputPane(aPaneContext, aWriter);
	}

	@Override
	public UserServiceEventOutcome processEventOutcome(
		UserServiceProcessEventOutcomeContext<S> aContext,
		UserServiceEventOutcome anEventOutcome)
	{
		if (anEventOutcome instanceof CustomOutcome)
		{
			CustomOutcome action = (CustomOutcome) anEventOutcome;
			switch (action)
			{
			case displayMainStep:
				this.step = new WizardStepMain(this);
				break;
			case displayEndStep:
				this.step = new WizardStepEnd(this);
				break;

			}
			return null;
		}

		return anEventOutcome;

	}

	public enum CustomOutcome implements UserServiceEventOutcome {
		displayMainStep, displayEndStep
	}

	public interface WizardStep
	{
		void writeInputPane(UserServicePaneContext aPaneContext, UserServicePaneWriter aWriter);

		void setupDisplay(
			UserServiceSetupDisplayContext<?> aContext,
			UserServiceDisplayConfigurator aConfigurator);

	}

	public class WizardStepMain implements WizardStep
	{

		private EncryptionService<?> parent;

		private WizardStepMain(EncryptionService<?> parent)
		{
			this.parent = parent;
		}

		@Override
		public void setupDisplay(
			UserServiceSetupDisplayContext<?> aContext,
			UserServiceDisplayConfigurator aConfigurator)
		{
			aConfigurator.setContent(this::writeInputPane);
		}

		@Override
		public void writeInputPane(
			UserServicePaneContext aPaneContext,
			UserServicePaneWriter aWriter)
		{
			aWriter.add("<form id=\"passwordEncryption\">");

			aWriter.add("<table id=\"logFileNames\" border=\"0\">");

			aWriter.add("<tbody>");

			aWriter.add("<tr>");
			aWriter.add("<td>");

			aWriter.add("<label for=\"name\">Value to be Encrypted:</label>");
			aWriter.add("</td>");
			aWriter.add("<td>");
			aWriter.add(
				"<input type=\"text\" id=\"stringToEncrypt\" name=\"stringToEncrypt\" required minlength=\"4\" maxlength=\"36\" size=\"36\">");
			aWriter.add("</td>");
			aWriter.add("<tr>");
			aWriter.add("<td>");
			aWriter.add("</td>");
			aWriter.add("<td>");
			aWriter.addButton(
				aWriter.newSubmitButton("Process String Encryption", this::encryptString));
			aWriter.add("</td>");
			aWriter.add("</tr>");
			aWriter.add("</tbody>");
			aWriter.add("</table>");

			aWriter.add("</Form>");

		}

		protected UserServiceEventOutcome encryptString(UserServiceEventContext anEventContext)
		{
			String[] values = anEventContext.getParameterValues("stringToEncrypt");
			for (String password : values)
			{
				parent.stringToEncrypt = password;
				break;
			}
			return CustomOutcome.displayEndStep;
		}
	}

	public class WizardStepEnd implements WizardStep
	{
		private EncryptionService<?> parent;

		private WizardStepEnd(EncryptionService<?> parent)
		{
			this.parent = parent;
		}

		@Override
		public void setupDisplay(
			UserServiceSetupDisplayContext<?> aContext,
			UserServiceDisplayConfigurator aConfigurator)
		{
			aConfigurator.setContent(this::writeInputPane);
		}

		@Override
		public void writeInputPane(
			UserServicePaneContext aPaneContext,
			UserServicePaneWriter aWriter)
		{
			aWriter.add("<div ");
			aWriter.addSafeAttribute("class", UICSSClasses.CONTAINER_WITH_TEXT_PADDING);
			aWriter.add(">");

			String stringToProcess = parent.stringToEncrypt;

			String result = null;
			try
			{
				result = EncryptionTool.encryptReturnBase64(stringToProcess);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			aWriter.add("<a> ");
			aWriter.add("Use this as the encrypted version of your value ==><B>");
			aWriter.add("<span style=\"color: #ff0000\">");
			aWriter.add(result);
			aWriter.add("</span>");
			aWriter.add("</B><==");
			aWriter.add("</a> ");

			aWriter.add("</div>");

		}
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		// TODO Auto-generated method stub

	}

}
