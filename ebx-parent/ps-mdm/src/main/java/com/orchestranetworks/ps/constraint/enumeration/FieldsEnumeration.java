package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *
 *         Enumerates the fields of a table.
 *
 *         If no data space name is specified, the data set will be searched in
 *         the current data space.
 *
 *         If no data set name is specified, the current data set name will be
 *         searched.
 *
 *         The path to the table is mandatory.
 *
 *         <pre>
 * {@code
 *          <osd:constraintEnumeration class="com.orchestranetworks.ps.constraint.enumeration.FieldsEnumeration">
 *          	<dataSpace>...</dataSpace>
 *          	<dataSet>...</dataSet>
 *          	<tablePath>...</tablePath>
 *          	<fromNode>...</fromNode>
 *  		</osd:constraintEnumeration>
 *  }
 * </pre>
 * @author MCH
 */
public class FieldsEnumeration implements ConstraintEnumeration<String>
{

	/**
	 * A map between path and nodes. Map keys are enumerated values. Map values
	 * aims to display occurrences
	 */
	private final Map<String, SchemaNode> map = new HashMap<>();

	/** The data space key. */
	private String dataSpace;

	/** The data set name. */
	private String dataSet;

	/** The path of the table from where fields must be enumerated. */
	private String tablePath;

	/** The path of the node in the table from where fields must be enumerated. */
	private String fromNode;

	/**
	 * The category or categories (semicolon separated) to consider. All fields
	 * are considered if no category is specified
	 */
	private String category;

	/** Including sub terminals nodes. */
	private boolean includingSubTerminals;

	/** Including over terminals nodes. */
	private boolean includingOverTerminals;

	/** Including selection nodes. */
	private boolean includingSelectionNodes;

	/** Including associations nodes. */
	private boolean includingAssociations;

