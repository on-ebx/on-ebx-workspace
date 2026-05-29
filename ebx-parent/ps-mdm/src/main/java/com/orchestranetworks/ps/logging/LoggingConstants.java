/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.logging;

/**
 */
public class LoggingConstants
{

	// logging properties - stored in ebx.properties
	public final static String LOGGING_FILE_NAME_PROPERTY = "ebx.customLogger.oututFileName";
	public final static String LOGGING_THRESHOLD_PROPERTY = "ebx.customLogger.threshold";
	public final static String LOGGING_LAYOUT_PROPERTY = "ebx.customLogger.layout";
	public final static String LOGGING_DEFAULT_FILE_NAME = "customLogger.log";
	public static final String LOGGING_CATEGORY_NAME_PROPERTY = "ebx.customLogger.categoryName";
	public static final String LOGGING_APPENDER_TYPE = "ebx.customLogger.appenderType";

	public static final String APPENDER_FILE = "File";
	public static final String APPENDER_CONSOLE = "Console";
	public static final String CATEGORY_DEFAULT_NAME = "customLogging";

	public static final String EBX_LOG_DIRECTORY_PROPERTY = "ebx.logs.directory";
	public final static String LOGGING_ROLLOVER_DATETIME_PATTERN = "ebx.customLogger.rollover.dateTime.pattern";
}
