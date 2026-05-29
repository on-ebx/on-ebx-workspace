package com.orchestranetworks.ps.procedure;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public final class SetValueFromStringProcedure implements Procedure
{
	private final Adaptation record;
	private final SchemaNode node;
	private final String value;

	public SetValueFromStringProcedure(
		final Adaptation aRecord,
		final SchemaNode aNode,
		final String aValue)
	{
		this.record = aRecord;
		this.node = aNode;
		this.value = aValue;
	}

	@Override
	public final void execute(ProcedureContext pContext) throws Exception
	{
		Object obj = (value == null) ? null : node.parseXsString(value);
		ModifyValuesProcedure mvp = new ModifyValuesProcedure(record);
		mvp.setValue(node.getPathInAdaptation(), obj);
		mvp.setAllPrivileges(true);
		mvp.execute(pContext);
	}

	public Adaptation getModifiedRecord()
	{
		return this.record;
	}
}
