/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.condition;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.*;

/**
 * This is a condition that checks if any changes have occurred in a data space.
 * It can check at either a data set, table, or record level.
 * <code>dataSpace</code> and <code>dataSet</code> parameters are required.
 * If <code>recordXPath</code> is provided, <code>tablePath</code> is ignored.
 * Both <code>recordXPath</code> and <code>tablePath</code> allow multiple values separated by a semicolon.
 */
public class DoesDataSpaceContainChangesCondition extends ConditionBean
{
	private static final String SEPARATOR = ";";

	private String dataSpace;
	private String dataSet;
	private String tableXPath;
	private String recordXPath;

	@Override
	public boolean evaluateCondition(ConditionBeanContext context) throws OperationException
	{
		if (dataSpace == null)
		{
			throw OperationException.createError("dataSpace must be specified.");
		}
		if (dataSet == null)
		{
			throw OperationException.createError("dataSet must be specified.");
		}

		AdaptationHome dataSpaceRef = context.getRepository().lookupHome(
			HomeKey.forBranchName(dataSpace));
		if (dataSpaceRef == null)
		{
			throw OperationException.createError("dataSpace " + dataSpace + " does not exist.");
		}

		AdaptationName dataSetName = AdaptationName.forName(dataSet);
		Adaptation dataSetRef = dataSpaceRef.findAdaptationOrNull(dataSetName);
		if (dataSetRef == null)
		{
			throw OperationException.createError("dataSet " + dataSet
				+ " does not exist in dataSpace " + dataSpace + ".");
		}

		AdaptationHome initialSnapshot = dataSpaceRef.getParent();
		Adaptation initialDataSet = initialSnapshot.findAdaptationOrNull(dataSetName);
		if (initialDataSet == null)
		{
			throw OperationException.createError("dataSet " + dataSet
				+ " does not exist in initial snapshot of dataSpace " + dataSpace);
		}

		if (recordXPath == null)
		{
			if (tableXPath == null)
			{
				return !DifferenceHelper.compareInstances(initialDataSet, dataSetRef, false)
					.isEmpty();
			}
			String[] pathStrArr = tableXPath.split(SEPARATOR);
			for (int i = 0; i < pathStrArr.length; i++)
			{
				String pathStr = pathStrArr[i].trim();
				if (pathStr.length() == 0)
				{
					throw OperationException.createError("tableXPath contains empty paths.");
				}
				Path path = Path.parse(pathStr);

				AdaptationTable table = dataSetRef.getTable(path);
				if (table == null)
				{
					throw OperationException.createError("table " + pathStr
						+ " does not exist in dataSet " + dataSet + " in dataSpace " + dataSpace
						+ ".");
				}

				AdaptationTable initialTable = initialDataSet.getTable(path);
				if (initialTable == null)
				{
					throw OperationException.createError("table " + pathStr
						+ " does not exist in dataSet " + dataSet
						+ " in initial snapshot of dataSpace " + dataSpace + ".");
				}

				if (!DifferenceHelper.compareAdaptationTables(initialTable, table, false).isEmpty())
				{
					return true;
				}
			}
			return false;
		}
		String[] recordStrArr = recordXPath.split(SEPARATOR);
		for (int i = 0; i < recordStrArr.length; i++)
		{
			String recordStr = recordStrArr[i];
			Adaptation recordRef = AdaptationUtil.getRecord(recordStr, dataSetRef, true, true);
			Adaptation initialRecord = AdaptationUtil.getRecord(
				recordStr,
				initialDataSet,
				true,
				true);
			if (!DifferenceHelper.compareOccurrences(initialRecord, recordRef, false).isEmpty())
			{
				return true;
			}
		}
		return false;
	}

	public String getDataSpace()
	{
		return this.dataSpace;
	}

	public void setDataSpace(String dataSpace)
	{
		this.dataSpace = dataSpace;
	}

	public String getDataSet()
	{
		return this.dataSet;
	}

	public void setDataSet(String dataSet)
	{
		this.dataSet = dataSet;
	}

	public String getTableXPath()
	{
		return this.tableXPath;
	}

	public void setTableXPath(String tableXPath)
	{
		this.tableXPath = tableXPath;
	}

	public String getRecordXPath()
	{
		return this.recordXPath;
	}

	public void setRecordXPath(String recordXPath)
	{
		this.recordXPath = recordXPath;
	}
}
