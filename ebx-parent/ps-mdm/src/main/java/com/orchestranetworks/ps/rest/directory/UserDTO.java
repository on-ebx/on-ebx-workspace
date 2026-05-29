package com.orchestranetworks.ps.rest.directory;

/**
 * The User DTO with getters and setters for the attributes of a User (userId and label).
 *
 */
public class UserDTO
{
	private String userId;
	private String label;

	/**
	 * @return the userId
	 */
	public String getUserId()
	{
		return userId;
	}
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId)
	{
		this.userId = userId;
	}
	/**
	 * @return the label
	 */
	public String getLabel()
	{
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

}
