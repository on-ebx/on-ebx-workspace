package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.adaptation.uifacade.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

public class MergeRecordsProcedure extends GenericProcedure
{
	private Adaptation sourceAdaptation;
	private Adaptation targetAdaptation;
	private Map<Path, Object> pathValueMap;

	public static Adaptation execute(
		final Adaptation sourceAdaptation,
		final Adaptation targetAdaptation,
		final Map<Path, Object> pathValueMap,
		Session session)
		throws OperationException
	{
		return MergeRecordsProcedure
			.execute(sourceAdaptation, targetAdaptation, pathValueMap, session, true, false);
	}

	public static Adaptation execute(
		final Adaptation sourceAdaptation,
		final Adaptation targetAdaptation,
		final Map<Path, Object> pathValueMap,
		Session session,
		boolean enableAllPrivileges,
		boolean disableTriggerActivation)
		throws OperationException
	{
		MergeRecordsProcedure procedure = new MergeRecordsProcedure(
			sourceAdaptation,
			targetAdaptation,
			pathValueMap);
		procedure.setAllPrivileges(enableAllPrivileges);
		procedure.setTriggerActivation(!disableTriggerActivation);
		ProcedureExecutor.executeProcedure(procedure, session, targetAdaptation);
		return procedure.getTargetAdaptation();
	}

	public MergeRecordsProcedure()
	{
	}

	public MergeRecordsProcedure(
		final Adaptation sourceAdaptation,
		final Adaptation targetAdaptation,
		final Map<Path, Object> pathValueMap)
	{
		this.sourceAdaptation = sourceAdaptation;
		this.targetAdaptation = targetAdaptation;
		this.pathValueMap = pathValueMap;
	}

	public Adaptation getSourceAdaptation()
	{
		return sourceAdaptation;
	}

	public void setSourceAdaptation(Adaptation sourceAdaptation)
	{
		this.sourceAdaptation = sourceAdaptation;
	}

	public Adaptation getTargetAdaptation()
	{
		return targetAdaptation;
	}

	public void setTargetAdaptation(Adaptation targetAdaptation)
	{
		this.targetAdaptation = targetAdaptation;
	}

	public Map<Path, Object> getPathValueMap()
	{
		return pathValueMap;
	}

	public void setPathValueMap(Map<Path, Object> pathValueMap)
	{
		this.pathValueMap = pathValueMap;
	}

	public static Adaptation execute(
		ProcedureContext pContext,
		Adaptation sourceAdaptation,
		Adaptation targetAdaptation,
		Map<Path, Object> pathValueMap)
		throws OperationException
	{
		return MergeRecordsProcedure
			.execute(pContext, sourceAdaptation, targetAdaptation, pathValueMap, true, false);
	}

	public static Adaptation execute(
		ProcedureContext pContext,
		Adaptation sourceAdaptation,
		Adaptation targetAdaptation,
		Map<Path, Object> pathValueMap,
		boolean enableAllPrivileges,
		boolean disableTriggerActivation)
		throws OperationException
	{
		boolean origAllPrivileges = pContext.isAllPrivileges();
		boolean origTriggerActivation = pContext.isTriggerActivation();
		if (enableAllPrivileges)
		{
			pContext.setAllPrivileges(true);
		}
		if (disableTriggerActivation)
		{
			pContext.setTriggerActivation(false);
		}
		Adaptation modifiedAdaptation;
		try
		{
			final ValueContextForUpdate vc = pContext
				.getContext(targetAdaptation.getAdaptationName());
			// CfContentHolderForUpdate isn't public API
			((CfContentHolderForUpdate) vc).setSourceInMerge(sourceAdaptation);
			//			for (Map.Entry<Path, Object> entry : pathValueMap.entrySet())
			//			{
			//				vc.setValue(entry.getValue(), entry.getKey());
			//			}
			modifiedAdaptation = pContext.doModifyContent(targetAdaptation, vc);
		}
		finally
		{
			if (enableAllPrivileges)
			{
				pContext.setAllPrivileges(origAllPrivileges);
			}
			if (disableTriggerActivation)
			{
				pContext.setTriggerActivation(origTriggerActivation);
			}
		}
		return modifiedAdaptation;
	}

	@Override
	protected void doExecute(ProcedureContext pContext) throws OperationException
	{
		final ValueContextForUpdate vc = pContext.getContext(targetAdaptation.getAdaptationName());
		((CfContentHolderForUpdate) vc).setSourceInMerge(sourceAdaptation);
		//			for (Map.Entry<Path, Object> entry : pathValueMap.entrySet())
		//			{
		//				vc.setValue(entry.getValue(), entry.getKey());
		//			}
		targetAdaptation = pContext.doModifyContent(targetAdaptation, vc);
	}
}
