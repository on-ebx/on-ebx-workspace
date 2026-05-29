package com.orchestranetworks.ps.constraint.enumeration;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;

/**
 */
public class DatasetsEnumeration implements ConstraintEnumeration<String>
{
	private Path dataspace;
	private String dataspaceKey;

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
		Repository repository = pContext.getHome().getRepository();
		String branchName = getBranchName(pContext);
		AdaptationHome home = repository.lookupHome(HomeKey.forBranchName(branchName));
		Adaptation instance = home.findAdaptationOrNull(AdaptationName.forName(pValue));
		return instance.getLabelOrName(pLocale);
	}

	public Path getDataspace()
	{
		return this.dataspace;
	}

	public String getDataspaceKey()
	{
		return dataspaceKey;
	}

	@Override
	public List<String> getValues(final ValueContext pContext) throws InvalidSchemaException
	{
		List<String> values = new ArrayList<>();
		Repository repository = pContext.getHome().getRepository();
		String branchName = getBranchName(pContext);
		if (StringUtils.isBlank(branchName))
		{
			return values;
		}
		AdaptationHome home = repository.lookupHome(HomeKey.forBranchName(branchName));
		for (Adaptation instance : home.findAllRoots())
		{
			values.add(instance.getAdaptationName().getStringName());
		}
		return values;
	}

	public void setDataspace(final Path dataspace)
	{
		this.dataspace = dataspace;
	}

	public void setDataspaceKey(final String dataspaceKey)
	{
		this.dataspaceKey = dataspaceKey;
	}

	@Override
	public void setup(final ConstraintContext aContext)
	{
		if (dataspace == null)
		{
			if (dataspaceKey == null)
			{
				aContext.addError("Must specify either dataspace or dataspaceKey parameter.");
			}
		}
		else
		{
			if (dataspaceKey != null)
			{
				aContext.addError("Can't specify both dataspace and dataspaceKey parameters.");
			}
		}
	}

	@Override
	public String toUserDocumentation(final Locale userLocale, final ValueContext aContext)
		throws InvalidSchemaException
	{
		return null;
	}

	private String getBranchName(final ValueContext pContext)
	{
		return this.dataspace == null ? this.dataspaceKey
			: (String) pContext.getValue(this.dataspace);
	}
}
