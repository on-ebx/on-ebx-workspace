package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 */
public class DataspacesEnumeration implements ConstraintEnumeration<String>
{
	@Override
	public void checkOccurrence(final String arg0, final ValueContextForValidation arg1)
		throws InvalidSchemaException
	{
	}

	@Override
	public String displayOccurrence(
		final String pValue,
		final ValueContext pContext,
		final Locale pLocale)
		throws InvalidSchemaException
	{
		AdaptationHome home = pContext.getHome().getRepository().lookupHome(
			HomeKey.forBranchName(pValue));
		return home.getLabelOrName(pLocale);
	}

	@Override
	public List<String> getValues(final ValueContext pContext) throws InvalidSchemaException
	{
		List<String> values = new ArrayList<>();
		Repository repository = pContext.getHome().getRepository();
		HomeCollector collector = new HomeCollector();
		collector.setIncludingVersion(false);
		List<AdaptationHome> homes = collector.collectHomes(repository.getReferenceBranch(), true);
		for (AdaptationHome home : homes)
		{
			values.add(home.getKey().getName());
		}
		return values;
	}

	@Override
	public void setup(final ConstraintContext aContext)
	{

	}

	@Override
	public String toUserDocumentation(final Locale userLocale, final ValueContext aContext)
		throws InvalidSchemaException
	{
		return null;
	}

}
