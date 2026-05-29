package com.orchestranetworks.ps.service;

import java.text.*;
import java.util.*;

import com.onwbp.base.misc.*;
import com.orchestranetworks.ps.messaging.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class BrowseMessagesService extends AbstractUserService<DataspaceEntitySelection>
{
	private WizardStep step = new SelectDestination();
	private MessagingClient client;
	private static final String TABLE_STYLE = "";
	private static final String[] TABLE_HEADERS = new String[] { "Message ID", "Delivery Time",
			"Message Text" };

	private boolean isTopic;
	private String topicDestinationName = null;
	private String queueDestinationName = null;
	private String destinationName = null;

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<DataspaceEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
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
		UserServiceProcessEventOutcomeContext<DataspaceEntitySelection> aContext,
		UserServiceEventOutcome anEventOutcome)
	{
		if (anEventOutcome instanceof CustomOutcome)
		{
			CustomOutcome action = (CustomOutcome) anEventOutcome;
			switch (action)
			{
			case selectDestination:
				this.step = new SelectDestination();
				break;
			default:
				break;

			}
			return null;
		}

		return anEventOutcome;
	}

	public enum CustomOutcome implements UserServiceEventOutcome {
		selectDestination
	}

	public interface WizardStep
	{
		public void setupDisplay(
			UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
			UserServiceDisplayConfigurator aConfigurator);

		void writeInputPane(UserServicePaneContext aPaneContext, UserServicePaneWriter aWriter);
	}

	public class SelectDestination implements WizardStep
	{
		@Override
		public void setupDisplay(
			UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
			UserServiceDisplayConfigurator aConfigurator)
		{
			aConfigurator.setContent(this::writeInputPane);
		}

		@Override
		public void writeInputPane(
			UserServicePaneContext aPaneContext,
			UserServicePaneWriter aWriter)
		{
			aWriter.add("<style>" + TABLE_STYLE + "</style>");

			aWriter.add("<form id=\"messageBrowserForm\">");
			aWriter.add("<br>");

			destinationName = StringUtils.isEmpty(queueDestinationName) ? topicDestinationName
				: queueDestinationName;

			isTopic = StringUtils.isEmpty(queueDestinationName) ? true : false;

			aWriter.add("<table id=\"destinationSelection\">");

			aWriter.add("<thead>");

			aWriter.add("<tr>");

			aWriter.add("<th>");

			aWriter.add("Select or type the Topic name");

			aWriter.add("</th>");

			aWriter.add("<th>");

			aWriter.add("Select or type the Queue name");

			aWriter.add("</th>");

			aWriter.add("</tr>");
			aWriter.add("</thead>");

			aWriter.add("<tbody>");
			aWriter.add("<tr>");
			aWriter.add("<td>");

			aWriter.add(
				"<input list=\"topicDestinations\" type=\"text\" name=\"topicDestinationName\"");
			if (isTopic)
			{
				aWriter.add(" value=\"" + destinationName + "\" ");
			}
			aWriter.add(">");
			aWriter.add("</td>");
			aWriter.add("<td>");

			aWriter
				.add("<input list=\"queueDestinations\" type=\"text\" name=\"queueDestinations\"");
			if (isTopic)
			{
				aWriter.add(" value=\"" + destinationName + "\" ");
			}
			aWriter.add(">");

			aWriter.add("</td>");
			aWriter.add("</tr>");
			aWriter.add("</tbody>");

			aWriter.add("<br>");

			try
			{
				List<String> topicDestinations = getClient().getListOfTopics();
				List<String> queueDestinations = getClient().getListOfQueues();

				writeDestinationOptions(aWriter, topicDestinations, "topicDestinations");
				writeDestinationOptions(aWriter, queueDestinations, "queueDestinations");
			}
			catch (Throwable tr)
			{
				aWriter.add("Something went wrong..... please contact your administrator.");
				aWriter.add("</Form>");
				return;
			}

			aWriter.add("<table id=\"messageBrowser\">");
			aWriter.add("<thead>");

			aWriter.add("<tr>");
			for (int i = 0; i < TABLE_HEADERS.length; i++)
			{

				aWriter.add("<th>");

				aWriter.add(TABLE_HEADERS[i]);

				aWriter.add("</th>");

			}

			aWriter.add("</tr>");
			aWriter.add("</thead>");

			if (!StringUtils.isEmpty(destinationName))
			{
				aWriter.add("<tbody>");

				List<InboundMessage> messages = null;
				if (isTopic)
				{
					messages = getClient().browseTopicMessages(destinationName);
				}
				else
				{
					messages = getClient().browseQueueMessages(destinationName);
				}
				if (messages != null)
				{

					for (InboundMessage inboundMessage : messages)
					{
						aWriter.add("<tr>");

						aWriter.add(
							MessageFormat.format(
								"<td>{0}</td> <td>{1}</td> <td>{2}</td>",
								inboundMessage.getMessageId(),
								inboundMessage.getDeliveryTime(),
								inboundMessage.getBody()));

						aWriter.add("</tr>");
					}
				}
				aWriter.add("</tbody>");
			}
			aWriter.add("</table>");
			aWriter.addButton(aWriter.newSubmitButton("Refresh", this::processEvent));
			aWriter.add("</Form>");

		}

		private void writeDestinationOptions(
			UserServicePaneWriter aWriter,
			List<String> destinations,
			String name)
		{
			if (destinations != null)
			{
				aWriter.add("<datalist id=\"" + name + "\">");
				for (String string : destinations)
				{
					aWriter.add("<option value=\"" + string + "\">");
				}
				aWriter.add("</datalist>");

			}

		}

		protected UserServiceEventOutcome processEvent(UserServiceEventContext anEventContext)
		{
			topicDestinationName = anEventContext.getParameter("topicDestinationName");
			queueDestinationName = anEventContext.getParameter("queueDestinationName");

			if (!StringUtils.isEmpty(topicDestinationName))
			{
				isTopic = true;
				destinationName = topicDestinationName;
			}
			else
			{
				isTopic = false;
				destinationName = queueDestinationName;
			}
			return null;
		}

	}

	@Override
	public void execute(Session session) throws OperationException
	{
		// TODO Auto-generated method stub

	}

	public MessagingClient getClient()
	{
		if (client == null)
		{
			client = MessagingConfiguration.getMessagingClient();
		}
		return client;
	}

}
