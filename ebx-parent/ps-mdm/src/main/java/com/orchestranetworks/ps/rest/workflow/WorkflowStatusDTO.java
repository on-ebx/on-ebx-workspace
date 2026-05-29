package com.orchestranetworks.ps.rest.workflow;

/**
 * The Workflow Status DTO with the processInstance related attributes.
 *
 */
public class WorkflowStatusDTO
{

	private String processInstanceLabel;
	private String status;
	private String createdDate;
	private String modifiedDate;

	/**
	 * @return the processInstanceLabel
	 */
	public String getProcessInstanceLabel()
	{
		return processInstanceLabel;
	}
	/**
	 * @param processInstanceLabel the processInstanceLabel to set
	 */
	public void setProcessInstanceLabel(String processInstanceLabel)
	{
		this.processInstanceLabel = processInstanceLabel;
	}
	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}
	/**
	 * @return the createdDate
	 */
	public String getCreatedDate()
	{
		return createdDate;
	}
	/**
	 * @param createdDate the createdDate to set
	 */
	public void setCreatedDate(String createdDate)
	{
		this.createdDate = createdDate;
	}
	/**
	 * @return the modifiedDate
	 */
	public String getModifiedDate()
	{
		return modifiedDate;
	}
	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(String modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

}
