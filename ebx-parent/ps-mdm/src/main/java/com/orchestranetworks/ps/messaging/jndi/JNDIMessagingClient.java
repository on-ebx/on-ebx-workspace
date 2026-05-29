package com.orchestranetworks.ps.messaging.jndi;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import javax.naming.*;

import com.orchestranetworks.ps.messaging.*;

import jakarta.jms.*;


public class JNDIMessagingClient extends AbstractMessagingClientInterface
{
	private Connection connection = null;
	private InitialContext initContext;

	public JNDIMessagingClient(MessagingConfiguration configuration) throws IOException
	{
		super(configuration);
	}

	@Override
	public void closeConnection()
	{

		if (!getMessagingConfiguration().toReuseConnection())
		{
			forceCloseConnection();
		}
	}

	private void forceCloseConnection()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void resetConnection()
	{
		forceCloseConnection();
		connection = null;
	}

	@Override
	public void createConnection() throws Exception
	{
		InitialContext init = getContext();
		ConnectionFactory connectionFactory = (ConnectionFactory) init
			.lookup(getMessagingConfiguration().getJNDIConnectionFactoryName());
		if (getMessagingConfiguration().getUserName() == null)
		{
			connection = connectionFactory.createConnection();
		}
		else
		{
			connection = connectionFactory.createConnection(
				getMessagingConfiguration().getUserName(),
				getMessagingConfiguration().getPassword());
		}
		connection.start();
	}

	@Override
	public List<InboundMessage> readMessages(String destinationName, int timeout)
	{
		try
		{
			jakarta.jms.Queue destination = (jakarta.jms.Queue) getContext().lookup(destinationName);
			return readInboundMessages(destination, timeout);
		}
		catch (NamingException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<InboundMessage> subscribe(String destinationName, int timeout)
	{
		try
		{
			jakarta.jms.Topic destination = (jakarta.jms.Topic) getContext().lookup(destinationName);
			return readInboundMessages(destination, timeout);
		}
		catch (NamingException e)
		{
			throw new RuntimeException(e);
		}

	}

	private List<InboundMessage> readInboundMessages(Destination destination, int timeout)
	{
		Session session = null;
		try
		{
			createConnection();
			session = connection.createSession();
			List<InboundMessage> recievedMessages = new ArrayList<>();

			MessageConsumer consumer = session.createConsumer(destination);
			jakarta.jms.Message message = null;
			while ((message = consumer.receive(timeout)) != null)
			{
				InboundMessage ebxMessage = createInboundMessage(message);
				recievedMessages.add(ebxMessage);
			}
			return recievedMessages;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (session != null)
			{
				try
				{
					session.close();
				}
				catch (JMSException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void publishMessage(String destinationName, OutboundMessage message) throws Exception
	{
		deliverMessage(destinationName, message, true);
	}

	private InboundMessage createInboundMessage(jakarta.jms.Message message)
	{
		if (message == null)
		{
			return null;
		}
		InboundMessage inboundMessage = new InboundMessage();
		try
		{
			@SuppressWarnings("unchecked")
			Enumeration<String> propertyNames = message.getPropertyNames();
			while (propertyNames.hasMoreElements())
			{
				String key = propertyNames.nextElement();
				String value = message.getStringProperty(key);
				inboundMessage.addHeaderProperty(key, value);
			}
			inboundMessage.setBody(message.getBody(String.class));
		}
		catch (JMSException e)
		{
			throw new RuntimeException(e);
		}
		return inboundMessage;
	}

	private void deliverMessage(String destinationName, OutboundMessage message, boolean isTopic)
	{
		Session session = null;
		try
		{
			createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = null;
			if (isTopic)
			{
				destination = session.createTopic(destinationName);
			}
			else
			{
				destination = session.createQueue(destinationName);
			}

			MessageProducer producer = session.createProducer(destination);
			TextMessage textMessage = createOutboundMessage(message, session);

			producer.send(textMessage);

		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if (session != null)
			{
				try
				{
					session.close();
				}
				catch (JMSException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private TextMessage createOutboundMessage(OutboundMessage message, Session session)
	{
		if (message == null)
		{
			return null;
		}
		try
		{
			TextMessage textMessage = session.createTextMessage();
			Set<Entry<String, Object>> properties = message.getHeaderProperies().entrySet();

			for (Entry<String, Object> property : properties)
			{
				textMessage.setStringProperty(property.getKey(), (String) property.getValue());
			}

			textMessage.setText(message.getBody());
			return textMessage;
		}
		catch (JMSException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public void shutdown()
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (JMSException e)
			{
				// no need to report anything.
				e.printStackTrace();
			}
			connection = null;
		}
	}

	@Override
	public void sendMessage(String destinationName, OutboundMessage message) throws Exception
	{
		deliverMessage(destinationName, message, false);
	}

	@Override
	public List<InboundMessage> browseQueueMessages(String destinationName)
	{
		return browseMessages(destinationName, false);
	}

	@Override
	public List<InboundMessage> browseTopicMessages(String destinationName)
	{
		return browseMessages(destinationName, true);
	}

	private InitialContext getContext()
	{
		if (initContext == null)
		{
			try
			{
				initContext = new InitialContext();
			}
			catch (NamingException e)
			{
				throw new RuntimeException(e);
			}
		}
		return initContext;
	}

	private List<InboundMessage> browseMessages(String destinationName, boolean isTopic)
	{
		List<InboundMessage> messages = new ArrayList<>();
		Session session = null;
		jakarta.jms.Queue destination;
		try
		{
			createConnection();
			session = connection.createSession();
			destination = session.createQueue(destinationName);

			QueueBrowser browser = session.createBrowser(destination);

			@SuppressWarnings("rawtypes")
			Enumeration msgs = browser.getEnumeration();

			while (msgs.hasMoreElements())
			{
				TextMessage message = (jakarta.jms.TextMessage) msgs.nextElement();
				messages.add(createInboundMessage(message));
			}
			return messages;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public List<String> getListOfQueues()
	{
		return new ArrayList<>();
	}

	@Override
	public List<String> getListOfTopics()
	{
		return new ArrayList<>();
	}

}
