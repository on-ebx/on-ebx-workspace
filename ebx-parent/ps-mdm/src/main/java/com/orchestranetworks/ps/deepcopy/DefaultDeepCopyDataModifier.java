/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;

/**
 * A default DeepCopyDataModifier that applies a special label to all fields
 * indicated in the config.
 */
public class DefaultDeepCopyDataModifier implements DeepCopyDataModifier
{
	public static final String DEFAULT_COPY_LABEL_PREFIX = "(Copy) ";

	private String copyLabelPrefix;

	public DefaultDeepCopyDataModifier()
	{
		this(DEFAULT_COPY_LABEL_PREFIX);
	}

	public DefaultDeepCopyDataModifier(String copyLabelPrefix)
	{
		this.copyLabelPrefix = copyLabelPrefix;
	}

	@Override
	public void modifyDuplicateRecordContext(
		ValueContextForUpdate context,
		Adaptation origRecord,
		DeepCopyConfig config,
		Session session)
	{
		applyCopyLabel(context, config);
	}

	protected void applyCopyLabel(ValueContextForUpdate context, DeepCopyConfig config)
	{
		// Prepend the copy label for any field indicated
		Set<Path> pathsToIndicateWithCopy = config.getPathsToIndicateWithCopy();
		for (Path pathToIndicateWithCopy : pathsToIndicateWithCopy)
		{
			String newValue = copyLabelPrefix + context.getValue(pathToIndicateWithCopy);
			// If adding the copy label makes the string exceed its max length,
			// then cut it down so it doesn't cause an error
			SchemaNode node = context.getNode(pathToIndicateWithCopy);
			SchemaFacetMaxLength maxLenFacet = node.getFacetMaxLength();
			if (maxLenFacet != null)
			{
				int maxLen = maxLenFacet.getValue().intValue();
				if (newValue.length() > maxLen)
				{
					newValue = newValue.substring(0, maxLen);
				}
			}
			context.setValue(newValue, pathToIndicateWithCopy);
		}
	}

	public String getCopyLabelPrefix()
	{
		return this.copyLabelPrefix;
	}

	public void setCopyLabelPrefix(String copyLabelPrefix)
	{
		this.copyLabelPrefix = copyLabelPrefix;
	}
}
