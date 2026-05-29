/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.behavior;

/**
 */
public class BehaviorConfigException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public BehaviorConfigException(BehaviorConfig config)
	{
		super("Behavior Config " + config.getClass().getName()
			+ " is invalid. See log for details.");
	}
}
