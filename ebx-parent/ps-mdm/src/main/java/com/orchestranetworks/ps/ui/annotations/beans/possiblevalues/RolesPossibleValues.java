package com.orchestranetworks.ps.ui.annotations.beans.possiblevalues;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.ui.annotations.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;

public class RolesPossibleValues extends PossibleValues
{

	@Override
	public void initValues()
	{
		DirectoryHandler directory = DirectoryHandler.getInstance(Repository.getDefault());
		List<Profile> profiles = directory.getProfiles(ProfileListContextBridge.getForWorkflow());
		for (Profile profile : profiles)
		{
			if (profile.isSpecificRole())
			{
				add(EnumerationValue.valueOf(((Role) profile).getRoleName()));
			}
		}
	}

}
