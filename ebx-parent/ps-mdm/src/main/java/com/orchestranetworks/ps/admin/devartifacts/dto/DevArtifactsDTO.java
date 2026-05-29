package com.orchestranetworks.ps.admin.devartifacts.dto;

import java.util.*;

import org.apache.commons.lang3.builder.*;
import org.apache.commons.text.*;

/**
 * A DTO encapsulating runtime parameters used in a Dev Artifacts invocation.
 * It includes the capability of converting itself into a JSON string for use
 * in a REST call, but isn't limited to being used only for that.
 */
public class DevArtifactsDTO
{
	private Boolean environmentCopy;
	private Boolean replaceMode;
	private Boolean skipNonExistingFiles;
	private Boolean publishWorkflowModels;
	private List<String> workflowModels;

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder().append(environmentCopy)
			.append(replaceMode)
			.append(skipNonExistingFiles)
			.append(publishWorkflowModels)
			.append(workflowModels)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && obj instanceof DevArtifactsDTO)
		{
			DevArtifactsDTO other = (DevArtifactsDTO) obj;
			return Objects.equals(environmentCopy, other.getEnvironmentCopy())
				&& Objects.equals(replaceMode, other.getReplaceMode())
				&& Objects.equals(skipNonExistingFiles, other.getSkipNonExistingFiles())
				&& Objects.equals(publishWorkflowModels, other.getPublishWorkflowModels())
				&& Objects.equals(workflowModels, other.getWorkflowModels());
		}
		return false;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("environmentCopy", environmentCopy)
			.append("replaceMode", replaceMode)
			.append("skipNonExistingFiles", skipNonExistingFiles)
			.append("publishWorkflowModels", publishWorkflowModels)
			.append("workflowModels", workflowModels)
			.toString();
	}

	/**
	 * Convert the DTO into a JSON string
	 * 
	 * @return the DTO, as JSON
	 */
	public String toJSON()
	{
		List<String> fieldValuePairs = getJSONFieldValuePairs();
		String jsonBody = String.join(",", fieldValuePairs);
		return new StringBuilder("{").append(jsonBody).append("}").toString();
	}

	protected List<String> getJSONFieldValuePairs()
	{
		List<String> fieldValuePairs = new ArrayList<>();
		addSingleValueFieldJSON(fieldValuePairs, "environmentCopy", environmentCopy);
		addSingleValueFieldJSON(fieldValuePairs, "replaceMode", replaceMode);
		addSingleValueFieldJSON(fieldValuePairs, "skipNonExistingFiles", skipNonExistingFiles);
		addSingleValueFieldJSON(fieldValuePairs, "publishWorkflowModels", publishWorkflowModels);
		addListValueFieldJSON(fieldValuePairs, "workflowModels", workflowModels);
		return fieldValuePairs;
	}

	protected void addSingleValueFieldJSON(
		List<String> fieldValuePairs,
		String fieldName,
		Object value)
	{
		if (value != null)
		{
			String fieldValuePair = new StringBuilder("\"").append(fieldName)
				.append("\":")
				.append(getValueJSON(value))
				.toString();
			fieldValuePairs.add(fieldValuePair);
		}
	}

	protected void addListValueFieldJSON(
		List<String> fieldValuePairs,
		String fieldName,
		List<?> valueList)
	{
		if (valueList != null && !valueList.isEmpty())
		{
			StringBuilder bldr = new StringBuilder("\"").append(fieldName).append("\":[");
			List<String> strList = new ArrayList<>();
			for (Object value : valueList)
			{
				if (value != null)
				{
					strList.add(getValueJSON(value));
				}
			}
			String fieldValuePair = bldr.append(String.join(",", strList)).append("]").toString();
			fieldValuePairs.add(fieldValuePair);
		}
	}

	// The only properties we have currently are boolean or string, so no need to complicate this further.
	// If we add other types of properties (i.e. date, number) then we'd need to handle formatting them here.
	protected String getValueJSON(Object value)
	{
		if (value == null)
		{
			return "null";
		}
		String valueStr = value.toString();
		if (value instanceof Boolean)
		{
			return valueStr;
		}
		return new StringBuilder("\"").append(StringEscapeUtils.escapeJson(valueStr))
			.append("\"")
			.toString();
	}

	public Boolean getEnvironmentCopy()
	{
		return environmentCopy;
	}

	public void setEnvironmentCopy(Boolean environmentCopy)
	{
		this.environmentCopy = environmentCopy;
	}

	public Boolean getReplaceMode()
	{
		return replaceMode;
	}

	public void setReplaceMode(Boolean replaceMode)
	{
		this.replaceMode = replaceMode;
	}

	public Boolean getSkipNonExistingFiles()
	{
		return skipNonExistingFiles;
	}

	public void setSkipNonExistingFiles(Boolean skipNonExistingFiles)
	{
		this.skipNonExistingFiles = skipNonExistingFiles;
	}

	public Boolean getPublishWorkflowModels()
	{
		return publishWorkflowModels;
	}

	public void setPublishWorkflowModels(Boolean publishWorkflowModels)
	{
		this.publishWorkflowModels = publishWorkflowModels;
	}

	public List<String> getWorkflowModels()
	{
		return workflowModels;
	}

	public void setWorkflowModels(List<String> workflowModels)
	{
		this.workflowModels = workflowModels;
	}
}
