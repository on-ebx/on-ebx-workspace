package com.orchestranetworks.ps.procedure;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * 
 * Set a string representation of a value (XsString) in a record's node.
 * @author MCH
 */
public final class SetXsStringValueProcedure extends GenericProcedure
{

	/** The record. */
	private final Adaptation record;

	/** The node. */
	private final SchemaNode node;

	/** The value. */
	private final String value;

	/**
	 * Set the value of a given node in a record
	 *
	 * @param aRecord the record
	 * @param aNode the node
	 * @param aValue the value as an XsString
	 */
	public SetXsStringValueProcedure(
		final Adaptation aRecord,
		final SchemaNode aNode,
		final String aValue)
	{
		this.record = aRecord;
		this.node = aNode;
		this.value = aValue;
	}

	@Override
	protected void doExecute(final ProcedureContext pContext) throws OperationException
	{
		final ValueContextForUpdate vcfu = pContext.getContext(this.record.getAdaptationName());
		vcfu.setValue(this.node.parseXsString(this.value), this.node.getPathInAdaptation());
		pContext.doModifyContent(this.record, vcfu);
	}

	@Override
	protected AdaptationHome getHome()
	{
		return record.getHome();
	}
}
