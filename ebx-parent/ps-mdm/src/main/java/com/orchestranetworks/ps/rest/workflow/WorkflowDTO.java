package com.orchestranetworks.ps.rest.workflow;

import java.util.*;

/**
 * The Workflow DTO with getters and setters for the data context variables from the json payload.
 *
 */
public class WorkflowDTO
{

	private String workflowInstanceLabel;
	private String workflowDescription;
	private String dataSet;
	private String record;
	private String workingDataSpace;
	private String masterDataSpace;
	private String xpathToTable;

	private Map<String, String> additionalDataContextVariables;

	/**
	 * @return the workflowInstanceLabel
	 */
	public String getWorkflowInstanceLabel()
	{
		return workflowInstanceLabel;
	}

	/**
	 * @param workflowInstanceLabel the workflowInstanceLabel to set
	 */
	public void setWorkflowInstanceLabel(String workflowInstanceLabel)
	{
		this.workflowInstanceLabel = workflowInstanceLabel;
	}

	/**
	 * @return the workflowDescription
	 */
	public String getWorkflowDescription()
	{
		return workflowDescription;
	}

	/**
	 * @param workflowDescription the workflowDescription to set
	 */
	public void setWorkflowDescription(String workflowDescription)
	{
		this.workflowDescription = workflowDescription;
	}

	/**
	 * @return the dataSet
	 */
	public String getDataSet()
	{
		return dataSet;
	}

	/**
	 * @param dataSet the dataSet to set
	 */
	public void setDataSet(String dataSet)
	{
		this.dataSet = dataSet;
	}

	/**
	 * @return the record
	 */
	public String getRecord()
	{
		return record;
	}

	/**
	 * @param record the record to set
	 */
	public void setRecord(String record)
	{
		this.record = record;
	}

	/**
	 * @return the workingDataSpace
	 */
	public String getWorkingDataSpace()
	{
		return workingDataSpace;
	}

	/**
	 * @param workingDataSpace the workingDataSpace to set
	 */
	public void setWorkingDataSpace(String workingDataSpace)
	{
		this.workingDataSpace = workingDataSpace;
	}

	/**
	 * @return the masterDataSpace
	 */
	public String getMasterDataSpace()
	{
		return masterDataSpace;
	}

	/**
	 * @param masterDataSpace the masterDataSpace to set
	 */
	public void setMasterDataSpace(String masterDataSpace)
	{
		this.masterDataSpace = masterDataSpace;
	}

	/**
	 * @return the xpathToTable
	 */
	public String getXpathToTable()
	{
		return xpathToTable;
	}

	/**
	 * @param xpathToTable the xpathToTable to set
	 */
	public void setXpathToTable(String xpathToTable)
	{
		this.xpathToTable = xpathToTable;
	}

	/**
	 * @return the additionalDataContextVariables
	 */
	public Map<String, String> getAdditionalDataContextVariables()
	{
		return additionalDataContextVariables;
	}

	/**
	 * @param additionalDataContextVariables the additionalDataContextVariables to set
	 */
	public void setAdditionalDataContextVariables(
		Map<String, String> additionalDataContextVariables)
	{
		this.additionalDataContextVariables = additionalDataContextVariables;
	}

}
