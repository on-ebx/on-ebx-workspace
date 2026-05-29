package com.orchestranetworks.ps.admin.devartifacts.util;

import org.apache.commons.lang3.builder.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;

/**
 * Contains information uniquely identifying a data set, for use in its creation
 */
public class DataSetCreationKey
{
	private HomeKey dataSpaceKey;
	private AdaptationName dataSetName;

	public DataSetCreationKey()
	{
	}

	public DataSetCreationKey(String dataSpaceName, String dataSetName)
	{
		this(HomeKey.forBranchName(dataSpaceName), AdaptationName.forName(dataSetName));
	}

	public DataSetCreationKey(HomeKey dataSpaceKey, AdaptationName dataSetName)
	{
		this.dataSpaceKey = dataSpaceKey;
		this.dataSetName = dataSetName;
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder bldr = new HashCodeBuilder();
		bldr.append(dataSpaceKey);
		bldr.append(dataSetName);
		return bldr.toHashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof DataSetCreationKey)
		{
			DataSetCreationKey dsci = (DataSetCreationKey) obj;
			EqualsBuilder bldr = new EqualsBuilder();
			bldr.append(dataSpaceKey, dsci.getDataSpaceKey());
			bldr.append(dataSetName, dsci.getDataSetName());
			return bldr.isEquals();
		}
		return false;
	}

	public HomeKey getDataSpaceKey()
	{
		return this.dataSpaceKey;
	}

	public void setDataSpaceKey(HomeKey dataSpaceKey)
	{
		this.dataSpaceKey = dataSpaceKey;
	}

	public AdaptationName getDataSetName()
	{
		return this.dataSetName;
	}

	public void setDataSetName(AdaptationName dataSetName)
	{
		this.dataSetName = dataSetName;
	}
}
