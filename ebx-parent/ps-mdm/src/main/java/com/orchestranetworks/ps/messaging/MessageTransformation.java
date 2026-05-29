package com.orchestranetworks.ps.messaging;

public abstract class MessageTransformation
{
	private boolean prettyPrint = false;
	static final String defaultInboundTransformerClassName = "";
	static final String defaultOutboundTransformerClassName = "";

	public boolean isPrettyPrint()
	{
		return prettyPrint;
	}
	public void setPrettyPrint(boolean prettyPrint)
	{
		this.prettyPrint = prettyPrint;
	}

}
