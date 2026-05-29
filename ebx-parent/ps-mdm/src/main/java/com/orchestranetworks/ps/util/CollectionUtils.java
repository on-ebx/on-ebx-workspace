package com.orchestranetworks.ps.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.collections4.*;

import com.orchestranetworks.ps.util.functional.*;

/**
 * Collection utilities.
 */
public class CollectionUtils
{
	public static <T> void addAll(
		Collection<T> toCollection,
		Collection<? extends T> fromCollection)
	{
		if (toCollection != null && fromCollection != null)
		{
			toCollection.addAll(fromCollection);
		}
	}

	public static <T> void addAll(Collection<T> toCollection, Iterator<? extends T> fromIterator)
	{
		if (toCollection != null && fromIterator != null)
		{
			for (; fromIterator.hasNext();)
			{
				toCollection.add(fromIterator.next());

			}
		}
	}

	public static <T> void addAll(Collection<? super T> toCollection, T[] fromArray)
	{
		if (toCollection != null && fromArray != null)
		{
			for (T t : fromArray)
			{
				toCollection.add(t);
			}
		}
	}

	/**
	 * Returns a singleton list of the specified object, except for null it will return an empty
	 * list.
	 */
	public static <T> List<T> toList(T object)
	{
		return object == null ? Collections.<T> emptyList() : Collections.singletonList(object);
	}

	/**
	 * Returns a new list containing each item in the iterator.
	 */
	public static <T> List<T> toList(Iterator<T> iterator)
	{
		return IteratorUtils.toList(iterator);
	}

	/**
	 * If the collection is empty null is returned, if not the 1st element is returned.
	 */
	public static <T> T getFirstOrNull(Collection<T> col)
	{
		if (col instanceof List)
		{
			return getFirstOrNull((List<T>) col);
		}
		return getFirstOrNull(col.iterator());
	}

