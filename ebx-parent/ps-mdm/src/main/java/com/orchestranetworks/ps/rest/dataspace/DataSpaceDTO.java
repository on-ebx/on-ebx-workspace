package com.orchestranetworks.ps.rest.dataspace;

/**
 * The DataSpace DTO with getters and setters for the attributes of a DataSpace (dataSpaceId and dataSpaceLabel).
 *
 */
public class DataSpaceDTO
{
	private String dataSpaceId;
	private String dataSpaceLabel;

	/**
	 * @return the dataSpaceId
	 */
	public String getDataSpaceId()
	{
		return dataSpaceId;
	}
	/**
	 * @param dataSpaceId the dataSpaceId to set
	 */
	public void setDataSpaceId(String dataSpaceId)
	{
		this.dataSpaceId = dataSpaceId;
	}
	/**
	 * @return the dataSpaceLabel
	 */
	public String getDataSpaceLabel()
	{
		return dataSpaceLabel;
	}
	/**
	 * @param dataSpaceLabel the dataSpaceLabel to set
	 */
	public void setDataSpaceLabel(String dataSpaceLabel)
	{
		this.dataSpaceLabel = dataSpaceLabel;
	}

}
