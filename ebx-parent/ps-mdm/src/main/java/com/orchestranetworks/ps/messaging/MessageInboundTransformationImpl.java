package com.orchestranetworks.ps.messaging;

import java.util.*;

import org.json.*;

public class MessageInboundTransformationImpl extends MessageInboundTransformation
{

	@Override
	public Map<String, Object> transformToAdaptation(
		TransformationContext context,
		String inputString)
	{
		Map<String, Object> result = new HashMap<>();
		JSONObject jsonObject;
		try
		{
			jsonObject = new JSONObject(inputString);
			Iterator<?> keys = jsonObject.keys();

			while (keys.hasNext())
			{
				String key = (String) keys.next();
				result.put(key, jsonObject.get(key));
			}
			return result;

		}
		catch (JSONException e)
		{
			throw new RuntimeException("Error converting input string to a JSON object", e);
		}
	}
}
