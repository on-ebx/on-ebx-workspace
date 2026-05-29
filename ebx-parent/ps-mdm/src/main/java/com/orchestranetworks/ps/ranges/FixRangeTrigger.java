package com.orchestranetworks.ps.ranges;

import java.math.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.deepcopy.*;
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
 * Used in conjunction with <code>RangesConstraint</code>, this trigger will fix adjacent ranges in the range set.
 * If creating a new range, it will default the min bound to the maximum max bound in the range set.
 * When modifying a range, it will 'fix' the adjacent ranges accordingly.
 * See <a href="https://docs.google.com/document/d/1I-zS_AJIoPGgEdt-BC6g-7HNEj34Y9QD16mw_R-P7bw/edit#">Range Support</a>
 * for more information.
 */
public class FixRangeTrigger extends TableTrigger
{
	private static final String DELETED_RANGE = "FixRange_DeletedRange";

	private RangeConfig config = new RangeConfig();

	public Path getCommonValuePath()
	{
		return config.getCommonValuePath();
	}

	public void setCommonValuePath(Path commonValuePath)
	{
		config.setCommonValuePath(commonValuePath);
	}

	public String getCommonValuePathsString()
	{
		return config.getCommonValuePathsString();
	}

	public void setCommonValuePathsString(String commonValuePathsString)
	{
		config.setCommonValuePathsString(commonValuePathsString);
	}

	public Path getMinValuePath()
	{
		return config.getMinValuePath();
	}

	public void setMinValuePath(Path minValuePath)
	{
		config.setMinValuePath(minValuePath);
	}

	public Path getMaxValuePath()
	{
		return config.getMaxValuePath();
	}

	public void setMaxValuePath(Path maxValuePath)
	{
		config.setMaxValuePath(maxValuePath);
	}

	public boolean isMinInclusive()
	{
		return config.isMinInclusive();
	}

	public void setMinInclusive(boolean minInclusive)
	{
		config.setMinInclusive(minInclusive);
	}

	public boolean isMaxInclusive()
	{
		return config.isMaxInclusive();
	}

	public void setMaxInclusive(boolean maxInclusive)
	{
		config.setMaxInclusive(maxInclusive);
	}

	public boolean isCheckOverlaps()
	{
		return config.isCheckOverlaps();
	}

	public void setCheckOverlaps(boolean checkOverlaps)
	{
		config.setCheckOverlaps(checkOverlaps);
	}

	public boolean isCheckGaps()
	{
		return config.isCheckGaps();
	}

	public void setCheckGaps(boolean checkGaps)
	{
		config.setCheckGaps(checkGaps);
	}

	public boolean isUsePadding()
	{
		return config.isUsePadding();
	}

	public void setUsePadding(boolean usePadding)
	{
		config.setUsePadding(usePadding);
	}

	public String getPadding()
	{
		return config.getPadding();
	}

	public void setPadding(String padding)
	{
		config.setPadding(padding);
	}

	public boolean isSortOnMax()
	{
		return config.isSortOnMax();
	}

