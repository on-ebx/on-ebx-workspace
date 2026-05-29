package com.orchestranetworks.ps.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.orchestranetworks.ps.procedure.ProcedureExecutor;
import com.orchestranetworks.ps.util.ImportExportUtils;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaTypeName;
import com.orchestranetworks.schema.dynamic.BeanDefinition;
import com.orchestranetworks.service.Archive;
import com.orchestranetworks.service.ArchiveExportSpec;
import com.orchestranetworks.service.ArchiveImportSpec;
import com.orchestranetworks.service.ArchiveImportSpecMode;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.service.comparison.DifferenceBetweenHomes;
import com.orchestranetworks.service.comparison.DifferenceHelper;
import com.orchestranetworks.ui.selection.DataspaceEntitySelection;
import com.orchestranetworks.userservice.ObjectKey;
import com.orchestranetworks.userservice.UserServiceEventOutcome;
import com.orchestranetworks.userservice.UserServiceObjectContext;
import com.orchestranetworks.userservice.UserServiceObjectContextBuilder;
import com.orchestranetworks.userservice.UserServiceSetupObjectContext;

/**
 *  Service to merge the changes from the parent branch (relative to the target branch's
 *  initial snapshot). 
 */
public class RebaseHomeService<S extends DataspaceEntitySelection> extends AbstractUserService<S>
{
	private AdaptationHome dataspace;
	private boolean preferChild = false;
	private static final ObjectKey _Input = ObjectKey.forName("input");
	private static final Path _PreferChildPath = Path.parse("./preferChild");
	/**
	 * Selection of the target dataspace -- by default it is the entity selection of
	 * service context.  Override this method if it needs to be a different target. 
	 * @return the target dataspace
	 */
	protected AdaptationHome getHome()
	{
		return context.getEntitySelection().getDataspace();
	}

	/**
	 * Indicates whether this service will allow user input for conflict resolution preferring child
	 *  or parent
	 *  @return whether or not to display choice of child/parent preference
	 */
	protected boolean allowChoice()
	{
		return true;
	}

	/**
	 * Indicates whether this service will preferring child values in rebase's merge in case of conflicts.
	 * @return whether or not child changes take precedence
	 */
	protected boolean isPreferChild()
	{
		return preferChild;
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		if (allowChoice())
		{
			preferChild = (Boolean) fromContext.getValueContext(_Input, _PreferChildPath)
				.getValue();
		}
		return super.readValues(fromContext);
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<S> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (allowChoice() && aContext.isInitialDisplay())
		{
			BeanDefinition def = context.defineObject(aBuilder, _Input);
			defineElement(
				def,
				_PreferChildPath,
				"Prefer child values",
				SchemaTypeName.XS_BOOLEAN,
				false);
		}
		super.setupObjectContext(aContext, aBuilder);
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		AdaptationHome home = getHome();
		this.dataspace = home;
		if (home == null || !home.isOpenBranch())
		{
			alert("Can only rebase an open branch");
			return;
		}
		try
		{
			//first, export delta archive from parent branch to temp file
			File archiveParent = exportDelta(session, true);
			File archiveChild = exportDelta(session, false);
			//next, import delta archive into this branch
			importDelta(session, archiveParent);
			importDelta(session, archiveChild);
			//last, delete archive
			archiveParent.delete();
			if (archiveChild != null)
				archiveChild.delete();
		}
		catch (IOException e)
		{
			throw OperationException.createError("Failed to rebase dataspace", e);
		}
	}

	private void importDelta(Session session, File file) throws IOException, OperationException
	{
		if (file == null)
			return;
		Archive archive = Archive.forFile(file);
		ArchiveImportSpec spec = new ArchiveImportSpec();
		spec.setArchive(archive);
		spec.setMode(ArchiveImportSpecMode.CHANGESET);
		ProcedureExecutor.executeProcedure(
			new ImportExportUtils.ImportArchiveProcedure(spec),
			session,
			dataspace);
	}

	private File exportDelta(Session session, boolean parent) throws IOException, OperationException
	{
		if (!isPreferChild() && !parent)
			return null;
		File file = File.createTempFile("Delta", ".ebx");
		Archive archive = Archive.forFile(file);
		ArchiveExportSpec spec = new ArchiveExportSpec();
		spec.setArchive(archive);
		DifferenceBetweenHomes diff = DifferenceHelper.compareHomes(
			parent ? dataspace.getParent() : dataspace,
			dataspace.getParentBranch(),
			false);
		spec.setDifferencesBetweenHomes(diff);
		spec.setDifferencesWithMinimalContentsOnRight(true);
		List<Adaptation> dataSets = dataspace.findAllRoots();
		for (Adaptation dataSet : dataSets)
		{
			spec.addInstance(dataSet.getAdaptationName(), true);
		}
		ProcedureExecutor.executeProcedure(
			new ImportExportUtils.ExportArchiveProcedure(spec),
			session,
			dataspace);
		return file;
	}

}
