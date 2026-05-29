package com.orchestranetworks.ps.admin.devartifacts.util;

import org.apache.commons.lang3.builder.*;

/**
 * Defines categories for Dev Artifacts, used by the error handling mechanism
 */
public enum ArtifactCategory {
	ADDON_ADIX("Add-on ADIX"),

	ADDON_DAMA("Add-on DAMA"),

	/** @deprecated DAQA add-on is no longer supported */
	@Deprecated
	ADDON_DAQA("Add-on DAQA"),

	ADDON_DINT("Add-on DINT"),

	ADDON_DMDV("Add-on DMDV"),
	
	ADDON_DPRA("Add-on DPRA"),

	ADDON_DQID("Add-on DQID"),

	/** @deprecated HMFH add-on is no longer supported */
	@Deprecated
	ADDON_HMFH("Add-on HMFH"),

	ADDON_REGISTRATIONS("Add-on Registrations"),

	ADDON_MAME("Add-on MAME"),

	/** @deprecated RPFL add-on is no longer supported */
	@Deprecated
	ADDON_RPFL("Add-on RPFL"),

	/** @deprecated TESE add-on is no longer supported */
	@Deprecated
	ADDON_TESE("Add-on TESE"),

	ADMIN_DATA_SET_PERMISSIONS("Admin Data Set Permissions"),

	DATA_SET("Data Set"),

	DATA_SPACE("Data Space"),

	DIRECTORY("Directory"),

	DMA("DMA"),

	GLOBAL_PERMISSIONS("Global Permissions"),

	HISTORIZATION_PROFILES("Historization Profiles"),

	MESSAGE_TEMPLATES("Message Templates"),

	PERSPECTIVES("Perspectives"),

	TABLE_DATA("Table Data"),

	TASKS("Tasks"),

	VIEWS("Views"),

	WORKFLOW_ADMIN_CONFIG("Workflow Admin Config"),

	WORKFLOW_LAUNCHERS("Workflow Launchers"),

	WORKFLOW_MODELS("Workflow Models"),

	WORKFLOW_MODEL_PUBLICATIONS("Workflow Model Publications");

	private String value;

	private ArtifactCategory(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).appendSuper(super.toString())
			.append("[")
			.append(value)
			.append("]")
			.toString();
	}

	public String getValue()
	{
		return value;
	}
}