	public void setSortOnMax(boolean sortOnMax)
	{
		config.setSortOnMax(sortOnMax);
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		config.setup(context);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		super.handleAfterCreate(context);
		if (!shouldDeal())
			return;
		ValueContext recordContext = context.getOccurrenceContext();
		dealWithRecord(
			recordContext,
			context.getAdaptationOccurrence(),
			true,
			null,
			(Comparable) recordContext.getValue(getMinValuePath()),
			true,
			null,
			(Comparable) recordContext.getValue(getMaxValuePath()),
			context.getProcedureContext());
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);
		ValueContextForUpdate recordContext = context.getOccurrenceContextForUpdate();
		setNextMinValue(recordContext);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setNextMinValue(ValueContextForUpdate recordContext)
	{
		// want to set the min value on the new record to the max existing maxValue
		List<Adaptation> ranges = AdaptationUtil
			.fetchRecordsMatching(recordContext, config.commonValuePaths, null);
		if (ranges == null)
			return;
		RangeUtils.sortRanges(ranges, config);
		Adaptation maxRecord = null;
		Path maxValuePath = getMaxValuePath();
		Comparable maxValue = (Comparable) recordContext.getValue(maxValuePath);
		Object globalMaxValue = null;
		if (!ranges.isEmpty())
		{
			maxRecord = ranges.get(ranges.size() - 1);
			if (maxRecord != null)
				globalMaxValue = maxRecord.get(maxValuePath);
			if (maxValue != null)
			{
				Adaptation lastRange = null;
				for (Adaptation adaptation : ranges)
				{
					Comparable localMaxValue = (Comparable) adaptation.get(maxValuePath);

					if (localMaxValue == null || maxValue.compareTo(localMaxValue) > 0)
						lastRange = adaptation;
					else
						break;
				}
				maxRecord = lastRange;
			}
		}
		if (maxRecord != null)
		{
			Path minValuePath = getMinValuePath();
			Object minVal = recordContext.getValue(minValuePath);
			if (minVal == null
				|| Objects.equals(recordContext.getNode(minValuePath).getDefaultValue(), minVal)
				|| minVal.equals(RangeUtils.padMax((Comparable) globalMaxValue, config.paddingNum)))
			{
				maxValue = (Comparable) maxRecord.get(maxValuePath);
				recordContext.setValueEnablingPrivilegeForNode(
					RangeUtils.padMax(maxValue, config.paddingNum),
					getMinValuePath());
			}
		}
	}

	@Override
	public void handleNewContext(NewTransientOccurrenceContext context)
	{
		super.handleNewContext(context);
		Boolean deepCopy = (Boolean) context.getSession()
			.getAttribute(DeepCopyImpl.DEEP_COPY_SESSION_ATTRIBUTE);
		if (!Boolean.TRUE.equals(deepCopy))
		{
			ValueContextForUpdate recordContext = context.getOccurrenceContextForUpdate();
			if (context.isDuplicatingOccurrence())
				recordContext.setValueEnablingPrivilegeForNode(null, getMaxValuePath()); // in the case of a duplicate, will
			// want the default
			setNextMinValue(recordContext);
		}
	}

