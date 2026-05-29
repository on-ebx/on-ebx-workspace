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
 * choosing a data space or snapshot
 */
public class AdaptationHomeConstraintEnumeration implements ConstraintEnumeration<String>
{
	private static final String MESSAGE = "Specify a data space/snapshot.";
	private boolean includeBranch;
	private boolean includeVersion;
	protected AdaptationHome parentDataSpace;

	@Override
	public void checkOccurrence(String aValue, ValueContextForValidation aValidationContext)
		throws InvalidSchemaException
	{
		if (aValue != null)
		{
			try
			{
				HomeKey key = HomeKey.parse(aValue);
				Repository repo = aValidationContext.getHome().getRepository();
				AdaptationHome home = repo.lookupHome(key);
				if (home == null)
					aValidationContext.addError(aValue + " does not exist");
			}
			catch (Exception e)
			{
				aValidationContext.addError(aValue + " does not exist");
			}
		}
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
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
			AdaptationHome home = null;
			try
			{
				HomeKey key = HomeKey.parse(aValue);
				Repository repo = aContext.getHome().getRepository();
				home = repo.lookupHome(key);
				if (home == null)
					return aValue + "<missing>";
				return home.getLabel(aLocale);
			}
			catch (Exception e)
			{
				return aValue + "<missing>";
			}
		}
		return null;
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		List<AdaptationHome> homes;
		HomeCollector homeCollector = new HomeCollector();
		homeCollector.setIncludingBranch(includeBranch);
		homeCollector.setIncludingVersion(includeVersion);
		if (parentDataSpace != null)
		{
			homes = homeCollector.collectHomes(parentDataSpace, true);
		}
		else
		{
			Repository repo = aContext.getHome().getRepository();
			homes = homeCollector.collectHomes(repo.getReferenceBranch(), true);
		}
		List<String> result = new ArrayList<>();
		for (AdaptationHome adaptationHome : homes)
		{
			if (!adaptationHome.isTechnicalBranch())
				result.add(adaptationHome.getKey().format());
		}
		return result;
	}

	public boolean isIncludeBranch()
	{
		return includeBranch;
	}

	public void setIncludeBranch(boolean includeBranch)
	{
		this.includeBranch = includeBranch;
	}

	public boolean isIncludeVersion()
	{
		return includeVersion;
	}

	public void setIncludeVersion(boolean includeVersion)
	{
		this.includeVersion = includeVersion;
	}

}
