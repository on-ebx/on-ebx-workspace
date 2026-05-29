/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */


/**
 */
public class CurrentDateDefaultingTableTrigger extends BaseTableTriggerEnforcingPermissions
{
	protected Path datePath;

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		Session session = context.getSession();
		if (session.getInteraction(true) != null)
		{
			ValueContextForUpdate updateContext = context.getOccurrenceContextForUpdate();
			updateContext.setValueEnablingPrivilegeForNode(new Date(), datePath);

		}
		super.handleNewContext(context);
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);
		if (datePath != null)
		{
			SchemaNode dateNode = context.getSchemaNode().getNode(datePath);
			if (dateNode == null)
			{
				context.addError("datePath " + datePath.format() + " does not exist.");
			}
		}
	}

	public String getDatePath()
	{
		return this.datePath.format();
	}

	public void setDatePath(String datePath)
	{
		this.datePath = Path.parse(datePath);
	}
}
