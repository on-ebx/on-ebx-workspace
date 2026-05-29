/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.uilabelrenderer;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;

/**
 * Renders a label for a table that acts similar to an enum. Can be used when you have a table with 2 fields: one for the value and one for the label.
 * If the label field isn't empty, it will be used as the label of the record. If it's empty, the value will be used.
 */
public class EnumSimilarUILabelRenderer implements UILabelRenderer
{
	private Path valueFieldPath;
	private Path labelFieldPath;

	@Override
	public void displayLabel(UILabelRendererContext context)
	{
		Adaptation record = context.getOccurrence();

		// Note that a NullPointerException can happen if the paths aren't specified or don't exist.
		// This is the correct behavior because that is a developer error and we don't want to ignore that.
		// It will result in no label showing up and the error will appear in the log.
		// I could do a bunch of checks for if the node exists, etc but no good way to report it really, and shouldn't
		// slow down performance of renderer each time it's called for the rare case that developer didn't set it up correctly.
		Object label = record.get(labelFieldPath);

		if (label == null)
		{
			Object value = record.get(valueFieldPath);
			context.add(value == null ? record.getOccurrencePrimaryKey().format()
				: value.toString());
		}
		else
		{
			context.add(label.toString());
		}
	}

	public Path getValueFieldPath()
	{
		return this.valueFieldPath;
	}

	public void setValueFieldPath(Path valueFieldPath)
	{
		this.valueFieldPath = valueFieldPath;
	}

	public Path getLabelFieldPath()
	{
		return this.labelFieldPath;
	}

	public void setLabelFieldPath(Path labelFieldPath)
	{
		this.labelFieldPath = labelFieldPath;
	}
}
