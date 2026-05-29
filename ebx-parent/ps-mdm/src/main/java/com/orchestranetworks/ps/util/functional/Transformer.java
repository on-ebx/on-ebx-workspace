package com.orchestranetworks.ps.util.functional;

/**
 * Transformers are used to change a {@link Generator} into something else, such as a {@link java.util.Collection}.
 */

public interface Transformer<I, O>
{
	public O transform(Generator<I> generator);
}