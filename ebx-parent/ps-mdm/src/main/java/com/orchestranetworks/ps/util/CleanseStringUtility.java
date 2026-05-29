package com.orchestranetworks.ps.util;

import com.onwbp.base.misc.*;

public class CleanseStringUtility
{
	public final static String SINGLE_SPACE = " ";

	public static String CleanseString(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		string = replaceWhiteSpacesCharacters(string);
		string = replaceMicrosoftSmartCharacters(string);
		string = removeControlCharacters(string);
		string = removeExtraSpaces(string);
		string = string.trim();
		return string;
	}

	/**
	 * 
	 * Microsoft Smart Character:
	 *
	 * U+201A (curved single quote) > U+0027 (apostrophe)
	 * U+2018 (curved single quote) > U+0027 (apostrophe)
	 * U+2019 (curved single quote) > U+0027 (apostrophe)
	 * 
	 * U+201E (curved double quote) > U+0022 (quotation)
	 * U+201C (curved double quote) > U+0022 (quotation)
	 * U+201D (curved double quote) > U+0022 (quotation)
	 *
	 * U+2013 (dash) > U+002D (hyphen)
	 * U+2014 (dash) > U+002D (hyphen)
	 *
	 */
	public static String replaceMicrosoftSmartCharacters(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		string = string.replaceAll("[\u201A\u2018\u2019]", "\u0027");

		// the \u005c is an escape code. without it code won't compile. 
		string = string.replaceAll("[\u201E\u201C\u201D]", "\u005c\u0022");
		string = string.replaceAll("[\u2013\u2014]", "\u002D");
		return string;
	}

	/**
	 * Control characters:
	 *
	 * U+0000 through U+001F (C0 controls)
	 * U+007F (delete)
	 * U+0080 through U+009F (C1 controls)
	 */

	public static String removeControlCharacters(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		string = string.replaceAll("[\u0000-\u001F]", "");
		string = string.replaceAll("[\u0080-\u009F]", "");
		string = string.replaceAll("\u007F", "");

		return string;
	}

	/**
	 * Whitespace characters:
	 * 
	 * U+0009 (horizontal tab)
	 * U+000A (line feed)
	 * U+000D (carriage return)
	 * U+00A0 (non-breaking space)
	 */
	public static String replaceWhiteSpacesCharacters(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		string = string.replaceAll("\u0009", "\u0020");
		string = string.replaceAll("\\u000A", "\u0020");
		string = string.replaceAll("\\u000D", "\u0020");
		string = string.replaceAll("\u00A0", "\u0020");
		return string;
	}

	/**
	 * Misc. Other > Replacement Character
	 * 
	 * U+2013 (en dash) > U+002D (hyphen)
	 * U+2014 (em dash) > U+002D (hyphen)
	 * U+2015 (horizontal bar) > U+002D (hyphen) 
	 * 
	 */
	public static String replaceMiscCharacters(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		string = string.replaceAll("[\u2013-\u2015]", "\u002D");
		return string;
	}

	/**
	 * Replace 2 spaces or more with a single space. string will be trimmed before replacing 2 or more space with a single space.
	 */

	public static String removeExtraSpaces(String string)
	{
		if (StringUtils.isEmpty(string))
		{
			return null;
		}
		return string.trim().replaceAll(" +", " ");
	}

	public static void main(String[] args)
	{
		/**
		 * replace Microsoft Smart character 
		 */
		String[] before = new String[] { "this is a simple test \u201a",
				"this is a simple test \u201E", "this is a simple test \u201D",
				"this is a simple test \u2019", };

		String[] after = new String[] { "this is a simple test \u0027", "this is a simple test \"",
				"this is a simple test \"", "this is a simple test \u0027" };

		for (int i = 0; i < before.length; i++)
		{
			String result = replaceMicrosoftSmartCharacters(before[i]);
			if (!after[i].equals(result))
			{
				throw new RuntimeException(
					"replaceMicrosoftSmartCahracters - Result " + result
						+ " doesn't match expected result " + after[i]);
			}
		}

		/**
		 * remove control character 
		 */
		before = new String[] { "this is a simple test \u0000", "this is a simple test \u001f",
				"this is a simple test \u0080", "this is a simple test \u009F",
				"this is \u007Fa simple test \u009F", "this is a simple test \u0088",
				"this is a simple test \u0099", };

		after = new String[] { "this is a simple test ", "this is a simple test ",
				"this is a simple test ", "this is a simple test ", "this is a simple test ",
				"this is a simple test ", "this is a simple test ", };

		for (int i = 0; i < before.length; i++)
		{
			String result = removeControlCharacters(before[i]);
			if (!after[i].equals(result))
			{
				throw new RuntimeException(
					"removeControlCahracters - Result " + result + " doesn't match expected result "
						+ after[i]);
			}
		}

		/**
		 * replace misc character 
		 */
		before = new String[] { "this is a simple test \u2013",
				"this is a simple test \u2014\u2014", "this is a simple test \u2013",
				"this is \u2015a simple test", };

		after = new String[] { "this is a simple test \u002D", "this is a simple test \u002D\u002D",
				"this is a simple test \u002D", "this is \u002Da simple test" };

		for (int i = 0; i < before.length; i++)
		{
			String result = replaceMiscCharacters(before[i]);
			if (!after[i].equals(result))
			{
				throw new RuntimeException(
					"replaceMicrosoftSmartCahracters - Result " + result
						+ " doesn't match expected result " + after[i]);
			}
		}

		/**
		 * remove extra spaces 
		 */
		before = new String[] { "this  is a    simple test", "this  is  a  simple test",
				"this is a simple test  ", };

		after = new String[] { "this is a simple test", "this is a simple test",
				"this is a simple test" };

		for (int i = 0; i < before.length; i++)
		{
			String result = removeExtraSpaces(before[i]);
			if (!after[i].equals(result))
			{
				throw new RuntimeException(
					"removeExtraSpaces - Result " + result + " doesn't match expected result "
						+ after[i]);
			}
		}

	}

}
