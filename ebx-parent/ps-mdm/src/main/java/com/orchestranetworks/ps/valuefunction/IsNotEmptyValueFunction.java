package com.orchestranetworks.ps.valuefunction;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This valueFunction returns true if the list field specified is not empty.
 * The listField can be an association or any repeating field. 
 */
public class IsNotEmptyValueFunction implements ValueFunction
{
	private Path listFieldPath;
	private SchemaNode listFieldNode;

	public Path getListFieldPath()
	{
		return listFieldPath;
	}

	public void setListFieldPath(Path startDatePath)
	{
		this.listFieldPath = startDatePath;
	}

	@Override
	public Object getValue(Adaptation context)
	{
		if (listFieldNode.isAssociationNode())
			return !listFieldNode.getAssociationLink().getAssociationResult(context).isEmpty();
		else
			return !CollectionUtils.isEmpty(context.getList(listFieldPath));
	}

	@Override
	public void setup(ValueFunctionContext context)
	{
		listFieldNode = PathUtils.setupFieldNode(context, listFieldPath, "listFieldPath", false);
		if (listFieldNode != null && listFieldNode.getMaxOccurs() == 1)
			context.addError(listFieldNode.getLabel(Locale.getDefault()) + " is not a list field");
	}

}
