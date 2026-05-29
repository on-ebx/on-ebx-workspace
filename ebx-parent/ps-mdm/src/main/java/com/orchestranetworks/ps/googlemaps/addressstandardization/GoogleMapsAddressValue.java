package com.orchestranetworks.ps.googlemaps.addressstandardization;

import java.util.Locale;

import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.schema.Path;

/**
 * Bean to set the values of each address component. <br>
 * Address components:
 * <ul>
 * <li>number</li>
 * <li>street</li>
 * <li>premise</li>
 * <li>subpremise</li>
 * <li>neighborhood</li>
 * <li>intersection</li>
 * <li>sublocality</li>
 * <li>administrative_area_level_2</li>
 * <li>postal_town</li>
 * <li>zipcode</li>
 * <li>city</li>
 * <li>region</li>
 * <li>country</li>
 * <li>latitude</li>
 * <li>longitude</li>
 * <li>formatted_address</li>
 * </ul>
 *
 */
public final class GoogleMapsAddressValue
{
	/**
	 * Creates the address value.
	 *
	 * @param pRecord the record
	 * @param pMapping the mapping
	 * @param pLocale the locale
	 * @return the google maps address value
	 */
	
	public static GoogleMapsAddressValue createAddressValue(
			final ValueContext vc,
			final GoogleMapsAddressFieldMapping pMapping,
			final Locale pLocale)
		{
			GoogleMapsAddressValue addressValue = new GoogleMapsAddressValue();

			if (vc == null || pMapping == null)
			{
				return addressValue;
			}

			if (pMapping.getNumberPath() != null)
			{
				Object numberObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getNumberPath()));
				if (numberObject != null)
				{
					addressValue.setNumber(numberObject.toString());
				}
			}
			if (pMapping.getStreetPath() != null)
			{
				Object streetObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getStreetPath()));
				if (streetObject != null)
				{
					addressValue.setStreet(streetObject.toString());
				}
			}
			if (pMapping.getPostalCodePath() != null)
			{
				Object zipCodeObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getPostalCodePath()));
				if (zipCodeObject != null)
				{
					addressValue.setZipcode(zipCodeObject.toString());
				}
			}
			if (pMapping.getLocalityPath() != null)
			{
				Object cityObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getLocalityPath()));
				if (cityObject != null)
				{
					addressValue.setCity(cityObject.toString());
				}
			}
			if (pMapping.getRegionPath() != null)
			{
				Object regionObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getRegionPath()));
				if (regionObject != null)
				{
					addressValue.setRegion(regionObject.toString());
				}
			}
			if (pMapping.getCountryPath() != null)
			{
				Object countryObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getCountryPath()));
				if (countryObject != null)
				{
					addressValue.setCountry(countryObject.toString());
				}
			}
			if (pMapping.getLatitudePath() != null)
			{
				Object latitudeObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getLatitudePath()));
				if (latitudeObject != null)
				{
					addressValue.setLatitude(latitudeObject.toString());
				}
			}
			if (pMapping.getLongitudePath() != null)
			{
				Object longitudeObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getLongitudePath()));
				if (longitudeObject != null)
				{
					addressValue.setLongitude(longitudeObject.toString());
				}
			}
			if (pMapping.getFormattedAddressPath() != null)
			{
				Object formattedAddressObject = (String) vc
						.getValue(Path.PARENT.add(pMapping.getFormattedAddressPath()));
				if (formattedAddressObject != null)
				{
					addressValue.setFormatted_address(formattedAddressObject.toString());
				}
			}

			return addressValue;
		}

	private String number;
	private String street;
	private String premise;
	private String neighborhood;
	private String intersection;
	private String sublocality;
	private String subadminarea;
	private String zipcode;
	private String city;
	private String region;
	private String country;
	private String latitude;
	private String longitude;
	private String formatted_address;
	private String administrative_area_level_2;
	private String postal_town;
	
	/**
	 * Instantiates a new google maps address value.
	 *
	 */
	public GoogleMapsAddressValue()
	{}

	/**
	 * Instantiates a new google maps address value.
	 *
	 * @param number the number
	 * @param street the street
	 * @param zipcode the zipcode
	 * @param city the city
	 * @param region the region
	 * @param country the country
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public GoogleMapsAddressValue(
		final String number,
		final String street,
		final String zipcode,
		final String city,
		final String region,
		final String country,
		final String latitude,
		final String longitude,
		final String formatted_address,
		final String premise,
		final String subpremise,
		final String post_box,
		final String administrative_area_level_2,
		final String postal_code_suffix,
		final String postal_town
		)
	{
		this.number = number;
		this.street = street;
		this.zipcode = zipcode;
		this.city = city;
		this.region = region;
		this.country = country;
		this.latitude = latitude;
		this.longitude = longitude;
		this.formatted_address = formatted_address;
		this.premise = premise;
		this.administrative_area_level_2 = administrative_area_level_2;
		this.postal_town = postal_town;
	}

	public String getCity()
	{
		if (this.city == null)
		{
			return "";
		}
		return this.city;
	}


	public String getConcatenatedAddress()
	{
		boolean before = false;
		String address = "";

		String addressNumber = this.getNumber();
		if (addressNumber != null && !"".equals(addressNumber))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressNumber;
			before = true;
		}

		String addressStreet = this.getStreet();
		if (addressStreet != null && !"".equals(addressStreet))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressStreet;
			before = true;
		}
		
		String premise = this.getPremise();
		if (premise != null && !"".equals(premise))
		{
			if (before)
			{
				address += ", ";
			}
			address += premise;
			before = true;
		}
		
		String neighborhood = this.getNeighborhood();
		if (neighborhood != null && !"".equals(neighborhood))
		{
			if (before)
			{
				address += ", ";
			}
			address += neighborhood;
			before = true;
		}
		
		String intersection = this.getIntersection();
		if (intersection != null && !"".equals(intersection))
		{
			if (before)
			{
				address += ", ";
			}
			address += intersection;
			before = true;
		}
		
		String sublocality = this.getSublocality();
		if (sublocality != null && !"".equals(sublocality))
		{
			if (before)
			{
				address += ", ";
			}
			address += sublocality;
			before = true;
		}
		
		String subadminarea = this.getSubadminarea();
		if (subadminarea != null && !"".equals(subadminarea))
		{
			if (before)
			{
				address += ", ";
			}
			address += subadminarea;
			before = true;
		}

		String addressZipcode = this.getZipcode();
		if (addressZipcode != null && !"".equals(addressZipcode))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressZipcode;
			before = true;
		}

		String addressCity = this.getCity();
		if (addressCity != null && !"".equals(addressCity))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressCity;
			before = true;
		}
		
		String addressRegion = this.getRegion();
		if (addressRegion != null && !"".equals(addressRegion))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressRegion;
			before = true;
		}

		String addressCountry = this.getCountry();
		if (addressCountry != null && !"".equals(addressCountry))
		{
			if (before)
			{
				address += ", ";
			}
			address += addressCountry;
			before = true;
		}

		return address;
	}

	public String getCountry()
	{
		if (this.country == null)
		{
			return "";
		}
		return this.country;
	}

	public String getLatitude()
	{
		return this.latitude;
	}

	public String getLongitude()
	{
		return this.longitude;
	}

	public String getNumber()
	{
		if (this.number == null)
		{
			return "";
		}
		return this.number;
	}

	public String getRegion()
	{
		if (this.region == null)
		{
			return "";
		}
		return this.region;
	}

	public String getStreet()
	{
		if (this.street == null)
		{
			return "";
		}
		return this.street;
	}

	public String getZipcode()
	{
		if (this.zipcode == null)
		{
			return "";
		}
		return this.zipcode;
	}

	public void setCity(final String city)
	{
		this.city = city;
	}

	public void setCountry(final String country)
	{
		this.country = country;
	}

	public void setLatitude(final String latitude)
	{
		this.latitude = latitude;
	}

	public void setLongitude(final String longitude)
	{
		this.longitude = longitude;
	}

	public void setNumber(final String number)
	{
		this.number = number;
	}

	public void setRegion(final String region)
	{
		this.region = region;
	}

	public void setStreet(final String street)
	{
		this.street = street;
	}

	public void setZipcode(final String zipcode)
	{
		this.zipcode = zipcode;
	}

	public String getFormatted_address() {
		return formatted_address;
	}

	public void setFormatted_address(String formatted_address) {
		this.formatted_address = formatted_address;
	}
	
	public String getPremise() {
		if (this.premise == null)
		{
			return "";
		}
		return premise;
	}

	public void setPremise(String premise) {
		this.premise = premise;
	}

	public String getAdministrative_area_level_2() {
		if (this.administrative_area_level_2 == null)
		{
			return "";
		}
		return administrative_area_level_2;
	}

	public void setAdministrative_area_level_2(String administrative_area_level_2) {
		this.administrative_area_level_2 = administrative_area_level_2;
	}

	public String getPostal_town() {
		if (this.postal_town == null)
		{
			return "";
		}
		return postal_town;
	}

	public void setPostal_town(String postal_town) {
		this.postal_town = postal_town;
	}
	
	public String getNeighborhood() {
		if (this.neighborhood == null)
		{
			return "";
		}
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	public String getIntersection() {
		if (this.intersection == null)
		{
			return "";
		}
		return intersection;
	}

	public void setIntersection(String intersection) {
		this.intersection = intersection;
	}

	public String getSublocality() {
		if (this.sublocality == null)
		{
			return "";
		}
		return sublocality;
	}

	public void setSublocality(String sublocality) {
		this.sublocality = sublocality;
	}

	public String getSubadminarea() {
		if (this.subadminarea == null)
		{
			return "";
		}
		return subadminarea;
	}

	public void setSubadminarea(String subadminarea) {
		this.subadminarea = subadminarea;
	}
}
