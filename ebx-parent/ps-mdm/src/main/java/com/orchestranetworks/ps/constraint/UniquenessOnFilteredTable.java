package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
/**
 * @author MCH
 * 
 * <osd:constraint class="com.orchestranetworks.ps.constraint.ontable.UniquenessOnFilteredTable">
 *		<uniqueFields>./field1,./field2</uniqueFields>
 * 		<filter>an xpath filter</filter>
 * 		<severity>F=Fatal, E=Error (default), W=Warning, I=Information</severity>
 * 		<recordLevelSeverity>can optionally provide a different severity level for the Record Level check</recordLevelSeverity>
 * </osd:constraint>
 *
 */
public class UniquenessOnFilteredTable implements ConstraintOnTableWithRecordLevelCheck
{
	private String uniqueFields;
	private String filter;
	private String severity;
	private Severity defaultSeverity = Severity.ERROR;
	private String recordLevelSeverity;
	private Severity defaultRecordLevelSeverity;

	@Override
	public void checkRecord(final ValueContextForValidationOnRecord pContext)
	{
		StringBuilder predicate = new StringBuilder();
		StringBuilder valueInMessage = new StringBuilder();
		StringBuilder fieldsInMessage = new StringBuilder();
		String value = null;
		SchemaNode node = null;

		String[] uniqueFieldsPaths = uniqueFields.split(",");
		predicate.append(filter + " and ");
		for (String path : uniqueFieldsPaths)
		{
			node = pContext.getRecord().getNode(Path.parse(path));
			value = node.formatToXsString(pContext.getRecord().getValue(Path.parse(path)));
			valueInMessage.append(value + " - ");
			fieldsInMessage.append(path + " - ");
			predicate.append(path + " = '" + value + "'");
			predicate.append(" and ");
		}
		predicate.delete(predicate.length() - 5, predicate.length());
		valueInMessage.delete(valueInMessage.length() - 3, valueInMessage.length());
		fieldsInMessage.delete(fieldsInMessage.length() - 3, fieldsInMessage.length());

		RequestResult result = pContext.getTable().createRequestResult(predicate.toString());
		try
		{
			if (result.isSizeGreaterOrEqual(2))
			{
				if (uniqueFieldsPaths.length == 1)
				{
					String message = "[" + fieldsInMessage + "] value '" + value
						+ "' must be unique in the table.";
					pContext.addMessage(
						node,
						AdaptationUtil.createUserMessage(
							message,
							getSeverityFromRecordLevel(pContext.getRecord())));
				}
				else
				{
					String message = "Set of values [" + valueInMessage + "] from fields ["
						+ fieldsInMessage + "] must be unique in the table.";
					pContext.addMessage(
						AdaptationUtil.createUserMessage(
							message,
							getSeverityFromRecordLevel(pContext.getRecord())));
				}
			}
		}
		finally
		{
			result.close();
		}
	}
	@Override
	public void checkTable(final ValueContextForValidationOnTable pContext)
	{

		RequestSortCriteria criteria = new RequestSortCriteria();

		StringBuilder fieldsInMessage = new StringBuilder();
		SchemaNode node = null;
		String[] uniqueFieldsPaths = uniqueFields.split(",");
		for (String path : uniqueFieldsPaths)
		{
			node = pContext.getTable().getTableOccurrenceRootNode().getNode(Path.parse(path));
			criteria.add(Path.parse(path));
			fieldsInMessage.append(path + " - ");
		}
		fieldsInMessage.delete(fieldsInMessage.length() - 3, fieldsInMessage.length());

		Request request = pContext.getTable().createRequest();
		request.setXPathFilter(filter);
		request.setSortCriteria(criteria);
		RequestResult result = request.execute();

		Adaptation previousRecord = null;
		Adaptation record = null;
		List<String> previousValues = null;
		List<Adaptation> duplicates = new ArrayList<>();
		List<String> values = null;
		StringBuilder valueInMessage = null;

		try
		{
			while ((record = result.nextAdaptation()) != null)
			{
				valueInMessage = new StringBuilder();
				values = new ArrayList<>();
				for (String path : uniqueFieldsPaths)
				{
					node = pContext.getTable().getTableOccurrenceRootNode().getNode(
						Path.parse(path));
					String value = node.formatToXsString(record.get(Path.parse(path)));
					values.add(value);
					valueInMessage.append(value + " - ");
				}
				valueInMessage.delete(valueInMessage.length() - 3, valueInMessage.length());

				if (previousRecord != null)
				{
					if (values.equals(previousValues))
					{
						duplicates.add(previousRecord);
						duplicates.add(record);
					}
					else
					{
						reportErrors(
							pContext,
							fieldsInMessage,
							node,
							uniqueFieldsPaths,
							duplicates,
							values,
							valueInMessage);
						duplicates = new ArrayList<>();
					}
				}
				previousRecord = record;
				previousValues = values;
			}

			if (!duplicates.isEmpty())
			{
				reportErrors(
					pContext,
					fieldsInMessage,
					node,
					uniqueFieldsPaths,
					duplicates,
					values,
					valueInMessage);
			}
		}
		finally
		{
			result.close();
		}
	}

