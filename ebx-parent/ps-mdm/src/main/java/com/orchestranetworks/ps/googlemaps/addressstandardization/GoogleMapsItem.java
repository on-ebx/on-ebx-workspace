package com.orchestranetworks.ps.googlemaps.addressstandardization;

public final class GoogleMapsItem
{
	public class JsObject
	{
		public class AddressValue
		{
			
			public final static String NUMBER = "number";
			
			public final static String STREET = "street";
			
			public final static String PREMISE = "premise";
			
			public final static String NEIGHBORHOOD = "neighborhood";
			
			public final static String INTERSECTION = "intersection";
			
			public final static String SUBLOCALITY = "sublocatity";
			
			public final static String CITY = "city";
			
			public final static String SUBADMINAREA = "subadminarea";
			
			public final static String ZIPCODE = "zipcode";

			public final static String REGION = "region";
			
			public final static String COUNTRY = "country";
			
			public final static String CONCATENATED_ADDRESS = "concatenatedAddress";
			
			public final static String LAT = "lat";

			public final static String LONG = "lng";
			
			public final static String FORMATTED_ADDRESS = "formatted_address";
		}

		/**
		 * The Constant PK with value pk, property of the GoogleMaps item javascript object.
		 */
		public final static String PK = "pk";
		/**
		 * The Constant LABEL with value label, property of the GoogleMaps item javascript object.
		 */
		public final static String LABEL = "label";
		/**
		 * The Constant ICON with value icon, property of the GoogleMaps item javascript object.
		 */
		public final static String ICON = "icon";
		/**
		 * The Constant CONTENT with value content, property of the GoogleMaps item javascript
		 * object.
		 */
		public final static String CONTENT = "content";
		/**
		 * The Constant ADDRESSVALUE with value addressValue, property of the GoogleMaps item
		 * javascript object.
		 *
		 * @see AddressValue
		 */
		public final static String ADDRESSVALUE = "addressValue";
	}

	private String pk;
	private String label;
	private GoogleMapsAddressValue addressValue;
	private String icon;
	private String content;

	/**
	 * Instantiates a new google maps item.
	 *
	 */
	public GoogleMapsItem()
	{
	}

	/**
	 * Instantiates a new google maps item.
	 *
	 * @param addressValue the address value
	 */
	public GoogleMapsItem(final GoogleMapsAddressValue addressValue)
	{
		this.addressValue = addressValue;
	}

	/**
	 * Instantiates a new google maps item.
	 *
	 * @param pk the pk
	 * @param label the label
	 * @param addressValue the address value
	 * @param icon the icon
	 * @param content the content
	 */
	public GoogleMapsItem(
		final String pk,
		final String label,
		final GoogleMapsAddressValue addressValue,
		final String icon,
		final String content)
	{
		this.pk = pk;
		this.label = label;
		this.addressValue = addressValue;
		this.icon = icon;
		this.content = content;
	}

	/**
	 * Gets the address value.
	 *
	 * @return the address value
	 */
	public GoogleMapsAddressValue getAddressValue()
	{
		return this.addressValue;
	}

	/**
	 * Gets the content.
	 *
	 * @return the content
	 */
	public String getContent()
	{
		if (this.content == null)
		{
			return "";
		}
		else
		{
			return this.content;
		}
	}

	/**
	 * Gets the url to the icon.
	 *
	 * @return the icon
	 */
	public String getIcon()
	{
		return this.icon;
	}

