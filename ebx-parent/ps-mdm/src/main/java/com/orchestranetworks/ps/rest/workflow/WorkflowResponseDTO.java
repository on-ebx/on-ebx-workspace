package com.orchestranetworks.ps.rest.workflow;

/**
 * The Workflow Response DTO with the processInstanceId and the processInstanceKey attributes.
 *
 */
public class WorkflowResponseDTO
{

	private Integer processInstanceId;
	private String processInstanceKey;

	/**
	 * @return the processInstanceId
	 */
	public Integer getProcessInstanceId()
	{
		return processInstanceId;
	}
	/**
	 * @param processInstanceId the processInstanceId to set
	 */
	public void setProcessInstanceId(Integer processInstanceId)
	{
		this.processInstanceId = processInstanceId;
	}
	/**
	 * @return the processInstanceKey
	 */
	public String getProcessInstanceKey()
	{
		return processInstanceKey;
	}
	/**
	 * @param processInstanceKey the processInstanceKey to set
	 */
	public void setProcessInstanceKey(String processInstanceKey)
	{
		this.processInstanceKey = processInstanceKey;
	}

}
