package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * A functor that takes one argument and returns a <code>boolean</code> value. Implementors are encouraged but not
 * required to make their functors {@link java.io.Serializable Serializable}. <br> credit: org.apache.commons.functor.
 * copied to extend functionality for ON.
 */
public abstract class UnaryPredicate<T>
{
	/**
	 * Evaluate this predicate.
	 * 
	 * @param object the object to test
	 * @return the result of this test
	 */
	public abstract boolean test(T object);

	public static final UnaryPredicate<Object> isNull = new UnaryPredicate<Object>()
	{
		@Override
		public boolean test(Object object)
		{
			return object == null;
		}

		@Override
		public String toString()
		{
			return "isNull()";
		}
	};

	public static final UnaryPredicate<Object> isNotNull = new UnaryPredicate<Object>()
	{
		@Override
		public boolean test(Object object)
		{
			return object != null;
		}

		@Override
		public String toString()
		{
			return "isNotNull()";
		}
	};

	public static final UnaryPredicate<Object> isNotNullObject = new UnaryPredicate<Object>()
	{
		@Override
		public boolean test(Object object)
		{
			return object != null;
		}

		@Override
		public String toString()
		{
			return "isNotNull()";
		}
	};

	public static UnaryPredicate<Object> isEqual(final Object equalsThis)
	{
		return new UnaryPredicate<Object>()
		{
			@Override
			public boolean test(Object object)
			{
				return Objects.equals(object, equalsThis);
			}

			@Override
			public String toString()
			{
				return "isEqual(" + equalsThis + ")";
			}
		};
	}

	public static UnaryPredicate<String> isEqualIgnoringCase(final String equalsThis)
	{
		return new UnaryPredicate<String>()
		{
			@Override
			public boolean test(String object)
			{
				if (object == null)
				{
					return false;
				}
				return equalsThis.equalsIgnoreCase(object);
			}

			@Override
			public String toString()
			{
				return "isEqualIgnoringCase(\"" + equalsThis + "\")";
			}
		};
	}

	public static UnaryPredicate<Object> isNotEqual(final Object equalsThis)
	{
		return new UnaryPredicate<Object>()
		{
			@Override
			public boolean test(Object object)
			{
				return !Objects.equals(object, equalsThis);
			}

			@Override
			public String toString()
			{
				return "isNotEqual(" + equalsThis + ")";
			}
		};
	}