	private void reportErrors(
		final ValueContextForValidationOnTable pContext,
		StringBuilder fieldsInMessage,
		SchemaNode node,
		String[] uniqueFieldsPaths,
		List<Adaptation> duplicates,
		List<String> values,
		StringBuilder valueInMessage)
	{
		for (Adaptation duplicate : duplicates)
		{
			if (uniqueFieldsPaths.length == 1)
			{
				String message = "[" + fieldsInMessage + "] value '" + values.get(0)
					+ "' must be unique in the table.";
				pContext.addMessage(
					duplicate,
					node,
					AdaptationUtil.createUserMessage(
						message,
						getSeverityFromTableLevel(duplicate.createValueContext())));
			}
			else
			{
				String message = "Set of values [" + valueInMessage + "] from fields ["
					+ fieldsInMessage + "] must be unique in the table.";
				pContext.addMessage(
					duplicate,
					AdaptationUtil.createUserMessage(
						message,
						getSeverityFromTableLevel(duplicate.createValueContext())));
			}

		}
	}

	public String getFilter()
	{
		return filter;
	}

	public String getUniqueFields()
	{
		return uniqueFields;
	}

	public void setFilter(final String filter)
	{
		this.filter = filter;
	}

	public void setUniqueFields(final String uniqueFields)
	{
		this.uniqueFields = uniqueFields;
	}

	/**
	 * Get the severity associated with violations of this constraint.
	 * 
	 * @param recordContext the record's context
	 * @return the severity
	 */
	protected Severity getSeverity(ValueContext recordContext)
	{
		return defaultSeverity;
	}

	protected Severity getSeverityFromRecordLevel(ValueContext recordContext)
	{
		return (defaultRecordLevelSeverity != null ? defaultRecordLevelSeverity
			: getSeverity(recordContext));
	}

	protected Severity getSeverityFromTableLevel(ValueContext recordContext)
	{
		return getSeverity(recordContext);
	}

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

	public String getRecordLevelSeverity()
	{
		return recordLevelSeverity;
	}

	public void setRecordLevelSeverity(String recordLevelSeverity)
	{
		this.recordLevelSeverity = recordLevelSeverity;
	}

	@Override
	public void setup(final ConstraintContextOnTable arg0)
	{
		if (severity != null)
		{
			this.defaultSeverity = Severity.parseFlag(severity);
		}
		if (recordLevelSeverity != null)
		{
			this.defaultRecordLevelSeverity = Severity.parseFlag(recordLevelSeverity);
		}

	}

	@Override
	public String toUserDocumentation(final Locale arg0, final ValueContext arg1)
		throws InvalidSchemaException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
