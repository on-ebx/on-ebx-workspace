package com.orchestranetworks.ps.service;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * When you add fields to the data model, these will appear as "INHERITED/unset"
 * in the corresponding table in the data set.  This service can be executed on a table to initialize any "INHERITED/Unset" fields
 * to their default value if one is defined in the data model, an empty collection if it is a repeating attribute, or to a Null Value otherwise.
 * EXCEPTION:  If a field is defined in the Data Model as an "Inherited Field", then any fields that currently have a value of "INHERITED/Unset" will be left alone
 */
public class InitializeDefaultFieldValues extends AbstractUserService<TableViewEntitySelection>
{

	@Override
	public void execute(Session session) throws OperationException
	{
		ProcedureExecutor.executeProcedure(
			this::initDefaults,
			session,
			context.getEntitySelection().getDataset());
	}

	private void initDefaults(ProcedureContext pContext) throws OperationException
	{
		TableViewEntitySelection selection = context.getEntitySelection();
		AdaptationTable table = selection.getTable();
		SchemaNode rootNode = table.getTableOccurrenceRootNode();
		List<SchemaNode> fieldsToDefault = new ArrayList<>();
		collectFields(rootNode.getNodeChildren(), fieldsToDefault);
		
		RequestResult allResults = null;
		RequestResult selectedResults = selection.getSelectedRecords().execute();
		try
		{
			RequestResult rr;
			if (selectedResults.isEmpty())
			{
				allResults = selection.getAllRecords().execute();
				rr = allResults;
			}
			else
			{
				rr = selectedResults;
			}
			initDefaults(pContext, fieldsToDefault, rr);
		}
		finally
		{
			selectedResults.close();
			if (allResults != null)
			{
				allResults.close();
			}
		}
	}
	
	private void initDefaults(
		ProcedureContext pContext,
		List<SchemaNode> fieldsToDefault,
		RequestResult requestResult)
		throws OperationException
	{
		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		Adaptation next;
		while ((next = requestResult.nextAdaptation()) != null)
		{
			initDefaults(pContext, next, fieldsToDefault, mvp);
		}
	}

	
	private void initDefaults(
		ProcedureContext pContext,
		Adaptation next,
		List<SchemaNode> fieldsToDefault,
		ModifyValuesProcedure mvp)
		throws OperationException
	{
		mvp.clearValues();
		mvp.setAdaptation(next);
		mvp.setAllPrivileges(true);
		for (SchemaNode schemaNode : fieldsToDefault)
		{
			Path path = schemaNode.getPathInAdaptation();
			if (AdaptationValue.INHERIT_VALUE.equals(next.getValueWithoutResolution(path)))
				mvp.setValue(path, schemaNode.getDefaultValue());
		}
		if (!mvp.isEmpty())
			mvp.execute(pContext);
	}

	private void collectFields(SchemaNode[] nodes, List<SchemaNode> fieldsToDefault)
	{
		for (SchemaNode node : nodes)
		{
			if (node.isTerminalValue() && node.getInheritanceProperties() == null)
			{
				fieldsToDefault.add(node);
			}
			if (node.isComplex())
				collectFields(node.getNodeChildren(), fieldsToDefault);
		}
	}
}
