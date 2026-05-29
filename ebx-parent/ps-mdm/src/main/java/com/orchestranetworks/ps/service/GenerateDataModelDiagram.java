package com.orchestranetworks.ps.service;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.jsp.*;

/**
 * @deprecated Servlets (that use {@link ServiceContext}) are deprecated. This is no longer needed anyway because generating a data model
 *             is a built-in feature of EBX now.
 */
@Deprecated
public class GenerateDataModelDiagram
{

	private final List<String> results = new LinkedList<>();
	private final List<String> relationships = new LinkedList<>();

	private ServiceContext serviceContext;
	private final boolean labels;
	private final int attributeLimit;

	public GenerateDataModelDiagram()
	{
		this(true, 0);
	}

	public GenerateDataModelDiagram(boolean labels, int attributeLimit)
	{
		this.labels = labels;
		this.attributeLimit = attributeLimit;
	}

	public void execute(HttpServletRequest request, JspWriter out) throws ServletException
	{
		serviceContext = ServiceContext.getServiceContext(request);

		Adaptation dataset = serviceContext.getCurrentAdaptation();

		helperExploreNode(dataset.getSchemaNode());

		try
		{
			display(out);
		}
		catch (IOException e)
		{
			throw new ServletException(e);
		}
	}

	private void display(JspWriter out) throws IOException
	{
		try
		{
			StringBuilder yumlString = new StringBuilder();
			String tablesYuml = StringUtils.join(results, ", ");
			String relationshipsYuml = StringUtils.join(relationships, ", ");

			yumlString.append(tablesYuml);
			yumlString.append(", ");
			yumlString.append(relationshipsYuml);

			out.append(
				String.format(
					"<img src=\"http://yuml.me/diagram/class/%s\" >",
					yumlString.toString()));
			// out.append(String.format("<div>%s</div>", yumlString.toString()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// <img src="http://yuml.me/diagram/class/[User]" >

		// <img
		// src="http://yuml.me/diagram/scruffy/class/[User|+Forename+;Surname;+HashedPassword;-Salt|+Login();+Logout()]"
		// >

		// <img src="http://yuml.me/diagram/scruffy/class/[Customer]->[Billing Address]" >

	}

	public void helperExploreNode(SchemaNode schemaNode)
	{
		if (schemaNode.isTableNode())
		{
			helperExploreTableNode(schemaNode);
		}
		else if (schemaNode.isComplex())
		{
			SchemaNode[] child = schemaNode.getNodeChildren();

			for (SchemaNode childNode : child)
			{
				helperExploreNode(childNode);
			}
		}
	}

	public void helperExploreTableNode(SchemaNode tableNode)
	{
		String formatTable = "[%s%s]";

		TableResult tableResult = helperExploreTableNodeAttributes(tableNode);
		String tableName = cleanLabel(tableNode);
		results.add(String.format(formatTable, tableName, tableResult.attributes));

		String formatRelationship = "[%s]-%s>[%s]";
		for (Relationship relationship : tableResult.relationships)
		{
			relationships.add(
				String.format(
					formatRelationship,
					tableName,
					relationship.field,
					cleanLabel(relationship.foreigntable)));
		}
	}

	private String cleanLabel(SchemaNode node)
	{
		if (labels)
		{
			String label = node.getLabel(serviceContext.getLocale());
			return StringUtils.replaceEachRepeatedly(
				label,
				new String[] { "#", "?", "(", ")", "-", "%", ">", "<" },
				new String[] { "", "", "", "", "", "", "", "" });
		}
		return node.getPathInAdaptation().format();
	}

	public static class TableResult
	{
		public String attributes;
		public List<Relationship> relationships;
	}

	public static class Relationship
	{
		public SchemaNode foreigntable;
		public String field;
	}

	public TableResult helperExploreTableNodeAttributes(SchemaNode tableNode)
	{
		StringBuilder sb = new StringBuilder();

		SchemaNode recordStructure = tableNode.getTableOccurrenceRootNode();
		SchemaNode[] children = recordStructure.getNodeChildren();

		List<SchemaNode> fields = new LinkedList<>();
		List<Relationship> relationships = new LinkedList<>();

		for (SchemaNode child : children)
		{
			helperExploreTableNodeComplexAttribute(child, fields, relationships);
		}

		if (attributeLimit > 0)
		{
			int count = 0;
			for (SchemaNode field : fields)
			{
				if (count == 0)
				{
					sb.append("|");
				}
				else
				{
					sb.append(";");
				}
				if (count > attributeLimit)
				{
					sb.append("_fields cropped_");
					break;
				}
				sb.append(cleanLabel(field));
				count++;
			}
		}

		TableResult result = new TableResult();
		result.attributes = sb.toString();
		result.relationships = relationships;

		return result;
	}

	public void helperExploreTableNodeComplexAttribute(
		SchemaNode complexRecordField,
		List<SchemaNode> results,
		List<Relationship> relationships)
	{

		if (complexRecordField.isComplex())
		{
			SchemaNode[] fields = complexRecordField.getNodeChildren();

			if (fields != null)
			{
				for (SchemaNode field : fields)
				{
					if (field.isComplex())
					{
						helperExploreTableNodeComplexAttribute(field, results, relationships);
					}
					else
					{
						results.add(field);
						handleRelationship(field, relationships);
					}
				}
			}
		}
		else
		{
			results.add(complexRecordField);
			handleRelationship(complexRecordField, relationships);
		}

	}

	private void handleRelationship(SchemaNode field, List<Relationship> relationships)
	{
		if (field.getFacetOnTableReference() != null)
		{
			Relationship relationship = new Relationship();
			relationship.field = field.getLabel(serviceContext.getLocale());
			relationship.foreigntable = field.getFacetOnTableReference().getTableNode();
			relationships.add(relationship);
		}
	}
}
