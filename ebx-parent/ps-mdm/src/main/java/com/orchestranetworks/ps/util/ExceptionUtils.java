package com.orchestranetworks.ps.util;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.ps.util.functional.*;

/**
 * Extends commons ExceptionUtils to add more utility functions.
 */
public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils
{
	public static final String CAUSED_BY_INDENTATION = "  ";
	public static final String CAUSED_BY_STRING = "caused by: ";
	public static final Pattern FORMAT_PATTERN = Pattern.compile("\\{(\\d+)\\}");

	@SuppressWarnings("rawtypes")
	public static final UnaryPredicate NON_INVOCATION_TARGET_EXCEPTION = UnaryPredicate
		.isInstance(InvocationTargetException.class)
		.not();

	@SuppressWarnings("rawtypes")
	public static final UnaryFunction getMessage = new UnaryFunction()
	{
		@Override
		public Object evaluate(Object object)
		{
			return ((Throwable) object).getMessage();
		}
	};

	/**
	 * Returns the first non-{@link InvocationTargetException} that caused this exception.
	 */
	public static Throwable getNonInvocationTargetException(InvocationTargetException e)
	{
		Throwable cause = e.getCause();
		if (cause instanceof InvocationTargetException)
		{
			return getNonInvocationTargetException((InvocationTargetException) cause);
		}
		return cause;
	}

	/**
	 * Given a throwable instance, find the first throwable instance in the chain of causes matching the given class.
	 * @return an instance of the specified throwable class or null if not found
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Throwable getFirstMatching(Throwable root, Class throwableClass)
	{
		Throwable[] chain = getThrowables(root);
		for (Throwable throwable : chain)
		{
			if (throwableClass.isAssignableFrom(throwable.getClass()))
			{
				return throwable;
			}
		}

		return null;
	}

	/**
	 * Given a throwable instance, find the last throwable instance in the chain of causes matching the given class.
	 * @return an instance of the specified throwable class
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Throwable getLastMatching(Throwable root, Class throwableClass)
	{
		Throwable[] chain = getThrowables(root);
		for (int i = chain.length - 1; i >= 0; i--)
		{
			Throwable throwable = chain[i];
			if (throwableClass.isAssignableFrom(throwable.getClass()))
			{
				return throwable;
			}
		}

		return null;
	}

	/**
	 * Throws the unchecked exception, if non-null.
	 * @param e An unchecked exception or null.
	 * @see #rethrowException
	 */
	public static void throwThisUncheckedException(Throwable e)
	{
		if (e == null)
		{
			return;
		}
		if (e instanceof RuntimeException)
		{
			throw (RuntimeException) e;
		}
		if (e instanceof Error)
		{
			throw (Error) e;
		}
		assert false;
	}

	/**
	 * Returns the message for a potentially nested exception.
	 * @param exception The exception in question
	 * @param predicate The matching predicate to choose which exceptions to display.
	 * @param exceptionFormatter A formatting function that will turn an exception into a string.
	 * @param showClassNames If true then class names are shown.
	 * @param depth The depth of nested exceptions to traverse.
	 * @return The nested message.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String getNestedMessage(
		Throwable exception,
		UnaryPredicate predicate,
		UnaryFunction exceptionFormatter,
		boolean showClassNames,
		boolean useShortForm,
		int depth)
	{
		// create a stringbuffer to hold the messages
		StringBuffer buf = new StringBuffer();
		// get the array of throwables
		Throwable[] throwables = ExceptionUtils.getThrowables(exception);
		int count = 0;
		for (int i = 0; i < throwables.length && count < depth; i++)
		{
			Throwable throwable = throwables[i];
			if (!predicate.test(throwable))
			{
				continue;
			}
			String className = throwable.getClass().getName();
			String message = (String) exceptionFormatter.evaluate(throwable);
			if (!showClassNames && message == null)
			{
				continue;
			}
			// append a newline after the 1st one
			if (count > 0)
			{
				if (useShortForm)
				{
					buf.append(", ");
				}
				else
				{
					buf.append('\n');
				}
			}
			// append the tabs
			if (!useShortForm)
			{
				for (int tabs = 0; tabs < i; tabs++)
				{
					buf.append(CAUSED_BY_INDENTATION);
				}
			}
			// append the caused by
			if (count > 0)
			{
				buf.append(CAUSED_BY_STRING);
			}
			// append the classname
			if (showClassNames)
			{
				buf.append(className);
			}
			// add the message
			if (message != null)
			{
				if (showClassNames)
				{
					buf.append(": ");
				}
				buf.append(message);
			}
			count++;
		}

		return buf.toString();
	}

	public static String getNestedMessage(String title, Throwable exception)
	{
		return title + ".\n\n" + getNestedMessage(exception);
	}

	public static String getNestedMessage(Throwable exception)
	{
		return getNestedMessage(exception, getMessage);
	}

	@SuppressWarnings({ "rawtypes" })
	public static String getNestedMessage(Throwable exception, UnaryFunction exceptionFormatter)
	{
		return getNestedMessage(exception, UnaryPredicate.TRUE, exceptionFormatter);
	}

	public static String getNestedMessage(
		Throwable exception,
		boolean skipInvocationTargetExceptions)
	{
		return getNestedMessage(
			exception,
			skipInvocationTargetExceptions ? NON_INVOCATION_TARGET_EXCEPTION : UnaryPredicate.TRUE);
	}

	@SuppressWarnings({ "rawtypes" })
	public static String getNestedMessage(Throwable exception, UnaryPredicate predicate)
	{
		return getNestedMessage(exception, predicate, getMessage);
	}

	@SuppressWarnings({ "rawtypes" })
	public static String getNestedMessage(
		Throwable exception,
		UnaryPredicate predicate,
		UnaryFunction exceptionFormatter)
	{
		return getNestedMessage(
			exception,
			predicate,
			exceptionFormatter,
			true,
			false,
			Integer.MAX_VALUE);
	}

	/**
	 * Returns a short exception message suitable for display in a single line.
	 */
	public static String getShortMessage(Throwable exception)
	{
		return getShortMessage(exception, getMessage);
	}

	/**
	 * Returns a short exception message suitable for display in a single line.
	 */
	@SuppressWarnings({ "rawtypes" })
	public static String getShortMessage(Throwable exception, UnaryFunction exceptionFormatter)
	{
		// TODO: Have more knowledge of specific exception types or make a systematic
		// improvement to Pantero's exceptions.

		// Usually, adding the first cause makes the message much more useful
		return getNestedMessage(
			exception,
			NON_INVOCATION_TARGET_EXCEPTION,
			exceptionFormatter,
			false,
			true,
			2);
	}

	public static String getStackTraceAsString(Throwable exception)
	{
		StringWriter writer = new StringWriter();
		return getStackTraceAsString(writer, exception);
	}

	/** Append a stack trace to a writer's contents and return it as a string. */
	public static String getStackTraceAsString(StringWriter writer, Throwable exception)
	{
		exception.printStackTrace(new PrintWriter(writer));
		String report = writer.toString();
		String newline = System.getProperty("line.separator");
		if (!"\n".equals(newline))
		{
			report = report.replaceAll(newline, "\n");
		}
		report = report.replaceAll("\t", "    ");
		return report;
	}

	/**
	 * Formats the exception message replacing {0-n} strings with the corresponding entry in the object array.
	 */
	public static String format(String message, Object[] args)
	{
		if (message.indexOf("{") == -1)
		{
			return message;
		}

		Matcher matcher = FORMAT_PATTERN.matcher(message);
		StringBuffer sb = new StringBuffer();
		while (matcher.find())
		{
			String indexStr = matcher.group(1);
			int index = Integer.parseInt(indexStr);
			if (index < args.length)
			{
				String replacement = Objects.toString(args[index], "null");
				// regex replace treats $ specially, escape it.
				if (replacement.indexOf("$") != -1)
				{
					replacement = Strings.CS.replace(replacement, "$", "\\$");
				}
				matcher.appendReplacement(sb, replacement);
			}
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Rethrow any throwable, making it into a PanteroException if it is a checked exception.
	 * @see #throwThisUncheckedException
	 */
	public static void rethrowException(Throwable throwable)
	{
		rethrowException(throwable, RuntimeException.class);
	}

	/**
	 * Rethrow any throwable, making it into a <code>RuntimeException</code> if it is a checked exception.
	 * @param clazz The class of the <code>RuntimeException</code> to throw.
	 * @see #throwThisUncheckedException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void rethrowException(Throwable throwable, Class clazz)
	{
		if (throwable instanceof InvocationTargetException)
		{
			rethrowException(throwable.getCause());
		}
		else if (throwable instanceof RuntimeException)
		{
			throw (RuntimeException) throwable;
		}
		else if (throwable instanceof Error)
		{
			throw (Error) throwable;
		}
		else
		{
			try
			{
				throw (RuntimeException) (clazz.getConstructor(new Class[] { Throwable.class })
					.newInstance(new Object[] { throwable }));
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Rethrow the given exception as a generic RuntimeException, where recursively making generic all wrapped causes.
	 */
	public static void rethrowGeneric(Throwable throwable)
	{
		Throwable genericCopy = getGenericCopy(throwable);
		if (throwable instanceof RuntimeException)
		{
			throw (RuntimeException) genericCopy;
		}
		else if (throwable instanceof Error)
		{
			throw (Error) genericCopy;
		}
		throw new RuntimeException(genericCopy);
	}

	/**
	 * Rethrows the specified exception as an InvocationTargetException that includes the current thread's name in the
	 * message.
	 */
	public static void rethrowInvocationTargetException(Throwable throwable)
		throws InvocationTargetException
	{
		String message = "Exception in thread \"" + Thread.currentThread().getName() + "\"";
		rethrowInvocationTargetException(throwable, message);
	}

	/**
	 * Rethrows the specified exception as an InvocationTargetException using the specified message.
	 */
	public static void rethrowInvocationTargetException(Throwable throwable, String title)
		throws InvocationTargetException
	{
		if (throwable instanceof RuntimeException && shouldNotWrapException(throwable))
		{
			throw (RuntimeException) throwable;
		}
		String message = getNestedMessage(title, throwable);
		throw new InvocationTargetException(throwable, message);
	}

	/**
	 * Returns true if the specified exception should never be wrapped.
	 */
	public static boolean shouldNotWrapException(Throwable exception)
	{
		return false;
	}

	/**
	 * Throws a wrapped exception that provides more details regarding the causing exception. Note that exceptions that
	 * should not be wrapped will be rethrown directly.
	 */
	public static void throwWrappingException(String message, Throwable cause)
	{
		if (shouldNotWrapException(cause))
		{
			throw (RuntimeException) cause;
		}
		throw new RuntimeException(message, cause);
	}

	/**
	 * Throws a wrapped exception that provides more details regarding the causing exception. Note that exceptions that
	 * should not be wrapped (such as CanceledException) will be rethrown directly.
	 */
	public static void throwWrappingException(String message, Throwable cause, Object... objects)
	{
		if (shouldNotWrapException(cause))
			throw (RuntimeException) cause;
		String formattedMessage = objects == null ? message
			: MessageFormat.format(message, objects);
		throw new RuntimeException(formattedMessage, cause);
	}

	/**
	 * Return a generic copy of the specified throwable. Recursively make generic all wrapped causes.
	 */
	public static Throwable getGenericCopy(Throwable throwable)
	{
		if (throwable == null)
		{
			return null;
		}
		Throwable cause = getGenericCopy(throwable.getCause());
		String message = throwable.toString();
		Throwable result;
		if (throwable instanceof Error)
		{
			result = new Error(message, cause);
		}
		else if (throwable instanceof RuntimeException)
		{
			result = new RuntimeException(message, cause);
		}
		else
		{
			result = new Exception(message, cause);
		}
		result.setStackTrace(throwable.getStackTrace());
		return result;
	}

}
