package com.orchestranetworks.ps.messaging;

import java.util.*;

import com.onwbp.base.misc.*;

public class Message
{
	private Map<String, Object> headerProperies = new HashMap<>();
	private String correlationID;
	private long expiration;
	private String messageId;
	private long deliveryTime;
	private String type;
	private String body;

	public String getCorrelationID()
	{
		return correlationID;
	}
	public void setCorrelationID(String correlationID)
	{
		this.correlationID = correlationID;
	}
	public long getExpiration()
	{
		return expiration;
	}
	public void setExpiration(long expiration)
	{
		this.expiration = expiration;
	}
	public String getMessageId()
	{
		return messageId;
	}
	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}
	public long getDeliveryTime()
	{
		return deliveryTime;
	}
	public void setDeliveryTime(long deliveryTime)
	{
		this.deliveryTime = deliveryTime;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public Map<String, Object> getHeaderProperies()
	{
		return headerProperies;
	}
	public void setHeaderProperies(Map<String, Object> headerProperies)
	{
		this.headerProperies = headerProperies;
	}

	public void addHeaderProperty(String headerName, String value)
	{
		headerProperies.put(headerName, value);
	}

	public String getHeaderProperty(String propertyName)
	{
		if (StringUtils.isEmpty(propertyName))
		{
			throw new NullPointerException("Header property name can not be null.");
		}
		return (String) headerProperies.get(propertyName);
	}

	public String getBody()
	{
		return body;
	}
	public void setBody(String body)
	{
		this.body = body;
	}

}
