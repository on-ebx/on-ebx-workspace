package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * This trigger should be placed on a field in a table that needs to be defaulted from the same field
 * in the parent record (in the same table) when displaying a new record form.
 * 
 * If the parent value isn't filled in on showing the form, then it will be checked before creating
 * the record (on save) but only when configured to be via a foreign key to the parent (see below)
 * and only when <code>defaultBeforeCreate</code> is <code>true</code>.
 * You don't have access to the parent record via association unless you're in a new record context
 * so it's impossible to default it before create for the other configurations.
 * 
 * If the table has a foreign key to the parent, then <code>parentPath</code> should be configured with
 * that foreign key, relative to the table root (e.g. <code>"./parent"</code>).
 * 
 * If the table is linked to its parent via a link table, then it will pick up the parent record from the context.
 * However, since it could be linked via multiple relationships, not all of which are specifying the parent
 * relationship, more information must be supplied giving details of where the create is coming from.
 * 
 * If it's being created via a foreign key from a link table, then the parameter <code>referencingForeignKeyPaths</code>
 * must be specified with the list of foreign key fields and <code>referencingTableParentPaths</code> must be
 * specified with the respective list of foreign key fields from the link table back to the parent. Each foreign key path
 * must be a full path and each parent path must be a relative path. Each list uses {@link PathUtils#DEFAULT_SEPARATOR}
 * as its separator.
 * 
 * For example, say there's a table Account with a parent/child relationship to itself via an AccountRelationship table.
 * AccountRelationship has a foreign key to the parent called parentAccount and a foreign key to the child called
 * childAccount. In this case, <code>referencingForeignKeyPaths</code> would be <code>"/root/AccountRelationship/childAccount"</code>
 * and <code>referencingTableParentPaths</code> would be <code>"./parentAccount"</code>. If there were a second link table
 * being configured, you'd add its details to both parameters, separated by {@link PathUtils#DEFAULT_SEPARATOR}.
 * 
 * 
 * If it's being created via an association over a link table (rather than via the foreign key from the link table itself),
 * then <code>referencingAssociationPaths</code> must be specified with the list of associations. Each path would be the
 * full path to the association node.
 * 
 * Using the same example from above, say Account has an association over the AccountRelationship table for all of its child
 * Accounts called childAccounts. <code>referencingAssociationPaths</code> would be <code>"/root/Account/childAccounts"</code>.
 * 
 * It's possible you may need to specify the two foreign key related parameters, as well as the association related parameter,
 * so that it works regardless of which context you're coming from.
 * 
 * Note that there's currently no way to retrieve the parent from the context when it's being created
 * over a link table via a hierarchy view. That only works when created via an association table.
 */
public class DefaultFieldFromParentTableTrigger extends BaseTableTriggerEnforcingPermissions
{
	private Path parentPath;
	private String referencingForeignKeyPaths;
	private String referencingTableParentPaths;
	private String referencingAssociationPaths;
	private boolean defaultBeforeCreate;

	private List<Path> referencingForeignKeyPathList;
	private List<Path> referencingTableParentPathList;
	private Set<Path> referencingAssociationPathSet;
	private SchemaNode node;

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		super.handleNewContext(context);

		ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
		// Get the value of the node that the trigger was put on, and default it if
		// its value isn't already set
		Object value = valueContext.getValue(node);
		if (value == null)
		{
			Object parentValue = null;
			// If no parent path was specified, then we need to check if we're in a fk or association context
			if (parentPath == null)
			{
				ValueContext referencingFKValueContext = context.getReferencingRecordContext();
				// If not in a fk context then check if it's an association context
				if (referencingFKValueContext == null)
				{
					ValueContext referencingAssocValueContext = context
						.getAssociationSourceRecordContext();
					if (referencingAssocValueContext != null)
					{
						// Get the association field for this link and see if it's in the list we configured
						// the trigger for
						AssociationLink associationLink = context.getAssociationLink();
						Path assocPath = associationLink.getOwnerNode().getPathInSchema();
						if (referencingAssociationPathSet.contains(assocPath))
						{
							parentValue = referencingAssocValueContext.getValue(node);
						}
					}
				}
				// Otherwise we're in a fk context
				else if (referencingForeignKeyPathList != null)
				{
					Path fkPath = context.getReferencingNode().getPathInSchema();
					// Get the index of the fk reference in the configured list,
					// and if it's found
					int ind = referencingForeignKeyPathList.indexOf(fkPath);
					if (ind != -1)
					{
						// Get the related parent path for that index in the parent path list
						Path referencingTableParentPath = referencingTableParentPathList.get(ind);
						// Follow that fk to the parent record and get the value from it
						Adaptation parent = AdaptationUtil
							.followFK(referencingFKValueContext, referencingTableParentPath);
						if (parent != null)
						{
							parentValue = parent.get(node);
						}
					}
				}
			}
			// There's a parent foreign key path so simply get the value from following the foreign key
			else
			{
				Adaptation parent = AdaptationUtil.followFK(valueContext, parentPath);
				if (parent != null)
				{
					parentValue = parent.get(node);
				}
			}

			if (parentValue != null)
			{
				valueContext
					.setValueEnablingPrivilegeForNode(parentValue, node.getPathInAdaptation());
			}
		}
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);

		if (defaultBeforeCreate)
		{
			ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
			Object value = valueContext.getValue(node);
			if (value == null)
			{
				Adaptation parent = AdaptationUtil.followFK(valueContext, parentPath);
				if (parent != null)
				{
					Object parentValue = parent.get(node);
					if (parentValue != null)
					{
						valueContext.setValueEnablingPrivilegeForNode(
							parentValue,
							node.getPathInAdaptation());
					}
				}
			}
		}
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);

		referencingForeignKeyPathList = null;
		referencingTableParentPathList = null;
		referencingAssociationPathSet = null;

		// Store the node that the trigger is on for later use
		node = context.getSchemaNode();

		if (parentPath == null)
		{
			if (referencingForeignKeyPaths == null)
			{
				if (referencingTableParentPaths == null)
				{
					if (referencingAssociationPaths == null)
					{
						context.addError(
							"Either parentPath or referencingForeignKeyPaths / referencingTableParentPaths or referencingAssociationPaths must be specified.");
					}
				}
				else
				{
					context.addError(
						"referencingForeignKeyPaths must be specified when referencingTableParentPaths is specified.");
				}
			}
			else
			{
				if (referencingTableParentPaths == null)
				{
					context.addError(
						"referencingTableParentPaths must be specified when referencingForeignKeyPaths is specified.");
				}
				else
				{
					PathUtils.validatePathString(
						context,
						referencingForeignKeyPaths,
						null,
						new String[] { Path.ROOT.format() });
					referencingForeignKeyPathList = PathUtils
						.convertStringToPathList(referencingForeignKeyPaths, null);

					PathUtils.validatePathString(
						context,
						referencingTableParentPaths,
						null,
						new String[] { Path.SELF.format() });
					referencingTableParentPathList = PathUtils
						.convertStringToPathList(referencingTableParentPaths, null);
				}
			}
			if (referencingAssociationPaths != null)
			{
				PathUtils.validatePathString(
					context,
					referencingAssociationPaths,
					null,
					new String[] { Path.ROOT.format() });
				referencingAssociationPathSet = PathUtils
					.convertStringToPathSet(referencingAssociationPaths, null);
			}

			if (defaultBeforeCreate)
			{
				context
					.addError("defaultBeforeCreate can only be true when parentPath is specified.");
			}
		}
		else
		{
			if (referencingForeignKeyPaths == null && referencingTableParentPaths == null
				&& referencingAssociationPaths == null)
			{
				PathUtils.setupFieldNode(context, parentPath, "parentPath", false, false);
			}
			else
			{
				context.addError(
					"When parentPath is specified, referencingForeignKeyPaths, referencingTableParentPaths, and referencingAssociationPaths must not be specified.");
			}
		}
	}

	public Path getParentPath()
	{
		return parentPath;
	}

	public void setParentPath(Path parentPath)
	{
		this.parentPath = parentPath;
	}

	public String getReferencingForeignKeyPaths()
	{
		return referencingForeignKeyPaths;
	}

	public void setReferencingForeignKeyPaths(String referencingForeignKeyPaths)
	{
		this.referencingForeignKeyPaths = referencingForeignKeyPaths;
	}

	public String getReferencingTableParentPaths()
	{
		return referencingTableParentPaths;
	}

	public void setReferencingTableParentPaths(String referencingTableParentPaths)
	{
		this.referencingTableParentPaths = referencingTableParentPaths;
	}

	public String getReferencingAssociationPaths()
	{
		return referencingAssociationPaths;
	}

	public void setReferencingAssociationPaths(String referencingAssociationPaths)
	{
		this.referencingAssociationPaths = referencingAssociationPaths;
	}

	public boolean isDefaultBeforeCreate()
	{
		return defaultBeforeCreate;
	}

	public void setDefaultBeforeCreate(boolean defaultBeforeCreate)
	{
		this.defaultBeforeCreate = defaultBeforeCreate;
	}
}
