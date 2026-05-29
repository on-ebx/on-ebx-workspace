package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public interface Procedures
{

	public static class Create
	{
		public static Adaptation execute(
			final Session session,
			final AdaptationTable adaptationTable,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Create.execute(session, adaptationTable, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final Session session,
			final AdaptationTable adaptationTable,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			CreateRecordProcedure procedure = new CreateRecordProcedure(
				adaptationTable,
				pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(session);
			return procedure.getCreatedRecord();
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final AdaptationTable adaptationTable,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Create.execute(pContext, adaptationTable, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final AdaptationTable adaptationTable,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			CreateRecordProcedure procedure = new CreateRecordProcedure(
				adaptationTable,
				pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(pContext);
			return procedure.getCreatedRecord();
		}
	}

	public static class Delete
	{
		public static void execute(final Session session, final Adaptation adaptation)
			throws OperationException
		{
			Procedures.Delete.execute(session, adaptation, true, false, false);
		}

		public static void execute(
			final Session session,
			final Adaptation adaptation,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation,
			final boolean deletingChildren) throws OperationException
		{
			DeleteRecordProcedure procedure = new DeleteRecordProcedure(adaptation);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.setDeletingChildren(deletingChildren);
			procedure.execute(session);
		}

		public static void execute(final ProcedureContext pContext, final Adaptation adaptation)
			throws OperationException
		{
			Delete.execute(pContext, adaptation, true, false, false);
		}

		public static void execute(
			final ProcedureContext pContext,
			final Adaptation adaptation,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation,
			final boolean deletingChildren) throws OperationException
		{
			DeleteRecordProcedure procedure = new DeleteRecordProcedure(adaptation);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.setDeletingChildren(deletingChildren);
			procedure.execute(pContext);
		}
	}

	public static class Modify
	{
		public static Adaptation execute(
			final Session session,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Modify.execute(session, adaptation, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final Session session,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			ModifyValuesProcedure procedure = new ModifyValuesProcedure(adaptation, pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(session);
			return procedure.getAdaptation();
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Modify.execute(pContext, adaptation, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			ModifyValuesProcedure procedure = new ModifyValuesProcedure(adaptation, pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(pContext);
			return procedure.getAdaptation();
		}
	}

	public static class Duplicate
	{
		public static Adaptation execute(
			final Session session,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Duplicate.execute(session, adaptation, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final Session session,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			DuplicateRecordProcedure procedure = new DuplicateRecordProcedure(
				adaptation,
				pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(session);
			return procedure.getCreatedRecord();
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap) throws OperationException
		{
			return Procedures.Duplicate.execute(pContext, adaptation, pathValueMap, true, false);
		}

		public static Adaptation execute(
			final ProcedureContext pContext,
			final Adaptation adaptation,
			final Map<Path, Object> pathValueMap,
			final boolean enableAllPrivileges,
			final boolean disableTriggerActivation) throws OperationException
		{
			DuplicateRecordProcedure procedure = new DuplicateRecordProcedure(
				adaptation,
				pathValueMap);
			procedure.setAllPrivileges(enableAllPrivileges);
			procedure.setTriggerActivation(!disableTriggerActivation);
			procedure.execute(pContext);
			return procedure.getCreatedRecord();
		}
	}
}
