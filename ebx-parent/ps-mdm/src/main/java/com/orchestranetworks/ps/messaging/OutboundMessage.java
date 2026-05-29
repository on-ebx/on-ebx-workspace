package com.orchestranetworks.ps.messaging;

import com.onwbp.adaptation.*;

public class OutboundMessage extends Message
{
	private Adaptation messageBody;

	public Adaptation getAdaptation()
	{
		return messageBody;
	}

	public void setAdaptation(Adaptation messageBody)
	{
		this.messageBody = messageBody;
	}

}
