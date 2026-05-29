package com.orchestranetworks.ps.trigger;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * Encapsulates some logic common to the "acyclic" classes: {@link com.orchestranetworks.ps.constraint.AcyclicConstraint},
 * {@link AcyclicTableTrigger}, and {@link com.orchestranetworks.ps.tablereffilter.AcyclicTableRefFilter}.
 * Essentially this holds the parameter values and the values derived from those, has the method for doing the setup,
 * and has methods for creating error messages.
 * 
 * The parameter <code>childPath</code> is only for use when this is utilized by a link table. In those cases, you don't want
 * to compare against the current record, you want to compare against the child record on the current record.
 */
public class AcyclicConfig
{
	private String pathsToParentString;
	private List<Path> parentPath;
	private BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents;
	private UnaryFunction<Adaptation, String> getRecordLabel;
	private String pathDisplay;
	private Path childPath;
	private static final String USER_DOC_MESSAGE = "Cycle not allowed in 'parent' path {0}";
	private static final String CHECK_VALUE_MESSAGE = "Cycle in 'parent' path {0} detected ({1})";
	private SchemaNode validateStartNode;
	private boolean pathsValidationIndeterminate;

	public void setup(SchemaNodeContext context)
	{
		validateStartNode = null;
		pathsValidationIndeterminate = false;
		SchemaNode childNode = PathUtils
			.setupFieldNode(context, context.getSchemaNode(), childPath, "childPath", false, false);
		if (childNode != null && childNode.getFacetOnTableReference() == null)
		{
			context.addError("childPath does not specify a foreign key.");
		}
		else if (getParents == null && pathsToParentString != null)
		{
			Locale locale = Locale.getDefault();
			this.parentPath = PathUtils.convertStringToPathList(pathsToParentString, null);
			SchemaNode currentNode = context.getSchemaNode();
			SchemaFacetTableRef currentNodeTableRef = currentNode.getFacetOnTableReference();
			// Start validating from the current node when the current node itself isn't a
			// foreign key (which will happen when it's a filter). Otherwise start from the foreign table.
			if (currentNodeTableRef == null)
			{
				validateStartNode = currentNode;
			}
			else
			{
				validateStartNode = currentNodeTableRef.getTableNode().getTableOccurrenceRootNode();
			}
			List<SchemaNode> nodes = PathUtils.validatePath(validateStartNode, parentPath);
			if (nodes.size() < parentPath.size())
			{
				// Sometimes it couldn't find the node because we're just not guaranteed in setup that everything
				// has been compiled in the appropriate order in order to find it (when associations are involved).
				// So give a warning and set a flag to true that we can check later and revalidate at runtime.
				context.addWarning(
					"Path " + parentPath.get(nodes.size()).format()
						+ " not found. During schema compilation, it is not always possible to validate association/selection links, so this doesn't necessarily indicate that something's wrong.");
				pathsValidationIndeterminate = true;
			}
			else
			{
				SchemaNode lastNode = nodes.get(nodes.size() - 1);
				if (!AdaptationUtil.isRelationshipNode(lastNode))
					context.addError(
						"Field " + lastNode.getLabel(locale)
							+ " does not specify a related record.");
			}
			final Path[] paths = parentPath.toArray(new Path[0]);
			getParents = new BinaryFunction<Adaptation, ValueContext, List<Adaptation>>()
			{
				@Override
				public List<Adaptation> evaluate(Adaptation record, ValueContext valueContext)
				{
					List<Adaptation> results;
					if (valueContext != null)
					{
						results = AdaptationUtil.evaluatePath(valueContext, paths);
					}
					else
					{
						results = AdaptationUtil.evaluatePath(record, paths);
					}
					return results;
				}
			};
			String parentPathString;
			if (parentPath == null || parentPath.isEmpty())
				parentPathString = "<unknown>";
			else
			{
				StringBuilder pathString = new StringBuilder();
				for (SchemaNode node : nodes)
				{
					if (pathString.length() > 0)
						pathString.append(PathUtils.DEFAULT_SEPARATOR);
					pathString.append(node.getLabel(locale));
				}
				parentPathString = pathString.toString();
			}
			pathDisplay = parentPathString;
		}
	}

	/**
	 * Create a message used by the user documentation methods
	 * 
	 * @return the message
	 */
	public String createUserDocMessage()
	{
		return MessageFormat.format(USER_DOC_MESSAGE, pathDisplay);
	}

	/**
	 * Create a message used by the constraint when it checks the occurrence
	 * 
	 * @param cycle the cycle that was detected
	 * @return the message
	 */
	public String createCheckContextMessage(String cycle)
	{
		return MessageFormat.format(CHECK_VALUE_MESSAGE, pathDisplay, cycle);
	}

	/**
	 * Get whether the paths validation was indeterminate at setup time, meaning we couldn't determine
	 * if they were valid or not
	 * 
	 * @return whether the paths validation was indeterminate
	 */
	public boolean isPathsValidationIndeterminate()
	{
		return pathsValidationIndeterminate;
	}

	/**
	 * Revalidate the paths and reset the state of whether the paths validation was indeterminate
	 * 
	 * @return the error message, if the paths aren't valid, otherwise <code>null</code>
	 */
	public String revalidatePaths()
	{
		List<SchemaNode> nodes = PathUtils.validatePath(validateStartNode, parentPath);
		if (nodes.size() < parentPath.size())
		{
			return "Path " + parentPath.get(nodes.size()).format() + " not found.";
		}
		pathsValidationIndeterminate = false;
		return null;
	}

	public List<Path> getParentPath()
	{
		return parentPath;
	}

	public String getPathDisplay()
	{
		return pathDisplay;
	}

	public String getPathsToParentString()
	{
		return pathsToParentString;
	}

	public void setPathsToParentString(String pathsToParentString)
	{
		this.pathsToParentString = pathsToParentString;
	}

	public BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getGetParents()
	{
		return getParents;
	}

	public void setGetParents(BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents)
	{
		this.getParents = getParents;
	}

	public UnaryFunction<Adaptation, String> getGetRecordLabel()
	{
		return getRecordLabel;
	}

	public void setGetRecordLabel(UnaryFunction<Adaptation, String> getRecordLabel)
	{
		this.getRecordLabel = getRecordLabel;
	}

	public Path getChildPath()
	{
		return childPath;
	}

	public void setChildPath(Path childPath)
	{
		this.childPath = childPath;
	}
}
