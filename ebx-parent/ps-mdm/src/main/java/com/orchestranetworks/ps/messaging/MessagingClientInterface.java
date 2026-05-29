package com.orchestranetworks.ps.messaging;

import java.util.*;

public interface MessagingClientInterface
{

	/**
	 * @param destinationName  - a queue name to send a message too.
	 * @param message - {@link com.orchestranetworks.ps.messaging.OutboundMessage}
	 * @throws Exception
	 * 
	 * This method will transform the message body using the default transformation class -{@link com.orchestranetworks.ps.messaging.MessageOutboundTransformationImpl}
	 * if no transformation is needed use - sendMessage(String destinationName, OutboundMessage message, MessageOutboundTransformation transformer)
	 * throws Exception, and pass null for transformer.
	 */
	void sendMessage(String destinationName, OutboundMessage message) throws Exception;

	/**
	 * @param destinationName  - a topic or exchange name to publish a message too.
	 * @param message - {@link com.orchestranetworks.ps.messaging.OutboundMessage}
	 * @throws Exception
	 * 
	 * This method will transform the message body using the default transformation class -{@link com.orchestranetworks.ps.messaging.MessageOutboundTransformationImpl}
	 * if no transformation is needed use - publishMessage(String destinationName, OutboundMessage message, MessageOutboundTransformation transformer)
	 * throws Exception, and pass null for transformer.
	 */
	void publishMessage(String destinationName, OutboundMessage message) throws Exception;

	/**
	 * Subscribe and read all messages from a topic. 
	 * @param topicName
	 * @param timeout
	 * @return - list of Inbound messages in the order that they are in the topic.
	 */
	List<InboundMessage> subscribe(String topicName, int timeout);

	/**
	 * Read all messages from a Queue.
	 * @param queueName 
	 * @param timeout
	 * @return - list of Inbound messages in the order that they are in the queue.
	 */
	List<InboundMessage> readMessages(String queueName, int timeout);

	/**
	 * Note: not all messaging technologies support this function
	 * Browse the messages in a queue.
	 * @param queueName - queue name to browse.
	 * @return - list of inbound messages in the order that they are in the queue. 
	 */
	List<InboundMessage> browseQueueMessages(String queueName);

	/**
	 * Note: not all messaging technologies support this function
	 * Browse the messages in a topic. 
	 * @param queueName - topic name to browse.
	 * @return - list of inbound messages in the order that they are in the topic. 
	 */
	List<InboundMessage> browseTopicMessages(String topicName);

	/**
	 * Note: not all messaging technologies support this function
	 * @return list of queue names.
	 */
	List<String> getListOfQueues();

	/**
	 * Note: not all messaging technologies support this function
	 * @return list of topic names.
	 */
	List<String> getListOfTopics();

	/**
	 * Note: not all messaging technologies support this function
	 * This method is called once a transaction is done and no reuse of a connection is required.	
	 */
	void closeConnection() throws Exception;

	/**
	 * Note: not all messaging technologies support this function
	 * This method is called once there was an error using an open connection and a retry was setup	
	 */
	void resetConnection() throws Exception;

	/**
	 * Note: not all messaging technologies support this function
	 * This method is called at the beginning of each transaction or when an error occurred and the framework is setup for retry.	
	 */
	void createConnection() throws Exception;

	/**
	 * Note: not all messaging technologies support this function
	 * This method is called when the server is shutdown or the module is being un-deployed	
	 */
	void shutdown();

}