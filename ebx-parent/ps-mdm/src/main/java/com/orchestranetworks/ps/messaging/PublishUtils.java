package com.orchestranetworks.ps.messaging;

import org.json.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.JSONUtil.*;

public class PublishUtils
{

	/**
	 * Generate a diff object between 2 data spaces with default compare options and publish it to a destination
	 * @param rightDataspace - the right data space
	 * @param leftDataspace - the left data space
	 * @param destinationName - destination name. can be eithre exchange name or a topic/queue name
	 */
	public static void publishDataspaceDiff(
		AdaptationHome rightDataspace,
		AdaptationHome leftDataspace,
		String destinationName)
	{
		publishDataspaceDiff(
			rightDataspace,
			leftDataspace,
			new JSONUtil.CompareOptions(),
			destinationName);
	}

	public static void publishDataspaceDiff(
		AdaptationHome rightDataspace,
		AdaptationHome leftDataspace,
		CompareOptions compareOption,
		String destinationName)
	{
		JSONObject diff;
		try
		{
			diff = JSONUtil.getJsonDiffs(rightDataspace, leftDataspace, compareOption);
			OutboundMessage message = new OutboundMessage();
			message.setBody(diff.toString());
			MessagingConfiguration.getMessagingClient().publishMessage(destinationName, message);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
