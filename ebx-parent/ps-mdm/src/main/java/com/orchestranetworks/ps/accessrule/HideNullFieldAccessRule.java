package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to hide a field if the value is null. 
 *
 */
public class HideNullFieldAccessRule implements AccessRule
{
	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isHistory() || adaptation.isSchemaInstance())
		{
			return AccessPermission.getReadWrite();
		}

		// Hide field if null
		if (adaptation.get(node.getPathInAdaptation()) == null)
		{
			return AccessPermission.getHidden();
		}
		else
		{
			return AccessPermission.getReadWrite();
		}
	}
}
