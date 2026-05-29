/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scripttask;

import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * This is an incredibly simple script task that simply takes an input value and sets it to
 * a context variable. For example, if you just want to set context variable foo to false,
 * you can set inputValue to false and outputValue to foo.
 * 
 * @deprecated Use built-in "Set a data context variable" library script instead. It does the same thing.
 */
@Deprecated
public class SetContextValueScriptTask extends ScriptTaskBean
{
	private String inputValue;
	private String outputValue;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		this.outputValue = inputValue;
	}

	public String getInputValue()
	{
		return this.inputValue;
	}

	public void setInputValue(String inputValue)
	{
		this.inputValue = inputValue;
	}

	public String getOutputValue()
	{
		return this.outputValue;
	}

	public void setOutputValue(String outputValue)
	{
		this.outputValue = outputValue;
	}
}
