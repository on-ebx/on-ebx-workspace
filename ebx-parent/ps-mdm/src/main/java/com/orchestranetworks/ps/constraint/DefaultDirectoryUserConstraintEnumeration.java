package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * A subclass of {@link UserConstraintEnumeration} that is specifically for use with EBX's default directory.
 * It allows you to specify an xpath filter to filter the users, or to override a method in a subclass to
 * provide an {@link AdaptationFilter}. If you don't need to filter the users, it's preferable to use
 * the parent class, which doesn't specifically rely on having the default directory.
 * 
 * Note that since this queries the built-in Users Roles table directly, it will not return any users that aren't
 * actually stored in that table (i.e. if your directory constructs users from another source, those won't be returned).
 */
public class DefaultDirectoryUserConstraintEnumeration extends UserConstraintEnumeration
{
	private String usersRolesXPathFilter;

	public DefaultDirectoryUserConstraintEnumeration()
	{
		setRelaxed(false);
	}

	@Override
	public void setup(ConstraintContext aContext)
	{
		// do nothing
	}

	@Override
	public List<String> getValues(ValueContext aContext) throws InvalidSchemaException
	{
		LinkedHashSet<String> usernames = new LinkedHashSet<>();
		RequestResult requestResult = queryUsersRoles(aContext);
		try
		{
			for (Adaptation usersRolesRecord; (usersRolesRecord = requestResult
				.nextAdaptation()) != null;)
			{
				usernames
					.add(usersRolesRecord.getString(AdminUtil.getDirectoryUsersRolesUserPath()));
			}
		}
		finally
		{
			requestResult.close();
		}
		return new ArrayList<>(usernames);
	}

	private RequestResult queryUsersRoles(ValueContext valueContext)
	{
		Repository repo = valueContext.getHome().getRepository();
		Adaptation directoryDataSet = AdminUtil.getDirectoryDataSet(repo);
		AdaptationTable usersRolesTable = AdminUtil.getDirectoryUsersRolesTable(directoryDataSet);
		Request request = usersRolesTable.createRequest();
		AdaptationFilter filter = getUsersRolesSpecificFilter(valueContext);
		// Apply the xpath only if there's no programmatic filter.
		// If there's also no xpath, then it will simply query for all rows.
		if (filter == null)
		{
			String xpath = getUsersRolesXPathFilter();
			if (xpath != null)
			{
				request.setXPathFilter(xpath);
			}
		}
		else
		{
			request.setSpecificFilter(filter);
		}
		return request.execute();
	}

	/**
	 * Get a programmatic filter over the Users Roles table, specifying the users to allow.
	 * The Users Roles table is the built-in EBX table that joins users to their roles.
	 * This is NOT a filter over the Users table itself, because while filtering you may wish to make
	 * decisions on which users to show based on a role.
	 * The enumeration will just contain the unique users referenced by these User Roles rows, however.
	 * If a user is returned twice, it won't be shown twice.
	 * 
	 * By default, this returns <code>null</code>, but can be subclassed to provide a filter.
	 * 
	 * @param valueContext the {@link ValueContext} of the current record
	 * @return a filter, or <code>null</code>
	 */
	protected AdaptationFilter getUsersRolesSpecificFilter(ValueContext valueContext)
	{
		return null;
	}

	/**
	 * @see {@link #setUsersRolesXPathFilter(String)}
	 */
	public String getUsersRolesXPathFilter()
	{
		return usersRolesXPathFilter;
	}

	/**
	 * Set an xpath to use as a filter on the Users Roles table, specifying the users to allow.
	 * See the javadoc for {@link #getUsersRolesSpecificFilter(ValueContext)} for more information
	 * about why it is the Users Roles table, not the Users table itself.
	 * 
	 * @param usersRolesXPathFilter the xpath
	 */
	public void setUsersRolesXPathFilter(String usersRolesXPathFilter)
	{
		this.usersRolesXPathFilter = usersRolesXPathFilter;
	}
}
