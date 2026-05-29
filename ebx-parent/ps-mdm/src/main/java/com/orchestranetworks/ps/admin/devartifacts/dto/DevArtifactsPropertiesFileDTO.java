package com.orchestranetworks.ps.admin.devartifacts.dto;

import java.util.*;

import org.apache.commons.lang3.builder.*;

/**
 * A {@link DevArtifactsDTO} that specifically is for use when Dev Artifacts
 * is configured from a properties file
 */
public class DevArtifactsPropertiesFileDTO extends DevArtifactsDTO
{
	private String propertiesFileSystemProperty;

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().appendSuper(super.hashCode())
			.append(propertiesFileSystemProperty)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof DevArtifactsPropertiesFileDTO)
		{
			DevArtifactsPropertiesFileDTO other = (DevArtifactsPropertiesFileDTO) obj;
			return super.equals(obj) && Objects
				.equals(propertiesFileSystemProperty, other.getPropertiesFileSystemProperty());
		}
		return false;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).appendSuper(super.toString())
			.append("propertiesFileSystemProperty", propertiesFileSystemProperty)
			.toString();
	}

	@Override
	protected List<String> getJSONFieldValuePairs()
	{
		List<String> fieldValuePairs = super.getJSONFieldValuePairs();
		addSingleValueFieldJSON(
			fieldValuePairs,
			"propertiesFileSystemProperty",
			propertiesFileSystemProperty);
		return fieldValuePairs;
	}

	public String getPropertiesFileSystemProperty()
	{
		return propertiesFileSystemProperty;
	}

	public void setPropertiesFileSystemProperty(String propertiesFileSystemProperty)
	{
		this.propertiesFileSystemProperty = propertiesFileSystemProperty;
	}
}
