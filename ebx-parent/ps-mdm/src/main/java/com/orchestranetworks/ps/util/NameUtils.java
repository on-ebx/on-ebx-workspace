package com.orchestranetworks.ps.util;

import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.util.functional.*;

public class NameUtils
{
	public static final String DEFAULT_DELIMITER = " ";
	public static final char UNDERSCORE = '_';
	public static final char AT = '@';
	public static final int MAX_UNIQUE_NAME_COUNT = 1000;

	//	List of java reserved words/keywords
	private static List<String> javaReservedKeywords = Arrays.asList(
		new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
				"class", "const", "continue", "default", "do", "double", "else", "enum", "extends",
				"false", "final", "finally", "float", "for", "goto", "if", "implements", "import",
				"instanceof", "int", "interface", "long", "native", "new", "null", "package",
				"private", "protected", "public", "return", "short", "static", "strictfp", "super",
				"switch", "synchronized", "this", "throw", "throws", "transient", "true", "try",
				"void", "volatile", "while" });

	public static boolean isValidJavaName(String aString)
	{
		if (aString == null || aString.length() == 0)
		{
			return false;
		}

		if (javaReservedKeywords.contains(aString))
		{
			return false;
		}

		int len = aString.length();
		boolean first = true;
		for (int i = 0; i < len; i++)
		{
			char c = aString.charAt(i);
			if (first)
			{
				first = false;
				if (!Character.isJavaIdentifierStart(c))
				{
					return false;
				}
			}
			else if (!Character.isJavaIdentifierPart(c))
			{
				return false;
			}
		}
		return true;
	}

	public static boolean isValidJavaPackageName(String aString)
	{
		// check first and last characters
		if (aString.endsWith(".") || aString.startsWith("."))
		{
			return false;
		}
		if (Strings.CI.contains(aString, ".."))
		{
			return false;
		}
		String[] packageNames = StringUtils.split(aString, '.');
		for (String packageName : packageNames)
		{
			String pack = packageName;
			if (!isValidJavaName(pack))
			{
				return false;
			}
		}
		return true;
	}

	protected static boolean isSimpleJavaName(String aString)
	{
		if (StringUtils.contains(aString, '.'))
		{
			return false;
		}
		return isValidJavaName(aString);
	}

	private static final String[][] reservedCharPairs = new String[][] {
			new String[] { ".", "_DOT_" }, new String[] { "!", "_BANG_" },
			new String[] { "@", "_AT_" }, new String[] { "#", "_POUND_" },
			new String[] { "%", "_PERCENT_" }, new String[] { "^", "_CARAT_" },
			new String[] { "&", "_AMP_" }, new String[] { "|", "_PIPE_" },
			new String[] { "*", "_STAR_" }, new String[] { "(", "_LPAREN_" },
			new String[] { ")", "_RPAREN_" }, new String[] { "{", "_LCURLY_" },
			new String[] { "}", "_RCURLY_" }, new String[] { "[", "_LSQUARE_" },
			new String[] { "]", "_RSQUARE_" }, new String[] { "\"", "_DQUOTE_" },
			new String[] { "'", "_SQUOTE_" }, new String[] { ";", "_SEMICOLON_" },
			new String[] { ":", "_COLON_" }, new String[] { "\\", "_BACKSLASH_" },
			new String[] { "/", "_SLASH_" }, new String[] { ",", "_COMMA_" },
			new String[] { "-", "_DASH_" }, new String[] { "+", "_PLUS_" },
			new String[] { "=", "_EQ_" }, new String[] { "<", "_LANGLE_" },
			new String[] { ">", "_RANGLE_" }, new String[] { "?", "_Q_" },
			new String[] { "`", "_TICK_" }, new String[] { "~", "_TILDE_" }, };

	/** Converts a string to a valid java name. */
	public static String toJavaName(String str)
	{
		return toJavaName(str, false);
	}

	public static String toCleanJavaName(String str)
	{
		return toJavaName(str, true);
	}

	private static String toJavaName(String str, boolean clean)
	{
		if (str == null)
		{
			return "defaultName";
		}
		if (str.length() == 0)
		{
			return "_EMPTY_";
		}
		if (isSimpleJavaName(str))
		{
			return str;
		}
		String name = StringUtils.deleteWhitespace(str);
		if ("".equals(name))
		{
			return "defaultName";
		}
		if (javaReservedKeywords.contains(name))
		{
			return "_" + name;
		}
		//if first char is not a valid start character add leading underscore
		if (!Character.isJavaIdentifierStart(name.charAt(0)))
		{
			name = "_" + name;
		}
		for (String[] reservedCharPair : reservedCharPairs)
		{
			String replacement = clean ? "" : reservedCharPair[1];
			name = Strings.CI.replace(name, reservedCharPair[0], replacement);
		}
		for (int i = 1; i < name.length(); i++)
		{
			if (!Character.isJavaIdentifierPart(name.charAt(i)))
			{
				name = name.replace(name.charAt(i), '_');
			}
		}
		if ("".equals(name) && clean)
		{
			return "defaultName";
		}
		return name;
	}

	public static String findPackageName(String name)
	{
		if (name == null)
		{
			return null;
		}
		int lastInd = name.lastIndexOf(".");
		if (lastInd == -1)
		{
			return null;
		}
		return name.substring(0, lastInd);
	}

	public static String findNameOnly(String name)
	{
		if (name == null)
		{
			return null;
		}
		int lastInd = name.lastIndexOf("$");
		if (lastInd >= 0)
		{
			return name.substring(lastInd + 1);
		}
		lastInd = name.lastIndexOf(".");
		if (lastInd >= 0)
		{
			return name.substring(lastInd + 1);
		}
		return name;
	}

	public static List<String> getJavaReservedKeywords()
	{
		return javaReservedKeywords;
	}

	private static Pattern hyphenateRE = Pattern.compile("([a-z])([A-Z])");

	public static String hyphenate(String string)
	{
		if (string == null || string.length() == 0)
		{
			return "";
		}
		return hyphenateRE.matcher(string).replaceAll("$1_$2").toUpperCase();
	}

	/**
	 * Returns a java Class name from the given name
	 */
	public static String makeClassName(String name)
	{
		return toJavaName(toCamelCase(name, false));
	}

	/**
	 * Return a java Method/Attribute name for a given name (column)
	 */
	public static String makeMemberName(String name)
	{
		return toJavaName(toCamelCase(name, true), true);
	}

	/**
	 * Return camel case name for one with underscores
	 */
	private static String toCamelCase(String name, boolean member)
	{
		String upperCase = name.toUpperCase();
		String lowerCase = name.toLowerCase();
		boolean hasUnderscore = name.indexOf(UNDERSCORE) >= 0;
		boolean hasAt = name.indexOf(AT) >= 0;
		if (!hasUnderscore && !hasAt)
		{ //less work to do
			String result = name;
			if (lowerCase.equals(name) || upperCase.equals(name))
			{
				result = lowerCase;
			}
			return member ? StringUtils.uncapitalize(result) : StringUtils.capitalize(result);
		}
		// otherwise do the camel case base on underscores.
		StringBuffer nameBuffer = new StringBuffer();
		boolean capNext = false;
		char[] nameChars = lowerCase.toCharArray();
		for (char nameChar : nameChars)
		{
			char c = nameChar;
			if (UNDERSCORE == c)
			{
				capNext = true;
				continue;
			}
			else if (AT == c)
			{ // Ignoring a @ in the name
				continue;
			}
			if (capNext)
			{
				c = Character.toUpperCase(c);
				capNext = false;
			}
			nameBuffer.append(c);
		}
		String result = nameBuffer.toString();
		return member ? StringUtils.uncapitalize(result) : StringUtils.capitalize(result);
	}

	/**
	 * Generate a new unique name using baseName as a prefix. The collection of names can contain either strings
	 * or we use the getName function to derive a name for the objects. Returns null if a unique name cannot be found.
	 */
	public static String generateUniqueName(
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String baseName)
	{
		return generateUniqueName(existingNames, getNameFunction, baseName, DEFAULT_DELIMITER);
	}

	/**
	 * Generate a new unique name using baseName as a prefix. The collection of names can contain either strings
	 *  we use the getName function to derive a name for the objects. Returns null if a unique name cannot be found.
	 */
	public static String generateUniqueName(
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String baseName,
		String delimiter)
	{
		return generateUniqueName(null, existingNames, getNameFunction, baseName, delimiter);
	}

	/**
	 * Generate a new unique name using baseName as a prefix. The collection of names can contain either strings
	 * or we use the getName function to derive a name for the objects. Returns null if a unique name cannot be found.
	 * @param existingName Another existing name to consider, in addition to those in the collection
	 */
	public static String generateUniqueName(
		String existingName,
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String baseName,
		String delimiter)
	{
		return generateUniqueName(
			existingName,
			existingNames,
			getNameFunction,
			baseName,
			1,
			delimiter);
	}

	/**
	 * Generate a new unique name using baseName as a prefix. The collection of names can contain either strings
	 * or we use the getName function to derive a name for the objects. Returns null if a unique name cannot be found.
	 * @param startIndex The first number to use as a suffix when testing for unique names.
	 */
	public static String generateUniqueName(
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String baseName,
		int startIndex,
		String delimiter)
	{
		return generateUniqueName(
			null,
			existingNames,
			getNameFunction,
			baseName,
			startIndex,
			delimiter);
	}

	/**
	 * Generate a new unique name using baseName as a prefix. The collection of names can contain either strings
	 * or we use the getName function to derive a name for the objects. Returns null if a unique name cannot be found.
	 * @param existingName Another existing name to consider, in addition to those in the collection
	 * @param startIndex The first number to use as a suffix when testing for unique names.
	 */
	private static String generateUniqueName(
		String existingName,
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String baseName,
		int startIndex,
		String delimiter)
	{
		for (int i = startIndex; i < MAX_UNIQUE_NAME_COUNT; i++)
		{
			String testName = generateName(baseName, i, delimiter);
			if (!testName.equals(existingName)
				&& !isNameTaken(existingNames, getNameFunction, testName))
			{
				return testName;
			}
		}

		return null;
	}

	/**
	 * Generate a new unique name using baseName as a prefix. Returns null if a unique name cannot be found.
	 */
	private static String generateUniqueName(
		String baseName,
		UnaryPredicate<String> isNameTaken,
		int startIndex,
		String delimiter)
	{
		for (int i = startIndex; i < MAX_UNIQUE_NAME_COUNT; i++)
		{
			String testName = generateName(baseName, i, delimiter);
			if (!isNameTaken.test(testName))
			{
				return testName;
			}
		}

		return null;
	}

	public static String generateName(String baseName, int i)
	{
		return generateName(baseName, i, DEFAULT_DELIMITER);
	}

	public static String generateName(String baseName, int i, String delimiter)
	{
		return baseName + delimiter + i;
	}

	public static boolean isNameTaken(
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String testName)
	{
		return isNameTaken(existingNames, getNameFunction, testName, false);
	}

	/**
	 * Is the testName passed in already in the collection of names. The collection of names can contain either strings
	 * or we use the getName function to derive a name for the objects.
	 */
	public static boolean isNameTaken(
		Collection<?> existingNames,
		UnaryFunction<Object, String> getNameFunction,
		String testName,
		boolean ignoreCase)
	{
		for (Object o : existingNames)
		{
			String name = getName(o, getNameFunction);
			boolean result = ignoreCase ? Strings.CI.equals(name, testName)
				: Objects.equals(name, testName);
			if (result)
			{
				return true;
			}
		}
		return false;
	}

	public static String getName(Object o, UnaryFunction<Object, String> getNameFunction)
	{
		if (o == null)
			return null;
		String name = null;
		if (getNameFunction != null)
			name = getNameFunction.evaluate(o);
		if (name == null)
			name = o.toString();
		return name;
	}

	/**
	 * Returns true if the two objects are both null or both have the same name.
	 */
	public static boolean haveSameName(
		Object o1,
		Object o2,
		UnaryFunction<Object, String> getNameFunction)
	{
		if (o1 == null && o2 == null)
		{
			return true;
		}
		if (o1 == null || o2 == null)
		{
			return false;
		}
		return Objects.equals(getName(o1, getNameFunction), getName(o2, getNameFunction));
	}

	/**
	 * Generate a unique name for an object given a UnaryPredicate to determine if the name is in use.
	 */
	public static String generateUniqueName(
		String baseName,
		String delimiter,
		UnaryPredicate<String> isNameTaken)
	{
		return generateUniqueName(baseName, isNameTaken, 1, delimiter);
	}

}
