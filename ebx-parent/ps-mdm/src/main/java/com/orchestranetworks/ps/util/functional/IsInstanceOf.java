package com.orchestranetworks.ps.util.functional;

import java.io.*;

/**
 * {@link #test Tests} <code>true</code> iff its argument {@link Class#isInstance is an instance} of some specified
 * {@link Class Class}. credit: org.apache.commons.functor. copied to extend functionality for ON.
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class IsInstanceOf extends UnaryPredicate implements Serializable
{

	// constructor
	// ------------------------------------------------------------------------
	public IsInstanceOf(Class klass)
	{
		this.klass = klass;
	}

	// predicate interface
	// ------------------------------------------------------------------------

	@Override
	public final boolean test(Object obj)
	{
		return klass.isInstance(obj);
	}

	// attributes
	// ------------------------------------------------------------------------
	private Class klass;

	@Override
	public String toString()
	{
		return "IsInstanceOf{" + "class=" + klass + "}";
	}
}
