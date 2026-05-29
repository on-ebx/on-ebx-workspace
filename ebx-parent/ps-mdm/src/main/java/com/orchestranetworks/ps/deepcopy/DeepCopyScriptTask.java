/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class DeepCopyScriptTask extends ScriptTaskBean
{
	private String recordXPath;
	private String dataSet;
	private String fromDataSpace;
	private String toDataSpace;
	private String copiedRecordXPath;

	protected AdaptationHome fromDataSpaceRef;
	protected AdaptationHome toDataSpaceRef;
	protected Adaptation recordToCopy;

	protected abstract DeepCopyConfigFactory getDeepCopyConfigFactory();

	protected DeepCopyDataModifier getDeepCopyDataModifier()
	{
		return new DefaultDeepCopyDataModifier();
	}

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		Repository repo = context.getRepository();
		initVariables(repo);
		executeDeepCopyProcedure(repo, context.getSession());
	}

	protected void initVariables(Repository repo) throws OperationException
	{
		if (recordXPath == null)
		{
			throw OperationException.createError("recordXPath parameter is required.");
		}
		if (fromDataSpace == null)
		{
			throw OperationException.createError("fromDataSpace parameter is required.");
		}
		if (dataSet == null)
		{
			throw OperationException.createError("dataSet parameter is required.");
		}
		fromDataSpaceRef = repo.lookupHome(HomeKey.forBranchName(fromDataSpace));
		if (fromDataSpaceRef == null)
		{
			throw OperationException
				.createError("fromDataSpace '" + fromDataSpace + "' not found.");
		}
		if (toDataSpace == null)
		{
			toDataSpaceRef = null;
		}
		else
		{
			toDataSpaceRef = repo.lookupHome(HomeKey.forBranchName(toDataSpace));
			if (toDataSpaceRef == null)
			{
				throw OperationException
					.createError("toDataSpace '" + toDataSpace + "' not found.");
			}
		}
		Adaptation dataSetRef = fromDataSpaceRef
			.findAdaptationOrNull(AdaptationName.forName(dataSet));
		if (dataSetRef == null)
		{
			throw OperationException.createError("dataSet '" + dataSet + "' not found.");
		}
		recordToCopy = AdaptationUtil.getRecord(recordXPath, dataSetRef, true, true);
	}

	protected void executeDeepCopyProcedure(Repository repo, Session session)
		throws OperationException
	{
		DeepCopyScriptTaskProcedure proc = new DeepCopyScriptTaskProcedure();
		ProcedureExecutor.executeProcedure(
			proc,
			session,
			toDataSpaceRef == null ? recordToCopy.getHome() : toDataSpaceRef);
		Adaptation copiedRecord = proc.getNewRecord();
		copiedRecordXPath = copiedRecord.toXPathExpression();
	}

	protected Adaptation deepCopy(ProcedureContext pContext) throws OperationException
	{
		DeepCopyImpl service = new DeepCopyImpl(getDeepCopyConfigFactory());
		AdaptationHome dataSpace = pContext.getAdaptationHome();
		if (dataSpace.getKey().equals(toDataSpaceRef.getKey()))
		{
			return service.deepCopy(
				pContext,
				recordToCopy,
				toDataSpaceRef,
				getDeepCopyDataModifier(),
				null,
				null);
		}
		return service.deepCopy(
			recordToCopy,
			toDataSpaceRef,
			getDeepCopyDataModifier(),
			null,
			null,
			pContext.getSession());
	}

	public String getRecordXPath()
	{
		return this.recordXPath;
	}

	public void setRecordXPath(String recordXPath)
	{
		this.recordXPath = recordXPath;
	}

	public String getDataSet()
	{
		return this.dataSet;
	}

	public void setDataSet(String dataSet)
	{
		this.dataSet = dataSet;
	}

	public String getFromDataSpace()
	{
		return this.fromDataSpace;
	}

	public void setFromDataSpace(String fromDataSpace)
	{
		this.fromDataSpace = fromDataSpace;
	}

	public String getToDataSpace()
	{
		return this.toDataSpace;
	}

	public void setToDataSpace(String toDataSpace)
	{
		this.toDataSpace = toDataSpace;
	}

	public String getCopiedRecordXPath()
	{
		return this.copiedRecordXPath;
	}

	public void setCopiedRecordXPath(String copiedRecordXPath)
	{
		this.copiedRecordXPath = copiedRecordXPath;
	}

	private class DeepCopyScriptTaskProcedure implements Procedure
	{
		private Adaptation newRecord;

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			newRecord = deepCopy(pContext);
		}

		public Adaptation getNewRecord()
		{
			return this.newRecord;
		}
	}
}
