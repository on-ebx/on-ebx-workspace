package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 *
 *         Enumerates the tables of a given data set in a given data space from
 *         a given node.
 *
 *         If no data space name is specified, the data set will be searched in
 *         the current data space.
 *
 *         If no data set name is specified, the current data set name will be
 *         searched.
 *
 *         dataspace and dataset can be either set to a constant or fields by another node.
 *         In this case a relative path must be set to pathToDataset or pathToDataspace
 *
 *         <pre>
 * {@code
 *          <osd:constraintEnumeration class="com.orchestranetworks.ps.constraint.enumeration.TablesEnumeration">
 *          	<dataspace>...</dataspace>
 *          	<pathToDataspace>...</pathToDataspace>
 *          	<dataset>...</dataset>
 *          	<pathToDataset>...</pathToDataset>
 *          	<fromNode>...</fromNode>
 *  		</osd:constraintEnumeration>
 *  }
 * </pre>
 * @author MCH
 */
public class TablesEnumeration implements ConstraintEnumeration<String>
{
	/**
	 * A map between path and nodes. Map keys are enumerated values. Map values
	 * aims to display occurrences
	 */
	private final Map<String, SchemaNode> map = new HashMap<>();

	/** The data space key. */
	private String dataspace;

	/** The path to the data space. */
	private Path pathToDataspace;

	/** The data set name. */
	private String dataset;

	/** The path to the data set. */
	private Path pathToDataset;

	/** The path of the node from where tables must be enumerated. */
	private String fromNode;

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#checkOccurrence(java.lang.Object,
	 * com.orchestranetworks.instance.ValueContextForValidation)
	 */
	@Override
	public void checkOccurrence(final String pValue, final ValueContextForValidation pContext)
		throws InvalidSchemaException
	{
	}

	/**
	 * Complete the map.
	 *
	 * @param pNode
	 *            The node from where tables must be enumerated.
	 */
	private void completeMap(final SchemaNode pNode)
	{
		this.map.clear();
		List<SchemaNode> nodes = SchemaUtils.getTableNodes(pNode);
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
		return pValue + "";
	}

	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 */
	public String getDataset()
	{
		return this.dataset;
	}

	/**
	 * Gets the data space.
	 *
	 * @return the data space
	 */
	public String getDataspace()
	{
		return this.dataspace;
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

	public Map<String, SchemaNode> getMap()
	{
		return this.map;
	}

	public Path getPathToDataset()
	{
		return this.pathToDataset;
	}

	public Path getPathToDataspace()
	{
		return this.pathToDataspace;
	}

	/*
	 * @see com.orchestranetworks.schema.ConstraintEnumeration#getValues(com.
	 * orchestranetworks.instance.ValueContext)
	 */
	/*
	 * @see com.orchestranetworks.schema.ConstraintEnumeration#getValues(com.
	 * orchestranetworks.instance.ValueContext)
	 */
	@Override
	public List<String> getValues(final ValueContext pContext) throws InvalidSchemaException
	{
		String branchName = this.dataspace;
		String adaptationName = this.dataset;
		if (StringUtils.isBlank(branchName) && this.pathToDataspace != null)
		{
			branchName = (String) pContext.getValue(this.pathToDataspace);
		}

		if (StringUtils.isBlank(adaptationName) && this.pathToDataset != null)
		{
			adaptationName = (String) pContext.getValue(this.pathToDataset);
		}

		/*
		 * If both data space and data set are null, map is initialized in setup
		 * as the list can only change when schema is compiled.
		 */
		if (branchName != null || adaptationName != null)
		{
			Adaptation instance;
			try
			{
				instance = AdaptationUtil
					.getDataSetOrThrowOperationException(pContext, branchName, adaptationName);
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
	 * Sets the data set.
	 *
	 * @param dataset
	 *            the new data set
	 */
	public void setDataset(final String dataset)
	{
		this.dataset = dataset;
	}

	/**
	 * Sets the data space.
	 *
	 * @param dataspace
	 *            the new data space
	 */
	public void setDataspace(final String dataspace)
	{
		this.dataspace = dataspace;
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

	public void setPathToDataset(final Path pathToDataset)
	{
		this.pathToDataset = pathToDataset;
	}

	public void setPathToDataspace(final Path pathToDataspace)
	{
		this.pathToDataspace = pathToDataspace;
	}

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#setup(com.orchestranetworks.schema
	 * .ConstraintContext)
	 */
	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#setup(com.orchestranetworks.schema
	 * .ConstraintContext)
	 */
	@Override
	public void setup(final ConstraintContext pContext)
	{
		if (this.dataspace != null || this.dataset != null)
		{
			return;
		}
		SchemaNode node = pContext.getSchemaNode().getNode(Path.parse("/"));

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
	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#toUserDocumentation(java.util
	 * .Locale, com.orchestranetworks.instance.ValueContext)
	 */
	@Override
	public String toUserDocumentation(final Locale pLocale, final ValueContext pContext)
		throws InvalidSchemaException
	{
		return null;
	}
}
