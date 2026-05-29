package com.orchestranetworks.ps.util;

import java.io.*;
import java.math.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.schema.definition.*;
import com.onwbp.base.schema.instance.*;
import com.onwbp.base.text.*;
import com.onwbp.boot.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.schema.relationships.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.workflow.impl.execution.*;

/**
 * A class comprised of static utility methods for dealing with Adaptations.  Most of the methods
 * act on a record <code>Adaptation</code> or a record <code>ValueContext</code>.
 */
public class AdaptationUtil
{
	/**
	 * Given a record <code>Adaptation</code> and a <code>Locale</code>, this function will produce
	 * a string representation
	 * of the record.
	 */
	public static final BinaryFunction<Adaptation, Locale, String> GetLabel = new BinaryFunction<Adaptation, Locale, String>()
	{
		@Override
		public String evaluate(Adaptation object, Locale locale)
		{
			return object == null ? "" : object.getLabel(locale);
		}
	};

	/**
	 * Given a record <code>Adaptation</code> and a <code>Locale</code>, this function will produce
	 * a string representation
	 * of the record.
	 */
	public static final BinaryFunction<ValueContext, Locale, String> GetLabelVC = new BinaryFunction<ValueContext, Locale, String>()
	{
		@Override
		public String evaluate(ValueContext object, Locale locale)
		{
			Adaptation record = getRecordForValueContext(object);
			return record == null ? "current record" : record.getLabel(locale);
		}
	};

	/**
	 * Given a record <code>Adaptation</code>, this function will produce a string representation
	 * of the record using the default locale.
	 */
	public static final UnaryFunction<Adaptation, String> GetDefaultLabel = GetLabel
		.bindRight(Locale.getDefault());

	/**
	 * Given a <code>SchemaNode</code> and a <code>Locale</code>, this function will produce a
	 * string representation
	 * of the record.
	 */
	public static final BinaryFunction<SchemaNode, Locale, String> GetNodeLabel = new BinaryFunction<SchemaNode, Locale, String>()
	{
		@Override
		public String evaluate(SchemaNode object, Locale locale)
		{
			return object == null ? "null" : object.getLabel(locale);
		}
	};
	public static final UnaryFunction<SchemaNode, String> GetNodeDefaultLabel = GetNodeLabel
		.bindRight(Locale.getDefault());

	/**
	 * Function to replace ${path} occurrences in an xpath predicate for a particular instance. If
	 * the
	 * path points to an instance the formatted primary key is used.
	 * TODO: compare with {@link #computeValuedPredicate(String, ValueContextForValidation)}
	 * for overlapping functionality.
	 */
	public static String calculateLocalValues(final ValueContext vc, final String predicate)
	{
		String parsed = "";
		int lastCopied = 0;
		final int len = predicate.length();
		for (int i = 0; i < len; i++)
		{
			// Look for start of substitution
			if (predicate.charAt(i) != '$')
			{
				continue;
			}
			if (i == len || predicate.charAt(i + 1) != '{')
			{
				continue;
			}
			parsed = parsed.concat(predicate.substring(lastCopied, i));
			i = i + 2;
			final int k = i;
			for (; i < len && predicate.charAt(i) != '}';)
			{
				i++;
			}
			lastCopied = i;

			// Determine substitute
			try
			{
				final String subst = predicate.substring(k, i);
				final Path p = Path.parse(subst);
				final Object o = vc.getValue(p);
				if (o == null)
				{
					return null;
				}
				if (o instanceof Adaptation)
				{
					parsed = parsed + "'" + ((Adaptation) o).getOccurrencePrimaryKey().format()
						+ "'";
				}
				else
				{
					parsed = parsed + "'" + o.toString() + "'";
				}
			}
			catch (final Exception e)
			{
				// log error
				VM.log.kernelWarn(
					"Exception determining contextual xPath predicate: " + e.getLocalizedMessage());
				return null;
			}
		}
		return parsed;
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing an association/select node
	 * field
	 * of that record, return the <code>RequestResult</code> that can be used to iterate over the
	 * related records.
	 * 
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @return RequestResult iterator for the related records
	 */
	public static RequestResult linkedRecordLookup(final Adaptation record, final Path path)
	{
		if (record == null || path == null)
		{
			return null;
		}
		final SchemaNode node = record.getSchemaNode().getNode(path);
		if (node == null)
			return null;
		if (node.isAssociationNode())
		{
			return node.getAssociationLink().getAssociationResult(record);
		}
		if (node.isSelectNode())
		{
			return node.getSelectionLink().getSelectionResult(record);
		}
		return null;
	}

	/**
	 * Deletes a list of records from a selection node or association
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @throws OperationException
	 */
	@SuppressWarnings("unchecked")
	public static void deleteLinkedRecordList(
		Adaptation record,
		Path path,
		ProcedureContext pContext)
		throws OperationException
	{
		List<Adaptation> records = (List<Adaptation>) getLinkedRecordList(record, path, null);
		DeleteRecordProcedure drp = new DeleteRecordProcedure();
		for (Adaptation childRecord : records)
		{
			drp.setAdaptation(childRecord);
			drp.setAllPrivileges(true);
			drp.execute(pContext);
		}
	}

	/**
	 * Get a list of records from a selection node or association
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @return the list of records, or an empty list
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> getLinkedRecordList(Adaptation record, Path path)
	{
		return (List<Adaptation>) getLinkedRecordList(record, path, null);
	}

	/**
	 * Get a list of values from a selection node or association by following a path within that
	 * table,
	 * or a list of records if no attribute path is specified
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @param attributePath
	 *            the path of the attribute on the selection node or association table
	 * @return the list of values or records, or an empty list
	 */
	public static List<?> getLinkedRecordList(Adaptation record, Path path, Path attributePath)
	{
		return getLinkedRecordList(record, path, attributePath, true, null, true);
	}

	/**
	 * Get a list of values from a selection node or association by following a path within that
	 * table,
	 * or a list of records if no attribute path is specified
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @param attributePath
	 *            the path of the attribute on the selection node or association table
	 * @param distinct
	 *            exclude duplicates
	 * @param sortField
	 *            on which to sort the related records
	 * @param ascending
	 *            sort type
	 * @return the list of values or records, or an empty list
	 */
	public static List<?> getLinkedRecordList(
		Adaptation record,
		Path path,
		Path attributePath,
		boolean distinct,
		Path sortField,
		boolean ascending)
	{
		return getLinkedRecordList(
			record,
			path,
			attributePath,
			distinct,
			sortField,
			ascending,
			true);
	}

	/**
	 * Get a list of values from a selection node or association by following a path within that
	 * table,
	 * or a list of records if no attribute path is specified
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @param attributePath
	 *            the path of the attribute on the selection node or association table
	 * @param distinct
	 *            exclude duplicates
	 * @param sortField
	 *            on which to sort the related records
	 * @param ascending
	 *            sort type
	 * @param exludeNulls
	 *            when attributePath is specified, this will exclude nulls from the list when true
	 * @return the list of values or records, or an empty list
	 */
	public static List<?> getLinkedRecordList(
		Adaptation record,
		Path path,
		Path attributePath,
		boolean distinct,
		Path sortField,
		boolean ascending,
		boolean excludeNulls)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		try
		{
			if (reqRes == null)
			{
				return new ArrayList<>();
			}
			Collection<Object> list = distinct ? new LinkedHashSet<>() : new ArrayList<>();
			if (sortField != null)
			{
				List<Adaptation> linkedRecords = getRecords(reqRes);
				Collections.sort(linkedRecords, createComparator(sortField));
				if (!ascending)
					Collections.reverse(linkedRecords);
				if (attributePath == null)
				{
					list.addAll(linkedRecords);
				}
				else
				{
					for (Adaptation adaptation : linkedRecords)
					{
						Object value = adaptation.get(attributePath);
						if (value != null || !excludeNulls)
							list.add(value);
					}
				}
			}
			else
			{
				Adaptation next;
				while ((next = reqRes.nextAdaptation()) != null)
				{
					if (attributePath == null)
					{
						list.add(next);
					}
					else
					{
						Object value = next.get(attributePath);
						if (value != null || !excludeNulls)
							list.add(value);
					}
				}
			}
			return distinct ? new ArrayList<>(list) : (List<?>) list;
		}
		finally
		{
			if (reqRes != null)
			{
				reqRes.close();
			}
		}
	}

	/**
	 * Get a list of primary keys as strings from a selection node or association by following a
	 * path within that table,
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @return the list of values, or an empty list
	 */
	public static List<String> getLinkedRecordKeyList(Adaptation record, Path path)
	{
		return getLinkedRecordKeyList(record, path, true, null, true);
	}

