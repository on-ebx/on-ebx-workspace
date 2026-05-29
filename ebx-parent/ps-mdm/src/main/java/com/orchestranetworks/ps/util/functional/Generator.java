package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Generators are pretty much the inverse of iterators, they run themselves. The flow of control is from within the
 * generator, not outside of it. Generators are meant to only be used once, do not attempt to cache and reuse a
 * generator.
 * 
 * credit: org.apache.commons.functor. copied to extend functionality for ON.
 */
public abstract class Generator<T>
{

	/**
	 * A generator can wrap another generator.
	 */
	private Generator<?> wrappedGenerator = null;

	/**
	 * Create a new generator.
	 */
	public Generator()
	{
	}

	/**
	 * A generator can wrap another generator. When wrapping generators you should use probably this constructor since
	 * doing so will cause the {@link #stop} method to stop the wrapped generator as well.
	 */
	public Generator(Generator<?> generator)
	{
		this.wrappedGenerator = generator;
	}

	/**
	 * Get the generator that is being wrapped.
	 */
	protected Generator<?> getWrappedGenerator()
	{
		return wrappedGenerator;
	}

	/**
	 * Generators must implement this method.
	 */
	public abstract void run(UnaryProcedure<? super T> proc);

	/**
	 * Stop the generator. Will stop the wrapped generator if one was set.
	 */
	public void stop()
	{
		if (wrappedGenerator != null)
		{
			wrappedGenerator.stop();
		}
		stopped = true;
	}

	/**
	 * Check if the generator is stopped.
	 */
	public boolean isStopped()
	{
		return stopped;
	}

	/**
	 * Throws an IllegalStateException if this generator has been stopped.
	 */
	protected void checkIfStopped()
	{
		if (isStopped())
		{
			throw new IllegalStateException("This generator is stopped.");
		}
	}

	/**
	 * Set to true when the generator is {@link #stop stopped}.
	 */
	private boolean stopped = false;

	/**
	 * See {@link Algorithms#apply}.
	 */
	public final <O> Generator<O> apply(UnaryFunction<? super T, O> func)
	{
		return Algorithms.apply(this, func);
	}

	/**
	 * See {@link Algorithms#contains}.
	 */
	public final boolean contains(UnaryPredicate<? super T> pred)
	{
		return Algorithms.contains(this, pred);
	}

	/**
	 * See {@link Algorithms#every}.
	 */
	public final boolean every(UnaryPredicate<? super T> pred)
	{
		return Algorithms.every(this, pred);
	}

	/**
	 * See {@link Algorithms#detect}.
	 */
	public final T detect(UnaryPredicate<? super T> pred)
	{
		return Algorithms.detect(this, pred);
	}

	/**
	 * See {@link Algorithms#detect}.
	 */
	public final T detect(UnaryPredicate<? super T> pred, T ifNone)
	{
		return Algorithms.detect(this, pred, ifNone);
	}

	/**
	 * See {@link Algorithms#detect}.
	 */
	public final T selectBest(Comparator<T> comparator)
	{
		return to(new SelectBest<>(comparator));
	}

	/**
	 * Synonym for run.
	 */
	public final void foreach(UnaryProcedure<? super T> proc)
	{
		Algorithms.foreach(this, proc);
	}

	/**
	 * See {@link Algorithms#inject}.
	 */
	public final T inject(T seed, BinaryFunction<T, T, T> func)
	{
		return Algorithms.inject(this, seed, func);
	}

	/**
	 * See {@link Algorithms#reject}.
	 */
	public final Generator<T> reject(UnaryPredicate<? super T> pred)
	{
		return Algorithms.reject(this, pred);
	}

	/**
	 * See {@link Algorithms#select}.
	 */
	public final Generator<T> select(UnaryPredicate<? super T> pred)
	{
		return Algorithms.select(this, pred);
	}

	/**
	 * Select instances of a specific class.
	 */
	public final <S extends T> Generator<S> selectInstanceOf(Class<S> clazz)
	{
		return Algorithms.select(this, clazz);
	}

	/**
	 * Filter out the nulls.
	 */
	public final Generator<T> filterNulls()
	{
		return Algorithms.select(this, UnaryPredicate.isNotNull);
	}

	/**
	 * See {@link Algorithms#until}.
	 */
	public final Generator<T> until(UnaryPredicate<? super T> pred)
	{
		return Algorithms.until(this, pred);
	}

	/**
	 * See {@link Algorithms#count}.
	 */
	public final int count(UnaryPredicate<? super T> pred)
	{
		return Algorithms.count(this, pred);
	}

	/**
	 * {@link Transformer Transforms} this generator using the passed in transformer. An example transformer might turn
	 * the contents of the generator into a {@link java.util.Collection} of elements.
	 */
	public <O> O to(Transformer<T, O> transformer)
	{
		return transformer.transform(this);
	}

	/**
	 * Check if this generator produces any elements.
	 */
	public boolean isEmpty()
	{
		return !contains(UnaryPredicate.TRUE);
	}

