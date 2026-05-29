package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Generator for each element of a collection. credit: org.apache.commons.functor. copied to extend functionality for
 * ON.
 */
public final class EachElement
{
	private EachElement()
	{
		// utility class
	}

	public static final <T> Generator<T> from(Collection<T> collection)
	{
		return Generator.fromCollection(collection);
	}

	public static final <K, V> Generator<Map.Entry<K, V>> from(Map<K, V> map)
	{
		return Generator.fromIterator(map.entrySet().iterator());
	}

	public static final <K, V> Generator<K> fromKeys(Map<K, V> map)
	{
		return Generator.fromIterator(map.keySet().iterator());
	}

	public static final <K, V> Generator<V> fromValues(Map<K, V> map)
	{
		return Generator.fromIterator(map.values().iterator());
	}

	public static final <T> Generator<T> from(T[] array)
	{
		return Generator.fromArray(array);
	}

	public static final <T> Generator<T> from(Iterator<T> iter)
	{
		return Generator.fromIterator(iter);
	}

	public static final <T> Generator<T> from(Enumeration<T> enumeration)
	{
		return Generator.fromEnumeration(enumeration);
	}
}
