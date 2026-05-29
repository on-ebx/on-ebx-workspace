package com.orchestranetworks.ps.valuefunction;

import com.orchestranetworks.schema.*;

/**
 * A {@link FieldConcatenationValueFunction} for addresses. It produces a one-line address in the format of:
 * addressLine1, addressLine2, addressLine3, city, stateOrProvince postalCode country.
 * (There's a comma and space between everything except between stateOrProvice and postalCode and between
 * postalCode and country, where there are just spaces. Addresses aren't supposed to have commas there.)
 * If a field is missing, the separator won't be shown either.
 * 
 * This assumes the fields have the string value that should be shown. If instead, one is a foreign key and you need to
 * show a label or pull a value from a foreign table, then you can override {@link #getFieldValue(com.onwbp.adaptation.Adaptation, SchemaNode)}
 * to do so.
 */
public class FullAddressValueFunction extends FieldConcatenationValueFunction
{
	private static final String ADDRESS_SEPARATOR_LIST = ",<space>;,<space>;,<space>;,<space>;<space>;<space>";

	private Path addressLine1Path;
	private Path addressLine2Path;
	private Path addressLine3Path;
	private Path cityPath;
	private Path stateOrProvincePath;
	private Path postalCodePath;
	private Path countryPath;

	@Override
	public void setup(ValueFunctionContext context)
	{
		setFieldList(createFieldList());
		setSeparatorList(ADDRESS_SEPARATOR_LIST);
		super.setup(context);
	}

	private String createFieldList()
	{
		StringBuilder bldr = new StringBuilder();
		addPathString(bldr, addressLine1Path, true);
		addPathString(bldr, addressLine2Path, true);
		addPathString(bldr, addressLine3Path, true);
		addPathString(bldr, cityPath, true);
		addPathString(bldr, stateOrProvincePath, true);
		addPathString(bldr, postalCodePath, true);
		addPathString(bldr, countryPath, true);
		return bldr.toString();
	}

	private static void addPathString(StringBuilder builder, Path path, boolean appendSeparator)
	{
		if (path != null)
		{
			builder.append(path.format());
		}
		if (appendSeparator)
		{
			builder.append(LIST_SEPARATOR);
		}
	}

	public Path getAddressLine1Path()
	{
		return addressLine1Path;
	}

	public void setAddressLine1Path(Path addressLine1Path)
	{
		this.addressLine1Path = addressLine1Path;
	}

	public Path getAddressLine2Path()
	{
		return addressLine2Path;
	}

	public void setAddressLine2Path(Path addressLine2Path)
	{
		this.addressLine2Path = addressLine2Path;
	}

	public Path getAddressLine3Path()
	{
		return addressLine3Path;
	}

	public void setAddressLine3Path(Path addressLine3Path)
	{
		this.addressLine3Path = addressLine3Path;
	}

	public Path getCityPath()
	{
		return cityPath;
	}

	public void setCityPath(Path cityPath)
	{
		this.cityPath = cityPath;
	}

	public Path getStateOrProvincePath()
	{
		return stateOrProvincePath;
	}

	public void setStateOrProvincePath(Path stateOrProvincePath)
	{
		this.stateOrProvincePath = stateOrProvincePath;
	}

	public Path getPostalCodePath()
	{
		return postalCodePath;
	}

	public void setPostalCodePath(Path postalCodePath)
	{
		this.postalCodePath = postalCodePath;
	}

	public Path getCountryPath()
	{
		return countryPath;
	}

	public void setCountryPath(Path countryPath)
	{
		this.countryPath = countryPath;
	}
}
