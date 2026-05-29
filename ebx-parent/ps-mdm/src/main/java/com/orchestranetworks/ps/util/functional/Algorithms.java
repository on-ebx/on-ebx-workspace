package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Utility methods and algorithms for applying functors to {@link Generator}s, Iterators and Collections.
 * {@link Generator}s also define these utility methods as instance methods. The {@link #apply}, {@link #select}, and
 * {@link #reject} methods return new Generators. This becomes useful for constructing nested expressions. For example:
 * 
 * <pre> Algorithms.apply(new EachElement(list), func1) .filter(pred) .apply(func2) .reject(func2) .toCollection();
 * </pre>
 * 
 * credit: org.apache.commons.functor
 */
public class Algorithms
{

	private Algorithms()
	{
		// utility class
	}

	/**
	 * Applies the function passed in to each element and returns a collection containing the results.
	 */
	public static <I, O> List<O> apply(Collection<I> collection, UnaryFunction<? super I, O> func)
	{
		List<O> result = new ArrayList<>(collection.size());
		apply(collection.iterator(), func, result);
		return result;
	}

	/**
	 * Applies the function passed in to each element and returns a collection containing the results.
	 */
	public static <I, O> List<O> apply(I[] objects, UnaryFunction<? super I, O> func)
	{
		List<O> result = new ArrayList<>(objects.length);
		for (I object : objects)
		{
			result.add(func.evaluate(object));
		}
		return result;
	}

	/**
	 * Applies the function passed in to each element and returns a collection containing the results.
	 */
	public static <I, O> List<O> apply(Iterator<I> iter, UnaryFunction<? super I, O> func)
	{
		List<O> result = new ArrayList<>();
		apply(iter, func, result);
		return result;
	}

	private static <I, O> void apply(
		Iterator<I> iter,
		UnaryFunction<? super I, O> func,
		List<O> into)
	{
		while (iter.hasNext())
		{
			into.add(func.evaluate(iter.next()));
		}
	}

	/**
	 * Returns a {@link Generator} that will apply the given {@link UnaryFunction} to each generated element.
	 */
	public static <I, O> Generator<O> apply(
		final Generator<I> gen,
		final UnaryFunction<? super I, O> func)
	{
		return new Generator<O>(gen)
		{
			@Override
			public void run(final UnaryProcedure<? super O> proc)
			{
				gen.run(new UnaryProcedure<I>()
				{
					@Override
					public void run(I obj)
					{
						proc.run(func.evaluate(obj));
					}
				});
			}
		};
	}

	/**
	 * Searches for an object matching the class.
	 */
	public static <T> boolean contains(Collection<T> collection, Class<?> matchingClass)
	{
		for (T t : collection)
		{
			if (matchingClass.isInstance(t))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for an object matching the predicate.
	 */
	public static <S, T extends S> boolean contains(
		Collection<T> collection,
		UnaryPredicate<S> pred)
	{
		return contains(collection.iterator(), pred);
	}

	// moderate optimization: don't create an iterator if the collection is a List
	public static <S, T extends S> boolean contains(List<T> collection, UnaryPredicate<S> pred)
	{
		int len = collection.size();
		for (int i = 0; i < len; i++)
		{
			if (pred.test(collection.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for an object matching the predicate.
	 */
	public static <T> boolean contains(T[] array, UnaryPredicate<? super T> pred)
	{
		for (T o : array)
		{
			if (pred.test(o))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Searches for an object matching the predicate.
	 */
	public static <T> boolean contains(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		while (iter.hasNext())
		{
			T o = iter.next();
			if (pred.test(o))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Return <code>true</code> if some element in the given {@link Generator} that matches the given
	 * {@link UnaryPredicate UnaryPredicate}.
	 */
	public static <T> boolean contains(final Generator<T> gen, final UnaryPredicate<? super T> pred)
	{
		// javas' inner classes suck, i should do this a different way i guess
		final boolean[] returnCode = new boolean[1];
		returnCode[0] = false;

		gen.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (pred.test(obj))
				{
					returnCode[0] = true;
					gen.stop();
				}
			}
		});

		return returnCode[0];
	}

	/**
	 * Return <code>true</code> if every element matches the given {@link UnaryPredicate UnaryPredicate}.
	 */
	public static <T> boolean every(Collection<T> collection, UnaryPredicate<? super T> pred)
	{
		return every(collection.iterator(), pred);
	}

	/**
	 * Return <code>true</code> if every element matches the given {@link UnaryPredicate UnaryPredicate}.
	 */
	public static <T> boolean every(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		while (iter.hasNext())
		{
			T o = iter.next();
			if (!pred.test(o))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Return <code>true</code> if every element matches the given {@link UnaryPredicate UnaryPredicate}.
	 */
	public static <T> boolean every(final Generator<T> gen, final UnaryPredicate<? super T> pred)
	{
		// javas' inner classes suck, i should do this a different way i guess
		final boolean[] returnCode = new boolean[1];
		returnCode[0] = true;

		gen.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (!pred.test(obj))
				{
					returnCode[0] = false;
					gen.stop();
				}
			}
		});

		return returnCode[0];
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or returns the given
	 * (possibly <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> T detect(T[] objects, UnaryPredicate pred)
	{
		for (T o : objects)
		{
			if (pred.test(o))
			{
				return o;
			}
		}

		throw new NoSuchElementException("No element matching " + pred + " was found.");
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or returns the given
	 * (possibly <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	public static <T> T detect(T[] objects, UnaryPredicate<? super T> pred, T ifNone)
	{
		for (T o : objects)
		{
			if (pred.test(o))
			{
				return o;
			}
		}

		return ifNone;
	}

	/**
	 * Return the first element that is an instance of the specified class, or returns the given (possibly
	 * <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	@SuppressWarnings("unchecked")
	public static <T, S extends T> S detect(Collection<T> objects, Class<S> clazz, S ifNone)
	{
		for (Object object : objects)
		{
			if (clazz.isInstance(object))
			{
				return (S) object;
			}
		}
		return ifNone;
	}

	/**
	 * Return the first element that is an instance of the specified class, or returns the given (possibly
	 * <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	public static <T, S extends T> S detect(Collection<T> objects, Class<S> clazz)
	{
		return detect(objects, clazz, null);
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or throws a
	 * {@link java.util.NoSuchElementException NoSuchElementException} if no matching element can be found.
	 */
	public static <T> T detect(Iterator<T> iter, UnaryPredicate<T> pred)
	{
		while (iter.hasNext())
		{
			T o = iter.next();
			if (pred.test(o))
			{
				return o;
			}
		}

		throw new NoSuchElementException("No element matching " + pred + " was found.");
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or returns the given
	 * (possibly <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	public static <T> T detect(Collection<T> collection, UnaryPredicate<T> pred, T ifNone)
	{
		return detect(collection.iterator(), pred, ifNone);
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or throws a
	 * {@link java.util.NoSuchElementException NoSuchElementException} if no matching element can be found.
	 */
	public static <T> T detect(Collection<T> collection, UnaryPredicate<T> pred)
	{
		return detect(collection.iterator(), pred);
	}

	/**
	 * Return the first element that matches the given {@link UnaryPredicate UnaryPredicate}, or returns the given
	 * (possibly <code>null</code> <code>Object</code> if no matching element can be found.
	 */
	public static <T> T detect(Iterator<T> iter, UnaryPredicate<T> pred, T ifNone)
	{
		while (iter.hasNext())
		{
			T o = iter.next();
			if (pred.test(o))
			{
				return o;
			}
		}

		return ifNone;
	}

	/**
	 * Returns the first element within the given {@link Generator} that matches the given {@link UnaryPredicate
	 * UnaryPredicate}, or throws a {@link java.util.NoSuchElementException NoSuchElementException} if no matching
	 * element can be found.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T detect(final Generator<T> gen, final UnaryPredicate pred)
	{
		final Object[] foundObj = new Object[1];
		gen.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (pred.test(obj))
				{
					foundObj[0] = obj;
					gen.stop();
				}
			}
		});

		if (foundObj[0] != null)
		{
			return (T) foundObj[0];
		}
		else
		{
			throw new NoSuchElementException("No element matching " + pred + " was found.");
		}
	}

	/**
	 * Return the first element within the given {@link Generator} that matches the given {@link UnaryPredicate
	 * UnaryPredicate}, or returns the given (possibly <code>null</code> <code>Object</code> if no matching element can
	 * be found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T detect(
		final Generator<T> gen,
		final UnaryPredicate<? super T> pred,
		T ifNone)
	{
		final Object[] foundObj = new Object[1];
		gen.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (pred.test(obj))
				{
					foundObj[0] = obj;
					gen.stop();
				}
			}
		});

		if (foundObj[0] != null)
		{
			return (T) foundObj[0];
		}
		else
		{
			return ifNone;
		}
	}

	/**
	 * Returns the value for an item in the collection if every item returns the same one.
	 * @param collection The collection of items from which to get the value.
	 * @param valueFunction The function to get the value from any item.
	 * @return The value that all unary functions returned, or null if any were different.
	 */
	public static <I, O> O getCommonValue(
		Collection<I> collection,
		UnaryFunction<? super I, O> valueFunction)
	{
		return getCommonValue(collection.iterator(), valueFunction);
	}

	/**
	 * Returns the value for an item from the iterator if every item returns the same one.
	 * @param iterator The iterator from which to get the value.
	 * @param valueFunction The function to get the value from any item.
	 * @return The value that all unary functions returned, or null if any were different.
	 */
	public static <I, O> O getCommonValue(
		Iterator<I> iterator,
		UnaryFunction<? super I, O> valueFunction)
	{
		O value = null;
		while (iterator.hasNext())
		{
			I object = iterator.next();
			O objectValue = valueFunction.evaluate(object);
			if (value == null)
			{
				value = objectValue;
			}
			else if (!value.equals(objectValue))
			{
				return null;
			}
		}
		return value;
	}

	/**
	 * foreach(collection.iterator(), proc);
	 */
	public static <T> void foreach(Collection<T> collection, UnaryProcedure<? super T> proc)
	{
		foreach(collection.iterator(), proc);
	}

	/**
	 * Pass each of the elements to the given {@link UnaryProcedure procedures} run method.
	 */
	public static <T> void foreach(Iterator<T> iter, UnaryProcedure<? super T> proc)
	{
		while (iter.hasNext())
		{
			proc.run(iter.next());
		}
	}

	/**
	 * Pass each of the elements to the given {@link UnaryProcedure procedures} run method.
	 */
	public static <T> void foreach(Generator<T> gen, UnaryProcedure<? super T> proc)
	{
		gen.run(proc);
	}

	/**
	 * inject(new IteratorToGeneratorAdapater(iter), seed, func);
	 */
	public static <T> T inject(Iterator<T> iter, T seed, BinaryFunction<T, T, T> func)
	{
		return inject(Generator.fromIterator(iter), seed, func);
	}

	/**
	 * {@link BinaryFunction#evaluate Evaluate} the pair <i>( previousResult, element )</i> for each element in the
	 * given {@link Generator} where previousResult is initially <i>seed</i>, and thereafter the result of the
	 * evaluation of the previous element in the iterator. Returns the result of the final evaluation. <p/> <p/> In
	 * code: <pre> while(iter.hasNext()) { seed = func.evaluate(seed,iter.next()); } return seed; </pre>
	 */
	@SuppressWarnings("unchecked")
	public static <T> T inject(Generator<T> gen, final T seed, final BinaryFunction<T, T, T> func)
	{
		final Object[] result = new Object[1];
		result[0] = seed;

		gen.run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				result[0] = func.evaluate((T) result[0], obj);
			}
		});

		return (T) result[0];
	}

	/**
	 * Returns a Collection that will only contain the elements that DO NOT match the given predicate.
	 */
	public static <T> List<T> reject(T[] objects, UnaryPredicate<? super T> pred)
	{
		List<T> results = new ArrayList<>();
		for (T object : objects)
		{
			T obj = object;
			if (!pred.test(obj))
			{
				results.add(obj);
			}
		}

		return results;
	}

	/**
	 * Returns a Collection that will only contain the elements that DO NOT match the given predicate.
	 */
	public static <T> List<T> reject(Collection<T> collection, UnaryPredicate<? super T> pred)
	{
		return reject(collection.iterator(), pred);
	}

	/**
	 * Returns a Collection that will only contain the elements that DO NOT match the given predicate.
	 */
	public static <T> List<T> reject(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		List<T> results = new ArrayList<>();
		while (iter.hasNext())
		{
			T obj = iter.next();
			if (!pred.test(obj))
			{
				results.add(obj);
			}
		}

		return results;
	}

	/**
	 * Returns a {@link Generator} that will only "generate" elements that DO NOT match the given predicate.
	 */
	public static <T> Generator<T> reject(
		final Generator<T> gen,
		final UnaryPredicate<? super T> pred)
	{
		return new Generator<T>(gen)
		{
			@Override
			public void run(final UnaryProcedure<? super T> proc)
			{
				gen.run(new UnaryProcedure<T>()
				{
					@Override
					public void run(T obj)
					{
						if (!pred.test(obj))
						{
							proc.run(obj);
						}
					}
				});
			}
		};
	}

	/**
	 * Returns the list of items that match the given predicate.
	 */
	public static <T> List<T> select(Collection<T> collection, UnaryPredicate<? super T> pred)
	{
		return select(collection.iterator(), pred);
	}

	// moderate optimization - avoid creation of iterator
	public static <T> List<T> select(List<T> collection, UnaryPredicate<? super T> pred)
	{
		List<T> results = new ArrayList<>();
		int len = collection.size();
		for (int i = 0; i < len; i++)
		{
			T o = collection.get(i);
			if (pred.test(o))
			{
				results.add(o);
			}
		}
		return results;
	}

	/**
	 * Returns the list of items that match the given class.
	 */
	public static <S, T extends S> List<T> select(Collection<S> collection, Class<T> itemClass)
	{
		List<T> results = new ArrayList<>();
		return addSelected(results, collection, itemClass);
	}

	/**
	 * Adds to the provided list the items of the collection that match the given class.
	 */
	@SuppressWarnings("unchecked")
	public static <S, T extends S> List<T> addSelected(
		List<T> results,
		Collection<S> collection,
		Class<T> itemClass)
	{
		for (S item : collection)
		{
			if (itemClass.isInstance(item))
			{
				results.add((T) item);
			}
		}
		return results;
	}

	/**
	 * Adds to the provided list the items of the collection for which the given predicate returns true.
	 */
	public static <T> List<T> addSelected(
		List<T> results,
		Collection<T> collection,
		UnaryPredicate<? super T> pred)
	{
		for (T item : collection)
		{
			if (pred.test(item))
			{
				results.add(item);
			}
		}
		return results;
	}

	/**
	 * Returns the list of items that match the given predicate.
	 */
	public static <T> List<T> select(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		List<T> results = new ArrayList<>();
		while (iter.hasNext())
		{
			T o = iter.next();
			if (pred.test(o))
			{
				results.add(o);
			}
		}

		return results;
	}

	/**
	 * Returns the list of items that match the given predicate.
	 */
	public static <T> List<T> select(T[] array, UnaryPredicate<? super T> pred)
	{
		List<T> results = new ArrayList<>();
		for (T o : array)
		{
			if (pred.test(o))
			{
				results.add(o);
			}
		}

		return results;
	}

	/**
	 * Returns a {@link Generator} that will only "generate" elements that DO match the given predicate.
	 */
	public static <T> Generator<T> select(
		final Generator<T> gen,
		final UnaryPredicate<? super T> pred)
	{
		return new Generator<T>(gen)
		{
			@Override
			public void run(final UnaryProcedure<? super T> proc)
			{
				gen.run(new UnaryProcedure<T>()
				{
					@Override
					public void run(T obj)
					{
						if (pred.test(obj))
						{
							proc.run(obj);
						}
					}
				});
			}
		};
	}

	/**
	 * Returns a {@link Generator} that will only "generate" elements that are instances of the specified class.
	 */
	public static <T, S extends T> Generator<S> select(
		final Generator<T> gen,
		final Class<S> instanceOfClass)
	{
		return new Generator<S>(gen)
		{
			@Override
			public void run(final UnaryProcedure<? super S> proc)
			{
				gen.run(new UnaryProcedure<T>()
				{
					@SuppressWarnings("unchecked")
					@Override
					public void run(T obj)
					{
						if (instanceOfClass.isInstance(obj))
						{
							proc.run((S) obj);
						}
					}
				});
			}
		};
	}

	/**
	 * Counts the number of items that match the given predicate.
	 */
	public static <T> int count(Collection<T> collection, UnaryPredicate<? super T> pred)
	{
		return count(collection.iterator(), pred);
	}

	/**
	 * Counts the number of items that match the given predicate.
	 */
	public static <T> int count(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		int count = 0;
		while (iter.hasNext())
		{
			T o = iter.next();
			if (pred.test(o))
			{
				count++;
			}
		}

		return count;
	}

	/**
	 * Counts the number of items that match the given predicate.
	 */
	public static <T> int count(final Generator<T> gen, final UnaryPredicate<? super T> pred)
	{
		Counter counter = new Counter();
		gen.select(pred).foreach(counter);
		return counter.getCount();
	}

	/**
	 * until(new IteratorToGeneratorAdapater(iter), pred);
	 */
	public static <T> Generator<T> until(Iterator<T> iter, UnaryPredicate<? super T> pred)
	{
		return until(Generator.fromIterator(iter), pred);
	}

	/**
	 * Returns a {@link Generator} that will stop when the predicate becomes true. This is useful for imposing
	 * {@link Generator} limits. For example: <pre> EachLine.open(file).until(new MaxIterations(1)); </pre>
	 * Would only "generate" 1 line from the file before {@link Generator#stop stopping} the generator.
	 */
	public static <T> Generator<T> until(
		final Generator<T> gen,
		final UnaryPredicate<? super T> pred)
	{
		return new Generator<T>(gen)
		{
			@Override
			public void run(final UnaryProcedure<? super T> proc)
			{
				gen.run(new UnaryProcedure<T>()
				{
					@Override
					public void run(T obj)
					{
						if (pred.test(obj))
						{
							stop();
						}
						else
						{
							proc.run(obj);
						}
					}
				});
			}
		};
	}
}