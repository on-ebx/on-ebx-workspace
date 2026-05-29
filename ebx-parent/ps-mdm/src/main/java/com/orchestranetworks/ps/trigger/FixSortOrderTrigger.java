package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */


/**
 * Configured with a semi-colon separated list of 'commonValuePaths' which is the
 * means of grouping a collection of records that can be sorted, and a 'sortOrderPath'
 * which represents an integer field on the record indicating its index in the collection,
 * this trigger will:
 * - on creation of a new record in the collection, will assign the existing collection-count
 *   to the sortOrder field
 * - on delete of an existing record, will decrement the sortOrder of subsequent records
 * - on modify of an existing record's sortOrder, will adjust the impacted records in the group
 * 
 * Optionally, can be configured to start at 0 (default is to start at 1).
 * Also, optionally, <code>getOtherProperties<code> method can be overridden to return a more specific set of records to fix the sequence order
 */
public class FixSortOrderTrigger extends BaseTableTriggerEnforcingPermissions
{
	private String commonValuePathsString;
	private List<Path> commonValuePaths;
	private Path sortOrderPath;
	private boolean startAtOne = true;

	@Override
	public void setup(TriggerSetupContext context)
	{
		if (commonValuePathsString != null)
		{
			commonValuePaths = PathUtils.convertStringToPathList(commonValuePathsString, null);
		}
		if (commonValuePaths != null)
		{
			for (Path path : commonValuePaths)
			{
				PathUtils.setupFieldNode(context, path, "commonValue", false);
			}
		}
		SchemaNode sortOrderNode = PathUtils
			.setupFieldNode(context, sortOrderPath, "sortOrderPath", false);
		SchemaTypeName sortOrderType = sortOrderNode.getXsTypeName();
		if (!(SchemaTypeName.XS_INT.equals(sortOrderType)
			|| SchemaTypeName.XS_INTEGER.equals(sortOrderType)))
		{
			context.addError("Node " + sortOrderPath.format() + " must be integer type");
		}
	}

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		if (!context.isDuplicatingOccurrence())
		{
			ValueContextForUpdate recordContext = context.getOccurrenceContextForUpdate();
			if (recordContext.getValue(sortOrderPath) == null) // when importing, allow the existing
																// value to be retained
			{
				List<Adaptation> otherProperties = AdaptationUtil
					.fetchRecordsMatching(recordContext, commonValuePaths, null);
				if (otherProperties == null)
					return;
				recordContext.setValueEnablingPrivilegeForNode(
					otherProperties.size() + (startAtOne ? 1 : 0),
					sortOrderPath);
			}
		}
		super.handleNewContext(context);
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);
		ValueContextForUpdate valueContext = context.getOccurrenceContextForUpdate();
		if (valueContext.getValue(sortOrderPath) == null)
		{
			valueContext.setValueEnablingPrivilegeForNode(Integer.MAX_VALUE, sortOrderPath);
		}
	}

	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		super.handleAfterCreate(context);
		ValueContext recordContext = context.getOccurrenceContext();
		List<Adaptation> otherProperties = getOtherProperties(recordContext);
		if (otherProperties == null)
			return;
		ProcedureContext pContext = context.getProcedureContext();
		int max = otherProperties.size() - (startAtOne ? 0 : 1);
		Integer _val = (Integer) recordContext.getValue(sortOrderPath);
		if (_val == Integer.valueOf(max))
		{
			return;
		}
		int value = _val == null ? max : _val;
		Map<Path, Object> map = new HashMap<>(1);
		// if newValue < oldValue, will need to increment subsequent records
		// if oldValue < newValue, will need to decrement subsequent records
		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		mvp.setAllPrivileges(true);
		mvp.setTriggerActivation(false);
		mvp.setPathValueMap(map);
		if (value >= max)
		{
			map.put(sortOrderPath, max);
			mvp.setAdaptation(context.getAdaptationOccurrence());
			mvp.execute(pContext);
			return;
		}
		for (Adaptation adaptation : otherProperties)
		{
			if (adaptation.equals(context.getAdaptationOccurrence()))
			{
				continue;
			}
			int order = adaptation.get_int(sortOrderPath);
			if (order >= value)
			{
				// if this other record's sequenceNumber is the same as the trigger record's
				// sequence number, then increment this record's sequence number
				// otherwise, use index within the other properties collection in case the current
				// sequence values have gaps
				if (order == value)
				{
					order++;
				}
				else
				{
					order = otherProperties.indexOf(adaptation) + (startAtOne ? 1 : 0);
				}
				map.put(sortOrderPath, order);
				mvp.setAdaptation(adaptation);
				mvp.execute(pContext);
			}
		}
	}

	@Override
	public void handleAfterDelete(AfterDeleteOccurrenceContext context) throws OperationException
	{
		ValueContext recordContext = context.getOccurrenceContext();
		List<Adaptation> otherProperties = getOtherProperties(recordContext);
		if (otherProperties == null)
			return;
		ProcedureContext pContext = context.getProcedureContext();
		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		mvp.setAllPrivileges(true);
		mvp.setTriggerActivation(false);
		int delta = startAtOne ? 1 : 0;
		for (int i = 0; i < otherProperties.size(); i++)
		{
			Adaptation next = otherProperties.get(i);
			int order = next.get_int(sortOrderPath);
			if (order != (i + delta))
			{
				mvp.setValue(sortOrderPath, i + delta);
				mvp.setAdaptation(next);
				mvp.execute(pContext);
			}
		}
		super.handleAfterDelete(context);
	}

	@Override
	public void handleAfterModify(AfterModifyOccurrenceContext context) throws OperationException
	{
		// if you change the index, update the other indices accordingly
		ValueChanges changes = context.getChanges();
		ValueChange indexChange = changes.getChange(sortOrderPath);
		if (indexChange != null && indexChange.getValueAfter() != null)
		{
			int newValue = (Integer) indexChange.getValueAfter();
			ValueContext recordContext = context.getOccurrenceContext();
			Adaptation record = context.getAdaptationOccurrence();
			List<Adaptation> otherProperties = getOtherProperties(recordContext);
			if (otherProperties == null)
				return;
			ProcedureContext pContext = context.getProcedureContext();
			ModifyValuesProcedure mvp = new ModifyValuesProcedure();
			mvp.setAllPrivileges(true);
			mvp.setTriggerActivation(false);
			int delta = startAtOne ? 1 : 0;
			otherProperties.remove(record);
			if (newValue - delta > otherProperties.size())
				otherProperties.add(record);
			else
				otherProperties.add(newValue - delta, record);
			for (int i = 0; i < otherProperties.size(); i++)
			{
				Adaptation next = otherProperties.get(i);
				int order = Objects.equals(next, record) ? newValue : next.get_int(sortOrderPath);
				if (order != (i + delta))
				{
					mvp.setValue(sortOrderPath, i + delta);
					mvp.setAdaptation(next);
					mvp.execute(pContext);
				}
			}
		}
		super.handleAfterModify(context);
	}

	protected List<Adaptation> getOtherProperties(ValueContext recordContext)
	{
		return AdaptationUtil
			.fetchRecordsMatching(recordContext, commonValuePaths, null, sortOrderPath);
	}

	public String getCommonValuePathsString()
	{
		return commonValuePathsString;
	}

	public void setCommonValuePathsString(String commonValuePathsString)
	{
		this.commonValuePathsString = commonValuePathsString;
	}

	public Path getSortOrderPath()
	{
		return sortOrderPath;
	}

	public void setSortOrderPath(Path sortOrderPath)
	{
		this.sortOrderPath = sortOrderPath;
	}

	public boolean isStartAtOne()
	{
		return startAtOne;
	}

	public void setStartAtOne(boolean startAtOne)
	{
		this.startAtOne = startAtOne;
	}

}
