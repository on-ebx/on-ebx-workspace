package com.orchestranetworks.ps.util;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * Utilities to convert strings to paths, find nodes, etc.
 */
public class PathUtils
{
	public static final String DEFAULT_SEPARATOR = ";";
	public static final String[] DEFAULT_VALID_PATH_PREFIXES = { Path.ROOT.format(),
			Path.SELF.format(), Path.PARENT.format() };

	public static void validatePathString(
		SchemaNodeContext context,
		String pathStr,
		String separator,
		String[] validPathPrefixes)
	{
		if (separator == null)
			separator = DEFAULT_SEPARATOR;
		if (validPathPrefixes == null)
			validPathPrefixes = DEFAULT_VALID_PATH_PREFIXES;
		if (pathStr != null)
		{
			String[] arr = pathStr.split(separator);
			for (String str : arr)
			{
				boolean foundPrefix = false;
				for (int i = 0; !foundPrefix && i < validPathPrefixes.length; i++)
				{
					foundPrefix = str.startsWith(validPathPrefixes[i]);
				}
				if (!foundPrefix)
				{
					StringBuilder bldr = new StringBuilder();
					bldr.append("Path \"");
					bldr.append(str);
					bldr.append("\" is invalid. Must begin with ");
					for (int i = 0; i < validPathPrefixes.length; i++)
					{
						bldr.append("\"");
						bldr.append(validPathPrefixes[i]);
						bldr.append("\"");
						if (i < validPathPrefixes.length - 1)
						{
							bldr.append(" or ");
						}
						bldr.append(".");
					}
					context.addError(bldr.toString());
				}
			}
		}
	}

	public static Set<Path> convertStringToPathSet(String pathStr, String separator)
	{
		Set<Path> paths = new HashSet<>();
		addPaths(paths, pathStr, separator);
		return paths;
	}

	public static List<Path> convertStringToPathList(String pathStr, String separator)
	{
		List<Path> paths = new ArrayList<>();
		addPaths(paths, pathStr, separator);
		return paths;
	}

	private static void addPaths(Collection<Path> paths, String pathStr, String separator)
	{
		String sep = (separator == null) ? DEFAULT_SEPARATOR : separator;
		if (pathStr != null)
		{
			String[] arr = pathStr.split(sep);
			for (String str : arr)
			{
				paths.add(Path.parse(str.trim()));
			}
		}
	}

	public static String convertPathArrayToString(Path[] paths, String separator)
	{
		String sep = (separator == null) ? null : DEFAULT_SEPARATOR;
		StringBuilder bldr = new StringBuilder();
		for (int i = 0; i < paths.length; i++)
		{
			bldr.append(paths[i].format());
			if (i < paths.length - 1)
			{
				bldr.append(sep);
			}
		}
		return bldr.toString();
	}

	/**
	 * Validate that the path list provided represents a legitimate path expression
	 * and return the resolved schema nodes for the paths.
	 * @param root
	 * @param paths
	 * @return resolved schema nodes
	 */
	public static List<SchemaNode> validatePath(SchemaNode root, List<Path> paths)
	{
		SchemaNode currNode = null;
		List<SchemaNode> nodes = new ArrayList<>();
		for (Path path : paths)
		{
			if (currNode == null && nodes.isEmpty())//first path -- find right root node
			{
				if (Path.PARENT.getFirstStep().equals(path.getFirstStep()))
					currNode = root;
				else
					currNode = root.getTableNode().getTableOccurrenceRootNode();
			}
			currNode = currNode != null ? currNode.getNode(path) : currNode;
			if (currNode == null)
				return nodes;
			nodes.add(currNode);
			SchemaNode tableNode = AdaptationUtil.getTableNodeForRelated(currNode);
			if (tableNode != null)
			{
				currNode = tableNode.getTableOccurrenceRootNode();
			}
		}
		return nodes;
	}

	/**
	 * Many component setup methods need to take a Foreign Key path and get the related field, and
	 * report errors when the path is missing or if the path is not a field, or if it is not referencing the intended table, etc.
	 * @param context SchemaNodeContext e.g. from most setup methods
	 * @param path path representing a field from the SchemaNodeContext's table node
	 * @param parameterName the name for the error message
	 * @param required true if the field is required
	 * @param addDependency -- for constraint context, optionally add field as a dependency
	 * @param targetTablepath path representing path of the Target Table for the Foreign Key Field
	 * @return SchemaNode the node corresponding to the field
	 */
	public static SchemaNode setupFKFieldNode(
		SchemaNodeContext context,
		Path path,
		String parameterName,
		boolean required,
		boolean addDependency,
		Path targetTablePath)
	{
		SchemaNode node = setupFieldNode(
			context,
			context.getSchemaNode().isTableNode() ? null : context.getSchemaNode().getTableNode(),
			path,
			parameterName,
			required,
			addDependency);

		if (node != null)
		{
			if (targetTablePath == null)
			{
				context.addError("target table path must be specified.");
			}
			else if (node.getFacetOnTableReference() == null
				|| !targetTablePath.equals(node.getFacetOnTableReference().getTablePath()))
			{
				context.addError(
					parameterName + " " + path.format() + " must reference the "
						+ targetTablePath.format() + " Table.");
			}
		}

		return node;
	}