	@Override
	public void handleBeforeDelete(BeforeDeleteOccurrenceContext aContext) throws OperationException
	{
		super.handleBeforeDelete(aContext);
		// if checking gaps, when we delete a record, we would like to close the gap created
		if (shouldDeal() && isCheckGaps())
		{
			aContext.getSession()
				.setAttribute(DELETED_RANGE + this, aContext.getAdaptationOccurrence());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void handleAfterDelete(AfterDeleteOccurrenceContext aContext) throws OperationException
	{
		super.handleAfterDelete(aContext);
		boolean checkGaps = isCheckGaps();
		if (shouldDeal() && checkGaps)
		{
			Adaptation existingRecord = (Adaptation) aContext.getSession()
				.getAttribute(DELETED_RANGE + this);
			if (existingRecord == null)
				return;
			aContext.getSession().setAttribute(DELETED_RANGE + this, null);
			ValueContext recordContext = aContext.getOccurrenceContext();
			List<Adaptation> ranges = AdaptationUtil
				.fetchRecordsMatching(recordContext, config.commonValuePaths, null);
			if (ranges == null)
				return;
			ranges.add(existingRecord);
			Path minValuePath = getMinValuePath();
			Path maxValuePath = getMaxValuePath();
			Comparable minValue = (Comparable) recordContext.getValue(minValuePath);
			RangeUtils.sortRanges(
				ranges,
				RangeUtils.createGetValue(existingRecord, minValue, minValuePath),
				null,
				false);
			int indexOfCurr = ranges.indexOf(existingRecord);
			Adaptation predecessor = indexOfCurr > 0 ? (Adaptation) ranges.get(indexOfCurr - 1)
				: null;
			Adaptation successor = indexOfCurr < ranges.size() - 1
				? (Adaptation) ranges.get(indexOfCurr + 1)
				: null;
			if (predecessor != null && successor != null)
			{
				// we can set the maxValue of the predecessor to the deleted record's maxValue
				ModifyValuesProcedure mvp = new ModifyValuesProcedure();
				if (isSortOnMax())
				{
					mvp.setValue(maxValuePath, recordContext.getValue(maxValuePath));
					mvp.setAdaptation(predecessor);
				}
				else
				{
					mvp.setValue(minValuePath, recordContext.getValue(minValuePath));
					mvp.setAdaptation(successor);
				}
				mvp.setAllPrivileges(true);
				mvp.execute(aContext.getProcedureContext());
			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void dealWithRecord(
		ValueContext recordContext,
		Adaptation existingRecord,
		boolean minChanged,
		Comparable prevMin,
		Comparable minValue,
		boolean maxChanged,
		Comparable prevMax,
		Comparable maxValue,
		ProcedureContext pContext)
		throws OperationException
	{
		// gap check only works with [)/(] ranges -- we assume minInclusive != maxInclusive
		if (existingRecord == null)
		{
			// do nothing. This trigger is for fixing existing sets of ranges
			return;
		}
		List<Adaptation> ranges = AdaptationUtil
			.fetchRecordsMatching(recordContext, config.commonValuePaths, null);
		if (ranges == null)
			return;
		Path minValuePath = getMinValuePath();
		Path maxValuePath = getMaxValuePath();
		boolean checkGaps = isCheckGaps();
		boolean checkOverlaps = isCheckOverlaps();
		BigDecimal paddingNum = config.paddingNum;
		if (minValue == null)
			minValue = (Comparable) recordContext.getValue(minValuePath);
		if (maxValue == null)
			maxValue = (Comparable) recordContext.getValue(maxValuePath);
		if (isSortOnMax())
		{
			RangeUtils.sortRanges(
				ranges,
				null,
				RangeUtils.createGetValue(existingRecord, maxValue, maxValuePath),
				true);
		}
		else
		{
			RangeUtils.sortRanges(
				ranges,
				RangeUtils.createGetValue(existingRecord, minValue, minValuePath),
				null,
				false);
		}
		int indexOfCurr = ranges.indexOf(existingRecord);
		Adaptation predecessor = indexOfCurr > 0 ? (Adaptation) ranges.get(indexOfCurr - 1) : null;
		Adaptation successor = indexOfCurr < ranges.size() - 1
			? (Adaptation) ranges.get(indexOfCurr + 1)
			: null;
		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		mvp.setAllPrivileges(true);
		mvp.setTriggerActivation(false);
		if (minChanged)
		{
			if (predecessor != null) // fix overlap/gap that may have been created with predecessor
			{
				boolean needFix = false;
				int minChange = compareMin(prevMin, minValue);
				Comparable pmax = (Comparable) predecessor.get(maxValuePath);
				if (prevMin == null || (minChange < 0 && checkGaps)
					|| (minChange > 0 && checkOverlaps))
				{
					// see if predecessor needs to be fixed
					int compare = compareMinMax(RangeUtils.padMin(minValue, paddingNum), pmax);
					if (checkGaps && pmax != null && compare > 0)
						needFix = true;
					if (checkOverlaps && (pmax == null || compare < 0))
						needFix = true;
				}
				if (needFix)
				{
					if (minValue == null)
					{
						mvp.setValue(minValuePath, RangeUtils.padMax(pmax, paddingNum));
						mvp.setAdaptation(existingRecord);
					}
					else
					{
						mvp.setValue(maxValuePath, RangeUtils.padMin(minValue, paddingNum));
						mvp.setAdaptation(predecessor);
					}
					mvp.execute(pContext);
				}
			}
			else if (successor != null)
			{ // the change made the existing record the first range -- may need to adjust the
				// successor
				// see if successor has problem
				boolean needFix = false;
				Comparable smin = (Comparable) successor.get(minValuePath);
				int compare = compareMinMax(smin, RangeUtils.padMax(maxValue, paddingNum));
				if (checkGaps && smin != null && compare > 0)
					needFix = true;
				if (checkOverlaps && (smin == null || compare < 0))
					needFix = true;
				if (needFix)
				{
					if (!maxChanged || maxValue == null)
					{
						mvp.setValue(maxValuePath, RangeUtils.padMin(smin, paddingNum));
						mvp.setAdaptation(existingRecord);
					}
					else
					{
						mvp.setValue(minValuePath, RangeUtils.padMax(maxValue, paddingNum));
						mvp.setAdaptation(successor);
					}
					mvp.execute(pContext);
				}
			}
		}
		mvp.clearValues();
		if (maxChanged)
		{
			if (successor != null) // fix overlap/gap that may have resulted with the successor
			{
				boolean needFix = false;
				int maxChange = compareMax(prevMax, maxValue);
				Comparable smin = (Comparable) successor.get(minValuePath);
				if (prevMax == null || (maxChange > 0 && checkGaps)
					|| (maxChange < 0 && checkOverlaps))
				{
					int compare = compareMinMax(smin, RangeUtils.padMax(maxValue, paddingNum));
					if (checkGaps && smin != null && compare > 0)
						needFix = true;
					if (checkOverlaps && (smin == null || compare < 0))
						needFix = true;
				}
				if (needFix)
				{
					if (maxValue == null)
					{
						mvp.setValue(maxValuePath, RangeUtils.padMin(smin, paddingNum));
						mvp.setAdaptation(existingRecord);
					}
					else
					{
						mvp.setValue(minValuePath, RangeUtils.padMax(maxValue, paddingNum));
						mvp.setAdaptation(successor);
					}
					mvp.execute(pContext);
				}
			}
			else if (predecessor != null)
			{ // change made existing record last -- may need to adjust its predecessor
				// see if predecessor has problem
				boolean needFix = false;
				Comparable pmax = (Comparable) predecessor.get(maxValuePath);
				int compare = compareMinMax(RangeUtils.padMin(minValue, paddingNum), pmax);
				if (checkGaps && pmax != null && compare > 0)
					needFix = true;
				if (checkOverlaps && (pmax == null || compare < 0))
					needFix = true;
				if (needFix)
				{
					if (!minChanged || minValue == null)
					{
						mvp.setValue(minValuePath, RangeUtils.padMax(pmax, paddingNum));
						mvp.setAdaptation(existingRecord);
					}
					else
					{
						mvp.setValue(maxValuePath, RangeUtils.padMin(minValue, paddingNum));
						mvp.setAdaptation(predecessor);
					}
					mvp.execute(pContext);
				}
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareMinMax(Comparable min, Comparable max)
	{
		int result = 0;
		if (min == null)
			result = -1;
		else if (max == null)
			result = 1;
		else
			result = min.compareTo(max);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareMin(Comparable aComp, Comparable bComp)
	{
		int result = 0;
		if (aComp == null)
			result = bComp == null ? 0 : -1;
		else if (bComp == null)
			result = 1;
		else
			result = aComp.compareTo(bComp);
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareMax(Comparable aComp, Comparable bComp)
	{
		int result = 0;
		if (aComp == null)
			result = bComp == null ? 0 : -1;
		else if (bComp == null)
			result = 1;
		else
			result = aComp.compareTo(bComp);
		return result;
	}

	private boolean shouldDeal()
	{
		Path minValuePath = getMinValuePath();
		Path maxValuePath = getMaxValuePath();
		boolean checkGaps = isCheckGaps();
		boolean checkOverlaps = isCheckOverlaps();
		if (!checkGaps && !checkOverlaps)
			return false; // nothing to do
		if ((config.commonValuePaths != null && config.commonValuePaths.contains(null))
			|| minValuePath == null || maxValuePath == null)
		{
			return false;
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void handleAfterModify(AfterModifyOccurrenceContext aContext) throws OperationException
	{
		super.handleAfterModify(aContext);
		if (!shouldDeal())
			return;
		ValueChanges changes = aContext.getChanges();
		Comparable prevMin = null, minValue = null, prevMax = null, maxValue = null;
		Path minValuePath = getMinValuePath();
		Path maxValuePath = getMaxValuePath();
		ValueChange minChange = changes.getChange(minValuePath);
		if (minChange != null)
		{
			prevMin = (Comparable) minChange.getValueBefore();
			minValue = (Comparable) minChange.getValueAfter();
		}
		ValueChange maxChange = changes.getChange(maxValuePath);
		if (maxChange != null)
		{
			prevMax = (Comparable) maxChange.getValueBefore();
			maxValue = (Comparable) maxChange.getValueAfter();
		}
		dealWithRecord(
			aContext.getOccurrenceContext(),
			aContext.getAdaptationOccurrence(),
			minChange != null,
			prevMin,
			minValue,
			maxChange != null,
			prevMax,
			maxValue,
			aContext.getProcedureContext());
	}

}
