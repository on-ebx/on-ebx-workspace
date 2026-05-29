package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/** 
 * Simple trigger is configured with paths to inherited fields that we would wish to reset to inherited
 * whenever the record is modified.  If no paths are supplied (via pathsToResetString), all inherited fields of the table will be
 * reset.  
 * 
 * Additionally, this trigger can be configured with paths to reset to inherited only when their 
 * value matches the value that would be inherited (pathsToResetOnlyWhenValueMatchesString).
 * 
 * Can also now be configured with a list of Paths Not to Reset, so that if you want to reserrt all inhererited fields EXCEPT certain ones, that can be specficied
 * 
 * When specifying paths in the Data Model, you must specify "/<field name>"  (do NOT specify a "." prefix)
 */
public class ResetInheritedFieldsTrigger extends TableTrigger
{
	protected String pathsToResetString;
	protected String pathsNotToResetString;
	protected String pathsToResetOnlyWhenValueMatchesString;
	private List<Path> pathsToReset;
	private List<Path> pathsNotToReset;
	private List<Path> pathsToResetOnlyWhenValueMatches;

	public String getPathsToResetString()
	{
		return pathsToResetString;
	}

	public String getPathsNotToResetString()
	{
		return pathsNotToResetString;
	}

	public void setPathsToResetString(String pathsToResetString)
	{
		this.pathsToResetString = pathsToResetString;
	}

	public void setPathsNotToResetString(String pathsNotToResetString)
	{
		this.pathsNotToResetString = pathsNotToResetString;
	}

	public String getPathsToResetOnlyWhenValueMatchesString()
	{
		return pathsToResetOnlyWhenValueMatchesString;
	}

	public void setPathsToResetOnlyWhenValueMatchesString(
		String pathsToResetOnlyWhenValueMatchesString)
	{
		this.pathsToResetOnlyWhenValueMatchesString = pathsToResetOnlyWhenValueMatchesString;
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		SchemaNode node = context.getSchemaNode().getTableNode().getTableOccurrenceRootNode();

		if (pathsToResetString != null)
		{
			// Populate PathsToReset from parameter set in Data Model
			pathsToReset = PathUtils.convertStringToPathList(pathsToResetString, null);
			checkInherited(context, node, pathsToReset);
		}
		else
		{
			// if no paths are specified, assume user wants all inherited fields reset
			List<Path> paths = new ArrayList<>();
			AdaptationUtil.collectInheritedFields(node, paths);
			this.pathsToReset = paths;
		}

		if (pathsToResetOnlyWhenValueMatchesString != null)
		{
			// Populate PathsToResetOnlyWhenValueMatches from parameter set in Data Model
			pathsToResetOnlyWhenValueMatches = PathUtils
				.convertStringToPathList(pathsToResetOnlyWhenValueMatchesString, null);
			checkInherited(context, node, pathsToResetOnlyWhenValueMatches);
			if (pathsToResetString == null)
			{
				// Remove from PathsToReset Collection
				pathsToReset.removeAll(pathsToResetOnlyWhenValueMatches);
			}
			else if (!CollectionUtils.intersection(pathsToResetOnlyWhenValueMatches, pathsToReset)
				.isEmpty())
			{
				context
					.addError("PathsToReset and pathsToResetOnlyWhenValueMatches cannot overlap.");
			}
		}

		if (pathsNotToResetString != null)
		{
			// Populate PathsNotToReset from parameter set in Data Model
			pathsNotToReset = PathUtils.convertStringToPathList(pathsNotToResetString, null);
			checkInherited(context, node, pathsNotToReset);
			if (pathsToResetString == null)
			{
				// Remove from PathsToReset Collection
				pathsToReset.removeAll(pathsNotToReset);
			}
			else if (!CollectionUtils.intersection(pathsNotToReset, pathsToReset).isEmpty())
			{
				context.addError("PathsToReset and PathsNotToReset cannot overlap.");
			}
			if (pathsToResetOnlyWhenValueMatchesString != null) // check when specified in the Data Model
			{
				if (!CollectionUtils.intersection(pathsNotToReset, pathsToResetOnlyWhenValueMatches)
					.isEmpty())
				{
					context.addError(
						"PathsToResetOnlyWhenValueMatches and PathsNotToReset cannot overlap.");
				}
			}
		}
	}

	private static void checkInherited(
		TriggerSetupContext context,
		SchemaNode node,
		List<Path> paths)
	{
		// check each path represents an inherited field
		for (Path path : paths)
		{
			SchemaNode fieldNode = node.getNode(Path.SELF.add(path));
			if (fieldNode == null || fieldNode.getInheritanceProperties() == null)
				context.addError("Path " + path.format() + " is not an inherited field");
		}
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext aContext) throws OperationException
	{
		super.handleBeforeModify(aContext);
		doReset(aContext.getOccurrenceContextForUpdate());
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext aContext) throws OperationException
	{
		super.handleBeforeCreate(aContext);
		doReset(aContext.getOccurrenceContextForUpdate());
	}

	private void doReset(ValueContextForUpdate context) throws OperationException
	{
		for (Path path : pathsToReset)
		{
			context.setValueEnablingPrivilegeForNode(AdaptationValue.INHERIT_VALUE, path);
		}

		if (pathsToResetOnlyWhenValueMatches != null)
		{
			for (Path path : pathsToResetOnlyWhenValueMatches)
			{
				Object value = context.getValue(path);
				context.setValueEnablingPrivilegeForNode(AdaptationValue.INHERIT_VALUE, path);
				Object inheritValue = context.getValue(path);
				if (!Objects.equals(value, inheritValue))
				{
					context.setValueEnablingPrivilegeForNode(value, path);
				}
			}
		}
	}
}
