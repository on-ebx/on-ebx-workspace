package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

public class CollectionNotEmptyConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private static final String MESSAGE = "{1} for a {0} cannot be empty.";

	private Path assocPath;

	public Path getAssocPath()
	{
		return assocPath;
	}

	public void setAssocPath(Path assocPath)
	{
		this.assocPath = assocPath;
	}

	private SchemaNode assocNode;
	private SchemaNode parentNode;
	private String message;

	@Override
	public void setup(ConstraintContextOnTable arg0)
	{
		super.setup(arg0);
		assocNode = PathUtils.setupFieldNode(arg0, assocPath, "assocPath", true);
		parentNode = arg0.getSchemaNode();
		Locale locale = Locale.getDefault();
		message = MessageFormat.format(
			MESSAGE,
			parentNode.getLabel(locale),
			assocNode.getLabel(locale));
	}

	@Override
	public String toUserDocumentation(Locale arg0, ValueContext arg1) throws InvalidSchemaException
	{
		return message;
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		Adaptation record = AdaptationUtil.getRecordForValueContext(recordContext);
		if (record != null)
		{
			if (AdaptationUtil.isLinkedRecordListEmpty(record, assocPath))
			{
				return message;
			}
		}
		return null;
	}

}