	/**
	 * If the list is empty null is returned, if not the 1st element is returned.
	 */
	public static <T> T getFirstOrNull(List<T> list)
	{
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	/**
	 * If the list is empty null is returned, if not the last element is returned.
	 */
	public static <T> T getLastOrNull(Collection<T> collection)
	{
		if (collection == null)
		{
			return null;
		}
		int size = collection.size();
		return size > 0 ? new ArrayList<>(collection).get(size - 1) : null;
	}

	/**
	 * If the array is empty null is returned, if not the 1st element is returned.
	 */
	public static <T> T getFirstOrNull(T[] array)
	{
		return array.length > 0 ? array[0] : null;
	}

	/**
	 * If the collection is empty null is returned, if not the 1st element is returned.
	 */
	public static <T> T getFirstOrNull(Iterator<T> iterator)
	{
		if (iterator.hasNext())
		{
			return iterator.next();
		}
		else
		{
			return null;
		}
	}

	/**
	 * If the collection is empty null is returned. If there is only 1 item in the collection it is
	 * returned. If there
	 * is more than 1 element in the collection an exception is thrown.
	 */
	public static <T> T getFirstNullOrThrow(Collection<T> col) throws IllegalStateException
	{
		return getFirstNullOrThrow(col.iterator());
	}

	/**
	 * If the iterator is empty null is returned. If there is only 1 item in the iterator it is
	 * returned. If there is
	 * more than 1 element in the iterator an exception is thrown.
	 */
	public static <T> T getFirstNullOrThrow(Iterator<T> iterator) throws IllegalStateException
	{
		if (iterator.hasNext())
		{
			T object = iterator.next();
			if (iterator.hasNext())
			{
				throw new IllegalStateException("More than one element found");
			}
			return object;
		}

		return null;
	}

	/**
	 * Get the value stored at the specific index of the ordered map.
	 */
	public static <K, V> V get(LinkedHashMap<K, V> map, int index)
	{
		Iterator<V> iterator = map.values().iterator();
		while (iterator.hasNext())
		{
			V value = iterator.next();
			if (index == 0)
			{
				return value;
			}
			index--;
		}
		return null;
	}

	public static <T> List<T> reverse(Collection<T> values)
	{
		List<T> reverse = new ArrayList<>(values);
		Collections.reverse(reverse);
		return reverse;
	}

	public static <T> Iterator<T> reverseIterator(List<T> list)
	{
		return reverseIterator(list, list.size() - 1);
	}

	public static <T> Iterator<T> reverseIterator(List<T> list, int startAt)
	{
		return reverseIterator(list, startAt, 0);
	}

	public static <T> Iterator<T> reverseIterator(
		final List<T> list,
		final int startAt,
		final int endAt)
	{
		if (startAt < -1 || startAt > list.size() - 1 || endAt < 0 || endAt > list.size())
		{
			throw new IndexOutOfBoundsException(
				"Start Index: " + startAt + ", End Index: " + endAt);
		}

		return new Iterator<T>()
		{
			private final ListIterator<T> listIterator = list.listIterator(startAt + 1);

			@Override
			public boolean hasNext()
			{
				return listIterator.hasPrevious()
					&& (endAt == 0 || listIterator.previousIndex() >= endAt);
			}

			@Override
			public T next()
			{
				return listIterator.previous();
			}

			@Override
			public void remove()
			{
				listIterator.remove();
			}
		};
	}

	/** Get the array class for the element type passed in. */
	@SuppressWarnings("rawtypes")
	public static Class getArrayClass(Class elementType)
	{
		return CollectionUtils.createArray(elementType, 0).getClass();
	}

	/**
	 * Creates a new array of type clazz. If clazz is an array type, an array of the type of the
	 * clazz array is created,
	 * not an array of arrays. clazz.getComponentType() is used to extract the type if clazz is an
	 * array.
	 */
	@SuppressWarnings("rawtypes")
	public static Object[] createArray(Class clazz, int size)
	{
		if (clazz.isArray())
		{
			clazz = clazz.getComponentType();
		}
		return (Object[]) Array.newInstance(clazz, size);
	}

	/**
	 * Calls createArray(clazz, size) and then copies the values from defaultValues into the new
	 * array.
	 */
	@SuppressWarnings("rawtypes")
	public static Object[] createArray(Class clazz, int size, Object[] defaultValues)
	{
		Object[] objs = createArray(clazz, size);
		if (defaultValues.length > 0)
		{
			System.arraycopy(defaultValues, 0, objs, 0, defaultValues.length);
		}
		return objs;
	}

	/**
	 * Creates an immutable map with 1 entry. The key will be argName and the value will be
	 * argValue.
	 * 
	 * @return The 1 element immutable map.
	 */
	public static Map<String, Object> createKeyValMap(String argName, Object argValue)
	{
		return new ArrayMap<>(new Object[] { argName, argValue });
	}

	/**
	 * Creates a map from a collection and a unary key function
	 */
	public static <K, V> Map<K, V> createMap(
		Collection<V> collection,
		UnaryFunction<? super V, K> keyfunction)
	{
		if (isEmpty(collection))
		{
			return Collections.<K, V> emptyMap();
		}
		Map<K, V> result = new LinkedHashMap<>();
		for (V value : collection)
		{
			K key = keyfunction.evaluate(value);
			if (!result.containsKey(key))
			{
				result.put(key, value);
			}
		}
		return result;
	}

	/** Return a new hash map, concurrent or not as specified */
	public static <K, V> Map<K, V> getMap(boolean concurrent)
	{
		return concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
	}

	/**
	 * Return a new hash map, concurrent or not as specified, with the specified initial capacity
	 */
	public static <K, V> Map<K, V> getMap(boolean concurrent, int initialCapacity)
	{
		return concurrent ? new ConcurrentHashMap<>(initialCapacity)
			: new HashMap<>(initialCapacity);
	}

	/**
	 * Sort the collection and returns it as a new sorted list.
	 * 
	 * @return If collection passed in is null a new empty list is returned.
	 */
	public static <T> List<T> sort(Collection<T> collection, Comparator<? super T> comparator)
	{
		if (collection == null || collection.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<T> list = new ArrayList<>(collection);
		Collections.sort(list, comparator);
		return list;
	}

	/**
	 * Orders the collection by ComparableFunction, descending or ascending as specified.
	 * 
	 * @return If collection passed in is null a new empty list is returned.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<T> orderBy(
		Collection<T> collection,
		ComparableFunction comparable,
		boolean ascending)
	{
		if (collection == null || collection.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<T> list = new ArrayList<>(collection);
		Collections.sort(list, new FunctionalComparator(comparable));
		if (!ascending)
		{
			list = reverse(list);
		}
		return list;
	}

	/**
	 * Orders the collection by ComparableFunction, descending or ascending as specified.
	 * 
	 * @return If collection passed in is null a new empty list is returned.
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> orderBy(T[] array, ComparableFunction comparable, boolean ascending)
	{
		if (array == null || array.length == 0)
		{
			return new ArrayList<>(0);
		}
		List<T> list = Arrays.asList(array);
		return orderBy(list, comparable, ascending);
	}

	/**
	 * Flattens a collection of collections (of collections ...) into a single collection. Returns
	 * the flattened
	 * collection.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection flatten(Collection collection)
	{
		Collection flattened = new ArrayList(collection.size());
		flatten(flattened, collection, true);
		return flattened;
	}

	/**
	 * Flattens a collection of collections (of collections ...) into a single collection. Returns
	 * the flattened
	 * collection.
	 */
	public static <T> Collection<T> flatten(Collection<?> collection, boolean skipNulls)
	{
		Collection<T> flattened = new ArrayList<>(collection.size());
		flatten(flattened, collection, skipNulls);
		return flattened;
	}

	/**
	 * Adds all of the elements, and subelements (and so on) to the collection passed in.
	 * 
	 * @param toFill
	 *            The collection to fill with the flattened elements.
	 * @param root
	 *            The root collection to traverse and flatten.
	 * @param skipNulls
	 *            If true, embedded nulls are skipped, if false they are included in the resulting
	 *            collection
	 */
	@SuppressWarnings("unchecked")
	public static <T> void flatten(Collection<T> toFill, Collection<?> root, boolean skipNulls)
	{
		for (Object object : root)
		{
			if (object instanceof Collection)
			{
				flatten(toFill, (Collection<T>) object, skipNulls);
			}
			else if (!skipNulls || object != null)
			{
				toFill.add((T) object);
			}
		}
	}

	/**
	 * Concatenates 2 lists. If list 1 is empty list 2 is returned, and vice versa. If neither list
	 * is empty a new list
	 * is created containing all of the items from both lists and returned.
	 */
	public static <T> List<? extends T> concat(List<? extends T> c1, List<? extends T> c2)
	{
		if (c1 == null || c1.isEmpty())
		{
			return c2;
		}
		if (c2 == null || c2.isEmpty())
		{
			return c1;
		}
		List<T> newlist = new ArrayList<>(c1.size() + c2.size());
		newlist.addAll(c1);
		newlist.addAll(c2);
		return newlist;
	}

	/**
	 * Returns true if the fullCollection starts with the items from the subCollection.
	 */
	public static <T> boolean startsWith(
		Collection<T> fullCollection,
		Collection<? extends T> subCollection)
	{
		int fullPathSize = fullCollection.size();
		int subPathSize = subCollection.size();
		if (subPathSize > fullPathSize)
		{
			return false;
		}
		Iterator<T> fullIterator = fullCollection.iterator();
		for (T subItem : subCollection)
		{
			T fullItem = fullIterator.next();
			if (!Objects.equals(fullItem, subItem))
			{
				return false;
			}
		}
		return true;
	}

	/******************************************
	 * Inner classes.
	 *****************************************/

	/** Checks if elements tested are members of the collection passed in. */
	public static <T> UnaryPredicate<T> isMember(final Collection<T> collection)
	{
		if (collection == null)
		{
			throw new IllegalArgumentException("Cannot determine isMember on null collection");
		}
		return new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T obj)
			{
				return collection.contains(obj);
			}
		};
	}

