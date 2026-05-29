package com.orchestranetworks.ps.messaging;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.misc.*;
import com.orchestranetworks.service.*;

public class MessagingClient
{

	private MessagingConfiguration configuration;
	private static MessagingClientInterface client = null;

	protected MessagingClient(MessagingConfiguration configuration) throws IOException
	{
		this.configuration = configuration;
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#sendMessage(java.lang.String, com.orchestranetworks.ps.messaging.OutboundMessage)
	 */
	public void sendMessage(String destinationName, OutboundMessage message) throws Exception
	{
		MessageOutboundTransformation transformer = new MessageOutboundTransformationImpl();
		submitMessage(destinationName, message, transformer, false);
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#sendMessage(java.lang.String, com.orchestranetworks.ps.messaging.OutboundMessage, com.orchestranetworks.ps.messaging.MessageOutboundTransformation)
	 */
	public void sendMessage(
		String destinationName,
		OutboundMessage message,
		MessageOutboundTransformation transformer)
		throws Exception
	{
		submitMessage(destinationName, message, transformer, false);
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#publishMessage(java.lang.String, com.orchestranetworks.ps.messaging.OutboundMessage)
	 */
	public void publishMessage(String destinationName, OutboundMessage message) throws Exception
	{
		MessageOutboundTransformation transformer = new MessageOutboundTransformationImpl();
		submitMessage(destinationName, message, transformer, true);
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#publishMessage(java.lang.String, com.orchestranetworks.ps.messaging.OutboundMessage, com.orchestranetworks.ps.messaging.MessageOutboundTransformation)
	 */
	public void publishMessage(
		String destinationName,
		OutboundMessage message,
		MessageOutboundTransformation transformer)
		throws Exception
	{
		submitMessage(destinationName, message, transformer, true);
	}

	public List<Map<String, Object>> subscribe(String destinationName, int timeout)
	{
		MessageInboundTransformation transformer = new MessageInboundTransformationImpl();
		return subscribe(destinationName, timeout, transformer);
	}

	public List<Map<String, Object>> subscribe(
		String destinationName,
		int timeout,
		MessageInboundTransformation transformer)
	{
		return recieveMessages(destinationName, timeout, transformer, true);
	}

	public List<Map<String, Object>> readMessages(String destinationName, int timeout)
	{
		MessageInboundTransformation transformer = new MessageInboundTransformationImpl();
		return readMessages(destinationName, timeout, transformer);
	}

	public List<Map<String, Object>> readMessages(
		String destinationName,
		int timeout,
		MessageInboundTransformation transformer)
	{
		return recieveMessages(destinationName, timeout, transformer, false);
	}

	private List<Map<String, Object>> recieveMessages(
		String destinationName,
		int timeout,
		MessageInboundTransformation transformer,
		boolean isTopic)
	{
		if (StringUtils.isEmpty(destinationName))
		{
			destinationName = getConfiguration().getOutboundDestination();
			if (StringUtils.isEmpty(destinationName))
			{
				throw new RuntimeException("Inbound destination is required.");
			}
		}

		List<InboundMessage> messages = null;
		if (isTopic)
		{
			messages = client.subscribe(destinationName, timeout);
		}
		else
		{
			messages = client.readMessages(destinationName, timeout);
		}

		List<Map<String, Object>> result = new ArrayList<>();
		if (messages != null)
		{

			TransformationContext context = new TransformationContext();
			for (InboundMessage message : messages)
			{

				Map<String, Object> valueMap = transformer
					.transformToAdaptation(context, message.getBody());
				result.add(valueMap);
			}
		}
		return result;

	}

	private void submitMessage(
		String destinationName,
		OutboundMessage message,
		MessageOutboundTransformation transformer,
		boolean isTopic)
		throws Exception
	{
		if (message == null)
		{
			LoggingCategory.getKernel().info("[MQ Client] Message can't be null");
			return;
		}
		Exception lastException = null;

		int numberOfRetries = 1;
		long retryDelay = 1;
		numberOfRetries = configuration.getNumberOfRetries();

		retryDelay = configuration.getDelayBetweenRetries();

		if (StringUtils.isEmpty(destinationName))
		{
			destinationName = configuration.getOutboundDestination();
			if (StringUtils.isEmpty(destinationName))
			{
				String errorMessage = new StringBuilder()
					.append("No outbound destination name was specified. ")
					.append(MessagingConstants.MESSAGING_MQ_OUTBOUND_QUEUE_NAME_PROPERTY)
					.append(" need to be set with a valid destination name.")
					.toString();
				LoggingCategory.getKernel().warn(errorMessage);
			}
		}

		if (numberOfRetries == 0)
		{
			numberOfRetries = 1;
		}

		for (int i = 0; i < numberOfRetries; i++)
		{
			lastException = null;
			try
			{
				Adaptation messageBody = message.getAdaptation();
				if (messageBody != null) //This only works if there is a messageBody
				{
					String outbuondMessage = null;
					if (transformer != null)
					{
						if (configuration.usePrettyPrint())
						{
							transformer.setPrettyPrint(true);
						}
						outbuondMessage = transformer.transformFromAdaptation(null, messageBody);
						message.setBody(outbuondMessage);
					}
				}
				//will work below as body is already part of message	
				if (isTopic)
				{

					client.publishMessage(destinationName, message);
				}
				else
				{
					client.sendMessage(destinationName, message);
				}
				break;
			}
			catch (Exception ioe)
			{
				if (LoggingCategory.getKernel().isInfo())
					LoggingCategory.getKernel()
						.error("[MQ Client] Connection error. attempt: " + (i + 1), ioe);
				lastException = ioe;
				client.closeConnection();
				Thread.sleep(retryDelay);
			}
		}

		if (lastException == null)
		{
			LoggingCategory.getKernel().info("[MQ Client] Message Sent :'" + message + "'");
		}
		else
		{
			throw lastException;
		}

	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#closeConnection()
	 */
	public void closeConnection() throws Exception
	{
		client.closeConnection();
	}
	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#resetConnection()
	 */
	public void resetConnection() throws Exception
	{
		client.resetConnection();
	}
	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#createConnection()
	 */
	public void createConnection() throws Exception
	{
		client.createConnection();
	}

	//	/* (non-Javadoc)
	//	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#subscribe(java.lang.String, int)
	//	 */
	//	public List<InboundMessage> subscribe(String topicName, int timeout) {
	//		client.sub
	//	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#browseQueueMessages(java.lang.String)
	 */
	public List<InboundMessage> browseQueueMessages(String queueName)
	{
		return client.browseQueueMessages(queueName);
	}
	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#browseTopicMessages(java.lang.String)
	 */
	public List<InboundMessage> browseTopicMessages(String topicName)
	{
		return client.browseTopicMessages(topicName);
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#getListOfQueues()
	 */
	public List<String> getListOfQueues()
	{
		return client.getListOfQueues();
	}
	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#getListOfTopics()
	 */
	public List<String> getListOfTopics()
	{
		return client.getListOfTopics();
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#shutdown()
	 */
	public void shutdown()
	{
		if (getConfiguration().toReuseConnection())
		{
			client.shutdown();
		}
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#getConfiguration()
	 */
	public MessagingConfiguration getConfiguration()
	{
		return configuration;
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.messaging.MessagingCLientInterface#setConfiguration(com.orchestranetworks.ps.messaging.PublishSubscribeConfiguration)
	 */
	public void setConfiguration(MessagingConfiguration configuration)
	{
		this.configuration = configuration;
	}

	public void setClient(MessagingClientInterface messagingClient)
	{
		client = messagingClient;
	}

	public MessagingClientInterface getClient()
	{
		return client;
	}

}
