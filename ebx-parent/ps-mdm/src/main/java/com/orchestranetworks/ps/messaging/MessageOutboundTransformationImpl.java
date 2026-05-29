package com.orchestranetworks.ps.messaging;

import java.util.*;

import com.onwbp.adaptation.*;

public class MessageOutboundTransformationImpl extends MessageOutboundTransformation
{
	@Override
	public String transformFromAdaptation(TransformationContext context, Adaptation adaptation)
	{
		return TransformationUtils.toString(adaptation, isPrettyPrint());
	}

	@Override
	public String transformFromAdaptation(
		TransformationContext context,
		List<Adaptation> adaptations)
	{
		StringBuilder result = new StringBuilder();
		if (adaptations != null)
		{
			result.append("{");
			for (Adaptation adaptation : adaptations)
			{
				result.append(transformFromAdaptation(context, adaptation));
			}
			result.append("}");
			return result.toString();

		}
		return null;
	}

}
