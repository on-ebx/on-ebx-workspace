package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

/**
 * Enforces that if a Country has at least one State (or Province) in the reference data,
 * that on the record referencing those tables, you are required to have a state when you
 * have a country.
 * 
 * This presumes that you have two reference tables, one for Country and one for State, and
 * that the State table references the Country via a foreign key. The table configured with
 * this constraint should have foreign keys to both Country and State.
 * 
 * These are the paths to configure it with:
 * <dl>
 *     <dt>countryPath</dt><dd>The path on the table being constrained to the country foreign key</dd>
 *     <dt>statePath</dt><dd>The path on the table being constrained to the state foreign key</dd>
 *     <dt>stateTableCountryPath</dt><dd>The path on the state table to the country foreign key</dd>
 * </dl>
 * 
 * All paths should be expressed relative to the root node, e.g <code>./country</code>
 */
public class StateRequiredWhenCountryHasStatesConstraint
	extends
	BaseConstraintOnTableWithRecordLevelCheck
{
	private Path countryPath;
	private Path statePath;
	private Path stateTableCountryPath;

	private SchemaNode countryNode;
	private SchemaNode stateNode;
	private SchemaNode stateTableNode;

	@Override
	public String toUserDocumentation(Locale locale, ValueContext valueContext)
		throws InvalidSchemaException
	{
		return createMessage(locale, valueContext, null);
	}

	@Override
	protected String checkValueContext(ValueContext valueContext)
	{
		String statePK = (String) valueContext.getValue(stateNode);
		// If there's no state selected
		if (statePK == null)
		{
			String countryPK = (String) valueContext.getValue(countryNode);
			// Show an error if there is a country selected and that country has at least one state
			if (countryPK != null && countryHasState(valueContext, countryPK))
			{
				return createMessage(Locale.getDefault(), valueContext, countryPK);
			}
		}
		return null;
	}

	// Return whether the given country has at least one state
	private boolean countryHasState(ValueContext valueContext, String countryPrimaryKey)
	{
		// Get the state table via the node that we saved in setup
		AdaptationTable stateTable = stateNode.getFacetOnTableReference().getTable(valueContext);
		// Query it for any records with the country matching the one passed in
		RequestResult requestResult = stateTable.createRequestResult(
			stateTableCountryPath.format() + "="
				+ XPathExpressionHelper.encodeLiteralStringWithDelimiters(countryPrimaryKey));
		try
		{
			// The country has a state if any result was found
			return !requestResult.isEmpty();
		}
		finally
		{
			requestResult.close();
		}
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		super.setup(context);
		countryNode = PathUtils.setupFieldNode(context, countryPath, "countryPath", true);
		if (countryNode != null && countryNode.getFacetOnTableReference() == null)
		{
			context.addError("countryPath must be a foreign key.");
		}

		stateNode = PathUtils.setupFieldNode(context, statePath, "statePath", true);
		if (stateNode != null)
		{
			SchemaFacetTableRef tableRef = stateNode.getFacetOnTableReference();
			if (tableRef == null)
			{
				context.addError("statePath must be a foreign key.");
			}
			else
			{
				stateTableNode = tableRef.getTableNode();
			}
		}

		if (stateTableCountryPath == null)
		{
			context.addError("stateTableCountryPath must be specified.");
		}
		else if (stateTableNode != null)
		{
			SchemaNode stateTableCountryNode = stateTableNode.getNode(stateTableCountryPath);
			if (stateTableCountryNode == null)
			{
				context.addError(
					stateTableCountryPath.format() + " does not exist in table "
						+ stateTableNode.getPathInSchema().format() + ".");
			}
			else if (stateTableCountryNode.getFacetOnTableReference() == null)
			{
				context.addError(
					"stateTableCountryPath in table " + stateTableNode.getPathInSchema().format()
						+ " must be a foreign key.");
			}
		}
	}

	// Create the message, which will be different based on whether it's the general documentation
	// message or an error message from execution of the constraint. Country will be passed in
	// when executing the constraint.
	private String createMessage(Locale locale, ValueContext valueContext, String countryPrimaryKey)
	{
		StringBuilder bldr = new StringBuilder(stateNode.getLabel(locale)).append(" is required");
		// If country wasn't passed in, we're creating a general message describing the constraint
		if (countryPrimaryKey == null)
		{
			bldr.append(" when the selected ")
				.append(countryNode.getLabel(locale))
				.append(" has at least one ")
				.append(stateTableNode.getLabel(locale));
		}
		// When country is passed in, we're producing a more concise user-friendly message
		// simply stating that for that country, you must have a state
		else
		{
			bldr.append(" for ").append(countryNode.getLabel(locale)).append(" ").append(
				countryNode.displayOccurrence(countryPrimaryKey, true, valueContext, locale));
		}
		bldr.append(".");
		return bldr.toString();
	}

	@Override
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return stateNode;
	}

	public Path getCountryPath()
	{
		return countryPath;
	}

	public void setCountryPath(Path countryPath)
	{
		this.countryPath = countryPath;
	}

	public Path getStatePath()
	{
		return statePath;
	}

	public void setStatePath(Path statePath)
	{
		this.statePath = statePath;
	}

	public Path getStateTableCountryPath()
	{
		return stateTableCountryPath;
	}

	public void setStateTableCountryPath(Path stateTableCountryPath)
	{
		this.stateTableCountryPath = stateTableCountryPath;
	}
}
