package com.orchestranetworks.ps.accessrule;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.annotations.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *	Allows to set many access rules on the same nodes and on the same occurrences.
 *	<p>
 *	This access rule must be set to all the nodes and the occurrences or at least on those which are subject to one of the rules set in this manager.
 *	<p>
 *	In essence, this class delegates to SchemaExtensionsContext. Access rules can be occurrence/record level rules or
 * 	node/field level rules, and can also be configured for a node and all its descendants.  Typically, an
 * 	implementation of SchemaExtensions will be defined and associated with a data model, and in its defineExtensions
 * 	definition, an AccessRulesManager will be constructed and rules will be added to it using one or more of the
 * 	'setAccessRulesOn' methods.
 *
 * @author MCH
 */

@FromEBXVersion("5.0.0")
public class AccessRulesManager implements AccessRule, AccessRuleForCreate
{

	private class ManagedAccessRule
	{
		private final AccessRule accessRule;
		private final AccessRuleForCreate accessRuleForCreate;
		private final boolean restrictive;

		public ManagedAccessRule(final AccessRule accessRule, final boolean restrictive)
		{
			super();
			this.accessRule = accessRule;
			this.accessRuleForCreate = null;
			this.restrictive = restrictive;
		}

		public ManagedAccessRule(
			final AccessRuleForCreate accessRuleForCreate,
			final boolean restrictive)
		{
			super();
			this.accessRuleForCreate = accessRuleForCreate;
			this.accessRule = null;
			this.restrictive = restrictive;
		}

		public AccessRule getAccessRule()
		{
			return this.accessRule;
		}

		public AccessRuleForCreate getAccessRuleForCreate()
		{
			return this.accessRuleForCreate;
		}

		public boolean isRestrictive()
		{
			return this.restrictive;
		}
	}

	/** The rules on nodes. */
	private final Map<Path, List<ManagedAccessRule>> rulesOnNodes = new HashMap<>();

	/** The rules on occurrences. */
	private final Map<Path, List<ManagedAccessRule>> rulesOnOccurrences = new HashMap<>();

	/** The context of the schema extension */
	private final SchemaExtensionsContext context;

	/**
	 * Instantiates a new access rules manager. By default, this constructor will be restrictive
	 *
	 * @param pContext
	 *            the context of the schema extension
	 */
	public AccessRulesManager(final SchemaExtensionsContext pContext)
	{
		this.context = pContext;
	}

