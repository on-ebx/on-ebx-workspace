/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.procedure.Procedures.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public class AutoIncrementedNumberGenerator
{
	private AdaptationTable table;
	private boolean deleteRecord;

	public AutoIncrementedNumberGenerator(AdaptationTable table)
	{
		this(table, true);
	}

	public AutoIncrementedNumberGenerator(AdaptationTable table, boolean deleteRecord)
	{
		this.table = table;
		this.deleteRecord = deleteRecord;
	}

	public String assignGeneratedNumberWithPrefixAsString(
		ProcedureContext pContext,
		Adaptation record,
		Path fieldPath,
		String prefix)
		throws OperationException
	{
		Integer generatedNum;
		Session session = pContext.getSession();

		if (!record.getHome().getKey().equals(table.getContainerAdaptation().getHome().getKey()))
		{
			generatedNum = generateNumber(session);
		}
		else
		{
			generatedNum = generateNumber(pContext);
		}

		StringBuilder bldr = new StringBuilder();
		if (prefix != null)
		{
			bldr.append(prefix);
		}
		bldr.append(generatedNum);
		String generatedNumStringWithPrefix = bldr.toString();

		ModifyValuesProcedure mvp = new ModifyValuesProcedure(record);
		mvp.setValue(fieldPath, generatedNumStringWithPrefix);
		mvp.setAllPrivileges(true);
		mvp.execute(pContext);

		return generatedNumStringWithPrefix;
	}

	public void assignGeneratedNumber(ProcedureContext pContext, Adaptation record, Path fieldPath)
		throws OperationException
	{
		Integer generatedNum;
		Session session = pContext.getSession();
		if (!record.getHome().getKey().equals(table.getContainerAdaptation().getHome().getKey()))
		{
			generatedNum = generateNumber(session);
		}
		else
		{
			generatedNum = generateNumber(pContext);
		}
		ModifyValuesProcedure mvp = new ModifyValuesProcedure(record);
		mvp.setValue(fieldPath, generatedNum);
		mvp.setAllPrivileges(true);
		mvp.execute(pContext);
	}

	public Integer generateNumber(Session session) throws OperationException
	{
		GenerateNumberProcedure proc = new GenerateNumberProcedure();
		ProcedureExecutor.executeProcedure(proc, session, table.getContainerAdaptation());
		return proc.getGeneratedNum();
	}

	public Integer generateNumber(ProcedureContext pContext) throws OperationException
	{
		Adaptation record = Create.execute(pContext, table, new HashMap<Path, Object>());
		Path pkPath = table.getPrimaryKeySpec()[0];
		Integer value = (Integer) record.get(pkPath);
		if (deleteRecord)
		{
			Delete.execute(pContext, record);
		}
		return value;
	}

	private class GenerateNumberProcedure implements Procedure
	{
		private Integer generatedNum;

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			generatedNum = generateNumber(pContext);
		}

		public Integer getGeneratedNum()
		{
			return this.generatedNum;
		}
	}
}
