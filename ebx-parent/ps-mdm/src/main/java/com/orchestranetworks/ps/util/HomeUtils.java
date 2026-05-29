package com.orchestranetworks.ps.util;

import java.text.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;

/**
 * Utility class to manipulate data spaces and snapshots.
 * 
 * @author MCH
 */
public class HomeUtils
{

	/**
	 * Gets the ancestors.
	 *
	 * @author MCH
	 * 
	 *         Get all the data spaces, ancestors of a data space or a snapshot
	 *         
	 * @param pHome the home to get the ancestors from
	 * @return data spaces, the ancestors
	 */
	public static List<AdaptationHome> getAncestors(final AdaptationHome pHome)
	{
		List<AdaptationHome> ancestors = new ArrayList<>();

		AdaptationHome initialVersion = pHome;
		if (!pHome.isBranch())
		{
			initialVersion = pHome.getParent();
		}

		if (initialVersion == null)
		{
			return ancestors;
		}

		AdaptationHome home = initialVersion.getParent();
		if (home == null)
		{
			return ancestors;
		}

		ancestors.add(home);
		ancestors.addAll(getAncestors(home));

		return ancestors;
	}

	public static String formatHomeLabel(AdaptationHome aHome, Locale aLocale)
	{
		StringBuffer buffer = new StringBuffer();
		int depth = HomeUtils.getHomeDepth(aHome);

		for (int i = 0; i < depth * 3; i++)
		{
			buffer.append("&nbsp;");
		}

		if (aHome.isInitialVersion())
		{
			for (int i = 0; i < 3; i++)
			{
				buffer.append("&nbsp;");
			}

			buffer.append((aHome.getBranchChildren().get(0)).getLabelOrName(aLocale))
				.append("&nbsp;&lt;initial version&gt;");
		}
		else
		{
			if (aHome.isVersion())
				buffer.append("(V)");
			buffer.append(aHome.getLabelOrName(aLocale));
		}
		return buffer.toString();
	}

	public static int getHomeDepth(AdaptationHome aHome)
	{

		AdaptationHome parentBranch = aHome.getParentBranch();

		if (parentBranch == null)
			return 0;

		return getHomeDepth(parentBranch) + 1;
	}

	public static void mergeDataSpaceToParent(
		final Session session,
		final AdaptationHome childDataSpaceHome)
		throws OperationException
	{
		// Merge of child data space procedure
		final Procedure merge = new Procedure()
		{
			@Override
			public void execute(final ProcedureContext pContext) throws Exception
			{
				closeDataSpaceChildren(childDataSpaceHome, session);
				pContext.setAllPrivileges(true);
				pContext.doMergeToParent(childDataSpaceHome);
				pContext.setAllPrivileges(false);
			}
		};

		// Merge of child data space execution
		final ProgrammaticService mergeService = ProgrammaticService
			.createForSession(session, childDataSpaceHome.getParentBranch());
		ProcedureResult result = mergeService.execute(merge);
		OperationException resultException = result.getException();
		if (resultException != null)
		{
			throw resultException;
		}
		if (result.hasFailed())
		{
			throw OperationException.createError("Dataspace Merge execution failed.");
		}
	}

	public static void closeDataSpaceChildren(AdaptationHome dataSpace, Session session)
		throws OperationException
	{
		// Closing a Data Space Child will recursively Close all its Ancestors
		List<AdaptationHome> dataSpaceChildList = dataSpace.getVersionChildren();
		for (AdaptationHome dataSpaceChild : dataSpaceChildList)
		{
			if (dataSpaceChild.isOpen())
			{
				dataSpaceChild.getRepository().closeHome(dataSpaceChild, session);
			}

		}

	}

	/**
	 * Create a child data space with a standard key and label.
	 * 
	 * This is equivalent to calling {@link #createChildDataSpace(Session, AdaptationHome, String, String, String)}
	 * with <code>null</code> for the <code>childNamePrefix</code>.
	 * 
	 * @param session the session
	 * @param dataSpace the parent data space
	 * @param childLabelPrefix an optional prefix to prepend to the standard data space label
	 * @param permissionsTemplateDataSpaceName the data space to use as the permissions template
	 * @return the created child data space
	 * @throws OperationException if an error occurred creating the child data space
	 */
	public static AdaptationHome createChildDataSpace(
		Session session,
		AdaptationHome dataSpace,
		String childLabelPrefix,
		String permissionsTemplateDataSpaceName)
		throws OperationException
	{
		return createChildDataSpace(
			session,
			dataSpace,
			null,
			childLabelPrefix,
			permissionsTemplateDataSpaceName);
	}

