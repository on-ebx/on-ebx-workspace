package com.orchestranetworks.ps.trigger;

public class TrimFieldTableTrigger extends CleanseFieldTableTrigger
{
	@Override
	protected String performCleanse(String value)
	{
		return value.trim();
	}

}
