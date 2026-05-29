package com.orchestranetworks.ps.validation.service;

import com.onwbp.base.text.*;

/**
 * @deprecated Use {@link GenerateDataSetValidationReportDeclarations.OnDataSet} instead
 */
@Deprecated
public class GenerateValidationReportDeclaration
	extends
	GenerateDataSetValidationReportDeclarations.OnDataSet
{
	public GenerateValidationReportDeclaration(String moduleName, Severity severity)
	{
		super(moduleName, severity);
	}

	public GenerateValidationReportDeclaration(
		String moduleName,
		Severity severity,
		String title,
		String description)
	{
		super(moduleName, severity, title, description);
	}
}