	/**
	 * Get a list of primary keys as strings from a selection node or association by following a
	 * path within that table,
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @param distinct
	 *            exclude duplicates
	 * @param sortField
	 *            on which to sort the related records
	 * @param ascending
	 *            sort type
	 * @return the list of values, or an empty list
	 */
	public static List<String> getLinkedRecordKeyList(
		Adaptation record,
		Path path,
		boolean distinct,
		Path sortField,
		boolean ascending)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		try
		{
			if (reqRes == null)
			{
				return new ArrayList<>();
			}

			Collection<String> list = distinct ? new LinkedHashSet<>() : new ArrayList<>();
			if (sortField != null)
			{
				List<Adaptation> records = getRecords(reqRes);
				Collections.sort(records, createComparator(sortField));
				if (!ascending)
					Collections.reverse(records);
				for (Adaptation adaptation : records)
				{
					list.add(adaptation.getOccurrencePrimaryKey().format());
				}
			}
			else
			{
				try
				{
					Adaptation next;
					while ((next = reqRes.nextAdaptation()) != null)
					{
						list.add(next.getOccurrencePrimaryKey().format());
					}
				}
				finally
				{
					reqRes.close();
				}
			}
			return distinct ? new ArrayList<>(list) : (List<String>) list;
		}
		finally
		{
			if (reqRes != null)
			{
				reqRes.close();
			}
		}
	}

	/**
	 * Determine if there are any related records to the given record related by the path
	 * representing
	 * an association or selection node.
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @return Boolean true if the iterator of related records is empty
	 */
	public static boolean isLinkedRecordListEmpty(Adaptation record, Path path)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		try
		{
			return reqRes == null || reqRes.isEmpty();
		}
		finally
		{
			if (reqRes != null)
			{
				reqRes.close();
			}
		}
	}

	/**
	 * Get first record from a selection node or association
	 * (useful for singletons)
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @return the first record, or null if empty
	 */
	public static Adaptation getFirstRecordFromLinkedRecordList(Adaptation record, Path path)
	{
		return (Adaptation) getFirstRecordFromLinkedRecordList(record, path, null);
	}

	/**
	 * Get first value from a selection node or association by following a path within that table,
	 * or a the record if no attribute path is specified
	 * (useful for singletons)
	 *
	 * @param record
	 *            the record
	 * @param path
	 *            the path of the selection node or association
	 * @param attributePath
	 *            the path of the attribute on the selection node or association table
	 * @return the value or record, or null if empty
	 */
	public static Object getFirstRecordFromLinkedRecordList(
		Adaptation record,
		Path path,
		Path attributePath)
	{
		RequestResult reqRes = linkedRecordLookup(record, path);
		try
		{
			if (reqRes == null)
			{
				return null;
			}
			Adaptation adaptation = reqRes.nextAdaptation();
			if (adaptation == null || attributePath == null)
			{
				return adaptation;
			}
			else
			{
				return adaptation.get(attributePath);
			}
		}
		finally
		{
			if (reqRes != null)
			{
				reqRes.close();
			}
		}
	}

	/**
	 * Given a parameterized predicate string (where parameters are of the form ${pathString},
	 * and using the provided value context, replace the parameters with the values of the paths
	 * specified.
	 *
	 * @param predicate
	 *            parameterized predicate string
	 * @param vc
	 *            value context
	 * @return compiled predicate string
	 */
	public static String computeValuedPredicate(
		final String predicate,
		final ValueContextForValidation vc)
	{
		final Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		final Matcher matcher = pattern.matcher(predicate);
		String computedPredicate = predicate;
		while (matcher.find())
		{
			final String predicatePart = matcher.group()
				.replaceAll("\\$\\{", "")
				.replaceAll("\\}", "");
			String predItem = "";
			String[] paths = predicatePart.split("\\|");
			for (int i = 0; i < paths.length; i++)
			{
				String path = paths[i];
				final Object value = vc.getValue(Path.parse(path));
				if (value != null)
				{
					predItem += value.toString();
					if (i < paths.length - 1)
					{
						predItem += "|";
					}
				}
				i++;

				computedPredicate = computedPredicate.replaceAll("\\$\\{" + path + "\\}", predItem);

			}
			//			computedPredicate = computedPredicate.replaceAll("\\$\\{", "'")
			//					.replaceAll("\\}", "'");
		}

		return computedPredicate;
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing a foreign key field, return
	 * the
	 * target table of the foreign key.
	 *
	 * @param record
	 *            the record
	 * @param fkPath
	 *            the path of the foreign key field
	 * @return AdaptationTable the target table
	 */
	public static AdaptationTable getFKTable(final Adaptation record, final Path fkPath)
	{
		if (record == null)
		{
			return null;
		}

		return getFKTable(record.createValueContext(), fkPath);
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a foreign key field, return
	 * the
	 * target table of the foreign key.
	 *
	 * @param valueContext
	 *            the record context
	 * @param fkPath
	 *            the path of the foreign key field
	 * @return AdaptationTable the target table
	 */
	public static AdaptationTable getFKTable(final ValueContext valueContext, final Path fkPath)
	{
		if (valueContext == null)
		{
			return null;
		}

		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getTable(valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing a foreign key field, return
	 * the
	 * target record specified by the value of the foreign key field.
	 *
	 * @param record
	 *            the record
	 * @param fkPath
	 *            the path of the foreign key field
	 * @return Adaptation the related record
	 */
	public static Adaptation followFK(final Adaptation record, final Path fkPath)
	{
		if (record == null)
		{
			return null;
		}

		SchemaNode fkNode = record.getSchemaNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecord(record);
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a foreign key field, return
	 * the
	 * target record specified by the value of the foreign key field.
	 *
	 * @param valueContext
	 *            the record context
	 * @param fkPath
	 *            the path of the foreign key field
	 * @return Adaptation the related record
	 */
	public static Adaptation followFK(final ValueContext valueContext, final Path fkPath)
	{
		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return fkNode.getFacetOnTableReference().getLinkedRecord(valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code>, a path representing a foreign key field, and
	 * a path representing a field on the target record, return the value of that field on
	 * the related record.
	 *
	 * @param record
	 *            the record
	 * @param fkPath
	 *            the path of the foreign key field
	 * @param fkAttributePath
	 *            the path of the field on the target record
	 * @return Object the value of the field on the related record
	 */
	public static Object followFK(
		final Adaptation record,
		final Path fkPath,
		final Path fkAttributePath)
	{
		final Adaptation target = followFK(record, fkPath);
		if (target == null)
		{
			return null;
		}
		return target.get(fkAttributePath);
	}

	/**
	 * Given a record <code>ValueContext</code>, a path representing a foreign key field, and
	 * a path representing a field on the target record, return the value of that field on
	 * the related record.
	 *
	 * @param valueContext
	 *            the record context
	 * @param fkPath
	 *            the path of the foreign key field
	 * @param fkAttributePath
	 *            the path of the field on the target record
	 * @return Object the value of the field on the related record
	 */
	public static Object followFK(
		final ValueContext valueContext,
		final Path fkPath,
		final Path fkAttributePath)
	{
		final Adaptation target = followFK(valueContext, fkPath);
		if (target == null)
		{
			return null;
		}
		return target.get(fkAttributePath);
	}

	/**
	 * Given a record <code>Adaptation</code> and a path representing a repeating foreign key field,
	 * return the list of target records specified by the value of the foreign key field.
	 *
	 * @param record
	 *            the record
	 * @param fkPath
	 *            the path of the repeating foreign key field
	 * @return the related records, a list of adaptations
	 */
	public static List<Adaptation> followFKList(final Adaptation record, final Path fkPath)
	{
		if (record == null)
		{
			return new ArrayList<>();
		}

		SchemaNode fkNode = record.getSchemaNode().getNode(fkPath);
		return getLinkedRecords(fkNode, record, null);
	}

	/**
	 * Due to a bug in 5.7.1, linked records returned from a tableRef do not respect the order
	 * specified for the repeating field.  This method is used to work around this defect.
	 * Either a source record or record context should be passed in.
	 * 
	 * @param fkNode
	 * @param record
	 * @param vc
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Adaptation> getLinkedRecords(
		SchemaNode fkNode,
		Adaptation record,
		ValueContext vc)
	{
		if (vc == null && record != null)
			vc = record.createValueContext();
		//todo: when fixed, simply: return fkNode.getFacetOnTableReference().getLinkedRecords(vc);
		List<String> keys = (List<String>) vc.getValue(fkNode);
		SchemaFacetTableRef tableRef = fkNode.getFacetOnTableReference();
		AdaptationTable refTable = tableRef.getTable(vc);
		List<Adaptation> result = new ArrayList<>();
		for (String key : keys)
		{
			Adaptation ref = refTable.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(key));
			if (ref != null)
				result.add(ref);
		}
		return result;
	}

	/**
	 * Given a record <code>ValueContext</code> and a path representing a repeating foreign key
	 * field,
	 * return the list of target records specified by the value of the foreign key field.
	 *
	 * @param valueContext
	 *            the record context
	 * @param fkPath
	 *            the path of the repeating foreign key field
	 * @return the related records
	 */
	public static List<Adaptation> followFKList(final ValueContext valueContext, final Path fkPath)
	{
		SchemaNode fkNode = valueContext.getNode().getNode(fkPath);
		return getLinkedRecords(fkNode, null, valueContext);
	}

	/**
	 * Given a record <code>Adaptation</code>, a path representing a repeating foreign key field,
	 * and
	 * a path representing a field on the target record, return the list of the values of that field
	 * on
	 * the related records.
	 *
	 * @param record
	 *            the record
	 * @param fkPath
	 *            the path of the repeating foreign key field
	 * @param fkAttributePath
	 *            the path of the field on the target record
	 * @param includeNulls
	 *            indicator of whether the resulting collection should include null values
	 * @return values of the field on the related records
	 */
	public static List<?> followFKList(
		final Adaptation record,
		final Path fkPath,
		final Path fkAttributePath,
		final boolean includeNulls)
	{
		final List<Adaptation> targetList = followFKList(record, fkPath);
		return getValuesForFKList(targetList, fkAttributePath, includeNulls);
	}

	/**
	 * Given a record <code>ValueContext</code>, a path representing a repeating foreign key field,
	 * and
	 * a path representing a field on the target record, return the list of the values of that field
	 * on
	 * the related records.
	 *
	 * @param valueContext
	 *            the record context
	 * @param fkPath
	 *            the path of the repeating foreign key field
	 * @param fkAttributePath
	 *            the path of the field on the target record
	 * @param includeNulls
	 *            indicator of whether the resulting collection should include null values
	 * @return values of the field on the related records
	 */
	public static List<?> followFKList(
		final ValueContext valueContext,
		final Path fkPath,
		final Path fkAttributePath,
		final boolean includeNulls)
	{
		final List<Adaptation> targetList = followFKList(valueContext, fkPath);
		return getValuesForFKList(targetList, fkAttributePath, includeNulls);
	}

	private static List<?> getValuesForFKList(
		List<Adaptation> targetList,
		Path fkAttributePath,
		boolean includeNulls)
	{
		final List<Object> valueList = new ArrayList<>();
		for (Adaptation targetRecord : targetList)
		{
			Object value = targetRecord.get(fkAttributePath);
			if (includeNulls || value != null)
			{
				valueList.add(value);
			}
		}
		return valueList;
	}

	/**
	 * Get a record based on the xpath
	 *
	 * @deprecated Use {@link #getRecord(String, Adaptation, boolean, boolean)} instead. It allows
	 *             you to specify
	 *             whether the xpath contains all of the pk fields, which will result in quicker
	 *             lookup time.
	 *             This method will assume <code>false</code> for backwards compatibility, but it
	 *             should be a
	 *             conscious decision by the caller.
	 * @param instance
	 *            the data set
	 * @param xPathPredicate
	 *            the xpath
	 */
	@Deprecated
	public static Adaptation getOccurrenceFromXPathExpression(
		final Adaptation instance,
		final String xPathPredicate)
	{
		if (xPathPredicate == null || "".equals(xPathPredicate))
			return null;
		final int indexOfOpeningBracket = xPathPredicate.indexOf('[');
		if (indexOfOpeningBracket < 0)
			return null;
		final int lenghtOfXPathPredicate = xPathPredicate.length();
		if (indexOfOpeningBracket >= lenghtOfXPathPredicate)
			return null;
		final String tablePath = xPathPredicate.substring(0, indexOfOpeningBracket);
		final AdaptationTable table = instance.getTable(Path.parse(tablePath));
		if (table == null)
			return null;
		final String predicate = xPathPredicate
			.substring(indexOfOpeningBracket + 1, lenghtOfXPathPredicate - 1);
		final RequestResult result = table.createRequestResult(predicate);
		try
		{
			return result.nextAdaptation();
		}
		finally
		{
			result.close();
		}
	}

	/**
	 * Given a value context, return the record for which the context holds proposed values.
	 * Note well: {@link ValueContext#getAdaptationInstance()} cannot be used since it returns the
	 * data set.
	 *
	 * @param valueContext
	 *            the value context
	 * @return the record associated with the value context
	 */
	public static Adaptation getRecordForValueContext(ValueContext valueContext)
	{
		AdaptationTable table = valueContext.getAdaptationTable();
		if (table == null)
		{
			return null;
		}
		return table.lookupAdaptationByPrimaryKey(valueContext);
	}

	/**
	 * Looks up the given record again, by its primary key. This can be used for when you have a record that
	 * may have been updated by another process and you want to look it up again so that it is up to date (i.e. the data isn't stale).
	 * 
	 * @param record the record to reload
	 * @return the reloaded record
	 */
	// TODO: You should be able to just call record.getUpToDateInstance() and we can deprecate this method.
	//       That's equivalent to calling record.getHome().findAdaptationOrNull(record.getAdaptationName()).
	public static Adaptation reloadRecord(Adaptation record)
	{
		return record.getContainerTable()
			.lookupAdaptationByPrimaryKey(record.getOccurrencePrimaryKey());
	}

	/**
	 * Given a field context (e.g. on a constraint) and a path to another field on the same record,
	 * return the value of the other field, specified by its adaptation-relative path.
	 * 
	 * @param fieldContext
	 * @param pathToOtherField
	 * @return Object value of the other field
	 */
	public static Object getValueOfOtherField(ValueContext fieldContext, Path pathToOtherField)
	{
		SchemaNode fieldNode = fieldContext.getNode();
		Path fieldPath = fieldNode.getPathInAdaptation();
		int parentCount = fieldPath.getSize();
		for (int i = 0; i < parentCount; i++)
		{
			pathToOtherField = Path.PARENT.add(pathToOtherField);
		}
		return fieldContext.getValue(pathToOtherField);
	}

	/**
	 * Get the same record from a different data space, if it exists.
	 *
	 * @param record
	 *            the record to look for
	 * @param otherDataSpace
	 *            the other data space to find it in
	 * @return the record from the other data space, or null
	 */
	public static Adaptation getRecordFromOtherDataSpace(
		Adaptation record,
		AdaptationHome otherDataSpace)
	{
		if (record == null || otherDataSpace == null)
		{
			return null;
		}
		Adaptation dataSet = record.getContainer();
		Adaptation otherDataSet = otherDataSpace.findAdaptationOrNull(dataSet.getAdaptationName());
		if (otherDataSet == null)
		{
			return null;
		}
		AdaptationTable table = record.getContainerTable();
		AdaptationTable otherTable = otherDataSet.getTable(table.getTablePath());
		if (otherTable == null)
		{
			return null;
		}
		Adaptation otherRecord = otherTable
			.lookupAdaptationByPrimaryKey(record.getOccurrencePrimaryKey());
		return otherRecord;
	}

	/**
	 * Get the same record from the parent data space, if it exists
	 *
	 * @param record
	 *            the record to look for
	 * @return the record from the parent data space, or null
	 */
	public static Adaptation getRecordFromParentDataSpace(Adaptation record)
	{
		if (record == null)
		{
			return null;
		}
		AdaptationHome parentDataSpace = record.getHome().getParentBranch();
		if (parentDataSpace == null)
		{
			return null;
		}
		return getRecordFromOtherDataSpace(record, parentDataSpace);
	}

	/**
	 * Get the same record from the initial snapshot data space, if it exists
	 *
	 * @param record
	 *            the record to look for
	 * @return the record from the initial snapshot data space, or null
	 */
	public static Adaptation getRecordFromInitialSnapshot(Adaptation record)
	{
		if (record == null)
		{
			return null;
		}
		AdaptationHome initialSnapshot = record.getHome().getParent();
		if (initialSnapshot == null)
		{
			return null;
		}
		return getRecordFromOtherDataSpace(record, initialSnapshot);
	}

	/**
	 * Get the same record from the initial snapshot data space, if it exists and restore the current record to the values from the initial snapshot record
	 *   If it does not exist, then it must have been created in the current dataspace, so it will now be deleted
	 *
	 * @param record
	 *            the current record to be restored record to look for
	 * @param procedureContext
	 *            the current procedureContext
	 * @return the restored record from the initial snapshot data space, or null of the record was created in the current dataspace
	 * @throws OperationException 
	 * @throws  
	 */
	public static Adaptation restoreRecordFromInitialSnapshot(
		Adaptation currentRecord,
		ProcedureContext procedureContext)
		throws OperationException
	{
		if (currentRecord == null)
		{
			return null;
		}
		Adaptation initialSnapshotRecord = getRecordFromInitialSnapshot(currentRecord);
		if (initialSnapshotRecord != null)
		{
			return procedureContext
				.doModifyContent(currentRecord, initialSnapshotRecord.createValueContext());
		}
		else
		{
			//Delete the current record since it is not in the initial snapshot
			procedureContext.doDelete(currentRecord.getAdaptationName(), true);
			return null;
		}
	}

	/**
	 * Get a record based on the xpath
	 *
	 * @deprecated Use {@link #getRecord(String, Adaptation, boolean, boolean)} instead. It allows
	 *             you to specify
	 *             whether the xpath contains all of the pk fields, which will result in quicker
	 *             lookup time.
	 *             This method will assume <code>false</code> for backwards compatibility, but it
	 *             should be a
	 *             conscious decision by the caller.
	 * @param recordXpath
	 *            the xpath
	 * @param dataSet
	 *            the data set
	 * @param errorIfNotFound
	 *            Whether to consider not found to be an error
	 * @throws OperationException
	 *             if an error occurs, or if not found and <code>errorIfNotFound</code> is
	 *             <code>true</code>
	 */
	@Deprecated
	public static Adaptation getRecord(
		String recordXpath,
		Adaptation dataSet,
		boolean errorIfNotFound)
		throws OperationException
	{
		return getRecord(recordXpath, dataSet, false, errorIfNotFound);
	}

	/**
	 * Get a record based on the xpath.
	 *
	 * @see XPathExpressionHelper#lookupFirstRecordMatchingXPath(boolean, Adaptation, String)
	 * @param recordXpath
	 *            the xpath
	 * @param dataSet
	 *            the data set
	 * @param checkActualPrimaryKey
	 *            Whether the xpath specifies each member of the primary key, for faster lookup
	 * @param errorIfNotFound
	 *            Whether to consider not found to be an error
	 * @throws OperationException
	 *             if an error occurs, or if not found and <code>errorIfNotFound</code> is
	 *             <code>true</code>
	 */
	public static Adaptation getRecord(
		String recordXpath,
		Adaptation dataSet,
		boolean checkActualPrimaryKey,
		boolean errorIfNotFound)
		throws OperationException
	{
		if (recordXpath == null || dataSet == null)
		{
			return null;
		}
		Adaptation recordAdaptation = XPathExpressionHelper
			.lookupFirstRecordMatchingXPath(checkActualPrimaryKey, dataSet, recordXpath);
		if (recordAdaptation == null && errorIfNotFound)
		{
			throw OperationException
				.createError("Record for '" + recordXpath + "' has not been found");
		}
		return recordAdaptation;
	}

	/**
	 * Return a user message with the specified message and severity where the message is prefixed
	 * by information about the record.
	 *
	 * @param record
	 *            the record
	 * @param msg
	 *            the simple message
	 * @param severity
	 *            the desired severity
	 * @return a UserMessage
	 */
	public static UserMessageString createUserMessage(
		Adaptation record,
		String msg,
		Severity severity)
	{
		String msgTxt = "Record " + record.getOccurrencePrimaryKey().format() + " in the "
			+ record.getContainerTable().getTableNode().getLabel(Locale.getDefault()) + " Table, "
			+ record.getLabel(Locale.getDefault()) + ": " + msg;

		return createUserMessage(msgTxt, severity);
	}

	/**
	 * Create and return a UserMessage with the specified message and severity
	 * 
	 * @param msg
	 *            the simple message
	 * @param severity
	 *            the desired severity
	 * @return new UserMessage
	 */
	public static UserMessageString createUserMessage(String msg, Severity severity)
	{
		if (Severity.FATAL == severity)
		{
			return UserMessage.createFatal(msg);
		}
		if (Severity.ERROR == severity)
		{
			return UserMessage.createError(msg);
		}
		if (Severity.WARNING == severity)
		{
			return UserMessage.createWarning(msg);
		}
		return UserMessage.createInfo(msg);
	}

	/**
	 * Gets all of the tables for a data set.
	 * This is equivalent of <code>getAllTables(dataSet, dataSet.getSchemaNode())</code>
	 *
	 * @param dataSet
	 *            the data set
	 * @return the tables for the data set, or an empty list if none exist
	 */
	public static List<AdaptationTable> getAllTables(Adaptation dataSet)
	{
		return getAllTables(dataSet, dataSet.getSchemaNode());
	}

	/**
	 * Gets all of the tables for a data set under the given node.
	 *
	 * @param dataSet
	 *            the data set
	 * @param parentNode
	 *            the schema node to look under
	 * @return the tables for the data set under the given node, or an empty list if none exist
	 */
	public static List<AdaptationTable> getAllTables(Adaptation dataSet, SchemaNode parentNode)
	{
		SchemaNode[] children = parentNode.getNodeChildren();
		ArrayList<AdaptationTable> tables = new ArrayList<>();
		for (SchemaNode child : children)
		{
			if (child.isTableNode())
			{
				tables.add(dataSet.getTable(child.getPathInSchema()));
			}
			else
			{
				tables.addAll(getAllTables(dataSet, child));
			}
		}
		return tables;
	}

	/**
	 * Gets the label for a field
	 *
	 * @param adaptation
	 *            the record or data set (for cases where you want a data set level field)
	 * @param fieldPath
	 *            the path of the field within the given adaptation
	 * @param session
	 *            the user's session
	 * @param includeGroupLabels
	 *            Include the labels of the parent group(s) of the field
	 * @return the label
	 */
	public static String getFieldLabel(
		Adaptation adaptation,
		Path fieldPath,
		Session session,
		boolean includeGroupLabels)
	{
		SchemaNode node = adaptation.getSchemaNode().getNode(fieldPath);
		StringBuilder bldr = new StringBuilder();
		Locale locale = session.getLocale();
		bldr.append(node.getLabel(locale));
		if (includeGroupLabels)
		{
			Path tablePath = node.getTableNode().getPathInSchema();
			// Loop through the parents until you get to a table node and for each group add its
			// label
			for (SchemaNode parentNode = node; (parentNode = parentNode
				.getNode(Path.PARENT)) != null && !tablePath.equals(parentNode.getPathInSchema());)
			{
				bldr.insert(0, " / ");
				bldr.insert(0, parentNode.getLabel(locale));
			}
		}
		return bldr.toString();
	}

	/**
	 * Depending on the schema type associated with the specified node, values will be quoted in
	 * predicates.
	 * For example, all the string types and date types would have their values in quotes.
	 * 
	 * @param node
	 *            the schema node representing a field
	 * @return <code>true</code> if values for that field would require quotes
	 */
	public static boolean isValueQuotedInPredicate(SchemaNode node)
	{
		SchemaTypeName type = node.getXsTypeName();
		return !(SchemaTypeName.XS_BOOLEAN.equals(type) || SchemaTypeName.XS_DECIMAL.equals(type)
			|| SchemaTypeName.XS_INT.equals(type) || SchemaTypeName.XS_INTEGER.equals(type));
	}

	public static DifferenceBetweenOccurrences getRecordDifferencesInChildDataSpace(
		Adaptation record,
		boolean resolvedMode)
		throws OperationException
	{
		AdaptationHome childDataSpace = record.getHome();
		AdaptationHome initialSnapshot = childDataSpace.getParent();
		if (initialSnapshot == null)
		{
			throw OperationException.createError(
				"No initial snapshot found for data space " + childDataSpace.getKey().getName());
		}
		Adaptation dataSet = record.getContainer();
		Adaptation initialDataSet = initialSnapshot
			.findAdaptationOrNull(dataSet.getAdaptationName());
		if (initialDataSet == null)
		{
			throw OperationException.createError(
				"Data set " + dataSet.getAdaptationName().getStringName()
					+ " not found in snapshot " + initialSnapshot.getKey().getName());
		}
		AdaptationTable table = record.getContainerTable();
		AdaptationTable initialTable = initialDataSet.getTable(table.getTablePath());
		if (initialTable == null)
		{
			throw OperationException.createError(
				"Table " + table.getTablePath().format() + " not found in data set "
					+ initialDataSet.getAdaptationName().getStringName() + " in snapshot "
					+ initialSnapshot.getKey().getName());
		}
		Adaptation initialRecord = initialTable.lookupAdaptationByName(record.getAdaptationName());
		if (initialRecord == null)
		{
			throw OperationException.createError(
				"Record " + record.toXPathExpression() + " not found in snapshot "
					+ initialSnapshot.getKey().getName());
		}
		return DifferenceHelper.compareOccurrences(record, initialRecord, resolvedMode);
	}

	/**
	 * Return the association record identified by the two records it associates and the association
	 * table
	 * 
	 * @param tablePath
	 *            a path within the same data set as the key1 adaptation
	 * @param key1
	 * @param key2
	 * @return Adaptation the record corresponding th the association
	 */
	public static Adaptation getAssociationRecord(Path tablePath, Adaptation key1, Adaptation key2)
	{
		AdaptationTable assocTable = key2.getContainerTable()
			.getContainerAdaptation()
			.getTable(tablePath);
		PrimaryKey key = assocTable.computePrimaryKey(
			new Object[] { key1.getOccurrencePrimaryKey().format(),
					key2.getOccurrencePrimaryKey().format() });
		return assocTable.lookupAdaptationByPrimaryKey(key);
	}

	/**
	 * Evaluate a 'path' expression where each of the paths except the last item represents an
	 * adaptation path
	 * against the adaptation represented by the previous path and the first path is one against the
	 * provided record.
	 * 
	 * @param recordContext
	 * @param paths
	 * @return list of objects representing the union of all evaluated paths
	 */
	public static List<Adaptation> evaluatePath(ValueContext recordContext, Path[] paths)
	{
		List<Adaptation> thisLevel = evaluateSingle(null, recordContext, paths[0]);
		for (int i = 1; i < paths.length; i++)
		{
			Path path = paths[i];
			List<Adaptation> nextLevel = new ArrayList<>();
			for (Adaptation a : thisLevel)
			{
				if (a != null)
					nextLevel.addAll(evaluateSingle(a, null, path));
			}
			if (nextLevel.isEmpty())
				return null;
			thisLevel = nextLevel;
		}
		return thisLevel;
	}

	/**
	 * Starting from either a recordRoot (Adaptation) or a contextRoot (ValueContext), find the
	 * related records,
	 * a list of Adaptations, resulting from the provided Path. The Path should represent a fk, fk
	 * list, association,
	 * or selection.  For an fk, the collection should contain 0 or 1 Adaptations.
	 * 
	 * @param recordRoot
	 * @param contextRoot
	 * @param path
	 * @return related adaptations
	 */
	public static List<Adaptation> evaluateSingle(
		Adaptation recordRoot,
		ValueContext contextRoot,
		Path path)
	{
		SchemaNode recordNode = recordRoot != null ? recordRoot.getSchemaNode()
			: contextRoot.getNode();
		SchemaNode fkNode = recordNode.getNode(path);
		SchemaFacetTableRef tableRef = fkNode.getFacetOnTableReference();
		if (tableRef != null)
		{
			if (fkNode.getMaxOccurs() > 1)
			{
				return getLinkedRecords(fkNode, recordRoot, contextRoot);
			}
			Adaptation linkedRecord = recordRoot != null ? tableRef.getLinkedRecord(recordRoot)
				: tableRef.getLinkedRecord(contextRoot);
			return linkedRecord == null ? Collections.<Adaptation> emptyList()
				: Collections.singletonList(linkedRecord);
		}
		else if (fkNode.isAssociationNode() || fkNode.isSelectNode())
		{
			if (recordRoot == null)
				recordRoot = getRecordForValueContext(contextRoot);
			return recordRoot != null ? getLinkedRecordList(recordRoot, path)
				: Collections.<Adaptation> emptyList();
		}
		return Collections.<Adaptation> emptyList();
	}

	/**
	 * Evaluate a 'path' expression where each of the paths except the last item represents an
	 * adaptation path
	 * against the adaptation represented by the previous path and the first path is one against the
	 * provided record.
	 * 
	 * @param record
	 * @param paths
	 * @return list of objects representing the union of all evaluated paths
	 */
	public static List<Adaptation> evaluatePath(Adaptation record, Path[] paths)
	{
		List<Adaptation> thisLevel = new ArrayList<>();
		thisLevel.add(record);
		for (int i = 0; i < paths.length; i++)
		{
			Path path = paths[i];
			List<Adaptation> nextLevel = new ArrayList<>();
			for (Adaptation a : thisLevel)
			{
				if (a != null)
					nextLevel.addAll(evaluateSingle(a, null, path));
			}
			if (nextLevel.isEmpty())
				return null;
			thisLevel = nextLevel;
		}
		return thisLevel;
	}

	/**
	 * Using the list of adaptations resulting from the paths against the record, produce the
	 * list of field values for those records.
	 * 
	 * @param recordContext
	 * @param paths
	 * @param field
	 * @return the list of values for the path
	 */
	public static List<Object> evaluatePath(
		ValueContext recordContext,
		Path[] paths,
		Path field,
		boolean skipNull)
	{
		List<Adaptation> records = evaluatePath(recordContext, paths);
		if (records == null)
			return null;
		List<Object> result = new ArrayList<>();
		for (Adaptation a : records)
		{
			SchemaNode node = a.getSchemaNode().getNode(field);
			if (node.getMaxOccurs() > 1)
				result.addAll(a.getList(field));
			else
				result.add(a.get(field));
		}
		if (skipNull)
			result = Algorithms.select(result, UnaryPredicate.isNotNull);
		return result;
	}

	/**
	 * Using the list of adaptations resulting from the paths against the record, produce the
	 * list of field values for those records.
	 * 
	 * @param record
	 * @param paths
	 * @param field
	 * @return the list of values for the path
	 */
	public static List<Object> evaluatePath(
		Adaptation record,
		Path[] paths,
		Path field,
		boolean skipNull)
	{
		List<Adaptation> records = evaluatePath(record, paths);
		if (records == null)
			return null;
		List<Object> result = new ArrayList<>();
		for (Adaptation a : records)
		{
			SchemaNode node = a.getSchemaNode().getNode(field);
			if (node.getMaxOccurs() != 1)
				result.addAll(a.getList(field));
			else
				result.add(a.get(field));
		}
		if (skipNull)
			result = Algorithms.select(result, UnaryPredicate.isNotNull);
		return result;
	}

	/**
	 * Answers whether the specified node represents a relationship field (foreign key, association
	 * or selection node).
	 *
	 * @param node
	 *            the node
	 * @return <code>true</code> if the node represetns a relationship
	 */
	public static boolean isRelationshipNode(SchemaNode node)
	{
		SchemaFacetTableRef tableRef = node.getFacetOnTableReference();
		if (tableRef != null)
			return true;
		return node.isAssociationNode() || node.isSelectNode();
	}

	/**
	 * For a given node, if the node is a relationship node (foreign key, association or selection node),
	 * return the target table of the relationship.
	 * 
	 * Note that if this is called during schema compilation (such as from a setup method), it is not
	 * guaranteed that link properties will have been determined by then and therefore,
	 * you shouldn't rely on this returning a value in those cases.
	 *
	 * @param node
	 *            the node
	 * @return the table node of the related table or null if the node is not a relationship node
	 */
	public static SchemaNode getTableNodeForRelated(SchemaNode node)
	{
		return getTableNodeForRelated(node, null);
	}

	/**
	 * For a given node, if the node is a relationship node (foreign key, association or selection node),
	 * return the target table of the relationship.
	 * 
	 * Note that if this is called during schema compilation (such as from a setup method), it is not
	 * guaranteed that link properties will have been determined by then and therefore,
	 * you shouldn't rely on this returning a value in those cases.
	 *
	 * @param node
	 *            the node
	 * @param dataSet
	 *            the data set for the table you are starting from
	 * @return the table node of the related table or null if the node is not a relationship node
	 */
	public static SchemaNode getTableNodeForRelated(SchemaNode node, Adaptation dataSet)
	{
		SchemaFacetTableRef tableRef = node.getFacetOnTableReference();
		if (tableRef != null)
		{
			return tableRef.getTableNode();
		}
		else if (node.isAssociationNode())
		{
			AssociationLink link = node.getAssociationLink();
			// During schema compilation, it can't be guaranteed what order things are compiled in,
			// and therefore it's expected that you can not yet know the link properties and it can be null.
			if (link == null)
				return null;
			HomeKey dataSpaceKey = link.getDataSpaceReference();
			AdaptationName dataSetKey = link.getDataSetReference();
			SchemaNode dataSetRoot = node;
			if (dataSetKey != null)
			{
				if (dataSet == null)
					throw new IllegalArgumentException(
						"If an association uses a table from another data set, an original data set is required to find the target table");
				AdaptationHome dataSpace = dataSet.getHome();
				if (dataSpaceKey != null)
					dataSpace = dataSpace.getRepository().lookupHome(dataSpaceKey);
				dataSet = dataSpace.findAdaptationOrNull(dataSetKey);
				dataSetRoot = dataSet.getSchemaNode();
			}
			if (link.isLinkTable())
			{
				AssociationLinkByLinkTable alink = (AssociationLinkByLinkTable) link;
				Path targetPath = alink.getFieldToTargetPath();
				SchemaNode fieldNode = dataSetRoot.getNode(targetPath);
				return getTableNodeForRelated(fieldNode, dataSet);
			}
			else if (link.isXPathLink())
			{
				AssociationLinkByXPathLink alink = (AssociationLinkByXPathLink) link;
				return alink.getTableNode();
			}
			else
			{
				AssociationLinkByTableRefInverse alink = (AssociationLinkByTableRefInverse) link;
				Path sourcePath = alink.getFieldToSourcePath();
				return dataSetRoot.getNode(sourcePath).getTableNode();
			}
		}
		else if (node.isSelectNode())
		{
			SelectionLink link = node.getSelectionLink();
			return link.getTableNode();
		}
		return null;
	}

	/**
	 * Given a record and function that will return a parent record for a given record, and an
	 * optional
	 * function to get the display-name for a given record (label is used by default), traverse the
	 * parent
	 * path implied by the getParent function and return a string representation of that path if it
	 * creates
	 * a cycle, null otherwise.
	 * 
	 * @param record
	 * @param recordContext
	 *            (for the record being modified, used to evaluate the first parent only)
	 * @param getParent
	 *            - binary function to get parent from record or context (pref to context)
	 * @param toString
	 * @return a string representing the path that creates the cycles
	 * @deprecated Use {@link #detectAncestorCycle(Adaptation, ValueContext, BinaryFunction, UnaryFunction)} or
	 *             {@link #isAncestorOf(PrimaryKey, Adaptation, BinaryFunction)} instead. This method is kept for
	 *             backwards-compatibility, but it doesn't check all potential parent paths for cycles. If the
	 *             record has a direct foreign key to its parent, it should still work okay, but if it is over
	 *             a link table, there are scenarios where it won't catch the cycle.
	 */
	@Deprecated
	public static String detectCycle(
		Adaptation record,
		ValueContext recordContext,
		BinaryFunction<Adaptation, ValueContext, Adaptation> getParent,
		UnaryFunction<Adaptation, String> toString)
	{
		if (toString == null)
			toString = GetDefaultLabel;
		Set<Adaptation> visited = new LinkedHashSet<>();
		Adaptation next = record;
		while (next != null)
		{
			if (!visited.add(next))
			{
				StringBuilder message = new StringBuilder();
				for (Adaptation adaptation : visited)
				{
					if (message.length() > 0)
						message.append("->");
					message.append(toString.evaluate(adaptation));
				}
				return message.toString();
			}
			next = getParent.evaluate(next, recordContext);
			recordContext = null; //only use context once
		}
		return null;
	}

	/**
	 * Given a record and function that will return parent records for a given record, and an optional
	 * function to get the display-name for a given record (label is used by default), traverse the parent
	 * path implied by the getParents function and return a string representation of that path if it creates
	 * a cycle, null otherwise.
	 * 
	 * @param record
	 * @param recordContext
	 *            (for the record being modified, used to evaluate the first parents only)
	 * @param getParents
	 *            - binary function to get parents from record or context (pref to context)
	 * @param toString
	 * @return a string representing the path that creates the cycles
	 */
	public static String detectAncestorCycle(
		Adaptation record,
		ValueContext recordContext,
		BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents,
		UnaryFunction<Adaptation, String> toString)
	{
		if (toString == null)
		{
			toString = GetDefaultLabel;
		}
		List<Adaptation> parents = getParents.evaluate(record, recordContext);
		PrimaryKey pk;
		if (recordContext == null)
		{
			pk = record.getOccurrencePrimaryKey();
		}
		else
		{
			pk = recordContext.getAdaptationTable().computePrimaryKey(recordContext);
		}
		List<Adaptation> cycleRecords = doDetectAncestorCycle(
			pk,
			parents,
			getParents,
			new ArrayList<>());
		if (cycleRecords != null)
		{
			StringBuilder message = new StringBuilder();
			if (record != null)
			{
				message.append(toString.evaluate(record));
			}
			for (int i = 0; i < cycleRecords.size(); i++)
			{
				Adaptation cycleRecord = cycleRecords.get(i);
				// Sometimes the record can appear as the first record in the cycle as well
				// (like when creating a join record with the same record as both parent & child).
				// Easiest thing to do is just check if it's the first record and it's the same
				// as the record being checked for cyclces, and don't process it if so.
				if (!(i == 0 && record != null && record.getOccurrencePrimaryKey()
					.equals(cycleRecord.getOccurrencePrimaryKey())))
				{
					if (message.length() > 0)
					{
						message.append("->");
					}
					message.append(toString.evaluate(cycleRecord));
				}
			}
			return message.toString();
		}
		return null;
	}

	/**
	 * Return whether or not the given primary key is in the ancestor tree of another record. This utilizes similar logic to
	 * {@link #detectAncestorCycle(Adaptation, ValueContext, BinaryFunction, UnaryFunction)} but it can be used when you're
	 * comparing a record to see if it's the parent of another record, which can be used for table filters, versus checking
	 * that a record has itself as a parent.
	 * 
	 * @param recordPrimaryKey the primary key of the record to check
	 * @param otherRecord the other record to check the ancestry on
	 * @param getParents binary function to get parents from the other record (can use same function as <code>detectAncestorCycle</code>)
	 * @return whether or not it's an ancestor
	 */
	public static boolean isAncestorOf(
		PrimaryKey recordPrimaryKey,
		Adaptation otherRecord,
		BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents)
	{
		List<Adaptation> parents = new ArrayList<>();
		parents.add(otherRecord);
		List<Adaptation> cycleRecords = doDetectAncestorCycle(
			recordPrimaryKey,
			parents,
			getParents,
			new ArrayList<>());
		return cycleRecords != null && !cycleRecords.isEmpty();
	}

	// This method recursively calls itself at every level up the tree, returning a list of records representing
	// a cycle, or null if there's no cycle. The existing potential children of the cycle is passed into it, and
	// in that way it builds up what the cycle will be if detected. This is needed because a record can have
	// multiple parents and we need to know which path this invocation went up.
	private static List<Adaptation> doDetectAncestorCycle(
		PrimaryKey recordPrimaryKey,
		List<Adaptation> parentRecords,
		BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents,
		List<Adaptation> cycleChildren)
	{
		List<Adaptation> returnVal = null;
		if (parentRecords != null)
		{
			Iterator<Adaptation> iter = parentRecords.iterator();
			while (returnVal == null && iter.hasNext())
			{
				Adaptation next = iter.next();
				// If we found the record we were looking for, return a new list containing
				// the cycle children and the record itself
				if (next.getOccurrencePrimaryKey().equals(recordPrimaryKey))
				{
					returnVal = new ArrayList<>(cycleChildren);
					returnVal.add(next);
				}
				// Else If we found the cycle where a parent is already present in the hierarchy, then return null, 
				//       because this means that the primary record itself does not have a cycle
				else if (cycleChildren.contains(next))
				{
					return null;
				}
				// Otherwise get the parents, create a new list and add the cycle
				// children and the record itself to it, and recursively call into
				// this method with that list. It's important to create a new list
				// because if the recursive call were to modify the list passed in
				// itself, then the calls to its siblings would get messed up.
				else
				{
					List<Adaptation> nextParents = getParents.evaluate(next, null);
					List<Adaptation> newCycleChidren = new ArrayList<>(cycleChildren);
					newCycleChidren.add(next);
					returnVal = doDetectAncestorCycle(
						recordPrimaryKey,
						nextParents,
						getParents,
						newCycleChidren);
				}
			}
		}
		return returnVal;
	}

	public static enum CompareOper {
		Equals, NotEquals, LessThan, LessThanOrEqual, GreaterThan, GreaterThanOrEqual, IsNull, IsNotNull, DateEarlier, DateEqual, DateLater, StartsWith, EndsWith, Contains, EqualsIgnoreCase
	};

	public static String createQueryPredicate(
		SchemaNode tableNode,
		Path[] paths,
		Object[] values,
		CompareOper[] compareOpers)
	{
		if (paths == null || values == null)
			return null;
		if (paths.length != values.length)
			throw new IllegalArgumentException("Paths and values should have the same count");
		if (compareOpers != null && compareOpers.length != paths.length)
			throw new IllegalArgumentException(
				"If compareOpers is provided, but have one for each path/value");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < paths.length; i++)
		{
			if (i > 0)
				sb.append(" and ");
			sb.append("(");
			appendPredicate(
				sb,
				tableNode,
				paths[i],
				values[i],
				compareOpers == null ? null : compareOpers[i]);
			sb.append(")");
		}
		return sb.toString();
	}

	public static String createQueryPredicate(
		SchemaNode tableNode,
		Path path,
		Object value,
		CompareOper compareOper)
	{
		StringBuilder sb = new StringBuilder();
		appendPredicate(sb, tableNode, path, value, compareOper);
		return sb.toString();
	}

	public static void appendPredicate(
		StringBuilder sb,
		SchemaNode tableNode,
		Path path,
		Object value,
		CompareOper compareOper)
	{
		SchemaNode node = tableNode.getNode(path);
		if (compareOper == null && isDate(node))
			compareOper = CompareOper.DateEqual;
		buildPredicate(sb, path, valueString(node, value, true), compareOper);
	}

	// builds a predicate equivalent to an IN or NOT IN Statement for a list of IDs
	// expects the compareOper to be Equals or NotEquals
	public static void appendIdListPredicate(
		StringBuilder predicate,
		SchemaNode tableNode,
		Path path,
		List<String> idList,
		CompareOper compareOper)
	{
		int size = idList.size();
		if (idList.isEmpty())
		{
			return;
		}
		predicate = predicate.append(" (");
		int count = 0;
		for (String id : idList)
		{
			count++;
			AdaptationUtil.appendPredicate(predicate, tableNode, path, id, compareOper);
			if (count < size)
			{
				if (compareOper.equals(CompareOper.Equals))
				{
					predicate.append(" or ");
				}
				else
				{
					predicate.append(" and ");
				}
			}
		}
		predicate = predicate.append(") ");
	}

	public static boolean isDate(SchemaNode node)
	{
		SchemaTypeName type = node.getXsTypeName();
		return SchemaTypeName.XS_DATE.equals(type) || SchemaTypeName.XS_DATETIME.equals(type);
	}

	public static Object convertStringToValueObject(SchemaNode node, String value)
		throws ParseException
	{
		if (value == null)
		{
			return null;
		}
		SchemaTypeName type = node.getXsTypeName();
		if (SchemaTypeName.XS_BOOLEAN.equals(type))
		{
			return Boolean.valueOf(value);
		}
		if (SchemaTypeName.XS_INT.equals(type) || SchemaTypeName.XS_INTEGER.equals(type))
		{
			return Integer.valueOf(value);
		}
		if (SchemaTypeName.XS_DECIMAL.equals(type))
		{
			return new BigDecimal(value);
		}
		if (SchemaTypeName.XS_DATE.equals(type))
		{
			return (new SimpleDateFormat(CommonConstants.EBX_DATE_FORMAT)).parse(value);
		}
		if (SchemaTypeName.XS_DATETIME.equals(type))
		{
			return (new SimpleDateFormat(CommonConstants.EBX_DATE_TIME_FORMAT)).parse(value);
		}
		if (SchemaTypeName.XS_TIME.equals(type))
		{
			return (new SimpleDateFormat(CommonConstants.EBX_TIME_FORMAT)).parse(value);
		}
		return value;
	}

	/**
	 * @deprecated Use {@link valueString(SchemaNode, Object, boolean} instead.
	 *             Invoking this method is equivalent of calling that with <code>true</code> as the last parameter,
	 *             but you don't always want to encode because if you know a field will never contain special characters
	 *             then you can avoid the extra processing.
	 */
	@Deprecated
	public static String valueString(SchemaNode node, Object value)
	{
		return valueString(node, value, true);
	}

	public static String valueString(SchemaNode node, Object value, boolean encodeString)
	{
		if (value == null)
			return null;
		String valString = null;
		SchemaTypeName type = node.getXsTypeName();
		if (SchemaTypeName.XS_DECIMAL.equals(type) || SchemaTypeName.XS_INT.equals(type)
			|| SchemaTypeName.XS_INTEGER.equals(type))
		{
			valString = value.toString();
		}
		else
		{
			valString = node.formatToXsString(value);
		}
		if (AdaptationUtil.isValueQuotedInPredicate(node))
		{
			if (encodeString)
			{
				valString = XPathExpressionHelper.encodeLiteralStringWithDelimiters(valString);
			}
			else
			{
				valString = "'" + valString + "'";
			}
		}
		return valString;
	}

	public static void buildPredicate(StringBuilder bldr, Path path, String value, CompareOper oper)
	{
		if (oper == null)
			oper = CompareOper.Equals;
		switch (oper)
		{
		case Equals:
			if (value == null)
				buildPredicate(bldr, path, value, CompareOper.IsNull);
			else
				buildBinaryOperator(bldr, path, value, " = ");
			break;
		case NotEquals:
			if (value == null)
				buildPredicate(bldr, path, value, CompareOper.IsNotNull);
			else
				buildBinaryOperator(bldr, path, value, " != ");
			break;
		case LessThan:
			buildBinaryOperator(bldr, path, value, " < ");
			break;
		case LessThanOrEqual:
			buildBinaryOperator(bldr, path, value, " <= ");
			break;
		case GreaterThan:
			buildBinaryOperator(bldr, path, value, " > ");
			break;
		case GreaterThanOrEqual:
			buildBinaryOperator(bldr, path, value, " >= ");
			break;
		case IsNull:
			buildUnaryFunction(bldr, path, "osd:is-null");
			break;
		case IsNotNull:
			buildUnaryFunction(bldr, path, "osd:is-not-null");
			break;
		case DateEarlier:
			buildBinaryFunction(bldr, path, value, "date-less-than");
			break;
		case DateEqual:
			if (value == null)
				buildPredicate(bldr, path, value, CompareOper.IsNull);
			else
				buildBinaryFunction(bldr, path, value, "date-equal");
			break;
		case DateLater:
			buildBinaryFunction(bldr, path, value, "date-greater-than");
			break;
		case StartsWith:
			buildBinaryFunction(bldr, path, value, "starts-with");
			break;
		case EndsWith:
			buildBinaryFunction(bldr, path, value, "ends-with");
			break;
		case Contains:
			buildBinaryFunction(bldr, path, value, "contains");
			break;
		case EqualsIgnoreCase:
			buildBinaryFunction(bldr, path, value, "osd:is-equal-case-insensitive");
			break;
		}
	}
	private static void buildBinaryFunction(
		StringBuilder bldr,
		Path path,
		String value,
		String function)
	{
		bldr.append(function)
			.append("(")
			.append(path.format())
			.append(",")
			.append(value)
			.append(")");
	}

	private static void buildUnaryFunction(StringBuilder bldr, Path path, String function)
	{
		bldr.append(function).append("(").append(path.format()).append(")");
	}

	private static void buildBinaryOperator(
		StringBuilder bldr,
		Path path,
		String value,
		String oper)
	{
		bldr.append(path.format()).append(oper).append(value);
	}

	/**
	 * Given a table and a path to the 'name' field, determine whether the given name exists in the
	 * table
	 */
	public static class IsNameTaken extends UnaryPredicate<String>
	{
		private final AdaptationTable table;
		private final Path namePath;

		public IsNameTaken(AdaptationTable table, Path namePath)
		{
			this.table = table;
			this.namePath = namePath;
		}

		@Override
		public boolean test(String object)
		{
			String predicate = namePath.format() + " = '" + object + "'";
			RequestResult result = table.createRequestResult(predicate);
			try
			{
				return !result.isEmpty();
			}
			finally
			{
				result.close();
			}
		}

	}

	public static Adaptation getOneRecordOrThrowOperationException(
		final Repository aRepository,
		final String aBranchName,
		final String aAdapationName,
		final String aXpath)
		throws OperationException
	{
		final AdaptationHome home = getDataSpaceOrThrowOperationException(aRepository, aBranchName);
		final Adaptation instance = getDataSetOrThrowOperationException(home, aAdapationName);
		Request request = null;
		try
		{
			request = XPathExpressionHelper.createRequestForXPath(instance, aXpath);
		}
		catch (Exception ex)
		{
			throw OperationException.createError(ex);
		}
		final RequestResult result = request.execute();
		final Adaptation record;
		try
		{
			record = result.nextAdaptation();
			if (record == null)
			{
				throw OperationException.createError("No record found for xpath '" + aXpath + "'");
			}
			if (result.nextAdaptation() != null)
			{
				throw OperationException
					.createError("More than one record match xpath '" + aXpath + "'");
			}
		}
		finally
		{
			result.close();
		}
		return record;
	}

	public static AdaptationHome getDataSpaceOrThrowOperationException(
		final Repository aRepository,
		final String aBranchName)
		throws OperationException
	{
		final AdaptationHome home = aRepository.lookupHome(HomeKey.forBranchName(aBranchName));
		if (home == null)
		{
			throw OperationException.createError("Data space '" + aBranchName + "' does not exist");
		}
		return home;
	}

	public static Adaptation getDataSetOrThrowOperationException(
		final AdaptationHome dataSpaceRef,
		final String dataSetName)
		throws OperationException
	{
		final Adaptation dataSetRef = dataSpaceRef
			.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		if (dataSetRef == null)
		{
			throw OperationException.createError("Data set '" + dataSetName + "' does not exist");
		}
		return dataSetRef;
	}

	public static Adaptation getDataSetOrThrowOperationException(
		final Repository aRepository,
		final String dataSpaceName,
		final String dataSetName)
		throws OperationException
	{
		AdaptationHome dataSpaceRef = getDataSpaceOrThrowOperationException(
			aRepository,
			dataSpaceName);
		return getDataSetOrThrowOperationException(dataSpaceRef, dataSetName);
	}

	/**
	 * Gets the data set.
	 *
	 * @author MCH
	 *
	 *         Get a data set from a ValueContext, a data space name and a data
	 *         set name.
	 *
	 *         If no data space name is specified, the data set will be searched
	 *         in the current data space.
	 *
	 *         If no data set name is specified, the current data set name will
	 *         be searched.
	 * @param valueContext            The value context
	 * @param dataSpaceName            the data space where to find the data set, null if in current
	 *            data space
	 * @param dataSetName            the data set to find, if null current data set name will be
	 *            searched.
	 * @return the data set
	 * @throws OperationException             if data space or data set not found
	 */
	public static Adaptation getDataSetOrThrowOperationException(
		final ValueContext valueContext,
		final String dataSpaceName,
		final String dataSetName)
		throws OperationException
	{
		AdaptationHome home = valueContext.getHome();
		if (dataSpaceName != null)
		{
			home = getDataSpaceOrThrowOperationException(home.getRepository(), dataSpaceName);
		}

		if (home == null)
		{
			throw OperationException.createError("Data space '" + dataSpaceName + "' not found");
		}

		Adaptation instance;
		if (dataSetName != null)
		{
			AdaptationName name = AdaptationName.forName(dataSetName);
			instance = home.findAdaptationOrNull(name);
			if (instance == null)
			{
				throw OperationException.createError(
					"Data set '" + dataSetName + "' not found in data space '" + dataSpaceName);
			}
		}
		else
		{
			AdaptationName name = valueContext.getAdaptationInstance().getAdaptationName();
			instance = home.findAdaptationOrNull(name);
		}

		return instance;
	}

	public static Adaptation getRecordByPrimaryKey(
		final Repository repository,
		final String dataspaceName,
		final String datasetName,
		final Path tablePath,
		final PrimaryKey primaryKey)
		throws OperationException
	{
		AdaptationHome dataspace = AdaptationUtil
			.getDataSpaceOrThrowOperationException(repository, dataspaceName);
		Adaptation dataset = AdaptationUtil
			.getDataSetOrThrowOperationException(dataspace, datasetName);
		AdaptationTable table = dataset.getTable(tablePath);
		return table.lookupAdaptationByPrimaryKey(primaryKey);
	}

	public static Adaptation getRecordByPrimaryKey(
		final AdaptationHome dataspaceOrSnapshot,
		final String datasetName,
		final Path tablePath,
		final PrimaryKey primaryKey)
		throws OperationException
	{
		Adaptation dataset = AdaptationUtil
			.getDataSetOrThrowOperationException(dataspaceOrSnapshot, datasetName);
		AdaptationTable table = dataset.getTable(tablePath);
		return table.lookupAdaptationByPrimaryKey(primaryKey);
	}

	/**
	 * Within the same table as the provided record context, find the other records that
	 * match the paths/values provided.  If the values provided is null, derive them from
	 * the recordContext
	 * 
	 * @param recordContext
	 * @param paths
	 * @param values
	 * @return the list of matching records
	 */
	public static List<Adaptation> fetchRecordsMatching(
		ValueContext recordContext,
		List<Path> paths,
		List<Object> values)
	{
		return fetchRecordsMatching(recordContext, paths, values, null);
	}

	/**
	 * Within the same table as the provided record context, find the other records that
	 * match the paths/values provided.  If the values provided is null, derive them from
	 * the recordContext
	 * 
	 * @param recordContext
	 * @param paths
	 * @param values
	 * @param sortField
	 * @return the list of matching records
	 */
	public static List<Adaptation> fetchRecordsMatching(
		ValueContext recordContext,
		List<Path> paths,
		List<Object> values,
		Path sortField)
	{
		AdaptationTable table = recordContext.getAdaptationTable();
		RequestResult reqRes;
		if (CollectionUtils.isEmpty(paths))
		{
			Request rq = table.createRequest();
			if (sortField != null)
				rq.setSortCriteria(new RequestSortCriteria().add(sortField));
			reqRes = rq.execute();
		}
		else
		{
			if (values == null)
				values = getPathValues(recordContext, paths);
			// Return null if all values are null
			if (Algorithms.every(values, UnaryPredicate.isNull))
				return null;
			SchemaNode tableOccNode = table.getTableNode().getTableOccurrenceRootNode();
			String predicate = AdaptationUtil.createQueryPredicate(
				tableOccNode,
				paths.toArray(new Path[0]),
				values.toArray(new Object[0]),
				null);
			if (sortField != null)
				reqRes = table
					.createRequestResult(predicate, new RequestSortCriteria().add(sortField));
			else
				reqRes = table.createRequestResult(predicate);
		}
		try
		{
			return getRecords(reqRes);
		}
		finally
		{
			reqRes.close();
		}
	}

	/**
	 * Take a query result and put it into a list.
	 * 
	 * The given request result will not be closed after processing, since the full context
	 * of the request result isn't known here. It is expected that the caller will close
	 * the request result, typically in a <code>finally</code> block.
	 * 
	 * @param rr the request result
	 * @return list of adaptations
	 */
	public static List<Adaptation> getRecords(RequestResult rr)
	{
		List<Adaptation> result = new ArrayList<>();
		getRecords(result, rr);
		return result;
	}

	/**
	 * Take a query result and put it into a set.
	 * 
	 * The given request result will not be closed after processing, since the full context
	 * of the request result isn't known here. It is expected that the caller will close
	 * the request result, typically in a <code>finally</code> block.
	 * 
	 * @param rr the request result
	 * @return set of adaptations
	 */
	public static Set<Adaptation> getRecordSet(RequestResult rr)
	{
		Set<Adaptation> result = new HashSet<>();
		getRecords(result, rr);
		return result;
	}

	/**
	 * Take a query result and put it into a collection.
	 * 
	 * The given request result will not be closed after processing, since the full context
	 * of the request result isn't known here. It is expected that the caller will close
	 * the request result, typically in a <code>finally</code> block.
	 * 
	 * @param intoCollection the collection to put them into
	 * @param rr the request result
	 */
	public static void getRecords(Collection<Adaptation> intoCollection, RequestResult rr)
	{
		Adaptation next;
		while ((next = rr.nextAdaptation()) != null)
		{
			intoCollection.add(next);
		}
	}

	/**
	 * Given a record's valueContext and a list of paths, return a list of values corresponding to
	 * those paths.
	 * 
	 * @param recordContext
	 * @param paths
	 * @return list of values of paths
	 */
	public static List<Object> getPathValues(ValueContext recordContext, List<Path> paths)
	{
		List<Object> result = new ArrayList<>();
		for (Path commonValuePath : paths)
		{
			result.add(recordContext.getValue(commonValuePath));
		}
		return result;
	}

	/**
	 * Given a value context representing a record occurrence, return the primary key string of that
	 * record.
	 * 
	 * @param recordContext
	 * @return string representation of the primary key
	 */
	public static String getOccurrencePrimaryKey(ValueContext recordContext)
	{
		AdaptationTable table = recordContext.getAdaptationTable();
		SchemaNode rootNode = table.getTableNode().getTableOccurrenceRootNode();
		Path[] keySpec = table.getPrimaryKeySpec();
		Object[] keyValues = new Object[keySpec.length];
		SchemaNode[] keyNodes = new SchemaNode[keySpec.length];
		for (int i = 0; i < keySpec.length; i++)
		{
			keyValues[i] = recordContext.getValue(keySpec[i]);
			keyNodes[i] = rootNode.getNode(Path.SELF.add(keySpec[i]));
		}
		return PrimaryKey.parseObjects(keyValues, keyNodes);
	}

	/**
	 * Given a table and path/object pairs, query a return the first matching record.
	 * 
	 * @param table
	 * @param paths
	 * @param values
	 * @param compareOpers
	 * @return Adaptation first record matching the query criteria
	 */
	public static Adaptation getFirstMatching(
		AdaptationTable table,
		Path[] paths,
		Object[] values,
		CompareOper[] compareOpers)
	{
		// TODO: Can we use table.lookupFirstRecordMatchingPredicate? It does the same thing.
		RequestResult rr = table.createRequestResult(
			createQueryPredicate(table.getTableOccurrenceRootNode(), paths, values, compareOpers));
		try
		{
			return rr.nextAdaptation();
		}
		finally
		{
			rr.close();
		}
	}

	/**
	 * Given the reference information (e.g. from a tableRef node), and a current data set,
	 * find the related table.
	 * 
	 * @param dataSet
	 * @param dataSpaceKey
	 * @param dataSetKey
	 * @param tablePath
	 * @return AdaptationTable that is referenced
	 */
	public static AdaptationTable getTable(
		Adaptation dataSet,
		HomeKey dataSpaceKey,
		AdaptationName dataSetKey,
		Path tablePath)
	{
		AdaptationHome dataSpace = dataSet.getHome();
		if (dataSpaceKey != null)
		{
			dataSpace = dataSpace.getRepository().lookupHome(dataSpaceKey);
		}
		if (dataSetKey != null)
		{
			dataSet = dataSpace.findAdaptationOrNull(dataSetKey);
		}
		return dataSet.getTable(tablePath);
	}

	/**
	 * Gets the table.
	 *
	 * @author MCH
	 * @param dataSpace the data space
	 * @param dataSetName the data set
	 * @param tablePath the table path
	 * @return the table
	 * @throws OperationException the operation exception
	 */
	public static AdaptationTable getTable(
		final AdaptationHome dataSpace,
		final String dataSetName,
		final Path tablePath)
		throws OperationException
	{
		Adaptation instance = getDataSetOrThrowOperationException(dataSpace, dataSetName);
		if (instance == null)
		{
			throw OperationException.createError(
				"Data set '" + dataSetName + "' not found in data space '" + dataSpace);
		}

		return instance.getTable(tablePath);
	}

	/**
	 * Create a list of group objects (for a repeating group) given a schema
	 * node (must be a TNode) and an array of object arrays
	 *
	 * @param node
	 *            the schema node representing the repeating group
	 * @param groups
	 *            the array of arrays of values
	 */
	public static List<GenericComplexObject> createRepeatingGroup(
		SchemaNode node,
		Object[]... groups)
	{
		if (node instanceof TNode)
		{
			TNode tnode = (TNode) node;
			List<GenericComplexObject> result = new ArrayList<>();
			for (Object[] group : groups)
			{
				GenericComplexObject gco = new GenericComplexObject();
				gco._initialize(tnode);
				for (int i = 0; i < group.length; i++)
				{
					gco.setValueAt(i, group[i]);
				}
				result.add(gco);
			}
			return result;
		}
		else
		{
			throw new IllegalArgumentException("Node to construct repeating group must be a TNode");
		}
	}

	/** Return a json representation of a complex object */
	@Deprecated
	public static String jsonFor(GenericComplexObject obj, SchemaNode complexNode)
	{
		return JSONUtil.jsonFor(obj, complexNode);
	}

	/**
	 * Given a record context and a list field node, answer whether the list field value has
	 * duplicates
	 */
	public static boolean hasDuplicates(ValueContext vc, SchemaNode field)
	{
		return hasDuplicates(vc, field, false);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Given a record context and a list field node, answer whether the list field value has
	 * duplicates
	 * allow option for lists containing String values to perform Case Insensitive check
	 */
	public static boolean hasDuplicates(ValueContext vc, SchemaNode field, Boolean caseInsensitive)
	{
		List list = (List) vc.getValue(field);
		if (list == null || list.isEmpty())
			return false;
		Set set;
		if (field.isComplex() && field.getJavaBeanClass() == null)
		{
			set = new HashSet();
			for (Object obj : list)
			{
				set.add(jsonFor((GenericComplexObject) obj, field));
			}
		}
		else if (caseInsensitive)
		{
			set = new HashSet();
			for (Object entry : list)
			{
				if (entry instanceof String)
				{
					set.add(((String) entry).toLowerCase());
				}
			}
		}
		else
		{
			set = new HashSet(list);
		}
		return set.size() < list.size();
	}

	/**
	 * Given a record, a field node representing a foreign key, and an optional filter (well formed
	 * xpath fk-filter),
	 * return the RequestResult of performing the query against the fk-table applying the filter
	 * (could have local record
	 * value parameters, relative to the fkNode)
	 * 
	 * @param record
	 * @param fkNode
	 * @param xpathForRef
	 * @return RequestResult result of the query
	 */
	public static RequestResult performFKFilterQuery(
		Adaptation record,
		SchemaNode fkNode,
		String xpathForRef)
	{
		SchemaFacetTableRef ref = fkNode.getFacetOnTableReference();
		AdaptationTable table = ref.getTable(record.createValueContext());
		if (xpathForRef == null)
			return table.createRequest().execute();
		StringTokenizer st = new StringTokenizer(xpathForRef);
		StringBuilder query = new StringBuilder();
		while (st.hasMoreTokens())
		{
			String nextToken = st.nextToken();
			if (nextToken.startsWith("${"))
			{
				Path path = Path.parse(
					StringUtils.substringBefore(StringUtils.substringAfter(nextToken, "${"), "}"));
				SchemaNode node = fkNode.getNode(path);
				query.append(AdaptationUtil.valueString(node, record.get(node)));
			}
			else
			{
				query.append(nextToken);
			}
			query.append(" ");
		}
		return table.createRequestResult(query.toString());
	}

	/** Find a matching record or create one */
	public static Adaptation findOrCreate(
		ProcedureContext pContext,
		AdaptationTable table,
		Path[] matchPaths,
		Map<Path, Object> createMap)
		throws OperationException
	{
		Adaptation result = find(table, matchPaths, createMap);
		if (result == null)
		{
			CreateRecordProcedure crp = new CreateRecordProcedure(table, createMap);
			crp.setAllPrivileges(true);
			crp.execute(pContext);
			result = crp.getCreatedRecord();
		}
		return result;
	}

	/** Find a matching record or create one by duplicating the one provided */
	public static Adaptation findOrDuplicate(
		ProcedureContext pContext,
		AdaptationTable table,
		Path[] matchPaths,
		Map<Path, Object> dupeMap,
		Adaptation origRecord)
		throws OperationException
	{
		Adaptation result = find(table, matchPaths, dupeMap);
		if (result == null)
		{
			DuplicateRecordProcedure dupeProc = new DuplicateRecordProcedure(origRecord, dupeMap);
			dupeProc.setAllPrivileges(true);
			dupeProc.execute(pContext);
			result = dupeProc.getCreatedRecord();
		}
		return result;
	}

	private static Adaptation find(
		AdaptationTable table,
		Path[] matchPaths,
		Map<Path, Object> createMap)
	{
		boolean key = false;
		Object[] matchValues;
		if (matchPaths == null)
		{
			//use key fields to match
			Path[] keySpec = table.getPrimaryKeySpec();
			matchPaths = new Path[keySpec.length];
			for (int i = 0; i < keySpec.length; i++)
			{
				matchPaths[i] = Path.SELF.add(keySpec[i]);
			}
			//check that each matchPath is in the createMap
			key = true;
			for (int i = 0; i < matchPaths.length; i++)
			{
				if (!createMap.containsKey(matchPaths[i]))
				{
					key = false;
					matchPaths = null;
					break;
				}
			}
		}
		if (matchPaths == null)
		{
			matchPaths = new Path[createMap.size()];
			matchValues = new Object[matchPaths.length];
			int i = 0;
			for (Map.Entry<Path, Object> entry : createMap.entrySet())
			{
				matchPaths[i] = entry.getKey();
				matchValues[i] = entry.getValue();
				i++;
			}
		}
		else
		{
			matchValues = new Object[matchPaths.length];
			for (int i = 0; i < matchPaths.length; i++)
			{
				matchValues[i] = createMap.get(matchPaths[i]);
			}
		}
		if (key)
			return table.lookupAdaptationByPrimaryKey(table.computePrimaryKey(matchValues));
		return getFirstMatching(table, matchPaths, matchValues, null);
	}

	/**
	 * Given a path to a comparable field, return a comparator to sort by that field value
	 */
	public static Comparator<Adaptation> createComparator(Path path)
	{
		return new FieldValueComparator(path);
	}

	public static class FieldValueComparator implements Comparator<Adaptation>
	{
		private final Path path;

		public FieldValueComparator(Path path)
		{
			this.path = path;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(Adaptation o1, Adaptation o2)
		{
			Comparable val1 = o1 != null ? (Comparable) o1.get(path) : null;
			Comparable val2 = o2 != null ? (Comparable) o2.get(path) : null;
			if (val1 == null && val2 == null)
				return 0;
			if (val1 == null)
				return -1;
			if (val2 == null)
				return 1;
			return val1.compareTo(val2);
		}

	}

	/**
	 * Given a path to a field and a value, return a predicate that tests whether the Adaptation's
	 * value for that field matches the value specified.
	 */
	public static UnaryPredicate<Adaptation> createFieldMatchPredicate(Path path, Object value)
	{
		return new FieldValuePredicate(path, value);
	}

	public static class FieldValuePredicate extends UnaryPredicate<Adaptation>
	{
		private final Path path;
		private final Object value;

		public FieldValuePredicate(Path path, Object value)
		{
			this.path = path;
			this.value = value;
		}

		@Override
		public boolean test(Adaptation object)
		{
			return Objects.equals(value, object.get(path));
		}

	}

	public static Path getRelativePath(ValueContext context, Path path)
	{
		Path contextPath = context.getNode().getPathInAdaptation();
		int parentCount = contextPath.getSize();
		for (int i = 0; i < parentCount; i++)
		{
			path = Path.PARENT.add(path);
		}
		return path;
	}

	/**
	 * An import component of any 'refactor' effort.  Essentially, take all references to the old object and
	 * redirect to the new object.  Specify if you want to copy and leave the old value also, or if you are
	 * planning to delete the old one and you want to clean up.
	 * @param pContext a procedure context in which to create/delete records
	 * @param fromTable AdaptationTable with potential records pointing to old record
	 * @param fkPath The foreign key path to the old record
	 * @param oldRecord the record being copied/replaced
	 * @param newRecord the new record
	 * @throws OperationException if problems occur creating/deleting records
	 */
	public static void redirectRefs(
		ProcedureContext pContext,
		AdaptationTable fromTable,
		Path fkPath,
		Adaptation oldRecord,
		Adaptation newRecord,
		boolean delete)
		throws OperationException
	{
		Path[] keySpec = fromTable.getPrimaryKeySpec();
		boolean key = CollectionUtils.indexInArray(keySpec, fkPath.getSubPath(1)) >= 0;
		RequestResult rr = fromTable.createRequestResult(
			AdaptationUtil.createQueryPredicate(
				fromTable.getTableOccurrenceRootNode(),
				fkPath,
				oldRecord.getOccurrencePrimaryKey().format(),
				null));
		try
		{
			Map<Path, Object> pvm = new HashMap<>();
			pvm.put(fkPath, newRecord.getOccurrencePrimaryKey().format());

			DuplicateRecordProcedure dupeProc = new DuplicateRecordProcedure();
			dupeProc.setPathValueMap(pvm);
			dupeProc.setAllPrivileges(true);
			DeleteRecordProcedure drp = new DeleteRecordProcedure();
			drp.setAllPrivileges(true);
			ModifyValuesProcedure mvp = new ModifyValuesProcedure();
			mvp.setPathValueMap(pvm);
			mvp.setAllPrivileges(true);
			Adaptation next;
			while ((next = rr.nextAdaptation()) != null)
			{
				if (!delete || key)
				{
					dupeProc.setAdaptation(next);
					dupeProc.execute(pContext);
					if (delete)
					{
						drp.setAdaptation(next);
						drp.execute(pContext);
					}
				}
				else
				{
					mvp.setAdaptation(next);
					mvp.execute(pContext);
				}
			}
		}
		finally
		{
			rr.close();
		}
	}

	public static UnaryPredicate<Adaptation> buildFieldMatchPredicate(Path path, Object value)
	{
		return new FieldMatchesPredicate(path, value);
	}

	public static class FieldMatchesPredicate extends UnaryPredicate<Adaptation>
	{
		public FieldMatchesPredicate(Path field, Object value)
		{
			super();
			this.field = field;
			this.value = value;
		}

		private final Path field;
		private final Object value;

		@Override
		public boolean test(Adaptation object)
		{
			return Objects.equals(value, object.get(field));
		}
	}

	/**
	 * Gets the node.
	 *
	 * @author MCH
	 * @param pAdaptation
	 *            either a record or an instance
	 * @param pPath
	 *            absolute path from the adaptation root
	 * @return the schema node located a the given path
	 */
	public static SchemaNode getNode(final Adaptation pAdaptation, final Path pPath)
	{
		return pAdaptation.getSchemaNode().getNode(pPath);
	}

	/**
	 * Gets the node.
	 *
	 * @author MCH
	 * @param pValueContext
	 *             a value context
	 * @param pPath
	 *            absolute path from the adaptation root
	 * @return the schema node located a the given path
	 */
	public static SchemaNode getNode(final ValueContext pValueContext, final Path pPath)
	{
		return pValueContext.getNode().getNode(pPath);
	}

	public static Map<AdaptationTable, String> deriveTableNames(
		Collection<AdaptationTable> tables,
		String separator,
		Locale locale)
	{
		// first iterate over tables grabbing simple names to see if there are
		// duplicates
		Map<AdaptationTable, String> result = new HashMap<>();
		Map<String, List<AdaptationTable>> names = new HashMap<>();
		for (AdaptationTable table : tables)
		{
			String simpleName = table.getTableNode().getLabel(locale);
			List<AdaptationTable> tablesWithName = names.get(simpleName);
			if (tablesWithName == null)
			{
				result.put(table, simpleName);
				tablesWithName = new ArrayList<>();
				names.put(simpleName, tablesWithName);
			}
			else
			{
				String qualName = table.getTableNode().getParent().getLabel(locale) + separator
					+ simpleName;
				result.put(table, qualName);
				for (AdaptationTable otherTable : tablesWithName)
				{
					String name = result.get(otherTable);
					if (!name.contains(separator))
					{
						qualName = otherTable.getTableNode().getParent().getLabel(locale)
							+ separator + simpleName;
						result.put(otherTable, qualName);
					}
				}
			}
			tablesWithName.add(table);
		}
		return result;
	}

	/**
	 * Return the history records related to the given record.  If history is not enabled,
	 * this method will return null.
	 * @param record
	 * @return list of history records related to the specified record
	 */
	public static List<Adaptation> getHistory(Adaptation record)
	{
		AdaptationTable table = record.getContainerTable();
		AdaptationTable historyTable = table.getHistory();
		if (historyTable == null)
			return null;
		Path[] keySpec = table.getPrimaryKeySpec();
		Object[] keyVals = new Object[keySpec.length];
		for (int i = 0; i < keySpec.length; i++)
		{
			Path path = keySpec[i];
			keySpec[i] = Path.SELF.add(path);
			keyVals[i] = record.get(path);
		}
		RequestResult rr = historyTable.createRequestResult(
			createQueryPredicate(
				historyTable.getTableOccurrenceRootNode(),
				keySpec,
				keyVals,
				null));
		try
		{
			return getRecords(rr);
		}
		finally
		{
			rr.close();
		}
	}

	/**
	 * Return a hyperlink url to the record, e.g.
	 * http://localhost:8080/ebx/?functionalCategory=data&branch=MyMasterDataSpace&instance=MyDataSet&xpath=%2Froot%2FMyTable%5B.%2Fid%3D%271%27
	 * @param record
	 * @return the permalink to that record
	 */
	public static String getRecordURL(Adaptation record)
	{
		AdaptationHome home = record.getHome();
		Repository repo = home.getRepository();
		Adaptation workflowAdmin = AdminUtil.getWorkflowAdminConfigurationDataSet(repo);
		String ebxUrl = workflowAdmin
			.getString(WfiPaths._TechnicalConfiguration_Mail_UrlDefinition_Url);
		if (ebxUrl == null)
		{
			ebxUrl = "http://localhost:8080/ebx/";
		}
		StringBuilder urlBuilder = new StringBuilder(ebxUrl);
		urlBuilder.append("?functionalCategory=data");
		if (home.isBranch())
		{
			urlBuilder.append("&branch=");
		}
		else
		{
			urlBuilder.append("&version=");
		}
		urlBuilder.append(home.getKey().getName());
		urlBuilder.append("&instance=")
			.append(record.getContainer().getAdaptationName().getStringName());
		String xpath = record.toXPathExpression();
		try
		{
			xpath = URLEncoder.encode(xpath, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
		}
		urlBuilder.append("&xpath=").append(xpath);
		return urlBuilder.toString();
	}

	/**
	 * Given a table occurence schema node, collect all its inherited descendents
	 * @param node
	 * @param paths
	 */
	public static void collectInheritedFields(SchemaNode node, List<Path> paths)
	{
		if (node.getInheritanceProperties() != null)
		{
			paths.add(node.getPathInAdaptation());
			return;
		}
		SchemaNode[] children = node.getNodeChildren();
		for (SchemaNode childNode : children)
		{
			collectInheritedFields(childNode, paths);
		}
	}

	/**
	 * For a field path representing an inherited field, find the related record and return the value
	 * of the inherit from node.
	 * @param record
	 * @param inheritField
	 * @return the value this record would inherit if it weren't overriden
	 */
	public static Object getInheritValue(Adaptation record, Path inheritField)
	{
		SchemaNode recordNode = record.getSchemaNode();
		SchemaInheritanceProperties inhProps = recordNode.getNode(inheritField)
			.getInheritanceProperties();
		Adaptation inheritFromRecord = inhProps.getLinkedRecord(record);
		if (inheritFromRecord == null)
		{
			return null;
		}
		return inheritFromRecord.get(inhProps.getInheritedNode());
	}

	/**
	 * Get the records that refer to the given record. This is done regardless of whether the current
	 * user has permission to see those records.
	 * 
	 * @see SchemaNode#getRelationships()
	 * 
	 * @param record
	 *            the record
	 * @param includeExternalDataSets
	 *            whether to include data sets other than the one the record is in when looking for references
	 * @param includedExternalDataSpaces
	 *            a list of data spaces to limit the search to, in addition to the data space that the record is in,
	 *            or <code>null</code> to search all data spaces.
	 *            For example, if the record is potentially referenced by records in data spaces A, B,
	 *            and a child data space of B, and only B is specified, then A and the child of B will not be searched.
	 *            The record's own data space can be included as well: If the record might be referenced by
	 *            the master data space of the record's data space, or if another data set in the same data space could
	 *            reference the record, then you may want to list the master data space.
	 *            Keep in mind, performance could be impacted if there are many data spaces and all are being searched
	 *            (i.e. <code>null</code> is specified).
	 * @return the set of records referencing the given record
	 */
	public static Set<Adaptation> getReferrers(
		Adaptation record,
		boolean includeExternalDataSets,
		Set<String> includedExternalDataSpaces)
	{
		return doGetReferrers(false, record, includeExternalDataSets, includedExternalDataSpaces);
	}

	/**
	 * Get the first record found that references the given record, or <code>null</code> if there are no references.
	 * There is no guarantee to the order of searching for records, so it should not be assumed that in multiple invocations,
	 * the same record will be found first each time.
	 * 
	 * This is logically equivalent to calling {@link #getReferrers(Adaptation, boolean, Set)} and reading the first record
	 * from the returned set, however this is much more efficient because it can stop searching as soon as it finds one,
	 * rather than fill the whole set in.
	 * 
	 * If all you want to know is "Does any record refer to this one?" then you should call this method and check if result is
	 * <code>null</code>. The returned record can provide some info to include in an error message to the user to give a hint as
	 * to what is referring to the record, although it won't provide the complete picture of everything referencing it.
	 * 
	 * @param record
	 *            the record
	 * @param includeExternalDataSets
	 *            whether to include data sets other than the one the record is in when looking for references
	 * @param includedExternalDataSpaces
	 *            a list of data spaces to limit the search to, in addition to the data space that the record is in,
	 *            or <code>null</code> to search all data spaces.
	 *            For example, if the record is potentially referenced by records in data spaces A, B,
	 *            and a child data space of B, and only B is specified, then A and the child of B will not be searched.
	 *            The record's own data space can be included as well: If the record might be referenced by
	 *            the master data space of the record's data space, or if another data set in the same data space could
	 *            reference the record, then you may want to list the master data space.
	 *            Keep in mind, performance could be impacted if there are many data spaces and all are being searched
	 *            (i.e. <code>null</code> is specified).
	 * @return the first found record referencing the given record, or <code>null</code> if none are found
	 */
	public static Adaptation getFirstFoundReferrer(
		Adaptation record,
		boolean includeExternalDataSets,
		Set<String> includedExternalDataSpaces)
	{
		Set<Adaptation> referrers = doGetReferrers(
			true,
			record,
			includeExternalDataSets,
			includedExternalDataSpaces);
		if (referrers.isEmpty())
		{
			return null;
		}
		return referrers.iterator().next();
	}

	private static Set<Adaptation> doGetReferrers(
		boolean returnFirstFound,
		Adaptation record,
		boolean includeExternalDataSets,
		Set<String> includedExternalDataSpaces)
	{
		Set<Adaptation> referrers = new LinkedHashSet<>();
		SchemaNode tableNode = record.getSchemaNode().getTableNode();
		SchemaNodeRelationships relationships = tableNode.getRelationships();

		// Look through the relationships from the same data set
		Iterator<ReverseRelationshipIntraDataset> intraDataSetRelationshipIter = relationships
			.getIntraDatasetReverseRelationships()
			.iterator();
		while (!doneFindingReferrers(referrers, returnFirstFound)
			&& intraDataSetRelationshipIter.hasNext())
		{
			ReverseRelationshipIntraDataset intraDataSetRelationship = intraDataSetRelationshipIter
				.next();
			if (isReverseRelationshipForeignKeyToTable(intraDataSetRelationship, tableNode))
			{
				ReverseRelationshipResult relationshipResult = intraDataSetRelationship
					.getReferringRecords(record);
				referrers.addAll(getReferrersForDataSet(relationshipResult, returnFirstFound));
			}
		}

		// If we're including external data sets and we're not done searching yet
		if (includeExternalDataSets && !doneFindingReferrers(referrers, returnFirstFound))
		{
			Iterator<ReverseRelationshipInterDataset> interDataSetRelationshipIter = relationships
				.getInterDatasetReverseRelationships()
				.iterator();
			while (!doneFindingReferrers(referrers, returnFirstFound)
				&& interDataSetRelationshipIter.hasNext())
			{
				ReverseRelationshipInterDataset interDataSetRelationship = interDataSetRelationshipIter
					.next();
				if (isReverseRelationshipForeignKeyToTable(interDataSetRelationship, tableNode))
				{
					ReverseRelationshipResultsInterDataset interDataSetRelationshipResults = interDataSetRelationship
						.getReferringRecords(record);
					// If the set of external data spaces to include is null, then it means we're searching all data spaces
					if (includedExternalDataSpaces == null)
					{
						Iterator<ReverseRelationshipResultInDataspace> relationshipResultByDataSpaceIter = interDataSetRelationshipResults
							.getResultsByDataspaces()
							.iterator();
						while (!doneFindingReferrers(referrers, returnFirstFound)
							&& relationshipResultByDataSpaceIter.hasNext())
						{
							ReverseRelationshipResultInDataspace relationshipByDataSpaceResult = relationshipResultByDataSpaceIter
								.next();
							referrers.addAll(
								getReferrersForDataSpace(
									relationshipByDataSpaceResult,
									returnFirstFound));
						}
					}
					// Otherwise we are only looking at that list of data spaces
					else
					{
						Repository repo = record.getHome().getRepository();
						Iterator<String> dataSpaceIter = includedExternalDataSpaces.iterator();
						while (!doneFindingReferrers(referrers, returnFirstFound)
							&& dataSpaceIter.hasNext())
						{
							String dataSpace = dataSpaceIter.next();
							HomeKey dataSpaceKey = HomeKey.forBranchName(dataSpace);
							if (repo.lookupHome(dataSpaceKey) == null)
							{
								// It's possible that the specified data space doesn't exist. That likely is a developer error,
								// but there certainly are no references from a non-existent data space, so don't think should
								// error out the whole method call. Just print a warning.
								LoggingCategory.getKernel()
									.warn(
										"Data space " + dataSpace
											+ " is specified as a data space to search for referrers, but is not found.");
							}
							else
							{
								// Get just the relationships for the current data space from the set that we're iterating over
								ReverseRelationshipResultInDataspace relationshipByDataSpaceResult = interDataSetRelationshipResults
									.getResultInDataspace(dataSpaceKey);
								// Will be null when there's no result in that data space
								if (relationshipByDataSpaceResult != null)
								{
									referrers.addAll(
										getReferrersForDataSpace(
											relationshipByDataSpaceResult,
											returnFirstFound));
								}
							}
						}
					}
				}
			}
		}
		return referrers;
	}

	// Whether to include the reverse relationship when looking for referrers.
	// Reverse relationships include records that reference this record at all, including
	// associations. It's not just records that have a foreign key to this record.
	// We want just those reverse relationships with an explicit relationship over a
	// foreign key reference back to this table.
	private static boolean isReverseRelationshipForeignKeyToTable(
		ReverseRelationship relationship,
		SchemaNode tableNode)
	{
		ExplicitRelationship explicitRelationship = relationship.getExplicitRelationship();
		SchemaNode ownerNode = explicitRelationship.getOwnerNode();
		SchemaFacetTableRef tableRef = ownerNode.getFacetOnTableReference();
		// If the reverse relationship's explicit relationship has an owner node that is a foreign
		// key, and that foreign key is to the same table, then include it
		return tableRef != null && relationship.getTableNode().equals(tableRef.getTableNode());
	}

	// A simple utility method to determine if we need to stop finding referrers,
	// because the logic was being called from multiple places.
	private static boolean doneFindingReferrers(Set<Adaptation> referrers, boolean returnFirstFound)
	{
		return returnFirstFound && !referrers.isEmpty();
	}

	// Get referrers from a data space relationship result (which contains the
	// relationship results from the data sets in a data space).
	// If returnFirstFound is true, the resulting set will have at most one record.
	private static Set<Adaptation> getReferrersForDataSpace(
		ReverseRelationshipResultInDataspace relationshipResultInDataSpace,
		boolean returnFirstFound)
	{
		Set<Adaptation> referrers = new LinkedHashSet<>();
		Iterator<ReverseRelationshipResult> resultByDataSetIter = relationshipResultInDataSpace
			.getResultByDatasets()
			.iterator();
		while ((!returnFirstFound || referrers.isEmpty()) && resultByDataSetIter.hasNext())
		{
			ReverseRelationshipResult resultByDataSet = resultByDataSetIter.next();
			referrers.addAll(getReferrersForDataSet(resultByDataSet, returnFirstFound));
		}
		return referrers;
	}

	// Get referrers from a relationship result, which is for a data set.
	// If returnFirstFound is true, the resulting set will have at most one record.
	private static Set<Adaptation> getReferrersForDataSet(
		ReverseRelationshipResult relationshipResult,
		boolean returnFirstFound)
	{
		Set<Adaptation> referrers = new LinkedHashSet<>();
		RequestResult requestResult = relationshipResult.getResult();
		try
		{
			// If returning the first found, just add the first record from the result.
			// Otherwise, add all records from the result.
			if (returnFirstFound)
			{
				Adaptation firstAdaptation = requestResult.nextAdaptation();
				if (firstAdaptation != null)
				{
					referrers.add(firstAdaptation);
				}
			}
			else
			{
				referrers.addAll(getRecords(requestResult));
			}
		}
		finally
		{
			requestResult.close();
		}
		return referrers;
	}

	/**
	 * Given a data set, return the updated and/or created records compared to its initial snapshot,
	 * for all tables in the data set.
	 * 
	 * This is equivalent to calling {@link #getChangedRecords(Adaptation, Set)} with <code>null</code>
	 * for the table paths.
	 * 
	 * @param dataset the data set
	 * @return list of updated/created records for the data set
	 */
	public static List<Adaptation> getChangedRecords(Adaptation dataset)
	{
		return getChangedRecords(dataset, null);
	}

	/**
	/**
	 * Given a data set, return the updated and/or created records compared to its initial snapshot,
	 * for the specified tables in the data set.
	 * 
	 * This is equivalent to calling {@link #getChangedRecords(Adaptation, Set, boolean)} with <code>false</code>
	 * for resolvedMode.
	 * 
	 * @param dataset the data set
	 * @param tablePaths the table paths, or <code>null</code> if it should include all tables
	 * @return list of updated/created records for the data set
	 */
	public static List<Adaptation> getChangedRecords(Adaptation dataset, Set<Path> tablePaths)
	{
		return getChangedRecords(dataset, tablePaths, false);
	}

	/**
	 * Given a data set, return the updated and/or created records compared to its initial snapshot.
	 * If table paths are specified, only return changes in those tables.
	 * 
	 * @param dataset the data set
	 * @param tablePaths the table paths, or <code>null</code> if it should include all tables
	 * @param resolvedMode use false for best performance, use true if inheritance and computed attributes need to be taken into account
	 * @return list of updated/created records for the data set
	 */
	public static List<Adaptation> getChangedRecords(
		Adaptation dataset,
		Set<Path> tablePaths,
		boolean resolvedMode)
	{
		return getChangedRecords(dataset, tablePaths, resolvedMode, null);
	}

	/**
	 * Given a data set, return the updated and/or created records compared to its initial snapshot.
	 * If table paths are specified, only return changes in those tables.
	 * If a record should be included only in certain circumstances, a function can be passed in that contains
	 * that logic.
	 * 
	 * @param dataset the data set
	 * @param tablePaths the table paths, or <code>null</code> if it should include all tables
	 * @param resolvedMode use false for best performance, use true if inheritance and computed attributes need to be taken into account
	 * @param includeChange a function to determine if a particular record's change should be included, or <code>null</code> if no
	 *        custom logic needs to be provided
	 * @return list of updated/created records for the data set
	 */
	public static List<Adaptation> getChangedRecords(
		Adaptation dataset,
		Set<Path> tablePaths,
		boolean resolvedMode,
		UnaryFunction<DifferenceBetweenOccurrences, Boolean> includeChange)
	{
		return getChangedRecords(dataset, tablePaths, resolvedMode, includeChange, false);
	}

	/**
	 * Given a data set, return the updated and/or created records compared to its initial snapshot.
	 * If table paths are specified, only return changes in those tables.
	 * If a record should be included only in certain circumstances, a function can be passed in that contains
	 * that logic.
	 * 
	 * @param dataset the data set
	 * @param tablePaths the table paths, or <code>null</code> if it should include all tables
	 * @param resolvedMode use false for best performance, use true if inheritance and computed attributes need to be taken into account
	 * @param includeChange a function to determine if a particular record's change should be included, or <code>null</code> if no
	 *        custom logic needs to be provided
	 * @param includeUpdatedRecordsOnly set to true if you only want updated records returned as opposed to created records also
	 * @return list of updated/created records for the data set
	 */
	public static List<Adaptation> getChangedRecords(
		Adaptation dataset,
		Set<Path> tablePaths,
		boolean resolvedMode,
		UnaryFunction<DifferenceBetweenOccurrences, Boolean> includeChange,
		boolean includeUpdatedRecordsOnly)
	{
		AdaptationHome home = dataset.getHome();
		AdaptationHome initHome = home.getParent();
		Adaptation initDataset = initHome.findAdaptationOrNull(dataset.getAdaptationName());
		List<Adaptation> changedRecords = new ArrayList<>();
		DifferenceBetweenInstances diffs = DifferenceHelper
			.compareInstances(dataset, initDataset, resolvedMode);
		if (diffs == null || diffs.isEmpty())
			return changedRecords;
		List<DifferenceBetweenTables> tableDiffs = diffs.getDeltaTables();
		if (tableDiffs == null)
			return changedRecords;
		for (DifferenceBetweenTables tableDiff : tableDiffs)
		{
			// Doesn't matter if you get the path on left or right, they're both the same
			Path tablePathOnLeft = tableDiff.getPathOnLeft();
			if (tablePathOnLeft == null || tablePaths == null
				|| tablePaths.contains(tablePathOnLeft))
			{
				List<ExtraOccurrenceOnLeft> adds = tableDiff.getExtraOccurrencesOnLeft();
				if (adds != null && !includeUpdatedRecordsOnly)
				{
					for (ExtraOccurrenceOnLeft newRecord : adds)
					{
						changedRecords.add(newRecord.getExtraOccurrence());
					}
				}
				List<DifferenceBetweenOccurrences> deltas = tableDiff.getDeltaOccurrences();
				if (deltas != null)
				{
					for (DifferenceBetweenOccurrences delta : deltas)
					{
						boolean addToList;
						// If no custom function was supplied, then add it to the list
						if (includeChange == null)
						{
							addToList = true;
						}
						// Otherwise, only add it to the list if the function says to include it
						else
						{
							Boolean includeChangeResult = includeChange.evaluate(delta);
							addToList = includeChangeResult != null
								&& includeChangeResult.booleanValue();
						}
						if (addToList)
						{
							changedRecords.add(delta.getContentOnLeft());
						}
					}
				}
			}
		}
		return changedRecords;
	}

	/**
	 * Given a data set, return the deleted records compared to its initial snapshot.
	 * The deleted records will be in the initial snapshot's data set (since they don't exist in child).
	 * 
	 * This is equivalent to calling {@link #getDeletedRecords(Adaptation, Set)} with <code>null</code>
	 * for the table paths.
	 * 
	 * @param dataset the data set
	 * @return list of deleted records for the data set
	 */
	public static List<Adaptation> getDeletedRecords(Adaptation dataset)
	{
		return getDeletedRecords(dataset, null);
	}

	/**
	 * Given a data set, return the deleted records compared to its initial snapshot.
	 * The deleted records will be in the initial snapshot's data set (since they don't exist in child).
	 * 
	 * @param dataset the data set
	 * @param tablePaths the table paths, or <code>null</code> if it should include all tables
	 * @return list of deleted records for the data set
	 */
	public static List<Adaptation> getDeletedRecords(Adaptation dataset, Set<Path> tablePaths)
	{
		AdaptationHome home = dataset.getHome();
		AdaptationHome initHome = home.getParent();
		Adaptation initDataset = initHome.findAdaptationOrNull(dataset.getAdaptationName());
		List<Adaptation> deletedRecords = new ArrayList<>();
		DifferenceBetweenInstances diffs = DifferenceHelper
			.compareInstances(dataset, initDataset, false);
		if (diffs == null || diffs.isEmpty())
			return deletedRecords;
		List<DifferenceBetweenTables> tableDiffs = diffs.getDeltaTables();
		if (tableDiffs == null)
			return deletedRecords;
		for (DifferenceBetweenTables tableDiff : tableDiffs)
		{
			Path tablePathOnRight = tableDiff.getPathOnRight();
			if (tablePathOnRight == null || tablePaths == null
				|| tablePaths.contains(tablePathOnRight))
			{
				List<ExtraOccurrenceOnRight> deletes = tableDiff.getExtraOccurrencesOnRight();
				if (deletes != null)
				{
					for (ExtraOccurrenceOnRight deletedRecord : deletes)
					{
						deletedRecords.add(deletedRecord.getExtraOccurrence());
					}
				}
			}
		}
		return deletedRecords;
	}
}
