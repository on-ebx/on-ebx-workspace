/*
 * Copyright Orchestra Networks 2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This constraint enumeration can be used to create a selection field for
 * choosing a data set from within a particular data space.  The data space
 * can be specified by a sibling field.  If not specified, the data space of
 * the containing table is used.
 */
public class DataSetConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a data set.";
	private Path homePath;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		if (aValue != null)
		{
			try
			{
				Adaptation ds = getDataSet(aValidationContext, aValue);
				if (ds == null)
					aValidationContext.addError(aValue + " does not exist");
			}
			catch (Exception e)
			{
				aValidationContext.addError(aValue + " does not exist");
			}
		}
	}

	private AdaptationHome getHome(ValueContext valueContext)
	{
		AdaptationHome home = valueContext.getHome();
		if (homePath != null)
		{
			String homeKey = (String) valueContext.getValue(Path.PARENT.add(homePath));
			if (homeKey != null)
				return home.getRepository().lookupHome(HomeKey.parse(homeKey));
		}
		return home;
	}

	private Adaptation getDataSet(ValueContext valueContext, String dsKey)
	{
		AdaptationHome home = getHome(valueContext);
		if (home == null)
			return null;
		return home.findAdaptationOrNull(AdaptationName.forName(dsKey));
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
		// optional path to other field from which to declare dataspace
		if (homePath != null)
		{
			PathUtils.setupFieldNode(aContext, homePath, "homePath", false, false);
		}
	}

	@Override
	public String toUserDocumentation(Locale userLocale, ValueContext aContext)
		throws InvalidSchemaException
	{
		return MESSAGE;
	}

	@Override
	public String displayOccurrence(String aValue, ValueContext aContext, Locale aLocale)
		throws InvalidSchemaException
	{
		if (aValue != null)
		{
			Adaptation ds = getDataSet(aContext, aValue);
			if (ds == null)
				return aValue;
			return ds.getLabel(aLocale);
		}
		return null;
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		AdaptationHome home = getHome(aContext);
		if (home == null)
			return Collections.emptyList();
		List<String> datasets = new ArrayList<>();
		List<Adaptation> rootDataSets = home.findAllRoots();
		for (Adaptation adaptation : rootDataSets)
		{
			datasets.add(adaptation.getAdaptationName().getStringName());
		}
		return datasets;
	}

	public Path getHomePath()
	{
		return homePath;
	}

	public void setHomePath(Path homePath)
	{
		this.homePath = homePath;
	}

}
