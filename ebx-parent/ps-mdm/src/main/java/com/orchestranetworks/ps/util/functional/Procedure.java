package com.orchestranetworks.ps.util.functional;

import java.lang.reflect.*;

import com.orchestranetworks.ps.util.*;

/**
 * A functor that takes no arguments and returns no value. Note that this functor implements the Runnable
 * interface, making all <code>Procedure</code>s immediately usable in a number of contexts (Swing, Jelly, etc.).
 * <br>credit: org.apache.commons.functor. copied to extend functionality for ON.
 */
public abstract class Procedure implements Runnable
{

	public static final Procedure NoOp = new Procedure()
	{
		@Override
		public void run()
		{
		}
	};

	/** Returns a procedure that will run the method reflectively. */
	public static final Procedure reflective(final Object obj, final String methodName)
	{
		try
		{
			return reflective(obj, obj.getClass().getMethod(methodName, (Class[]) null));
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}

	/** Returns a procedure that will run the method reflectively. */
	public static final Procedure reflective(final Object obj, final Method method)
	{
		return new Procedure()
		{
			@Override
			public void run()
			{
				try
				{
					method.invoke(obj, (Object[]) null);
				}
				catch (Throwable e)
				{
					ExceptionUtils.rethrowException(e);
				}
			}
		};
	}

	public Procedure then(final Procedure doThis)
	{
		final Procedure me = this;
		return new Procedure()
		{
			@Override
			public void run()
			{
				me.run();
				doThis.run();
			}
		};
	}

	/** On exception call this procedure. */
	public Procedure onException(final UnaryProcedure<Throwable> exceptionHandler)
	{
		final Procedure me = this;
		return new Procedure()
		{
			@Override
			public void run()
			{
				try
				{
					me.run();
				}
				catch (Throwable e)
				{
					exceptionHandler.run(e);
				}
			}
		};
	}

	/** Returns true if the procedure is null or a NoOp. */
	public static boolean isNoOp(final Procedure procedure)
	{
		return procedure == null || procedure.equals(Procedure.NoOp);
	}
}
