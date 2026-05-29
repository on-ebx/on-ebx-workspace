package com.orchestranetworks.ps.admin.devartifacts.modifier;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;

/**
 * Modifies a Role record from the directory, on import, to not clear out the email when it's empty.
 * In other words, if the email is specified, it will import it like normal, but if it's not,
 * it won't replace an existing email. This is because different environments may specify different
 * email addresses for a role, but if you do specify an email address as part of your artifacts,
 * then you want to import it.
 */
public class DirectoryRoleArtifactFileModifier extends ArtifactFileModifier
{
	private static final String NAME_FIELD_NAME = AdminUtil.getDirectoryRolesNamePath()
		.getLastStep()
		.format();
	private static final String EMAIL_FIELD_NAME = AdminUtil.getDirectoryRolesEmailPath()
		.getLastStep()
		.format();

	private String roleFieldName;
	private String currentRoleName;
	private boolean includesEmail;
	private Map<String, String> existingRoleEmailMap;

	/**
	 * Construct the modifier
	 * 
	 * @param rolesTable the Roles table
	 * @param existingRoleEmailMap a map of emails for existing records prior to the processing,
	 *        (key = the role name, value = the email). Should never be <code>null</code>.
	 */
	public DirectoryRoleArtifactFileModifier(
		AdaptationTable rolesTable,
		Map<String, String> existingRoleEmailMap)
	{
		roleFieldName = rolesTable.getTablePath().getLastStep().format();
		this.existingRoleEmailMap = existingRoleEmailMap;
	}

	@Override
	public List<String> modifyExport(String line)
	{
		// Don't change anything on export
		return null;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		// If a new role record is starting
		if (containsStartTag(line, roleFieldName))
		{
			// Re-initialize these values
			includesEmail = false;
			currentRoleName = null;
		}
		// Otherwise, if a role record is ending
		else if (containsEndTag(line, roleFieldName))
		{
			// If the role didn't include an email in the input
			// (also check that role name isn't null, but really it should never be null at this point)
			if (!includesEmail && currentRoleName != null)
			{
				// Get the email from the map, which is the email for the pre-existing record
				String existingRoleEmail = existingRoleEmailMap.get(currentRoleName);
				// If there was a pre-existing record and it had an email
				if (existingRoleEmail != null)
				{
					// Instead of just sending the end tag of the role record,
					// add a line for the email, specifying the value that already exists,
					// and then add the role record tag.
					// This works because email is the last field in the table.
					List<String> list = new ArrayList<>();
					list.add(
						new StringBuilder("<").append(EMAIL_FIELD_NAME)
							.append(">")
							.append(existingRoleEmail)
							.append("</")
							.append(EMAIL_FIELD_NAME)
							.append(">")
							.toString());
					list.add(line);
					return list;
				}
			}
		}
		// Otherwise, it's some other tag
		else
		{
			// Get the name from the line
			String name = getValue(line, NAME_FIELD_NAME);
			// If the name is null, then it wasn't a line for the name
			if (name == null)
			{
				// Get the email from the line, which will be null if there's
				// no email tag on this line
				String email = getValue(line, EMAIL_FIELD_NAME);
				if (email != null)
				{
					// The email could be an empty string if there's a tag with no
					// value in it. If there's an actual value though, set the boolean
					// to indicate that.
					if (email.length() > 0)
					{
						includesEmail = true;
					}
					// If there wasn't an actual value, we want to remove the line from the import file,
					// because we'll be adding it when we process the end tag for the role
					else
					{
						return new ArrayList<>();
					}
				}
			}
			// Otherwise, store the role name for later use
			else
			{
				currentRoleName = name;
			}
		}
		// If we didn't return anything above, then it means no change is needed
		return null;
	}
}
