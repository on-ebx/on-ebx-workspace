package com.orchestranetworks.ps.service;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.messaging.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * @author David Dahan
 * This class is provided as an example only.
 * It is a UI service that allow a user to select rows in any table and publish the selected rows as a json messages in proprietary format.
 *
 */
public class PublishSelectedRecordsService extends AbstractUserService<TableViewEntitySelection>
{

	@Override
	public void execute(Session session) throws OperationException
	{
		Request selectedRecords = context.getEntitySelection().getSelectedRecords();
		RequestResult result = selectedRecords.execute();
		try
		{
			Adaptation adaptation = result.nextAdaptation();
			while (adaptation != null)
			{
				//AdaptationToJAXB converter = new AdaptationToJAXB("com.test.beans");
				//Object jaxbStr = converter.generateJAXB(adaptation);
				// publish the adaptation to the configured destination
				MessagingClient messageClient = MessagingConfiguration.getMessagingClient();
				OutboundMessage message = new OutboundMessage();
				message.setDeliveryTime(System.currentTimeMillis());
				message.setAdaptation(adaptation);
				messageClient.publishMessage(null, message);
				adaptation = result.nextAdaptation();
			}
		}
		catch (Exception e)
		{
			throw OperationException.createError("Error publishing message.", e);
		}
		finally
		{
			result.close();
		}
	}
}
