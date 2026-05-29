/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.workflow.*;

/**
 * Configuration for performing {@link CleanWorkflowsUserService}.
 */
public class CleanWorkflowsConfig
{
	/**
	 * The possible policies to use when closing the data space.
	 * @see CleanWorkflowsConfig#setDataSpaceClosePolicy(DataSpaceClosePolicy).
	 */
	public enum DataSpaceClosePolicy {
		CLOSE, DELETE, DELETE_HISTORY;
	}

	private boolean useWorkingDataSpace;
	private DataSpaceClosePolicy dataSpaceClosePolicy;
	private List<AdaptationHome> masterDataSpaces;
	private List<AdaptationHome> childDataSpacesToSkip;
	private List<PublishedProcess> workflowPublications;
	private Date createdBeforeDate;
	private Integer createdBeforeNumOfDays;
	private boolean includeActive;
	private boolean includeCompleted;

	/**
	 * {@link #setUseWorkingDataSpace(boolean)}
	 */
	public boolean isUseWorkingDataSpace()
	{
		return useWorkingDataSpace;
	}

	/**
	 * Set whether to use the <code>workingDataSpace</code> data context variable of
	 * the workflows to determine which data spaces to clean
	 *
	 * @param useWorkingDataSpace whether to use <code>workingDataSpace</code>
	 */
	public void setUseWorkingDataSpace(boolean useWorkingDataSpace)
	{
		this.useWorkingDataSpace = useWorkingDataSpace;
	}

	/**
	 * {@link #setDataSpaceClosePolicy(DataSpaceClosePolicy)}
	 */
	public DataSpaceClosePolicy getDataSpaceClosePolicy()
	{
		return dataSpaceClosePolicy;
	}

	/**
	 * Set the data space close policy.
	 * {@link DataSpaceClosePolicy#CLOSE} indicates to just close the data space.
	 * {@link DataSpaceClosePolicy#DELETE} indicates to close and delete the data space
	 * (marks it for purge).
	 * {@link DataSpaceClosePolicy#DELETE_HISTORY} indicates to close and delete the data space,
	 * and also delete its history (will occur in the next purge).
	 *
	 * @param dataSpaceClosePolicy the close policy
	 */
	public void setDataSpaceClosePolicy(DataSpaceClosePolicy dataSpaceClosePolicy)
	{
		this.dataSpaceClosePolicy = dataSpaceClosePolicy;
	}

	/**
	 * {@link #setMasterDataSpaces(List)}
	 */
	public List<AdaptationHome> getMasterDataSpaces()
	{
		return this.masterDataSpaces;
	}

	/**
	 * Set the list of master data spaces whose child data spaces should be closed.
	 * If <code>useWorkingDataSpace</code> is specified, this is optional but can be
	 * specified in addition to the working data spaces.
	 *
	 * @param masterDataSpaces the list of master data spaces
	 */
	public void setMasterDataSpaces(List<AdaptationHome> masterDataSpaces)
	{
		this.masterDataSpaces = masterDataSpaces;
	}

	/**
	 * {@link #setChildDataSpacesToSkip(List)}
	 */
	public List<AdaptationHome> getChildDataSpacesToSkip()
	{
		return this.childDataSpacesToSkip;
	}

	/**
	 * Set the list of child data spaces to not close. This can be used when the master
	 * data space typically contains child data spaces specific to workflows, but also contains
	 * some static child data spaces that should never be closed.
	 *
	 * The list of child data spaces will be checked before any data space is closed, whether
	 * its a working data space or a child of one of the specified master data spaces.
	 *
	 * @param childDataSpacesToSkip the list of child data spaces to not close
	 */
	public void setChildDataSpacesToSkip(List<AdaptationHome> childDataSpacesToSkip)
	{
		this.childDataSpacesToSkip = childDataSpacesToSkip;
	}

	/**
	 * {@link #setWorkflowPublications(List)}
	 */
	public List<PublishedProcess> getWorkflowPublications()
	{
		return this.workflowPublications;
	}

	/**
	 * Set the list of workflow publications for the workflows that should be terminated and cleaned
	 *
	 * @param workflowPublications the list of workflow publications
	 */
	public void setWorkflowPublications(List<PublishedProcess> workflowPublications)
	{
		this.workflowPublications = workflowPublications;
	}

	/**
	 * {@link #setCreatedBeforeDate(Date)}
	 */
	public Date getCreatedBeforeDate()
	{
		return this.createdBeforeDate;
	}

	/**
	 * Set a date that indicates that only workflows created before it should be cleaned.
	 * The date is exclusive, meaning it's strictly before the date, not equal to it.
	 * <code>null</code> indicates to not restrict by date at all.
	 *
	 * @param createdBeforeDate the date
	 */
	public void setCreatedBeforeDate(Date createdBeforeDate)
	{
		this.createdBeforeDate = createdBeforeDate;
	}

	/**
	 * {@link #setCreatedBeforeNumOfDays(Integer)}
	 */
	public Integer getCreatedBeforeNumOfDays()
	{
		return this.createdBeforeNumOfDays;
	}

	/**
	 * Set the number of days that indicate not to clean workflows that were created within that many days from the current date.
	 * The current date is not considered a "day", so specifying 7 would indicate to keep workflows on the current day as well as
	 * 7 days prior. <code>null</code> indicates clean all workflows (unless a date is specified).
	 *
	 * @param createdBeforeNumOfDays
	 */
	public void setCreatedBeforeNumOfDays(Integer createdBeforeNumOfDays)
	{
		this.createdBeforeNumOfDays = createdBeforeNumOfDays;
	}

	/**
	 * {@link #setIncludeActive(boolean)}
	 */
	public boolean isIncludeActive()
	{
		return this.includeActive;
	}

	/**
	 * Get whether to include active workflows when cleaning. Default is <code>false</code>.
	 *
	 * @param includeActive whether to include active workflows
	 */
	public void setIncludeActive(boolean includeActive)
	{
		this.includeActive = includeActive;
	}

	/**
	 * {@link #setIncludeCompleted(boolean)}
	 */
	public boolean isIncludeCompleted()
	{
		return this.includeCompleted;
	}

	/**
	 * Get whether to include completed workflows when cleaning. Default is <code>false</code>.
	 *
	 * @param includeCompleted whether to include completed workflows
	 */
	public void setIncludeCompleted(boolean includeCompleted)
	{
		this.includeCompleted = includeCompleted;
	}
}
