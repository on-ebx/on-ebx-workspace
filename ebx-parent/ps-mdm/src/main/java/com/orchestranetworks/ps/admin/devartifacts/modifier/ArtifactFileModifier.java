package com.orchestranetworks.ps.admin.devartifacts.modifier;

import java.util.*;

/**
 * An abstract class for modifying a line of an artifact file on export or import
 */
public abstract class ArtifactFileModifier
{
	/**
	 * Modify a line on export
	 * 
	 * @param line the line of the artifact being written out
	 * @return a list of modified lines. It is a list in case the original line needs to result in multiple lines, or none.
	 *         Will return <code>null</code> if no modification is to be done.
	 */
	public abstract List<String> modifyExport(String line);

	/**
	 * Modify a line on import
	 * 
	 * @param line the line of the artifact being read in
	 * @return a list of modified lines. It is a list in case the original line needs to result in multiple lines, or none.
	 *         Will return <code>null</code> if no modification is to be done.
	 */
	public abstract List<String> modifyImport(String line);

	/**
	 * A convenience method for getting the value of a particular field, which will be an XML tag, from a line
	 * 
	 * @param line the line
	 * @param fieldName the name of the field
	 * @return the value for the field
	 */
	protected static String getValue(String line, String fieldName)
	{
		String startTag = getStartTag(fieldName);
		int startTagStartInd = line.indexOf(startTag);
		if (startTagStartInd == -1)
		{
			return null;
		}
		int startTagEndInd = startTagStartInd + startTag.length();
		int endTagStartInd = line.indexOf(getEndTag(fieldName), startTagEndInd);
		if (endTagStartInd == startTagEndInd)
		{
			return "";
		}
		return line.substring(startTagEndInd, endTagStartInd).trim();
	}

	/**
	 * A convenience method to replace the value of a particular field, which will be an XML tag, in a line
	 * 
	 * @param line the line
	 * @param fieldName the name of the field
	 * @param newValue the new value for the field
	 * @return the modified line
	 */
	protected static String replaceValue(String line, String fieldName, String newValue)
	{
		String startTag = getStartTag(fieldName);
		int startTagStartInd = line.indexOf(startTag);
		if (startTagStartInd == -1)
		{
			return line;
		}
		StringBuilder bldr = new StringBuilder();
		int startTagEndInd = startTagStartInd + startTag.length();
		bldr.append(line.substring(0, startTagEndInd)).append(newValue);
		int endTagStartInd = line.indexOf(getEndTag(fieldName), startTagEndInd);
		bldr.append(line.substring(endTagStartInd));
		return bldr.toString();
	}

	/**
	 * A convenience method to determine if a line contains a start tag for a particular field
	 * 
	 * @param line the line
	 * @param fieldName the name of the field
	 * @return whether the line contains the start tag
	 */
	protected static boolean containsStartTag(String line, String fieldName)
	{
		return line.indexOf(getStartTag(fieldName)) != -1;
	}

	/**
	 * A convenience method to determine if a line contains an end tag for a particular field
	 * 
	 * @param line the line
	 * @param fieldName the name of the field
	 * @return whether the line contains the end tag
	 */
	protected static boolean containsEndTag(String line, String fieldName)
	{
		return line.indexOf(getEndTag(fieldName)) != -1;
	}

	/**
	 * A convenience method to get the start tag for a particular field
	 * 
	 * @param fieldName the name of the field
	 * @return the start tag
	 */
	protected static String getStartTag(String fieldName)
	{
		return new StringBuilder("<").append(fieldName).append(">").toString();
	}

	/**
	 * A convenience method to get the end tag for a particular field
	 * 
	 * @param fieldName the name of the field
	 * @return the end tag
	 */
	protected static String getEndTag(String fieldName)
	{
		return new StringBuilder("</").append(fieldName).append(">").toString();
	}
}