	/**
	 * Adds the rule to the list of rules associated with the path. Create a new
	 * list if no one already exist.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pRule
	 *            to add the rule
	 * @param pRulesMap
	 *            the rules map
	 */
	private void addRuleToMap(
		final Path pPath,
		final ManagedAccessRule pRule,
		final Map<Path, List<ManagedAccessRule>> pRulesMap)
	{
		List<ManagedAccessRule> rules = pRulesMap.get(pPath);
		if (rules != null)
		{
			rules.add(pRule);
		}
		else
		{
			rules = new ArrayList<>();
			rules.add(pRule);
			pRulesMap.put(pPath, rules);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see
	 * com.orchestranetworks.service.AccessRule#getPermission(com.onwbp.adaptation
	 * .Adaptation, com.orchestranetworks.service.Session,
	 * com.orchestranetworks.schema.SchemaNode)
	 */
	@Override
	public AccessPermission getPermission(
		final Adaptation pAdaptation,
		final Session pSession,
		final SchemaNode pNode)
	{
		return getPermission(pAdaptation, pSession, pNode, null);
	}

	@Override
	public AccessPermission getPermission(
		final AccessRuleForCreateContext pAccessRuleForCreateContext)
	{
		return getPermission(
			null,
			pAccessRuleForCreateContext.getSession(),
			pAccessRuleForCreateContext.getNode(),
			pAccessRuleForCreateContext);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.orchestranetworks.service.AccessRule#getPermission(com.onwbp.adaptation
	 * .Adaptation, com.orchestranetworks.service.Session,
	 * com.orchestranetworks.schema.SchemaNode)
	 */
	public AccessPermission getPermission(
		final Adaptation pAdaptation,
		final Session pSession,
		final SchemaNode pNode,
		final AccessRuleForCreateContext pAccessRuleForCreateContext)
	{
		// Added check for isSystemUser due to issue in EBX 5.9.16 and EBX 6.0.5 -- could be removed when addressed in an upcoming release
		if (pSession.isSystemUser())
		{
			return AccessPermission.getReadWrite();
		}

		List<ManagedAccessRule> rules;
		if (pAdaptation != null && pAdaptation.isTableOccurrence()
			&& pNode.equals(pAdaptation.getSchemaNode()))
		{
			rules = this.rulesOnOccurrences.get(pNode.getTableNode().getPathInSchema());
		}
		else
		{
			rules = this.rulesOnNodes.get(pNode.getPathInSchema());
		}

		AccessPermission permission = null;
		boolean atLeastOneRestrictive = false;
		AccessPermission lowestRestrictivePermission = AccessPermission.getReadWrite();
		if (rules != null)
			for (ManagedAccessRule aRule : rules)
			{
				if (aRule.isRestrictive())
				{
					atLeastOneRestrictive = true;
				}

				AccessPermission thisPermission = null;
				if (aRule.getAccessRule() != null && pAdaptation != null)
				{
					thisPermission = aRule.getAccessRule()
						.getPermission(pAdaptation, pSession, pNode);
				}
				else if (aRule.getAccessRuleForCreate() != null
					&& pAccessRuleForCreateContext != null)

				{
					thisPermission = aRule.getAccessRuleForCreate()
						.getPermission(pAccessRuleForCreateContext);
				}
				else
				{
					continue;
				}
				lowestRestrictivePermission = lowestRestrictivePermission.min(thisPermission);

				if (permission == null)
				{
					permission = thisPermission;
				}
				else if (atLeastOneRestrictive)
				{
					permission = permission.min(thisPermission);
				}
				else
				{
					permission = permission.max(thisPermission);
				}
			}
		if (atLeastOneRestrictive)
		{
			return lowestRestrictivePermission;
		}
		return permission == null ? lowestRestrictivePermission : permission;
	}

	/**
	 * Sets the access rule on node. By default rules are set to restrictive mode.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pRule
	 *            the access rule
	 */
	public void setAccessRuleOnNode(final Path pPath, final AccessRule pRule)
	{
		setAccessRuleOnNode(pPath, pRule, true);
	}

	/**
	 * Sets the access rule on node.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pRule
	 *            the access rule
	 * @param restrictive
	 *            restrictive
	 */
	public void setAccessRuleOnNode(
		final Path pPath,
		final AccessRule pRule,
		final boolean restrictive)
	{
		this.context.setAccessRuleOnNode(pPath, this);
		this.addRuleToMap(pPath, new ManagedAccessRule(pRule, restrictive), this.rulesOnNodes);
	}

	/**
	 * Sets the access rule on node and all descendants. By default rules are set to restrictive mode.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pIncludeRoot
	 *            set to true to include the root node.
	 * @param pRule
	 *            the rule to set
	 */
	public void setAccessRuleOnNodeAndAllDescendants(
		final Path pPath,
		final boolean pIncludeRoot,
		final AccessRule pRule)
	{
		setAccessRuleOnNodeAndAllDescendants(pPath, pIncludeRoot, pRule, true);
	}

	/**
	 * Sets the access rule on node and all descendants.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pIncludeRoot
	 *            set to true to include the root node.
	 * @param pRule
	 *            the rule to set
	 * @param restrictive
	 *            restrictive
	 */
	public void setAccessRuleOnNodeAndAllDescendants(
		final Path pPath,
		final boolean pIncludeRoot,
		final AccessRule pRule,
		final boolean restrictive)
	{
		if (pIncludeRoot)
		{
			this.setAccessRuleOnNode(pPath, pRule, restrictive);
		}

		SchemaNode rootNode = this.context.getSchemaNode().getNode(pPath);
		if (rootNode.isTableNode())
		{
			rootNode = rootNode.getTableOccurrenceRootNode();
		}

		for (SchemaNode aNode : rootNode.getNodeChildren())
		{
			this.setAccessRuleOnNodeAndAllDescendants(
				aNode.getPathInSchema(),
				true,
				pRule,
				restrictive);
		}
	}

	/**
	 * Sets the access rule for create on node. By default rules are set to restrictive mode.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pRule
	 *            the access rule for create
	 */
	public void setAccessRuleForCreateOnNode(final Path pPath, final AccessRuleForCreate pRule)
	{
		setAccessRuleForCreateOnNode(pPath, pRule, true);
	}

	/**
	 * Sets the access rule for create on node.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pRule
	 *            the access rule
	 * @param restrictive
	 *            restrictive
	 */
	public void setAccessRuleForCreateOnNode(
		final Path pPath,
		final AccessRuleForCreate pRule,
		final boolean restrictive)
	{
		if (this.context.getSchemaNode().getNode(pPath).isTerminalValue())
		{
			this.context.setAccessRuleForCreateOnNode(pPath, this);
			this.addRuleToMap(pPath, new ManagedAccessRule(pRule, restrictive), this.rulesOnNodes);
		}
	}

	/**
	 * Sets the access rule for create on node and all descendants. By default rules are set to restrictive mode.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pIncludeRoot
	 *            set to true to include the root node.
	 * @param pRule
	 *            the rule to set
	 */
	public void setAccessRuleForCreateOnNodeAndAllDescendants(
		final Path pPath,
		final boolean pIncludeRoot,
		final AccessRuleForCreate pRule)
	{
		setAccessRuleForCreateOnNodeAndAllDescendants(pPath, pIncludeRoot, pRule, true);
	}

	/**
	 * Sets the access rule for create on node and all descendants.
	 *
	 * @param pPath
	 *            the path of the node
	 * @param pIncludeRoot
	 *            set to true to include the root node.
	 * @param pRule
	 *            the rule to set
	 * @param restrictive
	 *            restrictive
	 */
	public void setAccessRuleForCreateOnNodeAndAllDescendants(
		final Path pPath,
		final boolean pIncludeRoot,
		final AccessRuleForCreate pRule,
		final boolean restrictive)
	{
		if (pIncludeRoot)
		{
			this.setAccessRuleForCreateOnNode(pPath, pRule, restrictive);
		}

		SchemaNode rootNode = this.context.getSchemaNode().getNode(pPath);
		if (rootNode.isTableNode())
		{
			rootNode = rootNode.getTableOccurrenceRootNode();
		}

		for (SchemaNode aNode : rootNode.getNodeChildren())
		{
			this.setAccessRuleForCreateOnNodeAndAllDescendants(
				aNode.getPathInSchema(),
				true,
				pRule,
				restrictive);
		}
	}

	/**
	 * Sets the access rule on occurrence. By default rules are set to restrictive mode.
	 *
	 * @param pPath
	 *            the path
	 * @param pRule
	 *            the rule
	 */
	public void setAccessRuleOnOccurrence(final Path pPath, final AccessRule pRule)
	{
		setAccessRuleOnOccurrence(pPath, pRule, true);
	}

	/**
	 * Sets the access rule on occurrence.
	 *
	 * @param pPath
	 *            the path
	 * @param pRule
	 *            the rule
	 * @param restrictive
	 *            restrictive
	 */
	public void setAccessRuleOnOccurrence(
		final Path pPath,
		final AccessRule pRule,
		final boolean restrictive)
	{
		this.context.setAccessRuleOnOccurrence(pPath, this);
		this.addRuleToMap(
			pPath,
			new ManagedAccessRule(pRule, restrictive),
			this.rulesOnOccurrences);
	}
}