	/**
	 * Same as to(new CollectionTransformer(collection)).
	 */
	public <C extends Collection<T>> C to(final C collection)
	{
		run(new UnaryProcedure<T>()
		{
			@Override
			public void run(T object)
			{
				collection.add(object);
			}
		});

		return collection;
	}

	/**
	 * Same as to(new HashSet()).
	 */
	public Set<T> toSet()
	{
		return to(new HashSet<T>());
	}

	/**
	 * Same as to(new CollectionTransformer()).
	 */
	public Collection<T> toCollection()
	{
		return to(new ArrayList<T>());
	}

	/**
	 * Same as to(new ArrayList()).
	 */
	public List<T> toList()
	{
		return to(new ArrayList<T>());
	}

	/**
	 * Same as to(new ArrayList(size)).
	 */
	public List<T> toList(int size)
	{
		return to(new ArrayList<T>(size));
	}

	/**
	 * return (Iterator)toCollection().iterator();
	 */
	public Iterator<T> iterator()
	{
		return toCollection().iterator();
	}

	/**
	 * Chains generators together.
	 */
	public Generator<T> combine(final Generator<T> generator)
	{
		final Generator<T> me = this;
		return new Generator<T>()
		{
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				me.run(proc);
				generator.run(proc);
			}

			@Override
			public void stop()
			{
				me.stop();
				generator.stop();
				super.stop();
			}
		};
	}

	/**
	 * Combine this generator with the elements from the collection passed in.
	 */
	public Generator<T> combine(final Collection<T> collection)
	{
		return combine(fromCollection(collection));
	}

	/**
	 * Combine this generator with the elements from the array passed in.
	 */
	@SuppressWarnings("unchecked")
	public Generator<T> combine(final Object[] objects)
	{
		return combine((Generator<T>) fromArray(objects));
	}

	/**
	 * Chain this generator of generators together.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Generator chain()
	{
		return new GeneratorChain(this);
	}

	public static <T> Generator<T> fromIterator(final Iterator<T> iter)
	{
		return new Generator<T>()
		{
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				checkIfStopped();
				if (iter == null)
				{
					return;
				}
				while (iter.hasNext())
				{
					proc.run(iter.next());
					if (isStopped())
					{
						break;
					}
				}

				stop();
			}
		};
	}

	public static <T> Generator<T> fromEnumeration(final Enumeration<T> enumeration)
	{
		return new Generator<T>()
		{
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				checkIfStopped();
				if (enumeration == null)
				{
					return;
				}
				while (enumeration.hasMoreElements())
				{
					proc.run(enumeration.nextElement());
					if (isStopped())
					{
						break;
					}
				}

				stop();
			}
		};
	}

	public static <T> Generator<T> fromCollection(final Collection<T> col)
	{
		return new Generator<T>()
		{
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				checkIfStopped();
				if (col == null)
				{
					return;
				}
				Iterator<T> iter = col.iterator();
				while (iter.hasNext())
				{
					proc.run(iter.next());
					if (isStopped())
					{
						break;
					}
				}

				stop();
			}
		};
	}

	public static final <T> Generator<T> fromArray(final Object[] objs)
	{
		return new Generator<T>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				checkIfStopped();
				if (objs == null)
				{
					return;
				}
				for (Object obj : objs)
				{
					proc.run((T) obj);
					if (isStopped())
					{
						break;
					}
				}

				stop();
			}
		};
	}

	/** Returns a generator that generates a single object. */
	public static <T> Generator<T> forObject(final T object)
	{
		return new Generator<T>()
		{
			@Override
			public void run(UnaryProcedure<? super T> proc)
			{
				if (object != null)
				{
					checkIfStopped();
					proc.run(object);
					stop();
				}
			}
		};
	}

	/** Returns an empty generator. */
	@SuppressWarnings("rawtypes")
	public static Generator emptyGenerator()
	{
		return new Generator()
		{
			@Override
			public void run(UnaryProcedure proc)
			{
				checkIfStopped();
				stop();
			}
		};
	}

	/**
	 * Create a generator from the object passed to the function. If the object is a collection then
	 * EachElement.from(collection) is returned. If the object is an iterator then EachElement.from(iterator) is
	 * returned. If the object is an array object objects EachElement.from(Object[]) is returned. Otherwise
	 * Generator.from(obj) is returned. If the object is null the emptyGenerator is returned.
	 */
	public static final Generator<?> from(Object obj)
	{
		if (obj == null)
		{
			return emptyGenerator();
		}
		else if (obj instanceof Collection)
		{
			return Generator.fromCollection((Collection<?>) obj);
		}
		else if (obj instanceof Iterator)
		{
			return Generator.fromIterator((Iterator<?>) obj);
		}
		else if (obj instanceof Object[])
		{
			return Generator.fromArray((Object[]) obj);
		}
		else
		{
			return Generator.forObject(obj);
		}
	}

	/**
	 * When run calls create(obj) with the object passed to evaluate.
	 */
	public static final UnaryFunction<Object, Generator<?>> create = new UnaryFunction<Object, Generator<?>>()
	{
		@Override
		public Generator<?> evaluate(Object obj)
		{
			return from(obj);
		}
	};
}