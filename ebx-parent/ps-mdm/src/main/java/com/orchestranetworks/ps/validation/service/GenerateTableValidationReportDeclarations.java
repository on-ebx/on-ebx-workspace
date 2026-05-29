package com.orchestranetworks.ps.validation.service;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.ps.validation.service.GenerateDataSetValidationReportDeclarations.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Contains service declarations for the Generate Validation Report service on tables.
 * 
 * The typical use case is to declare the service on a table, using {@link OnTableView}.
 * 
 * However, when in a workflow whose main focus is a record, you don't have access to the table's Action menu.
 * In that case, you would use {@link OnRecord}.
 * The default service permission rule for those variants is to only allow the service when inside a workflow
 * and the entity selection is the focus of the workflow.
 */
public interface GenerateTableValidationReportDeclarations
{
	/**
	 * An abstract declaration that the other declarations extend, that defines some base logic
	 */
	public abstract class OnTable<S extends TableEntitySelection, U extends ActivationContextWithSchemaNodeSet<S>>
		extends
		GenericServiceDeclaration<S, U>
	{
		public static final String DEFAULT_SERVICE_TITLE = "Generate Table Validation Report";
		public static final String DEFAULT_SERVICE_DESCRIPTION = "Download a CSV file with all table validation messages over the min severity threshold";

		protected ServicePermissionRule<S> servicePermissionRule;

		protected Severity severity;

		protected OnTable(
			String moduleName,
			String serviceKey,
			Severity severity,
			String title,
			String description)
		{
			super(
				moduleName == null ? ServiceKey.forName(serviceKey)
					: ServiceKey.forModuleServiceName(moduleName, serviceKey),
				null,
				title,
				description,
				null);
			this.severity = severity;
		}

		@Override
		public void defineActivation(U definition)
		{
			definition.setPermissionRule(servicePermissionRule);
		}

		@Override
		public UserService<S> createUserService()
		{
			return new GenerateValidationReport<S>(
				GenerateValidationReport.DEFAULT_EXPORT_DIR_NAME,
				true,
				true,
				severity,
				true);
		}

		/**
		 * @see #setServicePermissionRule(ServicePermissionRule)
		 */
		public ServicePermissionRule<S> getServicePermissionRule()
		{
			return servicePermissionRule;
		}

		/**
		 * Set the permission rule to use.
		 * If multiple rules are needed, you should pass in a {@link CompoundServicePermissionRule}.
		 * 
		 * @param servicePermissionRule the rule
		 */
		public void setServicePermissionRule(ServicePermissionRule<S> servicePermissionRule)
		{
			this.servicePermissionRule = servicePermissionRule;
		}
	}

	/**
	 * Declares the service on a table view. It will display the service when in a workflow, and the table is the main focus
	 * of the workflow.
	 */
	public class OnTableView extends OnTable<TableViewEntitySelection, ActivationContextOnTableView>
		implements UserServiceDeclaration.OnTableView

	{
		public static final String SERVICE_NAME = "GenerateTableValidationReport";

		public OnTableView(String moduleName, Severity severity)
		{
			this(moduleName, severity, DEFAULT_SERVICE_TITLE, DEFAULT_SERVICE_DESCRIPTION);
		}

		public OnTableView(String moduleName, Severity severity, String title, String description)
		{
			super(moduleName, SERVICE_NAME, severity, title, description);
		}
	}

	/**
	 * Declares the service on a record. It will display the service when in a workflow, and the record is the main focus
	 * of the workflow.
	 */
	public class OnRecord extends OnTable<RecordEntitySelection, ActivationContextOnRecord>
		implements UserServiceDeclaration.OnRecord

	{
		public static final String SERVICE_NAME = OnDataSet.SERVICE_NAME + "OnRecord";

		public OnRecord(String moduleName, Severity severity)
		{
			this(moduleName, severity, DEFAULT_SERVICE_TITLE, DEFAULT_SERVICE_DESCRIPTION);
		}

		public OnRecord(String moduleName, Severity severity, String title, String description)
		{
			super(moduleName, SERVICE_NAME, severity, title, description);
		}
	}
}
