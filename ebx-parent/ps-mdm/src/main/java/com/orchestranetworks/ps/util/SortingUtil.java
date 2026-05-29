package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * This class is a utility for dealing with fields for sort order within a group of records.
 * There are two flavors to sorting records based on a sort field.  One in which the sort field is
 * user writable (think sorting within a rectangular list) and another, where the sort field is hidden
 * and usually maintained by the 'move to position' action in a hierarchy view. 
 */
public class SortingUtil
{
	public static final int DEFAULT_GAP = 32;
	private final Path sortField;
	private final List<Adaptation> records;
	private final boolean sortFieldHidden;
	private final Comparator<Adaptation> compareBySortField = new Comparator<Adaptation>()
	{
		@Override
		public int compare(Adaptation o1, Adaptation o2)
		{
			int i1 = o1.get_int(sortField);
			int i2 = o2.get_int(sortField);
			return i1 - i2;
		}
	};

	/**
	 * Construct an instance of this sorting utility with a list of records (may be initially unsorted),
	 * a sortField and whether that sort field is user visible.  User visible sort fields are a bit less efficient
	 * to maintain as when you change the order, all the records' sort field will need to be updated.  Hidden
	 * sort fields can use a spread technique so that inserting in the best case scenario requires only one update.
	 * 
	 * @param records list records whose order needs to be maintained
	 * @param sortField a field on the record used to sort -- must be writable
	 * @param sortFieldHidden whether or not this field is maintained directly by users
	 */
	public SortingUtil(List<Adaptation> records, Path sortField, boolean sortFieldHidden)
	{
		this.records = records;
		this.sortField = sortField;
		this.sortFieldHidden = sortFieldHidden;
		Collections.sort(records, compareBySortField); //make sure records are initially in order
	}

	/**
	 * When you want to change the order by inserting record1 just before record2 in the list
	 * @param pContext procedure context in which to perform updates
	 * @param record1 the record to be moved to just before the record2
	 * @param record2 the record to come next to record1
	 */
	public void insert(ProcedureContext pContext, Adaptation record1, Adaptation record2)
		throws OperationException
	{
		int indexOf1 = records.indexOf(record1);
		int indexOf2 = records.indexOf(record2);
		if (indexOf1 == indexOf2 - 1)
			return; //nothing to do
		if (indexOf1 == -1 || indexOf2 == -1)
		{
			//not found
			return;
		}
		int[] values = new int[records.size()];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = (Integer) pContext.getContext(records.get(i).getAdaptationName())
				.getValue(sortField);
		}
		int[] newValues = new int[records.size()];
		for (int i = 0; i < newValues.length; i++)
		{
			newValues[i] = -1;
		}
		if (sortFieldHidden)
		{
			int valueOf2 = values[indexOf2];
			int prevValue = 0;
			if (indexOf2 > 0)
				prevValue = values[indexOf2 - 1];
			if (valueOf2 - prevValue > 2)
				newValues[indexOf1] = ((valueOf2 - prevValue) / 2) + prevValue;
			else
			{
				//may need to massage the gaps
				//by default we make the gap 32 (enabling 5 inserts)
				newValues[indexOf2] = prevValue + (DEFAULT_GAP * 2);
				newValues[indexOf1] = prevValue + DEFAULT_GAP;
				int nextIndex = indexOf2 + 1;
				int currValue = newValues[indexOf2];
				while (true)
				{
					if (nextIndex == newValues.length)
						break;
					if (values[nextIndex] - currValue > 2)
						break;
					currValue += DEFAULT_GAP;
					newValues[nextIndex] = currValue;
					nextIndex++;
				}
			}
		}
		else
		{
			//if the field is user managed, reset all impacted values
			newValues[indexOf1] = values[indexOf2];
			if (values[indexOf1] < values[indexOf2])
			{
				for (int i = indexOf1 + 1; i < indexOf2; i++)
				{
					newValues[i] = values[i] - 1;
				}
			}
			else
			{
				for (int i = indexOf2 + 1; i < indexOf1; i++)
				{
					newValues[i] = values[i] + 1;
				}
			}
		}
		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		for (int i = 0; i < newValues.length; i++)
		{
			int newVal = newValues[i];
			if (newVal != -1)
			{
				Adaptation record = records.get(i);
				mvp.setAdaptation(record);
				mvp.setValue(sortField, newVal);
				mvp.setAllPrivileges(true);
				mvp.execute(pContext);
			}
		}
		records.remove(indexOf1);
		int newIndex = indexOf1 < indexOf2 ? indexOf2 - 1 : indexOf2;
		records.add(newIndex, record1); //make sure records represent the right order
	}

	public void sortWithComparator(ProcedureContext pContext, Comparator<Adaptation> comparator)
		throws OperationException
	{
		List<Adaptation> temp = new ArrayList<>(records);
		Collections.sort(temp, comparator);
		if (temp.equals(records)) //already in the right order
			return;

		ModifyValuesProcedure mvp = new ModifyValuesProcedure();
		int lastValue = -1;
		for (int i = 0; i < temp.size(); i++)
		{
			Adaptation record = temp.get(i);
			mvp.setAdaptation(record);
			int index = records.get(i).get_int(sortField);
			if (index <= lastValue)
				index = lastValue + (sortFieldHidden ? 32 : 1);
			lastValue = index;
			mvp.setValue(sortField, index);
			mvp.setAllPrivileges(true);
			mvp.execute(pContext);
		}
		records.clear();
		records.addAll(temp);
	}

	/**
	 * When creating a new record, what's the next index that should be used?
	 * @return the next index.  When using a visible sort field, this will be the number of
	 * records plus the first index.  When using a hidden field, this would not ordinarily be used
	 * as the trigger would take care of it?  But it's here in case you need to write that trigger...
	 * @return the next sortField value to use for a new record in the collection
	 */
	public int getNextIndex()
	{
		if (sortFieldHidden)
			return records.get(records.size() - 1).get_int(sortField) + DEFAULT_GAP;
		else
			return records.size() + records.get(0).get_int(sortField);
	}
}