	public static UnaryPredicate<Object> isSame(final Object sameAsThis)
	{
		return new UnaryPredicate<Object>()
		{
			@Override
			public boolean test(Object object)
			{
				return object == sameAsThis;
			}

			@Override
			public String toString()
			{
				return "isSame(" + sameAsThis + ")";
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static UnaryPredicate<Object> isInstance(final Class<?> ofThis)
	{
		return new IsInstanceOf(ofThis);
	}

	/**
	 * Constructs a predicate that will match any items that are instances of any of the classes in the list passed in.
	 * @param anyOfTheseClasses List of Class objects.
	 */
	public static UnaryPredicate<Object> isInstance(final List<Class<?>> anyOfTheseClasses)
	{
		UnaryPredicate<Object> predicate = UnaryPredicate.FALSE;
		if (anyOfTheseClasses != null && !anyOfTheseClasses.isEmpty())
		{
			predicate = UnaryPredicate.isInstance(anyOfTheseClasses.get(0));
			for (int i = 1; i < anyOfTheseClasses.size(); i++)
			{
				Class<?> clazz = anyOfTheseClasses.get(i);
				predicate = predicate.or(UnaryPredicate.isInstance(clazz));
			}
		}

		return predicate;
	}

	public static UnaryPredicate<Class<?>> isAssignableFrom(final Class<?> fromThis)
	{
		return new UnaryPredicate<Class<?>>()
		{
			@Override
			public boolean test(Class<?> object)
			{
				return fromThis.isAssignableFrom(object);
			}

			@Override
			public String toString()
			{
				return "isAssignableFrom(" + fromThis + ")";
			}
		};
	}

	public static <I> UnaryPredicate<I> not(final UnaryPredicate<I> predicate)
	{
		return new UnaryPredicate<I>()
		{
			@Override
			public boolean test(I object)
			{
				return !predicate.test(object);
			}

			@Override
			public String toString()
			{
				return "not(" + predicate + ")";
			}
		};
	}

	public UnaryPredicate<T> not()
	{
		return not(this);
	}

	public UnaryPredicate<T> and(final UnaryPredicate<? super T> predicate)
	{
		final UnaryPredicate<T> me = this;
		return new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T object)
			{
				return me.test(object) && predicate.test(object);
			}

			@Override
			public String toString()
			{
				return "and(" + me + ", " + predicate + ")";
			}
		};
	}

	/** Returns "and(UnaryPredicate.not(predicate))". */
	public UnaryPredicate<T> andNot(UnaryPredicate<? super T> predicate)
	{
		return and(UnaryPredicate.not(predicate));
	}

	public UnaryPredicate<T> or(final UnaryPredicate<? super T> predicate)
	{
		final UnaryPredicate<T> me = this;
		return new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T obj)
			{
				return me.test(obj) || predicate.test(obj);
			}

			@Override
			public String toString()
			{
				return "or(" + me + ", " + predicate + ")";
			}
		};
	}

	public UnaryProcedure<T> ifTrue(final UnaryProcedure<? super T> proc)
	{
		return new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (test(obj))
				{
					proc.run(obj);
				}
			}

			@Override
			public String toString()
			{
				return "ifTrue(" + proc + ")";
			}
		};
	}

	public UnaryProcedure<T> ifFalse(final UnaryProcedure<? super T> proc)
	{
		return new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (!test(obj))
				{
					proc.run(obj);
				}
			}

			@Override
			public String toString()
			{
				return "ifFalse(" + proc + ")";
			}
		};
	}

	public UnaryProcedure<T> ifElse(
		final UnaryProcedure<? super T> onTrue,
		final UnaryProcedure<? super T> onFalse)
	{
		return new UnaryProcedure<T>()
		{
			@Override
			public void run(T obj)
			{
				if (test(obj))
				{
					onTrue.run(obj);
				}
				else
				{
					onFalse.run(obj);
				}
			}
		};
	}

	/**
	 * Returns a predicate that will run the onTrue predicate if true, or the onFalse predicate if false.
	 */
	public UnaryPredicate<T> ifElse(
		final UnaryPredicate<? super T> onTrue,
		final UnaryPredicate<? super T> onFalse)
	{
		final UnaryPredicate<T> me = this;
		return new UnaryPredicate<T>()
		{
			@Override
			public boolean test(T obj)
			{
				if (me.test(obj))
				{
					return onTrue.test(obj);
				}
				else
				{
					return onFalse.test(obj);
				}
			}
		};
	}

	public <O> UnaryFunction<T, O> ifTrue(final UnaryFunction<? super T, O> func)
	{
		return new UnaryFunction<T, O>()
		{
			@Override
			public O evaluate(T obj)
			{
				if (test(obj))
				{
					return func.evaluate(obj);
				}
				else
				{
					return null;
				}
			}

			@Override
			public String toString()
			{
				return "ifTrue(" + func + ")";
			}
		};
	}

	public <O> UnaryFunction<T, O> ifFalse(final UnaryFunction<? super T, O> func)
	{
		return new UnaryFunction<T, O>()
		{
			@Override
			public O evaluate(T obj)
			{
				if (!test(obj))
				{
					return func.evaluate(obj);
				}
				else
				{
					return null;
				}
			}

			@Override
			public String toString()
			{
				return "ifFalse(" + func + ")";
			}
		};
	}

	public <O> UnaryFunction<T, O> ifElse(
		final UnaryFunction<? super T, O> onTrue,
		final UnaryFunction<? super T, O> onFalse)
	{
		return new UnaryFunction<T, O>()
		{
			@Override
			public O evaluate(T obj)
			{
				if (test(obj))
				{
					return onTrue.evaluate(obj);
				}
				else
				{
					return onFalse.evaluate(obj);
				}
			}
		};
	}

	/** Turns a UnaryPredicate into a Predicate and binds its param to the value. */
	public Predicate bind(final T value)
	{
		final UnaryPredicate<T> me = this;
		return new Predicate()
		{
			@Override
			public boolean test()
			{
				return me.test(value);
			}

			@Override
			public String toString()
			{
				return "bind(" + value + ")";
			}
		};
	}

	public static final UnaryPredicate<Object> TRUE = new UnaryPredicate<Object>()
	{
		@Override
		public boolean test(Object obj)
		{
			return true;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryPredicate not()
		{
			return FALSE;
		}

		/** Optimize out the extra predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryPredicate or(UnaryPredicate predicate)
		{
			return this;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifTrue(UnaryFunction func)
		{
			return func;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifFalse(UnaryFunction func)
		{
			return UnaryFunction.NULL;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifElse(UnaryFunction onTrue, UnaryFunction onFalse)
		{
			return onTrue;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifTrue(UnaryProcedure proc)
		{
			return proc;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifFalse(UnaryProcedure proc)
		{
			return UnaryProcedure.NoOp;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifElse(UnaryProcedure onTrue, UnaryProcedure onFalse)
		{
			return onTrue;
		}
	};

	public static final UnaryPredicate<Object> FALSE = new UnaryPredicate<Object>()
	{
		@Override
		public boolean test(Object obj)
		{
			return false;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryPredicate not()
		{
			return TRUE;
		}

		/** Optimize out the extra predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryPredicate and(UnaryPredicate predicate)
		{
			return this;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifTrue(UnaryFunction func)
		{
			return UnaryFunction.NULL;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifFalse(UnaryFunction func)
		{
			return func;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryFunction ifElse(UnaryFunction onTrue, UnaryFunction onFalse)
		{
			return onFalse;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifTrue(UnaryProcedure proc)
		{
			return UnaryProcedure.NoOp;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifFalse(UnaryProcedure proc)
		{
			return proc;
		}

		/** Optimize out the predicate. */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public UnaryProcedure ifElse(UnaryProcedure onTrue, UnaryProcedure onFalse)
		{
			return onFalse;
		}
	};
}
