package com.orchestranetworks.ps.validation.service;

import com.onwbp.base.text.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.ps.validation.service.GenerateValidationReport.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.ui.toolbar.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Contains service declarations for the Generate Validation Report service on data sets.
 * 
 * The typical use case is to declare the service on a data set, using {@link OnDataSet}.
 * 
 * However, when in a workflow whose main focus is a table or record, you don't have access to the data set's Action menu.
 * In that case, you would use {@link OnTableView} or {@link OnRecord}, respectively.
 * The default service permission rule for those variants is to only allow the service when inside a workflow
 * and the entity selection is the focus of the workflow.
 */
public interface GenerateDataSetValidationReportDeclarations
{
	/**
	 * An abstract declaration that the other declarations extend, that defines some base logic
	 */
	public abstract class AbstractGenerateDataSetValidationReportDeclaration<S extends DatasetEntitySelection, U extends ActivationContext<S>>
		extends
		GenericServiceDeclaration<S, U>
	{
		public static final String DEFAULT_SERVICE_TITLE = "Generate Dataset Validation Report";
		public static final String DEFAULT_SERVICE_DESCRIPTION = "Download a CSV file with all data set validation messages over the min severity threshold";

		protected ServicePermissionRule<S> servicePermissionRule;

		protected Severity severity;

		protected AbstractGenerateDataSetValidationReportDeclaration(
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
	 * Declares the service on a data set. This is the standard way of declaring the service.
	 */
	public class OnDataSet
		extends
		AbstractGenerateDataSetValidationReportDeclaration<DatasetEntitySelection, ActivationContextOnDataset>
		implements UserServiceDeclaration.OnDataset
	{
		public static final String SERVICE_NAME = "GenerateDataSetValidationReport";

		public OnDataSet(String moduleName, Severity severity)
		{
			this(moduleName, severity, DEFAULT_SERVICE_TITLE, DEFAULT_SERVICE_DESCRIPTION);
		}

		public OnDataSet(String moduleName, Severity severity, String title, String description)
		{
			super(moduleName, SERVICE_NAME, severity, title, description);
		}
	}

	/**
	 * Abstract declaration for declaring the service at a table level. It allows you to specify which tables
	 * it should be defined for.
	 * 
	 * By default, it's configured with a {@link WorkflowFocusOnlyServicePermissionRule} that only allows it
	 * when the focus of the workflow is the same as the entity selection's table/record.
	 * 
	 * This is an abstract class and can't be instantiated. You should instantiate one of its subclasses.
	 */
	public abstract class OnTable<S extends TableEntitySelection, U extends ActivationContextWithSchemaNodeSet<S>>
		extends
		AbstractGenerateDataSetValidationReportDeclaration<S, U>

	{
		protected Path[] includedTablePaths;
		protected Path[] excludedTablePaths;

		protected OnTable(
			String moduleName,
			String serviceKey,
			Severity severity,
			String title,
			String description)
		{
			super(moduleName, serviceKey, severity, title, description);
			setServicePermissionRule(new WorkflowFocusOnlyServicePermissionRule<>());
		}

		@Override
		public void defineActivation(U definition)
		{
			super.defineActivation(definition);
			if (includedTablePaths != null)
			{
				definition.includeSchemaNodesMatching(includedTablePaths);
			}
			if (excludedTablePaths != null)
			{
				definition.excludeSchemaNodesMatching(excludedTablePaths);
			}
		}

		@Override
		public UserService<S> createUserService()
		{
			GenerateValidationReport<S> service = (GenerateValidationReport<S>) super.createUserService();
			// Need to specify that it's a Data Set validation explicitly. Otherwise, the service will assume
			// it's validation tables or records based on the entity selection. Even though we're assigning it
			// to a table or record, we still want a data set validation.
			service.setValidationReportLevel(ValidationReportLevel.DATA_SET);
			return service;
		}

		/**
		 * @see #setIncludedTablePaths(Path[])
		 */
		public Path[] getIncludedTablePaths()
		{
			return includedTablePaths;
		}

		/**
		 * Set the list of paths that this service should be defined for. If <code>null</code>,
		 * will be defined for every table in the data set (with the exception of those specified
		 * by {@link #setExcludedTablePaths(Path[])}).
		 * 
		 * @param includedTablePaths the table paths to include
		 */
		public void setIncludedTablePaths(Path[] includedTablePaths)
		{
			this.includedTablePaths = includedTablePaths;
		}

		/**
		 * @see #setExcludedTablePaths(Path[])
		 */
		public Path[] getExcludedTablePaths()
		{
			return excludedTablePaths;
		}

		/**
		 * Set the list of table paths to exclude from the table paths that the service is defined for.
		 * This further restricts what may be set by {@link #setIncludedTablePaths(Path[])}.
		 * 
		 * @param excludedTablePaths the table paths to exclude
		 */
		public void setExcludedTablePaths(Path[] excludedTablePaths)
		{
			this.excludedTablePaths = excludedTablePaths;
		}
	}

	/**
	 * Declares the service on a table view. It will display the service when in a workflow, and the table is the main focus
	 * of the workflow.
	 */
	public class OnTableView extends OnTable<TableViewEntitySelection, ActivationContextOnTableView>
		implements UserServiceDeclaration.OnTableView

	{
		public static final String SERVICE_NAME = OnDataSet.SERVICE_NAME + "OnTableView";

		public OnTableView(String moduleName, Severity severity)
		{
			this(moduleName, severity, DEFAULT_SERVICE_TITLE, DEFAULT_SERVICE_DESCRIPTION);
		}

		public OnTableView(String moduleName, Severity severity, String title, String description)
		{
			super(moduleName, SERVICE_NAME, severity, title, description);
		}

		@Override
		public void defineActivation(ActivationContextOnTableView definition)
		{
			super.defineActivation(definition);
			// Hide it in the default menus for these toolbars. It should only show up on the table's Action menu.
			definition.setDisplayForLocations(
				ActionDisplaySpec.HIDDEN_IN_DEFAULT_MENU,
				ToolbarLocation.ASSOCIATION_ROW,
				ToolbarLocation.ASSOCIATION_TOP,
				ToolbarLocation.HIERARCHICAL_VIEW_NODE,
				ToolbarLocation.RECORD_VIEW_TOP,
				ToolbarLocation.TABLE_VIEW_ROW);
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

		@Override
		public void defineActivation(ActivationContextOnRecord definition)
		{
			super.defineActivation(definition);
			// Hide it in the default menus for these toolbars. It should only show up on the record's Action menu.
			definition.setDisplayForLocations(
				ActionDisplaySpec.HIDDEN_IN_DEFAULT_MENU,
				ToolbarLocation.ASSOCIATION_ROW,
				ToolbarLocation.ASSOCIATION_TOP,
				ToolbarLocation.HIERARCHICAL_VIEW_NODE,
				ToolbarLocation.HIERARCHICAL_VIEW_TOP,
				ToolbarLocation.TABLE_VIEW_ROW,
				ToolbarLocation.TABLE_VIEW_TOP);
		}
	}
}
