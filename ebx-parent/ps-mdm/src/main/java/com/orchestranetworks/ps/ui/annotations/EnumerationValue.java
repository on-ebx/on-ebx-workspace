package com.orchestranetworks.ps.ui.annotations;

public class EnumerationValue
{
	private String displayValue;
	private String storedValue;
	public static EnumerationValue nonSelected = new EnumerationValue("Not Defined....", "");

	private EnumerationValue(String displayValue, String storedValue)
	{
		this.displayValue = displayValue;
		this.storedValue = storedValue;
	}

	public String getDisplayValue()
	{
		return displayValue;
	}

	public String getStoredValue()
	{
		return storedValue;
	}

	public void setDisplayValue(String displayValue)
	{
		this.displayValue = displayValue;
	}

	public void setStoredValue(String storedValue)
	{
		this.storedValue = storedValue;
	}

	public static EnumerationValue valueOf(String displayValue, String storedValue)
	{
		return new EnumerationValue(displayValue, storedValue);
	}

	public static EnumerationValue valueOf(String displayValue)
	{
		return new EnumerationValue(displayValue, displayValue);
	}

}
