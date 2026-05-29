/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;

/**
 * Only allows you to execute the service if you have permission to create a record on that table.
 * 
 * Note that while it's not necessary, you may wish to extend this class to check for certain criteria
 * up front thus avoiding more processing and therefore slower performance. For example, if you know the
 * service should not be available when outside a workflow, you can check for that condition up front
 * rather than rely on inspecting the table record's permissions.
 */
public class DeepCopyServicePermission implements ServicePermission
{
	private Map<Path, DeepCopyConfig> configMap = new HashMap<>();
	private String configFactoryClass;

	@Override
	public ActionPermission getPermission(SchemaNode node, Adaptation adaptation, Session session)
	{
		AdaptationTable table = adaptation.isSchemaInstance()
			? adaptation.getTable(node.getPathInSchema())
			: adaptation.getContainerTable();
		Path tablePath = table.getTablePath();
		DeepCopyConfig config;
		if (configMap.containsKey(tablePath))
		{
			config = configMap.get(tablePath);
		}
		else
		{
			DeepCopyConfigFactory configFactory = createFactory();
			config = configFactory.createConfig(table);
			configMap.put(tablePath, config);
		}
		if (config == null)
		{
			return ActionPermission.getHidden();
		}
		boolean permitted = isUserPermitted(config, session, table);
		return permitted ? ActionPermission.getEnabled() : ActionPermission.getHidden();
	}

	private static boolean isUserPermitted(
		DeepCopyConfig config,
		Session session,
		AdaptationTable table)
	{
		boolean permitted = config.isEnforcePermission()
			? PermissionsUtil.isUserPermittedToCreateRecord(session, table)
			: true;
		if (!permitted)
		{
			return false;
		}

		SchemaNode tableOccNode = table.getTableOccurrenceRootNode();
		Adaptation tableDataSet = table.getContainerAdaptation();
		AdaptationHome tableDataSpace = tableDataSet.getHome();
		Map<Path, DeepCopyConfig> fkConfigMap = config.getForeignKeyConfigMap();
		for (Map.Entry<Path, DeepCopyConfig> entry : fkConfigMap.entrySet())
		{
			Path fkPath = entry.getKey();
			SchemaNode fkNode = tableOccNode.getNode(fkPath);
			SchemaFacetTableRef tableRef = fkNode.getFacetOnTableReference();
			HomeKey fkDataSpaceKey = tableRef.getContainerHome();

			// Can't allow modification to another data space from within same procedure context
			if (fkDataSpaceKey != null && !fkDataSpaceKey.equals(tableDataSpace.getKey()))
			{
				LoggingCategory.getKernel()
					.error("Deep copy is not allowed on a foreign key to another data space.");
				return false;
			}
			AdaptationName fkDataSetName = tableRef.getContainerReference();
			Adaptation fkDataSet = (fkDataSetName == null) ? tableDataSet
				: tableDataSpace.findAdaptationOrNull(tableDataSet.getAdaptationName());
			AdaptationTable fkTable = fkDataSet.getTable(tableRef.getTablePath());
			if (!isUserPermitted(entry.getValue(), session, fkTable))
			{
				return false;
			}
		}

		Map<Path, DeepCopyConfig> selNodeConfigMap = config.getSelectionNodeConfigMap();
		for (Map.Entry<Path, DeepCopyConfig> entry : selNodeConfigMap.entrySet())
		{
			Path selNodePath = entry.getKey();
			SchemaNode selNode = tableOccNode.getNode(selNodePath);
			SelectionLink selLink = selNode.getSelectionLink();
			AdaptationReference selNodeDataSetName = selLink.getContainerReference();
			// If no external data set was specified, use the same data set
			Adaptation selNodeDataSet = (selNodeDataSetName == null) ? tableDataSet
				: tableDataSet.getHome().findAdaptationOrNull(selNodeDataSetName);
			Path selNodeTablePath = selLink.getTableNode().getPathInSchema();
			AdaptationTable selNodeTable = selNodeDataSet.getTable(selNodeTablePath);
			if (!isUserPermitted(entry.getValue(), session, selNodeTable))
			{
				return false;
			}
		}
		Map<Path, DeepCopyConfig> associationConfigMap = config.getAssociationConfigMap();
		for (Map.Entry<Path, DeepCopyConfig> entry : associationConfigMap.entrySet())
		{
			DeepCopyConfig associationConfig = entry.getValue();
			AdaptationTable linkTable = associationConfig.getAssociationLinkTable();
			if (linkTable == null)
			{
				// TODO: We can't do any of this yet because we don't have the right API method
				return true;
				// Path associationPath = entry.getKey();
				// SchemaNode association = tableOccNode.getNode(associationPath);
				// AssociationLink assocLink = association.getAssociationLink();
				// HomeKey assocDataSpaceKey = assocLink.getDataSpaceReference();
				// // If no external data space was specified, use the same data space
				// AdaptationHome assocDataSpace = (assocDataSpaceKey == null) ?
				// tableDataSet.getHome()
				// : tableDataSet.getHome().getRepository().lookupHome(assocDataSpaceKey);
				// AdaptationReference assocDataSetName = assocLink.getDataSetReference();
				// Adaptation assocDataSet;
				// // If no external data set was specified, use either the same data set or
				// // the data set with the same name in the external data space
				// if (assocDataSetName == null)
				// {
				// assocDataSet = (assocDataSpaceKey == null) ? tableDataSet
				// : assocDataSpace.findAdaptationOrNull(tableDataSet.getAdaptationName());
				// }
				// // Else look up the external data set
				// else
				// {
				// assocDataSet = assocDataSpace.findAdaptationOrNull(assocDataSetName);
				// }
				// // TODO: This method doesn't exist yet in the API!!!
				// Path assocTablePath = assocLink.getTableNode().getPathInSchema();
				// AdaptationTable assocTable = assocDataSet.getTable(assocTablePath);
				// if (!isUserPermitted(associationConfig, session, assocTable))
				// {
				// return false;
				// }
			}
			else
			{
				if (!isUserPermitted(associationConfig, session, linkTable))
				{
					return false;
				}
			}
		}
		return true;
	}
	@SuppressWarnings("unchecked")
	// If overriding this class in your project, you will need to copy this method into your subclass
	protected DeepCopyConfigFactory createFactory()
	{
		Class<DeepCopyConfigFactory> clss;
		try
		{
			clss = (Class<DeepCopyConfigFactory>) Class.forName(configFactoryClass);
		}
		catch (ClassNotFoundException ex)
		{
			LoggingCategory.getKernel().error("Error loading config factory class", ex);
			return null;
		}
		try
		{
			return clss.getDeclaredConstructor().newInstance();
		}
		catch (ReflectiveOperationException ex)
		{
			LoggingCategory.getKernel().error("Error instantiating factory class", ex);
		}
		return null;
	}

	public String getConfigFactoryClass()
	{
		return this.configFactoryClass;
	}

	public void setConfigFactoryClass(String configFactoryClass)
	{
		this.configFactoryClass = configFactoryClass;
	}
}