	/** Checks if elements tested are NOT members of the collection passed in. */
	public static final <T> UnaryPredicate<T> isNotMember(final Collection<T> collection)
	{
		if (collection == null)
		{
			throw new IllegalArgumentException("Cannot determine isNotMember on null collection");
		}
		return new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T obj)
			{
				return !collection.contains(obj);
			}
		};
	}

	/**
	 * Compares elements from a collection and sorts them into the order they appear in the list
	 * passed into the
	 * constructor.
	 */
	public static class CompareByPosition<C, S> implements Comparator<C>
	{
		private final UnaryFunction<C, S> keyFunction;
		private List<S> inCollection = null;
		private boolean ascending = true;

		public CompareByPosition(List<S> inCollection, boolean ascending)
		{
			this(null, inCollection, ascending);
		}

		public CompareByPosition(
			UnaryFunction<C, S> keyFunction,
			List<S> inCollection,
			boolean ascending)
		{
			this.keyFunction = keyFunction;
			this.inCollection = inCollection;
			this.ascending = ascending;
		}

		@SuppressWarnings("unchecked")
		@Override
		public int compare(Object o1, Object o2)
		{
			int i1 = inCollection
				.indexOf(keyFunction == null ? (C) o1 : keyFunction.evaluate((C) o1));
			int i2 = inCollection
				.indexOf(keyFunction == null ? (C) o2 : keyFunction.evaluate((C) o2));
			if (i1 == i2)
			{
				return 0;
			}
			int result = i1 < i2 ? -1 : 1;
			return ascending ? result : -result;
		}
	}

	/**
	 * Compares elements from a collection and sorts them into the order they appear in the list
	 * passed into the constructor.
	 */
	public static class CompareByExampleList<C> implements Comparator<C>
	{
		private List<C> inCollection = null;
		private boolean ascending = true;

		public CompareByExampleList(List<C> inCollection, boolean ascending)
		{
			this.inCollection = inCollection;
			this.ascending = ascending;
		}

		@Override
		public int compare(Object o1, Object o2)
		{
			int i1 = inCollection.indexOf(o1);
			int i2 = inCollection.indexOf(o2);
			if (i1 == i2)
			{
				return 0;
			}
			int result = i1 < i2 ? -1 : 1;
			return ascending ? result : -result;
		}
	}

	/**
	 * Return the index in the array of the specified object or -1 if it is not there.
	 */
	public static int indexInArray(Object[] array, Object object)
	{
		if (array == null)
		{
			return -1;
		}
		for (int i = 0; i < array.length; i++)
		{
			if (Objects.equals(array[i], object))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Remove a value from a map -- this is useful if the key derived from the object has changed
	 * (out from under you).
	 * Note, if the object is mapped more than once in the map, all instances of it will be removed.
	 * 
	 * @param map
	 *            from which to find value
	 * @param value
	 *            an object in the values of the map
	 * @return true if the map has been modified
	 */
	public static <V> boolean removeValue(Map<?, V> map, V value)
	{
		return removeValue(map, value, null);
	}

	/**
	 * Remove a value from a map and collect the keys that use the value -- this is useful if the
	 * key derived from the
	 * object has changed (out from under you). Note, if the object is mapped more than once in the
	 * map, all instances
	 * of it will be removed.
	 * 
	 * @param map
	 *            from which to find value
	 * @param value
	 *            an object in the values of the map
	 * @param keys
	 *            a collection to fill with the keys that map to the value
	 * @return true if the map has been modified
	 */
	public static <K, V> boolean removeValue(Map<K, V> map, V value, Collection<K> keys)
	{
		Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
		boolean result = false;
		while (iter.hasNext())
		{
			Map.Entry<K, V> entry = iter.next();
			if (entry.getValue() == value)
			{
				if (keys != null)
				{
					keys.add(entry.getKey());
				}
				result = true;
				iter.remove();
			}
		}
		return result;
	}

	/**
	 * Check if an index position falls within the bounds of a list.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isWithinBounds(int index, List list)
	{
		return isWithin(index, list.size());
	}

	/**
	 * Check if an index position falls within the bounds of an array.
	 */
	public static boolean isWithinBounds(int index, Object[] objectArray)
	{
		return isWithin(index, objectArray.length);
	}

	/**
	 * Check if an index position is >= minPosition and < maxPosition.
	 */
	private static boolean isWithin(int index, int maxPosition)
	{
		return index >= 0 && index < maxPosition;
	}

	public static boolean isEmpty(Collection<?> coll)
	{
		return coll == null || coll.isEmpty();
	}

	public static boolean isEmpty(Object[] array)
	{
		return array == null || array.length == 0;
	}

	public static int size(Object[] array)
	{
		return array == null ? 0 : array.length;
	}

	@SuppressWarnings("rawtypes")
	public static int size(Object object)
	{
		if (object instanceof Collection)
			return ((Collection) object).size();
		if (object instanceof Object[])
			return ((Object[]) object).length;
		return 0;
	}

	public static boolean isEqualArray(Object[] first, Object[] second)
	{
		if (first.length != second.length)
		{
			return false;
		}
		for (int i = 0; i < first.length; i++)
		{
			if (!Objects.equals(first[i], second[i]))
			{
				return false;
			}
		}
		return true;
	}

	public static <T> Set<T> findDuplicates(Collection<T> collection)
	{
		Set<T> result = new LinkedHashSet<>();
		Set<T> nodupes = new HashSet<>();
		for (T element : collection)
		{
			if (!nodupes.add(element))
			{
				result.add(element);
			}
		}
		return result;
	}

	public static <T> Collection<T> union(
		Collection<? extends T> coll1,
		Collection<? extends T> coll2)
	{
		// need to be order preserving, so 'overriding'
		List<T> result = new ArrayList<>(coll1);
		List<T> subtractor = new ArrayList<>(coll1);
		for (T element : coll2)
		{
			if (!subtractor.remove(element))
			{
				result.add(element);
			}
		}
		return result;
	}

	public static <T> Collection<T> intersection(Collection<T> coll1, Collection<T> coll2)
	{
		// need to be order preserving, so 'overriding'
		List<T> result = new ArrayList<>();
		List<T> subtractor = new ArrayList<>(coll1);
		for (T element : coll2)
		{
			if (subtractor.remove(element))
			{
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Given a collection, a function to turn elements in the collection into strings, and a
	 * separator,
	 * return the string representation of the collection
	 * 
	 * @param coll
	 * @param toString
	 * @param separator
	 * @return string concatenation of the toString values
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String joinStrings(Collection coll, UnaryFunction toString, String separator)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object item : coll)
		{
			if (first)
				first = false;
			else
				sb.append(separator);
			sb.append(toString.evaluate(item));
		}
		return sb.toString();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int _compare(Comparable comp0, Comparable comp1)
	{
		if (comp0 == null && comp1 == null)
		{
			return 0;
		}
		if (comp0 == null)
		{
			return -1;
		}
		else if (comp1 == null)
		{
			return 1;
		}
		return comp0.compareTo(comp1);
	}

	@SuppressWarnings("rawtypes")
	public static final Comparator ComparableComparator = new Comparator()
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			Comparable comp0 = o1 instanceof Comparable ? (Comparable) o1 : null;
			Comparable comp1 = o2 instanceof Comparable ? (Comparable) o2 : null;
			return _compare(comp0, comp1);
		}
	};

	/**
	 * @param commaDelimitedString - a delimited string
	 * @param delimiter - the string delimiter
	 * Each string is trimmed before added to the result. 
	 * @return a List of strings.
	 */
	public static List<String> splitString(String commaDelimitedString, String delimiter)
	{
		List<String> result = new ArrayList<>();
		if (commaDelimitedString == null)
		{
			return result;
		}

		// When using a pipe ("|") as the delimiter, it must be preceded with double escape characters ("\\") 
		if (delimiter != null && delimiter.equals("|"))
		{
			delimiter = "\\|";
		}
		String[] splitString = commaDelimitedString.split(delimiter);
		for (int i = 0; i < splitString.length; i++)
		{
			result.add(splitString[i].trim());
		}
		return result;
	}

	public static interface ComparableFunction<T>
	{
		public Comparable<T> evaluate(Object object);
	}

	public static class FunctionalComparator<T> implements Comparator<T>
	{
		private final ComparableFunction<T> function;

		public FunctionalComparator(ComparableFunction<T> function)
		{
			this.function = function;
		}

		@Override
		public int compare(Object arg0, Object arg1)
		{
			Comparable<T> comp0 = function.evaluate(arg0);
			Comparable<T> comp1 = function.evaluate(arg1);
			return _compare(comp0, comp1);
		}
	}

}
