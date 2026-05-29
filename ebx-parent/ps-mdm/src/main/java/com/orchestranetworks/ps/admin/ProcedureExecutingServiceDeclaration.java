/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin;

import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

/**
 * A general service that will execute a procedure.  Construct with the procedure to execute.
 * It also allows you to execute the procedure in a new child data space which merges upon
 * completion of the procedure. In this case, you can also supply the label prefix for the
 * child data space (which will be followed by "at [timestamp]" and a data space name
 * to use for a permissions template.
 */
public class ProcedureExecutingServiceDeclaration
	extends
	GenericServiceDeclaration<DataspaceEntitySelection, ActivationContextOnDataspace>
	implements UserServiceDeclaration.OnDataspace
{
	private Procedure procedure;
	private boolean useChildDataSpace;
	private String childDataSpaceLabelPrefix;
	private String permissionsTemplateDataSpaceName;
	private boolean allowNonAdministrators;

	public ProcedureExecutingServiceDeclaration(
		ServiceKey aServiceKey,
		String aTitle,
		String aDescription,
		String anInstruction,
		Procedure procedure)
	{
		this(aServiceKey, aTitle, aDescription, anInstruction, procedure, false, null, null, false);
	}

	public ProcedureExecutingServiceDeclaration(
		ServiceKey aServiceKey,
		String aTitle,
		String aDescription,
		String anInstruction)
	{
		super(aServiceKey, null, aTitle, aDescription, anInstruction);
	}

	public ProcedureExecutingServiceDeclaration(
		ServiceKey aServiceKey,
		String aTitle,
		String aDescription,
		String anInstruction,
		Procedure procedure,
		boolean useChildDataSpace,
		String childDataSpaceLabelPrefix,
		String permissionsTemplateDataSpaceName,
		boolean allowNonAdministrators)
	{
		this(aServiceKey, aTitle, aDescription, anInstruction);
		this.procedure = procedure;
		this.useChildDataSpace = useChildDataSpace;
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
		this.allowNonAdministrators = allowNonAdministrators;
	}

	public boolean isUseChildDataSpace()
	{
		return this.useChildDataSpace;
	}

	public void setUseChildDataSpace(boolean useChildDataSpace)
	{
		this.useChildDataSpace = useChildDataSpace;
	}

	public String getChildDataSpaceLabelPrefix()
	{
		return this.childDataSpaceLabelPrefix;
	}

	public void setChildDataSpaceLabelPrefix(String childDataSpaceLabelPrefix)
	{
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
	}

	public String getPermissionsTemplateDataSpaceName()
	{
		return this.permissionsTemplateDataSpaceName;
	}

	public void setPermissionsTemplateDataSpaceName(String permissionsTemplateDataSpaceName)
	{
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}

	public boolean isAllowNonAdministrators()
	{
		return this.allowNonAdministrators;
	}

	public void setAllowNonAdministrators(boolean allowNonAdministrators)
	{
		this.allowNonAdministrators = allowNonAdministrators;
	}
	@Override
	public UserService<DataspaceEntitySelection> createUserService()
	{
		return new UserServiceNoUI<>(
			new ProcedureExecutingUserService(
				procedure,
				useChildDataSpace,
				childDataSpaceLabelPrefix,
				permissionsTemplateDataSpaceName,
				allowNonAdministrators));
	}
}
