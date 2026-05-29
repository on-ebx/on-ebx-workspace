/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to hide or show occulted records in a table view. 
 * This rule is primarily used when you have inherited datasets and you need to hide or show the occulted records in the child dataset.
 * By default, occulted records do not show up in a child dataset. This rule looks up a session attribute and toggles the permission accordingly.
 * You could have a service defined on the table that sets or unsets this session attribute.
 * 
 */
public class HideOrShowOccultedRecordsAccessRule implements AccessRule
{

	@Override
	public AccessPermission getPermission(Adaptation adaptation, Session session, SchemaNode node)
	{
		if (adaptation.isSchemaInstance() || adaptation.isHistory())
		{
			return AccessPermission.getReadWrite();
		}

		// Hide or show Occulted records based on the session attribute
		String showOccultedRecords = (String) session
			.getAttribute(CommonConstants.SHOW_OCCULTED_RECORDS_SESSION_ATTRIBUTE);

		if (!adaptation.isRootAdaptation()
			&& adaptation.getOccurrenceDefinitionMode().isOcculting())
		{
			if (showOccultedRecords != null && Boolean.TRUE.toString().equals(showOccultedRecords))
			{
				return AccessPermission.getReadWrite();
			}
			return AccessPermission.getHidden();
		}
		return AccessPermission.getReadWrite();
	}
}
