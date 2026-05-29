/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

/**
 */
public class DeepCopyConfig
{
	private Map<Path, DeepCopyConfig> foreignKeyConfigMap;
	private Map<Path, DeepCopyConfig> selectionNodeConfigMap;
	private Map<Path, DeepCopyConfig> associationConfigMap;
	private Path pathToParentSelectionNodeOrAssociation;
	private Path associationSortOrderField = null; //optional -- to be specified if using the FixSortOrderTrigger
	private AdaptationTable associationLinkTable;
	private Set<Path> pathsToIndicateWithCopy;
	private boolean enforcePermission;
	private DeepCopyRule rule;
	private DeepCopyDataModifier dataModifier;

	public DeepCopyConfig()
	{
		this(
			new HashMap<Path, DeepCopyConfig>(),
			new HashMap<Path, DeepCopyConfig>(),
			new HashMap<Path, DeepCopyConfig>(),
			null,
			new HashSet<Path>(),
			false);
	}

	public DeepCopyConfig(
		Map<Path, DeepCopyConfig> foreignKeyConfigMap,
		Map<Path, DeepCopyConfig> selectionNodeConfigMap,
		Map<Path, DeepCopyConfig> associationConfigMap,
		Path pathToParentSelectionNodeOrAssociation)
	{
		this(
			foreignKeyConfigMap,
			selectionNodeConfigMap,
			associationConfigMap,
			pathToParentSelectionNodeOrAssociation,
			new HashSet<Path>(),
			false);
	}

	public DeepCopyConfig(
		Map<Path, DeepCopyConfig> foreignKeyConfigMap,
		Map<Path, DeepCopyConfig> selectionNodeConfigMap,
		Map<Path, DeepCopyConfig> associationConfigMap,
		Path pathToParentSelectionNodeOrAssociation,
		Set<Path> pathsToIndicateWithCopy,
		boolean enforcePermission)
	{
		this.foreignKeyConfigMap = foreignKeyConfigMap;
		this.selectionNodeConfigMap = selectionNodeConfigMap;
		this.associationConfigMap = associationConfigMap;
		this.pathToParentSelectionNodeOrAssociation = pathToParentSelectionNodeOrAssociation;
		this.pathsToIndicateWithCopy = pathsToIndicateWithCopy;
		this.enforcePermission = enforcePermission;
	}

	public Map<Path, DeepCopyConfig> getForeignKeyConfigMap()
	{
		return this.foreignKeyConfigMap;
	}

	public void setForeignKeyConfigMap(Map<Path, DeepCopyConfig> foreignKeyConfigMap)
	{
		this.foreignKeyConfigMap = foreignKeyConfigMap;
	}

	public Map<Path, DeepCopyConfig> getSelectionNodeConfigMap()
	{
		return this.selectionNodeConfigMap;
	}

	public void setSelectionNodeConfigMap(Map<Path, DeepCopyConfig> selectionNodeConfigMap)
	{
		this.selectionNodeConfigMap = selectionNodeConfigMap;
	}

	public Map<Path, DeepCopyConfig> getAssociationConfigMap()
	{
		return this.associationConfigMap;
	}

	public void setAssociationConfigMap(Map<Path, DeepCopyConfig> associationConfigMap)
	{
		this.associationConfigMap = associationConfigMap;
	}

	public Path getPathToParentSelectionNodeOrAssociation()
	{
		return this.pathToParentSelectionNodeOrAssociation;
	}

	public void setPathToParentSelectionNodeOrAssociation(
		Path pathToParentSelectionNodeOrAssociation)
	{
		this.pathToParentSelectionNodeOrAssociation = pathToParentSelectionNodeOrAssociation;
	}

	public Path getAssociationSortOrderField()
	{
		return associationSortOrderField;
	}

	public void setAssociationSortOrderField(Path associationSortField)
	{
		this.associationSortOrderField = associationSortField;
	}

	public AdaptationTable getAssociationLinkTable()
	{
		return this.associationLinkTable;
	}

	public void setAssociationLinkTable(AdaptationTable associationLinkTable)
	{
		this.associationLinkTable = associationLinkTable;
	}

	public Set<Path> getPathsToIndicateWithCopy()
	{
		return this.pathsToIndicateWithCopy;
	}

	public void setPathsToIndicateWithCopy(Set<Path> pathsToIndicateWithCopy)
	{
		this.pathsToIndicateWithCopy = pathsToIndicateWithCopy;
	}

	public boolean isEnforcePermission()
	{
		return this.enforcePermission;
	}

	public void setEnforcePermission(boolean enforcePermission)
	{
		this.enforcePermission = enforcePermission;
	}

	public DeepCopyDataModifier getDataModifier()
	{
		return dataModifier;
	}

	public DeepCopyRule getRule()
	{
		return rule;
	}

	public void setRule(DeepCopyRule rule)
	{
		this.rule = rule;
	}

	public void setDataModifier(DeepCopyDataModifier dataModifier)
	{
		this.dataModifier = dataModifier;
	}
}
