/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 */
public class FieldNotNullableTableTrigger extends TableTrigger
{
	private Set<Path> fieldPaths;

	protected boolean rejectNullValue(Session session, ValueContext valueContext, Path fieldPath)
	{
		return !session.isUserInRole(CommonConstants.TECH_ADMIN);
	}

	protected Set<Path> setupFieldPaths(SchemaNode contextNode)
	{
		Set<Path> paths = new HashSet<>();
		paths.add(contextNode.getPathInAdaptation());
		return paths;
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		ValueChanges changes = context.getChanges();
		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();
		for (Path fieldPath : fieldPaths)
		{
			ValueChange change = changes.getChange(fieldPath);
			if (change != null && change.getValueAfter() == null
				&& rejectNullValue(context.getSession(), vc, fieldPath))
			{
				vc.setValue(change.getValueBefore(), fieldPath);
			}
		}
		super.handleBeforeModify(context);
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		fieldPaths = setupFieldPaths(context.getSchemaNode());
	}
}
