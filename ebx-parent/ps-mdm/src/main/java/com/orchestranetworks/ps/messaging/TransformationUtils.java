package com.orchestranetworks.ps.messaging;

import org.json.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;

public class TransformationUtils
{
	public static String toString(Adaptation adaptation, boolean prettyPrint)
	{
		try
		{
			JSONObject metadata = new JSONObject();
			metadata
				.put(MessagingConstants.MESSAGE_DATASPACE, adaptation.getHome().getKey().getName());
			metadata.put(
				MessagingConstants.MESSAGE_DATASET,
				adaptation.getContainer().getAdaptationName().getStringName());
			metadata.put(
				MessagingConstants.MESSAGE_TABLE,
				adaptation.getContainerTable().getTablePath().format());

			JSONObject wrapper = new JSONObject();
			wrapper.put(MessagingConstants.MESSAGE_METADATA, metadata);
			JSONObject result = JSONUtil.jsonObjectForAdaptation(adaptation, true);
			wrapper.put(MessagingConstants.MESSAGE_PAYLOAD, result);
			// if we don't have any payload we don't send a message.
			if (result != null)
			{
				if (prettyPrint)
				{
					return wrapper.toString(4);
				}
				else
				{
					return wrapper.toString();
				}
			}
			return null;
		}
		catch (JSONException e)
		{
			throw new RuntimeException(
				"There was an error converting an adaptation to a JSON object",
				e);
		}
	}

}
