package com.orchestranetworks.ps.accessrule;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.workflow.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Access Rule to hide a field on creation of a new record through the UI. Ignores if the record is created otherwise (like through an Import).
 * This rule uses the "_createdInUI" field to determine whether the creation of the new record is performed in the UI.
 *
 */
public class HideWhenCreatedInUiAccessRuleForCreate implements AccessRuleForCreate
{

	private Path createdInUiPath = Path.parse("./_createdInUi");
	// This is the path value assigned by default.
	//  -- can use alternate path if needed with constructor that takes a specific path

	public HideWhenCreatedInUiAccessRuleForCreate()
	{
	}

	public HideWhenCreatedInUiAccessRuleForCreate(Path createdInUiPath)
	{
		this.createdInUiPath = createdInUiPath;
	}

	@Override
	public AccessPermission getPermission(AccessRuleForCreateContext context)
	{
		Session session = context.getSession();
		if (session.isUserInRole(CommonConstants.TECH_ADMIN)
			|| WorkflowUtilities.isPermissionsUser(session))
		{
			return AccessPermission.getReadWrite();
		}

		// Hide if we are creating the record in the UI 
		//  Otherwise, allow it to be accessed (i.e. coming from an Import)
		Boolean createdInUi = (Boolean) context.getValueContext().getValue(createdInUiPath);
		if (createdInUi != null && createdInUi.booleanValue())
		{
			return AccessPermission.getHidden();
		}
		else
		{
			return AccessPermission.getReadWrite();
		}
	}

}
