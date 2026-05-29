package com.orchestranetworks.ps.admin.devartifacts.util;

import org.apache.commons.lang3.builder.*;

/**
 * Contains information needed for creating a data set
 */
public class DataSetCreationInfo
{
	private DataSetCreationKey dataSetCreationKey;
	private String dataModelXSD;

	public DataSetCreationInfo()
	{
	}

	public DataSetCreationInfo(DataSetCreationKey dataSetCreationKey, String dataModelXSD)
	{
		this.dataSetCreationKey = dataSetCreationKey;
		this.dataModelXSD = dataModelXSD;
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder bldr = new HashCodeBuilder();
		bldr.append(dataSetCreationKey);
		bldr.append(dataModelXSD);
		return bldr.toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof DataSetCreationInfo)
		{
			DataSetCreationInfo dsci = (DataSetCreationInfo) obj;
			EqualsBuilder bldr = new EqualsBuilder();
			bldr.append(dataSetCreationKey, dsci.getDataSetCreationKey());
			bldr.append(dataModelXSD, dsci.getDataModelXSD());
			return bldr.isEquals();
		}
		return false;
	}

	public DataSetCreationKey getDataSetCreationKey()
	{
		return this.dataSetCreationKey;
	}

	public void setDataSetCreationKey(DataSetCreationKey dataSetCreationKey)
	{
		this.dataSetCreationKey = dataSetCreationKey;
	}

	public String getDataModelXSD()
	{
		return this.dataModelXSD;
	}

	public void setDataModelXSD(String dataModelXSD)
	{
		this.dataModelXSD = dataModelXSD;
	}
}
