package com.orchestranetworks.ps.util.functional;

public class Counter extends UnaryProcedure<Object>
{
	private int count = 0;

	@Override
	public void run(Object object)
	{
		increment();
	}

	public void increment()
	{
		count++;
	}

	public void decrement()
	{
		count--;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public int getCount()
	{
		return count;
	}

	@Override
	public String toString()
	{
		return "count=" + count;
	}

	public Procedure getIncrementProcedure()
	{
		return increment.bind(this);
	}

	public static final UnaryProcedure<Counter> increment = new UnaryProcedure<Counter>()
	{
		@Override
		public void run(Counter obj)
		{
			obj.increment();
		}
	};
}
