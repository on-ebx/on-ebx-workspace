package com.orchestranetworks.ps.util;


public class CommonUtil
{
	private static final String PROP_ENVIRONMENT_MODE = "environmentMode";

	public static String getEnvironmentMode()
	{
		return System.getProperty(PROP_ENVIRONMENT_MODE);
	}
}
