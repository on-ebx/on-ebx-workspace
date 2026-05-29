/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.project.scripttask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.deepcopy.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.project.path.*;
import com.orchestranetworks.ps.project.workflow.*;
import com.orchestranetworks.ps.trigger.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.*;

/**
 */
public abstract class RestoreDetachedSubjectsScriptTask extends ScriptTask
	implements ProjectPathCapable
{
	protected abstract Path getSubjectTablePath(Adaptation projectRecord);

	protected abstract DeepCopyConfigFactory getDeepCopyConfigFactory();

	protected abstract Path getPathToSubject(Path tablePath);

	protected PrimaryKey getSubjectRecordPK(Adaptation subjectRelatedRecord)
	{
		Path pathToSubject = getPathToSubject(
			subjectRelatedRecord.getContainerTable().getTablePath());
		return pathToSubject == null ? subjectRelatedRecord.getOccurrencePrimaryKey()
			: PrimaryKey.parseString(subjectRelatedRecord.getString(pathToSubject));
	}

	@Override
	public void executeScript(ScriptTaskContext context) throws OperationException
	{
		Repository repo = context.getRepository();
		final Adaptation projectRecord = ProjectWorkflowUtilities
			.getProjectRecord(context, null, repo, getProjectPathConfig());

		Procedure proc = new Procedure()
		{
			@Override
			public void execute(ProcedureContext pContext) throws Exception
			{
				restoreDetachedSubjects(pContext, projectRecord);
			}
		};
		ProcedureExecutor.executeProcedure(proc, context.getSession(), projectRecord.getHome());
	}

	protected void restoreDetachedSubjects(ProcedureContext pContext, Adaptation projectRecord)
		throws OperationException
	{
		ProjectPathConfig pathConfig = getProjectPathConfig();
		Adaptation dataSet = projectRecord.getContainer();
		AdaptationHome dataSpace = dataSet.getHome();
		AdaptationHome initialSnapshot = dataSpace.getParent();
		Adaptation initialDataSet = initialSnapshot
			.findAdaptationOrNull(dataSet.getAdaptationName());

		String projectType = projectRecord.getString(pathConfig.getProjectProjectTypeFieldPath());
		Path projectProjectSubjectsPath = pathConfig
			.getProjectProjectSubjectsFieldPath(projectType);
		// TODO: Should we also handle a direct fk to subject (such as in Restaurant)? Don't have
		// need for it now since
		// we don't let them pick a different value after launching.
		if (projectProjectSubjectsPath == null)
		{
			throw OperationException.createError(
				"This script task can only be used with a project that has detachable subjects.");
		}
		List<Adaptation> currentSubjects = AdaptationUtil
			.getLinkedRecordList(projectRecord, projectProjectSubjectsPath);
		Set<PrimaryKey> currentSubjectPKs = new HashSet<>();
		for (Adaptation currentSubject : currentSubjects)
		{
			currentSubjectPKs.add(currentSubject.getOccurrencePrimaryKey());
		}

		Set<PrimaryKey> processedSubjectPKs = new HashSet<>();

		DifferenceBetweenInstances dataSetDiff = DifferenceHelper
			.compareInstances(dataSet, initialDataSet, false);
		List<DifferenceBetweenTables> tableDiffList = dataSetDiff.getDeltaTables();
		for (DifferenceBetweenTables tableDiff : tableDiffList)
		{

			List<DifferenceBetweenOccurrences> deltaDiffList = tableDiff.getDeltaOccurrences();
			for (DifferenceBetweenOccurrences deltaDiff : deltaDiffList)
			{
				processRecord(
					pContext,
					projectRecord,
					deltaDiff.getOccurrenceOnRight(),
					initialDataSet,
					processedSubjectPKs,
					currentSubjectPKs);
			}

			List<ExtraOccurrenceOnLeft> addedDiffList = tableDiff.getExtraOccurrencesOnLeft();
			for (ExtraOccurrenceOnLeft addedDiff : addedDiffList)
			{
				processRecord(
					pContext,
					projectRecord,
					addedDiff.getExtraOccurrence(),
					initialDataSet,
					processedSubjectPKs,
					currentSubjectPKs);
			}

			List<ExtraOccurrenceOnRight> deletedDiffList = tableDiff.getExtraOccurrencesOnRight();
			for (ExtraOccurrenceOnRight deletedDiff : deletedDiffList)
			{
				processRecord(
					pContext,
					projectRecord,
					deletedDiff.getExtraOccurrence(),
					initialDataSet,
					processedSubjectPKs,
					currentSubjectPKs);
			}
		}
	}

	protected void processRecord(
		ProcedureContext pContext,
		Adaptation projectRecord,
		Adaptation record,
		Adaptation initialDataSet,
		Set<PrimaryKey> processedSubjectPKs,
		Set<PrimaryKey> currentSubjectPKs) throws OperationException
	{
		Path subjectTablePath = getSubjectTablePath(projectRecord);
		Path recordTablePath = record.getContainerTable().getTablePath();
		Path pathToSubject = getPathToSubject(recordTablePath);
		PrimaryKey subjectRecordPK;
		if (pathToSubject == null)
		{
			// If record itself isn't the subject then it's not a record we need to process
			if (!subjectTablePath.equals(recordTablePath))
			{
				return;
			}
			subjectRecordPK = record.getOccurrencePrimaryKey();
		}
		else
		{
			subjectRecordPK = PrimaryKey.parseString(record.getString(pathToSubject));
		}

		if (!processedSubjectPKs.contains(subjectRecordPK))
		{
			if (!currentSubjectPKs.contains(subjectRecordPK))
			{
				AdaptationTable subjectTable = projectRecord.getContainer()
					.getTable(subjectTablePath);
				Adaptation subjectRecord = subjectTable
					.lookupAdaptationByPrimaryKey(subjectRecordPK);
				// There won't be a subject record when it's a delete.
				// Otherwise, delete it because we'll be replacing it from the initial snapshot
				if (subjectRecord != null)
				{
					CascadeDeleter.invokeCascadeDelete(subjectRecord, pContext);
				}

				AdaptationTable initialSubjectTable = initialDataSet.getTable(subjectTablePath);
				Adaptation initialSubjectRecord = record.getContainerTable()
					.toPublicReference()
					.equals(initialSubjectTable.toPublicReference()) ? record
						: initialSubjectTable.lookupAdaptationByPrimaryKey(subjectRecordPK);
				deepCopy(pContext, initialSubjectRecord);
			}
			processedSubjectPKs.add(subjectRecordPK);
		}
	}

	// private Adaptation lookupProjectSubjectRecord(
	// Adaptation projectRecord,
	// PrimaryKey subjectRecordPK) throws OperationException
	// {
	// ProjectPathConfig projectPathConfig = getProjectPathConfig();
	// SubjectPathConfig subjectPathConfig =
	// projectPathConfig.getSubjectPathConfig(getSubjectTablePath(projectRecord));
	// AdaptationTable projectSubjectTable = projectRecord.getContainer().getTable(
	// subjectPathConfig.getProjectSubjectTablePath());
	// Path[] pkPaths = projectSubjectTable.getPrimaryKeySpec();
	// Object[] pkValues = new Object[pkPaths.length];
	// for (int i = 0; i < pkPaths.length; i++)
	// {
	// Path pkPath = Path.SELF.add(pkPaths[i]);
	// if (pkPath.equals(subjectPathConfig.getProjectSubjectProjectFieldPath()))
	// {
	// pkValues[i] = projectRecord.getOccurrencePrimaryKey().format();
	// }
	// else if (pkPath.equals(subjectPathConfig.getProjectSubjectSubjectFieldPath()))
	// {
	// pkValues[i] = subjectRecordPK.format();
	// }
	// else
	// {
	// throw OperationException.createError("Table "
	// + projectSubjectTable.getTablePath()
	// + " contains an unknown primary key column. It should be a link table containing just a
	// project column and subject column.");
	// }
	// }
	// return
	// projectSubjectTable.lookupAdaptationByPrimaryKey(projectSubjectTable.computePrimaryKey(pkValues));
	// }

	protected Adaptation deepCopy(ProcedureContext pContext, Adaptation initialSubjectRecord)
		throws OperationException
	{
		DeepCopyImpl service = new DeepCopyImpl(getDeepCopyConfigFactory());
		return service.deepCopy(
			pContext,
			initialSubjectRecord,
			pContext.getAdaptationHome(),
			new RestoreDetachedSubjectDeepCopyDataModifier(),
			null,
			null);
	}

	/**
	 * Sets the new record's primary key fields to be the same as the original one, because by default EBX will create a new PK
	 * when it contains an auto-generated field.
	 */
	protected class RestoreDetachedSubjectDeepCopyDataModifier implements DeepCopyDataModifier
	{
		@Override
		public void modifyDuplicateRecordContext(
			ValueContextForUpdate context,
			Adaptation origRecord,
			DeepCopyConfig config,
			Session session)
		{
			Path[] pkSpec = origRecord.getContainerTable().getPrimaryKeySpec();
			for (int i = 0; i < pkSpec.length; i++)
			{
				Path pkPath = pkSpec[i];
				context.setValue(origRecord.get(pkPath), pkPath);
			}
		}
	}
}
