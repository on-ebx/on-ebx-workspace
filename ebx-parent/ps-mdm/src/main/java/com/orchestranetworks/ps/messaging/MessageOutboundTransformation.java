package com.orchestranetworks.ps.messaging;

import java.util.*;

import com.onwbp.adaptation.*;

public abstract class MessageOutboundTransformation extends MessageTransformation
{
	public abstract String transformFromAdaptation(
		TransformationContext context,
		Adaptation adaptation);
	public abstract String transformFromAdaptation(
		TransformationContext context,
		List<Adaptation> adaptation);
}
