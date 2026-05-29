package com.orchestranetworks.ps.scheduledtask;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.DateUtils.*;
import com.orchestranetworks.scheduler.*;
import com.orchestranetworks.service.*;

/**
 * Creates a snapshot of a specified data space and parses a label from the prefix and domain settings.
 * This scheduled task will also delete any snapshots that are older than specified in the daysTokeep parameter.
 * <p>
 * The following arguments are required by this scheduled task:
 * <ul>
 * <li><b>dataSpace:</b> String representing the data space (branch) from which the snapshot will be created.</li>
 * <li><b>domain:</b> String representing the Domain of the targeted data space.</li>
 * <li><b>prefix:</b> String to help distinguish the type of snapshot this scheduled task is creating.</li>
 * <li><b>daysToKeep:</b> Integer specifying the number of days to keep the snapshot.</li>
 * </ul>
 * The snapshot label will appear as <i>prefix</i>_<i>domain</i>_<i>date of creation</i>
 * <p>
 * For example, if the following configuration is defined for this scheduled task:
 * <ul>
 * <li><b>dataSpace</b> = MenuMasterDataSpace</li>
 * <li><b>prefix</b> = NIGHTLYSNAPSHOT</li>
 * <li><b>domain</b> = MENU</li>
 * <li><b>daysToKeep</b> = 7</li>
 * </ul>
 * then the snapshot label would read, for an August, 17, 2017 creation date, as:
 * <p>
 * <code>NIGHTLYSNAPSHOT_MENU_2017-08-17</code>
 * <p>
 * Once 7 days goes by, based on the example configuration above, the snapshot <code>NIGHTLYSNAPSHOT_MENU_2017-08-17</code> will be deleted. 
 * 
 * @author Orchestra Professional Services
 *
 */
public class SnapshotScheduledTask extends ScheduledTask
{
	/**
	 * Used to format the date on the snapshot label.
	 */
	private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
		CommonConstants.EBX_DATE_FORMAT);

	private String dataSpace = Repository.REFERENCE.getName();

	private String prefix = "";

	private String domain = "";

	private int daysToKeep;

	public String getDataSpace()
	{
		return dataSpace;
	}

	public void setDataSpace(String dataSpace)
	{
		this.dataSpace = dataSpace;
	}
	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public String getPrefix()
	{
		return prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public int getDaysToKeep()
	{
		return this.daysToKeep;
	}

	public void setDaysToKeep(int daysToKeep)
	{
		this.daysToKeep = daysToKeep;
	}

	/**
	 * Override this method if the data space is different than the one found by looking up by
	 * dataSpace name.
	 */
	protected AdaptationHome getBranch(Repository repo)
	{
		return repo.lookupHome(HomeKey.forBranchName(dataSpace));
	}

	/**
	 * If the branch can be null or closed, override this to return false
	 */
	protected boolean warnIfNoBranch()
	{
		return true;
	}

	@Override
	public void execute(ScheduledExecutionContext context)
		throws OperationException, ScheduledTaskInterruption
	{
		Repository repo = context.getRepository();
		AdaptationHome dataSpaceHome = getBranch(repo);
		if (dataSpaceHome == null || !dataSpaceHome.isOpen())
		{
			if (warnIfNoBranch())
				LoggingCategory.getKernel().warn("Data space " + dataSpace + " does not exist.");
		}
		createSnapshot(context, repo, dataSpaceHome);
		purgeSnapshots(context, dataSpaceHome);
	}

	protected void createSnapshot(
		ScheduledExecutionContext context,
		Repository repo,
		AdaptationHome dataSpaceHome)
		throws OperationException, ScheduledTaskInterruption
	{
		String creationDate = DATE_FORMATTER
			.format(new Date(Calendar.getInstance().getTimeInMillis()));

		String label = prefix + "_" + domain + "_" + creationDate;
		HomeKey key = HomeKey.forVersionName(label);

		HomeCreationSpec snapshot = new HomeCreationSpec();
		snapshot.setParent(dataSpaceHome);
		snapshot.setKey(key);
		snapshot.setOwner(CommonConstants.TECH_ADMIN);
		snapshot.setLabel(UserMessage.createInfo(label));
		snapshot.setDescription(
			UserMessage.createInfo(
				"Snapshot created on " + creationDate + " for the Data Space: " + dataSpace));
		snapshot.setHomeToCopyPermissionsFrom(dataSpaceHome);

		// Verify that the snapshot does not already exist. 
		if (repo.lookupHome(key) == null)
		{
			repo.createHome(snapshot, context.getSession());
		}
	}

	protected void purgeSnapshots(ScheduledExecutionContext context, AdaptationHome dataSpaceHome)
		throws OperationException
	{
		// Find the date at the specified number of days prior to the current date
		Date today = new Date();
		Date daysBack = DateUtils.subtract(today, DateConstant.DAY, daysToKeep);
		// Delete all children that were closed earlier than that date
		markChildrenForDeletion(context.getSession(), dataSpaceHome, daysBack);
	}

	protected void markChildrenForDeletion(
		Session session,
		AdaptationHome dataSpaceHome,
		Date earlierThanDate)
		throws OperationException
	{
		List<AdaptationHome> childVersions = dataSpaceHome.getVersionChildren();

		for (AdaptationHome childVersion : childVersions)
		{
			if (childVersion.isTechnicalVersion())
				continue;
			if (!childVersion.isVersion())
				continue;
			String name = childVersion.getKey().getName();

			// Search for snapshots with a label starting with the prefix_domain string.
			// This string makes up the first part of a snapshot created by this scheduled task. 
			if (name.startsWith(prefix + "_" + domain))
			{
				Date creationDate = childVersion.getCreationDate();

				if (DateTimeUtils.afterExclusive(earlierThanDate, creationDate))
				{
					if (childVersion.isOpen())
					{
						HomeUtils.closeDataSpace(session, childVersion);
					}

					if (!childVersion.isOpen() && childVersion.getTerminationDate() != null)
						HomeUtils.deleteDataspace(session, childVersion, false);
				}
			}
		}
	}

}