	/** Including computed fields. */
	private boolean includingComputed;

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#checkOccurrence(java.lang.Object,
	 * com.orchestranetworks.instance.ValueContextForValidation)
	 */
	@Override
	public void checkOccurrence(final String arg0, final ValueContextForValidation arg1)
		throws InvalidSchemaException
	{
		// TODO Auto-generated method stub
	}

	/**
	 * Complete the map.
	 *
	 * @param pNode
	 *            The node from where fields must be enumerated.
	 */
	private void completeMap(final SchemaNode pNode)
	{
		this.map.clear();
		FieldsCollector collector = new FieldsCollector();
		collector.setIncludingAssociations(this.includingAssociations);
		collector.setIncludingComputed(this.includingComputed);
		collector.setIncludingOverTerminals(this.includingOverTerminals);
		collector.setIncludingSelectionNodes(this.includingSelectionNodes);
		collector.setIncludingSubTerminals(this.includingSubTerminals);
		if (this.category != null)
		{
			String[] categories = this.category.split(";");
			for (String cat : categories)
			{
				collector.addCategory(cat);
			}
		}

		List<SchemaNode> nodes = collector.collectFieldsInTable(pNode);
		for (SchemaNode node : nodes)
		{
			this.map.put(node.getPathInAdaptation().format(), node);
		}
	}

	/*
	 * @see
	 * com.orchestranetworks.schema.ConstraintEnumeration#displayOccurrence(
	 * java.lang.Object, com.orchestranetworks.instance.ValueContext,
	 * java.util.Locale)
	 */
	@Override
	public String displayOccurrence(
		final String pValue,
		final ValueContext pContext,
		final Locale pLocale) throws InvalidSchemaException
	{
		SchemaNode node = this.map.get(pValue);
		if (node != null)
		{
			return node.getLabel(pLocale);
		}
		return pValue;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory()
	{
		return this.category;
	}

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 */
	public String getDataSet()
	{
		return this.dataSet;
	}

	/**
	 * Gets the data space.
	 *
	 * @return the data space
	 */
	public String getDataSpace()
	{
		return this.dataSpace;
	}

	/**
	 * Gets the from node.
	 *
	 * @return the from node
	 */
	public String getFromNode()
	{
		return this.fromNode;
	}

	/**
	 * Gets the table path.
	 *
	 * @return the table path
	 */
	public String getTablePath()
	{
		return this.tablePath;
	}

	/*
	 * @see com.orchestranetworks.schema.ConstraintEnumeration#getValues(com.
	 * orchestranetworks.instance.ValueContext)
	 */
	@Override
	public List<String> getValues(final ValueContext pContext) throws InvalidSchemaException
	{
		if (this.dataSpace != null || this.dataSet != null)
		{
			Adaptation instance;
			try
			{
				instance = AdaptationUtil
					.getDataSetOrThrowOperationException(pContext, this.dataSpace, this.dataSet);
			}
			catch (OperationException ex)
			{
				throw new InvalidSchemaException(ex);
			}

			if (instance == null)
			{
				// TODO manage this case
				return new ArrayList<>();
			}

			SchemaNode node = instance.getSchemaNode();

			node = node.getNode(Path.parse(this.tablePath));

			if (node == null || !node.isTableNode())
			{
				// TODO manage this case
				return new ArrayList<>();
			}

			node = node.getTableOccurrenceRootNode();

			if (this.fromNode != null)
			{
				node = node.getNode(Path.parse(this.fromNode));
			}

			if (node == null)
			{
				// TODO manage this case
				return new ArrayList<>();
			}

			this.completeMap(node);
		}

		return new ArrayList<>(this.map.keySet());
	}

	/**
	 * Checks if is including associations.
	 *
	 * @return true, if is including associations
	 */
	public boolean isIncludingAssociations()
	{
		return this.includingAssociations;
	}

	/**
	 * Checks if is including computed.
	 *
	 * @return true, if is including computed
	 */
	public boolean isIncludingComputed()
	{
		return this.includingComputed;
	}

	/**
	 * Checks if is including over terminals.
	 *
	 * @return true, if is including over terminals
	 */
	public boolean isIncludingOverTerminals()
	{
		return this.includingOverTerminals;
	}

	/**
	 * Checks if is including selection nodes.
	 *
	 * @return true, if is including selection nodes
	 */
	public boolean isIncludingSelectionNodes()
	{
		return this.includingSelectionNodes;
	}

	/**
	 * Checks if is including sub terminals.
	 *
	 * @return true, if is including sub terminals
	 */
	public boolean isIncludingSubTerminals()
	{
		return this.includingSubTerminals;
	}

	/**
	 * Sets the category.
	 *
	 * @param category
	 *            the new category
	 */
	public void setCategory(final String category)
	{
		this.category = category;
	}

	/**
	 * Sets the data set.
	 *
	 * @param dataSet
	 *            the new data set
	 */
	public void setDataSet(final String dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * Sets the data space.
	 *
	 * @param dataSpace
	 *            the new data space
	 */
	public void setDataSpace(final String dataSpace)
	{
		this.dataSpace = dataSpace;
	}

	/**
	 * Sets the from node.
	 *
	 * @param fromNode
	 *            the new from node
	 */
	public void setFromNode(final String fromNode)
	{
		this.fromNode = fromNode;
	}

	/**
	 * Sets the including associations.
	 *
	 * @param includingAssociations
	 *            the new including associations
	 */
	public void setIncludingAssociations(final boolean includingAssociations)
	{
		this.includingAssociations = includingAssociations;
	}

	/**
	 * Sets the including computed.
	 *
	 * @param includingComputed
	 *            the new including computed
	 */
	public void setIncludingComputed(final boolean includingComputed)
	{
		this.includingComputed = includingComputed;
	}

	/**
	 * Sets the including over terminals.
	 *
	 * @param includingOverTerminals
	 *            the new including over terminals
	 */
	public void setIncludingOverTerminals(final boolean includingOverTerminals)
	{
		this.includingOverTerminals = includingOverTerminals;
	}

	/**
	 * Sets the including selection nodes.
	 *
	 * @param includingSelectionNodes
	 *            the new including selection nodes
	 */
	public void setIncludingSelectionNodes(final boolean includingSelectionNodes)
	{
		this.includingSelectionNodes = includingSelectionNodes;
	}

	/**
	 * Sets the including sub terminals.
	 *
	 * @param includingSubTerminals
	 *            the new including sub terminals
	 */
	public void setIncludingSubTerminals(final boolean includingSubTerminals)
	{
		this.includingSubTerminals = includingSubTerminals;
	}

	/**
	 * Sets the table path.
	 *
	 * @param tablePath
	 *            the new table path
	 */
	public void setTablePath(final String tablePath)
	{
		this.tablePath = tablePath;
	}

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#setup(com.orchestranetworks.schema
	 * .ConstraintContext)
	 */
	@Override
	public void setup(final ConstraintContext pContext)
	{

		if (this.tablePath == null)
		{
			pContext.addError("Parameter 'tablePath' is mandatory");
			return;
		}

		if (this.dataSpace != null || this.dataSet != null)
		{
			return;
		}
		SchemaNode node = pContext.getSchemaNode().getNode(Path.parse("/"));
		node = node.getNode(Path.parse(this.tablePath));

		if (node == null || !node.isTableNode())
		{
			// TODO manage this case
			return;
		}

		node = node.getTableOccurrenceRootNode();

		if (this.fromNode != null)
		{
			node = node.getNode(Path.parse(this.fromNode));
		}

		if (node == null)
		{
			// TODO manage this case
			return;
		}

		this.completeMap(node);
	}

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#toUserDocumentation(java.util
	 * .Locale, com.orchestranetworks.instance.ValueContext)
	 */
	@Override
	public String toUserDocumentation(final Locale userLocale, final ValueContext aContext)
		throws InvalidSchemaException
	{
		return null;
	}

}
