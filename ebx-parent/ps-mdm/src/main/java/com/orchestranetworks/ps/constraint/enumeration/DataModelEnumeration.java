package com.orchestranetworks.ps.constraint.enumeration;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 */
public class DataModelEnumeration implements ConstraintEnumeration<String>
{
	public static Adaptation getAnyDataset(
		final Repository pRepository,
		final String pSchemaLocation)
	{
		Map<String, Adaptation> map = getUsedSchemasLocationsWithOneDataset(pRepository);
		return map.get(pSchemaLocation);
	}

	private static Map<String, Adaptation> getUsedSchemasLocationsWithOneDataset(
		final Repository repository)
	{
		Map<String, Adaptation> datasets = new HashMap<>();
		HomeCollector collector = new HomeCollector();
		collector.setIncludingVersion(false);
		List<AdaptationHome> dataspaces = collector
			.collectHomes(repository.getReferenceBranch(), true);
		for (AdaptationHome dataspace : dataspaces)
		{
			for (Adaptation instance : dataspace.findAllRoots())
			{
				datasets.put(instance.getSchemaLocation().format(), instance);
			}
		}
		return datasets;
	}

	@Override
	public void checkOccurrence(final String pValue, final ValueContextForValidation pContext)
		throws InvalidSchemaException
	{

	}

	@Override
	public String displayOccurrence(
		final String pValue,
		final ValueContext pContext,
		final Locale pLocale) throws InvalidSchemaException
	{
		int endModuleName = pValue.substring(15, pValue.length()).indexOf(":") + 15;
		String moduleName = pValue.substring(15, endModuleName);
		File file = SchemaLocation.parse(pValue).getFileOrNull();
		if (file == null)
		{
			return pValue;
		}
		String fileName = file.getName();

		return moduleName + " - " + fileName;
	}

	@Override
	public List<String> getValues(final ValueContext pContext) throws InvalidSchemaException
	{
		Map<String, Adaptation> map = DataModelEnumeration
			.getUsedSchemasLocationsWithOneDataset(pContext.getHome().getRepository());
		return new ArrayList<>(map.keySet());
	}

	@Override
	public void setup(final ConstraintContext aContext)
	{
	}

	@Override
	public String toUserDocumentation(final Locale pLocale, final ValueContext pContext)
		throws InvalidSchemaException
	{
		return null;
	}

}
