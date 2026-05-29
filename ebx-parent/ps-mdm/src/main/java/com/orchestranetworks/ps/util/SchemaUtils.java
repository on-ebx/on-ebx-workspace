/*
 */
package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;

/**
 * Utility class to manipulate schemas.
 *
 * @author MCH
 */
public final class SchemaUtils
{
	/**
	 * Get the list of table nodes from a given node.
	 *
	 * @author MCH
	 *
	 * @param pNode the node from where to search for table nodes
	 * @return the list of table nodes
	 *
	 */
	public static List<SchemaNode> getTableNodes(final SchemaNode pNode)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		if (pNode.isTableOccurrenceNode())
		{
			return nodes;
		}
		for (SchemaNode node : pNode.getNodeChildren())
		{
			if (node.isTableNode())
			{
				nodes.add(node);
			}
			else
			{
				nodes.addAll(getTableNodes(node));
			}
		}
		return nodes;
	}

	/**
	 * Get the list of tabs from a given node.
	 *
	 * @author MCH
	 *
	 * @param pRootNode the node from where to search for tabs
	 * @return the list of terminal nodes
	 *
	 */
	public static List<SchemaNode> getTabs(final SchemaNode pRootNode)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		for (SchemaNode node : pRootNode.getNodeChildren())
		{
			if (isDisplayedAsTab(node))
			{
				nodes.add(node);
			}
			else
			{
				nodes.addAll(getTabs(node));
			}
		}
		return nodes;
	}

	/**
	 * Get the list of terminal nodes from a given node.
	 *
	 * @author MCH
	 *
	 * @param pRootNode the node from where to search for terminal nodes
	 * @return the list of terminal nodes
	 *
	 */
	public static List<SchemaNode> getTerminalNodes(final SchemaNode pRootNode)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		for (SchemaNode node : pRootNode.getNodeChildren())
		{
			if (node.isTerminalValue())
			{
				nodes.add(node);
			}
			else
			{
				nodes.addAll(getTerminalNodes(node));
			}
		}
		return nodes;
	}

	/**
	 * Gets categories of a given node
	 * This method is used by DynamicFormBasedOnType and DynamicAccessRuleBasedOnType
	 *
	 * The category of an attribute is expected to be a succession of token separated by ';'.
	 * Each token is composed as the type and the associated mandatory boolean separated by a ','.
	 * Example : catA,true;catB,false -> the node belongs to 2 categories typeA and typeB.
	 *
	 * @see DynamicAccessRuleBasedOnTypes
	 * @see DynamicForm
	 *
	 * @author MCH
	 * @param pNode the node
	 *
	 * @return a list of types that can be empty
	 */
	public static List<String> getTypesForNode(final SchemaNode pNode)
	{
		List<String> types = new ArrayList<>();
		SchemaNodeInformation nodeInfo = pNode.getInformation();

		if (nodeInfo == null)
		{
			return types;
		}
		String information = nodeInfo.getInformation();
		if (information == null)
		{
			return types;
		}
		String[] tokens = information.split(";");
		for (int i = 0; i < tokens.length; i++)
		{
			types.add(tokens[i].split(",")[0]);
		}
		return types;
	}

	/**
	 * Get the list of nodes children of a given node which are visible by a user.
	 *
	 * @author MCH
	 *
	 * @param pRecordOrDataset the record to get the permission from, a data set otherwise.
	 * @param pNode the parent node
	 * @param excludedNodes
	 * @param pSession the session to get the permission from
	 * @return the list of terminal nodes
	 *
	 */
	public static List<SchemaNode> getVisibleFirstLevelNodes(
		final Adaptation pRecordOrDataset,
		final SchemaNode pNode,
		final List<SchemaNode> excludedNodes,
		final Session pSession)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		for (SchemaNode node : pNode.getNodeChildren())
		{
			if (excludedNodes != null && excludedNodes.contains(node))
			{
				continue;
			}
			if (!pSession.getPermissions().getNodeAccessPermission(node, pRecordOrDataset).isHidden()
				&& !node.getDefaultViewProperties().isHidden())
			{
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * Get the list of terminal nodes from a given node which are visible by a user.
	 *
	 * @author MCH
	 *
	 * @param pRecordOrDataset the record to get the permission from, a data set otherwise.
	 * @param pNode the node from where to search for terminal nodes
	 * @param excludedNodes
	 * @param pSession the session to get the permission from
	 * @return the list of terminal nodes
	 *
	 */
	public static List<SchemaNode> getVisibleTerminalNodes(
		final Adaptation pRecordOrDataset,
		final SchemaNode pNode,
		final List<SchemaNode> excludedNodes,
		final Session pSession)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		for (SchemaNode node : pNode.getNodeChildren())
		{
			if (excludedNodes != null && excludedNodes.contains(node))
			{
				continue;
			}
			if (node.isTerminalValue())
			{
				if (!pSession.getPermissions().getNodeAccessPermission(node, pRecordOrDataset).isHidden()
					&& !node.getDefaultViewProperties().isHidden())
				{
					nodes.add(node);
				}
			}
			else
			{
				nodes.addAll(getVisibleTerminalNodes(pRecordOrDataset, node, excludedNodes, pSession));
			}
		}
		return nodes;
	}

	/**
	 * Get the list of terminal nodes from a given node which are visible by a user for a given record.
	 *
	 * @author MCH
	 *
	 * @param pNode the node from where to search for terminal nodes
	 * @param pRecord the record to get the permission from.
	 * @param pSession the session to get the permission from
	 * @return the list of terminal nodes
	 *
	 */
	public static List<SchemaNode> getVisibleTerminalNodes(final SchemaNode pNode, final Session pSession)
	{
		List<SchemaNode> nodes = new ArrayList<>();
		for (SchemaNode node : pNode.getNodeChildren())
		{
			if (node.isTerminalValue())
			{
				if (!node.getDefaultViewProperties().isHidden())
				{
					nodes.add(node);
				}
			}
			else
			{
				nodes.addAll(getVisibleTerminalNodes(node, pSession));
			}
		}
		return nodes;
	}

	/**
	 * Checks if a node display mode is tab.
	 *
	 * @author MCH
	 * @param pNode the node
	 * @return true, if display mode is tab
	 */
	// TODO replace private code
	public static boolean isDisplayedAsTab(final SchemaNode pNode)
	{
		SchemaNodeDefaultView properties = pNode.getDefaultViewProperties();
		if (properties == null)
		{
			return false;
		}
		return "tab".equals(properties.getDisplayMode());
	}

	/**
	 * Checks if a node is a list.
	 *
	 * @author MCH
	 * @param pNode the node
	 * @return true, if is list
	 */
	public static boolean isList(final SchemaNode pNode)
	{
		return pNode.getMaxOccurs() > 1;
	}

	/**
	 * Checks if a node is mandatory regarding a specific type.
	 * This method is used by DynamicFormBasedOnType
	 *
	 * The information of an field is expected to be a succession of token separated by ';'.
	 * Each token is composed as the type and the associated mandatory boolean separated by a ','.
	 * Example : typeA,true;typeB,false -> the node belongs to 2 types typeA and typeB. the node is mandatory for type typeA.
	 *
	 * @see DynamicAccessRuleBasedOnTypes
	 * @see DynamicForm
	 *
	 * @author MCH
	 * @param pNode the node
	 * @param pTypes the category
	 * @return true, if the node is mandatory in the category.
	 */
	public static boolean isNodeMandatory(final SchemaNode pNode, final List<String> pTypes)
	{
		SchemaNodeInformation nodeInformation = pNode.getInformation();
		if (nodeInformation == null)
		{
			return pNode.getMinOccurs() > 0;
		}

		String information = nodeInformation.getInformation();
		if (information == null)
		{
			return pNode.getMinOccurs() > 0;
		}

		List<String> tokens = Arrays.asList(information.split(";"));
		for (String token : tokens)
		{
			String type = token.split(",")[0];
			if (pTypes.contains(type))
			{
				return Boolean.valueOf(token.split(",")[1]).booleanValue();
			}
		}
		return pNode.getMinOccurs() > 0;
	}

	/**
	 * Checks if is primary key.
	 *
	 * @author MCH
	 * @param pNode the node
	 * @return true, if is primary key
	 */
	public static boolean isPrimaryKey(final SchemaNode pNode)
	{
		if (!pNode.isTableOccurrenceNode())
		{
			return false;
		}
		SchemaNode tableNode = pNode.getTableNode();
		if (tableNode == null)
		{
			return false;
		}

		SchemaNode[] pkNodes = tableNode.getTablePrimaryKeyNodes();
		for (SchemaNode aPKNode : pkNodes)
		{
			if (aPKNode.equals(pNode))
			{
				return true;
			}
		}
		return false;
	}
}
