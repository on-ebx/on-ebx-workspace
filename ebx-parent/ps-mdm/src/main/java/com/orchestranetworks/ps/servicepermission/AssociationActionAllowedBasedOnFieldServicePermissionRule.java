package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * A rule for the a service that should be put on an association.
 * It prevents executing the service based on the value of a specified field.
 * 
 * @deprecated Use {@link AllowedBasedOnFieldServicePermissionRules.OnAssociation} instead.
 */
@Deprecated
public class AssociationActionAllowedBasedOnFieldServicePermissionRule
	extends
	AllowedBasedOnFieldServicePermissionRules.OnAssociation
{
	public AssociationActionAllowedBasedOnFieldServicePermissionRule(Path fieldPath)
	{
		this(fieldPath, null);
	}

	public AssociationActionAllowedBasedOnFieldServicePermissionRule(
		Path fieldPath,
		Object valueToAllowService)
	{
		super(fieldPath, valueToAllowService);
		setUseAssociationSourceRecord(true);
	}

	@Override
	protected boolean matchValue(Object value, Session session)
	{
		return acceptValue(value);
	}

	protected boolean acceptValue(Object value)
	{
		return Objects.equals(value, getValueToAllowService());
	}

	public Object getValueToAllowService()
	{
		return valueToMatch;
	}

	public void setValueToAllowService(Object valueToAllowService)
	{
		this.valueToMatch = valueToAllowService;
	}
}