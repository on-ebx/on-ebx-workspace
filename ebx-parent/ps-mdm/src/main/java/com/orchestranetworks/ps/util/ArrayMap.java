package com.orchestranetworks.ps.util;

import java.util.*;

public class ArrayMap<K, V> extends AbstractMap<K, V>
{

	/***************************************************
	 * Instance variables.
	 ***************************************************/

	/** The key array. */
	private final K[] keys;
	/** The value array. */
	private final V[] values;

	/***************************************************
	 * Constructors
	 ***************************************************/

	/**
	 * Construct the array map over the object array.
	 * @throws IllegalArgumentException If the keys array isn't the same length as the values array.
	 */
	public ArrayMap(K[] keys, V[] values)
	{
		if (keys.length != values.length)
		{
			throw new IllegalArgumentException(
				"Must have an even number of arguments that form key/value pairs");
		}

		this.keys = keys.clone();
		this.values = values.clone();
	}

	/**
	 * Construct the array map over the object array.
	 * 
	 * @param v Alternating key value pairs.
	 * @throws IllegalArgumentException If the array doesn't have an even number of arguments that form key/value pairs
	 */
	@SuppressWarnings("unchecked")
	public ArrayMap(Object v[])
	{
		if (v.length % 2 != 0)
		{
			throw new IllegalArgumentException(
				"Must have an even number of arguments that form key/value pairs");
		}

		int n = v.length / 2;
		keys = (K[]) new Object[n];
		values = (V[]) new Object[n];

		// copy the keys and values into their own arrays
		for (int i = 0; i < n; i++)
		{
			keys[i] = (K) v[2 * i];
			values[i] = (V) v[2 * i + 1];
		}
	}

	/***************************************************
	 * Instance methods
	 ***************************************************/

	/** Get the backing key array. */
	public K[] getKeys()
	{
		return keys;
	}

	/** Get the backing value array. */
	public V[] getValues()
	{
		return values;
	}

	/**
	 * Scans the key array for the key. If found returns the value at the array position the key was found at.
	 */
	@Override
	public V get(Object key)
	{
		for (int i = 0; i < keys.length; i++)
		{
			if (key.equals(keys[i]))
			{
				return values[i];
			}
		}
		return null;
	}

	/** Check if a certain key is in the key array. */
	@Override
	public boolean containsKey(Object key)
	{
		for (K key2 : keys)
		{
			if (key2.equals(key))
			{
				return true;
			}
		}
		return false;
	}

	/** Check if a certain value is in the value array. */
	@Override
	public boolean containsValue(Object value)
	{
		for (V value2 : values)
		{
			if (value2.equals(value))
			{
				return true;
			}
		}
		return false;
	}

	/** Constructs a list out of the array of values. */
	@Override
	public Collection<V> values()
	{
		return Arrays.asList(values);
	}

	/**
	 * Warning, this method must construct a new key set object and a new iterator object.
	 */
	@Override
	public Set<K> keySet()
	{
		return new AbstractSet<K>()
		{
			@Override
			public int size()
			{
				return keys.length;
			}

			@Override
			public Iterator<K> iterator()
			{
				return new Iterator<K>()
				{
					int index = 0;

					@Override
					public boolean hasNext()
					{
						return index < keys.length;
					}

					@Override
					public K next()
					{
						return keys[index++];
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/**
	 * Warning, this method must construct new entry set, iterator and {@link java.util.Map.Entry} objects. It is
	 * somewhat expensive.
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		return new AbstractSet<Map.Entry<K, V>>()
		{
			@Override
			public int size()
			{
				return keys.length;
			}

			@Override
			public Iterator<Map.Entry<K, V>> iterator()
			{
				return new Iterator<Map.Entry<K, V>>()
				{
					int index = 0;

					@Override
					public boolean hasNext()
					{
						return index < keys.length;
					}

					@Override
					public Map.Entry<K, V> next()
					{
						Map.Entry<K, V> entry = new Map.Entry<K, V>()
						{
							int entryIndex = index;

							@Override
							public K getKey()
							{
								return keys[entryIndex];
							}

							@Override
							public V getValue()
							{
								return values[entryIndex];
							}

							@Override
							public V setValue(V value)
							{
								V oldValue = values[entryIndex];
								values[entryIndex] = value;
								return oldValue;
							}
						};

						index++;
						return entry;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	/** Get the size of the map. */
	@Override
	public int size()
	{
		return keys.length;
	}

	/** Is the map empty. */
	@Override
	public boolean isEmpty()
	{
		return keys.length == 0;
	}

	/** @throws UnsupportedOperationException */
	@Override
	public V put(Object key, Object value)
	{
		throw new UnsupportedOperationException("Can't put data into ArrayMap");
	}

	/** @throws UnsupportedOperationException */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Can't clear ArrayMap");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof ArrayMap))
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}
		final ArrayMap arrayMap = (ArrayMap) o;
		if (!Arrays.equals(keys, arrayMap.keys))
		{
			return false;
		}
		if (!Arrays.equals(values, arrayMap.values))
		{
			return false;
		}
		return true;
	}

	/** Computes the hashcode from the keys and values. Caches the hashcode. */
	@Override
	public int hashCode()
	{
		if (hashcode == -1)
		{
			hashcode = 0;
			for (int i = 0; i < keys.length; i++)
			{
				hashcode += (keys[i] == null ? 0 : keys[i].hashCode())
					^ (values[i] == null ? 0 : values[i].hashCode());
			}
		}

		return hashcode;
	}

	/** Cached hashcode. */
	private int hashcode = -1;
}
