/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.extensions.*;
import com.orchestranetworks.workflow.*;

/**
 * Perform a table replication based on a named Replication configuration.
 *
 * This script task bean must be declared in module.xml as follow:
 * 
 * 	<bean
			className="com.orchestranetworks.ps.scripttask.PerformReplicationScriptTask">
			<documentation xml:lang="en-US">
				<label>Perform Replication</label>
				<description>
                    Run a table replication refresh
                </description>
			</documentation>
			<properties>
				<property name="dataspace" input="true">
					<documentation xml:lang="en-US">
						<label>dataspace</label>
						<description>
                           The Dataspace for the replication
                        </description>
					</documentation>
				</property>
				<property name="dataset" input="true">
					<documentation xml:lang="en-US">
						<label>dataset</label>
						<description>
                           The Dataset for the replication
                        </description>
					</documentation>
				</property>
				<property name="replication" input="true">
					<documentation xml:lang="en-US">
						<label>replication</label>
						<description>
                           Replication configuration within the data model
                        </description>
					</documentation>
				</property>
			</properties>
		</bean>
 */
public class PerformReplicationScriptTask extends ScriptTaskBean
{
	private String dataspace;
	private String dataset;
	private String replication;

	@Override
	public void executeScript(ScriptTaskBeanContext aContext) throws OperationException
	{
		AdaptationHome branch = aContext.getRepository()
			.lookupHome(HomeKey.forBranchName(dataspace));
		Adaptation dataSet = branch.findAdaptationOrNull(AdaptationName.forName(dataset));
		ReplicationUnit replicationUnit = ReplicationUnit
			.newReplicationUnit(ReplicationUnitKey.forName(replication), dataSet);
		ProcedureResult procResult = replicationUnit.performRefresh(aContext.getSession());
		OperationException ex = procResult.getException();
		if (ex != null)
		{
			throw ex;
		}
	}
	public String getDataspace()
	{
		return this.dataspace;
	}

	public void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	public String getDataset()
	{
		return this.dataset;
	}

	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}
	public String getReplication()
	{
		return replication;
	}
	public void setReplication(String replication)
	{
		this.replication = replication;
	}

}
