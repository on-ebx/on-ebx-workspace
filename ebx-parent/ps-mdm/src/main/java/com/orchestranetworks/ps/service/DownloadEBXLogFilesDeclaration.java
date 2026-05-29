package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.DataspaceEntitySelection;
import com.orchestranetworks.userservice.UserService;

public class DownloadEBXLogFilesDeclaration extends TechAdminOnlyServiceDeclaration
{

	public DownloadEBXLogFilesDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("DownloadEBXLogFiles")
				: ServiceKey.forModuleServiceName(moduleName, "DownloadEBXLogFiles"),
			null,
			"Download EBX log files",
			"Download a zip of the log files in the ebxLogs directory",
			null);
	}

	@Override
	public UserService<DataspaceEntitySelection> createUserService() {
		return new DownloadEBXFiles("ebx.logs.directory", null, "ebxLog");
	}
	
	
}