	/**
	 * Gets the JavaScript object of the Google Maps item.
	 *
	 * @return the JavaScript object of the item.
	 */
	public String getJsObject()
	{
		String jsObject = "{";
		jsObject += this.buildJsObjectContent();
		jsObject += "}";
		return jsObject;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel()
	{
		if (this.label == null)
		{
			return "";
		}
		else
		{
			return this.label;
		}
	}

	/**
	 * Gets the pk.
	 *
	 * @return the pk
	 */
	public String getPk()
	{
		if (this.pk == null)
		{
			return "";
		}
		else
		{
			return this.pk;
		}
	}

	/**
	 * Sets the address value.
	 *
	 * @param addressValue the new address value
	 */
	public void setAddressValue(final GoogleMapsAddressValue addressValue)
	{
		this.addressValue = addressValue;
	}

	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(final String content)
	{
		this.content = content;
	}

	/**
	 * Sets the icon used on map. <br>
	 * The argument is the url to the image. Custom image can be used, default are proposed in the
	 * class GoogleMaps.Icons.
	 *
	 * @param icon the url to the image
	 */
	public void setIcon(final String icon)
	{
		this.icon = icon;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 * @since 1.0.0
	 */
	public void setLabel(final String label)
	{
		this.label = label;
	}

	/**
	 * Sets the pk.
	 *
	 * @param pk the new pk
	 */
	public void setPk(final String pk)
	{
		this.pk = pk;
	}

	/**
	 * Builds the content of the JavaScript object.
	 *
	 * @return the string
	 */
	protected String buildJsObjectContent()
	{
		String jsObjectContent = "";

		jsObjectContent += GoogleMapsItem.JsObject.PK + ":'" + this.getPk() + "',";
		jsObjectContent += GoogleMapsItem.JsObject.LABEL + ":'"
			+ this.getLabel().replace("'", "\\'") + "',";
		jsObjectContent += GoogleMapsItem.JsObject.CONTENT + ":'"
			+ this.getContent().replace("'", "\\'") + "',";

		jsObjectContent += GoogleMapsItem.JsObject.ICON + ":";
		if (this.getIcon() == null)
		{
			jsObjectContent += "null,";
		}
		else
		{
			jsObjectContent += "'" + this.getIcon() + "',";
		}

		if (this.getAddressValue() == null)
		{
			jsObjectContent += GoogleMapsItem.JsObject.ADDRESSVALUE + ":null";
		}
		else
		{
			jsObjectContent += GoogleMapsItem.JsObject.ADDRESSVALUE + ":{";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.NUMBER + ":'"
				+ this.addressValue.getNumber().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.STREET + ":'"
				+ this.addressValue.getStreet().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.ZIPCODE + ":'"
				+ this.addressValue.getZipcode().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.CITY + ":'"
				+ this.addressValue.getCity().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.COUNTRY + ":'"
				+ this.addressValue.getCountry().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.REGION + ":'"
					+ this.addressValue.getRegion().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.PREMISE + ":'"
					+ this.addressValue.getPremise().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.NEIGHBORHOOD + ":'"
					+ this.addressValue.getNeighborhood().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.INTERSECTION + ":'"
					+ this.addressValue.getIntersection().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.SUBLOCALITY + ":'"
					+ this.addressValue.getSublocality().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.SUBADMINAREA + ":'"
					+ this.addressValue.getSubadminarea().replace("'", "\\'") + "',";
			jsObjectContent += GoogleMapsItem.JsObject.AddressValue.CONCATENATED_ADDRESS + ":'"
				+ this.addressValue.getConcatenatedAddress().replace("'", "\\'") + "',";
			if (this.addressValue.getLatitude() == null
				|| "".equals(this.addressValue.getLatitude()))
			{
				jsObjectContent += GoogleMapsItem.JsObject.AddressValue.LAT + ":null,";
			}
			else
			{
				jsObjectContent += GoogleMapsItem.JsObject.AddressValue.LAT + ":'"
					+ this.addressValue.getLatitude() + "',";
			}
			if (this.addressValue.getLongitude() == null
				|| this.addressValue.getLongitude().equals(""))
			{
				jsObjectContent += GoogleMapsItem.JsObject.AddressValue.LONG + ":null";
			}
			else
			{
				jsObjectContent += GoogleMapsItem.JsObject.AddressValue.LONG + ":'"
					+ this.addressValue.getLongitude() + "'";
			}
			jsObjectContent += "}";
		}

		return jsObjectContent;
	}
}