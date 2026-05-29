package com.orchestranetworks.ps.logging;

import com.orchestranetworks.service.*;

public class CustomLogger
{
	private static LoggingCategory customLogger;

	public static LoggingCategory getCustomLogger()
	{
		if (customLogger == null)
		{
			// We aren't storing this as the default in the static initialization because
			// it's always recommended to use the getter with each call so that it picks
			// up the latest changes
			return LoggingCategory.getKernel();
		}
		return customLogger;
	}

	public static void setCustomLogger(LoggingCategory logger)
	{
		if (LoggingCategory.getKernel().isDebug())
		{
			LoggingCategory.getKernel().debug("Setting custom logger. " + logger);
		}
		customLogger = logger;
	}

	public boolean isDebugEnabled()
	{
		return customLogger.isDebug();
	}

	public void debug(String message)
	{
		if (customLogger.isDebug())
		{
			customLogger.debug(message);
		}
	}

	public void debug(String message, Exception exp)
	{
		if (customLogger.isDebug())
		{
			customLogger.error(message, exp);
		}
	}

	public void info(String message)
	{
		if (customLogger.isInfo())
		{
			customLogger.info(message);
		}
	}

	public void info(String message, Exception exp)
	{
		if (customLogger.isInfo())
		{
			customLogger.error(message, exp);
		}
	}

}
