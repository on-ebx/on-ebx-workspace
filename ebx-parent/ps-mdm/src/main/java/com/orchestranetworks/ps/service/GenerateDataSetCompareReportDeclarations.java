package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.service.GenerateCompareReport.*;
import com.orchestranetworks.ps.servicepermission.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.ui.toolbar.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * Contains service declarations for the Generate Compare Report service on data sets.
 * 
 * The typical use case is to declare the service on a data set, using {@link OnDataSet}.
 * 
 * However, when in a workflow whose main focus is a table or record, you don't have access to the data set's Action menu.
 * In that case, you would use {@link OnTableView} or {@link OnRecord}, respectively.
 * The default service permission rule for those variants is to only allow the service when inside a workflow
 * and the entity selection is the focus of the workflow.
 */
public interface GenerateDataSetCompareReportDeclarations
{
	/**
	 * An abstract declaration that the other declarations extend, that defines some base logic
	 */
	public abstract class AbstractGenerateDataSetCompareReportDeclaration<S extends DatasetEntitySelection, U extends ActivationContext<S>>
		extends
		GenericServiceDeclaration<S, U>
	{
		protected static final String SERVICE_TITLE = "Generate Dataset Compare Report";
		protected static final String SERVICE_DESCRIPTION = "Download an excel workbook detailing the differences between this data set and the same from the data space's initial snapshot";

		protected ServicePermissionRule<S> servicePermissionRule = new ChildDataSpaceOnlyServicePermissionRule<>();

		protected AbstractGenerateDataSetCompareReportDeclaration(
			String moduleName,
			String serviceKey)
		{
			super(
				moduleName == null ? ServiceKey.forName(serviceKey)
					: ServiceKey.forModuleServiceName(moduleName, serviceKey),
				null,
				SERVICE_TITLE,
				SERVICE_DESCRIPTION,
				null);
		}

		@Override
		public void defineActivation(U definition)
		{
			definition.setPermissionRule(servicePermissionRule);
		}

		@Override
		public UserService<S> createUserService()
		{
			return new GenerateCompareReport<>();
		}

		/**
		 * @see #setServicePermissionRule(ServicePermissionRule)
		 */
		public ServicePermissionRule<S> getServicePermissionRule()
		{
			return servicePermissionRule;
		}

		/**
		 * Set the permission rule to use. By default, it will be enabled when in a child data space unless
		 * a different rule is passed in.
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
		AbstractGenerateDataSetCompareReportDeclaration<DatasetEntitySelection, ActivationContextOnDataset>
		implements UserServiceDeclaration.OnDataset
	{
		public static final String SERVICE_KEY = "GenerateDataSetCompareReport";

		public OnDataSet(String moduleName)
		{
			super(moduleName, SERVICE_KEY);
		}
	}

	/**
	 * Abstract declaration for declaring the service at a table level. It allows you to specify which tables
	 * it should be defined for.
	 * 
	 * By default, it's configured with a {@link WorkflowFocusOnlyServicePermissionRule} that only allows it
	 * in a child data space, when the focus of the workflow is the same as the entity selection's table/record.
	 * 
	 * This is an abstract class and can't be instantiated. You should instantiate one of its subclasses.
	 */
	public abstract class OnTable<S extends TableEntitySelection, U extends ActivationContextWithSchemaNodeSet<S>>
		extends
		AbstractGenerateDataSetCompareReportDeclaration<S, U>

	{
		protected Path[] includedTablePaths;
		protected Path[] excludedTablePaths;

		protected OnTable(String moduleName, String serviceKey)
		{
			super(moduleName, serviceKey);
			WorkflowFocusOnlyServicePermissionRule<S> rule = new WorkflowFocusOnlyServicePermissionRule<>();
			rule.setAllowInMaster(false);
			setServicePermissionRule(rule);
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
			GenerateCompareReport<S> service = (GenerateCompareReport<S>) super.createUserService();
			// Need to specify that it's a Data Set compare explicitly. Otherwise, the service will assume
			// it's comparing tables or records based on the entity selection. Even though we're assigning it
			// to a table or record, we still want a data set compare.
			service.setCompareReportLevel(CompareReportLevel.DATA_SET);
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
		public static final String SERVICE_KEY = OnDataSet.SERVICE_KEY + "OnTableView";

		public OnTableView(String moduleName)
		{
			super(moduleName, SERVICE_KEY);
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
		public static final String SERVICE_KEY = OnDataSet.SERVICE_KEY + "OnRecord";

		public OnRecord(String moduleName)
		{
			super(moduleName, SERVICE_KEY);
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
