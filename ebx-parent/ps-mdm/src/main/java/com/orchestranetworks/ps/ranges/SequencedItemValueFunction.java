package com.orchestranetworks.ps.ranges;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

public class SequencedItemValueFunction implements ValueFunction
{
	//next is true if you're looking for the next in the sequence; otherwise looking for prior
	private boolean next;
	//field used to sort items in the same table
	private RangeConfig config = new RangeConfig(false);
	//additional field (optional) to be returned from the next/previous item found
	private Path attribute;

	public String getCommonValuePathsString()
	{
		return getConfig().getCommonValuePathsString();
	}

	public void setCommonValuePathsString(String commonValuePathsString)
	{
		getConfig().setCommonValuePathsString(commonValuePathsString);
	}

	public Path getSortFieldPath()
	{
		return getConfig().getMinValuePath();
	}

	public void setSortFieldPath(Path minValuePath)
	{
		getConfig().setMinValuePath(minValuePath);
	}

	public boolean isNext()
	{
		return next;
	}

	public void setNext(boolean next)
	{
		this.next = next;
	}

	public Path getAttribute()
	{
		return attribute;
	}

	public String getPadding()
	{
		return getConfig().getPadding();
	}

	public void setPadding(String padding)
	{
		getConfig().setPadding(padding);
	}

	public boolean isUsePadding()
	{
		return getConfig().isUsePadding();
	}

	public void setUsePadding(boolean usePadding)
	{
		getConfig().setUsePadding(usePadding);
	}

	public void setAttribute(Path attribute)
	{
		this.attribute = attribute;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getValue(Adaptation arg0)
	{
		List<Path> commPaths = getConfig().getCommonValuePaths();
		List<Adaptation> siblings = commPaths != null ? AdaptationUtil.fetchRecordsMatching(
			arg0.createValueContext(),
			getConfig().getCommonValuePaths(),
			null) : AdaptationUtil.getRecords(arg0.getContainerTable().createRequest().execute());
		if (siblings == null)
			return null;
		RangeUtils.sortRanges(siblings, RangeUtils.createGetValue(
			arg0,
			(Comparable) arg0.get(getSortFieldPath()),
			getSortFieldPath()));
		int index = siblings.indexOf(arg0);
		int nextRecordIndex = next ? index + 1 : index - 1;
		if (nextRecordIndex < 0 || nextRecordIndex >= siblings.size())
			return null;
		Adaptation nextRecord = siblings.get(nextRecordIndex);
		if (attribute != null)
		{
			Object value = nextRecord.get(attribute);
			if (attribute.equals(getSortFieldPath()) && isUsePadding())
			{
				Comparable cv = (Comparable) value;
				if (next)
					return RangeUtils.padMin(cv, getConfig().getPaddingNum());
				else
					return RangeUtils.padMax(cv, getConfig().getPaddingNum());
			}
			return value;
		}
		return nextRecord.getOccurrencePrimaryKey().format();
	}
	@Override
	public void setup(ValueFunctionContext arg0)
	{
		getConfig().setup(arg0);
		if (attribute != null)
			PathUtils.setupFieldNode(arg0, attribute, "attributePath", false);
	}

	public RangeConfig getConfig()
	{
		return config;
	}

	public void setConfig(RangeConfig config)
	{
		this.config = config;
	}

}
