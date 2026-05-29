package com.orchestranetworks.ps.util;

import com.orchestranetworks.service.*;

public class ImportExportUtils
{
	public static class ExportArchiveProcedure implements Procedure
	{
		private ArchiveExportSpec spec;

		public ExportArchiveProcedure(ArchiveExportSpec spec)
		{
			this.spec = spec;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			pContext.doExportArchive(spec);
		}
	}

	public static class ImportArchiveProcedure implements Procedure
	{
		private ArchiveImportSpec spec;

		public ImportArchiveProcedure(ArchiveImportSpec spec)
		{
			this.spec = spec;
		}

		@Override
		public void execute(ProcedureContext pContext) throws Exception
		{
			boolean allPriv = pContext.isAllPrivileges();
			pContext.setAllPrivileges(true);
			try
			{
				pContext.doImportArchive(spec);
			}
			finally
			{
				pContext.setAllPrivileges(allPriv);
			}
		}
	}

}
