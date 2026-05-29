package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 * This includes a record if it references a data set that is in the list of data sets passed in.
 * If {@link #ALL_DATASETS} is specified, then it will only look at the data spaces passed in.
 */
public class DataSetInCollectionAdaptationFilter implements AdaptationFilter
{
	private static final String ALL_DATASETS = "*";

	private Collection<AdaptationHome> dataSpaces;
	private Collection<Adaptation> dataSets;
	private Path dataSpacePath;
	private Path dataSetPath;

	public DataSetInCollectionAdaptationFilter(
		Collection<AdaptationHome> dataSpaces,
		Collection<Adaptation> dataSets,
		Path dataSpacePath,
		Path dataSetPath)
	{
		this.dataSpaces = dataSpaces;
		this.dataSets = dataSets;
		this.dataSpacePath = dataSpacePath;
		this.dataSetPath = dataSetPath;
	}

	@Override
	public boolean accept(Adaptation adaptation)
	{
		String dataSpaceName = adaptation.getString(dataSpacePath);
		Repository repo = adaptation.getHome().getRepository();
		// The value starts with "B" so need to call the parse method, not forBranchName
		AdaptationHome dataSpace = repo.lookupHome(HomeKey.parse(dataSpaceName));
		if (dataSpace == null)
		{
			return false;
		}
		String dataSetName = adaptation.getString(dataSetPath);
		if (ALL_DATASETS.equals(dataSetName))
		{
			HomeKey dataSpaceKey = dataSpace.getKey();
			boolean found = false;
			Iterator<AdaptationHome> iter = dataSpaces.iterator();
			while (!found && iter.hasNext())
			{
				AdaptationHome iterDataSpace = iter.next();
				found = iterDataSpace.getKey().equals(dataSpaceKey);
			}
			return found;
		}

		Adaptation dataSet = dataSpace.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		return dataSet != null && dataSets.contains(dataSet);
	}
}
