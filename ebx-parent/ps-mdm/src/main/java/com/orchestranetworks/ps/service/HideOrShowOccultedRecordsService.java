/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.service;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * A service class to toggle between hide/show occulted records.
 * This service is primarily used when you have inherited datasets and you need to hide or show the occulted records in the child dataset.
 * By default, occulted records do not show up in a child dataset. 
 * This service sets a session attribute to true or false which will then be looked up by the access rule HideOrShowOccultedRecordsAccessRule to toggle the permission accordingly.
 * When you use this service, you need to set this access rule on occurrence on the table.
 * 
 */
public class HideOrShowOccultedRecordsService extends AbstractUserService<DataspaceEntitySelection>
{
	private String showOccultedRecords = null;

	public String getShowOccultedRecords()
	{
		return showOccultedRecords;
	}

	public void setShowOccultedRecords(String showOccultedRecords)
	{
		this.showOccultedRecords = showOccultedRecords;
	}

	public HideOrShowOccultedRecordsService(String showOccultedRecords)
	{
		this.showOccultedRecords = showOccultedRecords;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		session.setAttribute(
			CommonConstants.SHOW_OCCULTED_RECORDS_SESSION_ATTRIBUTE,
			showOccultedRecords);
	}

}
