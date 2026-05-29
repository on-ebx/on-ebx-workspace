package com.orchestranetworks.ps.validation.service;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;

public class DisplayValidationReportDeclaration
extends
GenericServiceDeclaration<DatasetEntitySelection, ActivationContextOnDataset>
implements UserServiceDeclaration.OnDataset
{
	public static final String SERVICE_NAME = "DisplayValidationReport";

	public static final String DEFAULT_SERVICE_TITLE = "Display Validation Report";
	public static final String DEFAULT_SERVICE_DESCRIPTION = "Display all data set validation messages over the min severity threshold";

	private Severity severity;

	public DisplayValidationReportDeclaration(String moduleName, Severity severity)
	{
		this(severity, DEFAULT_SERVICE_TITLE, DEFAULT_SERVICE_DESCRIPTION);
	}

	public DisplayValidationReportDeclaration(Severity severity, String title, String description)
	{
		super(ServiceKey.forName(SERVICE_NAME), null, title, description, null);
		this.severity = severity;
	}

	@Override
	public UserService<DatasetEntitySelection> createUserService()
	{
		return new DisplayValidationReport(
			GenerateValidationReport.DEFAULT_EXPORT_DIR_NAME,
			true,
			true,
			severity);
	}
}
