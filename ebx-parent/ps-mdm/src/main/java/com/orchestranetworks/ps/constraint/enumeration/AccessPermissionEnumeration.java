package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class AccessPermissionEnumeration implements ConstraintEnumeration<String>
{

	@Override
	public void checkOccurrence(final String arg0, final ValueContextForValidation arg1)
		throws InvalidSchemaException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String displayOccurrence(final String value, final ValueContext vc, final Locale locale)
		throws InvalidSchemaException
	{
		if (value != null)
		{
			return AccessPermission.parseFlag(value).getLabel();
		}

		return null;
	}

	@Override
	public List<String> getValues(final ValueContext vc) throws InvalidSchemaException
	{
		List<String> accessRights = new ArrayList<>();
		accessRights.add(AccessPermission.getHidden().getFlagString());
		accessRights.add(AccessPermission.getReadOnly().getFlagString());
		accessRights.add(AccessPermission.getReadWrite().getFlagString());
		return accessRights;
	}

	@Override
	public void setup(final ConstraintContext arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String toUserDocumentation(final Locale arg0, final ValueContext arg1)
		throws InvalidSchemaException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
