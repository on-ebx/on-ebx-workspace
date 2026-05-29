package com.orchestranetworks.ps.util.functional;

import java.util.*;

/**
 * Chains together generators.
 */
public final class GeneratorChain<T> extends Generator<T>
{

	/******************************************
	 * Instance variables.
	 *****************************************/

	private final Collection<Generator<T>> generators = new ArrayList<>();

	/******************************************
	 * Constructors.
	 *****************************************/

	public GeneratorChain()
	{

	}

	/** Add a collection of generators. */
	public GeneratorChain(Collection<Generator<T>> generators)
	{
		addAll(generators);
	}

	/** Add a collection of generators. */
	public GeneratorChain(Generator<Generator<T>> generators)
	{
		addAll(generators);
	}

	/** Start the chain off with 2 generators. */
	public GeneratorChain(Generator<T> generator1, Generator<T> generator2)
	{
		add(generator1);
		add(generator2);
	}

	/******************************************
	 * Instance methods.
	 *****************************************/

	/** Add a single generator to the chain. */
	public GeneratorChain<T> add(Generator<T> generator)
	{
		this.generators.add(generator);
		return this;
	}

	/** Add a collection of generators to the chain. */
	public GeneratorChain<T> addAll(Collection<Generator<T>> generatorsColl)
	{
		this.generators.addAll(generatorsColl);
		return this;
	}

	/** Add a collection of generators to the chain. */
	public GeneratorChain<T> addAll(Generator<Generator<T>> generatorsColl)
	{
		generatorsColl.foreach(new UnaryProcedure<Generator<T>>()
		{
			@Override
			public void run(Generator<T> gen)
			{
				add(gen);
			}
		});

		return this;
	}

	/** Run each of the generators. */
	@Override
	public void run(UnaryProcedure<? super T> proc)
	{
		for (Iterator<Generator<T>> iterator = generators.iterator(); iterator.hasNext()
			&& !isStopped();)
		{
			Generator<T> generator = iterator.next();
			generator.foreach(proc);
		}
	}

	/** Stop the chain from generating. */
	@Override
	public void stop()
	{
		super.stop();
		for (Generator<T> generator : generators)
		{
			generator.stop();
		}
	}

	/******************************************
	 * Class methods.
	 *****************************************/

	public static <T> Generator<T> from(Collection<Generator<T>> generators)
	{
		return new GeneratorChain<>(generators);
	}

	public static <T> Generator<T> from(Generator<Generator<T>> generators)
	{
		return new GeneratorChain<>(generators);
	}

	public static <T> Generator<T> from(Generator<T> generator1, Generator<T> generator2)
	{
		return new GeneratorChain<>(generator1, generator2);
	}
}
