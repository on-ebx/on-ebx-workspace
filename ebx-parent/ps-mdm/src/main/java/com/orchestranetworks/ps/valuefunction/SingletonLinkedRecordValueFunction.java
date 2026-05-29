/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.valuefunction;

import com.onwbp.adaptation.Adaptation;
import com.orchestranetworks.ps.util.AdaptationUtil;
import com.orchestranetworks.schema.*;

/**
 * A value function that looks up a field's value via a linked record list that is expected to return a single record.
 */
public class SingletonLinkedRecordValueFunction extends AbstractForeignKeyValueFunction
{
	@Override
	public Object getActualValue(Adaptation adaptation)
	{
		Object value = AdaptationUtil.getFirstRecordFromLinkedRecordList(
			adaptation,
			foreignKeyPath,
			attributePath);
		if (attributePath == null && value != null)
		{
			return ((Adaptation) value).getOccurrencePrimaryKey().format();
		}
		else
		{
			return value;
		}

	}

	@Override
	public boolean validateSetupForeignKeyPath(ValueFunctionContext context, SchemaNode foreignKeyNode)
	{
		return (foreignKeyNode.isAssociationNode() || foreignKeyNode.isSelectNode());
	}
}
