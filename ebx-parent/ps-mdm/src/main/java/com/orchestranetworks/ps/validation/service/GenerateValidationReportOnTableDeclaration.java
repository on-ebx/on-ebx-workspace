package com.orchestranetworks.ps.validation.service;

import com.onwbp.base.text.*;

/**
 * @deprecated Use {@link GenerateTableValidationReportDeclarations.OnTableView} instead
 */
@Deprecated
public class GenerateValidationReportOnTableDeclaration
	extends
	GenerateTableValidationReportDeclarations.OnTableView
{
	public GenerateValidationReportOnTableDeclaration(String moduleName, Severity severity)
	{
		super(moduleName, severity);
	}

	public GenerateValidationReportOnTableDeclaration(
		String moduleName,
		Severity severity,
		String title,
		String description)
	{
		super(moduleName, severity, title, description);
	}
}
