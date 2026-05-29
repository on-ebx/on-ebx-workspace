package com.ebx.mdm.common.module;

import com.orchestranetworks.ps.module.PSModuleRegistrationListener;

import jakarta.servlet.annotation.WebListener;

@WebListener
public class EBXModuleRegistrationListener extends PSModuleRegistrationListener
{
	
	public static final String MODULE_NAME = "onebx-mdm";
	
	public EBXModuleRegistrationListener()
	{
		super(new EBXModuleRegistration());
	}

}