	/**
	 * Many component setup methods need to take a path and get the related field, and
	 * report errors when the path is missing or if the path is not a field, etc.
	 * @param context SchemaNodeContext e.g. from most setup methods
	 * @param path path representing a field from the SchemaNodeContext's table node
	 * @param parameterName the name for the error message
	 * @param addDependency -- for constraint context, optionally add field as a dependency
	 * @return SchemaNode the node corresponding to the field
	 */
	public static SchemaNode setupFieldNode(
		SchemaNodeContext context,
		Path path,
		String parameterName,
		boolean addDependency)
	{
		return setupFieldNode(context, path, parameterName, true, addDependency);
	}

	/**
	 * Many component setup methods need to take a path and get the related field, and
	 * report errors when the path is missing or if the path is not a field, etc.
	 * @param context SchemaNodeContext e.g. from most setup methods
	 * @param path path representing a field from the SchemaNodeContext's table node
	 * @param paramterName the name for the error message
	 * @param required true if the field is required
	 * @param addDependency -- for constraint context, optionally add field as a dependency
	 * @return SchemaNode the node corresponding to the field
	 */
	public static SchemaNode setupFieldNode(
		SchemaNodeContext context,
		Path path,
		String parameterName,
		boolean required,
		boolean addDependency)
	{
		return setupFieldNode(context, null, path, parameterName, required, addDependency);
	}

	/**
	 * Many component setup methods need to take a path and get the related field, and
	 * report errors when the path is missing or if the path is not a field, etc.
	 * @param context SchemaNodeContext e.g. from most setup methods
	 * @param parentNode optional node from a related field, perhaps
	 * @param path path representing a field from the SchemaNodeContext's table node
	 * @param parameterName the name for the error message
	 * @param required true if the field is required
	 * @param addDependency -- for constraint context, optionally add field as a dependency
	 * @return SchemaNode the node corresponding to the field
	 */
	public static SchemaNode setupFieldNode(
		SchemaNodeContext context,
		SchemaNode parentNode,
		Path path,
		String parameterName,
		boolean required,
		boolean addDependency)
	{
		if (path == null)
		{
			if (required)
				context.addError(parameterName + " must be specified.");
		}
		else
		{
			if (parentNode == null)
			{
				SchemaNode tableNode = context.getSchemaNode().getTableNode();
				// There won't be a table node when it's a dynamic data model
				if (tableNode == null)
				{
					context.addError("parentNode must be specified when context isn't in a table.");
				}
				else
				{
					parentNode = tableNode.getTableOccurrenceRootNode();
				}
			}
			if (parentNode != null)
			{
				SchemaNode node = Path.PARENT.getFirstStep().equals(path.getFirstStep())
					? context.getSchemaNode().getNode(path)
					: parentNode.getNode(path);
				if (node == null)
				{
					context.addError(parameterName + " " + path.format() + " does not exist.");
				}
				// Need to check if it's a table occurrence node because when it's a dynamic data model,
				// it won't be and dependency only works on a node in a table
				else if (addDependency && node.isTableOccurrenceNode()
					&& context instanceof DependenciesDefinitionContext)
				{
					addDependency((DependenciesDefinitionContext) context, node);
				}
				return node;
			}
		}
		return null;
	}

	/**
	 * Add a fieldNode as a dependency.  If the field is an association field, add dependency to the association table.
	 * @param context
	 * @param fieldNode
	 */
	public static void addDependency(DependenciesDefinitionContext context, SchemaNode fieldNode)
	{
		if (fieldNode.isAssociationNode())
		{
			AssociationLink link = fieldNode.getAssociationLink();
			if (link.isLinkTable())
			{
				AssociationLinkByLinkTable alink = (AssociationLinkByLinkTable) link;
				Path tablePath = alink.getLinkTablePath();
				SchemaNode tableNode = fieldNode.getNode(tablePath);
				context.addDependencyToInsertDeleteAndModify(tableNode);
			}
			else if (link.isTableRefInverse())
			{
				AssociationLinkByTableRefInverse alink = (AssociationLinkByTableRefInverse) link;
				Path tablePath = alink.getFieldToSourcePath();
				SchemaNode tableNode = fieldNode.getNode(tablePath).getTableNode();
				context.addDependencyToInsertDeleteAndModify(tableNode);
			}
		}
		else
		{
			SchemaInheritanceProperties sip = fieldNode.getInheritanceProperties();
			if (sip == null)
				context.addDependencyToInsertDeleteAndModify(fieldNode);
		}
	}

	/**
	 * get the value of a field in a record starting from the value context of another field
	 * @param context
	 * @param path
	 */
	public static Object getOtherFieldFromFieldContext(ValueContext context, Path path)
	{
		return context.getValue(getRelativePathFromFieldContext(context, path));
	}

	/**
	 * get the relative path of a field in a record starting from the value context of another field
	 * @param context
	 * @param path
	 */
	public static Path getRelativePathFromFieldContext(ValueContext context, Path path)
	{
		Path fieldPath = context.getNode().getPathInAdaptation();
		int depth = fieldPath.getSize();
		Path computedPath = path;
		for (int i = 0; i < depth; i++)
		{
			computedPath = Path.PARENT.add(computedPath);
		}
		return computedPath;
	}

}
