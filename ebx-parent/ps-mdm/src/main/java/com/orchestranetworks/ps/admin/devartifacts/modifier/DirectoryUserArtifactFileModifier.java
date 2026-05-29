package com.orchestranetworks.ps.admin.devartifacts.modifier;

import java.util.*;

import com.orchestranetworks.ps.util.*;

/**
 * Modifies a User record from the directory, on import, to switch the value of the
 * built-in administrator role from <code>true</code> to <code>false</code>.
 * This is used when <code>removeAdministratorRole</code> property is specified
 * as <code>true</code>.
 */
public class DirectoryUserArtifactFileModifier extends ArtifactFileModifier
{
	private static final String ADMINISTRATOR_FIELD_NAME = AdminUtil
		.getDirectoryUsersBuiltInRolesAdministratorPath()
		.getLastStep()
		.format();

	@Override
	public List<String> modifyExport(String line)
	{
		// Don't change anything on export
		return null;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		// If the line contains the administrator field
		if (containsStartTag(line, ADMINISTRATOR_FIELD_NAME))
		{
			// If the value is true, replace it with false
			String administratorStr = getValue(line, ADMINISTRATOR_FIELD_NAME);
			if ("true".equals(administratorStr))
			{
				return Arrays.asList(replaceValue(line, ADMINISTRATOR_FIELD_NAME, "false"));
			}
		}
		// Otherwise no change is needed
		return null;
	}
}
