/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This class accomplishes basically the same thing as a uniqueness constraint, except that it enforces for a particular field that
 * there can't be two with the given value. It will allow duplicates if the value is different than that.
 * This is probably most useful for boolean fields, to ensure that only one row can have <code>true</code> for example.
 * The other fields to match on can be specified, so that you can say only restrict it when all of those other fields' values are the same.
 */
public class NoDuplicateRecordsForValueConstraint extends BaseConstraintOnTableWithRecordLevelCheck
{
	private static final String PREDICATE_TOKEN = "<V>";

	private Path restrictedFieldPath;
	private String matchingFieldPaths;
	private String uniqueValue;

	private SchemaNode restrictedFieldNode;
	private List<SchemaNode> matchingFieldNodes;
	private Object uniqueValueObject;
	private String predicatePattern;
	private String message;

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return message;
	}

	// This is overridden to be more efficient than what super class does.
	// We don't need to loop through every record calling its checkRecord because that will run multiple queries.
	// Instead, we can run one query for all matching the uniqueValue, keep track of the records found, and compare against that list as we go.
	@Override
	public void checkTable(ValueContextForValidationOnTable context)
	{
		HashSet<Adaptation> recordsFound = new HashSet<>();

		// Search for all matching the uniqueValue
		RequestResult requestResult = context.getTable().createRequestResult(
			createFieldEqualsPredicateString(restrictedFieldNode, uniqueValue));

		try
		{
			// Loop through all records matching that predicate
			for (Adaptation record; (record = requestResult.nextAdaptation()) != null;)
			{
				boolean matchFound = false;
				Iterator<Adaptation> recordsFoundIter = recordsFound.iterator();
				// For each record, loop through all records previously found by this process until a match is found
				while (!matchFound && recordsFoundIter.hasNext())
				{
					Adaptation foundRecord = recordsFoundIter.next();
					// Start by considering this record to be a match
					matchFound = true;
					Iterator<SchemaNode> schemaNodeIter = matchingFieldNodes.iterator();
					// Loop through all of the fields that need to match to be considered a duplicate until you find one that doesn't match
					while (matchFound && schemaNodeIter.hasNext())
					{
						SchemaNode node = schemaNodeIter.next();
						Object value = record.get(node);
						// If the value is different from this record, then it's not a match
						if (value != null && !value.equals(foundRecord.get(node)))
						{
							matchFound = false;
						}
					}
				}
				// If a match was found for this record, then show an error for it
				if (matchFound)
				{
					context.addMessage(
						record,
						AdaptationUtil.createUserMessage(
							record,
							message,
							getSeverity(record.createValueContext())));
				}
				// If it wasn't a match, then add the record to the set of records found so we'll consider it as we go through the rest of the records
				else
				{
					recordsFound.add(record);
				}
			}
		}
		finally
		{
			requestResult.close();
		}
	}

	@Override
	protected String checkValueContext(ValueContext recordContext)
	{
		Object restrictedFieldValue = recordContext.getValue(restrictedFieldNode);
		// There won't be any conflicts if there's no value for the field we're restricting
		// or if it's not equal to the unique value we're looking for
		if (restrictedFieldValue == null || !restrictedFieldValue.equals(uniqueValueObject))
		{
			return null;
		}

		String predicate;
		if (!matchingFieldNodes.isEmpty())
		{
			String[] segments = predicatePattern.split(PREDICATE_TOKEN);
			Iterator<SchemaNode> iter = matchingFieldNodes.iterator();

			StringBuilder bldr = new StringBuilder();

			for (String segment : segments)
			{
				bldr.append(segment);
				if (iter.hasNext())
				{
					SchemaNode node = iter.next();
					Object matchingFieldValue = recordContext.getValue(node);
					// If there's no value for one of the nodes to match on, then there won't be another matching record so
					// just return without even running the query
					if (matchingFieldValue == null)
					{
						return null;
					}
					bldr.append(AdaptationUtil.valueString(node, matchingFieldValue, true));
				}
			}
			predicate = bldr.toString();
		}
		else
		{
			predicate = predicatePattern;
		}

		AdaptationTable table = recordContext.getAdaptationTable();
		Adaptation savedRecord = AdaptationUtil.getRecordForValueContext(recordContext);
		RequestResult requestResult = table.createRequestResult(predicate);
		try
		{
			// If the record hasn't been saved yet
			if (savedRecord == null)
			{
				// If there's at least one record found, then it's a duplicate
				if (!requestResult.isEmpty())
				{
					return message;
				}
			}
			// Else the record has already been saved, so it's a duplicate if more than 1 was found (1 other + this one),
			// or if one was found and the old value of the restricted node didn't match the unique value we're looking for, but the new value does.
			// In other words, if a record was found that's not this one and this one WILL be a duplicate when it's saved.
			// This allows the constraint to error out in the pre-save validation as well as post-save.
			else if (requestResult.isSizeGreaterOrEqual(2) || (!requestResult.isEmpty()
				&& !uniqueValueObject.equals(savedRecord.get(restrictedFieldNode))
				&& uniqueValueObject.equals(restrictedFieldValue)))
			{
				return message;
			}
		}
		finally
		{
			requestResult.close();
		}
		return null;
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		restrictedFieldNode = PathUtils
			.setupFieldNode(context, restrictedFieldPath, "restrictedFieldPath", true);

		matchingFieldNodes = new ArrayList<>();
		if (matchingFieldPaths != null)
		{
			SchemaNode contextNode = context.getSchemaNode();
			List<Path> matchingFieldPathList = PathUtils
				.convertStringToPathList(matchingFieldPaths, null);
			for (Path path : matchingFieldPathList)
			{
				SchemaNode node = contextNode.getNode(path);
				if (node == null)
				{
					context
						.addError("matchingFieldPaths path " + path.format() + " does not exist.");
				}
				else
				{
					matchingFieldNodes.add(node);
				}
			}
		}

		if (uniqueValue == null)
		{
			context.addError("uniqueValue must be specified.");
		}
		else
		{
			try
			{
				uniqueValueObject = AdaptationUtil
					.convertStringToValueObject(restrictedFieldNode, uniqueValue);
			}
			catch (ParseException ex)
			{
				context.addError(
					"uniqueValue " + uniqueValue + " isn't a valid value for the node "
						+ restrictedFieldPath.format() + ".");
			}
		}

		predicatePattern = createPredicatePattern();
		message = createMessage(Locale.getDefault());
	}

	private String createPredicatePattern()
	{
		StringBuilder bldr = new StringBuilder();
		Iterator<SchemaNode> iter = matchingFieldNodes.iterator();
		while (iter.hasNext())
		{
			SchemaNode node = iter.next();
			bldr.append(createFieldEqualsPredicateString(node, PREDICATE_TOKEN));
			bldr.append(" and ");
		}

		String value;
		if (AdaptationUtil.isValueQuotedInPredicate(restrictedFieldNode))
		{
			value = XPathExpressionHelper.encodeLiteralStringWithDelimiters(uniqueValue);
		}
		else
		{
			value = uniqueValue;
		}
		bldr.append(createFieldEqualsPredicateString(restrictedFieldNode, value));

		return bldr.toString();
	}

	private static String createFieldEqualsPredicateString(SchemaNode node, String value)
	{
		StringBuilder bldr = new StringBuilder();

		String pathStr = Path.SELF.add(node.getPathInAdaptation()).format();
		SchemaTypeName typeName = node.getXsTypeName();

		if (SchemaTypeName.XS_DATE.equals(typeName) || SchemaTypeName.XS_DATETIME.equals(typeName)
			|| SchemaTypeName.XS_TIME.equals(typeName))
		{
			bldr.append("date-equal(");
			bldr.append(pathStr);
			bldr.append(",");
			bldr.append(value);
			bldr.append(")");
		}
		else
		{
			bldr.append(pathStr);
			bldr.append("=");
			bldr.append(value);
		}
		return bldr.toString();
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return restrictedFieldNode;
	}

	private String createMessage(Locale locale)
	{
		StringBuilder bldr = new StringBuilder();
		bldr.append("Only one record can exist with ");
		bldr.append(restrictedFieldNode.getLabel(locale));
		bldr.append(" equal to ");
		bldr.append(uniqueValue);
		int numOfNodes = matchingFieldNodes.size();
		if (numOfNodes > 0)
		{
			bldr.append(" for each ");

			if (numOfNodes > 1)
			{
				bldr.append("combination of ");
			}
			for (int i = 0; i < numOfNodes; i++)
			{
				SchemaNode node = matchingFieldNodes.get(i);
				bldr.append(node.getLabel(locale));
				if (numOfNodes > 2 && i < numOfNodes - 1)
				{
					bldr.append(", ");
				}
				if (i == numOfNodes - 2)
				{
					bldr.append(" and ");
				}
			}
		}
		bldr.append(".");
		return bldr.toString();
	}

	public Path getRestrictedFieldPath()
	{
		return this.restrictedFieldPath;
	}

	public void setRestrictedFieldPath(Path restrictedFieldPath)
	{
		this.restrictedFieldPath = restrictedFieldPath;
	}

	public String getMatchingFieldPaths()
	{
		return this.matchingFieldPaths;
	}

	public void setMatchingFieldPaths(String matchingFieldPaths)
	{
		this.matchingFieldPaths = matchingFieldPaths;
	}

	public String getUniqueValue()
	{
		return this.uniqueValue;
	}

	public void setUniqueValue(String uniqueValue)
	{
		this.uniqueValue = uniqueValue;
	}
}
