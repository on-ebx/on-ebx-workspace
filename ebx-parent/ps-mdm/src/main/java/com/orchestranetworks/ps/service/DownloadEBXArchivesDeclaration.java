package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.DataspaceEntitySelection;
import com.orchestranetworks.userservice.UserService;

public class DownloadEBXArchivesDeclaration extends TechAdminOnlyServiceDeclaration
{

	public DownloadEBXArchivesDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("DownloadEBXArchives")
					: ServiceKey.forModuleServiceName(moduleName, "DownloadEBXArchives"),
			null,
			"Download EBX archive",
			"Download a zip of selected previously exported archives",
			null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService() {
		return new DownloadEBXFiles("ebx.repository.directory", "archives", "ebxArchive");
	}

}