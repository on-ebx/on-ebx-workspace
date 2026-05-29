package com.orchestranetworks.ps.messaging;

public class MessagingConstants
{

	public final static String MESSAGING_MQ_CLINET_CLASS_NAME = "ebx.messaging.clientclassname";

	public final static String MESSAGING_MQ_HOST_PROPERTY = "ebx.messaging.host";
	public final static String MESSAGING_MQ_PORT_PROPERTY = "ebx.messaging.port";

	public final static String MESSAGING_MQ_CONNECTION_TIMEOUT_PROPERTY = "ebx.messaging.connection.timeout";
	public final static String MESSAGING_MQ_CONNECTION_RETRIES = "ebx.messaging.connection.retry";
	public final static String MESSAGING_MQ_CONNECTION_RETRIES_DELAY = "ebx.messaging.connection.retry.delay";
	public static final String MESSAGING_MQ_CONNECTION_REUSE = "ebx.messaging.connection.reuse";
	public static final String MESSAGING_MQ_CONNECTION_JNDI_FACTORY_NAME = "ebx.messaging.connection.jndifactoryname";

	public final static String MESSAGING_MQ_USERNAME_PROPERTY = "ebx.messaging.username";
	public final static String MESSAGING_MQ_PASSWORD_PROPERTY = "ebx.messaging.password";

	public final static String MESSAGING_MQ_DEAD_LETTER_DESTINATION_NAME_PROPERTY = "ebx.messaging.deadletterdestinationname";

	public final static String MESSAGING_MQ_INBOUND_DESTINATION_NAME_PROPERTY = "ebx.messaging.inbound.destinationname";
	public final static String MESSAGING_MQ_INBOUND_WAIT_TIME_PROPERTY = "ebx.messaging.inbound.waittime";

	public final static String MESSAGING_MQ_OUTBOUND_QUEUE_NAME_PROPERTY = "ebx.messaging.outbound.destinationname";
	public final static String MESSAGING_MQ_OUTBOUND_USEPRETTYPRINT_PROPERTY = "ebx.messaging.outbound.useprettyprint";

	// public final static String MESSAGING_MQ_CATEGORY_PROPERTY = "ebx.messaging.category";

	public final static String MESSAGE_HEADER_REPLY_TO = "replyTo";
	public final static String MESSAGE_HEADER_APPLICATION_ID = "applicationId";
	public final static String MESSAGE_HEADER_CLUSTER_ID = "clusterId";
	public final static String MESSAGE_HEADER_CONTENT_TYPE = "contentType";
	public final static String MESSAGE_HEADER_CONTENT_ENCODING = "contentEncoding";

	public final static String MESSAGE_HEADER_DELIVERY_MODE = "deliveryMode";
	public final static String MESSAGE_HEADER_EXPIRATION = "expiration";
	public final static String MESSAGE_HEADER_HEADERS = "headers";
	public final static String MESSAGE_HEADER_MESSAGE_ID = "messageId";

	public final static String MESSAGE_HEADER_PRIORITY = "priority";
	public final static String MESSAGE_HEADER_TIMESTAMP = "timestamp";
	public final static String MESSAGE_HEADER_TYPE = "type";

	public final static String MESSAGE_HEADER_USER_ID = "userId";
	public final static String MESSAGE_HEADER_CORRELATION_ID = "corellationId";

	public static final String MESSAGE_HEADER_DATASPACE_NAME = "ebx.dataspace.id";
	public static final String MESSAGE_HEADER_DATASET_NAME = "ebx.dataset.id";
	public static final String MESSAGE_HEADER_TABLE_PATH = "ebx.table.path";

	// messaging structure must have attributes:
	public static final String MESSAGE_TABLE = "messageTable";
	public static final String MESSAGE_DATASET = "messageDataset";
	public static final String MESSAGE_DATASPACE = "messageDataspace";
	public static final String MESSAGE_PAYLOAD = "messagePayload";
	public static final String MESSAGE_METADATA = "messageMetadata";

	public final static String EBX_PROPS = "ebx.properties";

	public final static String MESSAGING_PROPERTY_FILE_SYSTEM_PROPERTY = "ebx.messaging.properties";

	public final static String MESSAGING_SERVERS_PROPERTY_NAME = "ebx.messaging.servers";

}
