package com.orchestranetworks.ps.rest.directory;

import com.orchestranetworks.service.*;

/**
 * The Role DTO with getters and setters for the attributes of a Role (name and label).
 *
 */
public class RoleDTO
{
	private String name;
	private String label;

	public RoleDTO()
	{
	}

	public RoleDTO(String name, Session session)
	{
		this.name = name;
		if (session != null)
		{
			String roleLabel = session.getDirectory()
				.displaySpecificRole(Role.forSpecificRole(name), session.getLocale());
			if (roleLabel != null)
			{
				// Get rid of the [ and ]
				this.label = roleLabel.substring(1, roleLabel.length() - 1);
			}
		}
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
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
