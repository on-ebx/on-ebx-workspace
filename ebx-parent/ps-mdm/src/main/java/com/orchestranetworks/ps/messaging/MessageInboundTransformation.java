package com.orchestranetworks.ps.messaging;

import java.util.*;

public abstract class MessageInboundTransformation extends MessageTransformation
{
	public abstract Map<String, Object> transformToAdaptation(
		TransformationContext context,
		String inputString);
}
