package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 *
 *         Enumerates the tables of a given data model location indicated by another node
 *
 *         <pre>
 * {@code
 *          <osd:constraintEnumeration class="com.orchestranetworks.ps.constraint.enumeration.DataModelTablesEnumeration">
 *          	<dataModelNode>...</dataModelNode>
 *  		</osd:constraintEnumeration>
 *  }
 * </pre>
 * @author MCH
 */
public class DataModelTablesEnumeration implements ConstraintEnumeration<String>
{
	/**
	 * A map between path and nodes. Map keys are enumerated values. Map values
	 * aims to display occurrences
	 */
	private final Map<String, SchemaNode> map = new HashMap<>();

	/** The path of the node from where to get the schema location. */
	private Path dataModelNode;

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
		if (this.map.isEmpty())
		{
			String schemaLocationStr = (String) pContext.getValue(this.dataModelNode);
			if (!(schemaLocationStr == null))
			{
				Adaptation dataset = DataModelEnumeration
					.getAnyDataset(pContext.getHome().getRepository(), schemaLocationStr);
				this.completeMap(dataset.getSchemaNode());
			}
		}
		SchemaNode node = this.map.get(pValue);
		if (node != null)
		{
			return node.getLabel(pLocale);
		}
		return pValue + "";
	}

	public Path getDataModelNode()
	{
		return this.dataModelNode;
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
		String schemaLocationStr = (String) pContext.getValue(this.dataModelNode);
		if (schemaLocationStr == null)
		{
			return new ArrayList<>();
		}
		Adaptation dataset = DataModelEnumeration
			.getAnyDataset(pContext.getHome().getRepository(), schemaLocationStr);
		this.completeMap(dataset.getSchemaNode());

		return new ArrayList<>(this.map.keySet());
	}

	public void setDataModelNode(final Path dataModelNode)
	{
		this.dataModelNode = dataModelNode;
	}

	/*
	 * @see
	 * com.orchestranetworks.schema.Constraint#setup(com.orchestranetworks.schema
	 * .ConstraintContext)
	 */
	@Override
	public void setup(final ConstraintContext pContext)
	{
		// this.completeMap(pContext.getSchemaNode());

	}

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
}