	/**
	 * Create a child data space with a standard key and label.
	 * 
	 * @param session the session
	 * @param dataSpace the parent data space
	 * @param childNamePrefix an optional prefix to prepend to the standard data space name
	 * @param childLabelPrefix an optional prefix to prepend to the standard data space label
	 * @param permissionsTemplateDataSpaceName the data space to use as the permissions template
	 * @return the created child data space
	 * @throws OperationException if an error occurred creating the child data space
	 */
	public static synchronized AdaptationHome createChildDataSpace(
		Session session,
		AdaptationHome dataSpace,
		String childNamePrefix,
		String childLabelPrefix,
		String permissionsTemplateDataSpaceName)
		throws OperationException
	{
		UserReference user = session.getUserReference();
		Repository repo = dataSpace.getRepository();
		HomeCreationSpec homeCreationSpec = new HomeCreationSpec();
		homeCreationSpec.setOwner(user);
		homeCreationSpec.setParent(dataSpace);

		if (permissionsTemplateDataSpaceName != null)
		{
			AdaptationHome templateDataSpace = repo
				.lookupHome(HomeKey.forBranchName(permissionsTemplateDataSpaceName));
			if (templateDataSpace == null)
			{
				LoggingCategory.getKernel().error(
					"Permissions template data space " + permissionsTemplateDataSpaceName
						+ " not found.");
			}
			else
			{
				homeCreationSpec.setHomeToCopyPermissionsFrom(templateDataSpace);
			}
		}
		return createChildDataSpace(
			session,
			repo,
			childNamePrefix,
			childLabelPrefix,
			homeCreationSpec);
	}

	/**
	 * Create a child data space, passing in the {@link HomeCreationSpec} for the configuration.
	 * If the key is not specified in the configuration, then a default will be generated.
	 * Also, when the key is not specified and the label is also not specified, the default
	 * label will be generated. (If the key is specified, it is assumed that whatever the label is
	 * set to in the configuration is what is desired. If <code>null</code>, then it's desired to
	 * be <code>null</code>.)
	 * 
	 * @param session the session
	 * @param repository the repository
	 * @param childNamePrefix an optional prefix to prepend to the standard data space name
	 *                        (ignored if the specification provides the key)
	 * @param childLabelPrefix an optional prefix to prepend to the standard data space label
	 *                        (ignored if the specification provides the key, or does not provide
	 *                        the key but provides a label)
	 * @param homeCreationSpec the configuration details (including parent data space)
	 * @return the created child data space
	 * @throws OperationException if an error occurred creating the child data space
	 */
	public static synchronized AdaptationHome createChildDataSpace(
		Session session,
		Repository repository,
		String childNamePrefix,
		String childLabelPrefix,
		HomeCreationSpec homeCreationSpec)
		throws OperationException
	{
		if (homeCreationSpec.getKey() == null)
		{
			HomeKey homeKey = generateDefaultHomeKey(repository, childNamePrefix);
			homeCreationSpec.setKey(homeKey);

			if (homeCreationSpec.getLabel() == null)
			{
				String homeKeyName = homeKey.getName();
				String dateStr;
				if (childNamePrefix == null)
				{
					dateStr = homeKeyName;
				}
				else
				{
					dateStr = homeKeyName.substring(childNamePrefix.length());
				}
				String userLabel = session.getUserReference().getLabel();
				homeCreationSpec.setLabel(
					UserMessage
						.createInfo(childLabelPrefix + " by " + userLabel + " at " + dateStr));
			}
		}
		return repository.createHome(homeCreationSpec, session);
	}

	private static HomeKey generateDefaultHomeKey(Repository repository, String childNamePrefix)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat(
			CommonConstants.DATA_SPACE_NAME_DATE_TIME_FORMAT);

		Date newDate = new Date();
		HomeKey homeKey = generateHomeKey(childNamePrefix, newDate, dateFormat);

		// check if generated HomeKey already exists and if so increment the Date by 1 millisecond and try again (up to 10 times)
		//  -- should very rarely need more than 2 times
		for (int i = 0; i < 10 && repository.lookupHome(homeKey) != null; i++)
		{
			DateUtils.addMilliseconds(newDate, 1);
			homeKey = generateHomeKey(childNamePrefix, newDate, dateFormat);
		}
		return homeKey;
	}

	private static HomeKey generateHomeKey(
		String childNamePrefix,
		Date date,
		SimpleDateFormat dateFormat)
	{
		String childDataSpaceDateTimeStr = dateFormat.format(date);
		String branchName = childDataSpaceDateTimeStr;
		if (childNamePrefix != null)
		{
			branchName = childNamePrefix + branchName;
		}
		return HomeKey.forBranchName(branchName);
	}

	public static void closeDataSpace(Session session, AdaptationHome dataSpace)
		throws OperationException
	{
		dataSpace.getRepository().closeHome(dataSpace, session);
	}

	public static void deleteDataspace(
		Session session,
		AdaptationHome dataSpace,
		boolean deleteHistory)
		throws OperationException
	{
		Repository repo = dataSpace.getRepository();
		if (deleteHistory)
		{
			repo.getPurgeDelegate().markHomeForHistoryPurge(dataSpace, session);
		}
		repo.deleteHome(dataSpace, session);
	}

	/** Answer whether the first dataspace is a descendant of the second */
	public static boolean isDescendentOf(AdaptationHome desc, AdaptationHome master)
	{
		while (desc != null)
		{
			desc = desc.getParent();
			if (master.equals(desc))
				return true;
		}
		return false;
	}
}
