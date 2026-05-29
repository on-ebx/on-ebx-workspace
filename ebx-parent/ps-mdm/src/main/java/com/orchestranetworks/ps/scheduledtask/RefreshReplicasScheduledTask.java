/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scheduledtask;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.extensions.*;

/**
 * A scheduled task to refresh replicas for a given dataspace, dataset and replication unit.
 * To specify multiple, use a comma-separated list. The number of data spaces, data sets,
 * and replication unit names must match and be in the right order.
 * (i.e. if you specify two data sets for the same data space, you should repeat the data space twice)
 */
public class RefreshReplicasScheduledTask extends ScheduledTask
{
	private static final char SEPARATOR = ',';

	private String dataSpaceName;
	private String dataSetName;
	private String replicationUnitName;

	@Override
	public void execute(ScheduledExecutionContext context)
		throws OperationException, ScheduledTaskInterruption
	{
		Repository repo = context.getRepository();
		Session session = context.getSession();

		String[] dataSpaceNameArr = getValueAsArray(dataSpaceName);
		String[] dataSetNameArr = getValueAsArray(dataSetName);
		String[] replicationUnitNameArr = getValueAsArray(replicationUnitName);

		for (int i = 0; i < dataSpaceNameArr.length; i++)
		{
			refreshReplication(
				dataSpaceNameArr[i],
				dataSetNameArr[i],
				replicationUnitNameArr[i],
				repo,
				session);
		}
	}

	@Override
	public void validate(ValueContextForValidation context)
	{
		boolean checkSizes = true;

		if (dataSpaceName == null)
		{
			context.addError("dataSpaceName must be specified.");
			checkSizes = false;
		}
		if (dataSetName == null)
		{
			context.addError("dataSetName must be specified.");
			checkSizes = false;
		}
		if (replicationUnitName == null)
		{
			context.addError("replicationUnitName must be specified.");
			checkSizes = false;
		}

		if (checkSizes)
		{
			String[] dataSpaceNameArr = getValueAsArray(dataSpaceName);
			String[] dataSetNameArr = getValueAsArray(dataSetName);
			String[] replicationUnitNameArr = getValueAsArray(replicationUnitName);

			if (dataSpaceNameArr.length != dataSetNameArr.length
				|| dataSpaceNameArr.length != replicationUnitNameArr.length)
			{
				context.addError(
					"Number of specified data spaces, data sets, and replication units must match.");
			}
		}
	}

	private static void refreshReplication(
		String dSpace,
		String dSet,
		String rUnit,
		Repository repo,
		Session session)
		throws OperationException
	{
		AdaptationHome dataSpace = AdaptationUtil
			.getDataSpaceOrThrowOperationException(repo, dSpace);
		Adaptation dataSet = AdaptationUtil.getDataSetOrThrowOperationException(dataSpace, dSet);

		ReplicationUnit replicationUnit = ReplicationUnit
			.newReplicationUnit(ReplicationUnitKey.forName(rUnit), dataSet);
		ProcedureResult procResult = replicationUnit.performRefresh(session);
		OperationException ex = procResult.getException();
		if (ex != null)
		{
			throw ex;
		}
	}

	private static String[] getValueAsArray(String value)
	{
		return StringUtils.split(value, SEPARATOR);
	}

	public String getDataSpaceName()
	{
		return dataSpaceName;
	}

	public void setDataSpaceName(String dataSpaceName)
	{
		this.dataSpaceName = dataSpaceName;
	}

	public String getDataSetName()
	{
		return dataSetName;
	}

	public void setDataSetName(String dataSetName)
	{
		this.dataSetName = dataSetName;
	}

	public String getReplicationUnitName()
	{
		return replicationUnitName;
	}

	public void setReplicationUnitName(String replicationUnitName)
	{
		this.replicationUnitName = replicationUnitName;
	}
}
